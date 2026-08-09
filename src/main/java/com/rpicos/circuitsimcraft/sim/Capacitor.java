package com.rpicos.circuitsimcraft.sim;

/** Trapezoidal-integration companion model: a conductance in parallel with a history current source. */
public class Capacitor implements Element, AcElement {
	public final int a, b;
	public double farads;

	private double vPrev = 0;
	private double iPrev = 0;

	public Capacitor(int a, int b, double farads) {
		this.a = a;
		this.b = b;
		this.farads = farads;
	}

	@Override
	public void stamp(Circuit circuit, double[][] mat, double[] z, double dt) {
		double geq = 2 * farads / dt;
		double ieq = -(geq * vPrev + iPrev);
		circuit.stampConductance(mat, a, b, geq);
		circuit.stampCurrentSource(z, a, b, ieq);
	}

	@Override
	public void updateState(Circuit circuit, double dt) {
		double geq = 2 * farads / dt;
		double vNow = circuit.getVoltage(a) - circuit.getVoltage(b);
		double iNow = geq * (vNow - vPrev) - iPrev;
		vPrev = vNow;
		iPrev = iNow;
	}

	public double current() {
		return iPrev;
	}

	public double voltage() {
		return vPrev;
	}

	/** AC case: a capacitor's impedance is {@code Z = 1/(j*omega*C)}, so its admittance is
	 *  {@code Y = j*omega*C} directly - no companion history term, since AC analysis solves the
	 *  steady-state sinusoidal response at a single frequency rather than integrating forward
	 *  through time. */
	@Override
	public void stampAc(AcCircuit circuit, Complex[][] mat, Complex[] z, double omega) {
		circuit.stampAdmittance(mat, a, b, new Complex(0, omega * farads));
	}
}
