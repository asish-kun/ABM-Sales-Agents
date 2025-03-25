package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;

import controller.Controller;
import model.Model;
import model.ModelParameters;


/**
 * SA class to run and save results when running the MC model with different parameter configuration
 * from a config base. Two main functions, the first one is for running a SA on one parameter and the 
 * second one, for two parameters
 * 
 * @author mchica
 * @date 2022/06/15
 * @place Oeiras, Lisboa
 */


public class SensitivityAnalysis {		

	static String keyParam1;
	static double minParam1;
	static double maxParam1;
	static double stepParam1;
	static DoubleArrayList valuesParam1;

	static String keyParam2;
	static double minParam2;
	static double maxParam2;
	static double stepParam2;
	static DoubleArrayList valuesParam2;

	/**
	 * Set info of the first parameter and also create the array of values
	 * 
	 * @param _key the key of the parameter name in the Model's parameter map
	 * @param _min the min value of the parameter
	 * @param _max the max value of the parameter
	 * @param _step the step value for running the values from min to max
	 */
	public static void setParam1Info (String _key, double _min, double _max, double _step) {

		keyParam1 = _key;
		minParam1 = _min;
		maxParam1 = _max;
		stepParam1 = _step;		
		
		// *********************  Array with the first parameter's values
		
		// create the arrays with the values for the first parameter
		valuesParam1 = new DoubleArrayList();

		// save the values to the array starting by the min value and adding step value until max
		double value = minParam1;	
		
		while (value <= maxParam1 ){					    	
			valuesParam1.add(value);
			value += stepParam1;
		}
		valuesParam1.add(value);
	}
	
	/**
	 * Reset the list of values for the SA of the first parameter
	 * 
	 * @param _values is a double array with 
	 */
	public static void resetParam1Values (DoubleArrayList _values) {
		
		valuesParam1 = _values;
	}
	
	/**
	 * Reset the list of values for the SA of the second parameter
	 * 
	 * @param _values is a double array with 
	 */
	public static void resetParam2Values (DoubleArrayList _values) {
		
		valuesParam2 = _values;
	}
	
	/**
	 * Set info of the second parameter and also create the array of values
	 * 
	 * @param _key the key of the parameter name in the Model's parameter map
	 * @param _min the min value of the parameter
	 * @param _max the max value of the parameter
	 * @param _step the step value for running the values from min to max
	 */
	public static void setParam2Info (String _key, double _min, double _max, double _step) {

		keyParam2 = _key;
		minParam2 = _min;
		maxParam2 = _max;
		stepParam2 = _step;		
		
		// *********************  Array with the second parameter's values
		
		// create the arrays with the values for the second parameter
		valuesParam2 = new DoubleArrayList();

		// save the values to the array starting by the min value and adding step value until max
		double value = minParam2;	
		
		while (value <= maxParam2 ){					    	
			valuesParam2.add(value);
			value += stepParam2;
		}
		valuesParam2.add(value);
	}
	
	
	// LOGGING
	private static final Logger log = Logger.getLogger( Model.class.getName() );
	
		
	/**
	 * Static function to run a SA (OAT) on one parameters
	 * 
	 */	
	public static void runSAOnOneParameter (ModelParameters _params, String _paramsFile, 
			File fileAllMC, File fileSummaryMCRuns, File fileAllMCLQ, File fileSummaryMCRunsLQ) {

		// create output files in case they exist as we will append the simulation contents
		
		if (fileAllMC.exists() && !fileAllMC.isDirectory()) {
			fileAllMC.delete();
		}
		
		if (fileSummaryMCRuns.exists() && !fileSummaryMCRuns.isDirectory()) {
			fileSummaryMCRuns.delete();
		}
		
	 	if (ModelParameters.LQ) {
	
			if (fileAllMCLQ.exists() && !fileAllMCLQ.isDirectory()) {
				fileAllMCLQ.delete();
			}	 
		 	
			if (fileSummaryMCRunsLQ.exists() && !fileSummaryMCRunsLQ.isDirectory()) {
				fileSummaryMCRunsLQ.delete();
			}	
		}
											
	    Controller controller = new Controller (_params, _paramsFile);

	    for (int i = 0; i < valuesParam1.size(); i++ ) {

    		// set the param1 value for running the SA on one parameter
	    	_params.setParameterValue(keyParam1, valuesParam1.get(i));

	        System.out.println("-> OAT for " + keyParam1 + " = " + _params.getFloatParameter(keyParam1)  + "\n" );

			log.log(Level.FINE, "\n****** Running Monte-Carlo simulation for a one parameter OAT configuration : " + keyParam1 + " = " + 
					_params.getFloatParameter(keyParam1) + " \n");
	
			RunStats stats;

			long time1 = System.currentTimeMillis ();
			
	 		// running the model with the MC simulations
	 		stats = controller.runModel();		

	 		long  time2  = System.currentTimeMillis( );
	 		System.out.println("\n****** " + (double)(time2 - time1)/1000 + "s spent during the OAT simulation");

	 		// calculate the stats for this run and set the name of this experiment
	 		stats.setExpName(keyParam1 + ";" + _params.getFloatParameter(keyParam1) );		    	
	 		stats.calcAllStats();

			PrintWriter out = new PrintWriter(System.out, true);
			_params.printParameters(out);
			 
	 		// print the stats in the screen 		
	        System.out.println("\n****** Stats of this OAT configuration ******\n");
	 		stats.printSummaryStats (out, false);
		 	
	 		if (ModelParameters.LQ) 
	 			stats.printSummaryStatsByAveragingLastQuartile(out, false);

			// print the stats into a file
			System.out.println("\n****** Stats of this OAT configuration also saved into a file ******\n");
			
			PrintWriter printWriter;
	         
	 		try {
	 			
	 			// print all the runs in a file 			
	 			printWriter = new PrintWriter (new FileOutputStream(fileAllMC, true));
	 			
	 			if ( fileAllMC.exists() && !fileAllMC.isDirectory() )
	 		        stats.appendAllStats (printWriter);		 		        
	 		    else
		 			stats.printAllStats (printWriter, false);	
	 	        printWriter.close ();       	
	 	        
	 	        // print the summarized MC runs in a file
	 			printWriter = new PrintWriter (new FileOutputStream(fileSummaryMCRuns, true));
	 	        
	 	        if ( fileSummaryMCRuns.exists() && !fileSummaryMCRuns.isDirectory() )
	 	    	    stats.appendSummaryStats (printWriter);		 		        
	 		    else		 		    	
	 		    	stats.printSummaryStats (printWriter, false);	
	 	        printWriter.close (); 
	 	        
		 		if (ModelParameters.LQ) {
	
		 	        // repeat for the LQ stats
		 	        // print all the runs in a file 			
		 			printWriter = new PrintWriter (new FileOutputStream(fileAllMCLQ, true));
		 			
		 			if ( fileAllMCLQ.exists() && !fileAllMCLQ.isDirectory() )
			 			stats.printAllStatsByAveragingLastQuartile(printWriter, true);	      
		 		    else
			 			stats.printAllStatsByAveragingLastQuartile(printWriter, false);	
		 	        printWriter.close ();       	
		 	        
		 	        // print the summarized MC runs in a file
		 			printWriter = new PrintWriter (new FileOutputStream(fileSummaryMCRunsLQ, true));
		 	        
		 	        if ( fileSummaryMCRunsLQ.exists() && !fileSummaryMCRunsLQ.isDirectory() )
		 		    	stats.printSummaryStatsByAveragingLastQuartile(printWriter, true);	
		 		    else		 		    	
		 		    	stats.printSummaryStatsByAveragingLastQuartile(printWriter, false);	
		 	        printWriter.close ();    
		 		}
		 		
	 		} catch (FileNotFoundException e) {
	 			
	 			// TODO Auto-generated catch block
	 			e.printStackTrace();
	 			
	 		    log.log( Level.SEVERE, e.toString(), e );
	 		}     	
	    }	    					
		
	}

	/**
	 * Static function to run a SA (OAT) on two parameters
	 * 
	 */	
	public static void runSAOnTwoParameters (ModelParameters _params, String _paramsFile, 
			File fileAllMC, File fileSummaryMCRuns, File fileAllMCLQ, File fileSummaryMCRunsLQ) {

		// create output files in case they exist as we will append the simulation contents
		
		if (fileAllMC.exists() && !fileAllMC.isDirectory()) {
			fileAllMC.delete();
		}
		
		if (fileSummaryMCRuns.exists() && !fileSummaryMCRuns.isDirectory()) {
			fileSummaryMCRuns.delete();
		}

 		if (ModelParameters.LQ) {
			if (fileAllMCLQ.exists() && !fileAllMCLQ.isDirectory()) {
				fileAllMCLQ.delete();
			}
			
			if (fileSummaryMCRunsLQ.exists() && !fileSummaryMCRunsLQ.isDirectory()) {
				fileSummaryMCRunsLQ.delete();
			}	
 		}		
 		
	    Controller controller = new Controller (_params, _paramsFile);

	    for (int i = 0; i < valuesParam1.size(); i++ ) {

		    for (int j = 0; j < valuesParam2.size(); j++ ) {
	    		    		
	    		// set the (param1, param2) values for running the SA on two variables
		    	_params.setParameterValue(keyParam1, valuesParam1.get(i));
		    	_params.setParameterValue(keyParam2, valuesParam2.get(j));

		        System.out.println("-> OAT for " + keyParam1 + " = " + _params.getFloatParameter(keyParam1) 
		        + " " + keyParam2 + " = " + _params.getFloatParameter(keyParam2) + "\n");

				log.log(Level.FINE, "\n****** Running Monte-Carlo simulation for a 2 parameters OAT configuration : " + keyParam1 + " = " + 
						_params.getFloatParameter(keyParam1) + " "+ keyParam2 + "  = " + _params.getFloatParameter(keyParam2) + " \n");
		
				RunStats stats;

				long time1 = System.currentTimeMillis ();
				
		 		// running the model with the MC simulations
		 		stats = controller.runModel();		

		 		long  time2  = System.currentTimeMillis( );
		 		System.out.println("\n****** " + (double)(time2 - time1)/1000 + "s spent during the OAT simulation");

		 		// calculate the stats for this run and set the name of this experiment
		 		stats.setExpName(keyParam1 + ";" + _params.getFloatParameter(keyParam1) + ";" + keyParam2 + ";" + _params.getFloatParameter(keyParam2) );		    	
		 		stats.calcAllStats();

				PrintWriter out = new PrintWriter(System.out, true);
				_params.printParameters(out);
				 
		 		// print the stats in the screen 		
		        System.out.println("\n****** Stats of this OAT configuration ******\n");
		 		stats.printSummaryStats (out, false);
		 		
		 		if (ModelParameters.LQ) {
		 			stats.printSummaryStatsByAveragingLastQuartile(out, false);
		 		}
		 		
				// print the stats into a file
				System.out.println("\n****** Stats of this OAT configuration also saved into a file ******\n");
				
				PrintWriter printWriter;
		         
		 		try {
		 			
		 			// print all the runs in a file 			
		 			printWriter = new PrintWriter (new FileOutputStream(fileAllMC, true));
		 			
		 			if ( fileAllMC.exists() && !fileAllMC.isDirectory() )
		 		        stats.appendAllStats (printWriter);		 		        
		 		    else
			 			stats.printAllStats (printWriter, false);	
		 	        printWriter.close ();       	
		 	        
		 	        // print the summarized MC runs in a file
		 			printWriter = new PrintWriter (new FileOutputStream(fileSummaryMCRuns, true));
		 	        
		 	        if ( fileSummaryMCRuns.exists() && !fileSummaryMCRuns.isDirectory() )
		 	    	    stats.appendSummaryStats (printWriter);		 		        
		 		    else		 		    	
		 		    	stats.printSummaryStats (printWriter, false);	
		 	        printWriter.close ();    
		 	        
		 	        // repeat for the LQ stats
			 		if (ModelParameters.LQ) {
	
			 	        // print all the runs in a file 			
			 			printWriter = new PrintWriter (new FileOutputStream(fileAllMCLQ, true));
			 			
			 			if ( fileAllMCLQ.exists() && !fileAllMCLQ.isDirectory() )
				 			stats.printAllStatsByAveragingLastQuartile(printWriter, true);	      
			 		    else
				 			stats.printAllStatsByAveragingLastQuartile(printWriter, false);	
			 	        printWriter.close ();       	
			 	        
			 	        // print the summarized MC runs in a file
			 			printWriter = new PrintWriter (new FileOutputStream(fileSummaryMCRunsLQ, true));
			 	        
			 	        if ( fileSummaryMCRunsLQ.exists() && !fileSummaryMCRunsLQ.isDirectory() )
			 		    	stats.printSummaryStatsByAveragingLastQuartile(printWriter, true);	
			 		    else		 		    	
			 		    	stats.printSummaryStatsByAveragingLastQuartile(printWriter, false);	
			 	        printWriter.close ();    
			 		}
			 		
		 		} catch (FileNotFoundException e) {
		 			
		 			// TODO Auto-generated catch block
		 			e.printStackTrace();
		 			
		 		    log.log( Level.SEVERE, e.toString(), e );
		 		} 
	    	
		    }
	    }	    					
		
	}
	
}
