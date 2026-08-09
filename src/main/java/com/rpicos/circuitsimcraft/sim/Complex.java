package com.rpicos.circuitsimcraft.sim;

/** A complex number, immutable. Used only by the AC (frequency-domain) solver - the transient
 *  solver stamped by {@link Circuit} stays entirely real-valued. */
public record Complex(double re, double im) {
	public static final Complex ZERO = new Complex(0, 0);
	public static final Complex ONE = new Complex(1, 0);

	public static Complex real(double value) {
		return new Complex(value, 0);
	}

	/** {@code magnitude * e^{i*angleRad}}. */
	public static Complex polar(double magnitude, double angleRad) {
		return new Complex(magnitude * Math.cos(angleRad), magnitude * Math.sin(angleRad));
	}

	public Complex add(Complex other) {
		return new Complex(re + other.re, im + other.im);
	}

	public Complex subtract(Complex other) {
		return new Complex(re - other.re, im - other.im);
	}

	public Complex multiply(Complex other) {
		return new Complex(re * other.re - im * other.im, re * other.im + im * other.re);
	}

	public Complex divide(Complex other) {
		double denom = other.re * other.re + other.im * other.im;
		return new Complex((re * other.re + im * other.im) / denom, (im * other.re - re * other.im) / denom);
	}

	public Complex negate() {
		return new Complex(-re, -im);
	}

	public double magnitude() {
		return Math.hypot(re, im);
	}

	/** Phase angle in radians. */
	public double angle() {
		return Math.atan2(im, re);
	}
}
