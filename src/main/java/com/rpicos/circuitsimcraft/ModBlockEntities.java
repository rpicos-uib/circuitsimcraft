package com.rpicos.circuitsimcraft;

import com.rpicos.circuitsimcraft.blockentity.AcSourceBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.AmmeterBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.CapacitorBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.CccsBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.CcvsBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.CurrentSourceBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.DiodeBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.FrequencyModuleBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.FunctionGeneratorBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.GroundBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.InductorBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.MemristorBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.NmosBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.NpnBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.OpAmpBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.PmosBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.PnpBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.PowerSupplyBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.R2VConverterBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.ResistorBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.ThreePhaseAmmeterBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.ThreePhaseBundlerBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.ThreePhaseCapacitorBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.ThreePhaseInductorBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.ThreePhaseResistorBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.ThreePhaseSourceBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.ThreePhaseUnbundlerBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.ThreePhaseWireBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.V2RConverterBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.VccsBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.VcvsBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.VoltageModuleBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.VoltmeterBlockEntity;
import com.rpicos.circuitsimcraft.blockentity.WireBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

public final class ModBlockEntities {
	public static final BlockEntityType<ResistorBlockEntity> RESISTOR =
			register("resistor", ResistorBlockEntity::new, ModBlocks.RESISTOR);
	public static final BlockEntityType<CapacitorBlockEntity> CAPACITOR =
			register("capacitor", CapacitorBlockEntity::new, ModBlocks.CAPACITOR);
	public static final BlockEntityType<InductorBlockEntity> INDUCTOR =
			register("inductor", InductorBlockEntity::new, ModBlocks.INDUCTOR);
	public static final BlockEntityType<MemristorBlockEntity> MEMRISTOR =
			register("memristor", MemristorBlockEntity::new, ModBlocks.MEMRISTOR);
	public static final BlockEntityType<PowerSupplyBlockEntity> POWER_SUPPLY =
			register("power_supply", PowerSupplyBlockEntity::new, ModBlocks.POWER_SUPPLY);
	public static final BlockEntityType<FunctionGeneratorBlockEntity> FUNCTION_GENERATOR =
			register("function_generator", FunctionGeneratorBlockEntity::new, ModBlocks.FUNCTION_GENERATOR);
	public static final BlockEntityType<WireBlockEntity> WIRE =
			register("wire", WireBlockEntity::new, ModBlocks.WIRE);
	public static final BlockEntityType<AmmeterBlockEntity> AMMETER =
			register("ammeter", AmmeterBlockEntity::new, ModBlocks.AMMETER);
	public static final BlockEntityType<VoltageModuleBlockEntity> VOLTAGE_MODULE =
			register("voltage_module", VoltageModuleBlockEntity::new, ModBlocks.VOLTAGE_MODULE);
	public static final BlockEntityType<FrequencyModuleBlockEntity> FREQUENCY_MODULE =
			register("frequency_module", FrequencyModuleBlockEntity::new, ModBlocks.FREQUENCY_MODULE);
	public static final BlockEntityType<GroundBlockEntity> GROUND =
			register("ground", GroundBlockEntity::new, ModBlocks.GROUND);
	public static final BlockEntityType<DiodeBlockEntity> DIODE =
			register("diode", DiodeBlockEntity::new, ModBlocks.DIODE);
	public static final BlockEntityType<OpAmpBlockEntity> OP_AMP =
			register("op_amp", OpAmpBlockEntity::new, ModBlocks.OP_AMP);
	public static final BlockEntityType<AcSourceBlockEntity> AC_SOURCE =
			register("ac_source", AcSourceBlockEntity::new, ModBlocks.AC_SOURCE);
	public static final BlockEntityType<R2VConverterBlockEntity> R2V_CONVERTER =
			register("r2v_converter", R2VConverterBlockEntity::new, ModBlocks.R2V_CONVERTER);
	public static final BlockEntityType<V2RConverterBlockEntity> V2R_CONVERTER =
			register("v2r_converter", V2RConverterBlockEntity::new, ModBlocks.V2R_CONVERTER);
	public static final BlockEntityType<CurrentSourceBlockEntity> CURRENT_SOURCE =
			register("current_source", CurrentSourceBlockEntity::new, ModBlocks.CURRENT_SOURCE);
	public static final BlockEntityType<VoltmeterBlockEntity> VOLTMETER =
			register("voltmeter", VoltmeterBlockEntity::new, ModBlocks.VOLTMETER);
	public static final BlockEntityType<NpnBlockEntity> NPN =
			register("npn", NpnBlockEntity::new, ModBlocks.NPN);
	public static final BlockEntityType<PnpBlockEntity> PNP =
			register("pnp", PnpBlockEntity::new, ModBlocks.PNP);
	public static final BlockEntityType<NmosBlockEntity> NMOS =
			register("nmos", NmosBlockEntity::new, ModBlocks.NMOS);
	public static final BlockEntityType<PmosBlockEntity> PMOS =
			register("pmos", PmosBlockEntity::new, ModBlocks.PMOS);
	public static final BlockEntityType<VcvsBlockEntity> VCVS =
			register("vcvs", VcvsBlockEntity::new, ModBlocks.VCVS);
	public static final BlockEntityType<VccsBlockEntity> VCCS =
			register("vccs", VccsBlockEntity::new, ModBlocks.VCCS);
	public static final BlockEntityType<CccsBlockEntity> CCCS =
			register("cccs", CccsBlockEntity::new, ModBlocks.CCCS);
	public static final BlockEntityType<CcvsBlockEntity> CCVS =
			register("ccvs", CcvsBlockEntity::new, ModBlocks.CCVS);
	public static final BlockEntityType<ThreePhaseWireBlockEntity> THREE_PHASE_WIRE =
			register("three_phase_wire", ThreePhaseWireBlockEntity::new, ModBlocks.THREE_PHASE_WIRE);
	public static final BlockEntityType<ThreePhaseSourceBlockEntity> THREE_PHASE_SOURCE =
			register("three_phase_source", ThreePhaseSourceBlockEntity::new, ModBlocks.THREE_PHASE_SOURCE);
	public static final BlockEntityType<ThreePhaseAmmeterBlockEntity> THREE_PHASE_AMMETER =
			register("three_phase_ammeter", ThreePhaseAmmeterBlockEntity::new, ModBlocks.THREE_PHASE_AMMETER);
	public static final BlockEntityType<ThreePhaseBundlerBlockEntity> THREE_PHASE_BUNDLER =
			register("three_phase_bundler", ThreePhaseBundlerBlockEntity::new, ModBlocks.THREE_PHASE_BUNDLER);
	public static final BlockEntityType<ThreePhaseUnbundlerBlockEntity> THREE_PHASE_UNBUNDLER =
			register("three_phase_unbundler", ThreePhaseUnbundlerBlockEntity::new, ModBlocks.THREE_PHASE_UNBUNDLER);
	public static final BlockEntityType<ThreePhaseResistorBlockEntity> THREE_PHASE_RESISTOR =
			register("three_phase_resistor", ThreePhaseResistorBlockEntity::new, ModBlocks.THREE_PHASE_RESISTOR);
	public static final BlockEntityType<ThreePhaseInductorBlockEntity> THREE_PHASE_INDUCTOR =
			register("three_phase_inductor", ThreePhaseInductorBlockEntity::new, ModBlocks.THREE_PHASE_INDUCTOR);
	public static final BlockEntityType<ThreePhaseCapacitorBlockEntity> THREE_PHASE_CAPACITOR =
			register("three_phase_capacitor", ThreePhaseCapacitorBlockEntity::new, ModBlocks.THREE_PHASE_CAPACITOR);

	private static <T extends BlockEntity> BlockEntityType<T> register(
			String path, BlockEntityType.BlockEntitySupplier<T> factory, Block block) {
		ResourceKey<BlockEntityType<?>> key = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, CircuitSimCraft.id(path));
		BlockEntityType<T> type = new BlockEntityType<>(factory, Set.of(block));
		return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, type);
	}

	public static void init() {
	}
}
