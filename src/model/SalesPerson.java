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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

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
	private final XoRoShiRo128PlusRandom random;

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

	private int trainingRecordCount = 1500;

	private boolean hasTrainedOnce = false;

	private LinkedHashMap<Integer, Float> lastSortedLeads = new LinkedHashMap<>();
	private LinkedHashMap<Integer, Float> lastChosenTop3  = new LinkedHashMap<>();

	//TODO: map strategies with numbers for easy input.
	public enum SalesStrategy {
		HIGHEST_EXPECTED_VALUE,
		HIGHEST_CONVERSION_PROB,
		LARGEST_MAGNITUDE,
		PROB_EXPECTED_VALUE,   // probability ∝ E_ij
		PROB_CONV_PROB,        // probability ∝ p̂_ij^c
		PROB_MAGNITUDE,        // probability ∝ m_ij
		ESCALATION_OF_COMMITMENT,
		RECENCY_WEIGHTED_RANDOM,
		RANDOM_SELECTION
	}

	private SalesStrategy strategy;

	private final float accuracy;
	private int   leadsToChoose;
	private int   myPortfolioSize;



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

	public void setNnManager(NeuralNetworkManager manager) {
		this.nnManager = manager;
	}

	public void setStrategy(SalesStrategy strategy) {
		this.strategy = strategy;
	}

	public SalesStrategy getStrategy(){return strategy;}

	public void setStrategyByNumber(int num) {
		switch(num) {
			case 1: this.strategy = SalesStrategy.HIGHEST_EXPECTED_VALUE; break;
			case 2: this.strategy = SalesStrategy.HIGHEST_CONVERSION_PROB;  break;
			case 3: this.strategy = SalesStrategy.LARGEST_MAGNITUDE;       break;
			case 4: this.strategy = SalesStrategy.PROB_EXPECTED_VALUE;       break;
			case 5: this.strategy = SalesStrategy.PROB_CONV_PROB;       break;
			case 6: this.strategy = SalesStrategy.PROB_MAGNITUDE;      break;
			case 7: this.strategy = SalesStrategy.ESCALATION_OF_COMMITMENT;           break;
			case 8: this.strategy = SalesStrategy.RECENCY_WEIGHTED_RANDOM;           break;
			case 9: this.strategy = SalesStrategy.RANDOM_SELECTION;  break;

			default: throw new IllegalArgumentException("Unknown strategy code: " + num);
		}
	}

	public LinkedHashMap<Integer, Float> getLastSortedLeads() {
		return lastSortedLeads;
	}
	public LinkedHashMap<Integer, Float> getLastChosenTop3() {
		return lastChosenTop3;
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
	// inlucde this at the end to pass strategy to sales person : SalesStrategy strategy
	public SalesPerson(
			int _agentId,
			ModelParameters _params,
			XoRoShiRo128PlusRandom _random,
			NeuralNetworkManager nnManager,
			float agentAccuracy,
			int   leadsToPick,
			int   portfolioSize) {

		this.gamerAgentId = _agentId;

		// ---------------- NEW: Initialize the neural network manager -----------
		// Example: Suppose we have 4 input features for [magnitude, convCertainty, probToBeConverted, probToFallOff],
		// or any set of features that you want. Adjust as needed.
		this.nnManager = nnManager;
		this.random = _random;
		//this.strategy = strategy;
		this.accuracy      = agentAccuracy;
		this.leadsToChoose = leadsToPick;
		this.myPortfolioSize = portfolioSize;
		
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
		
		this.rateForBonus = _params.getFloatParameter("rateForBonus");

		this.portfolio = new FastList<Lead>(this.myPortfolioSize);
		for (int i = 0; i < this.myPortfolioSize; i++) {
			this.portfolio.add(new Lead(_random, _params, 0, this.nnManager, this.accuracy));
		}
		this.probOptions = new double[this.myPortfolioSize];


		this.probOptions = new double[_params.getIntParameter("portfolioSize")];

		// --------------------------------------------------
		// PHASE 1: Train the NN once at the start of the simulation
		// --------------------------------------------------
//		if(!hasTrainedOnce) {
//			trainNeuralNetworkFromExcel("data_injection/ANNTrainingData.xlsx",
//					9, // epochs
//					trainingRecordCount);
//			hasTrainedOnce = true;
//		}

	}

	private float getFloatCell(Cell cell) {
		if (cell == null) return 0.0f; // or some default value
		return (float) cell.getNumericCellValue();
	}

	public float getAccuracy() {
		return this.accuracy;
	}

	private float leadWeightRecency(Lead ld) {
		int rec = ld.getWeeksSinceLastChosen(currentStep);
		// weight ↑ if chosen recently, also ↑ with timesWorkedOn
		return (1f / (1+rec)) + 0.1f*ld.getTimesWorkedOn();
	}


	// ---------------------------------------------------------------------
// 1.1  New helper to compute “the metric I’m sorting by”  -------------
// ---------------------------------------------------------------------
	private float metricFor(Lead ld) {
		switch (strategy) {
			// Highest Expected Value, Prob of Conversion & Magnitude
			case HIGHEST_CONVERSION_PROB:
				return ld.getProbToBeConverted();
			case HIGHEST_EXPECTED_VALUE:
				return ld.getMagnitude() * ld.getProbToBeConverted();
			case LARGEST_MAGNITUDE:
				return ld.getMagnitude();
			// new probabilistic strategies ↓ (added in §B)
			case PROB_EXPECTED_VALUE:
				return ld.getMagnitude() * ld.getProbToBeConverted();
			case PROB_CONV_PROB:
				return ld.getProbToBeConverted();
			case PROB_MAGNITUDE:
				return ld.getMagnitude();
			// Commitment & Recency
			case ESCALATION_OF_COMMITMENT:
				return ld.getMagnitude() + ld.getConvCertainty();
			case RECENCY_WEIGHTED_RANDOM:
				return leadWeightRecency(ld);        // helper in §B‑4
			default:
				return 0f;          // RANDOM_SELECTION / fallback
		}
	}

	// Draw *all* lead indices without replacement, using a custom weight fn
	private List<Integer> drawByWeights(Function<Lead,Float> weightFn) {
		int n = portfolio.size();
		float[] w = new float[n];
		float total = 0f;
		for (int i=0;i<n;i++) {               // compute weights
			w[i] = Math.max(1e-6f, weightFn.apply(portfolio.get(i)));
			total += w[i];
		}
		List<Integer> order = new ArrayList<>();
		for (int pick = 0; pick < n; pick++) {
			float r = random.nextFloat() * total;
			float acc = 0f;
			int sel = -1;
			for (int i = 0; i < n; i++) {
				if (w[i] == 0f) continue;
				acc += w[i];
				if (acc >= r) {
					sel = i;
					break;
				}
			}
			// <<< NEW: fallback if nothing matched
			if (sel < 0) {
				for (int i = 0; i < n; i++) {
					if (w[i] > 0f) {
						sel = i;
						break;
					}
				}
			}
			// <<< still guard just in case
			if (sel < 0) {
				throw new RuntimeException("drawByWeights: no selectable index!");
			}

			order.add(sel);
			total -= w[sel];
			w[sel] = 0f;
		}
		return order;
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
	// ########################################################################
	// NEW: Selecting Top Leads according to the chosen strategy
	// ########################################################################
	private void decisionMakingLeadToWork () {
		// 1. Create a list of all indices in the portfolio
		List<Integer> indices = new ArrayList<>();
		for (int i = 0; i < this.portfolio.size(); i++) {
			indices.add(i);
		}

		// 2. Sort indices based on selected strategy
		switch (this.strategy) {
			case HIGHEST_CONVERSION_PROB:
				indices.sort((a, b) -> Float.compare(
						portfolio.get(b).getPredictedProbToBeConverted(),
						portfolio.get(a).getPredictedProbToBeConverted()
				));
				break;
			case HIGHEST_EXPECTED_VALUE:
				indices.sort((a, b) -> Float.compare(
						portfolio.get(b).getMagnitude() * portfolio.get(b).getPredictedProbToBeConverted(),
						portfolio.get(a).getMagnitude() * portfolio.get(a).getPredictedProbToBeConverted()
				));
				break;
			case LARGEST_MAGNITUDE:
				indices.sort((a, b) -> Float.compare(
						portfolio.get(b).getMagnitude(),
						portfolio.get(a).getMagnitude()
				));
				break;
			case PROB_EXPECTED_VALUE:
				indices = drawByWeights(l -> l.getMagnitude()*l.getPredictedProbToBeConverted());
				break;
			case PROB_CONV_PROB:
				indices = drawByWeights(Lead::getPredictedProbToBeConverted);
				break;
			case PROB_MAGNITUDE:
				indices = drawByWeights(Lead::getMagnitude);
				break;
			case RECENCY_WEIGHTED_RANDOM:
				indices = drawByWeights(this::leadWeightRecency);
				break;
			case RANDOM_SELECTION:
				shuffleWithXoRo(indices);
				break;
			case ESCALATION_OF_COMMITMENT:
				// Weighted random, see the strategy in the next section (detailed below)
				indices = weightedRandomSelection();
				break;
			default:
				throw new IllegalArgumentException("Unknown strategy: " + this.strategy);
		}

		// 3. Store the entire sorted list in `lastSortedLeads`
		//    (For random strategies, `indices` is the random order.)
		lastSortedLeads.clear();
		for(int idx : indices) {
			Lead lead = portfolio.get(idx);
			lastSortedLeads.put(lead.getID(), metricFor(lead));
		}

		// 4. Select top 3 leads to work on
		List<Integer> topLeads = indices.subList(0, Math.min(this.leadsToChoose, indices.size()));

		lastChosenTop3.clear();
		for (int idx : topLeads) {
			Lead lead = portfolio.get(idx);
			lastChosenTop3.put(lead.getID(), metricFor(lead));
		}

		// 5. Actually update the leads we "worked on"
		for (int i = 0; i < portfolio.size(); i++) {
			Lead lead = portfolio.get(i);
			boolean workedOn = topLeads.contains(i);
			if (workedOn) {
				lead.incrementTimesWorkedOn();
				lead.noteChosen(currentStep);
			}
			lead.updateLeadProbs(workedOn);
		}
		if (ModelParameters.DEBUG) {
			System.out.printf("Agent %d working on leads: %s\n", this.gamerAgentId, topLeads);
		}
    }

	private void shuffleWithXoRo(List<Integer> list) {
		for (int i = list.size() - 1; i > 0; i--) {
			// pick a random index from 0..i
			int r = this.random.nextInt(i + 1);
			// swap
			int tmp = list.get(i);
			list.set(i, list.get(r));
			list.set(r, tmp);
		}
	}

	private List<Integer> weightedRandomSelection() {
		int size = portfolio.size();
		float[] weights = new float[size];
		float sumWeights = 0f;

		// 1) Compute each lead's weight
		//    EXAMPLE: escalation = (1 + timesWorkedOn),
		//    or (magnitude + convCertainty), or combination
		for (int i = 0; i < size; i++) {
			Lead ld = portfolio.get(i);

			// If you want to escalate with timesWorkedOn:
			 float w = 1f + ld.getTimesWorkedOn();

			// Or if you prefer some domain approach, e.g. magnitude + convCertainty:
//			float w = ld.getMagnitude() + ld.getConvCertainty();

			weights[i] = w;
			sumWeights += w;
		}

		// 2) Repeatedly draw from the distribution (without replacement)
		//    to pick 3 distinct leads
		List<Integer> chosen = new ArrayList<>();
		for (int pick = 0; pick < 3 && pick < size; pick++) {
			// pick a random float in [0, sumWeights)
			float r = this.random.nextFloat() * sumWeights;

			// find which index that corresponds to
			float accum = 0f;
			int selectedIdx = -1;
			for (int i = 0; i < size; i++) {
				if (weights[i] <= 0f) {
					// this lead was already selected in a previous pick
					continue;
				}
				accum += weights[i];
				if (accum >= r) {
					selectedIdx = i;
					break;
				}
			}

			// Mark that index as chosen
			chosen.add(selectedIdx);

			// Remove it from distribution for the next pick
			sumWeights -= weights[selectedIdx];
			weights[selectedIdx] = 0f;
		}

		// 'chosen' is the final set of lead INDICES chosen by weighted random
		return chosen;
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
			Lead lead = this.portfolio.get(i);
			boolean wasWorked = lead.getWeeksSinceLastChosen(currentStep) == 0;

			float  randomProbability = model.random.nextFloat();

			//TODO: Will only do this check if it is a chosen lead
			//TODO: Weather chosen or not fall off probability is constantly updated
			//TODO: Compare it with the actual probability of conversion

			if(randomProbability < this.portfolio.get(i).getProbToBeConverted() && wasWorked){

//				System.out.printf("Lead %d CONVERTED! Prob: %.2f\n",
//						this.portfolio.get(i).getID(),
//						this.portfolio.get(i).getProbToBeConverted());

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

			}else{


				double r = model.random.nextDouble();
				float fallOffThresh = model.params.getFloatParameter("fallOffProbability");

				if (r < fallOffThresh) {

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
			
			// check if converted
//			if (this.portfolio.get(i).getProbToBeConverted()
//					>= model.params.getFloatParameter("thresholdForConversion")) {
//
//				System.out.printf("Lead %d CONVERTED! Prob: %.2f\n",
//						this.portfolio.get(i).getID(),
//						this.portfolio.get(i).getProbToBeConverted());
//
//				// lead obtained!!
//				this.convertedLeads[currentStep] += 1;
//
//				this.convertedLeadsByMagnitude[currentStep] += this.portfolio.get(i).getMagnitude();
//
//				// if we have expected leads, we annotate if it was won
//				if (model.params.isParameterSet("fileForLeads")) {
//
//					if (this.portfolio.get(i).getFinalStatus() == ModelParameters.LEAD_IS_WON) {
//						this.expectedConvertedLeads[currentStep] += 1;
//					} else {
//						// we annotate it as LOST (mismatch)
//						this.expectedFallOffLeads[currentStep] += 1;
//					}
//				}
//
//				// check to replace to renew it
//				replaceLead = true;
//
//				//if (ModelParameters.DEBUG == true)
//					//System.out.println("A-" + this.gamerAgentId + ": Lead " + this.portfolio.get(i).getID() + " WON with pC = " + this.portfolio.get(i).getProbToBeConverted() +
//						//	" and pF = " + this.portfolio.get(i).getProbToFallOff() + "Expected output was " + this.portfolio.get(i).getFinalStatus());
//
//			} else {
//
//				double r = model.random.nextDouble();
//
//				if (r < this.portfolio.get(i).getProbToFallOff()) {
//
//					// the lead falls-off
//					this.fallOffLeads[currentStep] += 1;
//					//System.out.println("agent " + this.gamerAgentId + "; new falloff lead");
//
//					// if we have expected leads, we annotate if it was won (then, it will be a mismatch)
//					if (model.params.isParameterSet("fileForLeads")) {
//
//						if (this.portfolio.get(i).getFinalStatus() == ModelParameters.LEAD_IS_WON) {
//							this.expectedConvertedLeads[currentStep] += 1;
//						} else {
//							// we annotate it as LOST (match)
//							this.expectedFallOffLeads[currentStep] += 1;
//						}
//					}
//
//					// check to replace to renew it
//					replaceLead = true;
//
//					//if (ModelParameters.DEBUG == true)
//					//System.out.println("A-" + this.gamerAgentId + ": Lead " + this.portfolio.get(i).getID() + " FALLINGOFF with pC = " + this.portfolio.get(i).getProbToBeConverted() +
//						//	" and pF = " + this.portfolio.get(i).getProbToFallOff() + "Expected output was " + this.portfolio.get(i).getFinalStatus());
//				}
//			}
				
			if (replaceLead) {	
				
				// we see if we have leads read from a file to get one
				if (model.params.isParameterSet("fileForLeads")) {
					
					// choose a lead from the list at random and set their values
					this.portfolio.get(i).generateValuesFromLeadData (model.random, model.params, currentStep, this.accuracy);
					
					//System.out.println("Salesperson pos " + this.gamerAgentId + " renewed portfolio with lead: " + ((Lead)this.portfolio.get(i)).printStatsLead());

				} else 
					
					// generate at random as we don't have data
					this.portfolio.get(i).generateValuesNewLeadAtRandom (model.random, model.params, currentStep, this.accuracy);
				
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
		
		// PHASE 2: DECIDE HOW MANY HOURS TO WORK DEPENDING ON THE POPULATION AVERAGE (ONLY WHEN t>0)
		if (currentStep > 0) {	
			
			// get the average with a 
			float valueToCompareWith = (float)(1. + model.params.getFloatParameter("socialComparison")) * model.getAvgPay(currentStep - 1);
							
			this.decideWeeklyWorkingHours(valueToCompareWith, model.params);			
			
		} else {			
			this.workingHours[currentStep] = ModelParameters.WORKINGHOURSPERWEEK;
		}

		if (currentStep == 0) {
			// Week 1: No conversions or fall-offs, only NN-based probability update
			for (Lead lead : portfolio) {
				lead.updateLeadProbs(false); // update using NN, no leads were worked
				lead.incrementWeeksElapsed(); // still simulate time passing
			}
		} else {
			// Week 2 and beyond: run normal strategy
			this.decisionMakingLeadToWork();    // choose and mark top leads
			this.updatePortfolio(state);       // conversion & fall-off logic
			this.calculatePayWithCompensation(); // compute salary/bonus

			// Final: update probabilities AFTER conversion/fall-off
			for (Lead lead : portfolio) {
				lead.updateLeadProbs(false); // re-evaluate probabilities for next round
				lead.incrementWeeksElapsed(); // advance weeks for every lead
			}
		}


	}
	//		for (int h = 0; h < this.workingHours[currentStep]; h++ ) {
//			int chosenLead = this.decisionMakingLeadToWork(model.random);
//
//			// Update leads, marking which was worked on
//			for (int k = 0; k < this.portfolio.size(); k++) {
//				Lead ld = this.portfolio.get(k);
//
//				ld.updateLeadProbs(k == chosenLead);
////				if (k == chosenLead) {
////
////				} else {
////					// A simpler way to penalize leads that were not worked on:
////					// e.g. just add a small increase to probToFallOff, or use the old decay
////					float currentFallOff = ld.getProbToFallOff();
////					ld.setProbToFallOff( Math.min(1.0f, currentFallOff + 0.01f) );
////					// or any simpler approximate update
////					// NO neural net call here.
////				}
//			}
//
//		}

}
