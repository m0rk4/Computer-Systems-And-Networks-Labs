package by.bsuir.m0rk4.csan.task.third.http;

import by.bsuir.m0rk4.csan.task.third.dto.FileDto;
import by.bsuir.m0rk4.csan.task.third.dto.FileDtosWrapper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public class HttpClient {

    private HttpClient() {};

    private static final HttpClient httpclient = new HttpClient();

    public static HttpClient getInstance() {
        return httpclient;
    }

    private static final String SERVER_API_URL = "http://localhost:9000/files/";

    public FileDto uploadFile(File file, String path) throws HttpException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        body.add("path", path);
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<FileDto> response = restTemplate.exchange(
                    SERVER_API_URL,
                    HttpMethod.PUT,
                    requestEntity,
                    FileDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }

    public void deleteFile(String filePath) throws HttpException {
        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        body.add("filename", filePath);
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, new HttpHeaders());
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.exchange(
                    SERVER_API_URL,
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class
            );
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }

    public void deleteDirectory(String directoryName) throws HttpException {
        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        body.add("directoryName", directoryName);
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, new HttpHeaders());
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.exchange(
                    SERVER_API_URL + "/dir",
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class
            );
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }

    public FileDto copyFile(String source, String destination) throws HttpException {
        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        body.add("source", source);
        body.add("destination", destination);
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, new HttpHeaders());
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<FileDto> exchange = restTemplate.exchange(
                    SERVER_API_URL + "/copy",
                    HttpMethod.POST,
                    requestEntity,
                    FileDto.class
            );
            return exchange.getBody();
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }

    public FileDto moveFile(String source, String destination) throws HttpException {
        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        body.add("source", source);
        body.add("destination", destination);
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, new HttpHeaders());
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<FileDto> exchange = restTemplate.exchange(
                    SERVER_API_URL + "/move",
                    HttpMethod.POST,
                    requestEntity,
                    FileDto.class
            );
            return exchange.getBody();
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }

    public FileDto appendContentToFile(String filename, String content) throws HttpException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(SERVER_API_URL)
                .queryParam("filename", filename);
        HttpEntity<String> requestEntity
                = new HttpEntity<>(content, new HttpHeaders());
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<FileDto> exchange = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.POST,
                    requestEntity,
                    FileDto.class
            );
            return exchange.getBody();
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }

    public List<FileDto> getDirContents(String dirPath) throws HttpException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(SERVER_API_URL + "/dir")
                .queryParam("directoryName", dirPath);
        HttpEntity<String> requestEntity
                = new HttpEntity<>("", new HttpHeaders());
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<FileDtosWrapper> exchange = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    requestEntity,
                    FileDtosWrapper.class
            );
            return exchange.getBody().getFileDtoList();
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }

    public InputStream downloadFile(String filename) throws HttpException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(SERVER_API_URL)
                .queryParam("filename", filename);
        HttpEntity<String> requestEntity
                = new HttpEntity<>("", new HttpHeaders());
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Resource> exchange = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    requestEntity,
                    Resource.class
            );
            Resource body = exchange.getBody();
            return body.getInputStream();
        } catch (Exception e) {
            throw new HttpException(e.getMessage(), e);
        }
    }




}
