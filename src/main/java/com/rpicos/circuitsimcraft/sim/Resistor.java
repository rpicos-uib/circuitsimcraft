package com.rpicos.circuitsimcraft.sim;

public class Resistor implements Element, AcElement {
	public final int a, b;
	public double ohms;

	public Resistor(int a, int b, double ohms) {
		this.a = a;
		this.b = b;
		this.ohms = ohms;
	}

	@Override
	public void stamp(Circuit circuit, double[][] mat, double[] z, double dt) {
		circuit.stampConductance(mat, a, b, 1.0 / ohms);
	}

	@Override
	public void updateState(Circuit circuit, double dt) {
		// stateless
	}

	public double current(Circuit circuit) {
		return (circuit.getVoltage(a) - circuit.getVoltage(b)) / ohms;
	}

	/** AC case: a resistor's impedance is frequency-independent, {@code Z = R}, so its admittance
	 *  is just the same real conductance used for the transient stamp. */
	@Override
	public void stampAc(AcCircuit circuit, Complex[][] mat, Complex[] z, double omega) {
		circuit.stampAdmittance(mat, a, b, Complex.real(1.0 / ohms));
	}
}
