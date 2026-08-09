package com.rpicos.circuitsimcraft;

import com.google.common.collect.ImmutableSet;
import com.rpicos.circuitsimcraft.mixin.PoiTypesInvokerMixin;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/** All three of the mod's villager professions: the Electrician (basic components, job site
 *  {@link ModBlocks#BREADBOARD}), the Electronics Engineer (advanced components - transistors,
 *  controlled sources, the diode - job site {@link ModBlocks#WORKBENCH}), and the Electrical
 *  Engineer (three-phase components, job site {@link ModBlocks#SWITCHBOARD}). Unlike vanilla trades
 *  (hardcoded Java factories), this MC version's VillagerProfession only carries {@link
 *  ResourceKey}s pointing at data-driven TradeSet entries per level - the actual offers live
 *  under data/circuitsimcraft/{villager_trade,tags/villager_trade,trade_set}/&lt;profession&gt;/. */
public final class ModVillagers {
	public static final ResourceKey<PoiType> ELECTRICIAN_POI =
			ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, CircuitSimCraft.id("electrician"));
	public static final ResourceKey<VillagerProfession> ELECTRICIAN =
			ResourceKey.create(Registries.VILLAGER_PROFESSION, CircuitSimCraft.id("electrician"));

	public static final ResourceKey<PoiType> ENGINEER_POI =
			ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, CircuitSimCraft.id("electronics_engineer"));
	public static final ResourceKey<VillagerProfession> ENGINEER =
			ResourceKey.create(Registries.VILLAGER_PROFESSION, CircuitSimCraft.id("electronics_engineer"));

	public static final ResourceKey<PoiType> ELECTRICAL_ENGINEER_POI =
			ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, CircuitSimCraft.id("electrical_engineer"));
	public static final ResourceKey<VillagerProfession> ELECTRICAL_ENGINEER =
			ResourceKey.create(Registries.VILLAGER_PROFESSION, CircuitSimCraft.id("electrical_engineer"));

	private static ResourceKey<TradeSet> tradeSetLevel(String profession, int level) {
		return ResourceKey.create(Registries.TRADE_SET, CircuitSimCraft.id(profession + "/level_" + level));
	}

	private static Int2ObjectMap<ResourceKey<TradeSet>> tradeSetsByLevel(String profession) {
		return Int2ObjectMap.ofEntries(
				Int2ObjectMap.entry(1, tradeSetLevel(profession, 1)),
				Int2ObjectMap.entry(2, tradeSetLevel(profession, 2)),
				Int2ObjectMap.entry(3, tradeSetLevel(profession, 3)),
				Int2ObjectMap.entry(4, tradeSetLevel(profession, 4)),
				Int2ObjectMap.entry(5, tradeSetLevel(profession, 5)));
	}

	/** Registers one villager profession's PoiType (both in the registry AND in vanilla's own
	 *  private block-state lookup table via the Mixin invoker - see the inline comment on the
	 *  invoker call for why both are required) plus the profession itself. Shared by both
	 *  professions below since the pattern is identical apart from which block/POI/profession
	 *  id/trade-set-prefix/sound is used. */
	private static void registerProfession(ResourceKey<PoiType> poiKey, ResourceKey<VillagerProfession> professionKey,
			Block jobSiteBlock, String tradeSetPrefix, String translationKey) {
		Set<BlockState> jobSiteStates = ImmutableSet.copyOf(jobSiteBlock.getStateDefinition().getPossibleStates());
		Holder.Reference<PoiType> poiHolder = Registry.registerForHolder(
				BuiltInRegistries.POINT_OF_INTEREST_TYPE, poiKey, new PoiType(jobSiteStates, 1, 1));
		PoiTypesInvokerMixin.circuitsimcraft$registerBlockStates(poiHolder, jobSiteStates);

		Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, professionKey, new VillagerProfession(
				Component.translatable(translationKey),
				holder -> holder.is(poiKey),
				holder -> holder.is(poiKey),
				ImmutableSet.of(),
				ImmutableSet.of(),
				SoundEvents.VILLAGER_WORK_TOOLSMITH,
				tradeSetsByLevel(tradeSetPrefix)));
	}

	public static void init() {
		registerProfession(ELECTRICIAN_POI, ELECTRICIAN, ModBlocks.BREADBOARD, "electrician",
				"entity.circuitsimcraft.villager.electrician");
		registerProfession(ENGINEER_POI, ENGINEER, ModBlocks.WORKBENCH, "electronics_engineer",
				"entity.circuitsimcraft.villager.electronics_engineer");
		registerProfession(ELECTRICAL_ENGINEER_POI, ELECTRICAL_ENGINEER, ModBlocks.SWITCHBOARD, "electrical_engineer",
				"entity.circuitsimcraft.villager.electrical_engineer");
	}
}
