package model;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.deeplearning4j.eval.Evaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.factory.Nd4j;
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

	//asish: Neural Network Variables
	private static boolean HAS_TRAINED_GLOBALLY = false;
	private static NeuralNetworkManager GLOBAL_NN_MANAGER = null;
	private String leadChoicesLogFile;

	
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

	private float getFloatCell(Cell cell) {
		if (cell == null) return 0.0f; // or some default value
		return (float) cell.getNumericCellValue();
	}

	private float decodeProb(int cls){ // 1→0 , 2→0.2 , 3→0.4 , 4→0.8
		switch (cls){
			case 1: return 0.0f;
			case 2: return 0.2f;
			case 3: return 0.4f;
			case 4: return 0.8f;
			default: return 0.2f;   // fallback = baseline
		}
	}

	private org.nd4j.linalg.dataset.DataSet loadDataFromExcel(String excelFilePath, int recordsUsed) throws Exception {
		FileInputStream fis = new FileInputStream(new File(excelFilePath));
		Workbook workbook = new XSSFWorkbook(fis);
		Sheet sheet = workbook.getSheetAt(0);

		List<float[]> featuresList = new ArrayList<>();
		List<float[]> labelsList   = new ArrayList<>();

		int rowCount = 0;
		for (Row row : sheet) {
			if (row.getRowNum() == 0) continue; // Skip header
			if (rowCount >= recordsUsed) break;

			// Skip rows with missing data
			try {
				float weeksElapsed   = getFloatCell(row.getCell( 8));   // “weeks_elapsed”
				float weeksDiffPrior = getFloatCell(row.getCell( 9));   // “weeks_diff_prior”
				float entryByWeek    = getFloatCell(row.getCell(12));   // “entry_by_week”
				float amount         = getFloatCell(row.getCell(27));   // “amount”
				float sector         = getFloatCell(row.getCell(31));   // “sector” (already encoded 0-N)
				float mktGen         = getFloatCell(row.getCell(33));   // “mkt_gen” (0/1)
				float prodService    = getFloatCell(row.getCell(34));   // “product_service” (0/1)

				float rawCls = getFloatCell(row.getCell(42));
				float labelConv = decodeProb(Math.round(rawCls));   // “conversion_prob_week”
//				float labelFallOffVal = getFloatCell(row.getCell(10));

				// Read input feature columns
				float[] inArr  = { weeksElapsed, weeksDiffPrior, entryByWeek,
						amount, sector, mktGen, prodService };

				float[] outArr = { labelConv };

				featuresList.add(inArr);
				labelsList.add(outArr);
				rowCount++;

			} catch (Exception ex) {
				// Log and skip rows with truly unreadable numeric cells
				System.out.println("Skipping row " + row.getRowNum() + " due to error: " + ex.getMessage());
			}

		}

		System.out.println(">>> Final loaded training rows: " + featuresList.size());
		if (featuresList.size() == 0) {
			throw new RuntimeException("No usable data found even after fallback parsing.");
		}

		workbook.close();
		fis.close();

		int nRows = featuresList.size();
		int nIn = 7;   // number of input features
		int nOut = 1;  // number of output targets

		System.out.println(">>> Loaded rows: " + nRows);
		if (nRows == 0) {
			System.err.println("ERROR: No valid rows loaded from Excel. Check for missing cells or empty file.");
		}

		INDArray featureMatrix = Nd4j.create(nRows, nIn);
		INDArray labelMatrix   = Nd4j.create(nRows, nOut);

		for (int i = 0; i < nRows; i++) {
			float[] in  = featuresList.get(i);
			float[] out = labelsList.get(i);
			for (int j = 0; j < nIn; j++) {
				featureMatrix.putScalar(new int[]{i, j}, in[j]);
			}
			for (int k = 0; k < nOut; k++) {
				labelMatrix.putScalar(new int[]{i, k}, out[k]);
			}
		}

		return new org.nd4j.linalg.dataset.DataSet(featureMatrix, labelMatrix);
	}
		
	
	//--------------------------- SimState methods --------------------------//

	/**
	 * Sets up the simulation. The first method called when the simulation.
	 */
	public void start() {

		super.start();
		
		this.MC_RUN ++;

		long ts = System.currentTimeMillis();
		this.leadChoicesLogFile =
				"./logs/agents/SalespeopleMicrOutput_"
						+ params.getStringParameter("outputFile")
						+ "_run" + this.MC_RUN
						+ "_" + ts + ".txt";
				 
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

		//(1) Initialize Neural Network
		if (!HAS_TRAINED_GLOBALLY) {
			// For example, you can define your training file, # epochs, etc.
			// Or read them from 'params' if you have them in your .properties or arguments:
			String excelPath  = "data_injection/abm_training_data.xlsx";
			int epochs        = 20;
			int recordsToUse  = 39000;

			// (a) Load training data
			DataSet ds = null;
			try {
				ds = loadDataFromExcel(excelPath, recordsToUse);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			// (b) Fit + transform
			NormalizerMinMaxScaler scaler = new NormalizerMinMaxScaler(0,1);
			scaler.fit(ds);
			scaler.transform(ds);

			// (c) Build manager + train
			NeuralNetworkManager localNN = new NeuralNetworkManager(7);
			localNN.train(ds, epochs, scaler);

			// (d) Assign them to static fields
			GLOBAL_NN_MANAGER = localNN;
			HAS_TRAINED_GLOBALLY = true;

			// Optional debug print:
			System.out.println("[Model.start()] Completed global NN training once, with "
					+ recordsToUse + " rows for " + epochs + " epochs.");

			//Evaluating model after training
			RegressionEvaluation regEval = new RegressionEvaluation(1);
			INDArray features    = ds.getFeatures();
			INDArray labels       = ds.getLabels();
			INDArray preds        = GLOBAL_NN_MANAGER.getModel().output(features);
			regEval.eval(labels, preds);
			System.out.println("Training MSE  = " + regEval.meanSquaredError(0));
			System.out.println("Training MAE  = " + regEval.meanAbsoluteError(0));
		}

		// --------  NEW  : parse the three per-agent arrays  -------
		Float[]  accArr    = splitFloatArray(params, "agentAccuracies");
		Integer[] kArr     = splitIntArray(params, "agentLeadChoices");
		Integer[] pSizeArr = splitIntArray(params, "agentPortfolioSizes");
        
        
		// at random, create an array with the IDs unsorted. 
		// In this way we can randomly distribute the agents 
		// this is done here to change every MC simulation without changing the SN
		// IMPORTANT! IDs of the SN nodes and agents must be the same to avoid BUGS!!
		    
		
        //(2) Initialization of the agents
		agents = new Bag();
		int nrAgents = params.getIntParameter("nrAgents");

		// Get agent strategies from parameter string like "1,2,1,3"
		String agentStrats = params.getStringParameter("agentStrategies");
		String[] tokens = (agentStrats != null) ? agentStrats.split(",") : new String[0];

		// 1) CREATE + SCHEDULE everyone, but **do not** assign any strategy yet
		for (int i = 0; i < nrAgents; i++) {
			float acc = (i < accArr.length)   ? accArr[i]   : 1.0f;
			int   k   = (i < kArr.length)     ? kArr[i]     : 3;
			int   pSz = (i < pSizeArr.length) ? pSizeArr[i] : params.getIntParameter("portfolioSize");

			SalesPerson sp = new SalesPerson(i, params, random, GLOBAL_NN_MANAGER, acc, k, pSz);
			agents.add(sp);
			schedule.scheduleRepeating(sp);
		}

		// shuffle agents in the Bag and later, reassign the id to the position in the bag
        agents.shuffle(this.random);




		// 3) NOW assign gamerAgentId, degree AND strategy in the *shuffled* order
		for (int agentId = 0; agentId < agents.size(); agentId++) {
			SalesPerson sp = (SalesPerson) agents.get(agentId);

			// reset the agent’s ID to match its new position
			sp.setGamerAgentId(agentId);

			// if you’re using a social network, update degree too
			if (params.network) {
				int deg = socialNetwork.getNeighborsOfNode(agentId).size();
				sp.setDegree(deg);
			}

			// finally, map the i‑th token onto the i‑th shuffled agent
			if (agentId < tokens.length) {
				try {
					int code = Integer.parseInt(tokens[agentId].trim());
					sp.setStrategyByNumber(code);
				} catch (NumberFormatException ex) {
					System.err.println("Bad strat code for agent "
							+ agentId + ": "
							+ tokens[agentId]);
					sp.setStrategyByNumber(0);
				}
			} else {
				// fallback default
				sp.setStrategyByNumber(0);
			}
		}
                	
        // Add anonymous agent to calculate statistics
        setAnonymousAgentApriori(scheduleCounter);
        scheduleCounter++;    
                           
        setAnonymousAgentAposteriori(scheduleCounter);              
			
	}

	private static Float[] splitFloatArray(ModelParameters p, String key) {
		if (!p.isParameterSet(key)) return new Float[0];
		return Arrays.stream(p.getStringParameter(key).split(","))
				.map(String::trim).filter(s->!s.isEmpty())
				.map(Float::parseFloat).toArray(Float[]::new);
	}

	private static Integer[] splitIntArray(ModelParameters p, String key) {
		if (!p.isParameterSet(key)) return new Integer[0];
		return Arrays.stream(p.getStringParameter(key).split(","))
				.map(String::trim).filter(s->!s.isEmpty())
				.map(Integer::parseInt).toArray(Integer[]::new);
	}


	//-------------------------- Auxiliary methods --------------------------//	
	
	/**
	 * Generates the agent with its properties
	 * This is important as we can save time not to ask at each step if the agent is active or not
	 * 
	 * @param _nodeId is the id of the agent
	 * @return the agent created by the method
	 */
//	private SalesPerson generateAgent(int _nodeId) {
//
//		SalesPerson  cl = new SalesPerson (_nodeId, params, random, GLOBAL_NN_MANAGER);
//
//		return cl;
//	}
	
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
				int currentStep = (int) schedule.getSteps();

				// 1. Update your aggregated KPI counters
				updateKPIsCounting(currentStep);

				// 2. Log each agent’s sorted leads & chosen top‐3 to a file
				//    (Do this every step except maybe step == 0 if you want)
				if (currentStep >= 0) {
					appendLeadChoicesToFile(currentStep);
				}

				// 3. If final step, do final prints
				if (currentStep == params.getIntParameter("maxSteps") - 1) {
					// ... your existing final-step code ...
					plotSaveAdditionalInfo(currentStep);
				}
			}			
		});				
	}

	private void appendLeadChoicesToFile(int currentStep) {
		// Be sure to open in APPEND mode
		File file = new File(leadChoicesLogFile);

		try (PrintWriter pw = new PrintWriter(new FileOutputStream(file, true))) {
			// For each agent
			for (int i = 0; i < agents.size(); i++) {
				SalesPerson sp = (SalesPerson) agents.get(i);

				// Build a CSV string of their sorted lead IDs:
				StringBuilder sortedSB = new StringBuilder();
				for (Map.Entry<Integer,Float> e : sp.getLastSortedLeads().entrySet()) {
					sortedSB.append("(")
							.append(e.getKey()).append(",")
							.append(String.format("%.4f", e.getValue()))
							.append("),");
				}
				if (sortedSB.length() > 0) sortedSB.setLength(sortedSB.length() - 1);

// Build a CSV of the top 3 chosen plus metric
				StringBuilder top3SB = new StringBuilder();
				for (Map.Entry<Integer,Float> e : sp.getLastChosenTop3().entrySet()) {
					top3SB.append("(")
							.append(e.getKey()).append(",")
							.append(String.format("%.4f", e.getValue()))
							.append("),");
				}
				if (top3SB.length() > 0) top3SB.setLength(top3SB.length() - 1);

				// Print line: step; agentId; sortedLeads; chosenTop3
				pw.println(
						currentStep + ";" +
								sp.getGamerAgentId() + ";" +
								sp.getStrategy() + ";" +
								"[" + sortedSB.toString() + "];" +
								"[" + top3SB.toString() + "]"
				);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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

			File fileStrength = new File(leadChoicesLogFile);

// Open in APPEND mode (true)
			try (PrintWriter printWriter = new PrintWriter(new FileOutputStream(fileStrength, true))) {

				// Optionally print a small header or separator to show
				// these lines are final aggregates, e.g.:
				printWriter.println();
				printWriter.println("=== FINAL AGGREGATE RESULTS ===");
				printWriter.println("agentId;riskAversion;strategy;accuracy;aggrConvertedLeads;aggrExpectedConvertedLeads;"
						+ "aggrExpectedFallOffLeads;aggrConvertedLeadsByMag;aggrPay;aggrFallOffLeads");

				// Then loop over each SalesPerson as before and write the final lines
				for (int i = 0; i < params.getIntParameter("nrAgents"); i++) {
					SalesPerson sp = (SalesPerson) agents.get(i);

					float aggrConvertedLeads = 0;
					float aggrExpectedConvertedLeads = 0;
					float aggrExpectedFallOffLeads = 0;
					float aggrConvertedLeadsByMag = 0;
					float aggrPay = 0;
					float aggrFallOffLeads = 0;

					for (int k = 0; k < params.getIntParameter("maxSteps"); k++) {
						aggrConvertedLeads += sp.getConvertedLeadsAtStep(k);
						aggrExpectedConvertedLeads += sp.getExpectedConvertedLeadsAtStep(k);
						aggrExpectedFallOffLeads += sp.getExpectedFallOffLeadsAtStep(k);
						aggrConvertedLeadsByMag += sp.getConvertedLeadsByMagnitudeAtStep(k);
						aggrPay += sp.getPayAtStep(k);
						aggrFallOffLeads += sp.getFallOffLeadsAtStep(k);
					}

					printWriter.println(
							i + ";"
									+ sp.getRiskAversion() + ";"
									+ sp.getStrategy().name() + ";"
									+ sp.getAccuracy() + ";"
									+ aggrConvertedLeads + ";"
									+ aggrExpectedConvertedLeads + ";"
									+ aggrExpectedFallOffLeads + ";"
									+ aggrConvertedLeadsByMag + ";"
									+ aggrPay + ";"
									+ aggrFallOffLeads
					);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				log.log(Level.SEVERE, e.toString(), e);
			}

		}	
				
	}


}
