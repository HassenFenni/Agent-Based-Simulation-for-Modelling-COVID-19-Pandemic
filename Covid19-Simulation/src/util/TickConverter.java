package util;

import repast.simphony.util.collections.Pair;

public class TickConverter {

	/**
	 * Ticks per minute (unit: ticks)
	 */
	public static final double TICKS_PER_MINUTE = 1.0 / 60;

	/**
	 * Ticks per week (unit: ticks)
	 */
	public static final int TICKS_PER_WEEK = 168;

	/**
	 * Ticks per day (unit: ticks)
	 */
	public static final int TICKS_PER_DAY = 24;

	/**
	 * Private constructor
	 */
	private TickConverter() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Day and time to ticks
	 * 
	 * @param day  Day
	 * @param time Time
	 */
	public static double dayTimeToTicks(int day, double time) {
		return (day - 1) * TICKS_PER_DAY + time;
	}

	/**
	 * Tick to day and time
	 * 
	 * @param tick Tick
	 */
	public static Pair<Double, Double> tickToDayTime(double tick) {
		double day = Math.floor(((tick / TICKS_PER_DAY) % 7) + 1);
		double time = (((tick / TICKS_PER_DAY) % 7) + 1 - day) * TICKS_PER_DAY;
		return new Pair<>(day, time);
	}

	/**
	 * Minutes to ticks
	 * 
	 * @param minutes Minutes
	 */
	public static double minutesToTicks(double minutes) {
		return minutes * TICKS_PER_MINUTE;
	}

	/**
	 * Days to ticks
	 * 
	 * @param days Days
	 */
	public static double daysToTicks(double days) {
		return days * TICKS_PER_DAY;
	}

	/**
	 * Ticks to days
	 * 
	 * @param ticks Ticks
	 */
	public static double ticksToDays(double ticks) {
		return ticks / TICKS_PER_DAY;
	}

}