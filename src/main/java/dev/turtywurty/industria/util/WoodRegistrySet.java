package dev.turtywurty.industria.util;

import com.google.common.collect.ImmutableSet;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.EntityTypeInit;
import dev.turtywurty.industria.init.ItemInit;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.industria.mixin.BlockEntityTypeAccessor;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.data.BlockFamily;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class WoodRegistrySet {
    private static final List<WoodRegistrySet> WOOD_SETS = new ArrayList<>();

    private final String name;
    private final TreeGrower saplingGenerator;

    public final WoodType woodType;
    public final BlockSetType blockSetType;
    public final RotatedPillarBlock log, strippedLog;
    public final RotatedPillarBlock strippedWood, wood;
    public final Block planks;
    public final Block leaves;
    public final SaplingBlock sapling;
    public final StairBlock stairs;
    public final SlabBlock slab;
    public final FenceBlock fence;
    public final FenceGateBlock fenceGate;
    public final DoorBlock door;
    public final TrapDoorBlock trapdoor;
    public final PressurePlateBlock pressurePlate;
    public final ButtonBlock button;

    public final StandingSignBlock sign;
    public final WallSignBlock wallSign;
    public final CeilingHangingSignBlock hangingSign;
    public final WallHangingSignBlock wallHangingSign;
    public final SignItem signItem;
    public final HangingSignItem hangingSignItem;

    public final EntityType<Boat> boatEntityType;
    public final EntityType<ChestBoat> chestBoatEntityType;
    public final Item boatItem;
    public final Item chestBoatItem;

    public final TagKey<Item> logsItemTag;
    public final TagKey<Block> logsBlockTag;

    public WoodRegistrySet(String name, TreeGrower saplingGenerator, Supplier<WoodType> woodType,
                           Function<BlockBehaviour.Properties, Block> planks,
                           Function<BlockBehaviour.Properties, RotatedPillarBlock> log, Function<BlockBehaviour.Properties, RotatedPillarBlock> strippedLog,
                           Function<BlockBehaviour.Properties, RotatedPillarBlock> strippedWood, Function<BlockBehaviour.Properties, RotatedPillarBlock> wood,
                           Function<BlockBehaviour.Properties, Block> leaves, Function<BlockBehaviour.Properties, SaplingBlock> sapling,
                           Function<BlockBehaviour.Properties, StairBlock> stairs, Function<BlockBehaviour.Properties, SlabBlock> slab,
                           Function<BlockBehaviour.Properties, FenceBlock> fence, Function<BlockBehaviour.Properties, FenceGateBlock> fenceGate,
                           Function<BlockBehaviour.Properties, DoorBlock> door, Function<BlockBehaviour.Properties, TrapDoorBlock> trapdoor,
                           Function<BlockBehaviour.Properties, PressurePlateBlock> pressurePlate, Function<BlockBehaviour.Properties, ButtonBlock> button,
                           Function<BlockBehaviour.Properties, StandingSignBlock> sign, Function<BlockBehaviour.Properties, WallSignBlock> wallSign,
                           Function<BlockBehaviour.Properties, CeilingHangingSignBlock> hangingSign, Function<BlockBehaviour.Properties, WallHangingSignBlock> wallHangingSign,
                           Function<net.minecraft.world.item.Item.Properties, SignItem> signItem, Function<net.minecraft.world.item.Item.Properties, HangingSignItem> hangingSignItem,
                           Function<Supplier<Item>, net.minecraft.world.entity.EntityType.Builder> boatEntityType, Function<Supplier<Item>, net.minecraft.world.entity.EntityType.Builder> chestBoatEntityType,
                           Function<net.minecraft.world.item.Item.Properties, Item> boatItem, Function<net.minecraft.world.item.Item.Properties, Item> chestBoatItem) {
        this.name = name;
        this.saplingGenerator = saplingGenerator;
        this.blockSetType = new BlockSetType(Industria.id(this.name).toString());
        this.woodType = WoodTypeBuilder.copyOf(woodType.get()).register(Industria.id(this.name), this.blockSetType);

        this.planks = BlockInit.registerWithItemCopy(this.name + "_planks",
                planks == null ? Block::new : planks, Blocks.OAK_PLANKS);
        this.log = BlockInit.registerWithItemCopy(this.name + "_log",
                log == null ? RotatedPillarBlock::new : log, Blocks.OAK_LOG);
        this.strippedLog = BlockInit.registerWithItemCopy(this.name + "_stripped_log",
                strippedLog == null ? RotatedPillarBlock::new : strippedLog, Blocks.STRIPPED_OAK_LOG);
        this.strippedWood = BlockInit.registerWithItemCopy(this.name + "_stripped_wood",
                strippedWood == null ? RotatedPillarBlock::new : strippedWood, Blocks.STRIPPED_OAK_WOOD);
        this.wood = BlockInit.registerWithItemCopy(this.name + "_wood",
                wood == null ? RotatedPillarBlock::new : wood, Blocks.OAK_WOOD);
        this.leaves = BlockInit.registerWithItemCopy(this.name + "_leaves",
                leaves == null ? settings -> new TintedParticleLeavesBlock(0.01F, settings) : leaves, Blocks.OAK_LEAVES);
        this.sapling = BlockInit.registerWithItemCopy(this.name + "_sapling",
                sapling == null ? settings -> new SaplingBlock(this.saplingGenerator, settings) : sapling, Blocks.OAK_SAPLING);
        this.stairs = BlockInit.registerWithItemCopy(this.name + "_stairs",
                stairs == null ? settings -> new StairBlock(this.planks.defaultBlockState(), settings) : stairs, Blocks.OAK_STAIRS);
        this.slab = BlockInit.registerWithItemCopy(this.name + "_slab",
                slab == null ? SlabBlock::new : slab, Blocks.OAK_SLAB);
        this.fence = BlockInit.registerWithItemCopy(this.name + "_fence",
                fence == null ? FenceBlock::new : fence, Blocks.OAK_FENCE);
        this.fenceGate = BlockInit.registerWithItemCopy(this.name + "_fence_gate",
                fenceGate == null ? settings -> new FenceGateBlock(this.woodType, settings) : fenceGate, Blocks.OAK_FENCE_GATE);
        this.door = BlockInit.registerWithItemCopy(this.name + "_door",
                door == null ? settings -> new DoorBlock(this.blockSetType, settings) : door, Blocks.OAK_DOOR);
        this.trapdoor = BlockInit.registerWithItemCopy(this.name + "_trapdoor",
                trapdoor == null ? settings -> new TrapDoorBlock(this.blockSetType, settings) : trapdoor, Blocks.OAK_TRAPDOOR);
        this.pressurePlate = BlockInit.registerWithItemCopy(this.name + "_pressure_plate",
                pressurePlate == null ? settings -> new PressurePlateBlock(this.blockSetType, settings) : pressurePlate, Blocks.OAK_PRESSURE_PLATE);
        this.button = BlockInit.registerWithItemCopy(this.name + "_button",
                button == null ? settings -> new ButtonBlock(this.blockSetType, 30, settings) : button, Blocks.OAK_BUTTON);

        this.sign = BlockInit.registerWithCopy(this.name + "_sign",
                sign == null ? settings -> new StandingSignBlock(this.woodType, settings) : sign, Blocks.OAK_SIGN);
        this.wallSign = BlockInit.registerWithCopy(this.name + "_wall_sign",
                wallSign == null ? settings -> new WallSignBlock(this.woodType, settings) : wallSign, Blocks.OAK_WALL_SIGN);
        this.hangingSign = BlockInit.registerWithCopy(this.name + "_hanging_sign",
                hangingSign == null ? settings -> new CeilingHangingSignBlock(this.woodType, settings) : hangingSign, Blocks.OAK_HANGING_SIGN);
        this.wallHangingSign = BlockInit.registerWithCopy(this.name + "_wall_hanging_sign",
                wallHangingSign == null ? settings -> new WallHangingSignBlock(this.woodType, settings) : wallHangingSign, Blocks.OAK_WALL_HANGING_SIGN);
        this.signItem = ItemInit.register(this.name + "_sign",
                signItem == null ? settings -> new SignItem(this.sign, this.wallSign, settings.stacksTo(16)) : signItem);
        this.hangingSignItem = ItemInit.register(this.name + "_hanging_sign",
                hangingSignItem == null ? settings -> new HangingSignItem(this.hangingSign, this.wallHangingSign, settings.stacksTo(16)) : hangingSignItem);

        this.boatEntityType = EntityTypeInit.register(this.name + "_boat",
                boatEntityType == null ? net.minecraft.world.entity.EntityType.Builder.<Boat>of((type, world) -> new Boat(type, world, this.planks::asItem), MobCategory.MISC)
                        .noLootTable()
                        .sized(1.375F, 0.5625F)
                        .eyeHeight(0.5625F)
                        .clientTrackingRange(10) : boatEntityType.apply(this.planks::asItem));
        this.chestBoatEntityType = EntityTypeInit.register(this.name + "_chest_boat",
                chestBoatEntityType == null ? net.minecraft.world.entity.EntityType.Builder.<ChestBoat>of((type, world) -> new ChestBoat(type, world, this.planks::asItem), MobCategory.MISC)
                        .noLootTable()
                        .sized(1.375F, 0.5625F)
                        .eyeHeight(0.5625F)
                        .clientTrackingRange(10) : chestBoatEntityType.apply(this.planks::asItem));
        this.boatItem = ItemInit.register(this.name + "_boat",
                boatItem == null ? settings -> new BoatItem(this.boatEntityType, settings.stacksTo(1)) : boatItem);
        this.chestBoatItem = ItemInit.register(this.name + "_chest_boat",
                chestBoatItem == null ? settings -> new BoatItem(this.chestBoatEntityType, settings.stacksTo(1)) : chestBoatItem);

        StrippableBlockRegistry.register(this.log, this.strippedLog);
        StrippableBlockRegistry.register(this.wood, this.strippedWood);

        this.logsItemTag = TagList.Items.of(this.name + "_logs");
        this.logsBlockTag = TagList.Blocks.of(this.name + "_logs");

        FlammableBlockRegistry flammableBlockRegistry = FlammableBlockRegistry.getDefaultInstance();
        flammableBlockRegistry.add(this.planks, 5, 20);
        flammableBlockRegistry.add(this.log, 5, 5);
        flammableBlockRegistry.add(this.strippedLog, 5, 5);
        flammableBlockRegistry.add(this.strippedWood, 5, 5);
        flammableBlockRegistry.add(this.wood, 5, 5);
        flammableBlockRegistry.add(this.leaves, 30, 60);
        flammableBlockRegistry.add(this.sapling, 60, 20);
        flammableBlockRegistry.add(this.stairs, 5, 20);
        flammableBlockRegistry.add(this.slab, 5, 20);
        flammableBlockRegistry.add(this.fence, 5, 20);
        flammableBlockRegistry.add(this.fenceGate, 5, 20);
        flammableBlockRegistry.add(this.door, 5, 20);
        flammableBlockRegistry.add(this.trapdoor, 5, 20);
        flammableBlockRegistry.add(this.pressurePlate, 5, 20);
        flammableBlockRegistry.add(this.button, 5, 20);
        flammableBlockRegistry.add(this.sign, 5, 20);
        flammableBlockRegistry.add(this.wallSign, 5, 20);
        flammableBlockRegistry.add(this.hangingSign, 5, 20);
        flammableBlockRegistry.add(this.wallHangingSign, 5, 20);

        DispenserBlock.registerBehavior(this.boatItem, new BoatDispenseItemBehavior(this.boatEntityType));
        DispenserBlock.registerBehavior(this.chestBoatItem, new BoatDispenseItemBehavior(this.chestBoatEntityType));

        addBlocksToBlockEntityType(BlockEntityType.SIGN, this.sign, this.wallSign);
        addBlocksToBlockEntityType(BlockEntityType.HANGING_SIGN, this.hangingSign, this.wallHangingSign);
    }

    static void addBlocksToBlockEntityType(BlockEntityType<?> blockEntityType, Block... blocks) {
        addBlocksToBlockEntityType(blockEntityType, Arrays.asList(blocks));
    }

    static void addBlocksToBlockEntityType(BlockEntityType<?> blockEntityType, Collection<Block> blocks) {
        BlockEntityTypeAccessor accessor = (BlockEntityTypeAccessor) blockEntityType;
        Set<Block> originalBlocks = accessor.getValidBlocks();
        accessor.setValidBlocks(ImmutableSet.<Block>builderWithExpectedSize(originalBlocks.size() + blocks.size())
                .addAll(originalBlocks)
                .addAll(blocks)
                .build());
    }

    public BlockFamily createBlockFamily() {
        return new net.minecraft.data.BlockFamily.Builder(this.planks)
                .button(this.button)
                .fence(this.fence)
                .fenceGate(this.fenceGate)
                .pressurePlate(this.pressurePlate)
                .sign(this.sign, this.wallSign)
                .slab(this.slab)
                .stairs(this.stairs)
                .door(this.door)
                .trapdoor(this.trapdoor)
                .recipeGroupPrefix("wooden")
                .recipeUnlockedBy("has_planks")
                .getFamily();
    }

    public static List<WoodRegistrySet> getWoodSets() {
        return WOOD_SETS;
    }

    public String getName() {
        return this.name;
    }

    public TreeGrower getSaplingGenerator() {
        return this.saplingGenerator;
    }

    @Override
    public String toString() {
        return "WoodRegistrySet[name=" + name + ", saplingGenerator=" + this.saplingGenerator + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        var that = (WoodRegistrySet) obj;
        return name.equals(that.name) && saplingGenerator.equals(that.saplingGenerator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, saplingGenerator);
    }

    public static class Builder {
        private final String name;
        private final TreeGrower saplingGenerator;

        private Supplier<WoodType> woodType = () -> WoodType.OAK;

        private Function<BlockBehaviour.Properties, Block> planks;
        private Function<BlockBehaviour.Properties, RotatedPillarBlock> log, strippedLog;
        private Function<BlockBehaviour.Properties, RotatedPillarBlock> strippedWood, wood;
        private Function<BlockBehaviour.Properties, Block> leaves;
        private Function<BlockBehaviour.Properties, SaplingBlock> sapling;
        private Function<BlockBehaviour.Properties, StairBlock> stairs;
        private Function<BlockBehaviour.Properties, SlabBlock> slab;
        private Function<BlockBehaviour.Properties, FenceBlock> fence;
        private Function<BlockBehaviour.Properties, FenceGateBlock> fenceGate;
        private Function<BlockBehaviour.Properties, DoorBlock> door;
        private Function<BlockBehaviour.Properties, TrapDoorBlock> trapdoor;
        private Function<BlockBehaviour.Properties, PressurePlateBlock> pressurePlate;
        private Function<BlockBehaviour.Properties, ButtonBlock> button;
        private Function<BlockBehaviour.Properties, StandingSignBlock> sign;
        private Function<BlockBehaviour.Properties, WallSignBlock> wallSign;
        private Function<BlockBehaviour.Properties, CeilingHangingSignBlock> hangingSign;
        private Function<BlockBehaviour.Properties, WallHangingSignBlock> wallHangingSign;
        private Function<net.minecraft.world.item.Item.Properties, SignItem> signItem;
        private Function<net.minecraft.world.item.Item.Properties, HangingSignItem> hangingSignItem;
        private Function<Supplier<Item>, net.minecraft.world.entity.EntityType.Builder> boatType;
        private Function<Supplier<Item>, net.minecraft.world.entity.EntityType.Builder> chestBoatType;
        private Function<net.minecraft.world.item.Item.Properties, Item> boatItem;
        private Function<net.minecraft.world.item.Item.Properties, Item> chestBoatItem;

        public Builder(String name, TreeGrower saplingGenerator) {
            this.name = name;
            this.saplingGenerator = saplingGenerator;
        }

        public Builder woodType(Supplier<WoodType> woodType) {
            this.woodType = woodType;
            return this;
        }

        public Builder planks(Function<BlockBehaviour.Properties, Block> planks) {
            this.planks = planks;
            return this;
        }

        public Builder log(Function<BlockBehaviour.Properties, RotatedPillarBlock> log, Function<BlockBehaviour.Properties, RotatedPillarBlock> strippedLog) {
            this.log = log;
            this.strippedLog = strippedLog;
            return this;
        }

        public Builder log(Function<BlockBehaviour.Properties, RotatedPillarBlock> log) {
            this.log = log;
            return this;
        }

        public Builder strippedLog(Function<BlockBehaviour.Properties, RotatedPillarBlock> strippedLog) {
            this.strippedLog = strippedLog;
            return this;
        }

        public Builder wood(Function<BlockBehaviour.Properties, RotatedPillarBlock> wood, Function<BlockBehaviour.Properties, RotatedPillarBlock> strippedWood) {
            this.wood = wood;
            this.strippedWood = strippedWood;
            return this;
        }

        public Builder strippedWood(Function<BlockBehaviour.Properties, RotatedPillarBlock> strippedWood) {
            this.strippedWood = strippedWood;
            return this;
        }

        public Builder wood(Function<BlockBehaviour.Properties, RotatedPillarBlock> wood) {
            this.wood = wood;
            return this;
        }

        public Builder leaves(Function<BlockBehaviour.Properties, Block> leaves) {
            this.leaves = leaves;
            return this;
        }

        public Builder sapling(Function<BlockBehaviour.Properties, SaplingBlock> sapling) {
            this.sapling = sapling;
            return this;
        }

        public Builder stairs(Function<BlockBehaviour.Properties, StairBlock> stairs) {
            this.stairs = stairs;
            return this;
        }

        public Builder slab(Function<BlockBehaviour.Properties, SlabBlock> slab) {
            this.slab = slab;
            return this;
        }

        public Builder fence(Function<BlockBehaviour.Properties, FenceBlock> fence) {
            this.fence = fence;
            return this;
        }

        public Builder fenceGate(Function<BlockBehaviour.Properties, FenceGateBlock> fenceGate) {
            this.fenceGate = fenceGate;
            return this;
        }

        public Builder door(Function<BlockBehaviour.Properties, DoorBlock> door) {
            this.door = door;
            return this;
        }

        public Builder trapdoor(Function<BlockBehaviour.Properties, TrapDoorBlock> trapdoor) {
            this.trapdoor = trapdoor;
            return this;
        }

        public Builder pressurePlate(Function<BlockBehaviour.Properties, PressurePlateBlock> pressurePlate) {
            this.pressurePlate = pressurePlate;
            return this;
        }

        public Builder button(Function<BlockBehaviour.Properties, ButtonBlock> button) {
            this.button = button;
            return this;
        }

        public Builder sign(Function<BlockBehaviour.Properties, StandingSignBlock> sign, Function<BlockBehaviour.Properties, WallSignBlock> wallSign,
                            Function<BlockBehaviour.Properties, CeilingHangingSignBlock> hangingSign, Function<BlockBehaviour.Properties, WallHangingSignBlock> wallHangingSign) {
            this.sign = sign;
            this.wallSign = wallSign;
            this.hangingSign = hangingSign;
            this.wallHangingSign = wallHangingSign;
            return this;
        }

        public Builder sign(Function<BlockBehaviour.Properties, StandingSignBlock> sign, Function<BlockBehaviour.Properties, WallSignBlock> wallSign) {
            this.sign = sign;
            this.wallSign = wallSign;
            return this;
        }

        public Builder sign(Function<BlockBehaviour.Properties, StandingSignBlock> sign) {
            this.sign = sign;
            return this;
        }

        public Builder wallSign(Function<BlockBehaviour.Properties, WallSignBlock> wallSign) {
            this.wallSign = wallSign;
            return this;
        }

        public Builder hangingSign(Function<BlockBehaviour.Properties, CeilingHangingSignBlock> hangingSign) {
            this.hangingSign = hangingSign;
            return this;
        }

        public Builder wallHangingSign(Function<BlockBehaviour.Properties, WallHangingSignBlock> wallHangingSign) {
            this.wallHangingSign = wallHangingSign;
            return this;
        }

        public Builder hangingSign(Function<BlockBehaviour.Properties, CeilingHangingSignBlock> hangingSign, Function<BlockBehaviour.Properties, WallHangingSignBlock> wallHangingSign) {
            this.hangingSign = hangingSign;
            this.wallHangingSign = wallHangingSign;
            return this;
        }

        public Builder signItem(Function<net.minecraft.world.item.Item.Properties, SignItem> signItem) {
            this.signItem = signItem;
            return this;
        }

        public Builder hangingSignItem(Function<net.minecraft.world.item.Item.Properties, HangingSignItem> hangingSignItem) {
            this.hangingSignItem = hangingSignItem;
            return this;
        }

        public Builder boatType(Function<Supplier<Item>, net.minecraft.world.entity.EntityType.Builder> boatType, Function<Supplier<Item>, net.minecraft.world.entity.EntityType.Builder> chestBoatType) {
            this.boatType = boatType;
            this.chestBoatType = chestBoatType;
            return this;
        }

        public Builder boatType(Function<Supplier<Item>, net.minecraft.world.entity.EntityType.Builder> boatType) {
            this.boatType = boatType;
            return this;
        }

        public Builder chestBoatType(Function<Supplier<Item>, net.minecraft.world.entity.EntityType.Builder> chestBoatType) {
            this.chestBoatType = chestBoatType;
            return this;
        }

        public Builder boatItem(Function<net.minecraft.world.item.Item.Properties, Item> boatItem, Function<net.minecraft.world.item.Item.Properties, Item> chestBoatItem) {
            this.boatItem = boatItem;
            this.chestBoatItem = chestBoatItem;
            return this;
        }

        public Builder boatItem(Function<net.minecraft.world.item.Item.Properties, Item> boatItem) {
            this.boatItem = boatItem;
            return this;
        }

        public Builder chestBoatItem(Function<net.minecraft.world.item.Item.Properties, Item> chestBoatItem) {
            this.chestBoatItem = chestBoatItem;
            return this;
        }

        public WoodRegistrySet build() {
            var set = new WoodRegistrySet(name, saplingGenerator, woodType, planks, log, strippedLog, strippedWood, wood,
                    leaves, sapling, stairs, slab, fence, fenceGate, door, trapdoor,
                    pressurePlate, button, sign, wallSign, hangingSign, wallHangingSign, signItem, hangingSignItem,
                    boatType, chestBoatType, boatItem, chestBoatItem);
            WOOD_SETS.add(set);
            return set;
        }
    }
}
