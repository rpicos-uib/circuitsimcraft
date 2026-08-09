package com.rpicos.circuitsimcraft.network;

import com.rpicos.circuitsimcraft.CircuitSimCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> client: sent right after the AC probe's first click (pinning an AC Source), so the
 *  HUD can show a hint ("source pinned, now click a signal point") until either a real
 *  {@link AcBodePayload} replaces it or the player unpins. */
public record AcHintPayload(BlockPos sourcePos) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<AcHintPayload> TYPE =
			new CustomPacketPayload.Type<>(CircuitSimCraft.id("ac_hint"));

	public static final StreamCodec<RegistryFriendlyByteBuf, AcHintPayload> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, AcHintPayload::sourcePos,
			AcHintPayload::new
	);

	@Override
	public CustomPacketPayload.Type<AcHintPayload> type() {
		return TYPE;
	}
}
