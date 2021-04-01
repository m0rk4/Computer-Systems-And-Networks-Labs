package by.bsuir.m0rk4.csan.task.third.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class FileDto {
    private String filename;
    private boolean isDirectory;
    private long size;

    @JsonCreator
    public FileDto(
            @JsonProperty("filename") String filename,
            @JsonProperty("isDirectory") boolean isDirectory,
            @JsonProperty("size") long size) {
        this.filename = filename;
        this.isDirectory = isDirectory;
        this.size = size;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFilename() {
        return filename;
    }

    public long getSize() {
        return size;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileDto fileDto = (FileDto) o;
        return isDirectory == fileDto.isDirectory &&
                size == fileDto.size &&
                Objects.equals(filename, fileDto.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, isDirectory, size);
    }

    @Override
    public String toString() {
        return "FileDto{" +
                "filename='" + filename + '\'' +
                ", isDirectory=" + isDirectory +
                ", size=" + size +
                '}';
    }
}
