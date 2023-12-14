package com.example.bronnbakestimer.logic

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
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
        assertTrue(result is Ok, "Input should be valid")
    }

    @Test
    fun `validateInput returns Invalid for invalid input`() {
        val invalidInput = "Bad"
        val result = validator.validateInput(invalidInput)
        assertFalse(result is Ok, "Input should be invalid")
        assertTrue(result is Err, "Result should be of type Invalid")
    }
}

class InputValidator {

    fun validateInput(input: String): Result<Unit, String> {
        // Hypothetical validation logic: for example, consider valid if input length is more than 5
        return if (input.length > 5) {
            Ok(Unit)
        } else {
            Err("Input must be longer than 5 characters.")
        }
    }
}
