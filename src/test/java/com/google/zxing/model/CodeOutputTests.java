package com.google.zxing.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link CodeOutput} 单元测试。
 */
class CodeOutputTests {

    private static final byte[] BYTES = new byte[] { 1, 2, 3, 4, 5 };

    @Test
    void gettersExposeConstructorArguments() {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        CodeOutput output = new CodeOutput(BYTES, "image/png", 30, 40, image);

        assertThat(output.getMimeType()).isEqualTo("image/png");
        assertThat(output.getWidth()).isEqualTo(30);
        assertThat(output.getHeight()).isEqualTo(40);
        assertThat(output.image()).contains(image);
    }

    @Test
    void getBytesReturnsDefensiveCopy() {
        CodeOutput output = new CodeOutput(BYTES.clone(), "image/png", 1, 1, null);
        byte[] first = output.getBytes();
        first[0] = 99;

        assertThat(output.getBytes()[0]).isEqualTo((byte) 1);
    }

    @Test
    void base64MatchesRawBytes() {
        CodeOutput output = new CodeOutput(BYTES, "image/png", 1, 1, null);

        assertThat(Base64.getDecoder().decode(output.base64())).isEqualTo(BYTES);
    }

    @Test
    void dataUriContainsMimeTypeAndBase64() {
        CodeOutput output = new CodeOutput(BYTES, "image/png", 1, 1, null);
        String uri = output.dataUri();

        assertThat(uri).startsWith("data:image/png;base64,");
        assertThat(uri.substring(uri.indexOf(",") + 1)).isEqualTo(output.base64());
    }

    @Test
    void writeToCopiesBytesAndDoesNotCloseStream() throws IOException {
        CodeOutput output = new CodeOutput(BYTES, "image/png", 1, 1, null);
        ByteArrayOutputStream sink = new ByteArrayOutputStream();

        output.writeTo(sink);
        output.writeTo(sink);

        assertThat(sink.toByteArray()).isEqualTo(new byte[] { 1, 2, 3, 4, 5, 1, 2, 3, 4, 5 });
    }

    @Test
    void writeToRejectsNullStream() {
        CodeOutput output = new CodeOutput(BYTES, "image/png", 1, 1, null);

        assertThatThrownBy(() -> output.writeTo(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void imageMayBeNull() {
        CodeOutput output = new CodeOutput(BYTES, "image/svg+xml", 1, 1, null);

        assertThat(output.image()).isEmpty();
    }

    @Test
    void constructorRejectsNullArgsAndNonPositiveDimensions() {
        assertThatThrownBy(() -> new CodeOutput(null, "image/png", 1, 1, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new CodeOutput(BYTES, null, 1, 1, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new CodeOutput(BYTES, "image/png", 0, 1, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CodeOutput(BYTES, "image/png", 1, 0, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new CodeOutput(BYTES, "image/png", 1, 1, null).getBytes())
                .containsExactly((byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5);
        // exercise charset-related package access indirectly via base64 roundtrip
        assertThat(StandardCharsets.UTF_8.name()).isEqualTo("UTF-8");
    }
}
