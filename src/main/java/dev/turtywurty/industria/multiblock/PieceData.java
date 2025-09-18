package dev.turtywurty.industria.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.util.ExtraCodecs;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class PieceData {
    public final BlockPos position;
    public BlockPredicate predicate;
    public char paletteChar;
    public final List<PortRule> portRules = new ArrayList<>();

    public static final Codec<PieceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("position").forGetter(data -> data.position),
            BlockPredicate.CODEC.fieldOf("predicate").forGetter(data -> data.predicate),
            ExtraCodecs.CHAR_CODEC.fieldOf("paletteChar").forGetter(data -> data.paletteChar),
            PortRule.CODEC.listOf().fieldOf("portRules").forGetter(data -> data.portRules)
    ).apply(instance, PieceData::new));

    public PieceData(BlockPos position, BlockPredicate predicate, char paletteChar, List<PortRule> portRules) {
        this.position = position;
        this.predicate = predicate;
        this.paletteChar = paletteChar;
        this.portRules.addAll(portRules);
    }

    public PieceData(BlockPos position, BlockPredicate predicate, char paletteChar) {
        this.position = position;
        this.predicate = predicate;
        this.paletteChar = paletteChar;
    }

    public PieceData(BlockPos position) {
        this.position = position;
        this.predicate = BlockPredicate.Builder.create().build();
        this.paletteChar = ' ';
    }
}
