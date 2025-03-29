package util;


import java.io.PrintWriter;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.eclipse.collections.impl.list.mutable.FastList;


/** 
 * Generic StatsRun for saving KPIs in a map and retrieve, calc stats and output in the
 * output text files 
 * 
 * This class will store all the results for the MonteCarlo simulation.
 * It will also update the stats and error metrics w.r.t. the historical data.
 * 
 * @author mchica
 * @date 2022/06/16
 * @place Oeiras
 *
 */


public class RunStats {
	
	private int numberRuns;				// number of MC simulations

	private int numberSteps;			// number of steps simulation
	
	private String expName;				// the key name of this experiment (all the MC runs)
	
	
	private FastList <StatsArrayData> listOfKPIs;
	
 	//private MutableSortedMap<String, StatsArrayData> mapOfKPIs;

	
	// ########################################################################	
	// Methods/Functions 	
	// ########################################################################

	//--------------------------- Getters and setters ---------------------------//


	/**
	 * @return the numberRuns
	 */
	public int getNumberRuns() {
		return numberRuns;
	}

	/**
//	 * @param __numberRuns the numberRuns to set
	 */
	public void setNumberRuns(int _numberRuns) {
		this.numberRuns = _numberRuns;
	}

	
	public String getExpName() {
		return expName;
	}

	public void setExpName(String expName) {
		this.expName = expName;
	}

	/**
	 * Generic data set for including new run KPI of specific key in the map
	 * 
//	 * @param _int is the index of the KPI in the list of pairs to be retrieved
//	 * @param _numberOfRun
//	 * @param _array data to set for run _numberOfRun
	 */
	public void setKPIForRun(int _index, int _numberOfRun, float _array[]) {

		(this.listOfKPIs.get(_index)).setDataMatrixForRun(_numberOfRun, _array);
				
	}
	
	/**
	 * Generic data set for including new run KPI of specific key in the map
	 * 
	 * @param _key the key of the KPI in the map to be retrieved
//	 * @param _numberOfRun
//	 * @param _array data to set for run _numberOfRun
	 */
	public void createKPIInMap (int _index, String _key) {
		
		this.listOfKPIs.add(_index, new StatsArrayData (_key, this.numberRuns, this.numberSteps));
		
		//this.mapOfKPIs.put(_key, new StatsArrayData (_key, this.numberRuns, this.numberSteps));
		
	}
	
	/**
	 * @return the numberSteps
	 */
	public int getNumberSteps() {
		return numberSteps;
	}

	/**
//	 * @param numberSteps the numberSteps to set
	 */
	public void setNumberSteps(int _numberSteps) {
		this.numberSteps = _numberSteps;
	}
	
    
	//--------------------------- Constructor ---------------------------//
	/**
	 * constructor of Stats
	 * @param _nRuns
	 * @param _nSteps
	 */
	public RunStats (int _nRuns, int _nSteps){
		
		numberRuns = _nRuns;
		numberSteps = _nSteps;

		this.listOfKPIs = new FastList <StatsArrayData> ();
		
		//this.mapOfKPIs = new MutableSortedMap<String, StatsArrayData> ();
		
	}
		
	/**
	 * This method prints all the steps values (avg and stdev of the MC RUNS) to a stream file 
	 * (or to the console)
	 * @param writer that is opened before calling the function
	 */
	public void printTimeSeriesStats(PrintWriter writer) {
		
		writer.print("step;");

		this.listOfKPIs.forEach((StatsArrayData statsData) -> {

			writer.print(statsData.getKeyString() + "Avg;" 
			+ statsData.getKeyString() + "Std;" 
			+ statsData.getKeyString() + "Min;" 
			+ statsData.getKeyString() + "Max;");

	    });
		
		
		/*writer.print(this.dataFollowersC.getKeyString() + "Avg;" + this.dataFollowersC.getKeyString() + "Std;" + this.dataFollowersC.getKeyString() + "Min;" + this.dataFollowersC.getKeyString() + "Max;");
		writer.print(this.dataFollowersD.getKeyString() + "Avg;" + this.dataFollowersD.getKeyString() + "Std;" + this.dataFollowersD.getKeyString() + "Min;" + this.dataFollowersD.getKeyString() + "Max;");
		writer.print(this.dataInfluencersC.getKeyString() + "Avg;" + this.dataInfluencersC.getKeyString() + "Std;" + this.dataInfluencersC.getKeyString() + "Min;" + this.dataInfluencersC.getKeyString() + "Max;");
		writer.print(this.dataInfluencersD.getKeyString() + "Avg;" + this.dataInfluencersD.getKeyString() + "Std;" + this.dataInfluencersD.getKeyString() + "Min;" + this.dataInfluencersD.getKeyString() + "Max;");
		*/
		
		
		writer.print("\n");
		
		for (int i = 0; i < this.numberSteps; i++) {
			
			String toPrint = i + ";"; 

		    for (int k = 0; k < this.listOfKPIs.size(); k++) 	    	
		    	toPrint += this.listOfKPIs.get(k).returnTimeSeriesStatsAtStep(i);
		    
			toPrint += "\n";
					
			writer.print (toPrint);
		}			
	}
		

	/**
	 * This method prints summarized stats (avg and std of MC runs) to a stream file (or to the console)
	 * @append true if we append the line to an existing file, false if we destroy it first
	 * @param writer that is opened before calling the function
	 */
	public void printSummaryStats (PrintWriter writer, boolean append) {
		
		String toPrint = "keyNameExp;" + this.expName + ";";
		

	    for (int k = 0; k < this.listOfKPIs.size(); k++) 	    	
	    	toPrint += this.listOfKPIs.get(k).returnSummaryStatsAtFinalStep();
		    
			
		if (append) {
			writer.append (toPrint);
			writer.append("\n");
		} else {
			writer.println (toPrint);
		}					
	}
	
	/**
	 * This method prints (by appending to an existing file) the 
	 * summarized stats (avg and std of MC runs) to a stream file (or to the console)
	 * @param writer that is opened before calling the function
	 */
	public void appendSummaryStats (PrintWriter writer) {		
		printSummaryStats (writer, true);				
	}
	
	/**
	 * This method prints all the stats of the MC runs (by appending to an existing file) 
	 * to a stream file (or to the console)
	 * @param writer that is opened before calling the function
	 */
	public void appendAllStats (PrintWriter writer) {		
		printAllStats (writer, true);				
	}

	
	/**
	 * This method prints all the stats of the MC runs to a stream file (or to the console)
	 * @param writer that is opened before calling the function
	 * @append true if we append the line to an existing file, false if we destroy it first
	 */
	public void printAllStats (PrintWriter writer, boolean append) {
		 			
		for (int i = 0; i < this.numberRuns; i++) {
			
			String toPrint = "keyNameExp;" + this.expName + ";MCrun;" + i + ";";
			
		    for (int k = 0; k < this.listOfKPIs.size(); k++) 	    	
		    	toPrint += this.listOfKPIs.get(k).returnAllStats (i);
			    		
			toPrint += "\n";
					
			if (append) 	
				writer.append (toPrint);				
			else 					
				writer.print (toPrint);					
		}	
	}
	
	/**
	 * This method prints summarized stats of the last quartile of the simulation (avg and std of MC runs) 
	 * to a stream file (or to the console)
	 * @append true if we append the line to an existing file, false if we destroy it first
	 * @param writer that is opened before calling the function
	 */
	public void printSummaryStatsByAveragingLastQuartile (PrintWriter writer, boolean append) {
	
		String toPrint = "LQkeyNameExp;" + this.expName + ";";

	    for (int k = 0; k < this.listOfKPIs.size(); k++) 	    	
	    	toPrint += this.listOfKPIs.get(k).returnSummaryStatsByAveragingLQ ();

		if (append) {
			writer.append (toPrint);
			writer.append("\n");
		} else {
			writer.println (toPrint);
		}				
	}
	
	/**
	 * This method calculates distance metric for a given KPI index 
	 * (_KPIIndex is an index for the model output and _KPIIndexForExpectedValue for the expected output
	 * @append true if we append the line to an existing file, false if we destroy it first
	 * @param writer that is opened before calling the function
	 * @param _KPIIndex the index of the KPI to compare with
	 * @param _KPIIndexForExpectedValue the index of the value expected to be compared with
	 */
	public void printSummaryStatsFitExpectedValues (PrintWriter writer, int _KPIIndex, int _KPIIndexForExpectedValue, boolean append) {
	
		String toPrint = "keyNameExp;" + this.expName + ";fitValue;";
				
		//EuclideanDistance dist = new EuclideanDistance ();
		double modelOutput[] = new double[1];
		double expected[] = new double[1];

		modelOutput[0] = this.listOfKPIs.get(_KPIIndex).getAvgDataMatrixLastStep();
		expected[0] = this.listOfKPIs.get(_KPIIndexForExpectedValue).getAvgDataMatrixLastStep();
			
		// Euclidean distance
		//double fitValue = dist.compute(modelOutput, expected);

		// to have a percentage value as the forecast can increase or decrease as well, better use MAPE
		double fitValue = Math.abs(modelOutput[0]  - expected[0]) / expected[0];
		toPrint += fitValue + ";CLs;" + modelOutput[0] + ";ExpectedCLs;" + expected[0];
		
		
		// hard-coded to fit 4497 as CLs and 2354 as FLs
		//double outputFLs = this.listOfKPIs.get(2).getAvgDataMatrixLastStep();
		//double fitValue = ((Math.abs(modelOutput[0] - 4497) / 4497) + (Math.abs(outputFLs - 2354) / 2354))/ 2;

		//toPrint += fitValue + ";CLs;" + modelOutput[0] + ";ExpectedCLs;" + 4497 + ";FLs;" + outputFLs + ";ExpectedFLs;" + 2354;
	   		
		
		if (append) {
			writer.append (toPrint);
			writer.append("\n");
		} else {
			writer.println (toPrint);
		}				
	}
	
	/**
	 * This method prints all the averaging stats in the last quartile of the simulation for all 
	 * the MC runs to a stream file (or to the console)
	 * @param writer that is opened before calling the function
	 * @append true if we append the line to an existing file, false if we destroy it first
	 */
	public void printAllStatsByAveragingLastQuartile (PrintWriter writer, boolean append) {
		
		for ( int i = 0; i < this.numberRuns; i++) {
						
			String toPrint = "LQKeyNameExp;" + this.expName + ";MCrun;" + i + ";";

		    for (int k = 0; k < this.listOfKPIs.size(); k++) 	    	
		    	toPrint += this.listOfKPIs.get(k).returnAllStatsLQ (i);
		    
			toPrint += "\n";
										
			if (append) 
				writer.append (toPrint);		
			else 
				writer.print (toPrint);			
			
		}			
	}
		
	/**
	 * This method is for calculating all the statistical information for 
	 * the runs of the metrics
	 * 	 
	 */
	public void calcAllStats () {

		this.listOfKPIs.forEach((StatsArrayData dataStat) -> {
			dataStat.calcAllStats();
			dataStat.computeLastQuartile();
	    });
	}

}
