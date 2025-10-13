package dev.turtywurty.industria.worldgen.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.worldgen.StructurePieceTypeInit;
import dev.turtywurty.industria.init.worldgen.StructureTypeInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.VineBlock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.math.*;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

public class FloatingOrbStructure extends Structure {
    public static final MapCodec<FloatingOrbStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Structure.configCodecBuilder(instance),
            BlockStateProvider.TYPE_CODEC.fieldOf("state1").forGetter(structure -> structure.state1),
            BlockStateProvider.TYPE_CODEC.fieldOf("state2").forGetter(structure -> structure.state2),
            IntProvider.VALUE_CODEC.fieldOf("radius").forGetter(structure -> structure.radius),
            HeightProvider.CODEC.fieldOf("y_offset").forGetter(structure -> structure.yOffset),
            FloatProvider.VALUE_CODEC.fieldOf("chance_of_second_block").forGetter(structure -> structure.chanceOfSecondBlock),
            FloatProvider.VALUE_CODEC.fieldOf("hollow_chance").forGetter(structure -> structure.hollowChance),
            FloatProvider.VALUE_CODEC.fieldOf("vine_chance").forGetter(structure -> structure.vineChance),
            IntProvider.VALUE_CODEC.fieldOf("vine_length").forGetter(structure -> structure.vineLength),
            BlockStateProvider.TYPE_CODEC.fieldOf("vine_block").forGetter(structure -> structure.vineState)
    ).apply(instance, FloatingOrbStructure::new));

    private final BlockStateProvider state1;
    private final BlockStateProvider state2;
    private final IntProvider radius;
    private final HeightProvider yOffset;
    private final FloatProvider chanceOfSecondBlock;
    private final FloatProvider hollowChance;
    private final FloatProvider vineChance;
    private final IntProvider vineLength;
    private final BlockStateProvider vineState;

    public FloatingOrbStructure(Structure.Config config, BlockStateProvider state1, BlockStateProvider state2,
                                IntProvider radius, HeightProvider yOffset, FloatProvider chanceOfSecondBlock,
                                FloatProvider hollowChance, FloatProvider vineChance, IntProvider vineLength,
                                BlockStateProvider vineState) {
        super(config);
        this.state1 = state1;
        this.state2 = state2;
        this.radius = radius;
        this.yOffset = yOffset;
        this.chanceOfSecondBlock = chanceOfSecondBlock;
        this.hollowChance = hollowChance;
        this.vineChance = vineChance;
        this.vineLength = vineLength;
        this.vineState = vineState;
    }

    @Override
    protected Optional<StructurePosition> getStructurePosition(Context context) {
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        ChunkPos chunkPos = context.chunkPos();
        HeightLimitView worldView = context.world();
        Random random = context.random();

        int sampledRadius = Math.max(1, this.radius.get(random));
        int vineLengthValue = Math.max(0, this.vineLength.get(random));

        int centerX = chunkPos.getStartX() + 8;
        int centerZ = chunkPos.getStartZ() + 8;

        int surfaceY = chunkGenerator.getHeightInGround(centerX, centerZ, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, worldView, context.noiseConfig());
        HeightContext heightContext = new HeightContext(chunkGenerator, worldView);
        int offset = this.yOffset.get(random, heightContext);

        int bottomY = worldView.getBottomY();
        int topY = worldView.getTopYInclusive() - 1;

        int minCenterY = bottomY + sampledRadius + vineLengthValue + 1;
        int maxCenterY = topY - sampledRadius - 1;
        if (minCenterY > maxCenterY)
            return Optional.empty();

        int desiredCenterY = surfaceY + offset + sampledRadius;
        int centerY = MathHelper.clamp(desiredCenterY, minCenterY, maxCenterY);
        if (centerY - sampledRadius <= surfaceY)
            return Optional.empty();

        float secondBlockChanceValue = MathHelper.clamp(this.chanceOfSecondBlock.get(random), 0.0F, 1.0F);
        float hollowChanceValue = MathHelper.clamp(this.hollowChance.get(random), 0.0F, 1.0F);
        float vineChanceValue = MathHelper.clamp(this.vineChance.get(random), 0.0F, 1.0F);

        boolean hollow = random.nextFloat() < hollowChanceValue;
        boolean hasVines = random.nextFloat() < vineChanceValue;

        BlockPos center = new BlockPos(centerX, centerY, centerZ);
        long seed = random.nextLong();

        return Optional.of(new Structure.StructurePosition(center, collector -> addPieces(
                collector,
                center,
                sampledRadius,
                hollow,
                secondBlockChanceValue,
                hasVines,
                vineLength,
                seed,
                vineChanceValue
        )));
    }

    private void addPieces(StructurePiecesCollector collector, BlockPos center, int radius, boolean hollow,
                           float secondBlockChance, boolean hasVines, IntProvider vineLength, long seed, float vineChance) {
        collector.addPiece(new Piece(center, radius, hollow, secondBlockChance, hasVines, vineLength, this.state1, this.state2, this.vineState, seed, vineChance));
    }

    @Override
    public StructureType<?> getType() {
        return StructureTypeInit.FLOATING_ORB;
    }

    public static class Piece extends StructurePiece {
        private static final String CENTER_X_KEY = "centerX";
        private static final String CENTER_Y_KEY = "centerY";
        private static final String CENTER_Z_KEY = "centerZ";
        private static final String RADIUS_KEY = "radius";
        private static final String HOLLOW_KEY = "hollow";
        private static final String SECOND_CHANCE_KEY = "secondChance";
        private static final String HAS_VINES_KEY = "hasVines";
        private static final String VINE_LENGTH_KEY = "vineLength";
        private static final String VINE_CHANCE_KEY = "vineChance";
        private static final String SEED_KEY = "seed";
        private static final String STATE1_KEY = "state1";
        private static final String STATE2_KEY = "state2";
        private static final String VINE_STATE_KEY = "vineState";

        private final BlockPos center;
        private final int radius;
        private final boolean hollow;
        private final float secondBlockChance;
        private final boolean hasVines;
        private final IntProvider vineLength;
        private final BlockStateProvider state1;
        private final BlockStateProvider state2;
        private final BlockStateProvider vineState;
        private final long seed;
        private final float vineChance;

        public Piece(BlockPos center,
                     int radius,
                     boolean hollow,
                     float secondBlockChance,
                     boolean hasVines,
                     IntProvider vineLength,
                     BlockStateProvider state1,
                     BlockStateProvider state2,
                     BlockStateProvider vineState,
                     long seed,
                     float vineChance) {
            super(StructurePieceTypeInit.FLOATING_ORB, 0, createBoundingBox(center, radius));
            this.center = center;
            this.radius = radius;
            this.hollow = hollow;
            this.secondBlockChance = secondBlockChance;
            this.hasVines = hasVines;
            this.vineLength = vineLength;
            this.state1 = state1;
            this.state2 = state2;
            this.vineState = vineState;
            this.seed = seed;
            this.vineChance = vineChance;
        }

        public Piece(NbtCompound nbt) {
            super(StructurePieceTypeInit.FLOATING_ORB, nbt);
            this.center = new BlockPos(
                    readIntOrThrow(nbt, CENTER_X_KEY),
                    readIntOrThrow(nbt, CENTER_Y_KEY),
                    readIntOrThrow(nbt, CENTER_Z_KEY)
            );
            this.radius = readIntOrThrow(nbt, RADIUS_KEY);
            this.hollow = readBooleanOrThrow(nbt, HOLLOW_KEY);
            this.secondBlockChance = readFloatOrThrow(nbt, SECOND_CHANCE_KEY);
            this.hasVines = readBooleanOrThrow(nbt, HAS_VINES_KEY);
            this.vineLength = IntProvider.NON_NEGATIVE_CODEC.decode(NbtOps.INSTANCE, nbt.get(VINE_LENGTH_KEY))
                    .resultOrPartial(Industria.LOGGER::error)
                    .orElseThrow(() -> new IllegalStateException("Failed to decode vine length provider for floating orb structure"))
                    .getFirst();
            this.seed = readLongOrThrow(nbt, SEED_KEY);
            this.state1 = decodeProvider(nbt.get(STATE1_KEY));
            this.state2 = decodeProvider(nbt.get(STATE2_KEY));
            this.vineState = decodeProvider(nbt.get(VINE_STATE_KEY));
            this.vineChance = nbt.contains(VINE_CHANCE_KEY)
                    ? readFloatOrThrow(nbt, VINE_CHANCE_KEY)
                    : (this.hasVines ? 1.0F : 0.0F);
        }

        private static BlockBox createBoundingBox(BlockPos center, int radius) {
            return new BlockBox(
                    center.getX() - radius,
                    center.getY() - radius,
                    center.getZ() - radius,
                    center.getX() + radius,
                    center.getY() + radius,
                    center.getZ() + radius
            );
        }

        private static BlockStateProvider decodeProvider(NbtElement element) {
            return BlockStateProvider.TYPE_CODEC.parse(NbtOps.INSTANCE, element)
                    .resultOrPartial(Industria.LOGGER::error)
                    .orElseThrow(() -> new IllegalStateException("Failed to decode block state provider for floating orb structure"));
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
            nbt.putInt(CENTER_X_KEY, this.center.getX());
            nbt.putInt(CENTER_Y_KEY, this.center.getY());
            nbt.putInt(CENTER_Z_KEY, this.center.getZ());
            nbt.putInt(RADIUS_KEY, this.radius);
            nbt.putBoolean(HOLLOW_KEY, this.hollow);
            nbt.putFloat(SECOND_CHANCE_KEY, this.secondBlockChance);
            nbt.putBoolean(HAS_VINES_KEY, this.hasVines);
            nbt.put(VINE_LENGTH_KEY, IntProvider.NON_NEGATIVE_CODEC.encodeStart(NbtOps.INSTANCE, this.vineLength)
                    .resultOrPartial(Industria.LOGGER::error)
                    .orElseThrow(() -> new IllegalStateException("Failed to encode vine length provider for floating orb structure")));
            nbt.putLong(SEED_KEY, this.seed);
            nbt.putFloat(VINE_CHANCE_KEY, this.vineChance);

            encodeProvider(nbt, STATE1_KEY, this.state1);
            encodeProvider(nbt, STATE2_KEY, this.state2);
            encodeProvider(nbt, VINE_STATE_KEY, this.vineState);
        }

        private static void encodeProvider(NbtCompound nbt, String key, BlockStateProvider provider) {
            BlockStateProvider.TYPE_CODEC.encodeStart(NbtOps.INSTANCE, provider)
                    .resultOrPartial(Industria.LOGGER::error)
                    .ifPresent(element -> nbt.put(key, element));
        }

        @Override
        public void generate(StructureWorldAccess world, net.minecraft.world.gen.StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            int radiusSq = this.radius * this.radius;
            int innerRadiusSq = Math.max(0, (this.radius - 1) * (this.radius - 1));

            var mutable = new BlockPos.Mutable();

            int minX = Math.max(chunkBox.getMinX(), this.center.getX() - this.radius);
            int maxX = Math.min(chunkBox.getMaxX(), this.center.getX() + this.radius);
            int minY = Math.max(chunkBox.getMinY(), this.center.getY() - this.radius);
            int maxY = Math.min(chunkBox.getMaxY(), this.center.getY() + this.radius);
            int minZ = Math.max(chunkBox.getMinZ(), this.center.getZ() - this.radius);
            int maxZ = Math.min(chunkBox.getMaxZ(), this.center.getZ() + this.radius);

            for (int x = minX; x <= maxX; x++) {
                int offsetX = x - this.center.getX();
                for (int y = minY; y <= maxY; y++) {
                    int offsetY = y - this.center.getY();
                    for (int z = minZ; z <= maxZ; z++) {
                        int offsetZ = z - this.center.getZ();
                        int distSq = offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ;
                        if (distSq > radiusSq)
                            continue;

                        if (this.hollow && distSq < innerRadiusSq)
                            continue;

                        mutable.set(x, y, z);
                        BlockPos placedPos = mutable.toImmutable();
                        placeOrbBlock(world, placedPos, chunkBox);
                    }
                }
            }

            if(this.hasVines) {
                generateVines(world, chunkBox);
            }
        }

        private void generateVines(StructureWorldAccess world, BlockBox chunkBox) {
            Random vineRandom = Random.create(this.seed ^ 0xABCDEF123456789L);

            int minX = Math.max(chunkBox.getMinX(), this.center.getX() - this.radius - 1);
            int maxX = Math.min(chunkBox.getMaxX(), this.center.getX() + this.radius + 1);
            int minY = Math.max(chunkBox.getMinY(), this.center.getY() - this.radius - 1);
            int maxY = Math.min(chunkBox.getMaxY(), this.center.getY() + this.radius + 1);
            int minZ = Math.max(chunkBox.getMinZ(), this.center.getZ() - this.radius - 1);
            int maxZ = Math.min(chunkBox.getMaxZ(), this.center.getZ() + this.radius + 1);

            BlockPos.Mutable mutable = new BlockPos.Mutable();

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        mutable.set(x, y, z);
                        BlockPos pos = mutable.toImmutable();

                        BlockState currentState = world.getBlockState(pos);
                        if (!currentState.isOf(this.state1.get(vineRandom, pos).getBlock())
                                && !currentState.isOf(this.state2.get(vineRandom, pos).getBlock()))
                            continue;

                        // Random chance per block
                        if (vineRandom.nextFloat() > this.vineChance)
                            continue;

                        // Try all four horizontal directions
                        for (var direction : Direction.Type.HORIZONTAL) {
                            BlockPos sidePos = pos.offset(direction);
                            BlockState sideState = world.getBlockState(sidePos);

                            // Only place vines if air and within bounds
                            if (!chunkBox.contains(sidePos) || !sideState.isAir())
                                continue;

                            int length = Math.max(1, this.vineLength.get(vineRandom));

                            // Start placing vines downward along that side
                            for (int i = 0; i < length; i++) {
                                BlockPos vinePos = sidePos.down(i);
                                if (!chunkBox.contains(vinePos) || !world.getBlockState(vinePos).isAir())
                                    break;

                                // Get a vine state facing *toward* the orb block
                                BlockState vine = this.vineState.get(vineRandom, vinePos);
                                if (vine.contains(VineBlock.getFacingProperty(direction.getOpposite()))) {
                                    vine = vine.with(VineBlock.getFacingProperty(direction.getOpposite()), true);
                                }

                                world.setBlockState(vinePos, vine, Block.NOTIFY_LISTENERS);
                            }
                        }
                    }
                }
            }
        }

        private void placeOrbBlock(StructureWorldAccess world, BlockPos pos, BlockBox chunkBox) {
            if (!chunkBox.contains(pos))
                return;

            Random blockRandom = createPositionRandom(pos, 0L);
            BlockStateProvider provider = blockRandom.nextFloat() < this.secondBlockChance ? this.state2 : this.state1;
            BlockState state = provider.get(blockRandom, pos);
            world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
        }

        private Random createPositionRandom(BlockPos pos, long salt) {
            long mixed = mixSeedValue(this.seed ^ salt, pos.getX());
            mixed = mixSeedValue(mixed, pos.getY());
            mixed = mixSeedValue(mixed, pos.getZ());
            return Random.create(mixed);
        }

        private static long mixSeedValue(long seed, int value) {
            long mixed = seed ^ ((long) value * 0x9E3779B97F4A7C15L);
            mixed = (mixed ^ (mixed >>> 30)) * 0xBF58476D1CE4E5B9L;
            mixed = (mixed ^ (mixed >>> 27)) * 0x94D049BB133111EBL;
            return mixed ^ (mixed >>> 31);
        }
    }

    private static int readIntOrThrow(NbtCompound nbt, String key) {
        return nbt.getInt(key).orElseThrow(
                () -> new IllegalStateException("Key '%s' is not an int in floating orb piece nbt!".formatted(key)));
    }

    private static boolean readBooleanOrThrow(NbtCompound nbt, String key) {
        return nbt.getBoolean(key).orElseThrow(
                () -> new IllegalStateException("Key '%s' is not a boolean in floating orb piece nbt!".formatted(key)));
    }

    private static float readFloatOrThrow(NbtCompound nbt, String key) {
        return nbt.getFloat(key).orElseThrow(
                () -> new IllegalStateException("Key '%s' is not a float in floating orb piece nbt!".formatted(key)));
    }

    private static long readLongOrThrow(NbtCompound nbt, String key) {
        return nbt.getLong(key).orElseThrow(
                () -> new IllegalStateException("Key '%s' is not a long in floating orb piece nbt!".formatted(key)));
    }
}
