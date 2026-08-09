package com.rpicos.circuitsimcraft.sim;

/** Trapezoidal-integration companion model, dual of {@link Capacitor}. */
public class Inductor implements Element, AcElement {
	public final int a, b;
	public double henries;

	private double vPrev = 0;
	private double iPrev = 0;

	public Inductor(int a, int b, double henries) {
		this.a = a;
		this.b = b;
		this.henries = henries;
	}

	@Override
	public void stamp(Circuit circuit, double[][] mat, double[] z, double dt) {
		double geq = dt / (2 * henries);
		double ieq = geq * vPrev + iPrev;
		circuit.stampConductance(mat, a, b, geq);
		circuit.stampCurrentSource(z, a, b, ieq);
	}

	@Override
	public void updateState(Circuit circuit, double dt) {
		double geq = dt / (2 * henries);
		double vNow = circuit.getVoltage(a) - circuit.getVoltage(b);
		double iNow = geq * vNow + geq * vPrev + iPrev;
		vPrev = vNow;
		iPrev = iNow;
	}

	public double current() {
		return iPrev;
	}

	/** AC case: an inductor's impedance is {@code Z = j*omega*L}, so its admittance is
	 *  {@code Y = 1/(j*omega*L)}. */
	@Override
	public void stampAc(AcCircuit circuit, Complex[][] mat, Complex[] z, double omega) {
		circuit.stampAdmittance(mat, a, b, Complex.ONE.divide(new Complex(0, omega * henries)));
	}
}
