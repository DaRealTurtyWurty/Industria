package dev.turtywurty.industria.mixin;

import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(BeehiveBlockEntity.class)
public interface BeehiveBlockEntityAccessor {
    @Accessor("IGNORED_BEE_TAGS")
    static List<String> getIgnoredBeeTags() {
        throw new AssertionError();
    }
}
