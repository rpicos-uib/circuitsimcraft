package com.rpicos.circuitsimcraft.sim;

/** The AC (frequency-domain) counterpart of {@link Element}: stamps a complex admittance rather
 *  than a real conductance, evaluated at a specific angular frequency rather than advanced by a
 *  timestep. */
public interface AcElement {

	/** Adds this element's contribution to the complex admittance matrix / RHS at angular
	 *  frequency {@code omega} (rad/s). */
	void stampAc(AcCircuit circuit, Complex[][] mat, Complex[] z, double omega);
}
