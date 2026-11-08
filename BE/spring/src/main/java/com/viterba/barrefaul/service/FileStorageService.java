package com.viterba.barrefaul.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.viterba.barrefaul.exception.InvalidRequestException;

@Service
public class FileStorageService {

    // TODO studiare

    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    // Il webp è un contenitore RIFF: "RIFF" nei byte 0-3 e "WEBP" nei byte 8-11
    private static final byte[] RIFF_SIGNATURE = "RIFF".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] WEBP_SIGNATURE = "WEBP".getBytes(StandardCharsets.US_ASCII);
    private static final int WEBP_SIGNATURE_OFFSET = 8;

    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create upload directory: " + this.uploadDir, e);
        }
    }

    // Salva il file con un nome generato e restituisce il nome con cui è stato salvato
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidRequestException("File is empty");
        }
        try {
            byte[] content = file.getBytes();
            // Il tipo si ricava dal contenuto: nome ed estensione inviati dal client non sono affidabili
            String extension = detectExtension(content)
                    .orElseThrow(() -> new InvalidRequestException(
                            "Unsupported file type: only jpg, png and webp are allowed"));
            String filename = UUID.randomUUID() + "." + extension;
            Files.write(uploadDir.resolve(filename), content);
            return filename;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store file", e);
        }
    }

    // Elimina il file se esiste
    public void delete(String filename) {
        Path target = uploadDir.resolve(filename).normalize();
        // Il file deve stare direttamente nella cartella di upload (protezione da path traversal)
        if (!uploadDir.equals(target.getParent())) {
            throw new InvalidRequestException("Invalid file name");
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete file", e);
        }
    }

    private static Optional<String> detectExtension(byte[] content) {
        if (matchesAt(content, 0, JPEG_SIGNATURE)) {
            return Optional.of("jpg");
        }
        if (matchesAt(content, 0, PNG_SIGNATURE)) {
            return Optional.of("png");
        }
        if (matchesAt(content, 0, RIFF_SIGNATURE) && matchesAt(content, WEBP_SIGNATURE_OFFSET, WEBP_SIGNATURE)) {
            return Optional.of("webp");
        }
        return Optional.empty();
    }

    private static boolean matchesAt(byte[] content, int offset, byte[] signature) {
        return content.length >= offset + signature.length
                && Arrays.equals(content, offset, offset + signature.length, signature, 0, signature.length);
    }

}