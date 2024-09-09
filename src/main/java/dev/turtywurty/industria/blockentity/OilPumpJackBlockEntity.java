package dev.turtywurty.industria.blockentity;

import com.mojang.datafixers.util.Pair;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.OilPumpJackBlock;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.init.AttachmentTypeInit;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.OilPumpJackScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class OilPumpJackBlockEntity extends UpdatableBlockEntity implements TickableBlockEntity, ExtendedScreenHandlerFactory<BlockPosPayload> {
    public static final Text TITLE = Text.translatable("container." + Industria.MOD_ID + ".oil_pump_jack");
    private final List<BlockPos> machinePositions = new ArrayList<>();
    public float clientRotation;
    public boolean reverseCounterWeights;

    public OilPumpJackBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.OIL_PUMP_JACK, pos, state);
    }

    @Override
    public void tick() {

    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new BlockPosPayload(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new OilPumpJackScreenHandler(syncId, playerInventory, this);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.machinePositions.clear();

        if(nbt.contains("MachinePositions", NbtElement.LIST_TYPE)) {
            NbtList machinePositions = nbt.getList("MachinePositions", NbtElement.INT_ARRAY_TYPE);
            for (NbtElement machinePosition : machinePositions) {
                this.machinePositions.add(BlockPos.CODEC.decode(NbtOps.INSTANCE, machinePosition).map(Pair::getFirst).getOrThrow());
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        var machinePositions = new NbtList();
        for (BlockPos machinePosition : this.machinePositions) {
            machinePositions.add(BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, machinePosition).result().orElseThrow());
        }

        nbt.put("MachinePositions", machinePositions);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = super.toInitialChunkDataNbt(registryLookup);
        writeSyncData(nbt, registryLookup);
        return nbt;
    }

    private void writeSyncData(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        // TODO
        // NO-OP
    }

    public void breakMachine() {
        if (this.world == null)
            return;

        for (BlockPos machinePos : this.machinePositions) {
            if(!this.world.getBlockState(machinePos).isOf(BlockInit.OIL_PUMP_JACK_MULTIBLOCK))
                continue;

            this.world.breakBlock(machinePos, false);

            Chunk chunk = this.world.getChunk(machinePos);
            Map<String, BlockPos> map = chunk.getAttached(AttachmentTypeInit.OIL_PUMP_JACK_ATTACHMENT);
            if (map == null)
                return;

            Map<String, BlockPos> copy = new HashMap<>(map);
            copy.remove(machinePos.toShortString());
            if(copy.isEmpty()) {
                chunk.removeAttached(AttachmentTypeInit.OIL_PUMP_JACK_ATTACHMENT);
            } else {
                chunk.setAttached(AttachmentTypeInit.OIL_PUMP_JACK_ATTACHMENT, copy);
            }
        }

        this.world.breakBlock(this.pos, true);
    }

    public void buildMachine() {
        if (this.world == null || this.world.isClient)
            return;

        long startTime = System.nanoTime();
        Direction facing = getCachedState().get(OilPumpJackBlock.FACING);
        List<BlockPos> checkPositions = findValidPositions(facing);
        long findValidPositionsEndTime = System.nanoTime();
        if(checkPositions.size() != 123) {
            this.world.breakBlock(this.pos, true);
            return;
        }

        for (BlockPos position : checkPositions) {
            this.world.setBlockState(position, BlockInit.OIL_PUMP_JACK_MULTIBLOCK.getDefaultState());
            this.machinePositions.add(position);

            Chunk chunk = this.world.getChunk(position);
            Map<String, BlockPos> map = chunk.getAttachedOrCreate(AttachmentTypeInit.OIL_PUMP_JACK_ATTACHMENT, HashMap::new);
            Map<String, BlockPos> copy = new HashMap<>(map);
            copy.put(position.toShortString(), this.pos);
            chunk.setAttached(AttachmentTypeInit.OIL_PUMP_JACK_ATTACHMENT, copy);
        }

        markDirty();

        long endTime = System.nanoTime();

        System.out.println("Time to find valid positions: " + (findValidPositionsEndTime - startTime) + "ns");
        System.out.println("Time to build machine: " + (endTime - findValidPositionsEndTime) + "ns");
        System.out.println("Total time: " + (endTime - startTime) + "ns");
    }

    private List<BlockPos> findValidPositions(Direction facing) {
        if(this.world == null)
            return List.of();

        List<BlockPos> correctPositions = new ArrayList<>();
        List<BlockPos> incorrectPositions = new ArrayList<>();

        var mutablePos = new BlockPos.Mutable();
        for (int k = 0; k <= 2; k++) {
            for (int i = -4; i <= 3; i++) {
                for (int j = -1; j <= 1; j++) {
                    if(i == 0 && j == 0 && k == 0)
                        continue;

                    if(i == 3 && j == 0 && k == 2)
                        continue;

                    // check east and west sides (relative to facing)
                    if(i == 3 && j != 0 && k == 0) {
                        BlockPos checkPos = this.pos.offset(facing, i)
                                .offset(j > 0 ? facing.rotateYCounterclockwise() : facing.rotateYClockwise(), 2);

                        if(isValidPosition(this.world, checkPos)) {
                            correctPositions.add(checkPos);
                        } else {
                            incorrectPositions.add(checkPos);
                        }
                    }

                    mutablePos.set(this.pos.offset(facing, i).offset(facing.rotateYCounterclockwise(), j).offset(Direction.UP, k));

                    if(isValidPosition(this.world, mutablePos)) {
                        correctPositions.add(mutablePos.toImmutable());
                    } else {
                        incorrectPositions.add(mutablePos.toImmutable());
                    }
                }
            }
        }

        for (int i = -3; i <= 2; i++) {
            for (int j = -1; j <= 1; j++) {
                if(i == -3 && j == 0)
                    continue;

                mutablePos.set(this.pos.offset(facing, i).offset(facing.rotateYCounterclockwise(), j).offset(Direction.UP, 3));
                if(isValidPosition(this.world, mutablePos)) {
                    correctPositions.add(mutablePos.toImmutable());
                } else {
                    incorrectPositions.add(mutablePos.toImmutable());
                }
            }
        }

        for (int i = -1; i <= 2; i++) {
            for (int j = 0; j <= 3; j++) {
                mutablePos.set(this.pos.offset(facing, i).offset(Direction.UP, 4 + j));
                if(((i == -1 && j < 2) || (i == 2 && j == 0))) {
                    // check east and west sides (relative to facing)
                    BlockPos immutablePos = mutablePos.toImmutable();
                    BlockPos eastPos = immutablePos.offset(facing.rotateYClockwise());
                    BlockPos westPos = immutablePos.offset(facing.rotateYCounterclockwise());
                    if(isValidPosition(this.world, eastPos)) {
                        correctPositions.add(eastPos);
                    } else {
                        incorrectPositions.add(eastPos);
                    }

                    if(isValidPosition(this.world, westPos)) {
                        correctPositions.add(westPos);
                    } else {
                        incorrectPositions.add(westPos);
                    }
                }

                if(isValidPosition(this.world, mutablePos)) {
                    correctPositions.add(mutablePos.toImmutable());
                } else {
                    incorrectPositions.add(mutablePos.toImmutable());
                }
            }
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j <= 4; j++) {
                mutablePos.set(this.pos.offset(facing, 4 + i).offset(Direction.UP, 4 + j));

                if(i == 0 && j > 1 && j < 4) {
                    BlockPos checkPos = mutablePos.offset(facing.getOpposite());
                    if(isValidPosition(this.world, checkPos)) {
                        correctPositions.add(checkPos);
                    } else {
                        incorrectPositions.add(checkPos);
                    }
                }

                if(isValidPosition(this.world, mutablePos)) {
                    correctPositions.add(mutablePos.toImmutable());
                } else {
                    incorrectPositions.add(mutablePos.toImmutable());
                }
            }
        }

        return correctPositions.size() == 123 ? correctPositions : incorrectPositions;
    }

    private static boolean isValidPosition(World world, BlockPos position) {
        return world.getBlockState(position).isReplaceable();
    }
}
