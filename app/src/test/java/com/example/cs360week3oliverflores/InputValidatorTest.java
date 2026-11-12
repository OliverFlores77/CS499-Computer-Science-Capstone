package com.example.cs360week3oliverflores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Local unit tests for InputValidator. They run on the development computer;
 * no emulator is needed.
 */
public class InputValidatorTest {

    // ----- credentials -----

    @Test
    public void validateCredentials_acceptsFilledFields() {
        assertNull(InputValidator.validateCredentials("user1", "pass1"));
    }

    @Test
    public void validateCredentials_rejectsEmptyOrBlankFields() {
        assertEquals(InputValidator.CREDENTIALS_ERROR, InputValidator.validateCredentials("", "pass1"));
        assertEquals(InputValidator.CREDENTIALS_ERROR, InputValidator.validateCredentials("user1", ""));
        assertEquals(InputValidator.CREDENTIALS_ERROR, InputValidator.validateCredentials("   ", "pass1"));
        assertEquals(InputValidator.CREDENTIALS_ERROR, InputValidator.validateCredentials("user1", "   "));
        assertEquals(InputValidator.CREDENTIALS_ERROR, InputValidator.validateCredentials(null, null));
    }

    // ----- weights -----

    @Test
    public void validateWeight_acceptsNormalWeights() {
        assertNull(InputValidator.validateWeight("150"));
        assertNull(InputValidator.validateWeight("150.5"));
        assertNull(InputValidator.validateWeight("  180  "));
        assertNull(InputValidator.validateWeight("150."));
    }

    @Test
    public void validateWeight_acceptsTheLimitsThemselves() {
        assertNull(InputValidator.validateWeight("20"));
        assertNull(InputValidator.validateWeight("1500"));
    }

    @Test
    public void validateWeight_rejectsEmptyInput() {
        assertEquals(InputValidator.WEIGHT_ERROR, InputValidator.validateWeight(""));
        assertEquals(InputValidator.WEIGHT_ERROR, InputValidator.validateWeight("   "));
        assertEquals(InputValidator.WEIGHT_ERROR, InputValidator.validateWeight(null));
    }

    @Test
    public void validateWeight_rejectsTextThatIsNotANumber() {
        // A lone "." used to crash the app.
        assertEquals(InputValidator.WEIGHT_ERROR, InputValidator.validateWeight("."));
        assertEquals(InputValidator.WEIGHT_ERROR, InputValidator.validateWeight("abc"));
        assertEquals(InputValidator.WEIGHT_ERROR, InputValidator.validateWeight("12.3.4"));
    }

    @Test
    public void validateWeight_rejectsZeroNegativeAndOutOfRangeValues() {
        assertEquals(InputValidator.WEIGHT_ERROR, InputValidator.validateWeight("0"));
        assertEquals(InputValidator.WEIGHT_ERROR, InputValidator.validateWeight("-5"));
        assertEquals(InputValidator.WEIGHT_ERROR, InputValidator.validateWeight("19.9"));
        assertEquals(InputValidator.WEIGHT_ERROR, InputValidator.validateWeight("1500.1"));
        assertEquals(InputValidator.WEIGHT_ERROR, InputValidator.validateWeight("99999"));
    }

    @Test
    public void validateWeight_rejectsNaNAndInfinity() {
        assertEquals(InputValidator.WEIGHT_ERROR, InputValidator.validateWeight("NaN"));
        assertEquals(InputValidator.WEIGHT_ERROR, InputValidator.validateWeight("Infinity"));
    }
}
