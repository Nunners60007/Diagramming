package Diagramming_V2;

import java.time.LocalTime;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SimpleLocation {

	public static void main(String[] args) throws ParseException {
		UserInterface ui = new UserInterface();
	}
	
	public static final String LOCATION_DETAIL_KEY = "locationDetail";
	public static final String PLATFORM_KEY = "platform";
	public static final String PLANNED_DEPARTURE_KEY = "gbttBookedDeparture";
	public static final String SERVICE_UID_KEY = "serviceUid";
	public static final String URL_START = "https://api.rtt.io/api/v1/json/search/";
	public static final String ARRIVALS = "/arrivals";
	public static final String SERVICES = "services";
	
	/**
	 * Attribute to indicate if there is an abscence of services at this location
	 */
	private boolean noService = false;
	
	/**
	 * Returns true if there are no service, false for services
	 * @return boolean indicating no services - true if there are no services, false if there are services
	 */
	public boolean ifNoServices() {
		return noService;
	}
	
	/**
	 * String containing JSON returned by API
	 */
	private String rawData;
	
	/**
	 * Contains headcode inString form, e.g. 2C49
	 */
	private String headcode;
	
	/**
	 * Returns headcode in format 2C35
	 * @return headcode if format 2C35
	 */
	public String getHeadcode() {
		return headcode;
	}
	
	/**
	 * Indicates if the service being sought has been found - used for nextTrainInfo
	 */
	private boolean found = false;
	
	/**
	 * Returns the attribute found to indicate if a service has been found
	 * @return found - if train has been found
	 */
	public boolean serviceFound() {
		return found;
	}
	
	/**
	 * Indicates if the time threshold for looking for a service has been reached - used for nextTrainInfo
	 */
	private boolean startFound = false;
	
	/**
	 * JSONArray containing the services at this location
	 */
	private JSONArray services;
	
	/**
	 * Holds the platform of a certain service at this location
	 */
	private String platform;
	
	/**
	 * JSONObject holding details about a service at this location, e.g. platform number, arrival time
	 */
	private JSONObject locations;
	
	/**
	 * Contains the TIPLOC ID of this location
	 */
	private String locationID;
	
	/**
	 * Contains the arrival time at destination of a service departing this location
	 */
	private String nextArrTime;
	
	/**
	 * Returns arrival time at destination of a service departing this location
	 * @return arrival time at destination of a service departing this location
	 */
	public String getNextArrTime() {
		return nextArrTime;
	}
	
	/**
	 * Parses arrival time at destination of a service departing this location into LocalTime and returns it
	 * @return arrival time at destination of a service departing this location as LocalTime
	 */
	public LocalTime getArrTime() {
		return Diagram.parsePlainTime(nextArrTime);
	}
	
	/**
	 * Contains the TIPLOC ID of the destination station of a service at this location
	 */
	private String destID;
	
	/**
	 * Returns TIPLOC ID of the destination station of a service at this location
	 * @return TIPLOC ID of the destination station of a service at this location
	 */
	public String getDestID() {
		return destID;
	}
	
	/**
	 * Holds name of destination station of a service at this location
	 */
	private String destName;
	
	/**
	 * Returns name of destination station of a service at this location
	 * @return name of destination station of a service at this location
	 */
	public String getDestName() {
		return destName;
	}
	
	/**
	 * Holds UID of a service at this location
	 */
	private String serviceUID;
	
	/**
	 * Returns UID of a service at this location
	 * @return UID of a service at this location
	 */
	public String getNextUID() {
		return serviceUID;
	}
	
	/**
	 * Holds departure time of a service at this location in a String, in format HHmm
	 */
	private String depTimeString;
	
	/**
	 * Returns departure time of a service at this location in a String, in format HHmm
	 * @return departure time of a service at this location in a String, in format HHmm
	 */
	public String getDepTimeString() {
		return depTimeString;
	}
	
	/**
	 * Parses departure time at of a service at this location into LocalTime and returns it
	 * @return departure time at of a service at this location as LocalTime
	 */
	public LocalTime getDepTime() {
		return Diagram.parsePlainTime(depTimeString);
	}
	
	/**
	 * Holds JSON Object containing information about the destination of a service at this location
	 */
	private JSONObject destinationJSON = null;
	
	/**
	 * Forms description with headcode (e.g.1A11), departure time (String) (HHmm), origin TIPLOC (this location) and destination TIPLOC
	 * @return description of service at this location
	 */
	public String getDescription() {
		return headcode+SimpleService.SPACE+depTimeString+SimpleService.SPACE+locationID+SimpleService.SPACE+destID;
	}
	
	/**
	 * Finds the platform of a service at this location, subject to it being the specified service
	 * Stores results in platform attribute
	 * @param service JSON Object containing information about a service at this location
	 * @param requiredServiceUID The service that the platform is being found for
	 */
	private void findPlat(JSONObject service, String requiredServiceUID) {
		String serviceUID = (String) service.get(SERVICE_UID_KEY); //gets service UID to check if it is correct
		if (serviceUID.equals(requiredServiceUID)) {
			JSONObject location = (JSONObject)service.get(LOCATION_DETAIL_KEY); //Holds details about the service at this location
			platform = (String) location.get(PLATFORM_KEY); //Sets platform attribute 
		}
	}
	
	/**
	 * Calls findPlat procedure for each service at this location
	 * @param serviceUID the service UID of the service to find the platform for
	 * @return platform of the service specified at this location
	 */
	public String getPlatform(String serviceUID) {
		services.forEach(service -> findPlat( (JSONObject) service, serviceUID) ); //finds platform of arriving service
		return platform;
		
	}
	
	/**
	 * Sets destination JSON Object to first destination
	 * @param destination JSON Object containing details about the destination of a service from this location
	 */
	private void handleDests(JSONObject destination) {
		if (destinationJSON == null) { //if no destination has been set yet
			destinationJSON = destination; //sets destination info to first destination 
		}
	}
	
	/**
	 * Cycles through every service at this location to find the first one that departs the specified platform
	 * after arrival time of a previous service
	 *
	 * @param platform platform service needs to depart from
	 * @param toc operator service needs to be operated by
	 */
	public void findNextTrain(String platform, String toc, LocalTime timeNeeded) {
		services.forEach(service -> nextTrainInfo( (JSONObject) service, platform, toc, timeNeeded) ); //gets information about next train from services
	}
	
	/**
	 * Checks a particular service to see if it meets the time, platform, and operator criteria
	 * @param service JSON Object containing information about the service
	 * @param platformNeeded platform the service must depart from
	 * @param toc operator of the service beingg searched for
	 * @param timeNeeded LocalTime containing threshold departure time for service
	 */
	private void nextTrainInfo(JSONObject service, String platformNeeded, String toc, LocalTime timeNeeded) {
		if (!found) { //if an applicable service has been found, code doesn't need to run
			String tocCheck = (String) service.get(SimpleService.TOC_KEY); //gets TOC of this service
			if (!tocCheck.equals(toc)) { //if the operators don't match, this service is not applicable
				return;
			}
			
			locations = (JSONObject)service.get(LOCATION_DETAIL_KEY); //information such as platform and dep time from this location for this service
			
			depTimeString = (String) locations.get(PLANNED_DEPARTURE_KEY); //gets (GBTT) departure time
			LocalTime searchTrainTime = Diagram.parsePlainTime(depTimeString); //parses to LocalTime for comparison
			if (searchTrainTime.isAfter(timeNeeded)) { //if time has passed threshold
				startFound = true; //the other code can run for services after this, as they are in chronological order
			}else if (!startFound) { //if start still not found, services not applicable
				return;
			}
			
			String platform = Diagram.PLAT_ERROR; //initialized platform String
			try {
				platform = (String) locations.get(PLATFORM_KEY); //gets platform of this service
				if (platform.equals(null)) { //if service has no platform
					return;
				}
			}catch (Exception e) {
				e.printStackTrace(); //if an error finding platform
				return;
			}
			
			if (platform.equals(platformNeeded)) { //if platform is the correct, one service has been found
				found = true; //correct service found
				
				//finding info about service
				
				headcode = (String) service.get(SimpleService.HEADCODE_KEY); //for user output
				serviceUID = (String) service.get(SERVICE_UID_KEY); //for identifying later and for user
				
				JSONArray dest = (JSONArray) locations.get(SimpleService.DEST_KEY); //gets destination info for all destinations
				dest.forEach(destination -> handleDests( (JSONObject) destination)); //sets destinationJSON to 1st destination
				
				nextArrTime = (String) destinationJSON.get(SimpleService.TIME_KEY); //gets arrival time at destination
				destID = (String) destinationJSON.get(SimpleService.LOCATION_ID); //gets TIPLOC ID of destination
			
				return;
			}
		}
	}
	
	/**
	 * Constructs Location, including getting information from RTT API
	 * @param inputLocationID This location's TIPLOC ID
	 * @param dateString Start date for searching for services
	 * @param arrivalTimeString time for searching for services
	 * @param arrivals boolean indicating whether searching for departures (false) or arrivals (true)
	 * @throws ParseException if JSON does not parse basic info correctly
	 */
	public SimpleLocation(String inputLocationID, String dateString, String arrivalTimeString, boolean arrivals) throws ParseException {
		String url = URL_START+inputLocationID+"/"+dateString+"/"+arrivalTimeString; //forming URL
		if (arrivals) {
			url = url + ARRIVALS; //sets URL to arrivals rather than departures (default)
		}
		locationID = inputLocationID; //sets ID attribute
		
		RTTInfo information = new RTTInfo(url); //gets data from API portal
		rawData = information.getData(); //retrieves data from class
		
		//parses to JSON object
		JSONObject jo = (JSONObject) new JSONParser().parse(rawData);
		services = (JSONArray) jo.get(SERVICES); //gets services from returned data (excluding station info)
		try {
			if (services.equals(null)) { //if there are no services
				noService = true;
				return;
			}
		}catch (Exception e) { //if there is an error
			noService = true;
			return;
		}	
	}
}
