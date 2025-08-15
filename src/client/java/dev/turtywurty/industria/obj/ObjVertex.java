package dev.turtywurty.industria.obj;

public record ObjVertex(int posIndex, int uvIndex, int normalIndex) {
    public ObjVertex(int posIndex) {
        this(posIndex, -1, -1);
    }

    public ObjVertex(int posIndex, int texIndex) {
        this(posIndex, texIndex, -1);
    }

    public static ObjVertex parse(String token, int vCount, int vtCount, int vnCount) {
        String[] parts = token.split("/", -1);
        int posIndex = parseIndex(parts[0], vCount);
        int uvIndex = parts.length > 1 && !parts[1].isEmpty() ? parseIndex(parts[1], vtCount) : 0;
        int normalIndex = parts.length > 2 && !parts[2].isEmpty() ? parseIndex(parts[2], vnCount) : 0;
        return new ObjVertex(posIndex, uvIndex, normalIndex);
    }

    private static int parseIndex(String part, int count) {
        if (part.isEmpty())
            return -1; // No index provided

        int index = Integer.parseInt(part);
        if(index > 0) return index;

        return count + index + 1; // Handle negative indices
    }
}