package com.rpicos.circuitsimcraft.client;

/** Formats a non-negative magnitude using the SI prefixes relevant to the values this mod's
 *  oscilloscopes actually display (a few kilo-something down to a few pico-something), so a
 *  scale label stays compact and readable regardless of whether the underlying signal is a
 *  handful of volts or a few microamps. */
final class SiFormat {
	private SiFormat() {
	}

	static String magnitude(float value) {
		if (value == 0f) {
			return "0.00";
		}
		if (value >= 1e3f) {
			return String.format("%.2fk", value / 1e3f);
		}
		if (value >= 1f) {
			return String.format("%.2f", value);
		}
		if (value >= 1e-3f) {
			return String.format("%.2fm", value / 1e-3f);
		}
		if (value >= 1e-6f) {
			return String.format("%.2fu", value / 1e-6f);
		}
		if (value >= 1e-9f) {
			return String.format("%.2fn", value / 1e-9f);
		}
		return String.format("%.2fp", value / 1e-12f);
	}
}
