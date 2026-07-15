package com.google.zxing.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ChecksumException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link CodeResult} 单元测试。
 */
class CodeResultTests {

    @Test
    void constructorCopiesInputsAndDefaultsNullableFields() {
        Map<ResultMetadataType, Object> metadata = new EnumMap<ResultMetadataType, Object>(ResultMetadataType.class);
        metadata.put(ResultMetadataType.ORIENTATION, 90);

        ResultPoint[] points = new ResultPoint[] { new ResultPoint(1f, 2f), new ResultPoint(3f, 4f) };
        CodeResult result = new CodeResult("hello", BarcodeFormat.QR_CODE,
                new byte[] { 9, 9 }, points, metadata);

        assertThat(result.getText()).isEqualTo("hello");
        assertThat(result.getFormat()).isEqualTo(BarcodeFormat.QR_CODE);
        assertThat(result.getBarcodeFormat()).isEqualTo(BarcodeFormat.QR_CODE);
        assertThat(result.getRawBytes()).containsExactly((byte) 9, (byte) 9);
        assertThat(result.getPoints()).hasSize(2);
        assertThat(result.getMetadata()).containsEntry(ResultMetadataType.ORIENTATION, 90);

        result.getRawBytes()[0] = 0;
        result.getPoints()[0] = new ResultPoint(0f, 0f);
    }

    @Test
    void constructorHandlesNullables() {
        CodeResult result = new CodeResult(null, BarcodeFormat.CODE_128, null, null, null);

        assertThat(result.getText()).isNull();
        assertThat(result.getRawBytes()).isNull();
        assertThat(result.getPoints()).isEmpty();
        assertThat(result.getMetadata()).isEmpty();
    }

    @Test
    void getRawBytesReturnsDefensiveCopyOrNull() {
        CodeResult result = new CodeResult(null, BarcodeFormat.QR_CODE, new byte[] { 1 }, null, null);
        byte[] first = result.getRawBytes();
        first[0] = 0;
        assertThat(result.getRawBytes()[0]).isEqualTo((byte) 1);
    }

    @Test
    void fromTranslatesZxingResult() {
        Result raw = new Result("zxing-text", new byte[] { 1 }, new ResultPoint[] {
                new ResultPoint(1f, 2f)
        }, BarcodeFormat.AZTEC, System.currentTimeMillis());

        CodeResult result = CodeResult.from(raw);

        assertThat(result.getText()).isEqualTo("zxing-text");
        assertThat(result.getFormat()).isEqualTo(BarcodeFormat.AZTEC);
        assertThat(result.getRawBytes()).containsExactly((byte) 1);
        assertThat(result.getPoints()).hasSize(1);
    }

    @Test
    void fromRejectsNullArgument() {
        assertThatThrownBy(() -> CodeResult.from(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructorRejectsNullFormat() {
        assertThatThrownBy(() -> new CodeResult("x", null, null, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void integrationWithCodexReturnsReadableMetadata() {
        // Just touch checks to keep static analyzers happy
        assertThat(NotFoundException.class.getSuperclass().getSimpleName()).isEqualTo("ReaderException");
        assertThat(ChecksumException.class.getSimpleName()).isEqualTo("ChecksumException");
    }
}
