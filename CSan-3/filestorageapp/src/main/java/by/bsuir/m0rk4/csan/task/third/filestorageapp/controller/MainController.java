package by.bsuir.m0rk4.csan.task.third.filestorageapp.controller;

import by.bsuir.m0rk4.csan.task.third.filestorageapp.exception.FileException;
import by.bsuir.m0rk4.csan.task.third.filestorageapp.exception.RestResponseEntityExceptionHandler;
import by.bsuir.m0rk4.csan.task.third.filestorageapp.model.FileDto;
import by.bsuir.m0rk4.csan.task.third.filestorageapp.model.FileDtosWrapper;
import by.bsuir.m0rk4.csan.task.third.filestorageapp.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/files")
public class MainController {

    private final FileService fileService;

    @Autowired
    public MainController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping
    public ResponseEntity<Resource> getFile(@RequestParam String filename) throws FileException {
        File file = fileService.getFile(filename);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + StringUtils.cleanPath(filename));
        try {
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (FileNotFoundException e) {
            throw new FileException("Server Error: File not found.", e.getCause());
        }
    }

    @GetMapping("/dir")
    public FileDtosWrapper getDirectoryFiles(@RequestParam String directoryName) throws FileException {
        return fileService.getDirectoryFiles(directoryName);
    }

    @PutMapping
    public FileDto uploadFile(
            @RequestParam(name = "file") MultipartFile multipartFile,
            @RequestParam(name = "path") String path) throws FileException {
        return fileService.uploadFile(multipartFile, path);
    }

    @PostMapping
    public FileDto appendFileInfo(@RequestParam String filename, @RequestBody String body) throws FileException {
        return fileService.appendFileInfo(filename, body);
    }

    @DeleteMapping
    public void deleteFile(@RequestParam String filename) throws FileException {
        fileService.deleteFile(filename);
    }

    @DeleteMapping("/dir")
    public void deleteDirectory(@RequestParam String directoryName) throws FileException {
        fileService.deleteDirectory(directoryName);
    }

    @PostMapping("/copy")
    public FileDto copyFile(
            @RequestParam(name = "source") String sourcePath,
            @RequestParam(name = "destination") String destPath) throws FileException {
        return fileService.copyFile(sourcePath, destPath);
    }

    @PostMapping("/move")
    public FileDto moveFile(
            @RequestParam(name = "source") String sourcePath,
            @RequestParam(name = "destination") String destPath) throws FileException {
        return fileService.moveFile(sourcePath, destPath);
    }
}
