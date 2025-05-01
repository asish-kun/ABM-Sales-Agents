package view;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import controller.Controller;
import model.Model;
import model.ModelParameters;
import util.RunStats;
import util.SensitivityAnalysis;

//----------------------------- MAIN FUNCTION -----------------------------//

/**
 * Main function for ABM4Sales
 * 
 * This model represents the decisions of salespeople when choosing 
 *
 * 
 * @author mchica
 * @date 2022/09/22
 * @place Granada
 */

public class ConsoleSimulation {		
	

	// LOGGING
	private static final Logger log = Logger.getLogger( Model.class.getName() );
	
		
	/**
	 * Create an options class to store all the arguments of the command-line call of the program
	 * 
	 * @param options the class containing the options, when returned. It has to be created before calling
	 */
	private static void createArguments (Options options) {
				
					
		options.addOption("paramsFile", true, "Pathfile with the parameters file");
		options.getOption("paramsFile").setRequired(true);

		options.addOption("maxSteps", true, "Max number of steps of the simulation");
		options.addOption("MCRuns", true, "Number of MC simulations");
		options.addOption("seed", true, "Seed for running the MC simulations");
		options.addOption("nrAgents", true, "Number of agents");		
		options.addOption("avgRiskAversion", true, "average risk aversion of the salespersons");	
		options.addOption("stdevRiskAversion", true, "stdev risk aversion of the salespersons");	
		options.addOption("thresholdForConversion", true, "threshold to get a lead when its prob. to be converted overcome this value");	
		options.addOption("decayRateLeads", true, "decay value to increase the prob. to fall-off of a lead in a portfolio if not working on it");	
		options.addOption("distributionForLeads", true, "Way to generate leads' magniture. 0 for Uniform and 1 for Normal");	
		options.addOption("quota", true, "Quota of the firm to incentivate salespeople");
		options.addOption("rateForBonus", true, "rate to apply to S-Q for the bonuses when quota is achieved");
		options.addOption("socialComparison", true, "Social comparison to move up and down the comparison w.r.t. the average performance of the pop");
		options.addOption("portfolioSize", true, "Size of the portfolio");
				
		//options.addOption("expectedCLs", true, "Expected CLs");
		options.addOption("fileForLeads", true, "File with the information about the leads");
				
		options.addOption("SNFile", true, "File with the SN to run");

		// NEW: user-specified accuracy
		options.addOption("accuracy", true, "Accuracy factor for actual vs predicted prob");

		// NEW: user-specified strategies, Accuracies, Chosen Leads and Portfolio Size for each sales agent
		options.addOption("agentStrategies", true,
				"Comma or bracketed list of strategy codes for each agent, e.g. [1,2,3]");
		options.addOption("agentAccuracies",     true,
				"Comma or bracketed float list – one accuracy per agent");
		options.addOption("agentLeadChoices",    true,
				"Comma / bracketed int list – #leads each agent works weekly");
		options.addOption("agentPortfolioSizes", true,
				"Comma / bracketed int list – portfolio size per agent");
		options.addOption(
				"fallOffProbability", true,
				"Constant fall-off probability for all leads (e.g. 0.1 for 10%)");


		options.addOption("SA_avgLeadsMagnitude", false, "SA on lead magnitude mean for Normal generation");
		options.addOption("SA_stdevLeadsMagnitude", false, "SA on lead magnitude stdev. for Normal generation");
		options.addOption("SA_avgRiskAversion", false, "SA on risk aversion for the salespeople");
		options.addOption("SA_avgRiskAversion", false, "SA on risk aversion for the salespeople");
		options.addOption("SA_thresholdForConversion_decayRateLeads", false, "SA on threshold for conversion and decay for falling-off");
		options.addOption("SA_avgRiskAversion_stdevRiskAversion", false, "SA on risk aversion (both avg and stdev)");
		options.addOption("SA_socialComparison", false, "SA on social influence for comparing with the rest");
		options.addOption("SA_quota", false, "SA on quota");
		//options.addOption("SA_bonus", false, "SA on bonus");

		options.addOption("outputFile", true, "File to store all the information about the simulation");
		options.getOption("outputFile").setRequired(true);

		options.addOption("DEBUG", false, "TO DEBUG THE MODEL BY SHOWING OUTPUT DEBUGGING");
		
		// specific parameters of the model
		// to show help
		options.addOption("help", false, "Show help information");	
		
	}
	
	/**
	 * MAIN CONSOLE-BASED FUNCTION TO RUN A SIMPLE RUN OR A SENSITIVITY ANALYSIS OF THE MODEL PARAMETERS
	 
	 * @param args
	 */
	public static void main (String[] args) {
    	
		String paramsFile = "";
		String outputFile = "";
		
		int indexForCLs = -1, indexForExpectedCLs = -1;
			
		ModelParameters params = null;
		
		// parsing the arguments
		Options options = new Options();
		
		createArguments (options);		

		// create the parser
	    CommandLineParser parser = new DefaultParser();
	    
	    try {
	    	
	        // parse the command line arguments for the given options
	        CommandLine line = parser.parse( options, args );

			// get parameters
			params = new ModelParameters();	
			
	        // retrieve the arguments
	        		    
		    if( line.hasOption( "paramsFile" ) )		    
		    	paramsFile = line.getOptionValue("paramsFile");
		    else 		    	
		    	System.err.println( "A parameters file is needed");

		    if( line.hasOption( "outputFile" ) ) 			    
		    	outputFile = line.getOptionValue("outputFile");

	    	
		    // read all the parameters from file
			params.readParameters(paramsFile);

			// add the output file to the configuration of the model
			params.addParameter("outputFile", outputFile);
						
			// once parameters from file are loaded, we modify those read by arguments of command line		
			
		    // load the parameters file and later, override them if there are console arguments for these parameters

			 // MC
		    if( line.hasOption( "MCRuns" ) ) 			    
		    	params.setParameterValue("MCRuns", Integer.parseInt(line.getOptionValue("MCRuns")));

			 // number of agents
		    if( line.hasOption( "nrAgents" ) ) 			    
		    	params.setParameterValue("nrAgents", Integer.parseInt(line.getOptionValue("nrAgents")));

		    // seed
		    if( line.hasOption( "seed" ) ) 			    
		    	params.setParameterValue("seed", Long.parseLong(line.getOptionValue("seed")));

		    // maxSteps
		    if( line.hasOption( "maxSteps" ) ) 			    
		    	params.setParameterValue("maxSteps", Integer.parseInt(line.getOptionValue("maxSteps")));
		    
			
		    // save file for the SN
		    if( line.hasOption( "SNFile" ) ) 
		    	// read again the network apart from setting the name of the file (inside next function)
		    	try {

					params.readGraphFromFile(line.getOptionValue("SNFile"));
					
		    	} catch (IOException e) {

					System.err.println("ConsoleSimulation: Error with SN file when loading parameters for the simulation " + line.getOptionValue("SNFile") + "\n"
							+ e.getMessage());
					e.printStackTrace(new PrintWriter(System.err));
				}


			// NEW: if we have "accuracy" param
			if(line.hasOption("accuracy")) {
				params.setParameterValue("accuracy", Float.parseFloat(line.getOptionValue("accuracy")));
			}

			// NEW: if we have "agentStrategies"
			if(line.hasOption("agentStrategies")) {
				// e.g. -agentStrategies "[1,2,1,3]"
				// parse the string into an int array
				String rawStr = line.getOptionValue("agentStrategies")
						.replace("[","").replace("]","");
				// e.g. "1,2,1,3"
				params.setParameterValue("agentStrategies", rawStr);
			}

			// NEW: if we have "agentAccuracies"
			if (line.hasOption("agentAccuracies"))
				params.setParameterValue("agentAccuracies",
						line.getOptionValue("agentAccuracies").replace("[","").replace("]",""));

			// NEW: if we have "agentLeadChoices"
			if (line.hasOption("agentLeadChoices"))
				params.setParameterValue("agentLeadChoices",
						line.getOptionValue("agentLeadChoices").replace("[","").replace("]",""));

			// NEW: if we have "agentPortfolioSizes"
			if (line.hasOption("agentPortfolioSizes"))
				params.setParameterValue("agentPortfolioSizes",
						line.getOptionValue("agentPortfolioSizes").replace("[","").replace("]",""));

			// after reading fallOffProbability parameter:
			if (line.hasOption("fallOffProbability")) {
				params.setParameterValue(
						"fallOffProbability",
						Float.parseFloat(line.getOptionValue("fallOffProbability"))
				);
			}

			// specific parameters of the model

		    if( line.hasOption( "distributionForLeads" ) ) 		    	
		    	params.setParameterValue("distributionForLeads", Integer.parseInt(line.getOptionValue("distributionForLeads")));

		    if( line.hasOption( "quota" ) ) 		    	
		    	params.setParameterValue("quota", Float.parseFloat(line.getOptionValue("quota")));

		    if( line.hasOption( "rateForBonus" ) ) 		    	
		    	params.setParameterValue("rateForBonus", Float.parseFloat(line.getOptionValue("rateForBonus")));
		     
		    if( line.hasOption( "portfolioSize" ) ) 		    	
		    	params.setParameterValue("portfolioSize", Float.parseFloat(line.getOptionValue("portfolioSize")));

		    if( line.hasOption( "socialComparison" ) ) 		    	
		    	params.setParameterValue("socialComparison", Float.parseFloat(line.getOptionValue("socialComparison")));

		    if( line.hasOption( "avgRiskAversion" ) ) 		    	
		    	params.setParameterValue("avgRiskAversion", Float.parseFloat(line.getOptionValue("avgRiskAversion")));		    

		    if( line.hasOption( "stdevRiskAversion" ) ) 		    	
		    	params.setParameterValue("stdevRiskAversion", Float.parseFloat(line.getOptionValue("stdevRiskAversion")));		    

		    if( line.hasOption( "thresholdForConversion" ) ) 		    	
		    	params.setParameterValue("thresholdForConversion", Float.parseFloat(line.getOptionValue("thresholdForConversion")));	
		    
		    if( line.hasOption( "decayRateLeads" ) ) 		    	
		    	params.setParameterValue("decayRateLeads", Float.parseFloat(line.getOptionValue("decayRateLeads")));		    
		    

		    if( line.hasOption( "fileForLeads" ) ) {	    
		    	params.setParameterValue("fileForLeads", line.getOptionValue("fileForLeads"));
		    	
		    	// we read the file and store the leads if the file is provided by arguments
		    	params.feedLeadsData();
		    }
		    
		    /*if( line.hasOption( "expectedCLs" ) ) 		    	
		    	params.setParameterValue("expectedCLs", Float.parseFloat(line.getOptionValue("expectedCLs")));		    
	*/
		    
		    if( line.hasOption( "SA_avgRiskAversion" ) ) {
		    	
		    	params.setParameterValue("SA_type", ModelParameters.SA_avgRiskAversion);
		    	
		    } else if( line.hasOption( "SA_thresholdForConversion_decayRateLeads" ) ) {
		    	
		    	params.setParameterValue("SA_type", ModelParameters.SA_thresholdForConversion_decayRateLeads);
		    	
		    } else if( line.hasOption( "SA_avgRiskAversion_stdevRiskAversion" ) ) {
		    	
		    	params.setParameterValue("SA_type", ModelParameters.SA_avgRiskAversion_stdevRiskAversion);
		    	
		    }  else if( line.hasOption( "SA_quota" ) ) {
		    	
		    	params.setParameterValue("SA_type", ModelParameters.SA_quota);
		    	
		    } /* else if( line.hasOption( "SA_bonus" ) ) {
		    	
		    	params.setParameterValue("SA_type", ModelParameters.SA_bonus);
		    	
		    }*/ else if( line.hasOption( "SA_socialComparison" ) ) {
		    	
		    	params.setParameterValue("SA_type", ModelParameters.SA_socialComparison);
		    	
		    } else if( line.hasOption( "SA_avgLeadsMagnitude" ) ) {
		    	
		    	params.setParameterValue("SA_type", ModelParameters.SA_avgLeadsMagnitude);
		    	
		    } else if( line.hasOption( "SA_stdevLeadsMagnitude" ) ) {
		    	
		    	params.setParameterValue("SA_type", ModelParameters.SA_stdevLeadsMagnitude);
		    
		    }else {
		    	
		    	params.setParameterValue("SA_type", ModelParameters.NO_SA);
		    }

		    // DEBUG
		    if( line.hasOption("DEBUG") ) {
		    	ModelParameters.DEBUG = true;
		    }
		    
		    
		    // help information
		    if( line.hasOption("help") ) {
			    	
			    // automatically generate the help statement
			    HelpFormatter formatter = new HelpFormatter();
			    formatter.printHelp( ModelParameters.modelName + " " + ModelParameters.modelDate + ". Manuel Chica", options);			   	
			}			  		    		   		   		    			
	    }
	    
	    catch (ParseException exp ) {
	    	
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
			
	    } catch (IllegalArgumentException e ) {
	    	
	        // oops, something went wrong
	        System.err.println( "IllegalArguments for command line.  Reason: " + e.getMessage() );
			log.log(Level.SEVERE, "IllegalArguments for command line.  Reason: " + e.toString(), e);
			
	    }
	    	
        System.out.println("\n****** STARTING " + ModelParameters.modelName + " " +  ModelParameters.modelDate + " ******\n");

        Date date = new Date();
    	System.out.println("Experiment output file: **" + params.getStringParameter("outputFile") + "**" );
        System.out.println("Launched on " + date.toString()+ "\n" + "\nParameters of the model:\n-----------------------");
    	
        File fileAllMC = new File ("./logs/" + "AllMCruns_" + params.getStringParameter("outputFile") + ".txt");
        File fileSummaryMC = new File ("./logs/" + "SummaryMCruns_" +  params.getStringParameter("outputFile") + ".txt");
        File fileAllMCLQ = new File ("./logs/" + "AllMCrunsLQ_" + params.getStringParameter("outputFile") + ".txt");
        File fileSummaryMCLQ = new File ("./logs/" + "SummaryMCrunsLQ_" +  params.getStringParameter("outputFile") + ".txt");
        File fileTimeSeriesMC = new File ("./logs/" + "TimeSeriesMCruns_" +   params.getStringParameter("outputFile") + ".txt");
        File fileSummaryFitExpectedData = new File ("./logs/" + "SummaryFitExpectedData_" +  params.getStringParameter("outputFile") + ".txt");
       
        // the SA check    	    			
        int SA = params.getIntParameter("SA_type");
        
	    if (SA == ModelParameters.NO_SA) {
	    	
	    	// no SA, simple run
	    	
	    	RunStats stats;
	    	
	    	// print parameters for double-checking
		    PrintWriter out = new PrintWriter(System.out, true);
	        params.printParameters(out);
	        
	        log.log(Level.FINE, "\n*** Parameters' values of the " + ModelParameters.modelName + " model:\n" + params.export());
	        
	        
	        // START PREPARING CONTROLLER FOR RUNNING
			long time1 = System.currentTimeMillis ();
		
			Controller controller;
			
			controller = new Controller (params, paramsFile);
			
	        // END PREPARING CONTROLLER FOR RUNNING
	 		
			
			// BEGIN RUNNING MODEL WITH ALL THE MC SIMULATION
			
	 		stats = controller.runModel();	
	 		
	 		// END RUNNING MODEL WITH ALL THE MC SIMULATION
	 		
	 		stats.setExpName (params.getStringParameter("outputFile"));
	 		
	 		long  time2  = System.currentTimeMillis( );
	 		System.out.println("\n****** " + (double)(time2 - time1)/1000 + "s spent during the simulation");
	 		
	 		stats.calcAllStats();
	         
	 		// print the stats in the screen 		
	 		stats.printSummaryStats(out, false);
	 			 		
	 		// if having file for the leads, then we create file with information for matching model's output with real data	 		
	 		if (params.isParameterSet("fileForLeads")) {	 			
	 			indexForCLs = 0;
	 			indexForExpectedCLs = 6;
	 			
	 			stats.printSummaryStatsFitExpectedValues (out, indexForCLs, indexForExpectedCLs, false); 
	 		}
	 		
	 		if (ModelParameters.LQ)
	 			stats.printSummaryStatsByAveragingLastQuartile(out, false);  // also the last quartile info

	 		System.out.println();
	 		
	 		// print the stats into a file
	        System.out.println("\n****** Stats also saved into a file ******\n");
	         
	        PrintWriter printWriter;
	         
	 		try {
	 			
	 			
	 			// print all the runs info into a file
	 			printWriter = new PrintWriter (fileAllMC);
	 			stats.printAllStats (printWriter, false);
	 	        printWriter.close (); 
	 	        
		 		if (params.isParameterSet("fileForLeads")) {
		 	        // save the distance between CLs and the expected ones
		 			printWriter = new PrintWriter (fileSummaryFitExpectedData);
		 					 			
		 			stats.printSummaryStatsFitExpectedValues (printWriter, indexForCLs, indexForExpectedCLs, false); 
		 	        printWriter.close ();  
		 		}
			 	 
			 	if (ModelParameters.LQ) {
		 	        // print all the runs info (last quartiles of the sims) into a file
		 			printWriter = new PrintWriter (fileAllMCLQ);
		 			stats.printAllStatsByAveragingLastQuartile (printWriter, false);
		 	        printWriter.close ();  
		 		}
			 	
	 	        // print the summarized MC runs into a file
	 	        printWriter = new PrintWriter (fileSummaryMC);
	 			stats.printSummaryStats (printWriter, false);
	 	        printWriter.close ();    
	 	        

				 if (ModelParameters.LQ) {
		 	        // print the summarized MC runs (last quartiles of the sims) into a file
		 	        printWriter = new PrintWriter (fileSummaryMCLQ);
		 			stats.printSummaryStatsByAveragingLastQuartile(printWriter, false);
		 	        printWriter.close ();    
			 	}

	 	        // print the time series into a file
	 	        printWriter = new PrintWriter (fileTimeSeriesMC);
	 			stats.printTimeSeriesStats (printWriter);
	 	        printWriter.close ();    
	 	        
	 	        
	 	        
	 		} catch (FileNotFoundException e) {
	 			
	 			// TODO Auto-generated catch block
	 			e.printStackTrace();
	 			
	 		    log.log( Level.SEVERE, e.toString(), e );
	 		} 
	    	
	    } else {
	    
	    	if ( SA == ModelParameters.SA_avgRiskAversion) {
	    	 
	    		// SA ON 1 PARAMETER
	    		 
	    		SensitivityAnalysis.setParam1Info ("avgRiskAversion",(float) 0, (float)1, (float)0.05);

	    		SensitivityAnalysis.runSAOnOneParameter (params, paramsFile, fileAllMC, fileSummaryMC, fileAllMCLQ, fileSummaryMCLQ);
	    		
	    	} else if ( SA == ModelParameters.SA_socialComparison) {
		    	 
		    		// SA ON 1 PARAMETER
		    		 
		    		SensitivityAnalysis.setParam1Info ("socialComparison",(float) -0.5, (float)0.5, (float)0.025);

		    		SensitivityAnalysis.runSAOnOneParameter (params, paramsFile, fileAllMC, fileSummaryMC, fileAllMCLQ, fileSummaryMCLQ);
		    		
		    } else if ( SA == ModelParameters.SA_quota) {
		    	 
		    		// SA ON 1 PARAMETER
		    		 
		    		// for quota Q makes sense to link quota to the portfolio size and magniture (more possible leads and higher magnitude, greater quotas)
		    		// we assume a very low quota is 20% of portfolio with a max magnitude of 1. a very high is 100% of the size 
		    		float maxQuotaSA = params.getIntParameter("portfolioSize");
		    		float minQuotaSA = 0; //(float)(0.2 * params.getIntParameter("portfolioSize"));
		    		
		    		// we calculate the step to have 20 values for Q
		    		float step = (maxQuotaSA - minQuotaSA) / (float)20;
		    		
		    		SensitivityAnalysis.setParam1Info ("quota",(float) minQuotaSA, (float)maxQuotaSA, (float)step);

		    		SensitivityAnalysis.runSAOnOneParameter (params, paramsFile, fileAllMC, fileSummaryMC, fileAllMCLQ, fileSummaryMCLQ);
		    		
		    } /* else if ( SA == ModelParameters.SA_bonus) {
		    	 
		    		// SA ON 1 PARAMETER
		    		 
		    		SensitivityAnalysis.setParam1Info ("bonus",(float) 0, (float)10, (float)1);

		    		SensitivityAnalysis.runSAOnOneParameter (params, paramsFile, fileAllMC, fileSummaryMC, fileAllMCLQ, fileSummaryMCLQ);
		    		
		    }*/  else if ( SA == ModelParameters.SA_avgLeadsMagnitude) {
		    	 
		    		// SA ON 1 PARAMETER
		    		 
		    		SensitivityAnalysis.setParam1Info ("avgLeadsMagnitude",(float) 0, (float)0.95, (float)0.01);

		    		SensitivityAnalysis.runSAOnOneParameter (params, paramsFile, fileAllMC, fileSummaryMC, fileAllMCLQ, fileSummaryMCLQ);
		    		
	 
		    } else if ( SA == ModelParameters.SA_stdevLeadsMagnitude) {
		    	 
		    		// SA ON 1 PARAMETER
		    		 
		    		SensitivityAnalysis.setParam1Info ("stdevLeadsMagnitude",(float) 0, (float)1.2, (float)0.01);

		    		SensitivityAnalysis.runSAOnOneParameter (params, paramsFile, fileAllMC, fileSummaryMC, fileAllMCLQ, fileSummaryMCLQ);
		    		
		    } else if ( SA == ModelParameters.SA_thresholdForConversion_decayRateLeads) {

	    		// SA ON 2 PARAMETERS


	    		SensitivityAnalysis.setParam1Info ("thresholdForConversion", (float)0.5, (float)0.95, (float)0.05);
	    		SensitivityAnalysis.setParam2Info ("decayRateLeads", (float)0, (float)0.3, (float)0.01);

	    		SensitivityAnalysis.runSAOnTwoParameters(params, paramsFile, fileAllMC, fileSummaryMC, fileAllMCLQ, fileSummaryMCLQ);
	    		
	    	} else if ( SA == ModelParameters.SA_avgRiskAversion_stdevRiskAversion) {

	    		// SA ON 2 PARAMETERS


	    		SensitivityAnalysis.setParam1Info ("avgRiskAversion", 0, 1, 0.05);
	    		SensitivityAnalysis.setParam2Info ("stdevRiskAversion", 0, 0.5, 0.005);

	    		SensitivityAnalysis.runSAOnTwoParameters(params, paramsFile, fileAllMC, fileSummaryMC, fileAllMCLQ, fileSummaryMCLQ);
	    		
	    	} 
	    }
	}
}

