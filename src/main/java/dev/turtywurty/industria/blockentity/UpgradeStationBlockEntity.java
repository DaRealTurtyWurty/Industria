package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.util.TickableBlockEntity;
import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import dev.turtywurty.industria.blockentity.util.energy.SyncingEnergyStorage;
import dev.turtywurty.industria.blockentity.util.energy.WrappedEnergyStorage;
import dev.turtywurty.industria.blockentity.util.inventory.OutputSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.SyncingSimpleInventory;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import dev.turtywurty.industria.init.MultiblockTypeInit;
import dev.turtywurty.industria.multiblock.MultiblockType;
import dev.turtywurty.industria.multiblock.Multiblockable;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.UpgradeStationScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UpgradeStationBlockEntity extends UpdatableBlockEntity implements ExtendedScreenHandlerFactory<BlockPosPayload>, TickableBlockEntity, Multiblockable {
    public static final Text TITLE = Industria.containerTitle("upgrade_station");

    private final WrappedInventoryStorage<SimpleInventory> wrappedInventoryStorage = new WrappedInventoryStorage<>();
    private final WrappedEnergyStorage wrappedEnergyStorage = new WrappedEnergyStorage();
    private final List<BlockPos> multiblockPositions = new ArrayList<>();

    @Nullable
    private RegistryKey<Recipe<?>> selectedRecipe;
    private final List<RegistryKey<Recipe<?>>> availableRecipes = new ArrayList<>();
    private int selectedRecipeIndex = 0;

    private int progress = 0;

    public UpgradeStationBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypeInit.UPGRADE_STATION, pos, state);

        this.wrappedInventoryStorage.addInventory(new SyncingSimpleInventory(this, 9), Direction.UP);
        this.wrappedInventoryStorage.addInventory(new OutputSimpleInventory(this, 1), Direction.DOWN);
        this.wrappedEnergyStorage.addStorage(new SyncingEnergyStorage(this, 100_000, 1_000, 0));
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new BlockPosPayload(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new UpgradeStationScreenHandler(syncId, playerInventory, this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.put("MultiblockPositions", Multiblockable.writeMultiblockToNbt(this));
        nbt.put("Inventory", this.wrappedInventoryStorage.writeNbt(registries));
        nbt.put("Energy", this.wrappedEnergyStorage.writeNbt(registries));

        if (this.selectedRecipe != null) {
            nbt.putString("SelectedRecipe", this.selectedRecipe.getValue().toString());
        }

        var availableRecipes = new NbtList();
        for (RegistryKey<Recipe<?>> recipe : this.availableRecipes) {
            availableRecipes.add(NbtString.of(recipe.getValue().toString()));
        }

        nbt.put("AvailableRecipes", availableRecipes);

        nbt.putInt("SelectedRecipeIndex", this.selectedRecipeIndex);

        nbt.putInt("Progress", this.progress);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains("MultiblockPositions", NbtElement.LIST_TYPE)) {
            Multiblockable.readMultiblockFromNbt(this, nbt.getList("MultiblockPositions", NbtElement.COMPOUND_TYPE));
        }

        if (nbt.contains("Inventory", NbtElement.LIST_TYPE)) {
            this.wrappedInventoryStorage.readNbt(nbt.getList("Inventory", NbtElement.COMPOUND_TYPE), registries);
        }

        if (nbt.contains("Energy", NbtElement.LIST_TYPE)) {
            this.wrappedEnergyStorage.readNbt(nbt.getList("Energy", NbtElement.COMPOUND_TYPE), registries);
        }

        if (nbt.contains("SelectedRecipe", NbtElement.STRING_TYPE)) {
            this.selectedRecipe = getRecipeKey(Objects.requireNonNull(nbt.get("SelectedRecipe")));
        }

        if (nbt.contains("AvailableRecipes", NbtElement.LIST_TYPE)) {
            this.availableRecipes.clear();
            NbtList availableRecipes = nbt.getList("AvailableRecipes", NbtElement.STRING_TYPE);
            for (NbtElement recipe : availableRecipes) {
                this.availableRecipes.add(getRecipeKey(recipe));
            }
        }

        if (nbt.contains("SelectedRecipeIndex", NbtElement.INT_TYPE)) {
            this.selectedRecipeIndex = nbt.getInt("SelectedRecipeIndex");
        }

        if (nbt.contains("Progress", NbtElement.INT_TYPE)) {
            this.progress = nbt.getInt("Progress");
        }
    }

    private static RegistryKey<Recipe<?>> getRecipeKey(NbtElement element) {
        return RegistryKey.of(RegistryKeys.RECIPE, Identifier.tryParse(element.asString()));
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        var nbt = super.toInitialChunkDataNbt(registries);
        writeNbt(nbt, registries);
        return nbt;
    }

    @Override
    public void tick() {
        if (this.world == null || this.world.isClient)
            return;
    }

    @Override
    public MultiblockType<?> type() {
        return MultiblockTypeInit.UPGRADE_STATION;
    }

    @Override
    public List<BlockPos> findPositions(@Nullable Direction facing) {
        List<BlockPos> positions = new ArrayList<>();
        positions.add(this.pos.up());

        BlockPos.Mutable mutable = this.pos.mutableCopy();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 2; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }

                mutable.set(this.pos.getX() + x, this.pos.getY(), this.pos.getZ() + z);
                positions.add(mutable.toImmutable());
            }
        }

        return positions;
    }

    @Override
    public List<BlockPos> getMultiblockPositions() {
        return this.multiblockPositions;
    }
}
