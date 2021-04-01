package by.bsuir.m0rk4.csan.task.third.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class FileDtosWrapper {
    private List<FileDto> fileDtoList;

    @JsonCreator
    public FileDtosWrapper(
            @JsonProperty("fileDtoList") List<FileDto> fileDtoList) {
        this.fileDtoList = fileDtoList;
    }

    public void setFileDtoList(List<FileDto> fileDtoList) {
        this.fileDtoList = fileDtoList;
    }

    public List<FileDto> getFileDtoList() {
        return fileDtoList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileDtosWrapper that = (FileDtosWrapper) o;
        return Objects.equals(fileDtoList, that.fileDtoList);
    }

    @Override
    public String toString() {
        return "FileDtosWrapper{" +
                "fileDtoList=" + fileDtoList +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileDtoList);
    }
}
