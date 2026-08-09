package com.rpicos.circuitsimcraft.sim;

/** A time-varying source value, in volts, as a function of simulation time in seconds. */
public interface Waveform {

	double valueAt(double timeSeconds);

	static Waveform dc(double volts) {
		return t -> volts;
	}

	static Waveform sine(double amplitude, double frequencyHz, double phaseRad, double offset) {
		return t -> offset + amplitude * Math.sin(2 * Math.PI * frequencyHz * t + phaseRad);
	}

	static Waveform square(double amplitude, double frequencyHz, double phaseRad, double offset) {
		double phaseCycles = phaseRad / (2 * Math.PI);
		return t -> {
			double phase = wrapUnitInterval(t * frequencyHz + phaseCycles);
			return offset + (phase < 0.5 ? amplitude : -amplitude);
		};
	}

	static Waveform triangle(double amplitude, double frequencyHz, double phaseRad, double offset) {
		double phaseCycles = phaseRad / (2 * Math.PI);
		return t -> {
			double phase = wrapUnitInterval(t * frequencyHz + phaseCycles);
			double tri = phase < 0.5
					? (4 * phase - 1)
					: (3 - 4 * phase);
			return offset + amplitude * tri;
		};
	}

	/** Wraps into [0, 1) - Java's {@code %} can return a negative result for a negative operand,
	 *  which a phase-shifted (possibly negative) cycle count can easily produce. */
	private static double wrapUnitInterval(double cycles) {
		double wrapped = cycles % 1.0;
		return wrapped < 0 ? wrapped + 1.0 : wrapped;
	}
}
