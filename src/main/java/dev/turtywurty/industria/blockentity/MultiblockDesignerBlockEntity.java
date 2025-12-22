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
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

        if (this.world != null && this.world.getBlockEntity(position) instanceof MultiblockPieceBlockEntity piece) {
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
