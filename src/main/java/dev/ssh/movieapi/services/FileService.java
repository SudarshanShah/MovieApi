package dev.ssh.movieapi.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface FileService {

    String uploadFile(String path, MultipartFile file) throws IOException;

    InputStream getResourceFile(String path, String fileName) throws FileNotFoundException;
}
