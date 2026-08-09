package com.rpicos.circuitsimcraft.client;

import com.rpicos.circuitsimcraft.CircuitSimCraft;
import com.rpicos.circuitsimcraft.network.AcBodePayload;
import com.rpicos.circuitsimcraft.network.AcHintPayload;
import com.rpicos.circuitsimcraft.network.OpenValueEditorPayload;
import com.rpicos.circuitsimcraft.network.ProbeDataPayload;
import com.rpicos.circuitsimcraft.network.XyProbeDataPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class CircuitSimCraftClient implements ClientModInitializer {
	private static final Identifier OSCILLOSCOPE_HUD_ID = CircuitSimCraft.id("oscilloscope");
	private static final Identifier XY_OSCILLOSCOPE_HUD_ID = CircuitSimCraft.id("xy_oscilloscope");
	private static final Identifier AC_OSCILLOSCOPE_HUD_ID = CircuitSimCraft.id("ac_oscilloscope");

	@Override
	public void onInitializeClient() {
		HudElementRegistry.addLast(OSCILLOSCOPE_HUD_ID, new OscilloscopeHud());
		HudElementRegistry.addLast(XY_OSCILLOSCOPE_HUD_ID, new XyOscilloscopeHud());
		HudElementRegistry.addLast(AC_OSCILLOSCOPE_HUD_ID, new AcOscilloscopeHud());

		ClientPlayNetworking.registerGlobalReceiver(ProbeDataPayload.TYPE,
				(payload, context) -> ProbeClientState.update(payload));
		ClientPlayNetworking.registerGlobalReceiver(XyProbeDataPayload.TYPE,
				(payload, context) -> XyProbeClientState.update(payload));
		ClientPlayNetworking.registerGlobalReceiver(OpenValueEditorPayload.TYPE,
				(payload, context) -> Minecraft.getInstance().setScreenAndShow(
						new ComponentValueScreen(payload.pos(), payload.fields())));
		ClientPlayNetworking.registerGlobalReceiver(AcHintPayload.TYPE,
				(payload, context) -> AcProbeClientState.updateHint(payload.sourcePos()));
		ClientPlayNetworking.registerGlobalReceiver(AcBodePayload.TYPE,
				(payload, context) -> AcProbeClientState.updateResult(payload));
	}
}
