package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.util.ExtraCodecs;
import net.minecraft.block.BlockState;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class MultiblockPieceBlockEntity extends UpdatableBlockEntity {
    private BlockPos designerPos;
    private char key = ' ';

    public MultiblockPieceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.MULTIBLOCK_PIECE, pos, state);
    }

    public void setDesignerPos(BlockPos pos) {
        this.designerPos = pos;
        update();
    }

    @Nullable
    public BlockPos getDesignerPos() {
        return this.designerPos;
    }

    public void setKey(char key) {
        this.key = key;
        update();
    }

    public char getKey() {
        return this.key;
    }

    @Override
    protected void writeData(WriteView view) {
        if (this.designerPos != null) {
            view.put("Designer", BlockPos.CODEC, this.designerPos);
        }

        view.put("Key", ExtraCodecs.CHAR_CODEC, this.key);
    }

    @Override
    protected void readData(ReadView view) {
        this.designerPos = view.read("Designer", BlockPos.CODEC).orElse(null);
        this.key = view.read("Key", ExtraCodecs.CHAR_CODEC).orElse(' ');
    }
}