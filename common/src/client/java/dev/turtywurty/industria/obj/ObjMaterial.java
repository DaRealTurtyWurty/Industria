package dev.turtywurty.industria.obj;

public class ObjMaterial {
    protected final String name;
    protected float[] ambientColor = new float[3];
    protected float[] diffuseColor = new float[3];
    protected float[] specularColor = new float[3];
    protected float specularExponent = 0.0f;
    protected float[] emissiveColor = new float[3];
    protected float opticalDensity = 1.0f;
    protected float dissolve = 1.0f;
    protected int illuminationModel = 0;

    protected String textureMap;
    protected String bumpMap;
    protected String displacementMap;
    protected String specularMap;
    protected String ambientMap;
    protected String normalMap;
    protected String alphaMap;

    public ObjMaterial(String name) {
        this.name = name;
    }
}
