package com.rpicos.circuitsimcraft.blockentity;

import com.rpicos.circuitsimcraft.ModBlockEntities;
import com.rpicos.circuitsimcraft.network.BundleBridge;
import com.rpicos.circuitsimcraft.network.BundleParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Bridges the ordinary and bundle graphs: three separate single-phase leads (North/East/South
 *  = phase A/B/C) on one side, one bundled lead (Up) on the other - purely topological, no
 *  circuit element stamped at all, exactly like a plain wire itself adds no element. Fixed,
 *  non-rotatable face roles (no {@code FACING} property, unlike a normal {@code ComponentBlock} -
 *  every face already has a distinct, permanent meaning). {@link ThreePhaseUnbundlerBlockEntity}
 *  extends this directly, registering under its own {@link BlockEntityType} and only overriding
 *  {@link #deviceName()} - the two are electrically identical, purely a naming/UX distinction for
 *  which "direction" a player thinks of the crossing as going. */
public class ThreePhaseBundlerBlockEntity extends NetworkBlockEntity implements BundleParticipant, BundleBridge {

	private static final Direction BUNDLE_FACE = Direction.UP;
	private static final Direction[] MONO_FACES = {Direction.NORTH, Direction.EAST, Direction.SOUTH};

	public ThreePhaseBundlerBlockEntity(BlockPos pos, BlockState state) {
		this(ModBlockEntities.THREE_PHASE_BUNDLER, pos, state);
	}

	/** Lets {@link ThreePhaseUnbundlerBlockEntity} extend this class directly while still
	 *  registering under its own, distinct {@link BlockEntityType} instead of the Bundler's. */
	protected ThreePhaseBundlerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public Direction bundleFace() {
		return BUNDLE_FACE;
	}

	@Override
	public Direction monoFace(int phase) {
		return MONO_FACES[phase];
	}

	@Override
	public boolean isConductiveTowards(Direction direction) {
		return direction == MONO_FACES[0] || direction == MONO_FACES[1] || direction == MONO_FACES[2];
	}

	@Override
	public boolean isBundleConductiveTowards(Direction direction) {
		return direction == BUNDLE_FACE;
	}

	protected String deviceName() {
		return "Bundler";
	}

	public String probeSummary() {
		return deviceName() + ": phase A=north, B=east, C=south <-> bundle=up";
	}
}
