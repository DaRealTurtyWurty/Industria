package dev.turtywurty.industria.blockentity;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.mojang.serialization.JsonOps;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.multiblock.PieceData;
import dev.turtywurty.industria.multiblock.VariedBlockList;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.MultiblockDesignerScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MultiblockDesignerBlockEntity extends UpdatableBlockEntity implements BlockEntityWithGui<BlockPosPayload> {
    public static final Component TITLE = Industria.containerTitle("multiblock_designer");

    private final Map<BlockPos, PieceData> pieces = new HashMap<>();

    public MultiblockDesignerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.MULTIBLOCK_DESIGNER, pos, state);
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new MultiblockDesignerScreenHandler(syncId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.store("Pieces", PieceData.CODEC.listOf(), this.pieces.values().stream().toList());
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);

        this.pieces.clear();
        for (PieceData piece : view.read("Pieces", PieceData.CODEC.listOf()).orElse(List.of())) {
            this.pieces.put(piece.position, piece);

            if (piece.name == null || piece.name.isBlank()) {
                piece.name = "Untitled";
            }
        }

        System.out.println("Loaded pieces: " + this.pieces);
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
        VariedBlockList sharedVariedBlockLists = getVariedBlockListsForPaletteChar(paletteChar, position);
        if (sharedVariedBlockLists != null) {
            pieceData.variedBlockList = sharedVariedBlockLists;
        }

        String sharedName = getNameForPaletteChar(paletteChar, position);
        if (sharedName != null) {
            pieceData.name = sharedName;
        }

        if (this.level != null && this.level.getBlockEntity(position) instanceof MultiblockPieceBlockEntity piece) {
            piece.setKey(paletteChar);
        }

        update();
    }

    public void setPieceVariedBlockList(BlockPos position, VariedBlockList variedBlockList) {
        PieceData pieceData = this.pieces.get(position);
        if (pieceData == null)
            return;

        setPaletteVariedBlockList(pieceData.paletteChar, variedBlockList);
    }

    public void setPaletteVariedBlockList(char paletteChar, VariedBlockList variedBlockList) {
        boolean changed = false;
        for (PieceData piece : this.pieces.values()) {
            if (piece.paletteChar == paletteChar && !Objects.equals(piece.variedBlockList, variedBlockList)) {
                piece.variedBlockList = variedBlockList;
                changed = true;
            }
        }

        if (changed) {
            update();
        }
    }

    public void setPaletteName(char paletteChar, String name) {
        boolean changed = false;
        for (PieceData piece : this.pieces.values()) {
            if (piece.paletteChar == paletteChar && !Objects.equals(piece.name, name)) {
                piece.name = name;
                changed = true;
            }
        }

        if (changed) {
            update();
        }
    }

    public void removePiecesWithChar(char paletteChar) {
        List<BlockPos> toRemove = new ArrayList<>();
        for (Map.Entry<BlockPos, PieceData> entry : this.pieces.entrySet()) {
            if (entry.getValue().paletteChar == paletteChar) {
                toRemove.add(entry.getKey());
            }
        }

        for (BlockPos pos : toRemove) {
            removePiece(pos);
        }
    }

    @Nullable
    private VariedBlockList getVariedBlockListsForPaletteChar(char paletteChar, BlockPos ignore) {
        for (Map.Entry<BlockPos, PieceData> entry : this.pieces.entrySet()) {
            if (entry.getKey().equals(ignore))
                continue;

            PieceData piece = entry.getValue();
            if (piece.paletteChar == paletteChar)
                return piece.variedBlockList;
        }

        return null;
    }

    @Nullable
    private String getNameForPaletteChar(char paletteChar, BlockPos ignore) {
        for (Map.Entry<BlockPos, PieceData> entry : this.pieces.entrySet()) {
            if (entry.getKey().equals(ignore))
                continue;

            PieceData piece = entry.getValue();
            if (piece.paletteChar == paletteChar) {
                return piece.name;
            }
        }

        return null;
    }

    public Map<BlockPos, PieceData> getPieces() {
        return ImmutableMap.copyOf(this.pieces);
    }

    public String exportMultiblock() {
        var array = new JsonArray();
        for (PieceData piece : this.pieces.values()) {
            array.add(PieceData.CODEC.encodeStart(JsonOps.INSTANCE, piece)
                    .result()
                    .orElseThrow()
            );
        }

        return array.toString();
    }
}
