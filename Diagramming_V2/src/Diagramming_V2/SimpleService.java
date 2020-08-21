package Diagramming_V2;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.time.LocalTime;
import java.util.ArrayList;

import org.json.simple.JSONArray;


/**
 * 
 * @author Alex Nunn
 * @author alexanderdgnunn@gmail.com
 * @version 1.0
 * @since 1.0
 *
 */
public class SimpleService {
	
	public void main(String[] args) throws ParseException {
		//Service j = new Service("https://api.rtt.io/api/v1/json/service/X22967/2020/07/08");
	}
	
	public static final String URL_START = "https://api.rtt.io/api/v1/json/service/";
	public static final String TIME_KEY = "publicTime";
	public static final String LOCATION_ID = "tiploc";
	public static final String DESCRIPTION_KEY = "description";
	public static final String LOCATION_NOTES = "associations";
	public static final String SPLIT_UID_KEY = "associatedUid";
	public static final String PLAN_ARR_KEY = "gbttBookedArrival";
	public static final String ACTUAL_ARR_KEY = "publicTime";
	public static final String JOIN_KEY = "join";
	public static final String DIVIDE_KEY = "divide";
	public static final String SPACE = " ";
	public static final String LOCATIONS_KEY = "locations";
	public static final String HEADCODE_KEY = "trainIdentity";
	public static final String TOC_KEY = "atocCode";
	public static final String DEST_KEY = "destination";
	public static final String ORIGIN_KEY = "origin";
	
	
	
	
	//private LocalDate splitRunDate;
	
	/**
	 * 
	 */
	private boolean error = false;;
	public boolean invalidService() {
		return error;
	}
	
	/** represents the unique identifier of the service for the given date
	 */
	private String serviceUID;
	
	/**
	 * returns the unique identifier of the service for the given data
	 * @return unique identifier of the service for the given date
	 */
	public String getServiceUid() {
		return serviceUID;
	}
	
	/** date that the service starts running, in format yyyy/mm/dd
	 */
	private String serviceRunDate;
	
	/**
	 * returns date that the service starts running, in format yyyy/mm/dd
	 * @return date that the service starts running, in format yyyy/mm/dd
	 */
	public String getServiceRunDate(){
		return serviceRunDate;
	}
	
	//------------------------------------------------------------------------------------
	
	/** Headcode of the service - identifies a service for every date that it runs. E.g. 1C04, 9T19
	 * Pureley for output to the user, not used within the program
	 */
	private String headcode;
	
	/**
	 * returns headcode of the service - identifies a service for every date that it runs. E.g. 1C04, 9T19
	 * @return headcode of the service - identifies a service for every date that it runs. E.g. 1C04, 9T19
	 */
	public String getHeadcode() {
		return headcode;
	}
	
	/** ArrayList of arrival times - Holds all arrival times of the service, in string form, e.g. 1247
	 */
	private ArrayList<String> arrTimes = new ArrayList<>();
	
	/**
	 * Returns first item of (MAIN arrival time) ArrayList of arrival times - Holds all arrival times of the service, in string form, e.g. 1247
	 * @return first item of ArrayList of arrival times - Holds all arrival times of the service, in string form, e.g. 1247
	 */
	public String getArrTime() {
		return arrTimes.get(0);
	}
	
	/** ArrayList of destination names - Holds all names of destinations of the service
	 */
	private ArrayList<String> destNames = new ArrayList<>();
	
	/** Returns first item of ArrayList of destination names - main destination of the service
	 * @return first item of ArrayList of destination names - main destination of the service
	 */
	public String getDestName() {
		return destNames.get(0);
	}
	
	/**
	 * Returns number of destinations (size of destNames ArrayList)
	 * @return number of destinations (size of destNames ArrayList)
	 */
	public int getNoDests() {
		return destNames.size();
	}
	
	/** ArrayList of destination IDs - Holds all TIPLOCs of destinations of the service
	 */
	private ArrayList<String> destIDs = new ArrayList<>();
	
	/**
	 * Returns first item of ArrayList of destination IDs - TIPLOC of main destination of the service
	 * @return first item of ArrayList of destination IDs - TIPLOC of main destination of the service
	 */
	public String getDestID() {
		return destIDs.get(0);
	}
	
	/**
	 * Holds 2-letter code of train operating company operating the service, e.g. SN for Southern, GC for Grand Central
	 */
	private String toc;
	
	/**
	 * Returns 2-letter code of train operating company operating the service, e.g. SN for Southern, GC for Grand Central
	 * @return 2-letter code of train operating company operating the service, e.g. SN for Southern, GC for Grand Central
	 */
	public String getToc() {
		return toc;
	}
	
	//-------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Holds indication of whether the service divides en-route
	 */
	private boolean divide = false;
	
	/**
	 * Returns indication of whether the service divides en-route
	 * @return indication of whether the service divides en-route
	 */
	public boolean getDivide() {
		return divide;
	}
	
	/**
	 * Holds indication of whether the service joins another en-route
	 */
	private boolean join = false;
	
	/**
	 * Returns indication of whether the service joins another en-route
	 * @return indication of whether the service joins another en-route
	 */
	public boolean getJoin() {
		return join;
	}
	
	//-------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Holds Unique Identifier for the given data of a service that splits from this one, or a service that this one joins
	 */
	private String splitUID; //UID of train that splits from this service, or that this service joins
	
	/**
	 * Returns Unique Identifier for the given data of a service that splits from this one, or a service that this one joins
	 * @return Unique Identifier for the given data of a service that splits from this one, or a service that this one joins
	 */
	public String getSplitID() {
		return splitUID;
	}
	
	/**
	 * Holds JSON String containing information about the service, fetched from RealTimeTrains Pull API
	 * All information except UID and Date of running is derived from this
	 */
	private String serviceInfo;
	
	/**
	 * Holds all departure times of the service
	 */
	private ArrayList<String> depTimes = new ArrayList<>();
	
	public String getDepTime() {
		return depTimes.get(0);
	}
	
	public LocalTime getDepTime2() {
		return Diagram.parsePlainTime(depTimes.get(0));
	}
	
	/**
	 * Holds all names of origins of the service
	 */
	private ArrayList<String> originNames = new ArrayList<>();
	
	/**
	 * Holds all TIPLOCs of origins of the service
	 */
	private ArrayList<String> originIDs = new ArrayList<>();
	
	/**
	 * Holds index of location currently being examined for splitting or joining
	 */
	private int locationIndex;
	
	/**
	 * Holds number of locations called at in the service
	 * Calculated by finding size of JSONArray conatining locations
	 */
	private int noLocations;
	
	/**
	 * Holds TIPLOC ID of the last location listed in the service
	 */
	private String actualDestID;
	
	/**
	 * Returns TIPLOC ID of the last location listed in the service
	 * @return TIPLOC ID of the last location listed in the service
	 */
	public String getActualDest() { 
		return actualDestID;
	}
	
	/**
	 * Holds arrival time at the last location listed in the service, in String format, e.g. 1247
	 */
	private String actualATime;

	/**
	 * Returns arrival time at the last location listed in the service, in String format, e.g. 1247
	 * @return arrival time at the last location listed in the service, in String format, e.g. 1247
	 */
	public String getActualArrival() {
		return actualATime;
	}
	
	//----------------------------------------------------------------------------------------------------------------
	
	/**
	 * Adds information about a destination to the relevant ArrayList (arrival time, name, and TIPLOC)]
	 * Time e.g. 1315 (String)
	 * @param dest JSON Object containing information about the destination, such as arrival time, destination name, and TIPLOC ID
	 */
	private void getDestinations(JSONObject dest) {
		arrTimes.add((String) dest.get(TIME_KEY));
		destNames.add((String) dest.get(DESCRIPTION_KEY));
		destIDs.add((String) dest.get(LOCATION_ID));
	}
	
	/**
	 * Adds information about an origin to the relevant ArrayList (departure time, name and TIPLOC)
	 * Time e.g. 1315 (String)
	 * @param origin a JSON Object containing information about the origin, e.g. name and departure time
	 */
	private void getOrigins(JSONObject origin) {
		depTimes.add((String) origin.get(TIME_KEY));
		originNames.add((String) origin.get(DESCRIPTION_KEY));
		originIDs.add((String) origin.get(LOCATION_ID));
	}
	
	/**
	 * Calls methods to get any associations of the location, e.g. the UID of a splitting train
	 * @param location JSON Object with information about a calling/passing point on the service
	 */
	private void getNotes(JSONObject location) {
		try { //in case there is a problem with getting a location TIPLOC or its assocaitions
			String locationTIPLOC = (String) location.get(LOCATION_ID);
			
			JSONArray notes = (JSONArray) location.get(LOCATION_NOTES);
			notes.forEach(note -> getSplitID( (JSONObject ) note, locationTIPLOC)); //gets SplitUID and adjusts Join/Divide attributes accordingly
			
			locationIndex = locationIndex+1; //increments the number of locations
			
		}catch (Exception e) {
			locationIndex = locationIndex+1; //increments the number of locations
		}
		
	}
	
	/**
	 * Called by getNotes in order to analyse the association of a location to retrieve the UID of a splitting/joining train
	 * @param association JSON Object with information about associations
	 * @param tiploc TIPLOC ID of location being analysed, to be used to inform user where train splits
	 */
	//TODO add info about where train splits
	private void getSplitID(JSONObject association, String tiploc) {
		try {
			splitUID = (String) association.get(SPLIT_UID_KEY);
			if (locationIndex!=noLocations-1) { //trains only join another at the last station, so any other case is an incorrect join
				join = false;
			}
			if (locationIndex==0) {//trains do not divide at first station, only at intermediate stations
				divide = false;
			}
		}catch (Exception e) { //there is no associated UID
			
		}
	}
	
	/**
	 * Gets information about the actual destination of a train
	 * Where trains join, the destination is not where the train joins the other
	 * This finds the location where the train joins the other
	 * @param location JSON Object describing a passing/calling point on the service
	 */
	private void getActualDest(JSONObject location) {
		actualDestID = (String) location.get(LOCATION_ID); 
		try {
			actualATime = (String) location.get(ACTUAL_ARR_KEY);
		}catch (Exception e) {
			actualATime = (String) location.get(PLAN_ARR_KEY);
		}
	}
	
	/**
	 * Checks index of divide in the service
	 * If there is no divide, no further checks are needed. 
	 * If there is a divide, other methods are called to get divide information
	 */
	private void checkDivide() {
		int divideIndex = serviceInfo.indexOf(DIVIDE_KEY);
		if (divideIndex==-1) {
			divide = false;
		}else {
			divide = true;
		}
	}
	
	/**
	 * Checks index of join in the service
	 * If there is no join, no further checks are needed. 
	 * If there is a join, other methods are called to get join information
	 */
	private void checkJoin() {
		int joinIndex = serviceInfo.indexOf(JOIN_KEY);
		if (joinIndex==-1) {
			join = false;
		}else {
			join = true;
		}
	}
	
	/**
	 * Assembles and returns a description of the service (Headcode, Start Time, Origin and Destination)
	 * Origin and Destination are TIPLOC IDs, and are the 1st (main) Origin/Destination in the ArrayLists
	 * Start Time is a String, e.g. 1544
	 * @return String describing the service (Headcode, Start Time, Origin and Destination
	 */
	public String getDescription() {
		return headcode+SPACE+depTimes.get(0)+SPACE+originIDs.get(0)+SPACE+destIDs.get(0);
	}
	
	/**
	 * Calls the join and divide checks to indicate whether further checks are needed
	 * @return boolean indicating index of split or join in the service information
	 */
	public boolean checkSplitJoin() {
		checkDivide();
		checkJoin();
		if (join||divide) {
			return true;
		}
		return false;
	}
	
	/**
	 * Alternative builder method for finding the actual destination of a service
	 * For when the service joins another and the joining point is not listed as the destination
	 */
	public void buildForDest() {
		//parses to JSON object
		Object obj;
		try {
			obj = new JSONParser().parse(serviceInfo);
			JSONObject jo = (JSONObject) obj;
			JSONArray locations = (JSONArray) jo.get(LOCATIONS_KEY);
			noLocations = locations.size();
			locations.forEach(location -> getActualDest( (JSONObject ) location));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Main builder method of the service
	 * Checks join and divide, to indicate if further checks are needed
	 * Gets TOC and headcode, for checks and output respectively
	 * Calls getDestinations and getOrigins to fill ArrayLists with information about origins and destinations
	 * Calls getNotes to find split UIDs of trains if there is a join or divide indicated in the service inforamtion
	 * @throws ParseException
	 */
	public void build() throws ParseException {
		checkDivide(); //simple index of divide in serviceInfo
		checkJoin(); //simple index of join in serviceInfo
		
		try {
			
			//parses to JSON object
			Object obj = new JSONParser().parse(serviceInfo);
			JSONObject jo = (JSONObject) obj;
			
			headcode = (String) jo.get(HEADCODE_KEY); //for output
			toc = (String) jo.get(TOC_KEY); //for checks
		
			JSONArray destination = (JSONArray) jo.get(DEST_KEY);
			destination.forEach(dest -> getDestinations( (JSONObject ) dest)); //assembles destination information
			
			JSONArray origins = (JSONArray) jo.get(ORIGIN_KEY);
			origins.forEach(origin -> getOrigins( (JSONObject ) origin)); //assembles origin information
					
			if (divide==true||join==true) { //if the words "join" or "divide" exist in the service Information
				locationIndex = 0; //resets to count number of locations
				JSONArray locations = (JSONArray) jo.get(LOCATIONS_KEY);
				noLocations = locations.size();
				//check associations
				locations.forEach(loc -> getNotes( (JSONObject ) loc));
				
			}
		}catch (NullPointerException npe){
			 UserInterface.showMessage(Diagram.TRAIN_NOT_FOUND);
			error = true;
		}
	}
	
	/**
	 * Forms the URL to query the RealTimeTrain Pull API
	 * Checks that the date includes a slash, for a basic format check
	 * @param dateString the start date that the service runs on, format yyyy/mm/dd
	 * @param serviceUID the UID of the service on the given date
	 * @return the URL that RealTimeTrains will be queried with
	 */
	private String formServiceURL(String dateString, String serviceUID) {
		if (dateString.indexOf("/")!=-1) {
			String returnedURL = URL_START+serviceUID+"/"+dateString;
			return returnedURL;
		}
		return Diagram.PLAT_ERROR;//if there is an error in the date format - must include /
	}
	
	//--------------------------------------------------------------------------------------
	
	/**
	 * Sets service UID of service, resets number of locations
	 * Calls formServiceURL and gets information from RealTimeTrains via getInfo
	 * JSON file from RealTimeTrains set to serviceInfo attribute
	 * @param dateString
	 * @param serviceUID1
	 * @throws ParseException
	 */
	public SimpleService(String dateString, String serviceUID1) throws ParseException {
		serviceUID = serviceUID1;
		noLocations = 0;
		
		String url = formServiceURL(dateString, serviceUID);
		
		//gets string from getInfo object
		RTTInfo information = new RTTInfo(url);
		if (information.getError()) {
			error = true;
		}
		serviceInfo = information.getData();
	}

}
