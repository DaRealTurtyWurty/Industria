package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.util.ExtraCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    protected void saveAdditional(ValueOutput view) {
        if (this.designerPos != null) {
            view.store("Designer", BlockPos.CODEC, this.designerPos);
        }

        view.store("Key", ExtraCodecs.CHAR_CODEC, this.key);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        this.designerPos = view.read("Designer", BlockPos.CODEC).orElse(null);
        this.key = view.read("Key", ExtraCodecs.CHAR_CODEC).orElse(' ');
    }
}