package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import sim.engine.*;
import sim.util.*;

import socialnetwork.*;
import util.BriefFormatter;

// https://github.com/eclipse/eclipse-collections/blob/master/docs/guide.md#eclipse-collections-reference-guide
//import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
//import org.eclipse.collections.impl.list.mutable.FastList;
//import org.eclipse.collections.impl.list.mutable.primitive.FloatArrayList;

//import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
//import org.graphstream.algorithm.Toolkit;
//import org.graphstream.graph.Node;

// random generator from DSI of Milano
import it.unimi.dsi.util.*;



/**
 * 
 * ABM4Sales model (Journal of Marketing paper)
 * 
 * Simulation core, responsible for scheduling agents of the model
 * 
 * @author mchica
 * @location Granada
 * @date 2022/09/22
 * 
 */
public class Model extends SimState {

	// ########################################################################
	// Variables
	// ########################################################################
			
	private static final long serialVersionUID = -8094551352549697295L;

	static String CONFIGFILENAME;
				
	// LOGGING
	public static final Logger log = Logger.getLogger( Model.class.getName() );
			
	// MODEL VARIABLES
	
	it.unimi.dsi.util.XoRoShiRo128PlusRandom random;
	
	ModelParameters params;
	
	Bag agents;		
	
	float peopleWorkingExtra[]; 		// a counter of people working extra
	float peopleWithBonus[]; 			// a counter of people with bonus
	float pay[]; 						// a counter of the pay by all the agents during the simulation
	float convertedLeadsByMagnitude[]; 	// a counter of the converted leads (measured by magnitude) by all the agents during the simulation
	float convertedLeads[]; 			// a counter of the converted leads by all the agents during the simulation
	float fallOffLeads[]; 				// a counter of the fall-off leads by all the agents during the simulation
	
	float expectedConvertedLeads[]; 	// a counter of the expected (when file is read) converted leads by all the agents during the simulation
	float expectedFallOffLeads[]; 		// a counter of the expected (when file is read) falloff leads by all the agents during the simulation

	
	// SOCIAL NETWORK
	GraphStreamer socialNetwork;
	float avgDegree;
	
	int MC_RUN = -1;
	
	
	//--------------------------- Clone method ---------------------------//	
	
		
	//--------------------------- Get/Set methods ---------------------------//	
	//
	
	
	public float getAvgDegree () {		
		return this.avgDegree;		
	}
		
	public static String getConfigFileName() {
		return CONFIGFILENAME;
	}
	
	public static void setConfigFileName(String _configFileName) {
		CONFIGFILENAME = _configFileName;
	}

	public GraphStreamer getSocialNetwork() {	
		return socialNetwork;	
	}	
		
	/**
	 * 
	 * @return the bag of agents.
	 */
	public Bag getAgents() {
		return agents;
	}

	/**
	 *  
	 * @param _agents is the bag (array) of agents
	 */
	public void setAgents(Bag _agents) {
		this.agents = _agents;
	}		

	/**
	 * Get the number of converted leads for all the period of time
	 * 
	 * @return an ArrayList with the converted leads (KPI)
	 */
	public float[] getConvertedLeadsArray() {
		return convertedLeads;
	}
	
	/**
	 * Get the number of EXPECTED CONVERTED (when getting a leads file) leads for all the period of time
	 * 
	 * @return an ArrayList with the converted leads (KPI)
	 */
	public float[] getExpectedConvertedLeadsArray() {
		return expectedConvertedLeads;
	}
	
	/**
	 * Get the number of EXPECTED FALLOFF (when getting a leads file) leads for all the period of time
	 * 
	 * @return an ArrayList with the converted leads (KPI)
	 */
	public float[] getExpectedFallOffLeadsArray() {
		return expectedFallOffLeads;
	}
	
	/**
	 * Get the number of converted leads, by magnitude, for all the period of time
	 * 
	 * @return an ArrayList with the converted leads (KPI)
	 */
	public float[] getConvertedLeadsByMagnitudeArray() {
		return convertedLeadsByMagnitude;
	}

	/**
	 * Get the number of converted leads, by magnitude, for a given step
	 * 
	 * @return the sales, avg. by magnitude, at the given step
	 */
	public float getConvertedLeadsByMagnitude(int _step) {
		return this.convertedLeadsByMagnitude[_step];		
	}
	
	/**
	 * 
	 */
	public float[] getPayArray() {
		return this.pay;
	}

	/**
	 */
	public float getPay(int _step) {
		return this.pay[_step];		
	}
	
	/**
	 * Get the average Pay of the salespeople, for a given step
	 * 
	 * @return the averaged Pay for the whole population, at the given step
	 */
	public float getAvgPay (int _step) {
		return (float)(this.pay[_step] / params.getIntParameter("nrAgents"));		
	}

	/**
	 * Get the number of fall-off leads for all the period of time
	 * 
	 * @return an ArrayList with the fall-off leads (KPI)
	 */
	public float[] getFallOffLeadsArray() {
		return this.fallOffLeads;
	}

	/**
	 * Get the people working extra
	 */
	public float[] getPeopleWorkingExtra() {
		return this.peopleWorkingExtra;
	}

	/**
	 * Get the people with bonus
	 */
	public float[] getPeopleWithBonus() {
		return this.peopleWithBonus;
	}
	
	/**
	 * Get the parameter object with all the input parameters
	 */
	public ModelParameters getParametersObject () {
		return  this.params;
	}

	/**
	 * Set the parameters
	 * @param _params the object to be assigned
	 */
	public void setParametersObject (ModelParameters _params) {
		this.params = _params;
	}

	
	
	// ########################################################################
	// Constructors
	// ########################################################################

	/**
	 * Initializes a new instance of the simulation model
	 * @param _params an object with all the parameters of the model
	 */
	public Model(ModelParameters _params) {
	    
		super( (long)_params.getLongParameter("seed") );	
		
		// use our specific class for random generator
		this.random = new XoRoShiRo128PlusRandom();
		this.random.setSeed((long)_params.getLongParameter("seed"));
		 
		
		try {  

	        // This block configure the logger with handler and formatter  
			long millis = System.currentTimeMillis();
			FileHandler fh = new FileHandler("./logs/" + _params.getStringParameter("outputFile") + "_" + millis + ".log");  
	        log.addHandler(fh);
	        BriefFormatter formatter = new BriefFormatter();  
	        fh.setFormatter(formatter);  
	        
	        log.setLevel(Level.FINE);

	        log.log(Level.FINE, "Log file created at " + millis +" (System Time Millis)\n");  

	    } catch (SecurityException e) {  
	        e.printStackTrace(); 	        
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }  

		// get parameters
		params = _params;	
		
		int _maxSteps =(int) params.getIntParameter("maxSteps");
		int _nrAgents =(int) params.getIntParameter("nrAgents");
	
				
		// Initialization of the type of KPI counts
		this.peopleWorkingExtra =  new float[_maxSteps];
		this.peopleWithBonus =  new float[_maxSteps];
		this.convertedLeads =  new float[_maxSteps];
		this.expectedConvertedLeads =  new float[_maxSteps];
		this.expectedFallOffLeads =  new float[_maxSteps];
		this.convertedLeadsByMagnitude =  new float[_maxSteps];
		this.fallOffLeads = new float[_maxSteps];
		this.pay = new float[_maxSteps];
					
		// social network initialization from file				
		if (params.getNetworkOption()) {
			
			socialNetwork = new GraphStreamer(_nrAgents, params);
			socialNetwork.setGraph(params);		
			this.avgDegree = (float) socialNetwork.getAvgDegree();

		} else {
			
			socialNetwork = null;
		}			
	}
		
	
	//--------------------------- SimState methods --------------------------//

	/**
	 * Sets up the simulation. The first method called when the simulation.
	 */
	public void start() {

		super.start();
		
		this.MC_RUN ++;
				 
        // reset counter for KPIs
        for (int i = 0; i < (int) params.getIntParameter("maxSteps"); i++) {
        	this.convertedLeads[i] =  0;
        	this.convertedLeadsByMagnitude[i] =  0;
        	this.expectedConvertedLeads[i] = 0;
        	this.expectedFallOffLeads[i] = 0;
        	this.fallOffLeads[i] = 0;
        	this.pay[i] = 0;
        	this.peopleWorkingExtra[i] = 0;
        	this.peopleWithBonus[i] = 0;
        }
		        
        final int FIRST_SCHEDULE = 0;
        int scheduleCounter = FIRST_SCHEDULE;
        
        
		// at random, create an array with the IDs unsorted. 
		// In this way we can randomly distribute the agents 
		// this is done here to change every MC simulation without changing the SN
		// IMPORTANT! IDs of the SN nodes and agents must be the same to avoid BUGS!!
		    
		
        // Initialization of the agents
        agents = new Bag();
        		
		// select the nodes by degree when having seeding Ps
        for (int i = 0; i < (int) params.getIntParameter("nrAgents"); i++) {
        	       	               
            // generate the agent, push in the bag, and schedule
            SalesPerson cl = generateAgent (i);  
                                    
            // Add agent to the list and schedule
            agents.add(cl);
            
            // Add agent to the schedule
            schedule.scheduleRepeating(Schedule.EPOCH, scheduleCounter, cl);             
        }       
         
        // shuffle agents in the Bag and later, reassign the id to the position in the bag        
        agents.shuffle(this.random);
                
          
        // assign shuffled IDs to agents and set their parameters
        for (int agentId = 0; agentId < agents.size(); agentId++) {

        	((SalesPerson) agents.get(agentId)).setGamerAgentId(agentId);
        
        	if (params.network)
        		
        		// set the degree of the agent
        		((SalesPerson) agents.get(agentId)).setDegree(socialNetwork.getNeighborsOfNode(agentId).size());
        	      	
        }	          
                	
        // Add anonymous agent to calculate statistics
        setAnonymousAgentApriori(scheduleCounter);
        scheduleCounter++;    
                           
        setAnonymousAgentAposteriori(scheduleCounter);              
			
	}


	//-------------------------- Auxiliary methods --------------------------//	
	
	/**
	 * Generates the agent with its properties
	 * This is important as we can save time not to ask at each step if the agent is active or not
	 * 
	 * @param _nodeId is the id of the agent
	 * @return the agent created by the method
	 */
	private SalesPerson generateAgent(int _nodeId) {
		
		SalesPerson  cl = new SalesPerson (_nodeId, params, random);
									
		return cl;
	}	
	
	/**
	 * Adds the anonymous agent to schedule (at the beginning of each step), 
	 * which calculates the statistics.
	 * @param scheduleCounter
	 */
	private void setAnonymousAgentApriori(int scheduleCounter) {
		
		// Add to the schedule at the end of each step
		schedule.scheduleRepeating(Schedule.EPOCH, scheduleCounter, new Steppable()
		{ 
			
			private static final long serialVersionUID = -2837885990121299044L;

			public void step(SimState state) {
			
			}			
		});				
	}
		

	/**
	 * Counts the KPI output at each time-step depending on the agents' information
	 * 
	 */
	private void updateKPIsCounting (int currentStep) {

		// KPIs are cumulative 
		// TODO IMPORTANT: THIS WAS COMMENTED BEFORE ADDING THE DATA READING!!
		if (currentStep > 0) {
			this.convertedLeads[currentStep] = this.convertedLeads[currentStep - 1];
			this.expectedConvertedLeads[currentStep] = this.expectedConvertedLeads[currentStep - 1];
			this.expectedFallOffLeads[currentStep] = this.expectedFallOffLeads[currentStep - 1];
			this.convertedLeadsByMagnitude[currentStep] = this.convertedLeadsByMagnitude[currentStep - 1];
			this.fallOffLeads[currentStep] = this.fallOffLeads[currentStep - 1];
		}
		
		//System.out.println("step " + currentStep);
		
		for (int i = 0; i < (int)params.getIntParameter("nrAgents"); i++) {
			
			//System.out.println("converted leads for agent" + i + ": " + ((SalesPerson) agents.get(i)).getConvertedLeadsAtStep(currentStep));
			
			this.convertedLeads[currentStep] += ((SalesPerson) agents.get(i)).getConvertedLeadsAtStep(currentStep);
			
			// we save here the expected converted leads for those closed at the timestep by the salesperson
			this.expectedConvertedLeads[currentStep] += ((SalesPerson) agents.get(i)).getExpectedConvertedLeadsAtStep(currentStep);
			this.expectedFallOffLeads[currentStep] += ((SalesPerson) agents.get(i)).getExpectedFallOffLeadsAtStep(currentStep);

			this.convertedLeadsByMagnitude[currentStep] += ((SalesPerson) agents.get(i)).getConvertedLeadsByMagnitudeAtStep(currentStep);

			//System.out.println("fall-off leads for agent" + i + ": " + ((SalesPerson) agents.get(i)).getFallOffLeadsAtStep(currentStep));

			this.fallOffLeads[currentStep] += ((SalesPerson) agents.get(i)).getFallOffLeadsAtStep(currentStep);		
			
			this.pay[currentStep] += ((SalesPerson) agents.get(i)).getPayAtStep(currentStep);		

			if (((SalesPerson) agents.get(i)).getWorkingHoursAtStep(currentStep) > ModelParameters.WORKINGHOURSPERWEEK) {
				this.peopleWorkingExtra[currentStep] += 1;
			}
			
			if (((SalesPerson) agents.get(i)).getPayAtStep(currentStep) > 
				(((SalesPerson) agents.get(i)).getConvertedLeadsByMagnitudeAtStep(currentStep) * ModelParameters.percForPay)) {
				this.peopleWithBonus[currentStep] += 1;
			}
		}	

		if (ModelParameters.DEBUG == true)
			System.out.println("End of step " + (int) schedule.getSteps() + "------\n") ;
								
		//System.out.println("Step " + currentStep + ", rate is " +  this.followers_rateCoopActs[currentStep]  + " having avg degree of " +  this.avgDegree);
	}
	
	/**
	 * Adds the anonymous agent to schedule (at the end of each step), 
	 * which calculates the statistics.
	 * @param scheduleCounter
	 */
	private void setAnonymousAgentAposteriori(int scheduleCounter) {

			
		// Add to the schedule at the end of each step
		schedule.scheduleRepeating(Schedule.EPOCH, scheduleCounter, new Steppable()
		{ 
			
			private static final long serialVersionUID = 3078492735754898981L;

			public void step(SimState state) { 
				
				int currentStep = (int) schedule.getSteps();
				
				updateKPIsCounting (currentStep);									
				
				
				for (int i = 0; i < (int) params.getIntParameter("nrAgents"); i++) {				
					
					// do sth for all the agents at the end of the time-step							
				
				}
					
				// show the result of the last step in the console				
				if (currentStep == ((int) params.getIntParameter("maxSteps") - 1)) {
				
					
					/*System.out.println(
							"Final step;" + "numInfluencersC;" + influencersC_Agents[currentStep] 
							+ ";numInfluencersD;" + influencersD_Agents[currentStep] + ";numFollowersC;" +
							followersC_Agents[currentStep] + ";numFollowersD;" + followersD_Agents[currentStep]);*/
				}
				
				// plotting and saving additional output information for analysis
				plotSaveAdditionalInfo(currentStep);						
			}										
		});		
	}
		
		
	/**
	 * This method wraps all the processes of saving and plotting additional
	 * information to study the dynamics of the simulation (e.g., data about groups, wealth 
	 * distribution of the agents etc)
	 * 
	 * 
	 * @param _currentStep is the step of the simulation
	 * 
	 */

	protected void plotSaveAdditionalInfo(int _currentStep) {
						
		// here, we save information about all the sales and info agent by agent
		if ((ModelParameters.OUTPUT_SALESPEOPLE_INFO) && (_currentStep == ((int) params.getIntParameter("maxSteps") - 1))) {

			try {

				// save
				File fileStrength = new File("./logs/agents/" + "SalespeopleMicrOutput_" 
						+ params.getStringParameter("outputFile") + "." + this.MC_RUN + ".txt");
				
				PrintWriter printWriter = new PrintWriter(fileStrength);

				printWriter.write("agentId;riskAversion;aggrConvertedLeads;aggrExpectedConvertedLeads;aggrExpectedFallOffLeads;aggrConvertedLeadsByMag;aggrPay;aggrFallOffLeads\n");

				// loop for all the agents
				for (int i = 0; i < (int)params.getIntParameter("nrAgents"); i++) {

					float aggrConvertedLeads = 0;
					float aggrExpectedConvertedLeads = 0;
					float aggrExpectedFallOffLeads = 0;
					float aggrConvertedLeadsByMag = 0;
					float aggrPay = 0;
					float aggrFallOffLeads = 0;
					
					for (int k = 0; k < (int)params.getIntParameter("maxSteps"); k++) {
					
						aggrConvertedLeads += ((SalesPerson) agents.get(i)).getConvertedLeadsAtStep(k);
						aggrExpectedConvertedLeads += ((SalesPerson) agents.get(i)).getExpectedConvertedLeadsAtStep(k);
						aggrExpectedFallOffLeads += ((SalesPerson) agents.get(i)).getExpectedFallOffLeadsAtStep(k);
						aggrConvertedLeadsByMag += ((SalesPerson) agents.get(i)).getConvertedLeadsByMagnitudeAtStep(k);
						aggrPay += ((SalesPerson) agents.get(i)).getPayAtStep(k);
						aggrFallOffLeads += ((SalesPerson) agents.get(i)).getFallOffLeadsAtStep(k);
					}
										
					// print all the aggregated info for the agent
					printWriter.write(i + ";" + ((SalesPerson) agents.get(i)).getRiskAversion() + ";" + aggrConvertedLeads + ";" + aggrExpectedConvertedLeads + ";"
							 + aggrExpectedFallOffLeads + ";" + aggrConvertedLeadsByMag + ";" + aggrPay + ";" + aggrFallOffLeads + "\n");
				
				}

				printWriter.close();

			} catch (FileNotFoundException e) {

				e.printStackTrace();
				log.log(Level.SEVERE, e.toString(), e);
			}
			
		}	
				
	}


}
