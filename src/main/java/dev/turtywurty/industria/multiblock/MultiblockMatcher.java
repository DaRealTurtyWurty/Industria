package dev.turtywurty.industria.multiblock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record MultiblockMatcher(@NotNull MultiblockDefinition definition) {
    public MultiblockMatcher {
        Objects.requireNonNull(definition, "MultiblockDefinition cannot be null");
    }

    public Optional<MatchResult> tryMatch(ServerWorld world, BlockPos controllerPos) {
        return tryMatch(world, controllerPos, false);
    }

    public Optional<MatchResult> tryMatch(ServerWorld world, BlockPos controllerPos, boolean skipControllerCheck) {
        for (AxisRotation rotation : definition.rotations()) {
            for (MirrorMode mirrorMode : definition.allowedMirrors()) {
                var transform = new MultiblockTransform(rotation, mirrorMode);
                Vec3i size = definition.size();
                Vec3i anchor = definition.anchor();

                BlockPos originPos = controllerPos.subtract(transform.applyToLocal(size, anchor));
                if (matches(world, originPos, transform)) {
                    return Optional.of(createResult(world, controllerPos, originPos, transform));
                }
            }
        }

        return Optional.empty();
    }

    private boolean matches(ServerWorld world, BlockPos originPos, MultiblockTransform transform) {
        Vec3i size = definition.size();

        boolean failed = false;
        for (int y = 0; y < size.getY(); y++) {
            for (int z = 0; z < size.getZ(); z++) {
                for (int x = 0; x < size.getX(); x++) {
                    char character = definition.getCharAt(x, (size.getY() - 1) - y, z);
                    BlockPredicate predicate = definition.palette().get(character);
                    if (predicate == null || predicate == BlockPredicate.alwaysTrue())
                        continue;

                    Vec3i localPos = transform.applyToLocal(size, x, y, z);
                    BlockPos pos = originPos.add(localPos);

                    if (!predicate.test(world, pos)) {
//                        for (ServerPlayerEntity player : world.getPlayers()) {
//                            player.sendMessage((Text.literal("Expected '" + character + "' at " + pos + ", but found '" + world.getBlockState(pos).getBlock().getTranslationKey() + "'")));
//                        }

                        world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK_MARKER, Blocks.BARRIER.getDefaultState()), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0, 0, 0.1);
                        failed = true;
                    }
                }
            }
        }

        return !failed;
    }

    private MatchResult createResult(
            StructureWorldAccess world,
            BlockPos controllerPos,
            BlockPos originPos,
            MultiblockTransform transform) {
        var builder = new MatchResult.Builder()
                .definitionId(definition.id())
                .controllerPos(controllerPos)
                .transform(transform)
                .origin(originPos)
                .size(definition.size());

        List<MatchResult.Problem> problems = new ArrayList<>();
        for (int y = 0; y < definition.size().getY(); y++) {
            for (int z = 0; z < definition.size().getZ(); z++) {
                for (int x = 0; x < definition.size().getX(); x++) {
                    char character = definition.getCharAt(x, y, z);
                    BlockPredicate predicate = definition.palette().get(character);
                    if (predicate == null || predicate == BlockPredicate.alwaysTrue())
                        continue;

                    Vec3i localPos = transform.applyToLocal(definition.size(), x, y, z);
                    BlockPos worldPos = originPos.add(localPos);
                    BlockState state = world.getBlockState(worldPos);

                    builder.addCell(worldPos, localPos.getX(), localPos.getY(), localPos.getZ(), character, state);

                    PortRule portRule = definition.portRules().get(character);
                    if (portRule != null) {
                        for (LocalDirection face : portRule.sides()) {
                            Direction absoluteFace = face.toWorld(face);
                            List<PortType> portTypes = portRule.types();
                            if (portTypes.isEmpty())
                                continue;

                            Set<MatchResult.PortMode> modes = portTypes.stream()
                                    .map(portType -> portType.isInput() ?
                                            portType.isOutput() ?
                                                    MatchResult.PortMode.BOTH :
                                                    MatchResult.PortMode.INPUT :
                                            MatchResult.PortMode.OUTPUT)
                                    .collect(Collectors.toSet());

                            builder.addPort(
                                    worldPos,
                                    absoluteFace,
                                    modes,
                                    Set.copyOf(portTypes),
                                    localPos.getX(),
                                    localPos.getY(),
                                    localPos.getZ(),
                                    character
                            );
                        }
                    }
                }
            }
        }

        problems.forEach(builder::addProblem);

        return builder.build();
    }

    public static final class MatchResult {
        private final Identifier definitionId;
        private final BlockPos controllerPos;
        private final MultiblockTransform transform;
        private final BlockPos originPos;
        private final Vec3i size;
        private final List<Cell> cells;
        private final List<ResolvedPort> ports;
        private final List<Problem> problems;

        private final Box boundingBox;

        public MatchResult(
                Identifier definitionId,
                BlockPos controllerPos,
                MultiblockTransform transform,
                BlockPos originPos,
                Vec3i size,
                List<Cell> cells,
                List<ResolvedPort> ports,
                List<Problem> problems) {
            this.definitionId = definitionId;
            this.controllerPos = controllerPos;
            this.transform = transform;
            this.originPos = originPos;
            this.size = size;
            this.cells = cells;
            this.ports = ports;
            this.problems = problems;
            this.boundingBox = boxFromPositions(positions());
        }

        public boolean isValid() {
            return problems.isEmpty();
        }

        public Stream<BlockPos> positions() {
            return cells.stream().map(Cell::position);
        }

        public boolean contains(BlockPos pos) {
            return boundingBox.contains(pos.getX(), pos.getY(), pos.getZ());
        }

        public Identifier definitionId() {
            return definitionId;
        }

        public BlockPos controllerPos() {
            return controllerPos;
        }

        public MultiblockTransform transform() {
            return transform;
        }

        public BlockPos originPos() {
            return originPos;
        }

        public Vec3i size() {
            return size;
        }

        public List<Cell> cells() {
            return cells;
        }

        public List<ResolvedPort> ports() {
            return ports;
        }

        public List<Problem> problems() {
            return problems;
        }

        public Box boundingBox() {
            return boundingBox;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (MatchResult) obj;
            return Objects.equals(this.definitionId, that.definitionId) &&
                    Objects.equals(this.controllerPos, that.controllerPos) &&
                    Objects.equals(this.transform, that.transform) &&
                    Objects.equals(this.originPos, that.originPos) &&
                    Objects.equals(this.size, that.size) &&
                    Objects.equals(this.cells, that.cells) &&
                    Objects.equals(this.ports, that.ports) &&
                    Objects.equals(this.problems, that.problems);
        }

        @Override
        public int hashCode() {
            return Objects.hash(definitionId, controllerPos, transform, originPos, size, cells, ports, problems);
        }

        @Override
        public String toString() {
            return "MatchResult[" +
                    "definitionId=" + definitionId + ", " +
                    "controllerPos=" + controllerPos + ", " +
                    "transform=" + transform + ", " +
                    "originPos=" + originPos + ", " +
                    "size=" + size + ", " +
                    "cells=" + cells + ", " +
                    "ports=" + ports + ", " +
                    "problems=" + problems + ']';
        }

        public static class Builder {
            private Identifier definitionId;
            private BlockPos controllerPos;
            private MultiblockTransform transform;
            private BlockPos origin;
            private Vec3i size;

            private final List<Cell> cells = new ArrayList<>();
            private final List<ResolvedPort> ports = new ArrayList<>();
            private final List<Problem> problems = new ArrayList<>();

            public Builder definitionId(Identifier identifier) {
                this.definitionId = identifier;
                return this;
            }

            public Builder controllerPos(BlockPos pos) {
                this.controllerPos = pos;
                return this;
            }

            public Builder transform(MultiblockTransform transform) {
                this.transform = transform;
                return this;
            }

            public Builder origin(BlockPos origin) {
                this.origin = origin;
                return this;
            }

            public Builder size(Vec3i size) {
                this.size = size;
                return this;
            }

            public Builder addCell(BlockPos worldPos, int localX, int localY, int localZ, char key, BlockState state) {
                this.cells.add(new Cell(worldPos, localX, localY, localZ, key, state));
                return this;
            }

            public Builder addPort(BlockPos worldPos, Direction absoluteFace,
                                   Set<PortMode> mode, Set<PortType> types,
                                   int localX, int localY, int localZ, char key) {
                this.ports.add(new ResolvedPort(worldPos, absoluteFace, mode, types, localX, localY, localZ, key));
                return this;
            }

            public Builder addProblem(Problem problem) {
                this.problems.add(problem);
                return this;
            }

            public MatchResult build() {
                Objects.requireNonNull(definitionId, "definitionId");
                Objects.requireNonNull(controllerPos, "controllerPos");
                Objects.requireNonNull(transform, "transform");
                Objects.requireNonNull(origin, "origin");
                Objects.requireNonNull(size, "size");

                return new MatchResult(definitionId, controllerPos, transform, origin, size, cells, ports, problems);
            }
        }

        public record Cell(BlockPos position, int localX, int localY, int localZ, char character, BlockState state) {
            public Cell(BlockPos position, int localX, int localY, int localZ, char character, BlockState state) {
                this.position = position.toImmutable();
                this.localX = localX;
                this.localY = localY;
                this.localZ = localZ;
                this.character = character;
                this.state = state;
            }
        }

        public record ResolvedPort(
                BlockPos pos,
                Direction face,
                Set<PortMode> mode,
                Set<PortType> portTypes,
                int localX,
                int localY,
                int localZ,
                char character) {
            public ResolvedPort(
                    BlockPos pos,
                    Direction face,
                    Set<PortMode> mode,
                    Set<PortType> portTypes,
                    int localX,
                    int localY,
                    int localZ,
                    char character) {
                this.pos = pos.toImmutable();
                this.face = face;
                this.mode = mode;
                this.portTypes = Set.copyOf(portTypes);
                this.localX = localX;
                this.localY = localY;
                this.localZ = localZ;
                this.character = character;
            }
        }

        public interface Problem {
            BlockPos pos();

            char expected();

            String message();
        }

        public record MismatchProblem(BlockPos pos, char expected, BlockState actualState) implements Problem {
            public MismatchProblem(BlockPos pos, char expected, BlockState actualState) {
                this.pos = pos.toImmutable();
                this.expected = expected;
                this.actualState = actualState;
            }

            @Override
            public String message() {
                return "Mismatch at " + pos + ": expected '" + expected + "', but found '" + actualState.getBlock().getTranslationKey() + "'";
            }
        }

        public record NotReplaceableProblem(BlockPos pos, char expected) implements Problem {
            public NotReplaceableProblem(BlockPos pos, char expected) {
                this.pos = pos.toImmutable();
                this.expected = expected;
            }

            @Override
            public String message() {
                return "Block at " + pos + " is not replaceable for character '" + expected + "'";
            }
        }

        public enum PortMode {
            INPUT, OUTPUT, BOTH
        }
    }

    private static Box boxFromPositions(Stream<BlockPos> positions) {
        var iterator = positions.iterator();
        if (!iterator.hasNext())
            return new Box(0, 0, 0, 0, 0, 0);

        BlockPos first = iterator.next();
        int minX = first.getX(), minY = first.getY(), minZ = first.getZ();
        int maxX = minX, maxY = minY, maxZ = minZ;
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        return new Box(
                minX, minY, minZ,
                maxX, maxY, maxZ
        );
    }
}
