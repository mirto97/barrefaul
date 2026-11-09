package com.viterba.barrefaul.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.viterba.barrefaul.exception.InvalidRequestException;

class FileStorageServiceTest {

    // Cartella temporanea di JUnit: la cartella di upload è una sua sottocartella, così
    // si può verificare che i file fuori dalla cartella di upload non vengano toccati
    @TempDir
    Path root;

    private Path uploadDir;
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        // La sottocartella non esiste ancora: la crea il costruttore
        uploadDir = root.resolve("uploads");
        fileStorageService = new FileStorageService(uploadDir.toString());
    }

    // Costruttore

    @Test
    void constructor_createsMissingDirectory() {
        assertThat(uploadDir).isDirectory();
    }

    @Test
    void constructor_acceptsExistingDirectory() {
        assertThatCode(() -> new FileStorageService(uploadDir.toString())).doesNotThrowAnyException();
    }

    @Test
    void constructor_failsWhenUploadPathIsAFile() throws IOException {
        Path file = Files.createFile(root.resolve("not-a-directory"));

        assertThatThrownBy(() -> new FileStorageService(file.toString()))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("upload directory");
    }

    // store

    @ParameterizedTest(name = "{0}")
    @MethodSource("supportedFiles")
    void store_savesSupportedFormatWithGeneratedName(String format, byte[] content, String extension) {
        // Nome e content-type del client sono volutamente generici: il tipo si ricava dal contenuto
        MockMultipartFile file = new MockMultipartFile("file", "original-name.bin", "application/octet-stream", content);

        String filename = fileStorageService.store(file);

        assertThat(filename)
                .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\." + extension);
        assertThat(uploadDir.resolve(filename)).hasBinaryContent(content);
    }

    @Test
    void store_detectsTypeFromContentAndIgnoresClientNameAndContentType() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", pngBytes());

        String filename = fileStorageService.store(file);

        assertThat(filename).endsWith(".png");
    }

    @Test
    void store_generatesDifferentNamesForTheSameContent() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", pngBytes());

        String first = fileStorageService.store(file);
        String second = fileStorageService.store(file);

        assertThat(first).isNotEqualTo(second);
        assertThat(uploadDir.resolve(first)).exists();
        assertThat(uploadDir.resolve(second)).exists();
    }

    @Test
    void store_rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> fileStorageService.store(file))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("empty");
        assertThat(uploadDir).isEmptyDirectory();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unsupportedContents")
    void store_rejectsUnsupportedContentEvenWithImageNameAndContentType(String description, byte[] content) {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", content);

        assertThatThrownBy(() -> fileStorageService.store(file))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Unsupported file type");
        assertThat(uploadDir).isEmptyDirectory();
    }

    @Test
    void store_wrapsReadErrorsInUncheckedIOException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException("read error"));

        assertThatThrownBy(() -> fileStorageService.store(file))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("Could not store file");
    }

    // delete

    @Test
    void delete_removesExistingFile() {
        String filename = fileStorageService.store(new MockMultipartFile("file", "photo.png", "image/png", pngBytes()));

        fileStorageService.delete(filename);

        assertThat(uploadDir.resolve(filename)).doesNotExist();
    }

    @Test
    void delete_missingFileIsIgnored() {
        assertThatCode(() -> fileStorageService.delete("missing.png")).doesNotThrowAnyException();
    }

    @Test
    void delete_leavesOtherFilesUntouched() {
        String toDelete = fileStorageService.store(new MockMultipartFile("file", "a.png", "image/png", pngBytes()));
        String toKeep = fileStorageService.store(new MockMultipartFile("file", "b.png", "image/png", pngBytes()));

        fileStorageService.delete(toDelete);

        assertThat(uploadDir.resolve(toKeep)).exists();
    }

    // Solo separatori "/": il backslash è un separatore su Windows ma non su Linux, quindi non è testabile allo stesso modo
    @ParameterizedTest(name = "[{0}]")
    @ValueSource(strings = {"../outside.png", "sub/file.png", "..", ".", ""})
    void delete_rejectsNamesThatDoNotPointDirectlyInsideUploadDirectory(String filename) {
        assertThatThrownBy(() -> fileStorageService.delete(filename))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Invalid file name");
    }

    @Test
    void delete_doesNotRemoveFileOutsideUploadDirectory() throws IOException {
        Path outside = Files.write(root.resolve("outside.png"), pngBytes());

        assertThatThrownBy(() -> fileStorageService.delete("../outside.png"))
                .isInstanceOf(InvalidRequestException.class);
        assertThat(outside).exists();
    }

    @Test
    void delete_rejectsAbsolutePathOutsideUploadDirectory() throws IOException {
        Path outside = Files.write(root.resolve("outside.png"), pngBytes());

        assertThatThrownBy(() -> fileStorageService.delete(outside.toAbsolutePath().toString()))
                .isInstanceOf(InvalidRequestException.class);
        assertThat(outside).exists();
    }

    // Dati di test

    static Stream<Arguments> supportedFiles() {
        return Stream.of(
                Arguments.of("jpeg", jpegBytes(), "jpg"),
                Arguments.of("png", pngBytes(), "png"),
                Arguments.of("webp", webpBytes(), "webp"));
    }

    static Stream<Arguments> unsupportedContents() {
        return Stream.of(
                Arguments.of("text", ascii("not an image")),
                Arguments.of("gif", ascii("GIF89a\0\0")),
                Arguments.of("pdf", ascii("%PDF-1.4")),
                // Stesso contenitore RIFF del webp ma con un altro formato (wav)
                Arguments.of("riff wav", ascii("RIFF\0\0\0\0WAVEfmt ")),
                // Intestazioni troncate: più corte della firma da controllare
                Arguments.of("truncated jpeg", new byte[] {(byte) 0xFF, (byte) 0xD8}),
                Arguments.of("truncated png", new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47}),
                Arguments.of("truncated webp", ascii("RIFF\0\0\0\0WEB")),
                Arguments.of("single byte", new byte[] {0x01}));
    }

    private static byte[] jpegBytes() {
        return new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10};
    }

    private static byte[] pngBytes() {
        return new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00};
    }

    // "RIFF", 4 byte di dimensione (non controllati dal service), "WEBP"
    private static byte[] webpBytes() {
        return ascii("RIFF\0\0\0\0WEBPVP8 ");
    }

    private static byte[] ascii(String value) {
        return value.getBytes(StandardCharsets.US_ASCII);
    }

}