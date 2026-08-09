# CircuitSimCraft: Experiment 8 - Bundle/Unbundle Round Trip
#
# A 3-Phase Source drives a bundled trunk into an Unbundler, which splits it into three separate
# mono legs - each with its own ordinary Resistor in series - that a Bundler then recombines back
# into a single bundle. That bundle feeds a 3-Phase Resistor and closes directly at a Ground
# block, with no second Unbundler needed: Ground is bundle-conductive as well as mono-conductive
# (see README's "Three-Phase Components" section), so a bundle run can end at Ground exactly like
# a mono run always could. Run with:
#
#   /function circuitsimcraft:three_phase_bundle_unbundle
#
# Stand where you want the bench's southwest corner before running it - everything is placed
# relative to that position (floor a few blocks below your feet, to leave room for the
# Unbundler's own two-level drop and the Bundler's rebuilt trunk above it). Clears its own space
# and pours its own foundation first, so it's safe to run on uneven terrain.
#
# No right-clicks needed: every component is left at its default preset (230V/1Hz source, 100 ohm
# resistors throughout). Flip the lever, then pin the given 3-phase probe to the source or the
# final 3-Phase Resistor to see all three phases at once - or pin the plain probe to any of the
# three individual mono resistors in the middle to see just that one phase's own voltage/current.
# Expected: each mono resistor reads one phase's instantaneous voltage/current (Ohm's law exact
# against its own 100 ohm), and the reunited bundle downstream of the Bundler carries the same
# three-phase signal the source itself produces - unbundling and rebundling changes nothing
# electrically, since the Bundler/Unbundler pair is purely topological (no impedance added).

fill ~-1 ~-3 ~-4 ~5 ~2 ~2 minecraft:air
fill ~-1 ~-3 ~-4 ~5 ~-3 ~2 minecraft:stone

# Source -> bundle trunk -> Unbundler (bundle face is fixed Up, so it sits one level below the
# trunk's last wire block, same convention as three_phase_load.mcfunction).
setblock ~0 ~0 ~0 circuitsimcraft:ground
setblock ~1 ~0 ~0 circuitsimcraft:three_phase_source[facing=east]
setblock ~2 ~0 ~0 circuitsimcraft:three_phase_wire
setblock ~3 ~0 ~0 circuitsimcraft:three_phase_wire
setblock ~3 ~-1 ~0 circuitsimcraft:three_phase_unbundler

# Phase A (Unbundler's north leg <-> Bundler's south leg): a straight north-south chain with one
# Resistor sandwiched directly between the two - the only one of the three phases whose Unbundler
# and Bundler leg point at each other directly, since both blocks share the same fixed North/
# East/South mono-face layout.
setblock ~3 ~-1 ~-1 circuitsimcraft:resistor[facing=north]
setblock ~3 ~-1 ~-2 circuitsimcraft:three_phase_bundler

# Phase B (Unbundler's east leg <-> Bundler's east leg): one wire block east of the Unbundler,
# a Resistor turning north, then one more wire block to actually reach the Bundler's own east
# leg - the Unbundler and Bundler are two blocks apart, so (like phase C, unlike phase A, where
# the Bundler happens to sit in the very next block) this run needs a block at each end plus the
# resistor in between, not just two blocks.
setblock ~4 ~-1 ~0 circuitsimcraft:wire
setblock ~4 ~-1 ~-1 circuitsimcraft:resistor[facing=north]
setblock ~4 ~-1 ~-2 circuitsimcraft:wire

# Phase C (Unbundler's south leg <-> Bundler's north leg): the two farthest-apart legs, routed
# one level below the other two phases so it can pass underneath them without ever touching
# either (a mono wire only unions with a block it's actually adjacent to - passing nearby at a
# different height is how three independent phase legs share the same neighborhood without
# shorting into each other).
setblock ~3 ~-1 ~1 circuitsimcraft:wire
setblock ~3 ~-2 ~1 circuitsimcraft:wire
setblock ~3 ~-2 ~0 circuitsimcraft:wire
setblock ~3 ~-2 ~-1 circuitsimcraft:resistor[facing=north]
setblock ~3 ~-2 ~-2 circuitsimcraft:wire
setblock ~3 ~-2 ~-3 circuitsimcraft:wire
setblock ~3 ~-1 ~-3 circuitsimcraft:wire

# Bundler's rebuilt bundle trunk (its bundle face is Up too) straight into a 3-Phase Resistor,
# closing directly at Ground - no second Unbundler needed.
setblock ~3 ~0 ~-2 circuitsimcraft:three_phase_resistor[facing=down]
setblock ~3 ~1 ~-2 circuitsimcraft:ground

# Lever on top of the source, left off - marks a clean t=0 once flipped.
setblock ~1 ~1 ~0 minecraft:lever[face=floor,facing=north,powered=false]

give @s circuitsimcraft:three_phase_probe 1
give @s circuitsimcraft:probe 1
