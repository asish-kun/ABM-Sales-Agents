package model;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.graphstream.graph.Graph;

import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSourceDGS;

import util.CSVReader4Leads;
import view.LeadData;


/**
 * 
 * PARAMETERS FOR THE ABM4SALES MODEL 

	Handling missing properties			https://commons.apache.org/proper/commons-configuration/userguide/howto_basicfeatures.html
				
 * @author mchica
 * @date 2022/09/22
 * @place Granada
 *
 */

public class ModelParameters {

	// Read configuration file (apache commons configuration 2.7)
	Configuration config;
	
	// constant with the name of the model
	public final static String modelName = "ABM4Sales"; 
	
	// constant with the date of the model
	public final static String modelDate = "April 2024"; 
	
	public static boolean DEBUG = false;

	// outputing extra information at the end of the simulation
	public static boolean OUTPUT_SALESPEOPLE_INFO = true;
	
	// constants to get different options for simple or SA runs
	public final static byte NO_SA = 0;
	public final static byte SA_avgRiskAversion = 1;
	public final static byte SA_thresholdForConversion_decayRateLeads = 2;
	public final static byte SA_avgRiskAversion_stdevRiskAversion = 3;
	public final static byte SA_socialComparison= 4;
	public final static byte SA_quota = 5;
	public final static byte SA_avgLeadsMagnitude= 6;
	public final static byte SA_stdevLeadsMagnitude = 7;

	public final static byte WORKINGHOURSPERWEEK = 40;
	
	public final static float percForPay = (float) 0.2;   // % of the sales revenue going to the pay
	
	public final static float MIN_MAGNITUDE = 0;
	public final static float MAX_MAGNITUDE = 1;
	
	public final static byte NO_NETWORK = -1;
	public final static byte NETWORK = 0;
	public final static byte NORMAL_LEADS_MAGNITUDE = 1;
	public final static byte UNIFORM_LEADS_MAGNITUDE = 0;

	public final static byte LEAD_IS_WON = 1;
	public final static byte LEAD_IS_LOST = 0;
	public final static byte LEAD_UNKNOWN_FINAL_STATUS = -1;
	
	public final static boolean LQ = false;	// if also averaging last quartile of stats and store in a file

	
	// ########################################################################
	// Variables
	// ########################################################################

	String outputFile;

	// modified just to use a static SN from a file

	// FILE_NETWORK ONLY!!
	boolean network;				// true if having a structured network such as SF or Lattice (read from file). false if we do not have it and it is a WM

	// graph read from file
	Graph graphFromFile;

	// LEADS READ FROM FILE
	List<LeadData> dataOfLeads;			// the leads read from a file


	// --------------------------- Wrapper methods for Config ---------------------------//
	//
	
	/**
	 * This function add a new parameter to the map of parameters of the model
	 * 
	 * @param _parameterKey is the key of the new parameter to create
	 * @param _value is the value to set to the parameter
	 */
	public void addParameter (String _parameterKey, Object _value) {		
		this.config.addProperty(_parameterKey, _value);		
	}
	
	/**
	 * This function set an existing parameter to the map of parameters of the model
	 * 
	 * @param _parameterKey is the key of the existing parameter to set
	 * @param _value is the value to set to the parameter
	 */
	public void setParameterValue (String _parameterKey, Object _value) {		
		this.config.setProperty(_parameterKey, _value);		
	}
		
	/**
	 * This function retrieve a parameter of type int from the configuration structure of parameters
	 */
	public int getIntParameter (String _parameterKey) {		
		
		return this.config.getInt(_parameterKey);		
		
	}

	/**
	 * This function retrieve a parameter of type float from the configuration structure of parameters
	 */
	public float getFloatParameter (String _parameterKey) {		
		
		return this.config.getFloat (_parameterKey);		
		
	}
	
	/**
	 * This function retrieve a parameter of type long from the configuration structure of parameters
	 */
	public long getLongParameter (String _parameterKey) {		
		
		return this.config.getLong (_parameterKey);		
		
	}

	/**
	 * This function retrieve a parameter of type String from the configuration structure of parameters
	 */
	public String getStringParameter (String _parameterKey) {		
		
		return this.config.getString (_parameterKey);		
		
	}
	

	/**
	 * This function returns true if there is a parameter loaded with this key
	 */
	public boolean isParameterSet (String _parameterKey) {		
		
		return config.containsKey(_parameterKey);		
		
	}
	
	// --------------------------- Methods for SN configuration ---------------------------//
	//
		
	
	/**
	* @return the value of having a network or not
	*/
	public boolean getNetworkOption () {
		return this.network;
	}
	
	/**
	* @param _v if having a network from file
	*/
	public void setNetworkOption (boolean _v) {
		this.network = _v;
	}
	

	/**
	 * @return the graph
	 */
	public Graph getGraph () {
		return graphFromFile;
	}

	/**
	 * @param _graph
	 *            to set
	 */
	public void setGraph (Graph _graph) {
		this.graphFromFile = _graph;
	}


	/**
//	 * @param _graph
	 *            to set
	 * @throws IOException
	 */
	public void readGraphFromFile(String fileNameGraph) throws IOException {

		FileSourceDGS fileSource = new FileSourceDGS();
		graphFromFile = new SingleGraph("SNFromFile");

		fileSource.addSink(graphFromFile);
		fileSource.readAll(fileNameGraph);

		fileSource.removeSink(graphFromFile);		
	
	}
	
	// ---------------- METHODS FOR PARSING THE LEADS FILE

	/* 
	 * This function get a random element from the list of leads and returns it
	 * @param _randomIndex the random value to get its position from the list of leads
	 */	
	public LeadData getReadLeadAtRandom (int _randomIndex) {	
		return this.dataOfLeads.get(_randomIndex);		
	}
	
	public int getNumberOfReadLeads () {	
		return this.dataOfLeads.size();		
	}	
	
	/**
	 * This function enriches the simulation by loading information about the leads from a real system
	 * 
	 */
	public void feedLeadsData() {
		
		// data structure to read leads from CSV file
		//java.util.Iterator<LeadData> it;
			
		if (this.getStringParameter("fileForLeads") != null) {
			
			// load vessels data of the pop		
			dataOfLeads = null;
				
			try {
				
				// read the CSV file if file is available
				dataOfLeads = CSVReader4Leads.parseCSVFile(this.getStringParameter("fileForLeads"));
									
				/* it = dataOfLeads.iterator();				
				while (it.hasNext()) {					
					LeadData l = it.next();					
						System.out.println("LeadID " + l.getLeadID() + ", Amount: " + l.getAmount() + 
						", WeekWon: " + l.getWeekWon() + ", WeekLost: " + l.getWeekLost() 
						+ ", CertaintyForConv: " + l.getCertaintyForConv() + ", ConvertedLead: " + l.getConvertedLead());
				} */		
				
				//System.out.println("There are " + dataOfLeads.size() + " leads");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
							
	}
	
	
	// ########################################################################
	// Constructors
	// ########################################################################

	/**
	 * 
	 */
	public ModelParameters() {

	}

	// ########################################################################
	// Export methods
	// ########################################################################

	public String export() {

		String values = "";

		values += exportGeneral() + "------\n";
		values += exportSpecifics();

		return values;
	}

	private String exportSpecifics() {

		String result = "";
		
		
		result += "portfolioSize = " + config.getString("portfolioSize") + "\n";


		result += "avgRiskAversion = " + config.getString("avgRiskAversion") + "\n";
		result += "stdevRiskAversion = " + config.getString("stdevRiskAversion") + "\n";
		
		result += "distributionForLeads = " + config.getString("distributionForLeads") + "\n";

		if (config.getString("distributionForLeads").compareTo("1") == 0) {
			result += "(Normal distr.) avgLeadsMagnitude = " + config.getString("avgLeadsMagnitude") + "\n";
			result += "(Normal distr.) stdevLeadsMagnitude = " + config.getString("stdevLeadsMagnitude") + "\n";
		}
		
		//result += "avgLeadConvCer = (orthogonal from " + config.getString("avgLeadMagnitude") + ")\n";
		
		result += "decayRateLeads = " + config.getString("decayRateLeads") + "\n";
		result += "thresholdForConversion = " + config.getString("thresholdForConversion") + "\n";

		result += "extraWeeklyHours = " + config.getString("extraWeeklyHours") + "\n";
		result += "socialComparison = " + config.getString("socialComparison") + "\n";

		result += "quota = " + config.getFloat("quota") + "\n";
		result += "bonus = " + config.getFloat("rateForBonus") + "*(salesRevenue - quota) " + "\n";

    		
		if (config.containsKey("fileForLeads")) {
			result += "fileForLeads = " + config.getString("fileForLeads") + "\n";
		} else {

			result += "fileForLeads = NO FILE FOR LEADS" + "\n";
		}
		return result;
	}
	
	/**
	 * Prints simple statistics evolution during the time.
	 */
	public void printParameters(PrintWriter writer) {

		// printing general params
		writer.println(this.export());

	}

	private String exportGeneral() {

		String result = "";

		result += "MC_runs = " + config.getInt("MCRuns") + "\n";
		result += "seed = " + config.getLong("seed") + "\n";
		result += "nrAgents = " + config.getInt("nrAgents") + "\n";
		result += "maxSteps = " + config.getInt("maxSteps") + "\n";
		result += "Base working hours a week] = " + ModelParameters.WORKINGHOURSPERWEEK + "\n";

		result += "Network? = " + this.network + "\n";
		result += "SNFile = " + config.getString("SNFile") + "\n";
		

		return result;
	}


	
	/**
	 * Reads parameters from the configuration file.
	 */
	public void readParameters(String CONFIGFILENAME) {

		Configurations configs = new Configurations();

		try {    
	    		    	
			// Read parameters from the file
			this.config = configs.properties(new File(CONFIGFILENAME));									
									
			// set type of network			
			if (config.getInt("typeOfNetwork") == NETWORK) 	
				this.network = true;
			else if (config.getInt("typeOfNetwork") == NO_NETWORK) 
					this.network = false;			
																	
						
			// if having a SN from file to read, do it
			if (this.network == true) 
				this.readGraphFromFile(config.getString("SNFile"));
				
			
			// if having a file for leads, init and parse from file				
			if (isParameterSet("fileForLeads")) 		
				feedLeadsData ();
			else 			
				this.dataOfLeads = null;
			
												
		} catch (IOException e) {

			System.err.println("ModelParameters: Error when handling or opening the properties file " + CONFIGFILENAME + "\n"
					+ e.getMessage());
			e.printStackTrace(new PrintWriter(System.err));
			
		} catch (ConfigurationException ce) {

			System.err.println("ModelParameters: Error when managing the configuration module (Apache) with file " + CONFIGFILENAME + "\n"
					+ ce.getMessage());
			ce.printStackTrace(new PrintWriter(System.err));
			
		} catch (NoSuchElementException noSuch) {

			System.err.println("Warning ModelParameters: Property not defined in " + CONFIGFILENAME + "\n"
					+ noSuch.getMessage());
		}
	}	
}
