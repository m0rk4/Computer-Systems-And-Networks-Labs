package by.bsuir.m0rk4.csan.task.third.filestorageapp.service.impl;

import by.bsuir.m0rk4.csan.task.third.filestorageapp.exception.FileException;
import by.bsuir.m0rk4.csan.task.third.filestorageapp.mapper.FileMapper;
import by.bsuir.m0rk4.csan.task.third.filestorageapp.model.FileDto;
import by.bsuir.m0rk4.csan.task.third.filestorageapp.model.FileDtosWrapper;
import by.bsuir.m0rk4.csan.task.third.filestorageapp.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileServiceImpl implements FileService {

    @Value("${storage.location}")
    private String rootDirectory;

    private final FileMapper fileMapper;

    @Autowired
    public FileServiceImpl(FileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    @Override
    public FileDtosWrapper getDirectoryFiles(String directoryPath) throws FileException {
        Path realDirectoryPath = Paths.get(rootDirectory, directoryPath);
        try (Stream<Path> filesStream = Files.walk(realDirectoryPath, 1)) {
            return new FileDtosWrapper(filesStream
                    .filter(path -> !path.equals(realDirectoryPath))
                    .map(Path::toFile)
                    .map(fileMapper::fileToFileDto)
                    .collect(Collectors.toList())
            );
        } catch (IOException e) {
            throw new FileException("Server Error: Couldn't get directory content.", e.getCause());
        }
    }

    @Override
    public FileDto uploadFile(MultipartFile multipartFile, String path) throws FileException {
        String filename = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        Path realPath = Paths.get(rootDirectory, path, filename);
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, realPath, StandardCopyOption.REPLACE_EXISTING);
            return fileMapper.fileToFileDto(realPath.toFile());
        } catch (IOException e) {
            throw new FileException("Server Error: Couldn't upload file.", e.getCause());
        }
    }

    @Override
    public void deleteFile(String filePath) throws FileException {
        Path realFilePath = Paths.get(rootDirectory, filePath);
        try {
            Files.delete(realFilePath);
        } catch (IOException e) {
            throw new FileException("Server Error: Couldn't delete file.", e.getCause());
        }
    }

    @Override
    public File getFile(String filename) throws FileException {
        return Optional.of(Paths.get(rootDirectory, filename).toFile())
                .orElseThrow(() -> new FileException("Server Error: Couldn't get file."));
    }

    @Override
    public void deleteDirectory(String directoryName) throws FileException {
        Path path = Paths.get(rootDirectory, directoryName);
        try {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new FileException("Server Error: Couldn't delete directory.", e.getCause());
        }
    }

    @Override
    public FileDto appendFileInfo(String filePath, String body) throws FileException {
        Path realFilePath = Paths.get(rootDirectory, filePath);
        try {
            Files.write(
                    realFilePath,
                    body.getBytes(),
                    StandardOpenOption.APPEND);
            return fileMapper.fileToFileDto(realFilePath.toFile());
        } catch (IOException e) {
            throw new FileException("Server Error: Couldn't append file info.", e.getCause());
        }
    }

    @Override
    public FileDto copyFile(String sourcePath, String destPath) throws FileException {
        Path source = Paths.get(rootDirectory, sourcePath);
        Path destination = Paths.get(rootDirectory, destPath);
        try {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            return fileMapper.fileToFileDto(destination.toFile());
        } catch (IOException e) {
            throw new FileException("Server Error: Couldn't copy file.", e.getCause());
        }
    }

    @Override
    public FileDto moveFile(String sourcePath, String destPath) throws FileException {
        Path source = Paths.get(rootDirectory, sourcePath);
        Path destination = Paths.get(rootDirectory, destPath);
        try {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            Files.delete(source);
            return fileMapper.fileToFileDto(destination.toFile());
        } catch (IOException e) {
            throw new FileException("Server Error: Couldn't move file.", e.getCause());
        }
    }
}
