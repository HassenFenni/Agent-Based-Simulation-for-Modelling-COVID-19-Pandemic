package agents;

import repast.simphony.random.RandomHelper;

public class Randomizer {
	/**
	 * Age ranges (unit: age). 
	 */
	protected static final int[][] AGE_RANGES = { { 0, 9 }, { 10, 19 },
			{ 20, 29 }, { 30, 39 }, { 40, 49 }, { 50, 59 }, { 60, 69 },
			{ 70, 79 }, { 80, 121 } };

	/**
	 * Age probabilities (unit: probability). 
	 */
	protected static final double[] AGE_PROBABILITIES = { 0.1443, 0.169, 0.1728,
			0.1487, 0.1221, 0.1104, 0.0728, 0.0393, 0.0206 };
	
	/**
	 * Death probabilities (unit: probability). Reference <https://www.worldometers.info/coronavirus/coronavirus-age-sex-demographics/>
	 * Website documentation:
	 * Death Rate = (number of deaths / number of cases) = probability of dying if infected by the virus (%). 
	 * The percentages do not have to add up to 100%, as they do NOT represent share of deaths by age group.
	 * In general, relatively few cases are seen among children.
	 * NOTE: we will convert the percentages to probability 
	 */
	protected static final double[] DEATH_PROBABILITIES = { 0.002, 0.002, 0.002,
			0.002, 0.004, 0.013, 0.036, 0.080, 0.148 };
	
	/**
	 * Private constructor
	 */
	private Randomizer() {
		throw new UnsupportedOperationException("Utility class");
	}
	
	
	public static int getRandomAge() {
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		double cummulativeProbability = 0;
		for (int i = 0; i < AGE_PROBABILITIES.length; i++) {
			cummulativeProbability += AGE_PROBABILITIES[i];
			if (r < cummulativeProbability) {
				return RandomHelper.nextIntFromTo(AGE_RANGES[i][0],
						AGE_RANGES[i][1]);
			}
		}
		return -1;
	}
	
	
	public static double getDeathProbability(int age) {
		for (int i=0;i<DEATH_PROBABILITIES.length; i++) {
			if (age>AGE_RANGES[i][0] && age<AGE_RANGES[i][1]) {
				return DEATH_PROBABILITIES[i];
			}
		}
		return -1.0; 
	}


	public static double getRandomMaskFactor() {
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		return r;
	}
}
