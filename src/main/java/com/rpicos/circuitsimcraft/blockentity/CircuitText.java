package com.rpicos.circuitsimcraft.blockentity;

import net.minecraft.network.chat.Component;

/** Translatable probe-readout text. Numbers are formatted by the caller before they reach the
 *  translation, so every language shows the same precision; units live in the lang files, so a
 *  translation can localize them (e.g. "ohm" -> "Ом"). */
public final class CircuitText {
	private CircuitText() {
	}

	public static Component probe(String key, Object... args) {
		return Component.translatable("probe.circuitsimcraft." + key, args);
	}

	/** Marks an unpowered source's readout with the "(off - needs redstone)" note. */
	public static Component powered(Component base, boolean redstonePowered) {
		return redstonePowered ? base : probe("off_needs_redstone", base);
	}
}
