# Industria Asset Checklist

This checklist treats all existing assets as if they do not exist. It is derived from the block and item registrations, including registrations created indirectly by the rubber wood and fluid helpers.

## Summary

- **125 registered blocks**
- **202 registered items**
- **111 3D item models**
- **91 flat 2D item models**

Here, a **3D item** means an item that displays block or custom geometry rather than a generated flat sprite.

## Blocks — 125

### Mineral and storage blocks — 41

These use simple cube or generated ore models:

- Aluminium: `bauxite_ore`, `deepslate_bauxite_ore`, `raw_bauxite_block`, `aluminium_block`
- Silver: `argentite_ore`, `deepslate_argentite_ore`, `raw_argentite_block`, `silver_block`
- Lead: `galena_ore`, `deepslate_galena_ore`, `raw_galena_block`, `lead_block`
- Titanium: `ilmenite_ore`, `deepslate_ilmenite_ore`, `raw_ilmenite_block`, `titanium_block`
- Zinc: `sphalerite_ore`, `deepslate_sphalerite_ore`, `raw_sphalerite_block`, `zinc_block`
- Cobalt: `cobaltite_ore`, `deepslate_cobaltite_ore`, `raw_cobaltite_block`, `cobalt_block`
- Nickel: `pentlandite_ore`, `deepslate_pentlandite_ore`, `raw_pentlandite_block`, `nickel_block`
- Iridium: `iridium_ore`, `deepslate_iridium_ore`, `iridium_block`
- Tin: `cassiterite_ore`, `deepslate_cassiterite_ore`, `raw_cassiterite_block`, `tin_block`
- Pyrite: `nether_pyrite_ore`, `end_pyrite_ore`, `pyrite_block`
- Steel: `steel_block`
- Quartz: `quartz_ore`, `deepslate_quartz_ore`

The stone/deepslate ore pairs can share one mineral overlay texture over vanilla stone or deepslate. Nether/end pyrite can similarly share a pyrite overlay.

### Static machines and infrastructure — 13

These need ordinary 3D block JSON models and textures:

- `alloy_furnace`
- `thermal_generator`
- `basic_battery`
- `advanced_battery`
- `elite_battery`
- `ultimate_battery`
- `creative_battery`
- `combustion_generator`
- `solar_panel`
- `drill_tube`
- `electric_furnace`
- `induction_heater`
- `fluid_pump`

### Complex or animated machines — 21

These should have custom 3D geometry/model textures, generally with a simple fallback block model as well:

- `crusher`
- `wind_turbine`
- `oil_pump_jack`
- `drill`
- `motor`
- `upgrade_station`
- `mixer`
- `digester`
- `clarifier`
- `crystallizer`
- `rotary_kiln_controller`
- `rotary_kiln`
- `electrolyzer`
- `fluid_tank`
- `wellhead`
- `shaking_table`
- `centrifugal_concentrator`
- `arc_furnace`
- `tree_tap`
- `agitator`
- `distillation_tower`

The drill needs separate visual components for its frame, motor, cable, and drill head.

Two apparent implementation gaps are worth noting:

- `wellhead` declares that it has a block-entity renderer, but no renderer is registered.
- The special 3D item setup for `agitator` is commented out.

Both still belong on the custom-3D asset list.

### Conveyors — 15

All should use custom 3D block models:

- `conveyor`
- `splitter_conveyor`
- `merger_conveyor`
- `alternator_conveyor`
- `feeder_conveyor`
- `hatch_conveyor`
- `side_injector_conveyor`
- `ladder_conveyor`
- `filter_conveyor`
- `magnetic_conveyor`
- `detector_conveyor`
- `drop_chute_conveyor`
- `count_conveyor`
- `delay_conveyor`
- `containment_conveyor`

The basic conveyor additionally needs straight, ascending, descending, left-turn, and right-turn variants. Ladder, feeder, side-injector, and containment conveyors also have moving or specially rendered components.

### Rubber wood set — 19

These use vanilla-style block models with a rubber wood texture family:

- `rubber_planks`
- `rubber_log`
- `rubber_stripped_log`
- `rubber_stripped_wood`
- `rubber_wood`
- `rubber_leaves`
- `rubber_sapling`
- `rubber_stairs`
- `rubber_slab`
- `rubber_fence`
- `rubber_fence_gate`
- `rubber_door`
- `rubber_trapdoor`
- `rubber_pressure_plate`
- `rubber_button`
- `rubber_sign`
- `rubber_wall_sign`
- `rubber_hanging_sign`
- `rubber_wall_hanging_sign`

This family also needs in-world sign/hanging-sign and boat/chest-boat entity textures.

### Fluid blocks — 9

Each needs still and flowing textures, normally animated, plus its fluid rendering/model definition:

- `crude_oil`
- `dirty_sodium_aluminate`
- `sodium_aluminate`
- `molten_aluminium`
- `molten_cryolite`
- `latex` — rendering expects the texture prefix `fluid_latex`
- `methanol`
- `formic_acid`
- `diluted_formic_acid`

## Item Models — 202

### Standard block items — 108

Every block above receives a standard block item except:

- `auto_multiblock`
- `auto_multiblock_io`
- `rotary_kiln_controller`
- `rotary_kiln`
- `crude_oil`
- `dirty_sodium_aluminate`
- `sodium_aluminate`
- `molten_aluminium`
- `molten_cryolite`
- `latex`
- `methanol`
- `formic_acid`
- `diluted_formic_acid`
- `rubber_sign`
- `rubber_wall_sign`
- `rubber_hanging_sign`
- `rubber_wall_hanging_sign`

Of the 108 standard block items:

- **106 should reuse their 3D block model.**
- `rubber_door` should use a **flat 2D item sprite**.
- `rubber_sapling` should use a **flat 2D item sprite**, normally reusing the sapling texture.

`rotary_kiln`, `rubber_sign`, and `rubber_hanging_sign` receive separately registered items below.

### Standalone 3D items — 5

- `seismic_scanner`
- `simple_drill_head`
- `block_builder_drill_head`
- `rotary_kiln`
- `mob_jar`

The two drill heads can potentially share base geometry while changing their appearance or attachments.

### Standalone 2D material items — 69

- Aluminium: `bauxite`, `crushed_bauxite`, `sodium_aluminate`, `aluminium_hydroxide`, `alumina`, `aluminium_ingot`, `aluminium_nugget`, `aluminium_plate`
- Silver: `argentite`, `crushed_argentite`, `argentite_concentrate`, `lead_bullion`, `dore_silver`, `silver_ingot`, `silver_nugget`
- Lead: `galena`, `crushed_galena`, `galena_concentrate`, `tetragonal_litharge`, `lead_ingot`, `lead_nugget`
- Titanium: `ilmenite`, `crushed_ilmenite`, `ilmenite_concentrate`, `titanium_tetrachloride`, `titanium_ingot`, `titanium_nugget`, `titanium_plate`
- Zinc: `sphalerite`, `crushed_sphalerite`, `sphalerite_concentrate`, `zinc_calcine`, `zinc_ingot`, `zinc_nugget`
- Cobalt: `cobaltite`, `crushed_cobaltite`, `cobalt_ingot`, `cobalt_nugget`
- Lithium: `crushed_spodumene`, `spodumene_concentrate`, `lithium_carbonate`, `lithium_ingot`, `lithium_nugget`
- Nickel: `pentlandite`, `crushed_pentlandite`, `pentlandite_concentrate`, `nickel_ingot`, `nickel_nugget`
- Iridium: `iridium_ingot`, `iridium_nugget`
- Silicon: `crushed_quartz`, `silicon_rod`, `silicon_ingot`, `silicon_pellet`
- Tin: `cassiterite`, `crushed_cassiterite`, `cassiterite_concentrate`, `tin_ingot`, `tin_nugget`
- Rubber: `coagulated_latex`, `raw_rubber`, `rubber`
- Sulfur: `pyrite`, `crushed_sulfur`, `sulfur`
- Steel: `steel_ingot`, `steel_nugget`
- Sodium: `sodium_hydroxide`, `sodium_carbonate`

### Other standalone 2D items — 7

- `red_mud`
- `cryolite`
- `carbon_rod`
- `wrench`
- `multiblock_exporter`
- `filled_mob_jar`
- `bottle_formic_acid`

The current item-generation code treats `filled_mob_jar` as 2D. Artistically, it may be better for it to reuse the empty jar's 3D model, potentially with the captured entity or contents rendered inside.

### Fluid bucket sprites — 9, all 2D

- `crude_oil_bucket`
- `dirty_sodium_aluminate_bucket`
- `sodium_aluminate_bucket`
- `molten_aluminium_bucket`
- `molten_cryolite_bucket`
- `latex_bucket`
- `methanol_bucket`
- `formic_acid_bucket`
- `diluted_formic_acid_bucket`

### Rubber utility and vehicle items — 4, all 2D

- `rubber_sign`
- `rubber_hanging_sign`
- `rubber_boat`
- `rubber_chest_boat`

## Source Locations

The registrations and rendering conventions used for this checklist are primarily defined in:

- `src/main/java/dev/turtywurty/industria/init/BlockInit.java`
- `src/main/java/dev/turtywurty/industria/init/ItemInit.java`
- `src/main/java/dev/turtywurty/industria/init/FluidInit.java`
- `src/main/java/dev/turtywurty/industria/util/WoodRegistrySet.java`
- `src/client/java/dev/turtywurty/industria/datagen/IndustriaModelProvider.java`
