package com.rpicos.circuitsimcraft.sim;

/**
 * The AC counterpart of {@link IdealOpAmp}: rather than an infinite-gain nullor (the standard
 * DC/transient idealization, exact for the frequencies this mod's transient solver actually
 * cares about), the AC solver uses a two-pole open-loop gain model,
 * <pre>A(s) = A0 / [(1 + s/omega_p1)(1 + s/omega_p2)],  s = j*omega</pre>
 * the textbook first-order approximation of a real op-amp's frequency-dependent gain rolloff
 * (a dominant pole from internal compensation, followed by a second, much higher, non-dominant
 * pole from a secondary internal stage). Values are fixed at construction to a single "generic
 * op-amp" preset (100 dB of DC gain, a dominant pole at 20 Hz, a second pole at 3 MHz) rather
 * than exposed per-instance, since the point of this model is to make Bode-plot gain rolloff and
 * phase margin visible at all, not to match any specific real part number.
 *
 * <p>Modeled in the AC solver as a voltage-controlled voltage source: {@code Vout = A(s) *
 * (V+ - V-)}, with infinite input impedance (no current drawn at {@code plus}/{@code minus}) and
 * zero output impedance, the same "own branch-current unknown, asymmetric constraint-vs-injection"
 * MNA treatment {@link IdealOpAmp} uses, except the constraint's coefficient is the complex gain
 * {@code A(s)} rather than a fixed 1.
 */
public final class AcOpAmp {
	public static final double DEFAULT_DC_GAIN = 1e5;
	public static final double DEFAULT_POLE1_HZ = 20;
	public static final double DEFAULT_POLE2_HZ = 3_000_000;

	private final int plus, minus, out;
	private final double dcGain;
	private final double pole1Hz;
	private final double pole2Hz;

	int branchIndex = -1;

	public AcOpAmp(int plus, int minus, int out) {
		this(plus, minus, out, DEFAULT_DC_GAIN, DEFAULT_POLE1_HZ, DEFAULT_POLE2_HZ);
	}

	public AcOpAmp(int plus, int minus, int out, double dcGain, double pole1Hz, double pole2Hz) {
		this.plus = plus;
		this.minus = minus;
		this.out = out;
		this.dcGain = dcGain;
		this.pole1Hz = pole1Hz;
		this.pole2Hz = pole2Hz;
	}

	public int plus() {
		return plus;
	}

	public int minus() {
		return minus;
	}

	public int out() {
		return out;
	}

	/** {@code A0 / [(1 + j*omega/omega_p1)(1 + j*omega/omega_p2)]}. */
	public Complex gainAt(double omega) {
		Complex denom1 = Complex.ONE.add(new Complex(0, omega / (2 * Math.PI * pole1Hz)));
		Complex denom2 = Complex.ONE.add(new Complex(0, omega / (2 * Math.PI * pole2Hz)));
		return Complex.real(dcGain).divide(denom1.multiply(denom2));
	}
}
