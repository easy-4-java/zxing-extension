package com.google.zxing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.zxing.model.AztecCodeRequest;
import com.google.zxing.model.CodeOutput;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AztecCodesTests {

    private static final String CONTENT = "https://example.com/aztec";

    @TempDir
    Path tempDirectory;

    @Test
    void defaultAndCustomRequestsRoundTrip() {
        CodeOutput defaultOutput = AztecCodes.encode(CONTENT);
        CodeOutput customOutput = AztecCodes.encode(AztecCodeRequest.builder(CONTENT)
                .size(320, 280)
                .errorCorrectionPercent(40)
                .margin(4)
                .build());

        assertThat(defaultOutput.dataUri()).startsWith("data:image/png;base64,");
        assertThat(AztecCodes.decode(defaultOutput.getBytes()).getText()).isEqualTo(CONTENT);
        assertThat(AztecCodes.decode(customOutput.getBytes()).getText()).isEqualTo(CONTENT);
    }

    @Test
    void decodeSupportsCommonInputTypes() throws IOException {
        CodeOutput output = AztecCodes.encode(CONTENT);
        Path path = tempDirectory.resolve("aztec.png");
        Files.write(path, output.getBytes());

        assertThat(AztecCodes.decode(output.image().orElseThrow(AssertionError::new)).getText()).isEqualTo(CONTENT);
        assertThat(AztecCodes.decode(path).getText()).isEqualTo(CONTENT);
        assertThat(AztecCodes.decode(path.toFile()).getText()).isEqualTo(CONTENT);
        assertThat(AztecCodes.decode(new ByteArrayInputStream(output.getBytes())).getText()).isEqualTo(CONTENT);
    }

    @Test
    void rejectsInvalidErrorCorrectionPercentage() {
        assertThatThrownBy(() -> AztecCodeRequest.builder(CONTENT).errorCorrectionPercent(101).build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
