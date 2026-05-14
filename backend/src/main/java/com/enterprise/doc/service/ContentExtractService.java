package com.enterprise.doc.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentExtractService {

    private final Tika tika = new Tika();
    private final FileService fileService;

    public String extractText(String storagePath) {
        try {
            File file = fileService.getFile(storagePath);
            if (file == null || !file.exists()) {
                log.warn("File not found: {}", storagePath);
                return "";
            }
            return tika.parseToString(file);
        } catch (IOException | TikaException e) {
            log.error("Failed to extract text from: {}", storagePath, e);
            return "";
        }
    }

    public String extractText(InputStream inputStream) {
        try {
            return tika.parseToString(inputStream);
        } catch (IOException | TikaException e) {
            log.error("Failed to extract text from input stream", e);
            return "";
        }
    }
}
