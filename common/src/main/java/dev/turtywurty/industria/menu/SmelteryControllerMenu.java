package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.SmelteryControllerBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.network.SmelteryControllerSnapshotPayload;
import dev.turtywurty.turtymultiloader.network.NetworkService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SmelteryControllerMenu extends AbstractContainerMenu {
    private final BlockPos controllerPos;
    private final ContainerLevelAccess context;
    private final Player player;

    private List<SmelteryChamberInfo> chambers = List.of();
    private float temperature;
    private boolean structureReady;
    private boolean keepMolten;
    private boolean snapshotSent;

    public SmelteryControllerMenu(int syncId, Inventory inventory, BlockPosPayload payload) {
        this(syncId, inventory, payload.pos());
    }

    public SmelteryControllerMenu(int syncId, Inventory inventory, BlockPos controllerPos) {
        super(ModMenuTypes.SMELTERY_CONTROLLER.get(), syncId);
        this.controllerPos = controllerPos.immutable();
        this.context = ContainerLevelAccess.create(inventory.player.level(), controllerPos);
        this.player = inventory.player;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (!(this.player instanceof ServerPlayer serverPlayer)
                || !this.player.level().isLoaded(this.controllerPos)
                || !(this.player.level().getBlockEntity(this.controllerPos) instanceof SmelteryControllerBlockEntity controller))
            return;

        float temperature = controller.getTemperature();
        boolean structureReady = controller.isStructureReady();
        boolean keepMolten = controller.isKeepingMolten();
        List<SmelteryChamberInfo> chambers = controller.getChamberInfos();
        if (snapshotSent
                && Float.compare(this.temperature, temperature) == 0
                && this.structureReady == structureReady
                && this.keepMolten == keepMolten
                && this.chambers.equals(chambers))
            return;

        applySnapshot(temperature, structureReady, keepMolten, chambers);
        NetworkService.get().sendToPlayer(serverPlayer, new SmelteryControllerSnapshotPayload(
                this.containerId,
                this.controllerPos,
                temperature,
                structureReady,
                keepMolten,
                chambers
        ));
        this.snapshotSent = true;
    }

    public void applySnapshot(float temperature, boolean structureReady, boolean keepMolten, List<SmelteryChamberInfo> chambers) {
        this.temperature = temperature;
        this.structureReady = structureReady;
        this.keepMolten = keepMolten;
        this.chambers = List.copyOf(chambers);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != 0
                || player.level().isClientSide()
                || !stillValid(player)
                || !player.level().isLoaded(this.controllerPos)
                || !(player.level().getBlockEntity(this.controllerPos) instanceof SmelteryControllerBlockEntity controller))
            return false;

        controller.setKeepMolten(!controller.isKeepingMolten());
        broadcastChanges();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, ModBlocks.SMELTERY_CONTROLLER.get());
    }

    public BlockPos getControllerPos() {
        return this.controllerPos;
    }

    public List<SmelteryChamberInfo> getChambers() {
        return this.chambers;
    }

    public float getTemperature() {
        return this.temperature;
    }

    public boolean isStructureReady() {
        return this.structureReady;
    }

    public boolean isKeepingMolten() {
        return this.keepMolten;
    }

    public record SmelteryChamberInfo(BlockPos hatchPos, int width, int height, int depth, FluidStack fluid,
                                      long capacity, float requiredTemperature, BlockState solidBlock) {
        public static final StreamCodec<RegistryFriendlyByteBuf, SmelteryChamberInfo> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, SmelteryChamberInfo::hatchPos,
                ByteBufCodecs.VAR_INT, SmelteryChamberInfo::width,
                ByteBufCodecs.VAR_INT, SmelteryChamberInfo::height,
                ByteBufCodecs.VAR_INT, SmelteryChamberInfo::depth,
                FluidStack.STREAM_CODEC, SmelteryChamberInfo::fluid,
                ByteBufCodecs.VAR_LONG, SmelteryChamberInfo::capacity,
                ByteBufCodecs.FLOAT, SmelteryChamberInfo::requiredTemperature,
                ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), SmelteryChamberInfo::solidBlock,
                SmelteryChamberInfo::new
        );
    }
}
