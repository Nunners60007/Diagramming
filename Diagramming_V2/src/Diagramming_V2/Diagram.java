package Diagramming_V2;

import org.json.simple.parser.ParseException;
import java.time.*;
import java.time.format.*;
import java.util.ArrayList;

/**
 * Contains several services that one train serves
 * @author Alex Nunn
 * @author alexanderdgnunn@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Diagram {

	public void main(String[] args) {
		String serviceUID = "L81534";
		startDiagram(serviceUID);
		try {
			findDiagram(serviceUID);
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}
//----------------------------------------------------------------------------------------------------	
	
	public static final String JOINS = " joins with ";
	public static final String DIVIDES = " divides to form ";
	public static final String PLAT_ERROR = "Error";
	public static final String TRAIN_NOT_FOUND = "Error finding service";
	
	
	/**
	 * Holds current diagram date in String format for use in URLs
	 */
	public String dateString; //Date in correct format for use in URLs
	
	/**
	 * Holds current diagram date in LocalDate format for use in calculations involving dates
	 * E.g. Comparing dates or for going to the next day
	 */
	private LocalDate date; //Date in format best for calculation etc. 
	
//------------------------------------------------------------------------------------------------------------
	
	//the following 4 variables are used to transfer from starting the diagram
	//to the main procedure
	
	/**
	 * Holds the TIPLOC ID of the destination station in String format, for use in URLs
	 */
	private String destStationID; //TIPLOC ID of the  destination station, for starting the main sequence, used in URL
	
	/**
	 * Holds (public) time of arrival of any service at the destination, to the nearest minute, in LocalTime format
	 */
	private LocalTime arrivalTime;
	
	/**
	 * Holds (public) time of arrival of any service at the destination, to the nearest minute, in String format
	 */
	private String arrivalTimeString;//time of arrival of the service at destination station
	
	/**
	 * Indicates whether the service joins another en-route
	 */
	private boolean joinIndex; //indicates whether the service joins another or not
	
//-----------------------------------------------------------------------------------------------------------------	
	
	/**
	 * Train Operating Company operating the diagram
	 * Same for all services on the diagram
	 * Used to provide a check when finding the next service from a platform in the Location class
	 */
	private String toc; //this stays the same for all services on the diagram - 
	//used to provide a check where an ECS move has been provided or trains stacked on platform
	
//-----------------------------------------------------------------------------------------------------------------
	
	/**
	 * Holds UIDs of services split from the main diagram that the algorithm will need to be run for from the start
	 */
	private ArrayList<String> diagramStartUIDs = new ArrayList<String>(); //contains UIDs of trains that will need to be diagrammed
	//as they have split from the main diagram
	
	/**
	 * Holds UIDs of trains in the same diagram
	 */
	private ArrayList<String> diagramUIDs = new ArrayList<String>(); //contains UIDs of trains in the same diagram
	public ArrayList<String> getServiceUIDs(){
		return diagramUIDs;
	}
	
	/**
	 * Contains descriptions of trains in the diagram, in format start time, headcode, origin TIPLOC, destination TIPLOC
	 */
	private ArrayList<String> descriptions = new ArrayList<String>();//contains descriptions of trains in the diagram
	//descriptions have format headcode, start time, origin TIPLOC, destination TIPLOC
	
	public ArrayList<String> getDescriptions(){
		return descriptions;
	}
	
	private ArrayList<String> splits = new ArrayList<String>();
	public ArrayList<String> getSplits(){
		return splits;
	}
	
//----------------------------------------------------------------------------------------------------------------
	
	//starts diagram
	//gets TOC for handling errors with ECS, stacking trains
	/**
	 * Finds the first service specified and find information about its destination station
	 * Finds description of service, destination TIPLOC, arrival Time (as String and LocalTime) and any train that split
	 * Does not used Location class
	 * If train joins another, join index set to true and diagram will end
	 * @param serviceUID UID of the service forming the first service of the diagram
	 */
	public boolean startDiagram(String serviceUID) { 
		
		try { //try-catch statement needed for calling the new object - it can throw a parse exception from parsing JSON

			SimpleService service = new SimpleService //calling service JSON to object to get service info
					(dateString, serviceUID);
			
			service.build();
			if (service.invalidService()) {
				return true;
			}
			descriptions.add(service.getDescription());
			
			joinIndex = service.getJoin(); //gets join - if service joins another then the diagram will need to stop
			//and not progress to the findDiagram method
			
			if (service.getDivide()||joinIndex) {
				
				diagramStartUIDs.add(service.getSplitID());
				diagramUIDs.add(service.getSplitID());
				if (joinIndex) {
					splits.add(serviceUID+JOINS+service.getSplitID()); //output for user
					return false;
				}else {
					splits.add(serviceUID+DIVIDES+ service.getSplitID()); //output for user
				}
			}
			
			destStationID = service.getDestID(); //gets TIPLOC ID of destination station, used in location URL
			arrivalTimeString = service.getArrTime(); //gets arrival time, for user and for location URL
			toc = service.getToc(); //gets TOC for checking...
			
			//arrival time at destination, in easily comparable format
			arrivalTime = parsePlainTime(arrivalTimeString);
			checkDateChange(service.getDepTime2(), arrivalTime); //checks to see if the next day has been entered	
		} catch (ParseException e) { //if JSON parsing goes wrong - should not happen, error is problem with RTT API
			e.printStackTrace(); //stack trace for debugging
		}
		return false;
	}
	
	/**
	 * Checks to see whether arrTime is before depTime
	 * If true, adds 1 to the date and dateString
	 * @param depTime 
	 * @param arrTime
	 */
	public void checkDateChange(LocalTime depTime, LocalTime arrTime) {
		if (arrTime.isBefore(depTime)) {
			date = date.plusDays(1);
			DateTimeFormatter formatter; //declares formatter for making time into string again
			formatter = DateTimeFormatter.ofPattern(RTTInfo.RTT_DATE_FORMAT); //format used in URLs for RTT
			dateString = date.format(formatter); //turns into String in correct format
		}
	}

//---------------------------------------------------------------------------------------------------------------	

	/**
	 * Finds the diagram of the service from information about the first train
	 * Loops until no TOC service from same platform in 1 hour
	 * Adds any splitting services/services that join to ArrayList for services to start a diagram for
	 * @param serviceUID UID of the previous service
	 * @throws ParseException
	 */
	public void findDiagram(String serviceUID) //service ID of service that was used in start - used to find prev. service
			throws ParseException { //instead of surrounding with try-catch, exception very unlikely to happen
		
		SimpleLocation arrivalObject = new SimpleLocation //to find the previous train in the search results
				//and find the platform
				(destStationID, dateString, arrivalTimeString, true);
		
		while (true) { //loops until no TOC service from same platform in 1 hour
			String platformNeeded = arrivalObject.getPlatform(serviceUID); //gets platform of previous service so it can be used
			//in next instance of the object
			try {
						
				if (platformNeeded.equals(PLAT_ERROR)) { //if no platform is found for the preceding service
					//try again by finding the actual last destination of the service, which will be different if the service has a join
					
					SimpleService prevService = new SimpleService(dateString, serviceUID);
					prevService.buildForDest();
					destStationID = prevService.getActualDest();
					arrivalTimeString = prevService.getActualArrival();
					arrivalTime = parsePlainTime(arrivalTimeString);
					
					arrivalObject = new SimpleLocation (destStationID, dateString, arrivalTimeString, true); //arrivals to destination station
					
					platformNeeded = arrivalObject.getPlatform(serviceUID); //gets platform of previous service so it can be used
					//in next instance of the object
					if (platformNeeded.equals(PLAT_ERROR)){
						 UserInterface.showMessage(TRAIN_NOT_FOUND);
						break;
					}
				}
			}catch (NullPointerException npe) {
				 UserInterface.showMessage("No platform found at "+destStationID + " at "+arrivalTimeString + " for "+serviceUID);
				break;
			}
			
			SimpleLocation depObject = new SimpleLocation (destStationID, dateString, arrivalTimeString, false); //departures from destination station
			if (depObject.ifNoServices()) { //if no services are found, findNextTrain will create an error so
				break;
			}
			depObject.findNextTrain(platformNeeded, toc, parsePlainTime(arrivalTimeString)); //finds next station, service UID and arrival time of next service in diagram
			
			//to find if diagram has ended - if no train with same TOC/platform in 1 hour
			if (!depObject.serviceFound()) { //if diagram has ended
				break; //ends while loop, variable will not be retrieveed
			}
			
			String nextStationID = depObject.getDestID(); //TIPLOC ID of destination, for search with URL
			arrivalTimeString = depObject.getNextArrTime(); //arrival time of previous train at destination, in string for URL
			String nextServiceUID = depObject.getNextUID(); //for finding next train in search
			serviceUID = nextServiceUID;
			
			//TOC does not change so no need to reset
			
			diagramUIDs.add(nextServiceUID); //adds service UID to ArrayList for output
			descriptions.add(depObject.getDescription()); //adds service description to ArrayList for output
		
			SimpleService nextService = new SimpleService(dateString, nextServiceUID); //creates next service for checking splitting/joining
			boolean splitJoin = nextService.checkSplitJoin(); //checks splitting/joining
			
			if (splitJoin) {
				nextService.build(); //service needs to be build to get details about splitting and joining
				if (nextService.getJoin()) {
					splits.add(nextServiceUID+JOINS+nextService.getSplitID()); //output for user
					diagramStartUIDs.add(nextService.getSplitID()); //adds for starting next service
					diagramUIDs.add(nextService.getSplitID()); //adds to diagram UIDs
					break;
				}
				boolean divide = nextService.getDivide();
				if (divide) {
					splits.add(nextServiceUID+DIVIDES+ nextService.getSplitID()); //output for user
					diagramStartUIDs.add(nextService.getSplitID()); //adds for starting next service
					diagramUIDs.add(nextService.getSplitID()); //adds to diagram UIDs
				}
			}
			checkDateChange(arrivalTime, depObject.getDepTime());
			checkDateChange(depObject.getDepTime(), depObject.getArrTime()); //checks to see if we have entered the next day
			//time in easy format for comparison, not used in URL
			arrivalTime = parsePlainTime(arrivalTimeString);
			arrivalObject = new SimpleLocation(nextStationID, dateString, arrivalTimeString, true); //to find platform for next train
			destStationID = nextStationID;
		}
	}
	
//------------------------------------------------------------------------------------------------------------
	
	
	/** 
	 * Parses from String to LocalTime for Strings with a colon between hours and minutes
	 * @param timeWithoutColon time in format HHmm
	 * @return LocalTime
	 */
	public static LocalTime parsePlainTime(String timeWithoutColon) {
		return LocalTime.parse(timeWithoutColon.substring(0, 2)+":"+timeWithoutColon.substring(2));
	}
	
//------------------------------------------------------------------------------------------------------------
	
	/**
	 * Takes date from String format from user to usable formats into String and localDate formats
	 * @param dateToParse date in String form from user
	 */
	private void handleDate(String dateToParse) { 
		try {
			date = LocalDate.parse(dateToParse); //parses date
			DateTimeFormatter formatter; //declares formatter for making time into string again
			formatter = DateTimeFormatter.ofPattern(RTTInfo.RTT_DATE_FORMAT); //format used in URLs for RTT
			dateString = date.format(formatter); //turns into String in correct format
		}catch (Exception e) {
			 UserInterface.showMessage(UserInterface.DATE_ERROR_MESSAGE);
		}
	}
	
//---------------------------------------------------------------------------------------------------------------
	
	/**
	 * Series of train services that a single train makes
	 * @param serviceUID first service in diagram
	 * @param dateToParse starting date of diagram
	 */
	public Diagram(String serviceUID, String dateToParse) {
		diagramStartUIDs.add(serviceUID); //Adds first service UID to ArrayList of services to start diagram
		diagramUIDs.add(serviceUID);
		
		while ((diagramStartUIDs.size())>0) { //while there are more train IDs to calculate diagrams for
			handleDate(dateToParse); //turns date into usuable formats
			boolean error = startDiagram(diagramStartUIDs.get(0)); //gets info about first train in diagram
			if (error) {
				break;
			}
			serviceUID = diagramStartUIDs.get(0);
			diagramStartUIDs.remove(0);//removed as it is being used
			
			try {
				if (!joinIndex) {
					findDiagram(serviceUID); //main sequence, uses attribute vars
				}
			} catch (ParseException e) { //if JSON parses incorrectly
				e.printStackTrace(); //for debugging
			}
		}
	}
}





















