package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.SteamBoilerBlockEntity;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.menu.base.IndustriaScreenHandler;
import dev.turtywurty.industria.menu.slot.PredicateSlot;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.industria.util.FuelUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;

public class SteamBoilerMenu extends IndustriaScreenHandler<SteamBoilerBlockEntity, BlockPosPayload> {
    private static final int WATER_AMOUNT = 0;
    private static final int STEAM_AMOUNT = 1;
    private static final int TEMPERATURE = 2;
    private static final int TARGET_TEMPERATURE = 3;
    private static final int BURN_PROGRESS = 4;
    private static final int STEAM_PRODUCTION_RATE = 5;
    private static final int BURN_RATE = 6;
    private static final int DATA_COUNT = 7;
    private static final int BURN_PROGRESS_SCALE = 1000;
    private static final int RATE_SCALE = 100;

    public SteamBoilerMenu(int syncId, Inventory inventory, BlockPosPayload payload) {
        super(ModMenuTypes.STEAM_BOILER.get(), DATA_COUNT, syncId, inventory, payload, SteamBoilerBlockEntity.class);
    }

    public SteamBoilerMenu(int syncId, Inventory inventory, SteamBoilerBlockEntity boiler) {
        super(ModMenuTypes.STEAM_BOILER.get(), syncId, inventory, boiler, boiler.getWrappedContainerStorage(),
            new ContainerData() {
                @Override
                public int get(int index) {
                    return switch (index) {
                        case WATER_AMOUNT -> toMillibuckets(boiler.getWaterStorage().getAmount());
                        case STEAM_AMOUNT -> toMillibuckets(boiler.getSteamStorage().getAmount());
                        case TEMPERATURE -> Math.round(boiler.getTemperature());
                        case TARGET_TEMPERATURE -> Math.round(boiler.getTargetTemperature());
                        case BURN_PROGRESS -> Math.round(boiler.getBurnProgress() * BURN_PROGRESS_SCALE);
                        case STEAM_PRODUCTION_RATE -> Math.round(boiler.calculateSteamProductionRate() * RATE_SCALE);
                        case BURN_RATE -> Math.round(boiler.calculateBurnRate() * RATE_SCALE);
                        default -> 0;
                    };
                }

                @Override
                public void set(int index, int value) {
                }

                @Override
                public int getCount() {
                    return DATA_COUNT;
                }
            });
    }

    @Override
    protected int getInventorySize() {
        return 1;
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        addSlot(new PredicateSlot(this.wrappedContainerStorage.getInventory(0), 0, 80, 31, FuelUtils::isFuel));
    }

    @Override
    public boolean stillValid(Player player) {
        return !this.blockEntity.isRemoved() && stillValid(this.context, player, ModBlocks.STEAM_BOILER.get());
    }

    public int getWaterAmount() {
        return this.propertyDelegate.get(WATER_AMOUNT);
    }

    public int getWaterCapacity() {
        return toMillibuckets(this.blockEntity.getWaterStorage().getCapacity());
    }

    public int getSteamAmount() {
        return this.propertyDelegate.get(STEAM_AMOUNT);
    }

    public int getSteamCapacity() {
        return toMillibuckets(this.blockEntity.getSteamStorage().getCapacity());
    }

    public int getTemperature() {
        return this.propertyDelegate.get(TEMPERATURE);
    }

    public int getTargetTemperature() {
        return this.propertyDelegate.get(TARGET_TEMPERATURE);
    }

    public float getBurnProgress() {
        return this.propertyDelegate.get(BURN_PROGRESS) / (float) BURN_PROGRESS_SCALE;
    }

    public float getSteamProductionRate() {
        return this.propertyDelegate.get(STEAM_PRODUCTION_RATE) / (float) RATE_SCALE;
    }

    public float getBurnRate() {
        return this.propertyDelegate.get(BURN_RATE) / (float) RATE_SCALE;
    }

    private static int toMillibuckets(long amount) {
        return (int) (amount * 1000 / FluidAmounts.BUCKET);
    }
}
