package com.example.notemelab3;

public class Note {
    private int id;
    private String title;
    private String subtitle;
    private String text;
    private String color;
    private byte[] image;

    public Note(int id, String title, String subtitle, String text, String color, byte[] image) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.text = text;
        this.color = color;
        this.image = image;
    }

    public int getId() { return id; }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getText() {
        return text;
    }

    public String getColor() {
        return color;
    }

    public byte[] getImage() {
        return image;
    }
}
