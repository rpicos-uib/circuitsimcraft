package com.rpicos.circuitsimcraft.sim;

/**
 * Charge-controlled linear-drift memristor (HP model, no boundary window function).
 * Resistance is a linear function of accumulated charge between {@code ron} and {@code roff}.
 *
 * <p>The state charge is updated one step behind the solve (this step's resistance comes from
 * last step's charge, then charge is advanced from this step's current) rather than solved
 * implicitly. That lag is negligible at the small timesteps this simulator runs at, but means
 * very large dt values can make the memristor's response ring or lag visibly.
 */
public class Memristor implements Element, AcElement {
	public final int a, b;
	public double ron;
	public double roff;
	public double qMax;

	private double q;
	private double rNow;

	public Memristor(int a, int b, double ron, double roff, double qMax, double initialFraction) {
		this.a = a;
		this.b = b;
		this.ron = ron;
		this.roff = roff;
		this.qMax = qMax;
		this.q = qMax * clamp01(initialFraction);
		this.rNow = ron + (roff - ron) * clamp01(this.q / qMax);
	}

	private static double clamp01(double x) {
		return Math.max(0, Math.min(1, x));
	}

	@Override
	public void stamp(Circuit circuit, double[][] mat, double[] z, double dt) {
		rNow = ron + (roff - ron) * clamp01(q / qMax);
		circuit.stampConductance(mat, a, b, 1.0 / rNow);
	}

	@Override
	public void updateState(Circuit circuit, double dt) {
		double v = circuit.getVoltage(a) - circuit.getVoltage(b);
		double i = v / rNow;
		q = Math.max(0, Math.min(qMax, q + i * dt));
	}

	public double resistance() {
		return rNow;
	}

	public double current(Circuit circuit) {
		return (circuit.getVoltage(a) - circuit.getVoltage(b)) / rNow;
	}

	public double stateFraction() {
		return q / qMax;
	}

	/** AC case: for now, the memristor's frequency-dependent behavior is not modeled at all - its
	 *  small-signal impedance is simply its current (charge-dependent) resistance, exactly as if
	 *  it were a plain resistor frozen at whatever value {@link #resistance()} currently holds.
	 *  A genuine AC memristor model would need its own frequency response derived from the
	 *  charge-controlled state equation; this is a deliberate simplification, not an oversight. */
	@Override
	public void stampAc(AcCircuit circuit, Complex[][] mat, Complex[] z, double omega) {
		circuit.stampAdmittance(mat, a, b, Complex.real(1.0 / rNow));
	}
}
