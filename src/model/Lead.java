package model;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;
import view.LeadData;

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
	
	private float decayRate;				// \in [0,1], it is the decay applied to the prob. when not working on it
	
	private byte finalStatus;				// final status of the lead in case it was read from a file of leads
	
	private int ID;							// ID of the lead in case it is read from file
	
	private String businessModel;			// type of business of the lead in case it is read from file

	
	// ------------  dynamic variables

	private float probToBeConverted;		// \in [0,1]; the prob. of be converted by the salesperson

	private float probToFallOff;			// \in [0,1]; it is the probability to fall-off after not working on it
	
	
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

	public float getDecayRate() {
		return decayRate;
	}

	public void setDecayRate(float decayRate) {
		this.decayRate = decayRate;
	}

	public float getProbToBeConverted() {
		return probToBeConverted;
	}

	public void setProbToBeConverted(float probToBeConverted) {
		this.probToBeConverted = probToBeConverted;
	}

	public float getProbToFallOff() {
		//return (1 - this.probToBeConverted);
		return this.probToFallOff;
	}

	public void setProbToFallOff(float probToFallOff) {
		this.probToFallOff = probToFallOff;
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
	
	//--------------------------- Constructor ---------------------------//
	/**
	 * constructor of Lead
//	 * @param atRandom is to set magnitude & convCertainty at random
	 */
	public Lead (XoRoShiRo128PlusRandom _random, ModelParameters _params, int _step){
		
		this.ID = -1;
		this.finalStatus = ModelParameters.LEAD_UNKNOWN_FINAL_STATUS;
		this.businessModel = "N/A";
		
		// this parameter is global for the whole set of leads
		this.decayRate = _params.getFloatParameter("decayRateLeads");
						
		// we see if we have leads read from a file to get one
		if (_params.isParameterSet("fileForLeads")) {			
			
			// choose a lead from the list at random and set their values			
			generateValuesFromLeadData (_random, _params, _step);
			
		} else {
			
			// generate those values at random
			generateValuesNewLeadAtRandom (_random, _params, _step);
			
		}
		
		
	}

	
	/**
	 * when a lead falls-off or we init the lead, instead of creating a new one, we set their values
	 * by calling this function and reset all the parameters for the new lead
	 * 
	 * @param 
	 */
	public void generateValuesNewLeadAtRandom (XoRoShiRo128PlusRandom _random, ModelParameters _params, int _step){

		float _magnitude, _convCer;
		
		// initialization of the dynamics variables
		this.probToBeConverted = 0;		
		this.probToFallOff = 0; 	// initially, it was set to  1 - this.convCertainty but a lead falls-off quickly...Make more sense to start with 0, as p_c, while increasing it when not working on it

		this.stepWhenCreated = _step;

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
		
	}

	/**
	 * when a lead falls-off or we init the lead, instead of creating a new one, we set their values
	 * by calling this function and reset all the parameters for the new lead. 
	 * Difference from function generateValuesNewLeadAtRandom is we use a lead read from file to set their values
	 * 
	 * @param 
	 */
	public void generateValuesFromLeadData (XoRoShiRo128PlusRandom _random, ModelParameters _params, int _step){
	
		// initialization of the dynamics variables
		this.probToBeConverted = 0;		
		this.probToFallOff = 0; 	// initially, it was set to  1 - this.convCertainty but a lead falls-off quickly...Make more sense to start with 0, as p_c, while increasing it when not working on it

		this.stepWhenCreated = _step;
		
		// getting a random read data from the list
		LeadData _readLead = _params.getReadLeadAtRandom(_random.nextInt(_params.getNumberOfReadLeads()));
		
		this.magnitude = Float.parseFloat(_readLead.getAmount());	
		this.convCertainty = Float.parseFloat(_readLead.getCertaintyForConv());	
		this.ID = Integer.parseInt(_readLead.getLeadID());
		this.businessModel = _readLead.getBusinessModel();
		
		if (_readLead.getConvertedLead().compareTo("1") == 0) 
			this.finalStatus = ModelParameters.LEAD_IS_WON;
		else
			this.finalStatus = ModelParameters.LEAD_IS_LOST;
		
	}


	/**
	 * 
	 * With this function we update the probs of the lead depending on its work by the salesperson or not
	 * 
	 */
	public void updateLeadProbs (boolean hasWorked) {
		
		if (hasWorked) {
			
			//System.out.println("prob before " + this.probToBeConverted);
			
			// the salesperson worked on that lead 		
			this.probToBeConverted += (this.convCertainty)  // / ModelParameters.WORKINGHOURSPERWEEK) 
					* (1. - this.probToBeConverted);
			
			if (this.probToBeConverted > 1)
				this.probToBeConverted = 1;
			
			//System.out.println("prob after " + this.probToBeConverted);
			
		} else {
			
			// the salesperson did not work on the lead
			
			/*this.probToBeConverted -= (this.decayRate / ModelParameters.WORKINGHOURSPERWEEK) 
					* this.probToBeConverted;
			
			if (this.probToBeConverted < 0)
				this.probToBeConverted = 0;
			*/
			
			// by not working on it, we increase its probability to fall-off
			// I prefer not to use this one as it is always increasing as probToBeConverted and then, we
			// need mechanisms not to have very high values for both probs and at the end, it is similar			
			
			//this.probToFallOff += (this.decayRate / ModelParameters.WORKING_DAYS) * (1. - this.probToFallOff);

			this.probToFallOff += this.decayRate // / ModelParameters.WORKINGHOURSPERWEEK) 
			* (1. - this.probToFallOff);
	
			if (this.probToFallOff > 1)
				this.probToFallOff = 1;			
		}
		
		/*if (this.ID == 231043728) {
			System.out.println(	this.printStatsLead());
		}*/
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
		result += "probToFallOff = " + this.probToFallOff + "; ";

		return result;
		
	}


}

