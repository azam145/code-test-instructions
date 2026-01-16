package com.tpximpact.urlshortener.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AliasGeneratorTest {

    @Test
    @DisplayName("Should generate a string of the specified length")
    void shouldGenerateStringOfCorrectLength() {
        int length = 7;
        String alias = AliasGenerator.generate(length);
        assertThat(alias).hasSize(length);
    }

    @Test
    @DisplayName("Should only contain Base62 characters")
    void shouldContainOnlyBase62Characters() {
        String alias = AliasGenerator.generate(50);
        // Base62 = a-z, A-Z, 0-9
        assertThat(alias).matches("^[a-zA-Z0-9]*$");
    }
}