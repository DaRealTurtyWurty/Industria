package dev.turtywurty.industria.testworld;

import com.mojang.authlib.GameProfile;
import dev.turtywurty.industria.block.PipeBlock;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.multiblocklib.data.MultiblockDefinition;
import dev.turtywurty.multiblocklib.match.BlockMatcherList;
import dev.turtywurty.multiblocklib.pattern.MultiblockPattern;
import dev.turtywurty.turtymultiloader.player.FakePlayerService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public record TestWorldContext(MinecraftServer server, ServerLevel overworld, BlockPos origin) {
    private static final int MAX_PATHFINDING_NODES = 250_000;

    public BlockPos pos(int x, int y, int z) {
        return origin.offset(x, y, z);
    }

    public TestWorldContext at(int x, int y, int z) {
        return new TestWorldContext(server, overworld, pos(x, y, z));
    }

    public void setBlock(int x, int y, int z, Supplier<? extends Block> blockSupplier) {
        setBlock(x, y, z, blockSupplier.get());
    }

    public void setBlock(int x, int y, int z, Block block) {
        setBlock(x, y, z, block.defaultBlockState());
    }

    public void setBlock(int x, int y, int z, BlockState state) {
        overworld.setBlockAndUpdate(pos(x, y, z), state);
    }

    public void removeBlock(int x, int y, int z) {
        overworld.removeBlock(pos(x, y, z), false);
    }

    public List<BlockPos> runPipe(BlockPos start, BlockPos end) {
        return runPipe(start, end, ModBlocks.FLUID_PIPE);
    }

    public List<BlockPos> runPipe(BlockPos start, BlockPos end, Supplier<? extends Block> pipeSupplier) {
        Objects.requireNonNull(pipeSupplier, "pipeSupplier");
        return placeBlockPath(start, end, pipeSupplier.get());
    }

    public List<BlockPos> runPipe(BlockPos start, BlockPos end, Block pipe) {
        return placeBlockPath(start, end, pipe);
    }

    public List<BlockPos> runPipe(List<BlockPos> positions) {
        return runPipe(positions, ModBlocks.FLUID_PIPE);
    }

    public List<BlockPos> runPipe(List<BlockPos> positions, Supplier<? extends Block> pipeSupplier) {
        Objects.requireNonNull(pipeSupplier, "pipeSupplier");
        return placeBlockPath(positions, pipeSupplier.get());
    }

    public List<BlockPos> runPipe(List<BlockPos> positions, Block pipe) {
        return placeBlockPath(positions, pipe);
    }

    public List<BlockPos> placeBlockPath(BlockPos start, BlockPos end, Block block) {
        Objects.requireNonNull(block, "block");
        return placeBlockPath(start, end, block.defaultBlockState());
    }

    public List<BlockPos> placeBlockPath(BlockPos start, BlockPos end, BlockState state) {
        Objects.requireNonNull(state, "state");

        List<BlockPos> path = findShortestPath(start, end, pathPos -> canPlacePathBlock(pathPos, state));
        placePathBlocks(path, state);
        return path;
    }

    public List<BlockPos> placeBlockPath(List<BlockPos> positions, Block block) {
        Objects.requireNonNull(block, "block");
        return placeBlockPath(positions, block.defaultBlockState());
    }

    public List<BlockPos> placeBlockPath(List<BlockPos> positions, BlockState state) {
        Objects.requireNonNull(positions, "positions");
        Objects.requireNonNull(state, "state");
        if (positions.isEmpty())
            return List.of();

        var path = new LinkedHashSet<BlockPos>();
        Predicate<BlockPos> canTraverse = pathPos -> canPlacePathBlock(pathPos, state);
        if (positions.size() == 1) {
            BlockPos position = positions.getFirst();
            path.addAll(findShortestPath(position, position, canTraverse));
        } else {
            for (int index = 1; index < positions.size(); index++) {
                path.addAll(findShortestPath(positions.get(index - 1), positions.get(index), canTraverse));
            }
        }

        List<BlockPos> result = List.copyOf(path);
        placePathBlocks(result, state);
        return result;
    }

    private boolean canPlacePathBlock(BlockPos pos, BlockState state) {
        BlockState existingState = overworld.getBlockState(pos);
        return existingState.canBeReplaced() || existingState.is(state.getBlock());
    }

    private void placePathBlocks(List<BlockPos> path, BlockState state) {
        for (BlockPos pathPos : path) {
            BlockState existingState = overworld.getBlockState(pathPos);
            if (existingState.is(state.getBlock()))
                continue;

            overworld.setBlockAndUpdate(pathPos, state);
            if (state.getBlock() instanceof PipeBlock<?, ?> pipeBlock) {
                var networkManager = pipeBlock.getNetworkManager(overworld);
                if (!networkManager.containsPipe(pathPos))
                    networkManager.placePipe(overworld, pathPos);
            }
        }
    }

    public List<BlockPos> findShortestPath(BlockPos start, BlockPos end, Predicate<BlockPos> canTraverse) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        Objects.requireNonNull(canTraverse, "canTraverse");

        BlockPos immutableStart = start.immutable();
        BlockPos immutableEnd = end.immutable();
        if (overworld.isOutsideBuildHeight(immutableStart) || overworld.isOutsideBuildHeight(immutableEnd))
            throw new IllegalArgumentException("Path endpoints must be inside the world's build height");

        if (!canTraverse.test(immutableStart))
            throw new IllegalStateException("Path start is obstructed at " + immutableStart);

        if (!canTraverse.test(immutableEnd))
            throw new IllegalStateException("Path end is obstructed at " + immutableEnd);

        if (immutableStart.equals(immutableEnd))
            return List.of(immutableStart);

        long nextSequence = 0;
        PathNode startNode = new PathNode(
                new PathState(immutableStart, null),
                0,
                0,
                manhattanDistance(immutableStart, immutableEnd),
                nextSequence++
        );

        PriorityQueue<PathNode> open = new PriorityQueue<>();
        open.add(startNode);

        Map<PathState, PathCost> bestCosts = new HashMap<>();
        bestCosts.put(startNode.state(), new PathCost(0, 0));

        Map<PathState, PathState> previous = new HashMap<>();
        int visitedNodes = 0;

        while (!open.isEmpty()) {
            PathNode current = open.remove();
            PathCost bestCost = bestCosts.get(current.state());
            if (bestCost == null || bestCost.steps() != current.steps() || bestCost.turns() != current.turns())
                continue;

            if (++visitedNodes > MAX_PATHFINDING_NODES)
                throw new IllegalStateException("Could not find a path from " + immutableStart + " to "
                        + immutableEnd + " after visiting " + MAX_PATHFINDING_NODES + " nodes");

            if (current.state().pos().equals(immutableEnd))
                return reconstructPath(current.state(), previous);

            for (Direction direction : Direction.values()) {
                BlockPos neighbour = current.state().pos().relative(direction);
                if (overworld.isOutsideBuildHeight(neighbour) || !canTraverse.test(neighbour))
                    continue;

                int steps = current.steps() + 1;
                int turns = current.turns();
                if (current.state().arrivalDirection() != null
                        && current.state().arrivalDirection() != direction)
                    turns++;

                PathState neighbourState = new PathState(neighbour, direction);
                PathCost neighbourCost = new PathCost(steps, turns);
                PathCost knownCost = bestCosts.get(neighbourState);
                if (knownCost != null && knownCost.compareTo(neighbourCost) <= 0)
                    continue;

                bestCosts.put(neighbourState, neighbourCost);
                previous.put(neighbourState, current.state());
                open.add(new PathNode(
                        neighbourState,
                        steps,
                        turns,
                        manhattanDistance(neighbour, immutableEnd),
                        nextSequence++
                ));
            }
        }

        throw new IllegalStateException("No unobstructed path exists from " + immutableStart + " to " + immutableEnd);
    }

    private static List<BlockPos> reconstructPath(PathState end, Map<PathState, PathState> previous) {
        List<BlockPos> reversedPath = new ArrayList<>();
        PathState current = end;
        while (current != null) {
            reversedPath.add(current.pos());
            current = previous.get(current);
        }

        return reversedPath.reversed().stream().toList();
    }

    private static long manhattanDistance(BlockPos first, BlockPos second) {
        return Math.abs((long) first.getX() - second.getX())
                + Math.abs((long) first.getY() - second.getY())
                + Math.abs((long) first.getZ() - second.getZ());
    }

    private record PathState(BlockPos pos, Direction arrivalDirection) {
    }

    private record PathCost(int steps, int turns) implements Comparable<PathCost> {
        @Override
        public int compareTo(PathCost other) {
            int stepComparison = Integer.compare(steps, other.steps);
            return stepComparison != 0 ? stepComparison : Integer.compare(turns, other.turns);
        }
    }

    private record PathNode(
            PathState state,
            int steps,
            int turns,
            long remainingDistance,
            long sequence
    ) implements Comparable<PathNode> {
        @Override
        public int compareTo(PathNode other) {
            int distanceComparison = Long.compare(steps + remainingDistance, other.steps + other.remainingDistance);
            if (distanceComparison != 0)
                return distanceComparison;

            int turnComparison = Integer.compare(turns, other.turns);
            if (turnComparison != 0)
                return turnComparison;

            int remainingComparison = Long.compare(remainingDistance, other.remainingDistance);
            if (remainingComparison != 0)
                return remainingComparison;

            return Long.compare(sequence, other.sequence);
        }
    }

    public void fill(
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ,
            Block block
    ) {
        fill(minX, minY, minZ, maxX, maxY, maxZ, block.defaultBlockState());
    }

    public void fill(
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ,
            BlockState state
    ) {
        BlockPos.betweenClosedStream(
                pos(minX, minY, minZ),
                pos(maxX, maxY, maxZ)
        ).forEach(pos -> overworld.setBlockAndUpdate(pos, state));
    }

    public void fillAir(
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ
    ) {
        fill(minX, minY, minZ, maxX, maxY, maxZ, Blocks.AIR);
    }

    public void fillReplace(
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ,
            BlockState state,
            BlockState replace
    ) {
        fillReplaceWhere(
                minX, minY, minZ,
                maxX, maxY, maxZ,
                state,
                blockState -> blockState.equals(replace)
        );
    }

    public void fillReplace(
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ,
            BlockState state,
            Block replace
    ) {
        fillReplaceWhere(
                minX, minY, minZ,
                maxX, maxY, maxZ,
                state,
                blockState -> blockState.is(replace)
        );
    }

    public void fillReplaceAir(
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ,
            BlockState state
    ) {
        fillReplace(minX, minY, minZ, maxX, maxY, maxZ, state, Blocks.AIR.defaultBlockState());
    }

    public void fillReplaceWhere(
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ,
            BlockState state,
            Predicate<BlockState> predicate
    ) {
        BlockPos.betweenClosedStream(
                pos(minX, minY, minZ),
                pos(maxX, maxY, maxZ)
        ).forEach(pos -> {
            if (predicate.test(overworld.getBlockState(pos))) {
                overworld.setBlockAndUpdate(pos, state);
            }
        });
    }

    public <T extends BlockEntity> T requireBlockEntity(int x, int y, int z, Class<T> type) {
        BlockPos pos = pos(x, y, z);
        BlockEntity blockEntity = overworld.getBlockEntity(pos);
        if (blockEntity == null)
            throw new IllegalStateException("Expected " + type.getSimpleName() + " at " + pos + ", but found no block entity");

        if (!type.isInstance(blockEntity))
            throw new IllegalStateException("Expected " + type.getSimpleName() + " at " + pos + ", but found " + blockEntity.getClass().getSimpleName());

        return type.cast(blockEntity);
    }

    public <T extends BlockEntity> void configureBlockEntity(int x, int y, int z, Class<T> type, Consumer<T> consumer) {
        T blockEntity = requireBlockEntity(x, y, z, type);
        consumer.accept(blockEntity);
        blockEntity.setChanged();
    }

    public void afterTicks(long ticks, Consumer<TestWorldContext> action) {
        if (ticks < 0)
            throw new IllegalArgumentException("Ticks must be non-negative");

        TestWorldScheduler.schedule(this, ticks, action);
    }

    public void constructMultiblockPattern(int x, int y, int z, MultiblockDefinition definition) {
        Objects.requireNonNull(definition, "definition");

        MultiblockPattern pattern = definition.pattern();
        BlockPos anchor = pos(x, y, z);
        pattern.positions().stream()
                .sorted(Comparator.comparingLong((BlockPos blockPos) -> blockPos.getY())
                        .thenComparingLong(BlockPos::getX)
                        .thenComparingLong(BlockPos::getZ))
                .forEach(localPos -> {
                    BlockMatcherList matcher = pattern.matcherAt(localPos);
                    if (matcher == null)
                        throw new IllegalStateException("Multiblock '" + definition.id()
                                + "' has no matcher at pattern position " + localPos);

                    BlockPos worldPos = anchor.offset(localPos);
                    BlockState exampleState = matcher.exampleState().orElse(null);
                    if (exampleState != null) {
                        overworld.setBlockAndUpdate(worldPos, exampleState);
                        return;
                    }

                    BlockState existingState = overworld.getBlockState(worldPos);
                    if (!matcher.matches(existingState))
                        throw new IllegalStateException("Cannot construct multiblock '" + definition.id()
                                + "' at " + worldPos + ": matcher " + matcher.describe()
                                + " has no example state and does not match the existing state " + existingState);
                });
    }

    public ServerPlayer createFakePlayer(int x, int y, int z, GameProfile profile) {
        BlockPos pos = pos(x, y, z);
        ServerPlayer player = FakePlayerService.get().get(overworld, profile);
        player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        return player;
    }

    public InteractionResult useItemOnBlock(int x, int y, int z, ServerPlayer player, ItemStack stack, InteractionHand hand) {
        BlockPos pos = pos(x, y, z);
        player.setItemInHand(hand, stack);

        Vec3 hitPos = pos.getCenter().add(0, 0.5, 0);
        var hitResult = new BlockHitResult(hitPos, Direction.UP, pos, false);
        return player.gameMode.useItemOn(player, overworld, stack, hand, hitResult);
    }

    public InteractionResult useItemOnBlock(int x, int y, int z, ServerPlayer player, Item item, InteractionHand hand) {
        return useItemOnBlock(x, y, z, player, item.getDefaultInstance(), hand);
    }

    public InteractionResult useItemOnBlock(int x, int y, int z, ServerPlayer player, Supplier<? extends Item> itemSupplier, InteractionHand hand) {
        return useItemOnBlock(x, y, z, player, itemSupplier.get(), hand);
    }
}
