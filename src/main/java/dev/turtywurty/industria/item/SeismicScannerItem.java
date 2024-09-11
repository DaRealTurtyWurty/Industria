package dev.turtywurty.industria.item;

import dev.turtywurty.industria.init.AttachmentTypeInit;
import dev.turtywurty.industria.init.FluidInit;
import dev.turtywurty.industria.network.OpenSeismicScannerPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SeismicScannerItem extends Item {
    public SeismicScannerItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient)
            return super.use(world, user, hand);

        ItemStack stack = user.getStackInHand(hand);
        ServerPlayNetworking.send((ServerPlayerEntity) user, new OpenSeismicScannerPayload(stack));

        return TypedActionResult.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (selected && entity instanceof PlayerEntity player) {
            Chunk chunk = world.getChunk(player.getBlockPos());
            if (chunk != null) {
                Map<BlockPos, FluidState> fluidMap = chunk.getAttachedOrGet(AttachmentTypeInit.FLUID_MAP_ATTACHMENT, HashMap::new)
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().isOf(FluidInit.CRUDE_OIL))
                        .map(entry -> {
                            String posStr = entry.getKey();
                            String[] split = posStr.split(",");
                            return Map.entry(new BlockPos(Integer.parseInt(split[0].strip()), Integer.parseInt(split[1].strip()), Integer.parseInt(split[2].strip())), entry.getValue());
                        })
                        .filter(entry -> entry.getKey().getY() < player.getBlockPos().getY())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                if (!fluidMap.isEmpty()) {
                    List<Text> fluidsBelow = fluidMap.values()
                            .stream()
                            .map(fluidState -> fluidState.getRegistryEntry().getKey())
                            .distinct()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(registryKey -> Text.translatable(registryKey.getValue().toTranslationKey()))
                            .collect(Collectors.toList());

                    MutableText text = Text.literal("Fluids below you:");
                    fluidsBelow.forEach(text::append);

                    player.sendMessage(text, false);
                }
            }
        }
    }
}
