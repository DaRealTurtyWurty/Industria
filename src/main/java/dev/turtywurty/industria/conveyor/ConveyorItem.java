package dev.turtywurty.industria.conveyor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ConveyorItem {
    public static final MapCodec<ConveyorItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(ConveyorItem::getId),
            ItemStack.CODEC.fieldOf("stack").forGetter(ConveyorItem::getStack),
            BlockPos.CODEC.fieldOf("position").forGetter(ConveyorItem::getPosition),
            Codec.INT.fieldOf("progress").forGetter(ConveyorItem::getProgress)
    ).apply(instance, (id, stack, position, progress) -> {
        var item = new ConveyorItem(id);
        item.setStack(stack);
        item.setPosition(position);
        item.setProgress(progress);
        return item;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConveyorItem> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, ConveyorItem::getId,
            ItemStack.STREAM_CODEC, ConveyorItem::getStack,
            BlockPos.STREAM_CODEC, ConveyorItem::getPosition,
            ByteBufCodecs.INT, ConveyorItem::getProgress,
            (id, stack, position, progress) -> {
                var item = new ConveyorItem(id);
                item.setStack(stack);
                item.setPosition(position);
                item.setProgress(progress);
                return item;
            }
    );

    private final UUID id;
    private ItemStack stack;
    private BlockPos position;
    private int progress;

    public ConveyorItem(UUID id) {
        this.id = id;
    }

    public ConveyorItem(BlockPos position, ItemStack stack) {
        this(UUID.randomUUID());
        this.position = position;
        this.stack = stack;
    }

    public UUID getId() {
        return id;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public BlockPos getPosition() {
        return position;
    }

    public void setPosition(BlockPos position) {
        this.position = position;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
