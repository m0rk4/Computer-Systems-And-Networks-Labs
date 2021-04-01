package by.bsuir.m0rk4.csan.task.third.filestorageapp.mapper;

import by.bsuir.m0rk4.csan.task.third.filestorageapp.model.FileDto;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Component
public class FileMapper {
    public FileDto fileToFileDto(File file) {
        long size = file.length();
        if (file.isDirectory()) {
            try {
                Optional<Long> reduce = Files.walk(file.toPath())
                        .map(Path::toFile)
                        .filter(f -> !f.isDirectory())
                        .map(File::length)
                        .reduce(Long::sum);
                if (reduce.isPresent()) {
                    size = reduce.get();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new FileDto(file.getName(), file.isDirectory(), size);
    }
}



