package model;

import sim.engine.*;

import org.eclipse.collections.impl.list.mutable.FastList;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

// DL4J / ND4J imports
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;

// Apache POI imports for Excel reading (example)
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Class of an agent SalesPerson of the ABM4Sales model

 * @author mchica
 * @date 2022/09/22
 *
 * Modified to include a neural network that is trained from an Excel dataset.
 *
 * @author ...
 * @date ...
 */

public class SalesPerson implements Steppable {

	// ########################################################################
	// Variables
	// ########################################################################	

	
	private static final long serialVersionUID = 1L;

	//--------------------------------- Fixed -------------------------------//
						
	int gamerAgentId;					// A unique agent Id
		
	int currentStep;					// the current step of the simulation
	
	int degree;							// to speed up, just count the neighbors once.
	
		
	//------------------------------- Dynamic -------------------------------// 

	float riskAversion;					// value in [0,1] with the risk aversion of the salesperson

	
	float workingHours[];				// array with the working hours decided by the agent

	float convertedLeads[];				// array with the evolution of converted leads (units of leads)
	
	float expectedConvertedLeads[];		// array with the expected (from file) converted leads (units of leads)
	float expectedFallOffLeads[];		// array with the expected (from file) fall off leads (units of leads)

	float convertedLeadsByMagnitude[];	// array with the evolution of converted leads but measuring its magnitude (i.e., revenue in USD$)
	
	float pay[];						// array with the evolution of the pay of the salesperson (included compensation or bonus)

	float fallOffLeads[];				// array with the evolution of fall-off leads

	double probOptions[];				// array with the probabilities to choose one of the leads active in the portfolio
			
	FastList<Lead> portfolio;			// array of size P that contains all the leads of the salesperson
	
	float quota;						// weekly quota to get the bonus
	float rateForBonus;

	// ---------------- NEW: Neural network for this salesperson ----------------
	private NeuralNetworkManager nnManager;

	private int trainingRecordCount = 600;

	private boolean hasTrainedOnce = false;

	//--------------------------- Get/Set methods ---------------------------//
	
	public float getRiskAversion() {
		return riskAversion;
	}

	public void setRiskAversion(float riskAversion) {
		this.riskAversion = riskAversion;
	}

	/**
	 * Gets the id of the agent
	 * @return
	 */
	public int getGamerAgentId() {
		return gamerAgentId;
	}

	/**
	 * Sets the id of the agent
//	 * @param gamerAgentId
	 */
	public void setGamerAgentId(int _gamerAgentId) {
		this.gamerAgentId = _gamerAgentId;
	}
	
	/** 
	 * 
	 * @param _step
	 * @return
	 */
	public float getWorkingHoursAtStep (int _step) {
		return this.workingHours [_step];
	}
	
	/** 
	 * 
	 * @param _step
	 * @return
	 */
	public float getConvertedLeadsAtStep (int _step) {
		return this.convertedLeads [_step];
	}
	
	/** 
	 * 
	 * @param _step
	 * @return
	 */
	public float getExpectedConvertedLeadsAtStep (int _step) {
		return this.expectedConvertedLeads [_step];
	}
	
	/** 
	 * 
	 * @param _step
	 * @return
	 */
	public float getExpectedFallOffLeadsAtStep (int _step) {
		return this.expectedFallOffLeads [_step];
	}
	
	/** 
	 * 
	 * @param _step
	 * @return
	 */
	public float getFallOffLeadsAtStep (int _step) {
		return this.fallOffLeads[_step];
	}
	
	/** 
	 * 
	 * @param _step
	 * @return
	 */
	public float getPayAtStep (int _step) {
		return this.pay[_step];
	}
	
	/** 
	 * 
	 * @param _step
	 * @param _value
	 * @return
	 */
	public void setConvertedLeadsAtStep (int _step, float _value) {
		this.convertedLeads[_step] = _value;
	}
	
	/** 
	 * 
	 * @param _step
	 * @param _value
	 * @return
	 */
	public void setWorkingHourstStep (int _step, float _value) {
		this.workingHours[_step] = _value;
	}

	/** 
	 * 
	 * @param _step
	 * @return _value
	 */
	public float getConvertedLeadsByMagnitudeAtStep (int _step) {
		return this.convertedLeadsByMagnitude[_step] ;
	}
	
	/** 
	 * 
	 * @param _step
	 * @param _value
	 * @return
	 */
	public void setConvertedLeadsByMagnitureAtStep (int _step, float _value) {
		this.convertedLeadsByMagnitude[_step] = _value;
	}
	
	/** 
	 * 
	 * @param _step
	 * @param _value
	 * @return
	 */
	public void setFallOffLeadsAtStep (int _step, float _value) {
		this.fallOffLeads[_step] = _value;
	}

	
	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}
	
	// ########################################################################
	// Constructors
	// ######################################################################## 		
	
	/**
	 * Initializes a new instance of the Agent class.
	 * 
//	 * @param _agentId is the if of the agent
//	 * @param _params the set of parameters of the model

//	 * @param _maxSteps the max steps of the simulation
	 * 
	 */
	public SalesPerson(int _agentId, ModelParameters _params, XoRoShiRo128PlusRandom _random) { 

		this.gamerAgentId = _agentId;

		// ---------------- NEW: Initialize the neural network manager -----------
		// Example: Suppose we have 4 input features for [magnitude, convCertainty, probToBeConverted, probToFallOff],
		// or any set of features that you want. Adjust as needed.
		this.nnManager = new NeuralNetworkManager(9);
		
		float _riskAv;

		do {
			double standardNormal = _random.nextGaussian();
			_riskAv = (float)(_params.getFloatParameter("avgRiskAversion")
					+ _params.getFloatParameter("stdevRiskAversion") * standardNormal);
		} while (_riskAv > 1 || _riskAv < 0);
			
		/*
		do { 
			_incrSucc = (float) _random.nextGaussian(_params.getFloatParameter("avgIncrSuccess"), 
					_params.getFloatParameter("stdevIncrSuccess"));
		} while (_incrSucc > 1 || _incrSucc < 0); */
		
		this.riskAversion = _riskAv;
				
		this.quota = _params.getFloatParameter("quota");

		int maxSteps = _params.getIntParameter("maxSteps");
		this.workingHours  = new float[maxSteps];
		this.pay           = new float[maxSteps];
		this.convertedLeads= new float[maxSteps];
		this.expectedConvertedLeads = new float[maxSteps];
		this.expectedFallOffLeads   = new float[maxSteps];
		this.convertedLeadsByMagnitude = new float[maxSteps];
		this.fallOffLeads   = new float[maxSteps];

		for (int i = 0; i < maxSteps; i++) {
			this.workingHours[i]  = 0.f;
			this.pay[i]           = 0.f;
			this.convertedLeads[i]= 0.f;
			this.expectedConvertedLeads[i] = 0.f;
			this.expectedFallOffLeads[i]   = 0.f;
			this.convertedLeadsByMagnitude[i] = 0.f;
			this.fallOffLeads[i]  = 0.f;
		}
		
		this.portfolio = new FastList <Lead> (_params.getIntParameter("portfolioSize"));
		
		this.rateForBonus = _params.getFloatParameter("rateForBonus");
		
		for (int i = 0; i < _params.getIntParameter("portfolioSize"); i++) {					
			this.portfolio.add(new Lead (_random, _params, 0, this.nnManager));
			
			if (ModelParameters.DEBUG == true)
				System.out.println("Portfolio pos " + i + " with lead: " + ((Lead)this.portfolio.get(i)).printStatsLead());
		}
		
		this.probOptions = new double[_params.getIntParameter("portfolioSize")];
	}

	// ########################################################################
	// NEW: Training the neural network from an Excel file
	// ########################################################################
	/**
	 * Loads training data from an Excel file and trains the neural network
	 * with a chosen number of records and epochs.
	 *
	 * @param excelFilePath path to the .xlsx file
	 * @param epochs how many passes to do over the subset of data
	 * @param recordsUsed how many rows from the Excel to train on
	 */
	public void trainNeuralNetworkFromExcel(String excelFilePath, int epochs, int recordsUsed) {
		try {
			DataSet ds = loadDataFromExcel(excelFilePath, recordsUsed);

			// Optionally scale/normalize your data:
			NormalizerMinMaxScaler scaler = new NormalizerMinMaxScaler(0, 1);
			scaler.fit(ds);
			scaler.transform(ds);

			nnManager.train(ds, epochs);

			if (ModelParameters.DEBUG) {
				System.out.println("SalesPerson " + this.gamerAgentId
						+ " finished NN training with " + recordsUsed
						+ " records, for " + epochs + " epochs.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load data from Excel (adjust columns as needed).
	 * This is exactly as in your original code.
	 */
	private DataSet loadDataFromExcel(String excelFilePath, int recordsUsed) throws Exception {
		FileInputStream fis = new FileInputStream(new File(excelFilePath));
		Workbook workbook = new XSSFWorkbook(fis);
		Sheet sheet = workbook.getSheetAt(0);

		List<float[]> featuresList = new ArrayList<>();
		List<float[]> labelsList   = new ArrayList<>();

		int rowCount = 0;
		for (Row row : sheet) {
			if (row.getRowNum() == 0) continue; // Skip header
			if (rowCount >= recordsUsed) break;

			// Read input feature columns
			Cell idLeadCell                   = row.getCell(0);
			Cell weeksElapsedSinceCreatedCell = row.getCell(1);
			Cell weeksDiffPriorCell           = row.getCell(2);
			Cell realLeadSumCell              = row.getCell(3);
			Cell processWeekCell              = row.getCell(4);
			Cell mktgGenCell                  = row.getCell(5);
			Cell sectorCell                   = row.getCell(6);
			Cell businessModelLeadCell        = row.getCell(7);
			Cell amountCell                   = row.getCell(8);

			// Target label columns (probConversion, probFallOff)
			Cell labelConvCell     = row.getCell(9);
			Cell labelFallOffCell  = row.getCell(10);

			// Skip rows with missing data
			if (idLeadCell == null || weeksElapsedSinceCreatedCell == null || weeksDiffPriorCell == null ||
					realLeadSumCell == null || processWeekCell == null || mktgGenCell == null ||
					sectorCell == null || businessModelLeadCell == null || amountCell == null ||
					labelConvCell == null || labelFallOffCell == null) {
				continue;
			}

			// Convert to float (with basic label encoding for strings)
			float id_lead = (float) idLeadCell.getNumericCellValue();
			float weeks_elapsed = (float) weeksElapsedSinceCreatedCell.getNumericCellValue();
			float weeks_diff = (float) weeksDiffPriorCell.getNumericCellValue();
			float real_lead_sum = (float) realLeadSumCell.getNumericCellValue();
			float process_week = (float) processWeekCell.getNumericCellValue();
			float mktg_gen = (float) mktgGenCell.getNumericCellValue();
			float sector = (float) sectorCell.getNumericCellValue();
			float businessModel = (float) businessModelLeadCell.getNumericCellValue();
			float amount = (float) amountCell.getNumericCellValue();

			// Labels
			float labelConvVal    = (float) labelConvCell.getNumericCellValue();
			float labelFallOffVal = (float) labelFallOffCell.getNumericCellValue();

			// Feature vector: 9 inputs
			float[] inArr = new float[]{
					id_lead,
					weeks_elapsed,
					weeks_diff,
					real_lead_sum,
					process_week,
					mktg_gen,
					sector,
					businessModel,
					amount
			};

			float[] outArr = new float[]{ labelConvVal, labelFallOffVal };

			featuresList.add(inArr);
			labelsList.add(outArr);
			rowCount++;
		}

		workbook.close();
		fis.close();

		int nRows = featuresList.size();
		int nIn = 9;   // number of input features
		int nOut = 2;  // number of output targets

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

		return new DataSet(featureMatrix, labelMatrix);
	}


	// ########################################################################
	// Existing methods
	// ########################################################################

	/**
	 * Calculate the utility values for each option, which is each lead in the portfolio
	 * 
	 * @return the chosen lead by the salesperson
	 * 
	 */
	private int decisionMakingLeadToWork (XoRoShiRo128PlusRandom _random) {
		
		int counter = 0;
		float sumUtilities = 0;
		float utility = 0;

		for(int i=0; i<this.portfolio.size(); i++) {
			Lead lead = this.portfolio.get(i);
			lead.updateLeadProbs(true);
		}
					
		for (int i = 0; i < this.portfolio.size(); i++ ) {
			
			Lead lead = this.portfolio.get(i);
			
			// calculate and save the utility (u = (1-r)*m*(1+B) - p_C)
			
			// calculate and save the utility (u = (1-r)*(1+B)*m/100 + r*convCert - \gamma p_C)
			utility = (float) ((1. - this.riskAversion) * lead.getMagnitude() + this.riskAversion * lead.getProbToBeConverted());
			
			utility = (float)Math.exp(utility);
						
			//if (ModelParameters.DEBUG == true && this.gamerAgentId == 0) {
				//System.out.println("A-" + this.gamerAgentId + " DM for lead" + i + ": convUnc=" + lead.getConvCertainty() + ", mag=" 
					//	+ lead.getMagnitude() + "--> U = " + utility);
			//}
								
			this.probOptions[counter] = utility;
					
			sumUtilities += utility;
			
			counter ++;		

	    }
		
		// update probs by dividing by the sum
		for (int i = 0; i < this.probOptions.length; i++) {
			
			if (this.probOptions[i] > 0)
				this.probOptions[i] /= sumUtilities;
			else
				this.probOptions[i] = 0;
			
			/*if (ModelParameters.DEBUG == true && this.gamerAgentId == 0) {
				System.out.println("prob lead " + i + " = " + this.probOptions[i] );
			}*/

		}
		
		// choose one at random
		
		int selectedOptionIndex = util.Functions.randomWeightedSelection 
			(this.probOptions, _random.nextDouble()); // [0, 1)
		
		return selectedOptionIndex;
	}
	

	/**
	 * 
	 * This function update the probs values of the lead, checks if a lead is converted when p_c >= thr
	 * or falls-off at random, as well as it creates new leads when a lead leaves the portfolio
	 * 
	 */
	private void updatePortfolio (SimState state) {
		
		Model model = (Model) state;				
		currentStep = (int) model.schedule.getSteps();
		
		for (int i = 0; i < this.portfolio.size(); i++) {

			boolean replaceLead = false;
			
			// check if converted
			if (this.portfolio.get(i).getProbToBeConverted() 
					>= model.params.getFloatParameter("thresholdForConversion")) {
			
				// lead obtained!!
				this.convertedLeads[currentStep] += 1;

				this.convertedLeadsByMagnitude[currentStep] += this.portfolio.get(i).getMagnitude();
				
				// if we have expected leads, we annotate if it was won 
				if (model.params.isParameterSet("fileForLeads")) {
					
					if (this.portfolio.get(i).getFinalStatus() == ModelParameters.LEAD_IS_WON) {
						this.expectedConvertedLeads[currentStep] += 1;
					} else {
						// we annotate it as LOST (mismatch)
						this.expectedFallOffLeads[currentStep] += 1;
					}
				}
				
				// check to replace to renew it
				replaceLead = true;
				
				//if (ModelParameters.DEBUG == true) 
					//System.out.println("A-" + this.gamerAgentId + ": Lead " + this.portfolio.get(i).getID() + " WON with pC = " + this.portfolio.get(i).getProbToBeConverted() + 
						//	" and pF = " + this.portfolio.get(i).getProbToFallOff() + "Expected output was " + this.portfolio.get(i).getFinalStatus());
				
			} else {
				
				double r = model.random.nextDouble();
				
				if (r < this.portfolio.get(i).getProbToFallOff()) {
					
					// the lead falls-off
					this.fallOffLeads[currentStep] += 1; 
					//System.out.println("agent " + this.gamerAgentId + "; new falloff lead");

					// if we have expected leads, we annotate if it was won (then, it will be a mismatch)
					if (model.params.isParameterSet("fileForLeads")) {
						
						if (this.portfolio.get(i).getFinalStatus() == ModelParameters.LEAD_IS_WON) {
							this.expectedConvertedLeads[currentStep] += 1;
						} else {
							// we annotate it as LOST (match)
							this.expectedFallOffLeads[currentStep] += 1;
						}						
					}
					
					// check to replace to renew it
					replaceLead = true;

					//if (ModelParameters.DEBUG == true) 
					//System.out.println("A-" + this.gamerAgentId + ": Lead " + this.portfolio.get(i).getID() + " FALLINGOFF with pC = " + this.portfolio.get(i).getProbToBeConverted() + 
						//	" and pF = " + this.portfolio.get(i).getProbToFallOff() + "Expected output was " + this.portfolio.get(i).getFinalStatus());				
				}
			}
				
			if (replaceLead) {	
				
				// we see if we have leads read from a file to get one
				if (model.params.isParameterSet("fileForLeads")) {
					
					// choose a lead from the list at random and set their values
					this.portfolio.get(i).generateValuesFromLeadData (model.random, model.params, currentStep);
					
					//System.out.println("Salesperson pos " + this.gamerAgentId + " renewed portfolio with lead: " + ((Lead)this.portfolio.get(i)).printStatsLead());

				} else 
					
					// generate at random as we don't have data
					this.portfolio.get(i).generateValuesNewLeadAtRandom (model.random, model.params, currentStep);
				
			}
	    }
			
	}
	
	/**
	 * 
	 */
	private void decideWeeklyWorkingHours(float avgPayLastStep, ModelParameters params) {
				
		if (this.pay[currentStep - 1] < avgPayLastStep) {
			
			this.workingHours[currentStep] = ModelParameters.WORKINGHOURSPERWEEK + params.getFloatParameter("extraWeeklyHours");
			
			if (ModelParameters.DEBUG == true)
				System.out.println("A-" + this.gamerAgentId +": Pay < avg. (" + this.pay[currentStep - 1] + " < " + avgPayLastStep + "). then Work Extra hs") ;
			
		} else {			
			this.workingHours[currentStep] = ModelParameters.WORKINGHOURSPERWEEK;

			if (ModelParameters.DEBUG == true)
				System.out.println("A-" + this.gamerAgentId +" Pay >= avg. (" + this.pay[currentStep - 1] + " >= " +  + avgPayLastStep + "). then NO extra hs") ;
		}
	}
	
	/**
	 * In this method we apply the compensation plan (bonus) when sales revenue is equal or above a quota Q
	 * 
	 * Quota (Q) is applied to revenue (leads * magnitude)
	 * 
	 * Bonus is directly linked to Quota by applying a rate (i.e., 20%) to the pay of the salesperson
	 * 
	 */
	private void calculatePayWithCompensation () {
				
		// we apply a compensation plan based on getting a quota
		// bonus is directly link to (salesRevenue - Quota)
				
		float aboveQuota = this.convertedLeadsByMagnitude[currentStep] - this.quota;
		
		if (aboveQuota > 0) {

			float bonus = this.rateForBonus * aboveQuota;
			//float bonus = 5;
			
			if (ModelParameters.DEBUG == true)
				System.out.println("A-" + this.gamerAgentId +" BONUS of " + bonus + " as Q was obtained (" + this.convertedLeadsByMagnitude[currentStep] + " >= Q(" + this.quota + "))") ;
			
			this.pay[currentStep] = this.convertedLeadsByMagnitude[currentStep]*ModelParameters.percForPay + bonus;
			
		} else {
			
			if (ModelParameters.DEBUG == true)
				System.out.println("A-" + this.gamerAgentId +" BASE SALARY, NO BONUS! No Q obtained (" + this.convertedLeadsByMagnitude[currentStep] + " < Q(" + this.quota + "))") ;
			
			this.pay[currentStep] = this.convertedLeadsByMagnitude[currentStep]*ModelParameters.percForPay;
			
		}		
	}

	// >>> 5) Utility method to create the input features for each lead
	private float[] extractFeatures(Lead lead, int currentStep) {
		// For demonstration, let's pick 3 features:
		// 1) lead magnitude, 2) lead convCertainty, 3) the current step
		return new float[] {
				lead.getMagnitude(),
				lead.getConvCertainty(),
				(float)currentStep
		};
	}
	
	
	//--------------------------- Steppable method --------------------------//	
	
	/**
	 * Step of the simulation.
	 * @param state - a simulation model object (SimState).
	 */
	
	//@Override	
	public void step(SimState state) {

		Model model = (Model) state;
		
		currentStep = (int) model.schedule.getSteps();

		// --------------------------------------------------
		// PHASE 1: Train the NN once at the start of the simulation
		// --------------------------------------------------
		if (currentStep == 0 && !hasTrainedOnce) {
			// Example: use 5 epochs, trainingRecordCount rows from /data_injection/ANNTrainingData.xlsx
			trainNeuralNetworkFromExcel("data_injection/ANNTrainingData.xlsx",
					9, // epochs
					trainingRecordCount);
			hasTrainedOnce = true;
		}
		
		// PHASE 2: DECIDE HOW MANY HOURS TO WORK DEPENDING ON THE POPULATION AVERAGE (ONLY WHEN t>0)
		if (currentStep > 0) {	
			
			// get the average with a 
			float valueToCompareWith = (float)(1. + model.params.getFloatParameter("socialComparison")) * model.getAvgPay(currentStep - 1);
							
			this.decideWeeklyWorkingHours(valueToCompareWith, model.params);			
			
		} else {			
			this.workingHours[currentStep] = ModelParameters.WORKINGHOURSPERWEEK;			
			
		}

		// PHASE 3: Define multiple strategies for sales person to choose, we specify strategy for each sales agent at the beginning of the simulation and will be following the same strategy throughout the simulation
		// strategy 1: choose by magnitude size
		// strategy 2: choose by conversion probability
		// strategy 2: choose by Expected Value

		// PHASE 2: DECIDE, FOR EVERY HOUR OF THE TIME-STEP (WEEK) THE LEADS TO WORK ON
		// asish: 40 hours : 2 x 20 hours time blocks.
		// asish: choose top 2 leads out of 3 lead (debug stage)
		// 2 params number leads to choose: 2, total leads (portfolio size): 3
		// selecting the top 2 leads based on the strategy chosen for selecting leads.


		for (int h = 0; h < this.workingHours[currentStep]; h++ ) {
			int chosenLead = this.decisionMakingLeadToWork(model.random);

			// Update leads, marking which was worked on
			for (int k = 0; k < this.portfolio.size(); k++) {
				Lead ld = this.portfolio.get(k);

				if (k == chosenLead) {
					ld.updateLeadProbs(true);
				} else {
					// A simpler way to penalize leads that were not worked on:
					// e.g. just add a small increase to probToFallOff, or use the old decay
					float currentFallOff = ld.getProbToFallOff();
					ld.setProbToFallOff( Math.min(1.0f, currentFallOff + 0.01f) );
					// or any simpler approximate update
					// NO neural net call here.
				}
			}

		}
		
		// PHASE 4: UPDATE THE PROBS (ONLY IF SALESPEOPLE WERE WORKING ON IT A WEEK) OF THE PORTFOLIO TO ALSO COUNT THOSE CONVERTED AND FALLEN-OFF
		this.updatePortfolio(state);		
		this.calculatePayWithCompensation();		
	}
}
