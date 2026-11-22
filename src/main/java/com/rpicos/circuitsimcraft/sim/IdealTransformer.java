package com.rpicos.circuitsimcraft.sim;

/**
 * An ideal single-phase transformer with turns ratio {@code N:M} (primary:secondary):
 * <pre>V_s = (M/N) * V_p,    I_s = (N/M) * I_p</pre>
 * lossless and with no magnetizing inductance, so each port presents whatever is connected
 * to the other one, scaled by the square of the ratio - a load {@code Z} on the secondary
 * looks like {@code (N/M)^2 * Z} from the primary, and the source driving the primary looks
 * like a source of {@code M/N} times its voltage behind {@code (M/N)^2} times its impedance
 * from the secondary. Being ideal, it couples DC as well as AC (a real transformer does not).
 *
 * <p>Modeled the standard MNA way, like {@link VoltageSource} and {@link IdealOpAmp}: one extra
 * branch-current unknown {@code i} (the current into the primary's + terminal). With
 * {@code a = N/M} the stamp is
 * <pre>
 *   KCL rows:   p+ : +i     p- : -i     s+ : -a*i     s- : +a*i
 *   branch row: V(p+) - V(p-) - a*(V(s+) - V(s-)) = 0
 * </pre>
 * which makes the power into the primary, {@code V_p * i}, equal the power out of the
 * secondary, {@code V_s * a*i}. The coefficients are real, so the same element serves both the
 * transient {@link Circuit} and the small-signal {@link AcCircuit} solver.
 */
public class IdealTransformer {
	public final int primaryPlus, primaryMinus, secondaryPlus, secondaryMinus;
	/** Turns ratio {@code a = N/M} (primary turns over secondary turns). */
	public final double ratio;

	int branchIndex = -1;
	private double primaryCurrent;

	public IdealTransformer(int primaryPlus, int primaryMinus, int secondaryPlus, int secondaryMinus, double ratio) {
		this.primaryPlus = primaryPlus;
		this.primaryMinus = primaryMinus;
		this.secondaryPlus = secondaryPlus;
		this.secondaryMinus = secondaryMinus;
		this.ratio = ratio;
	}

	void setSolvedCurrent(double current) {
		this.primaryCurrent = current;
	}

	/** Current flowing into the primary's + terminal. */
	public double primaryCurrent() {
		return primaryCurrent;
	}

	/** Current the secondary's + terminal delivers to its load. */
	public double secondaryCurrent() {
		return ratio * primaryCurrent;
	}
}
