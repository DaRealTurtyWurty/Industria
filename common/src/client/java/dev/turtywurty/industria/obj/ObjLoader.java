package dev.turtywurty.industria.obj;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjLoader {
    public static ObjMesh load(String data) {
        List<float[]> vertexPositionsList = new ArrayList<>();
        List<float[]> vertexUVsList = new ArrayList<>();
        List<float[]> vertexNormalsList = new ArrayList<>();
        // Final, deduplicated buffers
        List<Float> positions = new ArrayList<>();
        List<Float> uvs = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        Map<ObjVertex, Integer> remap = new HashMap<>();

        List<String> lines = data.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .toList();

        for (String line : lines) {
            if(line.startsWith("v ")) {
                String[] parts = line.split("\\s+");
                if (parts.length != 4)
                    throw new IllegalArgumentException("Invalid vertex line: " + line);

                vertexPositionsList.add(new float[] {
                    Float.parseFloat(parts[1]),
                    Float.parseFloat(parts[2]),
                    Float.parseFloat(parts[3])
                });
            } else if (line.startsWith("vt ")) {
                String[] parts = line.split("\\s+");
                if (parts.length != 3)
                    throw new IllegalArgumentException("Invalid texture coordinate line: " + line);

                vertexUVsList.add(new float[] {
                    Float.parseFloat(parts[1]),
                    Float.parseFloat(parts[2])
                });
            } else if (line.startsWith("vn ")) {
                String[] parts = line.split("\\s+");
                if (parts.length != 4)
                    throw new IllegalArgumentException("Invalid normal line: " + line);

                vertexNormalsList.add(new float[] {
                    Float.parseFloat(parts[1]),
                    Float.parseFloat(parts[2]),
                    Float.parseFloat(parts[3])
                });
            } else if (line.startsWith("f ")) {
                String[] parts = line.split("\\s+");

                int[] faceIndices = new int[parts.length - 1];
                for (int i = 1; i < parts.length; i++) {
                    var key = ObjVertex.parse(parts[i], vertexPositionsList.size(), vertexUVsList.size(), vertexNormalsList.size());
                    Integer outIndex = remap.get(key);
                    if (outIndex == null) {
                        outIndex = positions.size() / 3;

                        float[] pos = vertexPositionsList.get(key.posIndex() - 1);
                        positions.add(pos[0]);
                        positions.add(pos[1]);
                        positions.add(pos[2]);

                        if(key.uvIndex() != 0) {
                            float[] uv = vertexUVsList.get(key.uvIndex() - 1);
                            uvs.add(uv[0]);
                            uvs.add(uv[1]);
                        } else {
                            uvs.add(0f);
                            uvs.add(0f);
                        }

                        if(key.normalIndex() != 0) {
                            float[] normal = vertexNormalsList.get(key.normalIndex() - 1);
                            normals.add(normal[0]);
                            normals.add(normal[1]);
                            normals.add(normal[2]);
                        } else {
                            normals.add(0f);
                            normals.add(0f);
                            normals.add(0f);
                        }

                        remap.put(key, outIndex);
                    }

                    faceIndices[i - 1] = outIndex;
                }

                for (int i = 0; i < faceIndices.length; i++) {
                    int nextIndex = (i + 1) % faceIndices.length;
                    indices.add(faceIndices[0]);
                    indices.add(faceIndices[i]);
                    indices.add(faceIndices[nextIndex]);
                }
            }
        }

        return new ObjMesh(
            toFloatArray(positions),
            toFloatArray(uvs),
            toFloatArray(normals),
            indices.stream().mapToInt(Integer::intValue).toArray()
        );
    }

    private static float[] toFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }

        return array;
    }

    public static ObjMesh load(byte[] data) {
        return load(new String(data, StandardCharsets.UTF_8));
    }

    public static ObjMesh load(Path path) {
        try {
            byte[] data = Files.readAllBytes(path);
            return load(data);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to read OBJ data from path: " + path, exception);
        }
    }

    public static ObjMesh load(InputStream stream) {
        try {
            byte[] data = stream.readAllBytes();
            return load(data);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to read OBJ data from InputStream", exception);
        }
    }
}
