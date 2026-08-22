package dev.turtywurty.industria.mixin;

import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(ModelPart.class)
public interface ModelPartAccessor {
    @Accessor("children")
    Map<String, ModelPart> industria$getChildren();

    @Accessor("cubes")
    List<ModelPart.Cube> industria$getCubes();

    @Invoker("addAllChildren")
    void industria$addAllChildren(BiConsumer<String, ModelPart> output);
}
