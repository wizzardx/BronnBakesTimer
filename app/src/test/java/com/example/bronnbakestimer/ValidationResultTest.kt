package com.example.bronnbakestimer

import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("FunctionMaxLength")
class InputValidatorTest {

    private lateinit var validator: InputValidator

    @Before
    fun setUp() {
        validator = InputValidator()
    }

    @Test
    fun `validateInput returns Valid for valid input`() {
        val validInput = "ValidInputTest"
        val result = validator.validateInput(validInput)
        assertTrue(result.isValid, "Input should be valid")
    }

    @Test
    fun `validateInput returns Invalid for invalid input`() {
        val invalidInput = "Bad"
        val result = validator.validateInput(invalidInput)
        assertFalse(result.isValid, "Input should be invalid")
        assertTrue(result.isInvalid, "Result should be of type Invalid")
    }
}

class InputValidator {

    fun validateInput(input: String): ValidationResult {
        // Hypothetical validation logic: for example, consider valid if input length is more than 5
        return if (input.length > 5) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Input must be longer than 5 characters.")
        }
    }
}
