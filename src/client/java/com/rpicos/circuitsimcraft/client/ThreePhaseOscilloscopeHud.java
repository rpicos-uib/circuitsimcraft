package com.rpicos.circuitsimcraft.client;

import com.rpicos.circuitsimcraft.item.ThreePhaseProbeItem;
import com.rpicos.circuitsimcraft.network.ThreePhaseProbeDataPayload;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * The bundle analog of {@link OscilloscopeHud}: up to
 * {@link com.rpicos.circuitsimcraft.network.ThreePhaseProbeWatchManager#MAX_CHANNELS} pinned
 * channels stacked in the corner, exactly as before - except each channel now overlays all
 * three phases (A/B/C) on the same graph, in the standard phase-color convention (red/yellow/
 * blue, matching the mod's block textures) rather than one trace per channel.
 */
public class ThreePhaseOscilloscopeHud implements HudElement {

	private static final int WIDTH = 118;
	private static final int HEIGHT = 74;
	private static final int MARGIN = 6;
	private static final int GAP = 4;
	private static final int HIGHLIGHT_COLOR = 0xFFFFD060;

	// Standard phase-color convention (A=red, B=yellow, C=blue), matching the 3-phase block textures.
	private static final int[] PHASE_COLORS = {0xFFD02828, 0xFFDCBE28, 0xFF325AD2};

	@Override
	public void extractRenderState(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null || !isHoldingProbe(player)) {
			return;
		}

		Font font = client.font;
		List<ThreePhaseProbeDataPayload> channels = ThreePhaseProbeClientState.currentChannels();

		if (channels.isEmpty()) {
			int y0 = extractor.guiHeight() - HEIGHT - MARGIN;
			drawFrame(extractor, MARGIN, y0);
			extractor.text(font, "no signal", MARGIN + 6, y0 + HEIGHT / 2 - 4, 0xFF808080);
			return;
		}

		for (int i = 0; i < channels.size(); i++) {
			int y0 = extractor.guiHeight() - MARGIN - (i + 1) * HEIGHT - i * GAP;
			drawChannel(extractor, font, MARGIN, y0, channels.get(i));
		}
	}

	private static void drawFrame(GuiGraphicsExtractor extractor, int x0, int y0) {
		extractor.fill(x0, y0, x0 + WIDTH, y0 + HEIGHT, 0xC0101014);
		extractor.outline(x0, y0, WIDTH, HEIGHT, 0xFF3A3A40);
	}

	private static void drawChannel(GuiGraphicsExtractor extractor, Font font, int x0, int y0,
			ThreePhaseProbeDataPayload data) {
		drawFrame(extractor, x0, y0);
		if (data.willBeReplacedNext()) {
			extractor.outline(x0 - 2, y0 - 2, WIDTH + 4, HEIGHT + 4, HIGHLIGHT_COLOR);
		}

		int x1 = x0 + WIDTH;
		List<List<Float>> histories = List.of(data.historyA(), data.historyB(), data.historyC());
		int graphX0 = x0 + 4;
		int graphY0 = y0 + 4;
		int graphX1 = x1 - 4;
		int graphY1 = y0 + HEIGHT - 24;
		int graphHeight = graphY1 - graphY0;
		int midY = graphY0 + graphHeight / 2;

		ScopeGrid.draw(extractor, graphX0, graphY0, graphX1, graphY1, 4, 4);
		extractor.horizontalLine(graphX0, graphX1, midY, 0xFF2E4A2E);

		// One shared auto-scale across all three phases, so their relative magnitudes stay
		// comparable on the same graph rather than each phase independently filling the height.
		float maxAbs = 0.001f;
		for (List<Float> history : histories) {
			for (float v : history) {
				maxAbs = Math.max(maxAbs, Math.abs(v));
			}
		}

		int axisColor = 0xFF889078;
		extractor.text(font, "+" + SiFormat.magnitude(maxAbs), graphX0, graphY0, axisColor, false);
		extractor.text(font, "-" + SiFormat.magnitude(maxAbs), graphX0, graphY1 - 8, axisColor, false);

		for (int phase = 0; phase < 3; phase++) {
			drawTrace(extractor, histories.get(phase), graphX0, graphX1, midY, graphHeight, maxAbs, PHASE_COLORS[phase]);
		}

		String reading = String.format("A%.1f B%.1f C%.1f V", data.voltageA(), data.voltageB(), data.voltageC());
		extractor.text(font, reading, x0 + 4, graphY1 + 2, 0xFFDDDDDD, false);
		String summary = data.willBeReplacedNext() ? data.summary() + " (next)" : data.summary();
		extractor.text(font, summary, x0 + 4, graphY1 + 12, 0xFFDDDDDD, false);
	}

	private static void drawTrace(GuiGraphicsExtractor extractor, List<Float> history,
			int graphX0, int graphX1, int midY, int graphHeight, float maxAbs, int traceColor) {
		if (history.size() < 2) {
			return;
		}
		int n = history.size();
		int span = graphX1 - graphX0;
		int prevX = graphX0;
		int prevY = midY - Math.round(history.get(0) / maxAbs * (graphHeight / 2f));
		for (int i = 1; i < n; i++) {
			int x = graphX0 + span * i / (n - 1);
			int y = midY - Math.round(history.get(i) / maxAbs * (graphHeight / 2f));
			extractor.fill(prevX, Math.min(prevY, y), x + 1, Math.max(prevY, y) + 1, traceColor);
			prevX = x;
			prevY = y;
		}
	}

	private static boolean isHoldingProbe(Player player) {
		ItemStack main = player.getMainHandItem();
		ItemStack off = player.getOffhandItem();
		return main.getItem() instanceof ThreePhaseProbeItem || off.getItem() instanceof ThreePhaseProbeItem;
	}
}
