package dev.turtywurty.industria.molding;

import dev.turtywurty.industria.Industria;

import java.util.List;

public final class MoldPatterns {
    private MoldPatterns() {
    }

    public static final MoldPattern INGOT = new MoldPattern(
            Industria.id("ingot"),
            List.of(
                    List.of(
                            new MoldPoint(0.2f, 0.3f),
                            new MoldPoint(0.8f, 0.3f),
                            new MoldPoint(0.8f, 0.7f),
                            new MoldPoint(0.2f, 0.7f)
                    )
            )
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
}
