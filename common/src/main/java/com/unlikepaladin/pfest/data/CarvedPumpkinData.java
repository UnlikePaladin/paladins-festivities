package com.unlikepaladin.pfest.data;

import net.minecraft.nbt.CompoundTag;

import java.util.BitSet;

public class CarvedPumpkinData {
    public static final int DEFAULT_EXP = 4; // 16x16
    private int resolutionExp;
    private final BitSet[] faces = new BitSet[4];

    public CarvedPumpkinData() {
        this(DEFAULT_EXP);
    }

    public CarvedPumpkinData(int resolutionExp) {
        this.resolutionExp = resolutionExp;
        for (int i = 0; i < 4; i++) {
            this.faces[i] = new BitSet(getResolution() * getResolution());
        }
    }

    public int getResolutionExp() {
        return this.resolutionExp;
    }

    public int getResolution() {
        return 1 << this.resolutionExp;
    }

    public void setResolutionExp(int resolutionExp) {
        this.resolutionExp = resolutionExp;
        for (int i = 0; i < 4; i++) {
            this.faces[i].clear();
        }
    }

    public boolean isCarved(int face, int x, int y) {
        if (face < 0 || face >= 4) return false;
        int res = getResolution();
        if (x < 0 || x >= res || y < 0 || y >= res) return false;
        return this.faces[face].get(y * res + x);
    }

    public void setCarved(int face, int x, int y, boolean carved) {
        if (face < 0 || face >= 4) return;
        int res = getResolution();
        if (x < 0 || x >= res || y < 0 || y >= res) return;
        this.faces[face].set(y * res + x, carved);
    }

    public void carveBrush(int face, int cx, int cy, int brushSize, boolean carved) {
        if (brushSize <= 1) {
            setCarved(face, cx, cy, carved);
        } else if (brushSize == 2) {
            for (int dx = 0; dx <= 1; dx++) {
                for (int dy = 0; dy <= 1; dy++) {
                    setCarved(face, cx + dx, cy + dy, carved);
                }
            }
        } else {
            int radius = brushSize / 2;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    setCarved(face, cx + dx, cy + dy, carved);
                }
            }
        }
    }

    public byte[] getFaceBytes(int face) {
        if (face < 0 || face >= 4) return new byte[0];
        return this.faces[face].toByteArray();
    }

    public void setFaceBytes(int face, byte[] bytes) {
        if (face < 0 || face >= 4) return;
        this.faces[face] = BitSet.valueOf(bytes);
    }

    public boolean hasFaceCarved(int face) {
        if (face < 0 || face >= 4) return false;
        return !this.faces[face].isEmpty();
    }

    public boolean isEmpty() {
        for (int i = 0; i < 4; i++) {
            if (!this.faces[i].isEmpty()) return false;
        }
        return true;
    }

    public void clearFace(int face) {
        if (face >= 0 && face < 4) {
            this.faces[face].clear();
        }
    }

    public void clearAll() {
        for (int i = 0; i < 4; i++) {
            this.faces[i].clear();
        }
    }

    public void copyFrom(CarvedPumpkinData other) {
        this.resolutionExp = other.resolutionExp;
        for (int i = 0; i < 4; i++) {
            this.faces[i] = (BitSet) other.faces[i].clone();
        }
    }

    public void saveToNbt(CompoundTag tag) {
        tag.putByte("ResolutionExp", (byte) this.resolutionExp);
        CompoundTag facesTag = new CompoundTag();
        for (int i = 0; i < 4; i++) {
            facesTag.putByteArray("Face" + i, this.faces[i].toByteArray());
        }
        tag.put("Faces", facesTag);
    }

    public void loadFromNbt(CompoundTag tag) {
        if (tag.contains("ResolutionExp")) {
            this.resolutionExp = tag.getByte("ResolutionExp");
        } else {
            this.resolutionExp = DEFAULT_EXP;
        }

        if (tag.contains("Faces", 10)) {
            CompoundTag facesTag = tag.getCompound("Faces");
            for (int i = 0; i < 4; i++) {
                if (facesTag.contains("Face" + i)) {
                    this.faces[i] = BitSet.valueOf(facesTag.getByteArray("Face" + i));
                } else {
                    this.faces[i] = new BitSet(getResolution() * getResolution());
                }
            }
        } else {
            clearAll();
        }
    }
}
