package com.google.zxing.exception;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link CodeException} 单元测试。
 */
class CodeExceptionTests {

    @Test
    void messageOnlyConstructor() {
        CodeException ex = new CodeException("boom");

        assertThat(ex.getMessage()).isEqualTo("boom");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void constructorWithCause() {
        IOException cause = new IOException("io");
        CodeException ex = new CodeException("boom", cause);

        assertThat(ex.getCause()).isSameAs(cause);
        assertThat(ex.getMessage()).isEqualTo("boom");
    }
}
