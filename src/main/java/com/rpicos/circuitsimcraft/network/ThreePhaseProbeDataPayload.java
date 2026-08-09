package com.rpicos.circuitsimcraft.network;

import com.rpicos.circuitsimcraft.CircuitSimCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/** Server -> client: a bundled component's live readout and recent history, phase A/B/C each -
 *  the bundle analog of {@link ProbeDataPayload}. */
public record ThreePhaseProbeDataPayload(BlockPos pos, String summary,
		float voltageA, float voltageB, float voltageC,
		float currentA, float currentB, float currentC,
		List<Float> historyA, List<Float> historyB, List<Float> historyC,
		boolean willBeReplacedNext) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ThreePhaseProbeDataPayload> TYPE =
			new CustomPacketPayload.Type<>(CircuitSimCraft.id("three_phase_probe_data"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ThreePhaseProbeDataPayload> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, ThreePhaseProbeDataPayload::pos,
			ByteBufCodecs.STRING_UTF8, ThreePhaseProbeDataPayload::summary,
			ByteBufCodecs.FLOAT, ThreePhaseProbeDataPayload::voltageA,
			ByteBufCodecs.FLOAT, ThreePhaseProbeDataPayload::voltageB,
			ByteBufCodecs.FLOAT, ThreePhaseProbeDataPayload::voltageC,
			ByteBufCodecs.FLOAT, ThreePhaseProbeDataPayload::currentA,
			ByteBufCodecs.FLOAT, ThreePhaseProbeDataPayload::currentB,
			ByteBufCodecs.FLOAT, ThreePhaseProbeDataPayload::currentC,
			ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), ThreePhaseProbeDataPayload::historyA,
			ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), ThreePhaseProbeDataPayload::historyB,
			ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), ThreePhaseProbeDataPayload::historyC,
			ByteBufCodecs.BOOL, ThreePhaseProbeDataPayload::willBeReplacedNext,
			ThreePhaseProbeDataPayload::new
	);

	@Override
	public CustomPacketPayload.Type<ThreePhaseProbeDataPayload> type() {
		return TYPE;
	}
}
