# CircuitSimCraft: Experiment 7 - Balanced Three-Phase Resistive Load
#
# A 3-phase source driving a balanced resistive load through a bundled 3-phase wire trunk (with
# a turn, to show the bundle carries all three phases through any run, not just straight lines),
# closed back to the source's own neutral via an Unbundler and three separate Ground blocks - any
# Ground anchors to the same 0V reference regardless of position, so the three phase-return legs
# don't need to be physically routed back to one shared point. See
# latex_mod/sections/07_results_experiments.tex for the equivalent single-phase writeups this
# continues (once that paper's own three-phase section exists). Run with:
#
#   /function circuitsimcraft:three_phase_load
#
# Stand where you want the bench's southwest corner before running it - everything is placed
# relative to that position (floor one block below your feet). Clears its own space and pours
# its own foundation first, so it's safe to run on uneven terrain.
#
# No right-clicks needed: the source is left at its default 230V/1Hz preset, the resistor at its
# default 100 ohm preset. Flip the lever, then pin the given 3-phase oscilloscope probe to the
# resistor (or any bundle wire block along the trunk) to see all three phases at once. Expected:
# three equal-amplitude sinusoids 120 degrees apart, each phase's voltage and current in an exact
# Ohm's-law relationship with the shared 100 ohm load (~2.3A peak per phase), and - the defining
# balanced three-phase signature - the three instantaneous values summing to exactly zero at
# every moment, not just on average.

fill ~-1 ~0 ~-2 ~6 ~3 ~3 minecraft:air
fill ~-1 ~-1 ~-2 ~6 ~-1 ~3 minecraft:stone

# The loop: ground - 3-phase source - bundle wire (with a turn) - 3-phase resistor - bundle wire
# - Unbundler - three separate mono legs, each closed by its own Ground block.
setblock ~0 ~0 ~0 circuitsimcraft:ground
setblock ~1 ~0 ~0 circuitsimcraft:three_phase_source[facing=east]
setblock ~2 ~0 ~0 circuitsimcraft:three_phase_wire
setblock ~3 ~0 ~0 circuitsimcraft:three_phase_resistor[facing=east]
setblock ~4 ~0 ~0 circuitsimcraft:three_phase_wire

# The Unbundler sits directly below the trunk's last wire block - its bundle face is always Up,
# so it can only receive a bundled connection from directly above, not inline in a horizontal run.
setblock ~4 ~-1 ~0 circuitsimcraft:three_phase_unbundler

# Three separate mono return legs (phase A/B/C = north/east/south of the Unbundler), each closed
# by its own Ground block - no need to physically route them back to the source's own Ground.
setblock ~4 ~-1 ~-1 circuitsimcraft:ground
setblock ~5 ~-1 ~0 circuitsimcraft:ground
setblock ~4 ~-1 ~1 circuitsimcraft:ground

# Lever on top of the source, left off - marks a clean t=0 once flipped.
setblock ~1 ~1 ~0 minecraft:lever[face=floor,facing=north,powered=false]

give @s circuitsimcraft:three_phase_probe 1
