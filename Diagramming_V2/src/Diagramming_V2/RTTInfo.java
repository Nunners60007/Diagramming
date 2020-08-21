package Diagramming_V2;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner; // Import the Scanner class to read text files


public class RTTInfo {
	
	private static final String RTT_ERROR = "{\"error\":\"No schedule found\"}";
	private static final String ERROR_MESSAGE = "Cannot access RealTimeTrains API. Please check credentials or internet connection";
	private static final String CREDENTIALS_ERROR = "Could not find credentials";
	public static final String RTT_DATE_FORMAT = "yyyy/MM/dd";
	
	/**
	 * Contains indication of whether retrieving information generate an error
	 */
	private boolean error = false;
	
	/**
	 * Returns an indication of whether retrieving information generate an error
	 * @return indication of whether retrieving information generate an error
	 */
	public boolean getError() {
		return error;
	}
	
	/**
	 * Holds String returned by RTT API
	 */
	private String data;
	
	/**
	 * Returns String returned by RTT API
	 * @return String returned by RTT API
	 */
	public String getData() {
		return data;
	}
	
	/**
	 * Holds user credentials for accessing RTT API
	 */
	private static String credentials = null;
	

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
    	RTTInfo gi = new RTTInfo("https://api.rtt.io/api/v1/json/service/X10388/2020/08/15");
    	//getInfo gi = new getInfo("https://api.rtt.io/api/v1/json/search/BOG/2020/08/16/0019/arrivals");
    	String output = gi.getData();
    	System.out.println(output);
    }
    
    /**
     * Fetches credentials from RTTCredentials.txt, which is created and written to in userInterface class
     * @return user credentials, e.g. rttapi_AlecC:9bc090e05b3f38eb6605943ce91b7e952313d39c
     */
    public String getCredentials() {
    	try {
    	      File myObj = new File(UserInterface.CREDENTIALS_FILENAME);
    	      @SuppressWarnings("resource")
			Scanner myReader = new Scanner(myObj);
    	      while (myReader.hasNextLine()) {
    	        String data = myReader.nextLine();
    	        return data;
    	      }
    	      myReader.close();
    	    } catch (FileNotFoundException e) {
    	     UserInterface.showMessage(CREDENTIALS_ERROR);
    	      e.printStackTrace();
    	    }
    	
    	return "";
    }
    
    /**
     * Fetches last line of response from queried page
     * Fetches credentials from RTTCredentials.txt file to send for authorization (if not done already)
     * @param urlString String that is queried
     */
    public RTTInfo(String urlString){
    	if (credentials==null) {
    		credentials = getCredentials();
    	}
    	 try {
             URL url = new URL (urlString);
             String encoding = Base64.getEncoder().encodeToString(credentials.getBytes("utf-8"));
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.setRequestProperty  ("Authorization", "Basic " + encoding);
             InputStream content = (InputStream)connection.getInputStream();
             BufferedReader in   = 
                 new BufferedReader (new InputStreamReader (content));
             String line;
             while ((line = in.readLine()) != null) {
                 data = line;
                 if (data==null||data.equals(RTT_ERROR)) {
                	 System.out.println(urlString);
                	 error = true;
                 }
             }
         } catch(Exception e) {
        	 error = true;
        	 UserInterface.showMessage(ERROR_MESSAGE);
             e.printStackTrace();
         }

     }
}
        



