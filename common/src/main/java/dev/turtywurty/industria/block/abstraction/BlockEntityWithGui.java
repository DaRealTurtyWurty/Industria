package dev.turtywurty.industria.block.abstraction;

import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.network.UpgradeStationOpenPayload;
import dev.turtywurty.turtymultiloader.menu.ExtendedMenuProvider;
import dev.turtywurty.turtymultiloader.menu.MenuService;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.OptionalInt;

public interface BlockEntityWithGui<T extends CustomPacketPayload> extends ExtendedMenuProvider<T> {
    default OptionalInt openMenu(ServerPlayer player) {
        T openingData = getMenuOpeningData(player);
        if (openingData instanceof BlockPosPayload payload) {
            return MenuService.get().openExtendedMenu(player, this, payload, BlockPosPayload.CODEC);
        } else if (openingData instanceof UpgradeStationOpenPayload payload) {
            return MenuService.get().openExtendedMenu(player, this, payload, UpgradeStationOpenPayload.CODEC);
        }

        throw new IllegalStateException("Unsupported menu opening payload: " + openingData.type().id());
    }
}
