package Diagramming_V2;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import javax.swing.*;

import javax.swing.JOptionPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class UserInterface {
	
	public static final String CREDENTIALS_FILENAME = "RTTCredentials.txt";
	private static final String CREDENTIALS_INPUT_MESSAGE = "Input RTT API Credentials (format username:password), \n"
	      		+ "E.g. rttapi_AlecC:9bc090e05b3f38eb6605943ce91b7e952313d39c";
	private static final String CREDENTIALS_ERROR_MESSAGE = "Could not change credentials. Program will exit";
	private static final String NO_FILE_MESSAGE = "File not found.";
	private static final String EMPTY_FILE_MESSAGE = "File has no lines";
	private static final int NO_OF_COLUMNS = 2;
	private static final String CHANGE_CREDENTIALS_MESSAGE =  "Would You Like to Change your credentials?";
	private static final String SERVICE_INPUT_MESSAGE = "Input Service UID";
	private static final String SERVICE_ERROR_MESSAGE = "Invalid service UID for the given date, please try again.";
	private static final String DATE_INPUT_MESSAGE = "Input service start date, in format YYYY-MM-DD";
	public static final String DATE_ERROR_MESSAGE = "Invalid date. Please try again.";
	private static final String EXTRA_INFO_TITLE = "Additional Information:";
	public static final String NEWLINE = "\n";
	
	/**
	 * Fills the frame horizontally if true
	 */
	final static boolean shouldFill = true;
    
	/**
	 * JFrame to present results to user - attribute as it needs to be disposed from separate procedure
	 */
    private JFrame frame = new JFrame("Diagramming Program");
    
    /**
     * Contains descriptions of all trains from the Diagram class
     */
    private ArrayList<String> descriptions;
    
    /**
     * Contains service UIDs of all services from the Diagram class
     */
    private ArrayList<String> serviceUIDs;
    
    /**
     * Contains information about splitting and joining trains from the Diagram class
     */
    private ArrayList<String> splits;

	public static void main(String[] args) {
		UserInterface ui = new UserInterface();
	}
	
	/**
	 * Validates if there are correct credentials for the API
	 * @return true is credentials are missing, false if not
	 */
	public static boolean credsMissing() {
    	try {
    		File myObj = new File(CREDENTIALS_FILENAME); //file with credentials
    	    Scanner myReader = new Scanner(myObj);
    	    String data = myReader.nextLine(); //gets first line
    	    if (data.startsWith("rttapi_")) { //all credentials should start with this
    	    	myReader.close(); //closes file
    	    	return false; //credentials not missing
    	    }
    	    myReader.close();
    	}catch (FileNotFoundException e) {
    	    //showMessage(NO_FILE_MESSAGE);
    	}catch (NoSuchElementException nsee) {
    		//showMessage(EMPTY_FILE_MESSAGE);
    	}catch (Exception e){
    		showMessage("Random Exception "+e.getMessage());
    	}
    	return true; //incorrect credentials or credentials missing; need to be re-entered
    }
	
	/**
	 * Re-writes Credentials file with credentials entered from user
	 */
	public static void changeCredentials() {
		try {
  	      FileWriter myWriter = new FileWriter(CREDENTIALS_FILENAME); //file with credentials
  	      myWriter.write(JOptionPane.showInputDialog(CREDENTIALS_INPUT_MESSAGE)); //gets creds from user
  	      myWriter.close();
  	    } catch (IOException e) {
  	    	showMessage(CREDENTIALS_ERROR_MESSAGE);
			System.exit(0);
  	    }
	}
	
	/**
	 * Creates new file with credentials entered by user
	 */
	public static void makeCredentials() {
    	try {
    		File myObj = new File(CREDENTIALS_FILENAME); //file with credentials
    	    if (myObj.createNewFile()) {}
    	} catch (IOException e) {
    		showMessage(CREDENTIALS_ERROR_MESSAGE);
    		System.exit(0);
    	}
    	try {
    	      FileWriter myWriter = new FileWriter(CREDENTIALS_FILENAME);
    	      myWriter.write(JOptionPane.showInputDialog(CREDENTIALS_INPUT_MESSAGE)); //gets creds from user
    	      myWriter.close();
    	    } catch (IOException e) {
    	    	showMessage(CREDENTIALS_ERROR_MESSAGE);
    	    	System.exit(0);
    	    }
    }
	
	/**
	 * Creates 2D String Array to present details in table
	 * @return 2D Array to present table in ScrollPane
	 */
	private String[][] getTable(){
		String[][] data = new String[descriptions.size()][NO_OF_COLUMNS]; //2 columns
		for (int i=0;i<descriptions.size();i++) { 
			String[] row = {serviceUIDs.get(i), descriptions.get(i)}; //creates array with info from same position of the 2 ArrayLists
			data[i] = row; //adds row to 2D Array
		}
		return data; //returns table
	}
	
	/**
	 * Adds components to JFrame
	 * @param pane the container the layout is being added to
	 */
	private void addComponentsToPane(Container pane) {
        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        if (shouldFill) {
        	c.fill = GridBagConstraints.HORIZONTAL; //fills the frame horizontally
        }
        
        String[][] data = getTable(); //gets 2D Array to hold table
        String[] columnNames = {"Service UID","Description"}; //names of columns for table, for display
        JTable toDoTable = new JTable(data, columnNames); //creates new table
	    JScrollPane scrollPane = new JScrollPane(toDoTable); //scrollpane including table - means scroll bar can be addedd
	    scrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED); //vertical scrollbar as needed
	    c.gridy = 0; //top element
	    pane.add(scrollPane, c);
	    
	    JButton button = new JButton("Click here to find another diagram"); //text on button
	    c.gridy = 1; //2nd element from top
	    pane.add(button, c);
	    button.addActionListener(event -> { anotherOne();}); //procedure when button is clicked
	    
	    JTextPane textPane1 = new JTextPane(); //to hold text
	    textPane1.setEditable(false); //user cannot type in
	    textPane1.setText(getSplitString()); //contents of splits ArrayList
	    StyledDocument doc = textPane1.getStyledDocument();
	    SimpleAttributeSet center = new SimpleAttributeSet();
	    StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER); //text centred in textpane
	    doc.setParagraphAttributes(0, doc.getLength(), center, false);
	    c.gridy = 2; //bottom element
	    pane.add(textPane1, c);
        
	}
	
	private void createAndShowGUI() {
        //Create and set up the window.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //program stops when X clicked
        
        //following code to centre JFrame on user's screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = screenSize.height;
        int width = screenSize.width;
        frame.setSize(width/2, height/2);
        frame.setLocationRelativeTo(null); 
        //Set up the content pane.
        addComponentsToPane(frame.getContentPane()); //adds the component

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
	
	/**
	 * Closes JFrame and creates another userinterface to diagram another service
	 */
	public void anotherOne() {
		frame.dispose(); //closes JFrame
		UserInterface ui = new UserInterface();
	}
	
	/**
	 * Converts ArrayList contents into String for display in textPane
	 * Each item in ArrayList is on a line
	 * @return
	 */
	public String getSplitString() {
		String text = EXTRA_INFO_TITLE+NEWLINE; //title for textPane (displayed at top)
		for (int i=0;i<splits.size();i++) {
			text = text + splits.get(i)+ NEWLINE; //puts next item on new line
		}
		return text;
	}
	
	/**
	 * Shows default message dialogue
	 * @param message String displayed in the message
	 */
	public static void showMessage(String message) {
		Component frame = null;
		JOptionPane.showMessageDialog(frame, message);
	}
	
	/**
	 * Tests validity of the given serviceUID on the given date
	 * @param serviceUID serviceUID being tested
	 * @param date valid date string in correct format for URL
	 * @return false if data invalid, true if no errors produced
	 */
	public boolean validUID(String serviceUID, String date) {
		RTTInfo gi = new RTTInfo(SimpleService.URL_START+serviceUID+"/"+date); //URL to test validity
		if (gi.getError()) { //if an error was generated trying to get the service information
			showMessage(SERVICE_ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	/**
	 * Interfaces with user and sends data entered to be diagrammed
	 * Contains most validation for user input
	 */
	public UserInterface() {
		if (credsMissing()) {
			makeCredentials();
		}else {
			int dialogButton = 0;
			int dialogResult = JOptionPane.showConfirmDialog (null, CHANGE_CREDENTIALS_MESSAGE,"Change Credentials?",dialogButton);
			if(dialogResult == JOptionPane.YES_OPTION){
				changeCredentials();
			}
			if (dialogResult == -1) {
				System.exit(0);
			}
		}
		String date = null;
		String dateString;
		String serviceUID = null;
		while (true) {
			while (true) {
				try {
					date = JOptionPane.showInputDialog(DATE_INPUT_MESSAGE);
					LocalDate tempDate = LocalDate.parse(date); //parses date to check validity
					DateTimeFormatter formatter; //declares formatter for making time into string again
					formatter = DateTimeFormatter.ofPattern(RTTInfo.RTT_DATE_FORMAT); //format used in URLs for RTT
					dateString = tempDate.format(formatter); //turns into String in correct format
					break;
				}catch (NullPointerException npe) {
					System.exit(0);
				}catch (Exception e) {
					Component frame = null;
					JOptionPane.showMessageDialog(frame, DATE_ERROR_MESSAGE);
				}
			}
			try {
				serviceUID = JOptionPane.showInputDialog(SERVICE_INPUT_MESSAGE);
				if (serviceUID.equals(null)) {
					System.exit(0);
				}
				serviceUID = serviceUID.toUpperCase();
				if (validUID(serviceUID, dateString)) {
					break; //no need for re-entry if date and UID are correct
				}
			}catch (NullPointerException npe) {
				System.exit(0);
			}
		}
		
		Diagram diagram = new Diagram(serviceUID, date);
		
		descriptions = diagram.getDescriptions();
		serviceUIDs = diagram.getServiceUIDs();
		splits = diagram.getSplits();
		
		createAndShowGUI();
	}
}

























