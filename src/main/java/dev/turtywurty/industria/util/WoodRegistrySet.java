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
import net.minecraft.block.*;
import net.minecraft.block.dispenser.BoatDispenserBehavior;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.ChestBoatEntity;
import net.minecraft.item.BoatItem;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.Item;
import net.minecraft.item.SignItem;
import net.minecraft.registry.tag.TagKey;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class WoodRegistrySet {
    private static final List<WoodRegistrySet> WOOD_SETS = new ArrayList<>();
    public final WoodType woodType;
    public final BlockSetType blockSetType;
    public final PillarBlock log, strippedLog;
    public final PillarBlock strippedWood, wood;
    public final Block planks;
    public final Block leaves;
    public final SaplingBlock sapling;
    public final StairsBlock stairs;
    public final SlabBlock slab;
    public final FenceBlock fence;
    public final FenceGateBlock fenceGate;
    public final DoorBlock door;
    public final TrapdoorBlock trapdoor;
    public final PressurePlateBlock pressurePlate;
    public final ButtonBlock button;
    public final SignBlock sign;
    public final WallSignBlock wallSign;
    public final HangingSignBlock hangingSign;
    public final WallHangingSignBlock wallHangingSign;
    public final SignItem signItem;
    public final HangingSignItem hangingSignItem;
    public final EntityType<BoatEntity> boatEntityType;
    public final EntityType<ChestBoatEntity> chestBoatEntityType;
    public final Item boatItem;
    public final Item chestBoatItem;
    public final TagKey<Item> logsItemTag;
    public final TagKey<Block> logsBlockTag;
    private final String name;
    private final SaplingGenerator saplingGenerator;

    public WoodRegistrySet(String name, SaplingGenerator saplingGenerator, Supplier<WoodType> woodType,
                           Function<AbstractBlock.Settings, Block> planks,
                           Function<AbstractBlock.Settings, PillarBlock> log, Function<AbstractBlock.Settings, PillarBlock> strippedLog,
                           Function<AbstractBlock.Settings, PillarBlock> strippedWood, Function<AbstractBlock.Settings, PillarBlock> wood,
                           Function<AbstractBlock.Settings, Block> leaves, Function<AbstractBlock.Settings, SaplingBlock> sapling,
                           Function<AbstractBlock.Settings, StairsBlock> stairs, Function<AbstractBlock.Settings, SlabBlock> slab,
                           Function<AbstractBlock.Settings, FenceBlock> fence, Function<AbstractBlock.Settings, FenceGateBlock> fenceGate,
                           Function<AbstractBlock.Settings, DoorBlock> door, Function<AbstractBlock.Settings, TrapdoorBlock> trapdoor,
                           Function<AbstractBlock.Settings, PressurePlateBlock> pressurePlate, Function<AbstractBlock.Settings, ButtonBlock> button,
                           Function<AbstractBlock.Settings, SignBlock> sign, Function<AbstractBlock.Settings, WallSignBlock> wallSign,
                           Function<AbstractBlock.Settings, HangingSignBlock> hangingSign, Function<AbstractBlock.Settings, WallHangingSignBlock> wallHangingSign,
                           Function<Item.Settings, SignItem> signItem, Function<Item.Settings, HangingSignItem> hangingSignItem,
                           Function<Supplier<Item>, EntityType.Builder<BoatEntity>> boatEntityType, Function<Supplier<Item>, EntityType.Builder<ChestBoatEntity>> chestBoatEntityType,
                           Function<Item.Settings, Item> boatItem, Function<Item.Settings, Item> chestBoatItem) {
        this.name = name;
        this.saplingGenerator = saplingGenerator;
        this.blockSetType = new BlockSetType(Industria.id(this.name).toString());
        this.woodType = WoodTypeBuilder.copyOf(woodType.get()).register(Industria.id(this.name), this.blockSetType);

        this.planks = BlockInit.registerWithItemCopy(this.name + "_planks",
                planks == null ? Block::new : planks, Blocks.OAK_PLANKS);
        this.log = BlockInit.registerWithItemCopy(this.name + "_log",
                log == null ? PillarBlock::new : log, Blocks.OAK_LOG);
        this.strippedLog = BlockInit.registerWithItemCopy(this.name + "_stripped_log",
                strippedLog == null ? PillarBlock::new : strippedLog, Blocks.STRIPPED_OAK_LOG);
        this.strippedWood = BlockInit.registerWithItemCopy(this.name + "_stripped_wood",
                strippedWood == null ? PillarBlock::new : strippedWood, Blocks.STRIPPED_OAK_WOOD);
        this.wood = BlockInit.registerWithItemCopy(this.name + "_wood",
                wood == null ? PillarBlock::new : wood, Blocks.OAK_WOOD);
        this.leaves = BlockInit.registerWithItemCopy(this.name + "_leaves",
                leaves == null ? settings -> new TintedParticleLeavesBlock(0.01F, settings) : leaves, Blocks.OAK_LEAVES);
        this.sapling = BlockInit.registerWithItemCopy(this.name + "_sapling",
                sapling == null ? settings -> new SaplingBlock(this.saplingGenerator, settings) : sapling, Blocks.OAK_SAPLING);
        this.stairs = BlockInit.registerWithItemCopy(this.name + "_stairs",
                stairs == null ? settings -> new StairsBlock(this.planks.getDefaultState(), settings) : stairs, Blocks.OAK_STAIRS);
        this.slab = BlockInit.registerWithItemCopy(this.name + "_slab",
                slab == null ? SlabBlock::new : slab, Blocks.OAK_SLAB);
        this.fence = BlockInit.registerWithItemCopy(this.name + "_fence",
                fence == null ? FenceBlock::new : fence, Blocks.OAK_FENCE);
        this.fenceGate = BlockInit.registerWithItemCopy(this.name + "_fence_gate",
                fenceGate == null ? settings -> new FenceGateBlock(this.woodType, settings) : fenceGate, Blocks.OAK_FENCE_GATE);
        this.door = BlockInit.registerWithItemCopy(this.name + "_door",
                door == null ? settings -> new DoorBlock(this.blockSetType, settings) : door, Blocks.OAK_DOOR);
        this.trapdoor = BlockInit.registerWithItemCopy(this.name + "_trapdoor",
                trapdoor == null ? settings -> new TrapdoorBlock(this.blockSetType, settings) : trapdoor, Blocks.OAK_TRAPDOOR);
        this.pressurePlate = BlockInit.registerWithItemCopy(this.name + "_pressure_plate",
                pressurePlate == null ? settings -> new PressurePlateBlock(this.blockSetType, settings) : pressurePlate, Blocks.OAK_PRESSURE_PLATE);
        this.button = BlockInit.registerWithItemCopy(this.name + "_button",
                button == null ? settings -> new ButtonBlock(this.blockSetType, 30, settings) : button, Blocks.OAK_BUTTON);

        this.sign = BlockInit.registerWithCopy(this.name + "_sign",
                sign == null ? settings -> new SignBlock(this.woodType, settings) : sign, Blocks.OAK_SIGN);
        this.wallSign = BlockInit.registerWithCopy(this.name + "_wall_sign",
                wallSign == null ? settings -> new WallSignBlock(this.woodType, settings) : wallSign, Blocks.OAK_WALL_SIGN);
        this.hangingSign = BlockInit.registerWithCopy(this.name + "_hanging_sign",
                hangingSign == null ? settings -> new HangingSignBlock(this.woodType, settings) : hangingSign, Blocks.OAK_HANGING_SIGN);
        this.wallHangingSign = BlockInit.registerWithCopy(this.name + "_wall_hanging_sign",
                wallHangingSign == null ? settings -> new WallHangingSignBlock(this.woodType, settings) : wallHangingSign, Blocks.OAK_WALL_HANGING_SIGN);
        this.signItem = ItemInit.register(this.name + "_sign",
                signItem == null ? settings -> new SignItem(this.sign, this.wallSign, settings.maxCount(16)) : signItem);
        this.hangingSignItem = ItemInit.register(this.name + "_hanging_sign",
                hangingSignItem == null ? settings -> new HangingSignItem(this.hangingSign, this.wallHangingSign, settings.maxCount(16)) : hangingSignItem);

        this.boatEntityType = EntityTypeInit.register(this.name + "_boat",
                boatEntityType == null ? EntityType.Builder.<BoatEntity>create((type, world) -> new BoatEntity(type, world, this.planks::asItem), SpawnGroup.MISC)
                        .dropsNothing()
                        .dimensions(1.375F, 0.5625F)
                        .eyeHeight(0.5625F)
                        .maxTrackingRange(10) : boatEntityType.apply(this.planks::asItem));
        this.chestBoatEntityType = EntityTypeInit.register(this.name + "_chest_boat",
                chestBoatEntityType == null ? EntityType.Builder.<ChestBoatEntity>create((type, world) -> new ChestBoatEntity(type, world, this.planks::asItem), SpawnGroup.MISC)
                        .dropsNothing()
                        .dimensions(1.375F, 0.5625F)
                        .eyeHeight(0.5625F)
                        .maxTrackingRange(10) : chestBoatEntityType.apply(this.planks::asItem));
        this.boatItem = ItemInit.register(this.name + "_boat",
                boatItem == null ? settings -> new BoatItem(this.boatEntityType, settings.maxCount(1)) : boatItem);
        this.chestBoatItem = ItemInit.register(this.name + "_chest_boat",
                chestBoatItem == null ? settings -> new BoatItem(this.chestBoatEntityType, settings.maxCount(1)) : chestBoatItem);

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

        DispenserBlock.registerBehavior(this.boatItem, new BoatDispenserBehavior(this.boatEntityType));
        DispenserBlock.registerBehavior(this.chestBoatItem, new BoatDispenserBehavior(this.chestBoatEntityType));

        addBlocksToBlockEntityType(BlockEntityType.SIGN, this.sign, this.wallSign);
        addBlocksToBlockEntityType(BlockEntityType.HANGING_SIGN, this.hangingSign, this.wallHangingSign);
    }

    static void addBlocksToBlockEntityType(BlockEntityType<?> blockEntityType, Block... blocks) {
        addBlocksToBlockEntityType(blockEntityType, Arrays.asList(blocks));
    }

    static void addBlocksToBlockEntityType(BlockEntityType<?> blockEntityType, Collection<Block> blocks) {
        BlockEntityTypeAccessor accessor = (BlockEntityTypeAccessor) blockEntityType;
        Set<Block> originalBlocks = accessor.getBlocks();
        accessor.setBlocks(ImmutableSet.<Block>builderWithExpectedSize(originalBlocks.size() + blocks.size())
                .addAll(originalBlocks)
                .addAll(blocks)
                .build());
    }

    public static List<WoodRegistrySet> getWoodSets() {
        return WOOD_SETS;
    }

    public BlockFamily createBlockFamily() {
        return new BlockFamily.Builder(this.planks)
                .button(this.button)
                .fence(this.fence)
                .fenceGate(this.fenceGate)
                .pressurePlate(this.pressurePlate)
                .sign(this.sign, this.wallSign)
                .slab(this.slab)
                .stairs(this.stairs)
                .door(this.door)
                .trapdoor(this.trapdoor)
                .group("wooden")
                .unlockCriterionName("has_planks")
                .build();
    }

    public String getName() {
        return this.name;
    }

    public SaplingGenerator getSaplingGenerator() {
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
        private final SaplingGenerator saplingGenerator;

        private Supplier<WoodType> woodType = () -> WoodType.OAK;

        private Function<AbstractBlock.Settings, Block> planks;
        private Function<AbstractBlock.Settings, PillarBlock> log, strippedLog;
        private Function<AbstractBlock.Settings, PillarBlock> strippedWood, wood;
        private Function<AbstractBlock.Settings, Block> leaves;
        private Function<AbstractBlock.Settings, SaplingBlock> sapling;
        private Function<AbstractBlock.Settings, StairsBlock> stairs;
        private Function<AbstractBlock.Settings, SlabBlock> slab;
        private Function<AbstractBlock.Settings, FenceBlock> fence;
        private Function<AbstractBlock.Settings, FenceGateBlock> fenceGate;
        private Function<AbstractBlock.Settings, DoorBlock> door;
        private Function<AbstractBlock.Settings, TrapdoorBlock> trapdoor;
        private Function<AbstractBlock.Settings, PressurePlateBlock> pressurePlate;
        private Function<AbstractBlock.Settings, ButtonBlock> button;
        private Function<AbstractBlock.Settings, SignBlock> sign;
        private Function<AbstractBlock.Settings, WallSignBlock> wallSign;
        private Function<AbstractBlock.Settings, HangingSignBlock> hangingSign;
        private Function<AbstractBlock.Settings, WallHangingSignBlock> wallHangingSign;
        private Function<Item.Settings, SignItem> signItem;
        private Function<Item.Settings, HangingSignItem> hangingSignItem;
        private Function<Supplier<Item>, EntityType.Builder<BoatEntity>> boatType;
        private Function<Supplier<Item>, EntityType.Builder<ChestBoatEntity>> chestBoatType;
        private Function<Item.Settings, Item> boatItem;
        private Function<Item.Settings, Item> chestBoatItem;

        public Builder(String name, SaplingGenerator saplingGenerator) {
            this.name = name;
            this.saplingGenerator = saplingGenerator;
        }

        public Builder woodType(Supplier<WoodType> woodType) {
            this.woodType = woodType;
            return this;
        }

        public Builder planks(Function<AbstractBlock.Settings, Block> planks) {
            this.planks = planks;
            return this;
        }

        public Builder log(Function<AbstractBlock.Settings, PillarBlock> log, Function<AbstractBlock.Settings, PillarBlock> strippedLog) {
            this.log = log;
            this.strippedLog = strippedLog;
            return this;
        }

        public Builder log(Function<AbstractBlock.Settings, PillarBlock> log) {
            this.log = log;
            return this;
        }

        public Builder strippedLog(Function<AbstractBlock.Settings, PillarBlock> strippedLog) {
            this.strippedLog = strippedLog;
            return this;
        }

        public Builder wood(Function<AbstractBlock.Settings, PillarBlock> wood, Function<AbstractBlock.Settings, PillarBlock> strippedWood) {
            this.wood = wood;
            this.strippedWood = strippedWood;
            return this;
        }

        public Builder strippedWood(Function<AbstractBlock.Settings, PillarBlock> strippedWood) {
            this.strippedWood = strippedWood;
            return this;
        }

        public Builder wood(Function<AbstractBlock.Settings, PillarBlock> wood) {
            this.wood = wood;
            return this;
        }

        public Builder leaves(Function<AbstractBlock.Settings, Block> leaves) {
            this.leaves = leaves;
            return this;
        }

        public Builder sapling(Function<AbstractBlock.Settings, SaplingBlock> sapling) {
            this.sapling = sapling;
            return this;
        }

        public Builder stairs(Function<AbstractBlock.Settings, StairsBlock> stairs) {
            this.stairs = stairs;
            return this;
        }

        public Builder slab(Function<AbstractBlock.Settings, SlabBlock> slab) {
            this.slab = slab;
            return this;
        }

        public Builder fence(Function<AbstractBlock.Settings, FenceBlock> fence) {
            this.fence = fence;
            return this;
        }

        public Builder fenceGate(Function<AbstractBlock.Settings, FenceGateBlock> fenceGate) {
            this.fenceGate = fenceGate;
            return this;
        }

        public Builder door(Function<AbstractBlock.Settings, DoorBlock> door) {
            this.door = door;
            return this;
        }

        public Builder trapdoor(Function<AbstractBlock.Settings, TrapdoorBlock> trapdoor) {
            this.trapdoor = trapdoor;
            return this;
        }

        public Builder pressurePlate(Function<AbstractBlock.Settings, PressurePlateBlock> pressurePlate) {
            this.pressurePlate = pressurePlate;
            return this;
        }

        public Builder button(Function<AbstractBlock.Settings, ButtonBlock> button) {
            this.button = button;
            return this;
        }

        public Builder sign(Function<AbstractBlock.Settings, SignBlock> sign, Function<AbstractBlock.Settings, WallSignBlock> wallSign,
                            Function<AbstractBlock.Settings, HangingSignBlock> hangingSign, Function<AbstractBlock.Settings, WallHangingSignBlock> wallHangingSign) {
            this.sign = sign;
            this.wallSign = wallSign;
            this.hangingSign = hangingSign;
            this.wallHangingSign = wallHangingSign;
            return this;
        }

        public Builder sign(Function<AbstractBlock.Settings, SignBlock> sign, Function<AbstractBlock.Settings, WallSignBlock> wallSign) {
            this.sign = sign;
            this.wallSign = wallSign;
            return this;
        }

        public Builder sign(Function<AbstractBlock.Settings, SignBlock> sign) {
            this.sign = sign;
            return this;
        }

        public Builder wallSign(Function<AbstractBlock.Settings, WallSignBlock> wallSign) {
            this.wallSign = wallSign;
            return this;
        }

        public Builder hangingSign(Function<AbstractBlock.Settings, HangingSignBlock> hangingSign) {
            this.hangingSign = hangingSign;
            return this;
        }

        public Builder wallHangingSign(Function<AbstractBlock.Settings, WallHangingSignBlock> wallHangingSign) {
            this.wallHangingSign = wallHangingSign;
            return this;
        }

        public Builder hangingSign(Function<AbstractBlock.Settings, HangingSignBlock> hangingSign, Function<AbstractBlock.Settings, WallHangingSignBlock> wallHangingSign) {
            this.hangingSign = hangingSign;
            this.wallHangingSign = wallHangingSign;
            return this;
        }

        public Builder signItem(Function<Item.Settings, SignItem> signItem) {
            this.signItem = signItem;
            return this;
        }

        public Builder hangingSignItem(Function<Item.Settings, HangingSignItem> hangingSignItem) {
            this.hangingSignItem = hangingSignItem;
            return this;
        }

        public Builder boatType(Function<Supplier<Item>, EntityType.Builder<BoatEntity>> boatType, Function<Supplier<Item>, EntityType.Builder<ChestBoatEntity>> chestBoatType) {
            this.boatType = boatType;
            this.chestBoatType = chestBoatType;
            return this;
        }

        public Builder boatType(Function<Supplier<Item>, EntityType.Builder<BoatEntity>> boatType) {
            this.boatType = boatType;
            return this;
        }

        public Builder chestBoatType(Function<Supplier<Item>, EntityType.Builder<ChestBoatEntity>> chestBoatType) {
            this.chestBoatType = chestBoatType;
            return this;
        }

        public Builder boatItem(Function<Item.Settings, Item> boatItem, Function<Item.Settings, Item> chestBoatItem) {
            this.boatItem = boatItem;
            this.chestBoatItem = chestBoatItem;
            return this;
        }

        public Builder boatItem(Function<Item.Settings, Item> boatItem) {
            this.boatItem = boatItem;
            return this;
        }

        public Builder chestBoatItem(Function<Item.Settings, Item> chestBoatItem) {
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
