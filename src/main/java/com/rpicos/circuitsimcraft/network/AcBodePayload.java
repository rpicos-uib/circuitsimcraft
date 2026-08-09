package com.rpicos.circuitsimcraft.network;

import com.rpicos.circuitsimcraft.CircuitSimCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/** Server -> client: the result of an AC probe's second click - a full Bode sweep (log-spaced
 *  frequency, magnitude in dB, phase in degrees, all in matching order) of the signal position's
 *  voltage relative to the pinned AC source, or a non-null {@code warning} in place of sweep data
 *  if the sweep couldn't be performed at all. */
public record AcBodePayload(BlockPos sourcePos, BlockPos signalPos, List<Float> freqsHz,
		List<Float> magnitudesDb, List<Float> phasesDeg, String warning) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<AcBodePayload> TYPE =
			new CustomPacketPayload.Type<>(CircuitSimCraft.id("ac_bode"));

	public static final StreamCodec<RegistryFriendlyByteBuf, AcBodePayload> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, AcBodePayload::sourcePos,
			BlockPos.STREAM_CODEC, AcBodePayload::signalPos,
			ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), AcBodePayload::freqsHz,
			ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), AcBodePayload::magnitudesDb,
			ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), AcBodePayload::phasesDeg,
			ByteBufCodecs.STRING_UTF8, AcBodePayload::warning,
			AcBodePayload::new
	);

	@Override
	public CustomPacketPayload.Type<AcBodePayload> type() {
		return TYPE;
	}
}
