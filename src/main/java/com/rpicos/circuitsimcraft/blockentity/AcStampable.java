package com.rpicos.circuitsimcraft.blockentity;

import com.rpicos.circuitsimcraft.sim.AcCircuit;

/** Implemented by any two-terminal component that participates in an AC (Bode-plot) sweep,
 *  mirroring {@link ComponentBlockEntity#addToCircuit} - registers this component's contribution
 *  into the given {@link AcCircuit} once; the frequency dependence itself lives inside whatever
 *  {@code com.rpicos.circuitsimcraft.sim.AcElement} or {@code AcVoltageSource} gets added here; it is
 *  supplied later, once per swept frequency, when the circuit is actually solved, exactly as
 *  {@code dt} is only supplied to {@link com.rpicos.circuitsimcraft.sim.Circuit#step} and not to
 *  {@code addToCircuit} itself. */
public interface AcStampable {
	void addToAcCircuit(AcCircuit circuit, int nodeA, int nodeB);
}
