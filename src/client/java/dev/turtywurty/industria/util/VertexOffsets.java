package dev.turtywurty.industria.util;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;

public interface VertexOffsets {
    int STRIDE = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSize() / 4;
    int POSITION = getOffset(VertexFormatElement.POSITION);
    int COLOR = getOffset(VertexFormatElement.COLOR);
    int UV0 = getOffset(VertexFormatElement.UV0);
    int UV1 = getOffset(VertexFormatElement.UV1);
    int UV2 = getOffset(VertexFormatElement.UV2);
    int NORMAL = getOffset(VertexFormatElement.NORMAL);

    private static int getOffset(VertexFormatElement element) {
        if (VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.contains(element)) {
            return VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getOffset(element) / 4;
        }

        return -1;
    }
}
