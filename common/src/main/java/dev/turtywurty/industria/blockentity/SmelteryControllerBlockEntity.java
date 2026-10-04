package dev.turtywurty.industria.blockentity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.block.abstraction.BlockEntityWithGui;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import dev.turtywurty.industria.init.ModBlockEntityTypes;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModRecipeTypes;
import dev.turtywurty.industria.menu.SmelteryControllerMenu;
import dev.turtywurty.industria.metal.MetalDefinition;
import dev.turtywurty.industria.metal.MetalRegistry;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.recipe.SmelteryRecipe;
import dev.turtywurty.industria.recipe.input.SingleItemStackRecipeInput;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.FluidAmounts;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.serialization.StorageSnapshot;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("DataFlowIssue")
public class SmelteryControllerBlockEntity extends IndustriaBlockEntity implements TickableBlockEntity, BlockEntityWithGui<BlockPosPayload> {
    public static final Component TITLE = Industria.containerTitle("smeltery_controller");

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float AMBIENT_TEMPERATURE = 20.0F;
    private static final float MAX_TEMPERATURE = 4000.0F;
    private static final float HEATING_PER_TICK = 0.5F;
    private static final float COOLING_PER_TICK = 0.1F;
    private final Codec<List<SmelteryChamber>> chambersCodec = SmelteryChamber.codec(this).listOf();

    private final List<SmelteryChamber> chambers = new ArrayList<>();
    private final List<RetainedChamberFluid> retainedFluids = new ArrayList<>();
    private BlockPos fireBoxPos = null;
    private boolean structureReady;
    private float temperature = AMBIENT_TEMPERATURE;
    private boolean keepMolten = false;
    private int fuelSpaceSize;

    public SmelteryControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.SMELTERY_CONTROLLER.get(), ModBlockEntityTypes.SMELTERY_CONTROLLER.get(), pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Chambers", this.chambersCodec, this.chambers);
        output.storeNullable("FireBoxPos", BlockPos.CODEC, this.fireBoxPos);
        output.store("RetainedFluids", RetainedChamberFluid.CODEC.listOf(), this.retainedFluids);
        output.putFloat("Temperature", this.temperature);
        output.putBoolean("KeepMolten", this.keepMolten);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.structureReady = false;
        this.fuelSpaceSize = 0;

        this.chambers.clear();
        this.chambers.addAll(input.read("Chambers", this.chambersCodec).orElseGet(List::of));

        this.fireBoxPos = input.read("FireBoxPos", BlockPos.CODEC).orElse(null);

        this.retainedFluids.clear();
        this.retainedFluids.addAll(input.read("RetainedFluids", RetainedChamberFluid.CODEC.listOf()).orElseGet(List::of));

        this.temperature = Mth.clamp(input.getFloatOr("Temperature", AMBIENT_TEMPERATURE), AMBIENT_TEMPERATURE, MAX_TEMPERATURE);
        this.keepMolten = input.getBooleanOr("KeepMolten", false);
    }

    @Override
    public void tick() {
        if (!hasLevel() || this.level.isClientSide())
            return;

        if (this.level.getGameTime() % 20 == 0) {
            validateStructure();
        }

        float targetTemperature = getTargetTemperature();
        boolean heating = false;
        if (this.structureReady
                && this.fireBoxPos != null
                && this.level.isLoaded(this.fireBoxPos)
                && this.level.getBlockEntity(this.fireBoxPos) instanceof FireboxBlockEntity firebox
                && targetTemperature > AMBIENT_TEMPERATURE
                && this.temperature <= targetTemperature
        ) {
            firebox.setFuelSpaceSize(this.fuelSpaceSize);
            heating = firebox.burnForTick();
        }

        updateTemperature(heating, targetTemperature);
        processChambers();
    }

    private void updateTemperature(boolean heating, float targetTemperature) {
        float previousTemperature = this.temperature;
        if (heating) {
            this.temperature = Math.min(this.temperature + HEATING_PER_TICK, targetTemperature);
        } else {
            this.temperature = Math.max(this.temperature - COOLING_PER_TICK, AMBIENT_TEMPERATURE);
        }

        if (previousTemperature != this.temperature) {
            setChanged();
        }
    }

    public boolean isStructureReady() {
        return this.structureReady;
    }

    public List<SmelteryControllerMenu.SmelteryChamberInfo> getChamberInfos() {
        if (!this.structureReady)
            return List.of();

        List<SmelteryControllerMenu.SmelteryChamberInfo> infos = new ArrayList<>();
        for (SmelteryChamber chamber : this.chambers) {
            AABB boundingBox = chamber.getBoundingBox();
            SyncingFluidStorage storage = chamber.getFluidStorage();
            int width = (int) boundingBox.getXsize();
            int height = (int) boundingBox.getYsize();
            int depth = (int) boundingBox.getZsize();

            var fluidStack = new FluidStack(storage.getResource(), storage.getAmount());
            infos.add(new SmelteryControllerMenu.SmelteryChamberInfo(
                    chamber.hatchPos,
                    width, height, depth,
                    fluidStack,
                    storage.getCapacity(),
                    getRequiredTemperatureForFluid(fluidStack.variant()),
                    getSolidBlockForFluid(fluidStack.variant())
            ));
        }

        return List.copyOf(infos);
    }

    public float getTemperature() {
        return this.temperature;
    }

    public int getFuelSpaceSize() {
        return this.fuelSpaceSize;
    }

    private boolean isValidMeltingRecipe(SmelteryRecipe recipe) {
        return recipe.processTime() > 0
                && !recipe.output().isEmpty()
                && recipe.input().stackData().count() > 0
                && recipe.requiredTemperature() >= AMBIENT_TEMPERATURE
                && recipe.preferredTemperature() > recipe.requiredTemperature()
                && recipe.preferredTemperature() <= MAX_TEMPERATURE
                && Float.isFinite(recipe.maxSpeedMultiplier())
                && recipe.maxSpeedMultiplier() >= 1.0F;
    }

    public Optional<RecipeHolder<SmelteryRecipe>> getMatchingRecipe(ItemStack stack) {
        if (!(this.level instanceof ServerLevel serverLevel))
            return Optional.empty();

        return serverLevel.recipeAccess().getRecipeFor(ModRecipeTypes.SMELTERY.get(), SingleItemStackRecipeInput.of(stack), this.level);
    }

    public float getRequiredTemperatureForFluid(ResourceVariant<Fluid> fluid) {
        if (fluid.isBlank())
            return -1f;

        Optional<MetalDefinition> metalDefinition = MetalRegistry.findBy(fluid.value());
        return metalDefinition.map(MetalDefinition::meltingTemperature).orElse(-1f);
    }

    public BlockState getSolidBlockForFluid(ResourceVariant<Fluid> fluid) {
        if (fluid.isBlank())
            return Blocks.AIR.defaultBlockState();

        Optional<MetalDefinition> optMetalDefinition = MetalRegistry.findBy(fluid.value());
        return optMetalDefinition.map(MetalDefinition::storageBlock)
                .map(Supplier::get)
                .map(Block::defaultBlockState)
                .orElseGet(Blocks.AIR::defaultBlockState);
    }

    public boolean canTapOutput(BlockPos outletPos) {
        SmelteryChamber chamber = getChamberForOutlet(outletPos);
        if (chamber == null)
            return false;

        SyncingFluidStorage fluidStorage = chamber.getFluidStorage();
        if (fluidStorage.getAmount() <= 0 || fluidStorage.getResource().isBlank())
            return false;

        float requiredTemperature = getRequiredTemperatureForFluid(fluidStorage.getResource());
        return requiredTemperature >= 0 && this.temperature >= requiredTemperature;
    }

    public boolean isKeepingMolten() {
        return this.keepMolten;
    }

    public void setKeepMolten(boolean keepMolten) {
        if (this.keepMolten != keepMolten) {
            this.keepMolten = keepMolten;
            setChanged();
        }
    }

    private float getTargetTemperature() {
        float target = AMBIENT_TEMPERATURE;
        if (!this.structureReady)
            return target;

        for (SmelteryChamber chamber : this.chambers) {
            if (this.keepMolten) {
                FluidStack fluidStack = chamber.getFluidStorage().getFluidStack();
                if (!fluidStack.isEmpty()) {
                    float requiredTemperature = getRequiredTemperatureForFluid(fluidStack.variant());
                    if (requiredTemperature > 0) {
                        target = Math.clamp(requiredTemperature + 5.0f, target, MAX_TEMPERATURE); // Add a small buffer to ensure the fluid stays molten
                    }
                }
            }

            if (chamber.hatchPos == null
                    || !this.level.isLoaded(chamber.hatchPos)
                    || !(this.level.getBlockEntity(chamber.hatchPos) instanceof ChargingHatchBlockEntity hatch))
                continue;

            int processingSlots = Math.min(hatch.getProcessingSlotCount(), hatch.getInventory().getContainerSize());
            for (int slot = 0; slot < hatch.getInventory().getContainerSize() && slot < processingSlots; slot++) {
                ItemStack stack = hatch.getInventory().getItem(slot);
                if (stack.isEmpty())
                    continue;

                Optional<RecipeHolder<SmelteryRecipe>> matchingRecipe = getMatchingRecipe(stack);
                if (matchingRecipe.isEmpty())
                    continue;

                SmelteryRecipe recipe = matchingRecipe.get().value();
                if (!isValidMeltingRecipe(recipe)
                        || !recipe.input().testForRecipe(stack)
                        || !chamber.getFluidStorage().canInsert(recipe.output()))
                    continue;

                target = Math.max(target, recipe.preferredTemperature());
            }
        }

        return target;
    }

    private void processChambers() {
        if (!this.structureReady)
            return;

        for (SmelteryChamber chamber : this.chambers) {
            if (chamber.hatchPos == null || !this.level.isLoaded(chamber.hatchPos))
                continue;

            if (this.level.getBlockEntity(chamber.hatchPos) instanceof ChargingHatchBlockEntity hatch) {
                processChamber(hatch, chamber);
            }
        }
    }

    private void processChamber(ChargingHatchBlockEntity hatch, SmelteryChamber chamber) {
        int processingSlots = Math.min(hatch.getProcessingSlotCount(), hatch.getInventory().getContainerSize());
        for (int slot = 0; slot < hatch.getInventory().getContainerSize() && slot < processingSlots; slot++) {
            processSlot(hatch, chamber, slot);
        }
    }

    private void processSlot(ChargingHatchBlockEntity hatch, SmelteryChamber chamber, int slot) {
        if (slot < 0 || slot >= hatch.getProcessingSlotCount())
            return;

        ItemStack stack = hatch.getInventory().getItem(slot);
        if (stack.isEmpty()) {
            hatch.resetMeltingProgress(slot);
            return;
        }

        Optional<RecipeHolder<SmelteryRecipe>> matchingRecipe = getMatchingRecipe(hatch.getInventory().getItem(slot));
        if (matchingRecipe.isEmpty()) {
            hatch.resetMeltingProgress(slot);
            return;
        }

        SmelteryRecipe recipe = matchingRecipe.get().value();
        if (!isValidMeltingRecipe(recipe) || !recipe.input().testForRecipe(stack)) {
            hatch.resetMeltingProgress(slot);
            return;
        }

        hatch.prepareMeltingSlot(slot, recipe.processTime());
        if (this.temperature < recipe.requiredTemperature()
                || !chamber.getFluidStorage().canInsert(recipe.output()))
            return;

        if (hatch.getMeltingProgress(slot) < recipe.processTime()) {
            hatch.advanceMeltingProgress(slot, recipe.getSpeedMultiplier(this.temperature));
        }

        if (hatch.getMeltingProgress(slot) >= recipe.processTime()) {
            if (completeMelting(hatch, chamber, slot, recipe)) {
                hatch.resetMeltingProgress(slot);
            }
        }
    }

    private boolean completeMelting(ChargingHatchBlockEntity hatch, SmelteryChamber chamber, int slot, SmelteryRecipe recipe) {
        ItemStack stack = hatch.getInventory().getItem(slot);
        if (!recipe.input().testForRecipe(stack))
            return false;

        int requiredCount = recipe.input().stackData().count();
        ResourceStorage<ResourceVariant<Item>> itemStorage = hatch.getInventoryProvider(null);
        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            long extracted = itemStorage.extract(slot, ResourceVariant.ofItem(stack), requiredCount, transaction);
            if (extracted != requiredCount)
                return false;

            long inserted = chamber.getFluidStorage().insert(recipe.output().variant(), recipe.output().amount(), transaction);
            if (inserted != recipe.output().amount())
                return false;

            transaction.commit();
            return true;
        }
    }

    private void validateStructure() {
        this.structureReady = false;
        this.fuelSpaceSize = 0;

        logValidation("START tick={} activeChambers={} retainedRecords={}",
                this.level.getGameTime(), this.chambers.size(), this.retainedFluids.size());
        SearchResult result = collectSmelteryBlocks();
        logValidation("Structure search: result={} connectedBlocks={}", result.type(), result.positions().size());
        if (result.type() != SearchResultType.VALID)
            logValidation("ABORT: structure search incomplete or invalid; keeping previous state");
        if (result.type() == SearchResultType.VALID) {
            Set<BlockPos> positions = result.positions();

            SmelteryComponents components = collectComponents(positions);
            if (components.controllerPositions().size() != 1) {
                logValidation("INVALID: expected exactly one controller, found {}; deactivating chambers", components.controllerPositions().size());
                this.fireBoxPos = null;
                deactivateChambers();
                return;
            }

            Set<BlockPos> fireboxPositions = components.fireboxPositions();
            Set<BlockPos> chimneyPositions = components.chimneyPositions();
            Set<BlockPos> hatchPositions = components.hatchPositions();
            Set<BlockPos> outletPositions = components.outletPositions();

            logValidation("Components: fireboxes={} hatches={} chimneys={} outlets={}",
                    fireboxPositions, hatchPositions, chimneyPositions, outletPositions);
            if (fireboxPositions.size() == 1) {
                this.fireBoxPos = fireboxPositions.iterator().next();
            } else {
                logValidation("INVALID: expected exactly one firebox, found {}; deactivating chambers", fireboxPositions.size());
                this.fireBoxPos = null;
                deactivateChambers();
                return;
            }

            AABB boundingBox = calculateBounds(positions);

            Direction fuelSpaceDirection = this.level.getBlockState(this.fireBoxPos).getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
            BlockPos fuelSpaceStartPos = this.fireBoxPos.relative(fuelSpaceDirection);
            logValidation("Structure bounds={} firebox={} inwardDirection={} fuelStart={}",
                    boundingBox, this.fireBoxPos, fuelSpaceDirection, fuelSpaceStartPos);
            SearchResult fuelSpaceResult = floodFillFuelSpace(fuelSpaceStartPos, boundingBox);
            logValidation("Fuel search: result={} connectedAirBlocks={}", fuelSpaceResult.type(), fuelSpaceResult.positions().size());
            if (fuelSpaceResult.type() == SearchResultType.NOT_FOUND) {
                logValidation("INVALID: fuel space failed enclosure/sandwich checks; deactivating chambers");
                deactivateChambers();
                return;
            }

            if (fuelSpaceResult.type() != SearchResultType.VALID) {
                logValidation("ABORT: fuel search {}; keeping previous state", fuelSpaceResult.type());
                return;
            }

            List<ChamberCandidate> chamberCandidates = discoverChambers(
                    boundingBox,
                    hatchPositions,
                    chimneyPositions,
                    outletPositions,
                    fuelSpaceResult
            );

            if (chamberCandidates == null)
                return;

            boolean controllerInWall = false;
            for (ChamberCandidate candidate : chamberCandidates) {
                AABB aabb = candidate.boundingBox();
                if (isControllerInWall((int) aabb.minX - 1, (int) aabb.maxX, (int) aabb.minY - 1, (int) aabb.maxY, (int) aabb.minZ - 1, (int) aabb.maxZ)) {
                    controllerInWall = true;
                    break;
                }
            }

            if(!controllerInWall) {
                logValidation("INVALID: controller is not embedded in any chamber wall; deactivating chambers");
                deactivateChambers();
                return;
            }

            logValidation("Discovery finished: candidates={} previousChambers={} previousRetainedRecords={}",
                    chamberCandidates.size(), this.chambers.size(), this.retainedFluids.size());

            reconcileChambers(chamberCandidates, fuelSpaceResult.positions().size());
        }
    }

    private SmelteryComponents collectComponents(Set<BlockPos> positions) {
        Set<BlockPos> controllerPositions = new HashSet<>();
        Set<BlockPos> fireboxPositions = new HashSet<>();
        Set<BlockPos> chimneyPositions = new HashSet<>();
        Set<BlockPos> hatchPositions = new HashSet<>();
        Set<BlockPos> outletPositions = new HashSet<>();

        for (BlockPos pos : positions) {
            BlockState state = this.level.getBlockState(pos);
            if (state.is(ModBlocks.SMELTERY_CONTROLLER.get())) {
                controllerPositions.add(pos);
            } else if (state.is(ModBlocks.FIREBOX.get())) {
                fireboxPositions.add(pos);
            } else if (state.is(ModBlocks.CHIMNEY.get())) {
                chimneyPositions.add(pos);
            } else if (state.is(ModBlocks.CHARGING_HATCH.get())) {
                hatchPositions.add(pos);
            } else if (state.is(ModBlocks.TAPPING_OUTLET.get())) {
                outletPositions.add(pos);
            }
        }

        return new SmelteryComponents(controllerPositions, fireboxPositions, chimneyPositions, hatchPositions, outletPositions);
    }

    private AABB calculateBounds(Set<BlockPos> positions) {
        return new AABB(positions.stream().mapToDouble(BlockPos::getX).min().orElse(0),
                positions.stream().mapToDouble(BlockPos::getY).min().orElse(0),
                positions.stream().mapToDouble(BlockPos::getZ).min().orElse(0),
                positions.stream().mapToDouble(BlockPos::getX).max().orElse(0) + 1,
                positions.stream().mapToDouble(BlockPos::getY).max().orElse(0) + 1,
                positions.stream().mapToDouble(BlockPos::getZ).max().orElse(0) + 1);
    }

    private BlockPos findChimney(
            Set<BlockPos> chimneyPositions,
            int firstX, int lastX, int lastY,
            int firstZ, int lastZ
    ) {
        BlockPos chamberChimneyPos = null;
        for (BlockPos chimneyPos : chimneyPositions) {
            if (chimneyPos.getY() == lastY + 1
                    && chimneyPos.getX() > firstX && chimneyPos.getX() < lastX
                    && chimneyPos.getZ() > firstZ && chimneyPos.getZ() < lastZ) {
                chamberChimneyPos = chimneyPos;
                break;
            }
        }

        return chamberChimneyPos;
    }

    private List<BlockPos> findOutlets(
            Set<BlockPos> outletPositions,
            int firstX, int lastX, int firstY, int lastY,
            int firstZ, int lastZ
    ) {
        List<BlockPos> chamberOutlets = new ArrayList<>();
        for (BlockPos outletPos : outletPositions) {
            if ((outletPos.getY() > firstY && outletPos.getY() < lastY)
                    && (((outletPos.getX() == firstX - 1 || outletPos.getX() == lastX + 1) && outletPos.getZ() > firstZ && outletPos.getZ() < lastZ)
                    || ((outletPos.getZ() == firstZ - 1 || outletPos.getZ() == lastZ + 1) && outletPos.getX() > firstX && outletPos.getX() < lastX))) {
                chamberOutlets.add(outletPos);
            }
        }

        return chamberOutlets;
    }

    private boolean isControllerInWall(int firstX, int lastX, int firstY, int lastY, int firstZ, int lastZ) {
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();

        return y > firstY && y < lastY
                && ((x == firstX || x == lastX) && z > firstZ && z < lastZ
                || (z == firstZ || z == lastZ) && x > firstX && x < lastX);
    }

    private SearchResultType validateChamberShell(
            BlockPos hatchPos,
            int firstX, int lastX, int firstY, int lastY, int firstZ, int lastZ
    ) {
        boolean shellValid = true;
        for (int x = firstX; x <= lastX; x++) {
            for (int y = firstY; y <= lastY; y++) {
                for (int z = firstZ; z <= lastZ; z++) {
                    if (!(x == firstX || x == lastX || y == firstY || y == lastY || z == firstZ || z == lastZ))
                        continue;

                    var pos = new BlockPos(x, y, z);
                    if (!this.level.isLoaded(pos)) {
                        logValidation("ABORT: shell position {} is unloaded", pos);
                        return SearchResultType.UNLOADED_CHUNK;
                    }

                    BlockState state = this.level.getBlockState(pos);
                    if (state.is(ModBlocks.FIREBRICK.get())
                            || (pos.equals(hatchPos) && state.is(ModBlocks.CHARGING_HATCH.get()))
                            || (pos.equals(this.worldPosition)
                            && state.is(ModBlocks.SMELTERY_CONTROLLER.get())
                            && isControllerInWall(firstX, lastX, firstY, lastY, firstZ, lastZ)))
                        continue;

                    logValidation("REJECT hatch {}: invalid shell block at {} state={}", hatchPos, pos, state);
                    shellValid = false;
                }
            }
        }

        return shellValid ? SearchResultType.VALID : SearchResultType.NOT_FOUND;
    }

    private SearchResultType validateChamberFoundation(
            BlockPos hatchPos,
            int firstX, int lastX, int firstY,
            int firstZ, int lastZ,
            SearchResult fuelSpaceResult
    ) {
        boolean foundationValid = true;
        for (int x = firstX + 1; x < lastX; x++) {
            for (int z = firstZ + 1; z < lastZ; z++) {
                BlockPos airPos = new BlockPos(x, firstY - 1, z);
                BlockPos firebrickBottomPos = new BlockPos(x, firstY - 2, z);

                if (!this.level.isLoaded(airPos) || !this.level.isLoaded(firebrickBottomPos)) {
                    logValidation("ABORT: foundation unloaded at air={} bottom={}", airPos, firebrickBottomPos);
                    return SearchResultType.UNLOADED_CHUNK;
                }

                BlockState airState = this.level.getBlockState(airPos);
                BlockState firebrickBottomState = this.level.getBlockState(firebrickBottomPos);

                if (!airState.isAir()
                        || !firebrickBottomState.is(ModBlocks.FIREBRICK.get())
                        || !fuelSpaceResult.positions().contains(airPos)) {
                    logValidation("REJECT hatch {}: foundation air={} state={} bottom={} state={} reachableFromFirebox={}",
                            hatchPos, airPos, airState, firebrickBottomPos, firebrickBottomState,
                            fuelSpaceResult.positions().contains(airPos));
                    foundationValid = false;
                    break;
                }
            }

            if (!foundationValid)
                break;
        }

        return foundationValid ? SearchResultType.VALID : SearchResultType.NOT_FOUND;
    }

    private List<ChamberCandidate> discoverChambers(
            AABB boundingBox,
            Set<BlockPos> hatchPositions,
            Set<BlockPos> chimneyPositions,
            Set<BlockPos> outletPositions,
            SearchResult fuelSpaceResult
    ) {
        List<ChamberCandidate> chamberCandidates = new ArrayList<>();
        for (BlockPos hatchPos : hatchPositions) {
            BlockState hatchState = this.level.getBlockState(hatchPos);
            Direction insideDirection = hatchState.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
            BlockPos insidePos = hatchPos.relative(insideDirection);
            logValidation("Hatch {}: facing={} interiorStart={}", hatchPos,
                    hatchState.getValue(BlockStateProperties.HORIZONTAL_FACING), insidePos);
            SearchResult chamberPositions = floodFillChamber(insidePos, boundingBox);
            logValidation("Hatch {}: interior search={} airBlocks={}", hatchPos,
                    chamberPositions.type(), chamberPositions.positions().size());
            if (chamberPositions.type() == SearchResultType.UNLOADED_CHUNK || chamberPositions.type() == SearchResultType.TOO_LARGE) {
                logValidation("ABORT: incomplete interior search at hatch {}", hatchPos);
                return null;
            }

            if (chamberPositions.type() != SearchResultType.VALID) {
                logValidation("REJECT hatch {}: no enclosed air interior", hatchPos);
                continue;
            }

            var chamberBoundingBox = calculateBounds(chamberPositions.positions());

            long width = (long) chamberBoundingBox.getXsize();
            long height = (long) chamberBoundingBox.getYsize();
            long depth = (long) chamberBoundingBox.getZsize();

            long expectedCapacity = width * height * depth;
            long actualCapacity = chamberPositions.positions().size();
            logValidation("Hatch {}: interiorBounds={} dimensions={}x{}x{} expectedBlocks={} actualBlocks={}",
                    hatchPos, chamberBoundingBox, width, height, depth, expectedCapacity, actualCapacity);
            if (expectedCapacity != actualCapacity) {
                // The chamber is not a solid rectangular prism, so we skip it
                logValidation("REJECT hatch {}: interior is not a complete cuboid", hatchPos);
                continue;
            }

            int firstX = (int) chamberBoundingBox.minX - 1;
            int lastX = (int) chamberBoundingBox.maxX;
            int firstY = (int) chamberBoundingBox.minY - 1;
            int lastY = (int) chamberBoundingBox.maxY;
            int firstZ = (int) chamberBoundingBox.minZ - 1;
            int lastZ = (int) chamberBoundingBox.maxZ;

            SearchResultType shellResult = validateChamberShell(hatchPos, firstX, lastX, firstY, lastY, firstZ, lastZ);
            if (shellResult == SearchResultType.UNLOADED_CHUNK)
                return null;

            if (shellResult != SearchResultType.VALID) {
                logValidation("REJECT hatch {}: shell validation failed", hatchPos);
                continue;
            }
            logValidation("Hatch {}: shell valid", hatchPos);

            BlockPos chamberChimneyPos = findChimney(chimneyPositions, firstX, lastX, lastY, firstZ, lastZ);
            if (chamberChimneyPos == null) {
                logValidation("REJECT hatch {}: no chimney above roof Y={} within interior footprint", hatchPos, lastY + 1);
                continue;
            }
            logValidation("Hatch {}: chimney={}", hatchPos, chamberChimneyPos);

            List<BlockPos> chamberOutletPositions = findOutlets(outletPositions, firstX, lastX, firstY, lastY, firstZ, lastZ);
            if (chamberOutletPositions.isEmpty()) {
                logValidation("REJECT hatch {}: no outlet directly outside a wall", hatchPos);
                continue;
            }
            logValidation("Hatch {}: outlets={}", hatchPos, chamberOutletPositions);

            SearchResultType foundationResult = validateChamberFoundation(hatchPos, firstX, lastX, firstY, firstZ, lastZ, fuelSpaceResult);
            if (foundationResult == SearchResultType.UNLOADED_CHUNK)
                return null;

            if (foundationResult != SearchResultType.VALID) {
                logValidation("REJECT hatch {}: foundation validation failed", hatchPos);
                continue;
            }

            var chamber = new ChamberCandidate(chamberBoundingBox,
                    chamberChimneyPos, hatchPos, chamberOutletPositions,
                    FluidAmounts.BLOCK * expectedCapacity);

            if (chamberCandidates.stream().noneMatch(c -> c.boundingBox().equals(chamber.boundingBox()))) {
                chamberCandidates.add(chamber);
                logValidation("ACCEPT candidate: bounds={} hatch={} chimney={} outlets={} fluidCapacity={}",
                        chamber.boundingBox(), hatchPos, chamberChimneyPos, chamberOutletPositions, chamber.fluidCapacity());
            } else {
                logValidation("SKIP duplicate candidate bounds={}", chamber.boundingBox());
            }
        }

        return chamberCandidates;
    }

    private List<ChamberFluidSource> collectFluidSources() {
        List<ChamberFluidSource> sources = new ArrayList<>();

        for (SmelteryChamber chamber : this.chambers) {
            SyncingFluidStorage storage = chamber.getFluidStorage();
            var fluidStack = new FluidStack(storage.getResource(), storage.getAmount());
            sources.add(new ChamberFluidSource(chamber.getBoundingBox(), fluidStack));
        }

        for (RetainedChamberFluid retainedFluid : this.retainedFluids) {
            boolean matched = false;
            for (int index = 0; index < sources.size(); index++) {
                ChamberFluidSource source = sources.get(index);
                if (source.previousBounds().equals(retainedFluid.previousBounds())
                        && (source.fluidStack().isEmpty()
                        || source.fluidStack().variant().equals(retainedFluid.fluidStack().variant()))) {
                    long combinedAmount = source.fluidStack().amount() + retainedFluid.fluidStack().amount();
                    sources.set(index, new ChamberFluidSource(source.previousBounds(),
                            new FluidStack(retainedFluid.fluidStack().variant(), combinedAmount)));
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                sources.add(new ChamberFluidSource(retainedFluid.previousBounds(), retainedFluid.fluidStack()));
            }
        }

        return sources;
    }

    private List<ChamberReconciliationGroup> groupChamberOverlaps(
            List<ChamberFluidSource> sources,
            List<ChamberCandidate> candidates
    ) {
        List<ChamberReconciliationGroup> groups = new ArrayList<>();
        Set<ChamberFluidSource> remainingSources = new HashSet<>(sources);
        Set<ChamberCandidate> remainingCandidates = new HashSet<>(candidates);

        while (!remainingCandidates.isEmpty()) {
            ChamberCandidate startingCandidate = remainingCandidates.iterator().next();
            remainingCandidates.remove(startingCandidate);

            List<ChamberFluidSource> groupSources = new ArrayList<>();
            List<ChamberCandidate> groupCandidates = new ArrayList<>();

            groupCandidates.add(startingCandidate);

            boolean changed;
            do {
                changed = false;

                for (ChamberFluidSource source : new ArrayList<>(remainingSources)) {
                    if (groupCandidates.stream().anyMatch(
                            candidate -> source.previousBounds().intersects(candidate.boundingBox()))) {
                        groupSources.add(source);
                        remainingSources.remove(source);
                        changed = true;
                    }
                }

                for (ChamberCandidate candidate : new ArrayList<>(remainingCandidates)) {
                    if (groupSources.stream().anyMatch(source ->
                            source.previousBounds().intersects(candidate.boundingBox()))) {
                        groupCandidates.add(candidate);
                        remainingCandidates.remove(candidate);
                        changed = true;
                    }
                }
            } while (changed);

            groupSources.sort(Comparator.comparingDouble((ChamberFluidSource s) -> s.previousBounds().minX)
                    .thenComparingDouble(s -> s.previousBounds().minY)
                    .thenComparingDouble(s -> s.previousBounds().minZ));
            groupCandidates.sort(Comparator.comparingDouble((ChamberCandidate c) -> c.boundingBox().minX)
                    .thenComparingDouble(c -> c.boundingBox().minY)
                    .thenComparingDouble(c -> c.boundingBox().minZ));

            groups.add(new ChamberReconciliationGroup(groupSources, groupCandidates));
        }

        for (ChamberFluidSource source : remainingSources) {
            groups.add(new ChamberReconciliationGroup(List.of(source), List.of()));
        }

        return groups;
    }

    private boolean reconcileGroup(
            ChamberReconciliationGroup group,
            List<SmelteryChamber> newChambers,
            List<RetainedChamberFluid> newRetainedFluids
    ) {
        if (group.candidates().isEmpty()) {
            for (ChamberFluidSource source : group.sources()) {
                if (!source.fluidStack().isEmpty()) {
                    newRetainedFluids.add(new RetainedChamberFluid(source.previousBounds(), source.fluidStack()));
                }
            }

            return true;
        }

        FluidStack survivingFluid = group.sources().isEmpty()
                ? FluidStack.EMPTY
                : group.sources().getFirst().fluidStack();
        long remaining = survivingFluid.amount();

        for (ChamberCandidate candidate : group.candidates()) {
            long amountToKeep = Math.min(remaining, candidate.fluidCapacity());
            var storage = new SyncingFluidStorage(this, candidate.fluidCapacity());
            if (amountToKeep > 0) {
                var snapshot = new StorageSnapshot<>(List.of(
                        new StorageSnapshot.Entry<>(survivingFluid.variant(), amountToKeep)));
                if (!snapshot.apply(storage)) {
                    logValidation("ABORT: group snapshot failed for {}", candidate.boundingBox());
                    return false;
                }
            }

            remaining -= amountToKeep;

            var newChamber = new SmelteryChamber(candidate.boundingBox(), candidate.chimneyPos(),
                    candidate.hatchPos(), candidate.outletPositions(), storage);
            newChambers.add(newChamber);
        }

        if (remaining > 0) {
            newRetainedFluids.add(new RetainedChamberFluid(group.candidates().getFirst().boundingBox(),
                    new FluidStack(survivingFluid.variant(), remaining)));
        }

        return true;
    }

    private void reconcileChambers(List<ChamberCandidate> chamberCandidates, int fuelSpaceSize) {
        List<ChamberFluidSource> sources = collectFluidSources();
        List<ChamberReconciliationGroup> groups = groupChamberOverlaps(sources, chamberCandidates);

        List<SmelteryChamber> newChambers = new ArrayList<>();
        List<RetainedChamberFluid> newRetainedFluids = new ArrayList<>();

        for (ChamberReconciliationGroup group : groups) {
            if (!reconcileGroup(group, newChambers, newRetainedFluids))
                return;
        }

        if (newChambers.size() != chamberCandidates.size()) {
            logValidation("ABORT: reconciliation count mismatch; candidates={} newChambers={}",
                    chamberCandidates.size(), newChambers.size());
            return;
        }

        commitChambers(newChambers, newRetainedFluids, fuelSpaceSize);
    }

    public SmelteryChamber getChamberForHatch(BlockPos hatchPos) {
        if (!this.structureReady || hatchPos == null)
            return null;

        for (SmelteryChamber chamber : this.chambers) {
            if (Objects.equals(chamber.hatchPos, hatchPos))
                return chamber;
        }

        return null;
    }

    public SmelteryChamber getChamberForOutlet(BlockPos outletPos) {
        if (!this.structureReady || outletPos == null)
            return null;

        for (SmelteryChamber chamber : this.chambers) {
            if (chamber.outletPositions.contains(outletPos))
                return chamber;
        }

        return null;
    }

    private void clearHatchLink(BlockPos hatchPos) {
        if (hatchPos == null || this.level == null || !this.level.isLoaded(hatchPos))
            return;

        if (!(this.level.getBlockEntity(hatchPos) instanceof ChargingHatchBlockEntity hatch))
            return;

        if (Objects.equals(hatch.getControllerPos(), this.worldPosition)) {
            hatch.setControllerPos(null);
        }
    }

    private void clearOutletLink(BlockPos outletPos) {
        if (outletPos == null || this.level == null || !this.level.isLoaded(outletPos))
            return;

        if (!(this.level.getBlockEntity(outletPos) instanceof TappingOutletBlockEntity outlet))
            return;

        if (Objects.equals(outlet.getControllerPos(), this.worldPosition)) {
            outlet.setControllerPos(null);
        }
    }

    private void commitChambers(
            List<SmelteryChamber> newChambers,
            List<RetainedChamberFluid> newRetainedFluids,
            int fuelSpaceSize
    ) {
        for (SmelteryChamber oldChamber : this.chambers) {
            if (newChambers.stream().noneMatch(candidate ->
                    Objects.equals(candidate.hatchPos, oldChamber.hatchPos))) {
                clearHatchLink(oldChamber.hatchPos);
            }

            for (BlockPos outletPos : oldChamber.outletPositions) {
                if (newChambers.stream().noneMatch(candidate ->
                        candidate.outletPositions.contains(outletPos))) {
                    clearOutletLink(outletPos);
                }
            }
        }

        this.chambers.clear();
        this.chambers.addAll(newChambers);
        this.retainedFluids.clear();
        this.retainedFluids.addAll(newRetainedFluids);
        this.structureReady = !this.chambers.isEmpty();
        this.fuelSpaceSize = fuelSpaceSize;

        for (SmelteryChamber chamber : this.chambers) {
            if (this.level.getBlockEntity(chamber.hatchPos) instanceof ChargingHatchBlockEntity hatch) {
                hatch.setControllerPos(this.worldPosition);

                AABB boundingBox = chamber.getBoundingBox();
                int slotCount = Math.multiplyExact(
                        Math.multiplyExact(
                                (int) boundingBox.getXsize(),
                                (int) boundingBox.getYsize()),
                        (int) boundingBox.getZsize());
                hatch.setProcessingSlotCount(slotCount);
            }

            for (BlockPos outletPos : chamber.outletPositions) {
                if (this.level.getBlockEntity(outletPos) instanceof TappingOutletBlockEntity outlet) {
                    outlet.setControllerPos(this.worldPosition);
                }
            }
        }

        setChanged();
        logValidation("COMMIT: activeChambers={} retainedRecords={}", this.chambers.size(), this.retainedFluids.size());
        for (SmelteryChamber chamber : this.chambers) {
            logValidation("ACTIVE: bounds={} hatch={} chimney={} outlets={} resource={} amount={} capacity={}",
                    chamber.getBoundingBox(), chamber.hatchPos, chamber.chimneyPos, chamber.outletPositions,
                    chamber.getFluidStorage().getResource(), chamber.getFluidStorage().getAmount(),
                    chamber.getFluidStorage().getCapacity());
        }
    }

    // Temporary verbose diagnostics at INFO so the development console shows them without log configuration.
    private void logValidation(String message, Object... arguments) {
        LOGGER.info("[Smeltery " + this.worldPosition + "] " + message, arguments);
    }

    private void deactivateChambers() {
        this.structureReady = false;
        this.fuelSpaceSize = 0;

        if (this.chambers.isEmpty())
            return;

        logValidation("DEACTIVATE: moving {} active chambers into retained fluid", this.chambers.size());
        for (SmelteryChamber chamber : this.chambers) {
            clearHatchLink(chamber.hatchPos);
            for (BlockPos outletPos : chamber.outletPositions) {
                clearOutletLink(outletPos);
            }

            SyncingFluidStorage storage = chamber.getFluidStorage();
            var fluid = new FluidStack(storage.getResource(), storage.getAmount());
            if (fluid.isEmpty())
                continue;

            logValidation("DEACTIVATE retain: bounds={} resource={} amount={}",
                    chamber.getBoundingBox(), fluid.variant(), fluid.amount());
            var retainedFluid = new RetainedChamberFluid(chamber.getBoundingBox(), fluid);
            this.retainedFluids.add(retainedFluid);
        }

        this.chambers.clear();
        setChanged();
    }

    private boolean isSandwiched(BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockPos belowPos = pos.below();

        BlockState aboveState = this.level.getBlockState(abovePos);
        BlockState middleState = this.level.getBlockState(pos);
        BlockState belowState = this.level.getBlockState(belowPos);
        return aboveState.is(ModBlocks.FIREBRICK.get())
                && middleState.isAir()
                && belowState.is(ModBlocks.FIREBRICK.get());
    }

    private SearchResult floodFillFuelSpace(BlockPos startPos, AABB boundingBox) {
        if (!this.level.isLoaded(startPos) || !this.level.isLoaded(startPos.above()) || !this.level.isLoaded(startPos.below())) {
            logValidation("Fuel failure: start/ceiling/bottom unloaded at {}", startPos);
            return SearchResult.unloadedChunk();
        }

        if (!boundingBox.contains(startPos.getX() + 0.5, startPos.getY() + 0.5, startPos.getZ() + 0.5)) {
            logValidation("Fuel failure: start {} outside structure bounds {}", startPos, boundingBox);
            return SearchResult.notFound();
        }

        if (!isSandwiched(startPos)) {
            logValidation("Fuel failure: start {} is not sandwiched; above={} middle={} below={}",
                    startPos, this.level.getBlockState(startPos.above()), this.level.getBlockState(startPos),
                    this.level.getBlockState(startPos.below()));
            return SearchResult.notFound();
        }

        Deque<BlockPos> toVisit = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        toVisit.add(startPos);
        visited.add(startPos);

        while (!toVisit.isEmpty()) {
            BlockPos currentPos = toVisit.removeFirst();
            for (Direction direction : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
                BlockPos neighborPos = currentPos.relative(direction);
                if (visited.contains(neighborPos))
                    continue;

                if (!this.level.isLoaded(neighborPos)) {
                    logValidation("Fuel failure: horizontal neighbor {} unloaded", neighborPos);
                    return SearchResult.unloadedChunk();
                }

                BlockState neighborState = this.level.getBlockState(neighborPos);
                if (neighborState.is(ModBlocks.FIREBRICK.get()))
                    continue;

                if (neighborState.is(ModBlocks.FIREBOX.get())) {
                    if (!neighborPos.equals(this.fireBoxPos) || !currentPos.equals(startPos)) {
                        logValidation("Fuel failure: firebox {} reached from {} rather than its opening {}",
                                neighborPos, currentPos, startPos);
                        return SearchResult.notFound();
                    }

                    continue;
                }

                if (!neighborState.isAir()) {
                    logValidation("Fuel failure: perimeter neighbor {} must be firebrick, air, or firebox opening; state={}",
                            neighborPos, neighborState);
                    return SearchResult.notFound();
                }

                if (!boundingBox.contains(neighborPos.getX() + 0.5, neighborPos.getY() + 0.5, neighborPos.getZ() + 0.5)) {
                    logValidation("Fuel failure: air escapes structure bounds at {}", neighborPos);
                    return SearchResult.notFound();
                }

                if (!this.level.isLoaded(neighborPos.above()) || !this.level.isLoaded(neighborPos.below())) {
                    logValidation("Fuel failure: ceiling/bottom unloaded at {}", neighborPos);
                    return SearchResult.unloadedChunk();
                }

                if (!isSandwiched(neighborPos)) {
                    logValidation("Fuel failure: air {} lacks firebrick ceiling/bottom; above={} below={}",
                            neighborPos, this.level.getBlockState(neighborPos.above()), this.level.getBlockState(neighborPos.below()));
                    return SearchResult.notFound();
                }

                visited.add(neighborPos);
                toVisit.add(neighborPos);

                if (visited.size() > 10_000) {
                    logValidation("Fuel failure: search budget exceeded at {} visited={}", neighborPos, visited.size());
                    return SearchResult.tooLarge();
                }
            }
        }

        return SearchResult.valid(visited);
    }

    private SearchResult floodFillChamber(BlockPos startPos, AABB boundingBox) {
        if (!this.level.isLoaded(startPos)) {
            logValidation("Interior failure: start {} unloaded", startPos);
            return SearchResult.unloadedChunk();
        }

        if (!boundingBox.contains(startPos.getX() + 0.5, startPos.getY() + 0.5, startPos.getZ() + 0.5)) {
            logValidation("Interior failure: start {} outside structure bounds", startPos);
            return SearchResult.notFound();
        }

        BlockState startState = this.level.getBlockState(startPos);
        if (!startState.isAir()) {
            logValidation("Interior failure: start {} is not air; state={}", startPos, startState);
            return SearchResult.notFound();
        }

        Deque<BlockPos> toVisit = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        toVisit.add(startPos);
        visited.add(startPos);

        while (!toVisit.isEmpty()) {
            BlockPos currentPos = toVisit.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = currentPos.relative(direction);
                if (visited.contains(neighborPos))
                    continue;

                if (!this.level.isLoaded(neighborPos)) {
                    logValidation("Interior failure: neighbor {} unloaded", neighborPos);
                    return SearchResult.unloadedChunk();
                }

                BlockState neighborState = this.level.getBlockState(neighborPos);
                if (!neighborState.isAir())
                    continue;

                if (!boundingBox.contains(neighborPos.getX() + 0.5, neighborPos.getY() + 0.5, neighborPos.getZ() + 0.5)) {
                    logValidation("Interior failure: air escapes structure bounds at {}", neighborPos);
                    return SearchResult.notFound();
                }

                visited.add(neighborPos);
                toVisit.add(neighborPos);

                if (visited.size() > 10_000) {
                    logValidation("Interior failure: search budget exceeded at {} visited={}", neighborPos, visited.size());
                    return SearchResult.tooLarge();
                }
            }
        }

        return SearchResult.valid(visited);
    }

    private SearchResult collectSmelteryBlocks() {
        if (this.level == null) {
            logValidation("Structure failure: no level attached");
            return SearchResult.notFound();
        }

        Deque<BlockPos> toVisit = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        toVisit.add(this.worldPosition);
        visited.add(this.worldPosition);

        while (!toVisit.isEmpty()) {
            BlockPos currentPos = toVisit.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = currentPos.relative(direction);
                if (visited.contains(neighborPos))
                    continue;

                if (!this.level.isLoaded(neighborPos)) {
                    logValidation("Structure failure: neighbor {} unloaded", neighborPos);
                    return SearchResult.unloadedChunk();
                }

                BlockState neighborState = this.level.getBlockState(neighborPos);
                if (!isValidSmelteryBlock(neighborState))
                    continue;

                visited.add(neighborPos);
                toVisit.add(neighborPos);

                if (visited.size() > 10_000) {
                    logValidation("Structure failure: search budget exceeded at {} visited={}", neighborPos, visited.size());
                    return SearchResult.tooLarge();
                }
            }
        }

        return SearchResult.valid(visited);
    }

    private boolean isValidSmelteryBlock(BlockState state) {
        return state.is(ModBlocks.FIREBRICK.get())
                || state.is(ModBlocks.SMELTERY_CONTROLLER.get())
                || state.is(ModBlocks.FIREBOX.get())
                || state.is(ModBlocks.CHARGING_HATCH.get())
                || state.is(ModBlocks.CHIMNEY.get())
                || state.is(ModBlocks.TAPPING_OUTLET.get())
                || state.is(ModBlocks.SMELTERY_GLASS.get());
    }

    @Override
    public BlockPosPayload getMenuOpeningData(ServerPlayer player) {
        return new BlockPosPayload(this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
        return new SmelteryControllerMenu(syncId, inventory, this.worldPosition);
    }

    public static class SmelteryChamber {
        public static Codec<SmelteryChamber> codec(SmelteryControllerBlockEntity owner) {
            return SavedChamber.CODEC.flatXmap(data -> {
                if (data.capacity() < 0 || data.fluid().amount() < 0 || data.fluid().amount() > data.capacity())
                    return DataResult.error(() -> "Invalid smeltery chamber capacity or fluid amount");

                BlockPos chimneyPos = data.chimneyPos().orElse(null);
                BlockPos hatchPos = data.hatchPos().orElse(null);
                List<BlockPos> outletPositions = data.outletPositions();

                var storage = new SyncingFluidStorage(owner, data.capacity());
                var snapshot = new StorageSnapshot<>(List.of(
                        new StorageSnapshot.Entry<>(data.fluid().variant(), data.fluid().amount())));
                if (!snapshot.apply(storage))
                    return DataResult.error(() -> "Stored fluid does not fit in the smeltery chamber");

                return DataResult.success(new SmelteryChamber(data.boundingBox(), chimneyPos, hatchPos, outletPositions, storage));
            }, chamber -> DataResult.success(new SavedChamber(
                    chamber.boundingBox,
                    Optional.ofNullable(chamber.chimneyPos),
                    Optional.ofNullable(chamber.hatchPos),
                    chamber.outletPositions,
                    chamber.fluidStorage.getCapacity(),
                    new FluidStack(chamber.fluidStorage.getResource(), chamber.fluidStorage.getAmount()))));
        }

        private record SavedChamber(AABB boundingBox, Optional<BlockPos> chimneyPos, Optional<BlockPos> hatchPos,
                                    List<BlockPos> outletPositions, long capacity,
                                    FluidStack fluid) {
            private static final Codec<SavedChamber> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ExtraCodecs.BOX_CODEC.fieldOf("bounding_box").forGetter(SavedChamber::boundingBox),
                    BlockPos.CODEC.optionalFieldOf("chimney_pos").forGetter(SavedChamber::chimneyPos),
                    BlockPos.CODEC.optionalFieldOf("hatch_pos").forGetter(SavedChamber::hatchPos),
                    ExtraCodecs.listOf(BlockPos.CODEC).optionalFieldOf("outlet_positions", List.of()).forGetter(SavedChamber::outletPositions),
                    Codec.LONG.fieldOf("capacity").forGetter(SavedChamber::capacity),
                    FluidStack.CODEC.codec().fieldOf("fluid").forGetter(SavedChamber::fluid)
            ).apply(instance, SavedChamber::new));
        }

        private final AABB boundingBox;
        private final SyncingFluidStorage fluidStorage;
        private final BlockPos chimneyPos;
        private final BlockPos hatchPos;
        private final List<BlockPos> outletPositions;

        public SmelteryChamber(AABB boundingBox, BlockPos chimneyPos, BlockPos hatchPos, List<BlockPos> outletPositions, SyncingFluidStorage fluidStorage) {
            this.boundingBox = boundingBox;
            this.chimneyPos = chimneyPos;
            this.hatchPos = hatchPos;
            this.outletPositions = List.copyOf(outletPositions);
            this.fluidStorage = fluidStorage;
        }

        public AABB getBoundingBox() {
            return boundingBox;
        }

        public SyncingFluidStorage getFluidStorage() {
            return fluidStorage;
        }
    }

    private record SearchResult(SearchResultType type, Set<BlockPos> positions) {
        public static SearchResult valid(Set<BlockPos> positions) {
            return new SearchResult(SearchResultType.VALID, positions);
        }

        public static SearchResult unloadedChunk() {
            return new SearchResult(SearchResultType.UNLOADED_CHUNK, Collections.emptySet());
        }

        public static SearchResult tooLarge() {
            return new SearchResult(SearchResultType.TOO_LARGE, Collections.emptySet());
        }

        public static SearchResult notFound() {
            return new SearchResult(SearchResultType.NOT_FOUND, Collections.emptySet());
        }
    }

    private enum SearchResultType {
        VALID,
        UNLOADED_CHUNK,
        TOO_LARGE,
        NOT_FOUND
    }

    private record ChamberCandidate(
            AABB boundingBox,
            BlockPos chimneyPos,
            BlockPos hatchPos,
            List<BlockPos> outletPositions,
            long fluidCapacity
    ) {
    }

    private record RetainedChamberFluid(
            AABB previousBounds,
            FluidStack fluidStack
    ) {
        public static final Codec<RetainedChamberFluid> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.BOX_CODEC.fieldOf("previous_bounds").forGetter(RetainedChamberFluid::previousBounds),
                FluidStack.CODEC.codec().fieldOf("fluid_stack").forGetter(RetainedChamberFluid::fluidStack)
        ).apply(instance, RetainedChamberFluid::new));
    }

    private record ChamberFluidSource(
            AABB previousBounds,
            FluidStack fluidStack
    ) {
    }

    private record ChamberReconciliationGroup(
            List<ChamberFluidSource> sources,
            List<ChamberCandidate> candidates
    ) {
    }

    private record SmelteryComponents(
            Set<BlockPos> controllerPositions,
            Set<BlockPos> fireboxPositions,
            Set<BlockPos> chimneyPositions,
            Set<BlockPos> hatchPositions,
            Set<BlockPos> outletPositions
    ) {
    }
}
