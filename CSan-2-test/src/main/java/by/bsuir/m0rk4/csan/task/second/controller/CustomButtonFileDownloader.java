package by.bsuir.m0rk4.csan.task.second.controller;

import javafx.scene.control.Button;

public class CustomButtonFileDownloader extends Button {

    private final String filename;
    private final byte[] contents;

    public CustomButtonFileDownloader(String filename, byte[] contents) {
        this.filename = filename;
        this.contents = contents;
        setText(filename);
        setWrapText(true);
    }

    public byte[] getContents() {
        return contents;
    }

    public String getFilename() {
        return filename;
    }
}
