package by.bsuir.m0rk4.csan.task.third.filestorageapp.service;

import by.bsuir.m0rk4.csan.task.third.filestorageapp.exception.FileException;
import by.bsuir.m0rk4.csan.task.third.filestorageapp.model.FileDto;
import by.bsuir.m0rk4.csan.task.third.filestorageapp.model.FileDtosWrapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface FileService {
    FileDtosWrapper getDirectoryFiles(String directoryName) throws FileException;

    FileDto uploadFile(MultipartFile multipartFile, String path) throws FileException;

    void deleteFile(String filePath) throws FileException;

    File getFile(String filename) throws FileException;

    void deleteDirectory(String directoryName) throws FileException;

    FileDto appendFileInfo(String filePath, String body) throws FileException;

    FileDto copyFile(String sourcePath, String destPath) throws FileException;

    FileDto moveFile(String sourcePath, String destPath) throws FileException;
}
