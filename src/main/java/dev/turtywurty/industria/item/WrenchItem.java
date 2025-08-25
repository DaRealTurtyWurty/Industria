package dev.turtywurty.industria.item;

import dev.turtywurty.industria.multiblock.MultiblockAssembler;
import dev.turtywurty.industria.multiblock.MultiblockController;
import dev.turtywurty.industria.multiblock.MultiblockDefinition;
import dev.turtywurty.industria.multiblock.MultiblockMatcher;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class WrenchItem extends Item {
    public WrenchItem(Settings settings) {
        super(settings);
    }
}
