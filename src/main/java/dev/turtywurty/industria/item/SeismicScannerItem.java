package dev.turtywurty.industria.item;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.component.FluidPocketsComponent;
import dev.turtywurty.industria.init.ComponentTypeInit;
import dev.turtywurty.industria.network.OpenSeismicScannerPayload;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeismicScannerItem extends Item {
    public static final Component TITLE = Component.translatable("screen." + Industria.MOD_ID + ".seismic_scanner");

    public SeismicScannerItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (world.isClientSide())
            return super.use(world, user, hand);

        ItemStack stack = user.getItemInHand(hand);
        if (user.isShiftKeyDown()) {
            WorldFluidPocketsState state = WorldFluidPocketsState.getServerState((ServerLevel) world);
            List<WorldFluidPocketsState.FluidPocket> existsBelow = state.existsBelow(user.blockPosition());
            if (existsBelow.isEmpty()) {
                user.displayClientMessage(Component.literal("There are no fluid fluidPockets here!"), false); // TODO: Translatable
                return InteractionResult.FAIL;
            }

            stack.set(ComponentTypeInit.FLUID_POCKETS, new FluidPocketsComponent(existsBelow));
            return InteractionResult.SUCCESS;
        }

        ServerPlayNetworking.send((ServerPlayer) user, new OpenSeismicScannerPayload(stack));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        if (!(entity instanceof Player player))
            return;

        boolean selected = player.getItemBySlot(EquipmentSlot.MAINHAND).is(this) ||
                player.getItemBySlot(EquipmentSlot.OFFHAND).is(this);
        if (selected) {
            WorldFluidPocketsState state = WorldFluidPocketsState.getServerState(world);
            List<Component> fluidBelow = new ArrayList<>();
            for (WorldFluidPocketsState.FluidPocket fluidPocket : state.existsBelow(player.blockPosition())) {
                Optional<ResourceKey<Fluid>> regKey = fluidPocket.fluidState().typeHolder().unwrapKey();
                regKey.ifPresent(key -> fluidBelow.add(Component.translatable(key.identifier().toLanguageKey()).append(": " + fluidPocket.minY())));
            }

            if (fluidBelow.isEmpty())
                return;

            MutableComponent text = Component.literal("Fluids below: "); // TODO: Translatable
            for (int index = 0; index < fluidBelow.size(); index++) {
                text.append(fluidBelow.get(index));
                if (index != fluidBelow.size() - 1) {
                    text.append(Component.literal(", "));
                }
            }

            player.displayClientMessage(text, false);
        }
    }
}
