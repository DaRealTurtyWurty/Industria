package dev.turtywurty.industria.util;

public enum ColorMode {
    ADDITION,
    MULTIPLICATION,
    SUBTRACTION,
    DIVISION,
    REPLACE;

    public static int modifyColor(int color, int modifierColor, ColorMode mode) {
        float redModifier = (modifierColor >> 16 & 0xFF) / 255.0F;
        float greenModifier = (modifierColor >> 8 & 0xFF) / 255.0F;
        float blueModifier = (modifierColor & 0xFF) / 255.0F;
        float alphaModifier = (modifierColor >> 24 & 0xFF) / 255.0F;

        return modifyColor(color, redModifier, greenModifier, blueModifier, alphaModifier, mode);
    }

    public static int modifyColor(int color, float redModifier, float greenModifier, float blueModifier, float alphaModifier, ColorMode mode) {
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = (color >> 24 & 0xFF) / 255.0F;

        switch (mode) {
            case ADDITION:
                r += redModifier;
                g += greenModifier;
                b += blueModifier;
                a += alphaModifier;
                break;
            case MULTIPLICATION:
                r *= redModifier;
                g *= greenModifier;
                b *= blueModifier;
                a *= alphaModifier;
                break;
            case SUBTRACTION:
                r -= redModifier;
                g -= greenModifier;
                b -= blueModifier;
                a -= alphaModifier;
                break;
            case DIVISION:
                r /= redModifier;
                g /= greenModifier;
                b /= blueModifier;
                a /= alphaModifier;
                break;
            case REPLACE:
                r = redModifier;
                g = greenModifier;
                b = blueModifier;
                a = alphaModifier;
                break;
            default:
                throw new UnsupportedOperationException("Unexpected value: " + mode);
        }

        return ((int) (r * 255.0F) << 16) | ((int) (g * 255.0F) << 8) | (int) (b * 255.0F) | ((int) (a * 255.0F) << 24);
    }
}