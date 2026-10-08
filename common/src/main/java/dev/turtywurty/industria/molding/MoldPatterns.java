package dev.turtywurty.industria.molding;

import dev.turtywurty.industria.Industria;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MoldPatterns {
    private MoldPatterns() {
    }

    private static final MoldTextureCutout INGOT_CUTOUT = new MoldTextureCutout(
            Industria.id("textures/item/steel_ingot.png"), 0, 0, 1, 1);

    public static final MoldPattern INGOT = new MoldPattern(
            Industria.id("ingot"),
            List.of(
                    List.of(
                            INGOT_CUTOUT.point(0.625f, 0.125f),
                            INGOT_CUTOUT.point(0.75f, 0.125f),
                            INGOT_CUTOUT.point(1, 0.375f),
                            INGOT_CUTOUT.point(1, 0.625f),
                            INGOT_CUTOUT.point(0.375f, 0.875f),
                            INGOT_CUTOUT.point(0.1875f, 0.875f),
                            INGOT_CUTOUT.point(0, 0.625f),
                            INGOT_CUTOUT.point(0, 0.375f)
                    )
            ),
            Map.of(0, INGOT_CUTOUT),
            true
    );

    public static final MoldPattern ROD = new MoldPattern(
            Industria.id("rod"),
            List.of(
                    List.of(
                            new MoldPoint(0.4f, 0.15f),
                            new MoldPoint(0.6f, 0.15f),
                            new MoldPoint(0.6f, 0.85f),
                            new MoldPoint(0.4f, 0.85f)
                    )
            )
    );

    public static final List<MoldPattern> ALL = List.of(INGOT, ROD);

    public static Optional<MoldPattern> find(Identifier id) {
        return ALL.stream().filter(pattern -> pattern.id().equals(id)).findFirst();
    }
}
