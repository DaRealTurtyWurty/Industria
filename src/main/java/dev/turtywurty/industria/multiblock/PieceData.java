package dev.turtywurty.industria.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.util.ExtraCodecs;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PieceData {
    public final BlockPos position;
    public VariedBlockList variedBlockList;
    public char paletteChar;
    public String name;
    public final List<PortRule> portRules = new ArrayList<>();

    public static final Codec<PieceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("position").forGetter(data -> data.position),
            VariedBlockList.CODEC.fieldOf("blockList").forGetter(data -> data.variedBlockList),
            ExtraCodecs.CHAR_CODEC.fieldOf("paletteChar").forGetter(data -> data.paletteChar),
            PortRule.CODEC.listOf().fieldOf("portRules").forGetter(data -> data.portRules),
            Codec.STRING.optionalFieldOf("name", "Untitled").forGetter(data -> data.name)
    ).apply(instance, PieceData::new));

    public PieceData(BlockPos position, VariedBlockList variedBlockList, char paletteChar, List<PortRule> portRules, String name) {
        this.position = position;
        this.variedBlockList = variedBlockList;
        this.paletteChar = paletteChar;
        this.name = Objects.requireNonNullElse(name, "Untitled");
        this.portRules.addAll(portRules);
    }

    public PieceData(BlockPos position, VariedBlockList variedBlockList, char paletteChar) {
        this.position = position;
        this.variedBlockList = variedBlockList;
        this.paletteChar = paletteChar;
        this.name = "Untitled";
    }

    public PieceData(BlockPos position) {
        this.position = position;
        this.variedBlockList = new VariedBlockList();
        this.paletteChar = ' ';
        this.name = "Untitled";
    }
}
