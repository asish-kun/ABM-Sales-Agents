package util;

import it.unimi.dsi.util.XoRoShiRo128PlusRandom;

/**
 * Handy functions for EGT and agent-based models
 * 
 * @author mchica
 * @location Oeiras, Lisboa
 * 
 *
 */
public class Functions {

	// Include zero when generating random value from 0 to 1
	public static final boolean INCLUDE_ZERO = true;
	
	// Include one when generating random value from 0 to 1	
	public static final boolean INCLUDE_ONE = true;
	
	/**
	 * Defines the Heaviside function. If argument _Value is >= 0, then the function returns 1.
	 * If the argument is negative, the value is 0
	 * 
	 * @param _value the real value of the heaviside function
	 * @return 1 if the argument is >= 0. 0 Otherwise
	 */
	public static int heaviside (double _value) {
		return (_value < 0) ? 0 : 1;
	}
	
	/**
	 * Compares two double values. They will be considered as equals if the difference
	 * between their values is less than delta.
	 * @param a The first double value.
	 * @param b The second double value.
	 * @param delta The threshold where the values are considered as equal.
	 * @return wherever the difference between both values is lesser than delta.
	 */
	public static boolean equals(double a, double b, double delta) {
		double diff=Math.abs(a-b);
		return diff<=delta;
	}
	
	/**
	 * Applies a normal distribution centered on "value" over [0,interval].
	 * @param mean the value used as mean for the distribution.
	 * @param gaussian the value used for a standard deviation of one.
	 * @param interval the top value used for the bounded distribution.
	 * @return the given value normally distributed.
	 */
	public static double applyNormalDistribution(
			double mean, 
			double gaussian, 
			double interval) 
	{
		return Functions.scaleGaussianValue(mean,gaussian,9.0,0.0,interval);	
	}
	
	/**
	 * Applies a normal distribution centered on "value" with the given standard
	 * deviation, fitting all values into the interval [bottom, top].
	 * @param mean the value used as mean for the distribution.
	 * @param gaussian the value used for a standard deviation of one.
	 * @param stDeviation the value used for standard deviation.
	 * @param bottom the bottom value for the interval.
	 * @param top the top value for the interval.
	 * @return
	 */
	public static double scaleGaussianValue(
			double mean, 
			double gaussian,	//TODO [KT] We can use gaussian generator inside to avoid errors 
			double stDeviation,
			double bottom,
			double top) 
	{
		if(gaussian==0.0) {
			return mean;
		} else {			
			double result=0.0;			
			if (gaussian>0) {
				if(mean+stDeviation>top) {
					stDeviation=top-mean;
				}				
			} else {
				if(mean-stDeviation<bottom) {
					stDeviation=mean-bottom;
				}
			}
			result=(gaussian*stDeviation)+mean;
			return result;
		}		
	}
	
	/**
	 * Returns numbers between -1 and 1, normally distributed with 0 as
	 * its mean. [KT] Keep in mind that dividing by 3, the stdev is 0.333.
	 * @param randomizer.
	 * @return the next value from the normal distribution.
	 */
	public static double nextGaussian(XoRoShiRo128PlusRandom randomizer) {
		double gaussian;
		
		do {
			gaussian=randomizer.nextGaussian()/3.0;
		} while(gaussian>1.0 || gaussian < -1.0);
		
		return gaussian;
	}
	
	/**
	 * Returns numbers between -boundary and boundary, normally distributed with 0 as
	 * its mean. TODO [KT] I guess that stdev is set to 1.0 ??? Then, when we set the
	 * boundary to 3 (3 times then stdev) we cover 99.7% of the values.
	 * [KT] Keep in mind that dividing by boundary, the stdev is boundary.
	 * @param randomizer
	 * @param boundary - bounds the normal distribution from both sides.
	 * @return - the next value from the normal distribution.
	 */
	public static double nextGaussian(XoRoShiRo128PlusRandom randomizer, double boundary) {
		double gaussian;
		
		do {
			gaussian=randomizer.nextGaussian();
		} while(gaussian>boundary || gaussian < -boundary);
		
//		return gaussian/boundary;
		return gaussian;
	}
	
	/**
	 * Random Weighted Selection (like tournament selection in GA)
	 * @param ProbVec - the vector of probability weights.
	 * @param r - the random value.
	 * @return - the value selected
	 */	
	public static int randomWeightedSelection(double ProbVec[], double r) {
		// Variables
		final double NOTCONSIDERED = 0.000;
		double totalWeight=0;
		int VecLength = ProbVec.length;
		double randValue;
		
		// Calculate total weight
		for(int i=0; i<VecLength; i++) {
			totalWeight += ProbVec[i];			
		}
		
		randValue = r * totalWeight;
		
		// Select the output
		for(int i=0; i<VecLength; i++)
		{
			if(randValue < ProbVec[i])
			{
				if(ProbVec[i]== NOTCONSIDERED) {
					System.err.println("Node connection prob. is: " 
									+ ProbVec[i] + " randValue is: " + randValue);
				}
				return i;
			}
			randValue=randValue-ProbVec[i];
		}
		// A robust code, return the last value
		return (VecLength-1);
	}
	
	/**
	 * Random Weighted Selection (like tournament selection in GA) with
	 * restricted entries provided by a boolean array. Those entries
	 * marked by the boolean array are not used in calculation. 
	 * @param ProbVec - the vector of probability weights.
	 * @param restricted - the vector of restricted entries.
	 * @param r - the random value.
	 * @return - the value selected
	 */	
	public static int randomWeightedSelectionRestricted(
		double ProbVec[], boolean restricted[], double r
	) {
		// Variables
		final double NOTCONSIDERED = 0.000;
		double totalWeight=0;
		int VecLength = ProbVec.length;
		double randValue;
		
		// Calculate total weight
		for(int i=0; i<VecLength; i++) {
			if(!restricted[i]) {
				totalWeight += ProbVec[i];			
			}	
		}
		
		randValue = r * totalWeight;
		// Select the output
		for(int i=0; i<VecLength; i++) {
			if(!restricted[i]) {
				if(randValue < ProbVec[i])
				{
					if(ProbVec[i]== NOTCONSIDERED) {
						System.err.println("Node connection prob. is: " 
										+ ProbVec[i] + " randValue is: " + randValue);
					}
					return i;
				}
				randValue=randValue-ProbVec[i];				
			}
		}
		// A robust code, return the last value
		return (VecLength-1);
	}
		
	/**
	 * Normalization of a value in a given range [minIn, maxIn] to a new range [minOut, maxOut]
	 * 
	 * @param valueIn
	 * @param minIn
	 * @param maxIn
	 * @param minOut
	 * @param maxOut
	 * @return the converted value from previous range to new range
	 */
	
	public static double normalizeMinMax(
		double valueIn, double minIn, double maxIn, double minOut, double maxOut) {
		double valueOut = 0;
		valueOut = minOut + ((valueIn - minIn) * (maxOut - minOut)) / (maxIn - minIn);
		return valueOut;
	}
	
	/**
	 * Generates the array of the indices based on the random weighted order.
	 * It uses the values of the array to weight the probabilities of each
	 * index to be chosen.
	 * @param array - the array of values to be ordered.
	 * @param random - the random number generator.
	 * @return - the array of the indices.
	 */
	public static int[] getIndicesRandomWeightedOrder(
		double[] array, XoRoShiRo128PlusRandom random
	) {
		int size = array.length;
		boolean[] used = new boolean[size];
		int[] results = new int[size];
		int index;

		for (int i = 0; i < size; i++) {
			index = Functions.randomWeightedSelectionRestricted(array, used, random.nextDouble());
			results[i] = index;
			used[index] = true;
		}

		return results;
	}
	
	
	/**
	 * 
	 * Compute the Fermi function for two fitness values
	 * 
	 * @param neighFitness - fitness of the neighbor agent (j)
	 * @param focalAgentFitness - fitness of the focal agent (i)
	 * @param beta - the intensity of selection parameter of the function
	 * @return - the computed Fermi value
	 */
	public static double fermiFunction (double neighFitness, double focalAgentFitness, double beta) {
		
		return 1.0 / (1 + Math.exp(-1 * beta * (neighFitness - focalAgentFitness)));		
		
	}
	
						

		
}
	

