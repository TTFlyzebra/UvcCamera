package com.flyzebra.uvccamera.data;

/**
 * Author: FlyZebra
 * Time: 18-4-16 下午9:33.
 * Discription: This is SuperSizeMode
 */
public class SuperSizeMode {
    private int index;

    private int type;

    int width;

    int height;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "SuperSizeMode{" +
                "index=" + index +
                ", type=" + type +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
