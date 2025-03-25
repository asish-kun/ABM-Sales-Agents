package controller;

import java.util.logging.Level;
import java.util.logging.Logger;


import model.*;
import util.RunStats;

/** 
 * Controller of the ABM4SALES model
 * 
 * This class is the controller to call the model  
 * It calls the model, run it to get all the steps and returns a list
 * of simulated values
 * 
 * @author mchica
 * @date 2022/09/22
 * @place Granada
 * 
 */

public class Controller {

	// LOGGING
	private static final Logger log = Logger.getLogger( Model.class.getName() );
	
	protected Model model = null;
	
	
	/**
	 * @return the ModelParameters object where all the parameters are defined
	 */
	public ModelParameters getModelParameters() {
		return model.getParametersObject();
	}


	/**
	 * Set the ModelParameters object for all the defined parameters 
	 */
	public void setModelParameters(ModelParameters _params) {
		model.setParametersObject(_params);
	}

	
	/**
	 * Constructor having config, seed and maxSteps
	 */
	public Controller(ModelParameters _params, String _paramsFile) {
				
		Model.setConfigFileName(_paramsFile);
		
		this.model = new Model(_params);
	}
		
	
	/**
	 * Run the model one time
	 * @return the statistics after running the model
	 * 
	 */
	public RunStats runModel() {
		
		// starting and looping the mode

		int mcRuns = model.getParametersObject().getIntParameter("MCRuns");
		int maxSteps = model.getParametersObject().getIntParameter("maxSteps");
		
		// object to store results into the stats object
		
		RunStats stats = new RunStats(mcRuns, maxSteps);
		
		// create the map of the KPI stats for the 3 different KPIs of this model
		stats.createKPIInMap(0, "CLs");
		stats.createKPIInMap(1, "CLsByMag");
		stats.createKPIInMap(2, "FLs");
		stats.createKPIInMap(3, "Pay");
		stats.createKPIInMap(4, "PeopleWorkExtra");
		stats.createKPIInMap(5, "PeopleWithBonus");
		
		// in case we loaded a file for leads, we save expected CLs
		if (model.getParametersObject().isParameterSet("fileForLeads")) {
			stats.createKPIInMap(6, "ExpectedCLs");
			stats.createKPIInMap(7, "ExpectedFLs");
		}
				
		
		log.log(Level.FINE, "\n** Starting MC agent-based simulation (" + ModelParameters.modelName + ")\n");
		log.log (Level.FINE, "\n" + model.getParametersObject().export()  + "\n");	

        System.out.print(mcRuns + " MC runs"  + ": ");
     
        
   		for (int i = 0; i <  mcRuns; i++) {
						
			// for each MC run we start the model and run it
    		        
			model.start();
			
			do {					
				if (!model.schedule.step(model)) break;
				 				
			} while (model.schedule.getSteps() <  maxSteps );			

			model.finish();	
					        
			
	        // store to the stats object for the KPIs of the model	
			
			stats.setKPIForRun(0, i, model.getConvertedLeadsArray());
			stats.setKPIForRun(1, i, model.getConvertedLeadsByMagnitudeArray());
			stats.setKPIForRun(2, i, model.getFallOffLeadsArray());
			stats.setKPIForRun(3, i, model.getPayArray());
			stats.setKPIForRun(4, i, model.getPeopleWorkingExtra());
			stats.setKPIForRun(5, i, model.getPeopleWithBonus());

			// in case we loaded a file for leads, we save expected CLs
			if (model.getParametersObject().isParameterSet("fileForLeads")) {
				stats.setKPIForRun(6, i, model.getExpectedConvertedLeadsArray());
				stats.setKPIForRun(7, i, model.getExpectedFallOffLeadsArray());
			}
			
			log.log(Level.FINE, "MC-" + (i+1) + "/" + 
					mcRuns + " ended\n\n");

	        System.out.print(".");
		}						
        
        System.out.println("");        		        			    	
        
		return stats;		
	}
		
}
