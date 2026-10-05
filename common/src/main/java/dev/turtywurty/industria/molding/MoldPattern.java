package dev.turtywurty.industria.molding;

import net.minecraft.resources.Identifier;

import java.util.List;

public record MoldPattern(Identifier id, List<List<MoldPoint>> polygons) {
}
