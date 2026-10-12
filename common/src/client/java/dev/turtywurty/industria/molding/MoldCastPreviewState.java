package dev.turtywurty.industria.molding;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class MoldCastPreviewState implements PictureInPictureRenderState {
    private final int x;
    private final int y;
    private final int size;
    private final TextureAtlasSprite sprite;
    private final MoldCastSurface.Snapshot surface;
    private final @Nullable ScreenRectangle scissorArea;

    public MoldCastPreviewState(
        int x,
        int y,
        int size,
        TextureAtlasSprite sprite,
        MoldCastSurface.Snapshot surface,
        @Nullable ScreenRectangle scissorArea
    ) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.sprite = sprite;
        this.surface = surface;
        this.scissorArea = scissorArea;
    }

    @Override
    public int x0() {
        return x;
    }

    @Override
    public int x1() {
        return x + size;
    }

    @Override
    public int y0() {
        return y;
    }

    @Override
    public int y1() {
        return y + size;
    }

    @Override
    public float scale() {
        return size;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return scissorArea;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        return PictureInPictureRenderState.getBounds(x0(), y0(), x1(), y1(), scissorArea);
    }

    public TextureAtlasSprite getSprite() {
        return sprite;
    }

    public MoldCastSurface.Snapshot surface() {
        return surface;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof MoldCastPreviewState state
            && x == state.x
            && y == state.y
            && size == state.size
            && sprite == state.sprite
            && surface == state.surface
            && Objects.equals(scissorArea, state.scissorArea);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, size, sprite, surface, scissorArea);
    }
}
