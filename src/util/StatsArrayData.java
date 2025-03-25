package util;

import java.util.Locale;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;


/**
 * Class to save arrays of the KPIs of the simulation for max/min/stdev and other stats
 * 
 * @author mchica
 * @date 2022/04/25
 * @place Oeiras, Lisboa
 */

public class StatsArrayData {		
	
	private int numberRuns;				// number of MC simulations
	private int numberSteps;			// number of steps simulation
	
	// fields for storing the data
	private float dataMatrix[][];			// all the KPI information from the simulation(each for run and step)
	private float avgDataMatrix[];			
	private float stdDataMatrix[];			
	private float[] minDataMatrix;
	private float[] maxDataMatrix;

	private float []lastQuartileValue;
	
	private float lastQuartileValueAvg;
	private float lastQuartileValueStd;

	private String keyString;		// the key to show or print together with the value
	
	
	
	/**
	 * @param _data the data to set for run _numberOfRun
	 */
	public void setDataMatrixForRun (int _numberOfRun, float _data[]) {

		for (int i = 0; i < this.numberSteps; i++)
			this.dataMatrix[_numberOfRun][i] = _data[i];
	}

	/**
	 * @return the avg data value for all the MC runs in the given step
	 * @param _numberOfStep the step to collect
	 */
	public float getAvgDataMatrix(int _numberOfStep) {
		return this.avgDataMatrix[_numberOfStep];
		
	}

	/**
	 * @return the avg data value for all the MC runs in the last step
	 */
	public float getAvgDataMatrixLastStep() {
		return this.avgDataMatrix[this.numberSteps - 1];
		
	}

	/**
	 * @return the numberRuns
	 */
	public int getNumberRuns() {
		return numberRuns;
	}

	/**
	 * @param _numberRuns the numberRuns to set
	 */
	public void setNumberRuns(int _numberRuns) {
		this.numberRuns = _numberRuns;
	}
	
	/**
	 * @return the numberSteps
	 */
	public int getNumberSteps() {
		return numberSteps;
	}

	
	/**
	 * @param _numberSteps the numberSteps to set
	 */
	public void setNumberSteps(int _numberSteps) {
		this.numberSteps = _numberSteps;
	}

	/**
	 * 
	 */
	public String getKeyString () {
		return this.keyString;
	}
	
	/**
	 * 
	 */
	public void setKeyString (String _v) {
		this.keyString = _v;
	}
	
	/**
	 * @param 
	 */
	public void computeLastQuartile () {

		for ( int i = 0; i < this.numberRuns; i++) {
						
			// check the number of steps which means the last 25% of them
			int quartileSteps = (int) Math.round(0.25 * this.numberSteps);

			lastQuartileValue[i] = 0;
			lastQuartileValueAvg = 0;
			lastQuartileValueStd = 0;
			
			for ( int j = 1; j <= quartileSteps; j++) {
				lastQuartileValue[i] += this.dataMatrix[i][(this.numberSteps - j)];
				lastQuartileValueAvg += this.avgDataMatrix[(this.numberSteps - j)];
				lastQuartileValueStd += this.stdDataMatrix[(this.numberSteps - j)];
			}

			lastQuartileValue[i] /= quartileSteps;
			lastQuartileValueAvg /= quartileSteps;
			lastQuartileValueStd /= quartileSteps;
			
		}
		
	}
	
	//--------------------------- Constructor ---------------------------//
	/**
	 * constructor of StatsArrayData
	 * @param _nRuns
	 */
	public StatsArrayData (String _key, int _nRuns, int _nSteps){

		this.keyString = _key;
		this.numberRuns = _nRuns;
		this.numberSteps = _nSteps;
		
		this.dataMatrix = new float[_nRuns][_nSteps];
		this.avgDataMatrix = new float[_nSteps];	
		this.stdDataMatrix = new float[_nSteps];
		this.minDataMatrix = new float[_nSteps];	
		this.maxDataMatrix = new float[_nSteps];
		
		this.lastQuartileValue = new float[_nRuns];
	}
			
	/**
	 * 
	 */
	public String returnTimeSeriesStatsAtStep(int _step) {
		
		return String.format(Locale.US, "%.4f", this.avgDataMatrix[_step]) + ";" + String.format(Locale.US, "%.4f", this.stdDataMatrix[_step]) + ";" +
				String.format(Locale.US,"%.4f", this.minDataMatrix[_step]) + ";" + String.format(Locale.US, "%.4f", this.maxDataMatrix[_step]) + ";";

	}
	
	/**
	 * 
	 */
	public String returnSummaryStatsAtFinalStep() {
		
		return this.keyString + ";" + String.format(Locale.US,"%.4f", this.avgDataMatrix[(this.numberSteps - 1)]) + ";" + 
				String.format(Locale.US,"%.4f", this.stdDataMatrix[(this.numberSteps - 1)]) + ";";

	}
	
	/**
	 * 
	 */
	public String returnAllStatsLQ (int _run) {
		
		return this.keyString + "LQ;" + String.format(Locale.US,"%.4f", this.lastQuartileValue[_run]) + ";";

	}
	
	/**
	 * 
	 */
	public String returnAllStats (int _run) {
		
		return this.keyString + ";" + String.format(Locale.US,"%.4f", this.dataMatrix[_run][(this.numberSteps - 1)]) + ";";

	}
	/**
	 * 
	 */
	public String returnSummaryStatsByAveragingLQ () {
				
		return this.keyString + "LQ;"+ String.format(Locale.US,"%.4f", lastQuartileValueAvg) + ";" + String.format(Locale.US,"%.4f", lastQuartileValueStd) + ";";

	}
	
	
	/** 
	 * function to calculate the basic stats of the data and store it
	 */
	public void calcAllStats () {
		
		for( int j = 0; j < this.numberSteps; j++) {
			
			// Get a DescriptiveStatistics instance
			DescriptiveStatistics stats= new DescriptiveStatistics();
			
			// Add the data from the array
			for( int i = 0; i < this.numberRuns; i++) 				        
		        stats.addValue(this.dataMatrix[i][j]);	        
		       
			// calc mean and average for all of them
			
			this.avgDataMatrix[j] = (float)stats.getMean();	
			this.stdDataMatrix[j] = (float)stats.getStandardDeviation();
			this.minDataMatrix[j] = (float)stats.getMin();	
			this.maxDataMatrix[j] = (float)stats.getMax();	
		}
	}

}

