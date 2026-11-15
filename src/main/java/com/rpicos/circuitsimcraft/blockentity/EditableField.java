package com.rpicos.circuitsimcraft.blockentity;

import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import io.netty.buffer.ByteBuf;

/** One numeric parameter exposed by a {@link ValueEditable} component to the value-editor
 *  screen: a label, a display unit, and the same min/max range already used to bound that
 *  component's existing right-click preset cycle - the editor never accepts a value outside
 *  the range the component could already reach some other way. {@code label} is a translation
 *  key; {@code unit} is a plain symbol that a lang file may optionally localize. */
public record EditableField(String label, String unit, double min, double max, double current) {

	public Component labelText() {
		return Component.translatable(label);
	}

	/** The unit as display text - translated where a lang file defines
	 *  {@code unit.circuitsimcraft.<unit>} (e.g. "ohm" -> "Ом"), otherwise the symbol itself. */
	public Component unitText() {
		return Component.translatableWithFallback("unit.circuitsimcraft." + unit, unit);
	}

	public static final StreamCodec<ByteBuf, EditableField> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, EditableField::label,
			ByteBufCodecs.STRING_UTF8, EditableField::unit,
			ByteBufCodecs.DOUBLE, EditableField::min,
			ByteBufCodecs.DOUBLE, EditableField::max,
			ByteBufCodecs.DOUBLE, EditableField::current,
			EditableField::new
	);
}
