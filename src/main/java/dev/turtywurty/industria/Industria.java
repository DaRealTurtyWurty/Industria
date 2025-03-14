package dev.turtywurty.industria;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.turtywurty.fabricslurryapi.api.storage.SlurryStorage;
import dev.turtywurty.industria.block.MultiblockBlock;
import dev.turtywurty.industria.blockentity.*;
import dev.turtywurty.industria.command.ConfigCommand;
import dev.turtywurty.industria.config.ServerConfig;
import dev.turtywurty.industria.fluid.FluidData;
import dev.turtywurty.industria.init.*;
import dev.turtywurty.industria.init.list.TagList;
import dev.turtywurty.industria.init.worldgen.BiomeModificationInit;
import dev.turtywurty.industria.init.worldgen.FeatureInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.network.*;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import dev.turtywurty.industria.screenhandler.BatteryScreenHandler;
import dev.turtywurty.industria.screenhandler.DrillScreenHandler;
import dev.turtywurty.industria.screenhandler.MotorScreenHandler;
import dev.turtywurty.industria.util.ExtraPacketCodecs;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.reborn.energy.api.EnergyStorage;

public class Industria implements ModInitializer {
    public static final String MOD_ID = "industria";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static Text containerTitle(String name) {
        return Text.translatable("container." + MOD_ID + "." + name);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Loading Industria...");

        // Registries
        IndustriaRegistries.init();
        ItemInit.init();
        BlockInit.init();
        BlockEntityTypeInit.init();
        ScreenHandlerTypeInit.init();
        RecipeTypeInit.init();
        RecipeSerializerInit.init();
        ItemGroupInit.init();
        BiomeModificationInit.init();
        FeatureInit.init();
        FluidInit.init();
        AttachmentTypeInit.init();
        PositionSourceTypeInit.init();
        ComponentTypeInit.init();
        EntityTypeInit.init();
        RecipeBookCategoryInit.init();
        MultiblockTypeInit.init();
        SlurryInit.init();

        ExtraPacketCodecs.registerDefaults();

        // Item Lookup
        ItemStorage.SIDED.registerForBlockEntity(AlloyFurnaceBlockEntity::getInventoryProvider, BlockEntityTypeInit.ALLOY_FURNACE);

        EnergyStorage.SIDED.registerForBlockEntity(ThermalGeneratorBlockEntity::getEnergyProvider, BlockEntityTypeInit.THERMAL_GENERATOR);
        FluidStorage.SIDED.registerForBlockEntity(ThermalGeneratorBlockEntity::getFluidProvider, BlockEntityTypeInit.THERMAL_GENERATOR);
        ItemStorage.SIDED.registerForBlockEntity(ThermalGeneratorBlockEntity::getInventoryProvider, BlockEntityTypeInit.THERMAL_GENERATOR);

        EnergyStorage.SIDED.registerForBlockEntity(BatteryBlockEntity::getEnergyProvider, BlockEntityTypeInit.BATTERY);
        ItemStorage.SIDED.registerForBlockEntity(BatteryBlockEntity::getInventoryProvider, BlockEntityTypeInit.BATTERY);

        EnergyStorage.SIDED.registerForBlockEntity(CombustionGeneratorBlockEntity::getEnergyProvider, BlockEntityTypeInit.COMBUSTION_GENERATOR);
        ItemStorage.SIDED.registerForBlockEntity(CombustionGeneratorBlockEntity::getInventoryProvider, BlockEntityTypeInit.COMBUSTION_GENERATOR);

        EnergyStorage.SIDED.registerForBlockEntity(SolarPanelBlockEntity::getEnergyProvider, BlockEntityTypeInit.SOLAR_PANEL);

        EnergyStorage.SIDED.registerForBlockEntity(CrusherBlockEntity::getEnergyProvider, BlockEntityTypeInit.CRUSHER);
        ItemStorage.SIDED.registerForBlockEntity(CrusherBlockEntity::getInventoryProvider, BlockEntityTypeInit.CRUSHER);

        EnergyStorage.SIDED.registerForBlockEntity(CableBlockEntity::getStorageProvider, BlockEntityTypeInit.CABLE);
        FluidStorage.SIDED.registerForBlockEntity(FluidPipeBlockEntity::getStorageProvider, BlockEntityTypeInit.FLUID_PIPE);
        SlurryStorage.SIDED.registerForBlockEntity(SlurryPipeBlockEntity::getStorageProvider, BlockEntityTypeInit.SLURRY_PIPE);

        EnergyStorage.SIDED.registerForBlockEntity(WindTurbineBlockEntity::getEnergyProvider, BlockEntityTypeInit.WIND_TURBINE);

        EnergyStorage.SIDED.registerForBlockEntity(MotorBlockEntity::getEnergyProvider, BlockEntityTypeInit.MOTOR);

        EnergyStorage.SIDED.registerForBlockEntity(ElectricFurnaceBlockEntity::getEnergyProvider, BlockEntityTypeInit.ELECTRIC_FURNACE);
        ItemStorage.SIDED.registerForBlockEntity(ElectricFurnaceBlockEntity::getInventoryProvider, BlockEntityTypeInit.ELECTRIC_FURNACE);

        FluidStorage.SIDED.registerForBlockEntity(FractionalDistillationControllerBlockEntity::getFluidProvider, BlockEntityTypeInit.FRACTIONAL_DISTILLATION_CONTROLLER);
        FluidStorage.SIDED.registerForBlockEntity(FractionalDistillationTowerBlockEntity::getFluidProvider, BlockEntityTypeInit.FRACTIONAL_DISTILLATION_TOWER);

        EnergyStorage.SIDED.registerForBlockEntity(InductionHeaterBlockEntity::getEnergyProvider, BlockEntityTypeInit.INDUCTION_HEATER);

        FluidStorage.SIDED.registerForBlockEntity(FluidPumpBlockEntity::getFluidProvider, BlockEntityTypeInit.FLUID_PUMP);
        EnergyStorage.SIDED.registerForBlockEntity(FluidPumpBlockEntity::getEnergyProvider, BlockEntityTypeInit.FLUID_PUMP);

        ItemStorage.SIDED.registerForBlockEntity(MixerBlockEntity::getInventoryProvider, BlockEntityTypeInit.MIXER);
        FluidStorage.SIDED.registerForBlockEntity(MixerBlockEntity::getFluidProvider, BlockEntityTypeInit.MIXER);
        EnergyStorage.SIDED.registerForBlockEntity(MixerBlockEntity::getEnergyProvider, BlockEntityTypeInit.MIXER);
        SlurryStorage.SIDED.registerForBlockEntity(MixerBlockEntity::getSlurryProvider, BlockEntityTypeInit.MIXER);

        EnergyStorage.SIDED.registerForBlockEntity(DigesterBlockEntity::getEnergyProvider, BlockEntityTypeInit.DIGESTER);
        SlurryStorage.SIDED.registerForBlockEntity(DigesterBlockEntity::getSlurryProvider, BlockEntityTypeInit.DIGESTER);
        FluidStorage.SIDED.registerForBlockEntity(DigesterBlockEntity::getFluidProvider, BlockEntityTypeInit.DIGESTER);

        FluidStorage.SIDED.registerForBlockEntity(ClarifierBlockEntity::getFluidProvider, BlockEntityTypeInit.CLARIFIER);
        ItemStorage.SIDED.registerForBlockEntity(ClarifierBlockEntity::getInventoryProvider, BlockEntityTypeInit.CLARIFIER);

        for (TransferType<?, ?> transferType : TransferType.getValues()) {
            transferType.registerForMultiblockIo();
        }

        // Payloads
        PayloadTypeRegistry.playC2S().register(BatteryChargeModePayload.ID, BatteryChargeModePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenSeismicScannerPayload.ID, OpenSeismicScannerPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncFluidPocketsPayload.ID, SyncFluidPocketsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ChangeDrillingPayload.ID, ChangeDrillingPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RetractDrillPayload.ID, RetractDrillPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ChangeDrillOverflowModePayload.ID, ChangeDrillOverflowModePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetMotorTargetRPMPayload.ID, SetMotorTargetRPMPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UpgradeStationUpdateRecipesPayload.ID, UpgradeStationUpdateRecipesPayload.CODEC);

        // Packets
        ServerPlayNetworking.registerGlobalReceiver(BatteryChargeModePayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    ServerPlayerEntity player = context.player();
                    ScreenHandler handler = player.currentScreenHandler;
                    if (handler instanceof BatteryScreenHandler batteryScreenHandler) {
                        batteryScreenHandler.getBlockEntity().setChargeMode(payload.chargeMode());
                    }
                }));

        ServerPlayNetworking.registerGlobalReceiver(ChangeDrillingPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (player.currentScreenHandler instanceof DrillScreenHandler handler) {
                DrillBlockEntity blockEntity = handler.getBlockEntity();
                blockEntity.setDrilling(payload.drilling());
                blockEntity.update();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(RetractDrillPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (player.currentScreenHandler instanceof DrillScreenHandler handler) {
                DrillBlockEntity blockEntity = handler.getBlockEntity();
                blockEntity.setDrilling(false);
                blockEntity.setRetracting(true);
                blockEntity.update();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(ChangeDrillOverflowModePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (player.currentScreenHandler instanceof DrillScreenHandler handler) {
                DrillBlockEntity blockEntity = handler.getBlockEntity();
                blockEntity.setOverflowMethod(payload.overflowMethod());
                blockEntity.update();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetMotorTargetRPMPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (player.currentScreenHandler instanceof MotorScreenHandler handler) {
                MotorBlockEntity blockEntity = handler.getBlockEntity();
                blockEntity.setTargetRotationSpeed(payload.targetRPM() / 60f);
                blockEntity.update();
            } else if (player.currentScreenHandler instanceof DrillScreenHandler handler) {
                DrillBlockEntity blockEntity = handler.getBlockEntity();
                blockEntity.setTargetRotationSpeed(payload.targetRPM() / 60f);
                blockEntity.update();
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            sender.sendPacket(WorldFluidPocketsState.createSyncPacket(handler.player.getServerWorld()));
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerWorld serverWorld))
                return;

            WorldFluidPocketsState serverState = WorldFluidPocketsState.getServerState(serverWorld);
            if (serverState.removePosition(pos)) {
                WorldFluidPocketsState.sync(serverWorld);
            }
        });

        ServerWorldEvents.LOAD.register((server, world) -> ServerConfig.onServerLoad(server));
        ServerWorldEvents.UNLOAD.register((server, world) -> ServerConfig.onServerSave(server));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal(Industria.MOD_ID)
                            .requires(source -> source.hasPermissionLevel(3))
                            .then(CommandManager.literal("config").then(ConfigCommand.register()))
            );
        });

        ServerTickEvents.START_WORLD_TICK.register(world -> MultiblockBlock.SHAPE_CACHE.clear());

        // Fluid Properties
        var crudeOilAttributes = new FluidVariantAttributeHandler() {
            @Override
            public int getViscosity(FluidVariant variant, @Nullable World world) {
                return 7500;
            }
        };

        FluidVariantAttributes.register(FluidInit.CRUDE_OIL, crudeOilAttributes);
        FluidVariantAttributes.register(FluidInit.CRUDE_OIL_FLOWING, crudeOilAttributes);

        // Fluid Data
        var crudeOilData = new FluidData.Builder(TagList.Fluids.CRUDE_OIL)
                .preventsBlockSpreading()
                .canSwim()
                .fluidMovementSpeed((entity, speed) -> 0.01F)
                .applyWaterMovement()
                .applyBuoyancy(itemEntity -> itemEntity.setVelocity(itemEntity.getVelocity().add(0.0D, 0.01D, 0.0D)))
                .canCauseDrowning()
                .shouldWitchDrinkWaterBreathing()
                .affectsBlockBreakSpeed()
                .bubbleParticle(ParticleTypes.ASH)
                .splashParticle(ParticleTypes.HEART)
                .build();

        FluidData.registerFluidData(FluidInit.CRUDE_OIL, crudeOilData);
        FluidData.registerFluidData(FluidInit.CRUDE_OIL_FLOWING, crudeOilData);

        LOGGER.info("Industria has finished loading!");
    }
}