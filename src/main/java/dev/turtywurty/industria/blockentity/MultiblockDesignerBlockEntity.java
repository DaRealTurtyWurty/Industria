package dev.turtywurty.industria.blockentity;

import com.google.common.collect.ImmutableMap;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.multiblock.PieceData;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.MultiblockDesignerScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiblockDesignerBlockEntity extends UpdatableBlockEntity implements BlockEntityWithGui<BlockPosPayload> {
    public static final Text TITLE = Industria.containerTitle("multiblock_designer");

    private final Map<BlockPos, PieceData> pieces = new HashMap<>();

    public MultiblockDesignerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.MULTIBLOCK_DESIGNER, pos, state);
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new BlockPosPayload(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new MultiblockDesignerScreenHandler(syncId, playerInventory, this);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.put("Pieces", PieceData.CODEC.listOf(), this.pieces.values().stream().toList());
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        this.pieces.clear();
        for (PieceData piece : view.read("Pieces", PieceData.CODEC.listOf()).orElse(List.of())) {
            this.pieces.put(piece.position, piece);
            if (this.world != null) {
                piece.predicate = BlockPredicate.Builder.create()
                        .tag(this.world.getRegistryManager().getOrThrow(RegistryKeys.BLOCK), BlockTags.LOGS)
                        .build();
            }
        }
    }

    public void addPiece(BlockPos position) {
        if (!this.pieces.containsKey(position)) {
            this.pieces.put(position, new PieceData(position));
            update();
        }
    }

    @Override
    public boolean shouldWaitForEndTick() {
        return false;
    }

    public void removePiece(BlockPos position) {
        if (this.pieces.remove(position) != null) {
            update();
        }
    }

    public void setPaletteChar(BlockPos position, char paletteChar) {
        PieceData pieceData = this.pieces.get(position);
        if (pieceData == null || pieceData.paletteChar == paletteChar)
            return;

        pieceData.paletteChar = paletteChar;
        if (this.world != null && this.world.getBlockEntity(position) instanceof MultiblockPieceBlockEntity piece) {
            piece.setKey(paletteChar);
        }

        update();
    }

    public Map<BlockPos, PieceData> getPieces() {
        return ImmutableMap.copyOf(this.pieces);
    }
}
