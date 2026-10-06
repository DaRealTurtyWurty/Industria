package dev.turtywurty.industria.molding;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public record MoldPattern(Identifier id, List<List<MoldPoint>> polygons) {
    public Component getDisplayName() {
        return Component.translatable("mold." + this.id.getNamespace() + "." + this.id.getPath());
    }
}
