package com.labelverifier.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.labelverifier.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class FileValidationServiceTest {
    private final FileValidationService service = new FileValidationService();

    @Test
    void acceptsPngSignatureMatchingContentType() {
        MockMultipartFile file = new MockMultipartFile(
            "image",
            "label.png",
            "image/png",
            new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
        );

        assertThatCode(() -> service.validateImage(file)).doesNotThrowAnyException();
    }

    @Test
    void rejectsSpoofedContentType() {
        MockMultipartFile file = new MockMultipartFile(
            "image",
            "label.png",
            "image/png",
            "not an image".getBytes()
        );

        assertThatThrownBy(() -> service.validateImage(file))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("File content does not match");
    }

    @Test
    void rejectsImagesOverTenMegabytes() {
        byte[] content = new byte[(10 * 1024 * 1024) + 1];
        content[0] = (byte) 0xFF;
        content[1] = (byte) 0xD8;
        content[2] = (byte) 0xFF;
        MockMultipartFile file = new MockMultipartFile("image", "label.jpg", "image/jpeg", content);

        assertThatThrownBy(() -> service.validateImage(file))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("10 MB or smaller");
    }
}
