package model;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import view.LeadData;

import static model.Model.log;

/**
 * Class to store one lead which will belong to the portfolio of a salesperson
 * 
 * @author mchica
 * @date 2022/10/29
 * @place Oeiras, Lisboa
 */

public class Lead {		

	// ----------- static parameters of the agent
	private int stepWhenCreated;			// this timestep is when the lead was 
	
	private float magnitude;				// \in [0,1]; the magnitude of the lead
	
	private float convCertainty;			// \in [0,1]; the uncertainty to be converted
	
	private double decayRate;				// \in [0,1], it is the decay applied to the prob. when not working on it
	
	private byte finalStatus;				// final status of the lead in case it was read from a file of leads
	
	private int ID;							// ID of the lead in case it is read from file
	
	private String businessModel;			// type of business of the lead in case it is read from file

	
	// ------------  dynamic variables

	private float probToBeConverted;		// \in [0,1]; the prob. of be converted by the salesperson
	private float predictedProbToBeConverted;
	public float getPredictedProbToBeConverted() {
		return predictedProbToBeConverted;
	}
	public float pHat() { return predictedProbToBeConverted; }

	private float actualProbToBeConverted;

	private float accuracy; // Accuracy for converting predicted -> actual. Provided by user param.

	private NeuralNetworkManager nnManager;

	private XoRoShiRo128PlusRandom random;

	// asish: -------------- generate variable to match attributes in training data

	private int weeksElapsed;          // weeks_elapsed_since_created
	private int weeksDiffPrior;        // weeks_diff_prior
	private float processWeek;         // process_week
	private float mktgGen;             // mktg_gen
	private float encodedSector;       // sector (encoded)
	private float amount;              // amount (already present as 'magnitude', you can map or duplicate)
	private float entryByWeek;       // entry_by_week
	private float productService;    // product_service (0/1 encoded already)

	private int timesWorkedOn = 0;

	private int lastChosenStep = -1;
	public void noteChosen(int step) { lastChosenStep = step; }

	// Functions or methods of the class
	// -----------------------------------------------
	// -----------------------------------------------
	public String getBusinessModel() {
		return businessModel;
	}

	public void setBusinessModel(String businessModel) {
		this.businessModel = businessModel;
	}
	
	public int getStepWhenCreated() {
		return stepWhenCreated;
	}

	public void setStepWhenCreated(int stepWhenCreated) {
		this.stepWhenCreated = stepWhenCreated;
	}

	public float getMagnitude() {
		return magnitude;
	}

	public void setMagnitude(float magnitude) {
		this.magnitude = magnitude;
	}

	public float getConvCertainty() {
		return convCertainty;
	}

	public void setConvUncertainty(float convCertainty) {
		this.convCertainty = convCertainty;
	}

	public double getDecayRate() {
		return decayRate;
	}

	public void setDecayRate(double decayRate) {
		this.decayRate = decayRate;
	}

	public float getProbToBeConverted() {
		return probToBeConverted;
	}

	public void setProbToBeConverted(float probToBeConverted) {
		this.probToBeConverted = probToBeConverted;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public byte getFinalStatus() {
		return finalStatus;
	}

	public void setFinalStatus(byte finalStatus) {
		this.finalStatus = finalStatus;
	}

	public void incrementWeeksElapsed() {
		this.weeksElapsed++;
	}

	public void incrementTimesWorkedOn() {
		timesWorkedOn++;
	}

	public int getTimesWorkedOn() {
		return timesWorkedOn;
	}

	public int getWeeksSinceLastChosen(int currentStep){
		return (lastChosenStep<0)? Integer.MAX_VALUE : currentStep - lastChosenStep;
	}
	
	//--------------------------- Constructor ---------------------------//
	/**
	 * constructor of Lead
//	 * @param atRandom is to set magnitude & convCertainty at random
	 */
	public Lead (XoRoShiRo128PlusRandom _random, ModelParameters _params, int _step, NeuralNetworkManager nnManager, float agentAccuracy){
		
		this.ID = -1;
		this.finalStatus = ModelParameters.LEAD_UNKNOWN_FINAL_STATUS;
		this.businessModel = "N/A";
		
		// this parameter is global for the whole set of leads
		this.decayRate = 0.1;

		this.nnManager = nnManager;

		this.random = _random;

		// If there is an "accuracy" parameter in the console, store it here.
		this.accuracy = agentAccuracy;
						
		// we see if we have leads read from a file to get one
		if (_params.isParameterSet("fileForLeads")) {			
			
			// choose a lead from the list at random and set their values			
			generateValuesFromLeadData (_random, _params, _step, this.accuracy);
			
		} else {
			
			// generate those values at random
			generateValuesNewLeadAtRandom (_random, _params, _step, this.accuracy);
			
		}
		
		
	}

	
	/**
	 * when a lead falls-off or we init the lead, instead of creating a new one, we set their values
	 * by calling this function and reset all the parameters for the new lead
	 * 
	 * @param 
	 */
	public void generateValuesNewLeadAtRandom (XoRoShiRo128PlusRandom _random, ModelParameters _params, int _step, float agentAccuracy){

		float _magnitude, _convCer;

//		this.probToBeConverted = 0; // Check it's not affecting sales Agents performance
//		this.probToFallOff     = 0; // Set it to the globally defined fall off value

		// 1) Randomly pick an ID, business model, etc., from your CSV just as before:
		LeadData _readLead = _params.getReadLeadAtRandom(_random.nextInt(_params.getNumberOfReadLeads()));
		this.magnitude     = Float.parseFloat(_readLead.getAmount());
		this.convCertainty = Float.parseFloat(_readLead.getCertaintyForConv());
		this.ID            = Integer.parseInt(_readLead.getLeadID());
		this.businessModel = _readLead.getBusinessModel();
		// (Same code you had before...)

		// 2) For the new variables, generate random values that make sense:
		this.weeksElapsed   = 0;
		this.weeksDiffPrior = 0;
		this.stepWhenCreated   = _step;
//		this.processWeek    = (float) _random.nextInt(8); // e.g. 0..11
		this.mktgGen        = _random.nextFloat();         // e.g. 0..1

		// 3) Sectors and business models can be random picks or explicit encoding:
		// Example: 0 = 'tech', 1 = 'finance', 2 = 'healthcare'
		this.encodedSector = 1 + (float) _random.nextInt(20);

		// Example: 0 = 'b2b', 1 = 'b2c'
		this.productService = (float) _random.nextInt(2);

		this.entryByWeek    = _random.nextFloat() * 20f;

		// 4) If you want "amount" to be separate from "magnitude," you can do:
		this.amount = _random.nextFloat() * 1000f; // random lead total in 0..1000

		do { 			
			_magnitude = -1;

			if (_params.getIntParameter("distributionForLeads") == ModelParameters.NORMAL_LEADS_MAGNITUDE) {
				double standardNormal = _random.nextGaussian();
				_magnitude = (float)(_params.getFloatParameter("avgLeadsMagnitude") +
						_params.getFloatParameter("stdevLeadsMagnitude") * standardNormal);
			}
			else if (_params.getIntParameter("distributionForLeads") == ModelParameters.UNIFORM_LEADS_MAGNITUDE ) {
				// instead of using a Normal distribution, we apply an uniform to have more diversity in the
				_magnitude = ModelParameters.MIN_MAGNITUDE + _random.nextFloat() * (ModelParameters.MAX_MAGNITUDE - ModelParameters.MIN_MAGNITUDE);

				//System.out.println("uniform distrib. used");
			}				
			
		} while (_magnitude > 1 || _magnitude < 0);

		this.magnitude = _magnitude;
		
		// REMOVED: NO ORTHOGONALITY FOR conversion uncertainty 
		
		/*if (_magnitude > ((ModelParameters.MAX_MAGNITUDE - ModelParameters.MIN_MAGNITUDE) / 2))
			_convCer = (float) _random.nextFloat((float)0, (float)0.5);
		else
			_convCer = (float) _random.nextFloat((float)0.5, (float)1); */
		
		// END REMOVED: NO ORTHOGONALITY FOR conversion uncertainty 

		_convCer = _random.nextFloat();  // Generates a value in [0, 1)

		this.convCertainty = _convCer;

		// ← insert here:
		float[] feats = asNNFeatures();
		float[] pred  = nnManager.predictScaled(feats);
		this.predictedProbToBeConverted = pred[0];
		this.probToBeConverted          = pred[0];
//		this.probToFallOff              = 1.0f - _random.nextFloat();
//		log.info("New random Lead @ step " + _step + ": " + printStatsLead());
		
	}

	/**
	 * when a lead falls-off or we init the lead, instead of creating a new one, we set their values
	 * by calling this function and reset all the parameters for the new lead. 
	 * Difference from function generateValuesNewLeadAtRandom is we use a lead read from file to set their values
	 * 
	 * @param 
	 */
	public void generateValuesFromLeadData (XoRoShiRo128PlusRandom _random, ModelParameters _params, int _step, float agentAccuracy){
	
		// initialization of the dynamics variables
		this.probToBeConverted = 0;		
//		this.probToFallOff = 0; 	// initially, it was set to  1 - this.convCertainty but a lead falls-off quickly...Make more sense to start with 0, as p_c, while increasing it when not working on it

		this.stepWhenCreated = _step;
		
		// getting a random read data from the list
		LeadData _readLead = _params.getReadLeadAtRandom(_random.nextInt(_params.getNumberOfReadLeads()));

		// weeksElapsed
		String weeksElapsedStr = _readLead.getWeeksElapsed();
		if (weeksElapsedStr == null) {
			weeksElapsedStr = "0"; // default value for integer
		}
		this.weeksElapsed = 0;

		// weeksDiffPrior
		String weeksDiffPriorStr = _readLead.getWeeksDiffPrior();
		if (weeksDiffPriorStr == null) {
			weeksDiffPriorStr = "0";
		}
		this.weeksDiffPrior = 0;

		// realLeadSum
		String realLeadSumStr = _readLead.getRealLeadSum();
		if (realLeadSumStr == null) {
			realLeadSumStr = "0.0"; // default value for float
		}

		// processWeek
		String processWeekStr = _readLead.getProcessWeek();
		if (processWeekStr == null) {
			processWeekStr = "0.0";
		}
		this.processWeek = Float.parseFloat(processWeekStr);

		// mktgGen
		String mktgGenStr = _readLead.getMktgGen();
		if (mktgGenStr == null) {
			mktgGenStr = "0.0";
		}
		this.mktgGen = Float.parseFloat(mktgGenStr);

		// encodedSector
		String encodedSectorStr = _readLead.getSector();
		if (encodedSectorStr == null) {
			encodedSectorStr = "0.0";
		}
		this.encodedSector = Float.parseFloat(encodedSectorStr);

		// encodedBusinessModel
		String encodedBusinessModelStr = _readLead.getBusinessModelEncoded();
		if (encodedBusinessModelStr == null) {
			encodedBusinessModelStr = "0.0";
		}
		this.productService = Float.parseFloat(encodedBusinessModelStr);

		this.entryByWeek    = _random.nextFloat() * 20f;

		// amount
		String amountStr = _readLead.getAmount();
		if (amountStr == null) {
			amountStr = "0.0";
		}
		this.amount = Float.parseFloat(amountStr);

		this.magnitude = Float.parseFloat(_readLead.getAmount());	
		this.convCertainty = Float.parseFloat(_readLead.getCertaintyForConv());
		// initialize both our prediction & “actual” p_conversion:
		float[] feats = asNNFeatures();
		float[] pred  = nnManager.predictScaled(feats);
		this.predictedProbToBeConverted = pred[0];
		this.probToBeConverted          = pred[0];

		// set fall‑off = 1 – p_conversion
//		this.probToFallOff = 1.0f - _random.nextFloat();
		this.ID = Integer.parseInt(_readLead.getLeadID());
		this.businessModel = _readLead.getBusinessModel();
		
		if (_readLead.getConvertedLead().compareTo("1") == 0) 
			this.finalStatus = ModelParameters.LEAD_IS_WON;
		else
			this.finalStatus = ModelParameters.LEAD_IS_LOST;

//		log.info("New file‐based Lead @ step " + _step + ": " + printStatsLead());
	}

	private float[] asNNFeatures() {
		return new float[] {
				weeksElapsed,
				weeksDiffPrior,
				entryByWeek,
				amount,
				encodedSector,
				mktgGen,
				productService
		};
	}

	/**
	 * 
	 * With this function we update the probs of the lead depending on its work by the salesperson or not
	 * 
	 */
	public void updateLeadProbs(boolean hasWorked) {
		float[] feats = asNNFeatures();
		float[] pred  = nnManager.predictScaled(feats);

		this.predictedProbToBeConverted = pred[0];

		float delta = accuracy * predictedProbToBeConverted; // percentage-based margin
		float lowerBound = Math.max(0f, predictedProbToBeConverted - delta);
		float upperBound = Math.min(1f, predictedProbToBeConverted + delta);

		float range = upperBound - lowerBound;
		float randomOffset = (float) Math.random() * range;

		this.probToBeConverted = lowerBound + randomOffset;
	}


	/**
	 * 
	 * Print out by returning a string with the info about the lead
	 * 
	 */
	public String printStatsLead () {
		
		String result = "";

		result += "ID = " + this.ID + "; ";		
		result += "businessModel = " + this.businessModel+ "; ";
		result += "finalStatus = " + this.finalStatus + "; ";		
		result += "magnitude = " + this.magnitude + "; ";		
		result += "convCer = " + this.convCertainty + "; ";
		result += "decayRate = " + this.decayRate+ "; ";
		result += "probToBeConverted = " + this.probToBeConverted + "; ";
//		result += "probToFallOff = " + 0.1 + "; ";

		return result;
		
	}


}

