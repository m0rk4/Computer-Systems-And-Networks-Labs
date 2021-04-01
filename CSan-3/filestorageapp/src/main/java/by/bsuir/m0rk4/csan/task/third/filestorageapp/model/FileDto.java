package by.bsuir.m0rk4.csan.task.third.filestorageapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileDto {
    private final String filename;
    private final boolean isDirectory;
    private final long size;
}
