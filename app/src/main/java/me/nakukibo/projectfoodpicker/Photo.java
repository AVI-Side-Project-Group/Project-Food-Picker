package me.nakukibo.projectfoodpicker;

public class Photo {
    private String reference;
    private int width;
    private int height;

    public Photo(String reference, int width, int height) {
        this.reference = reference;
        this.width = width;
        this.height = height;
    }

    public String getReference() {
        return reference;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
