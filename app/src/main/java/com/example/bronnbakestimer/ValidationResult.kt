package com.example.bronnbakestimer

/**
 * A sealed class representing the result of a validation operation.
 *
 * This class is used to encapsulate the result of a validation operation. It has two possible states:
 * Valid and Invalid. The Valid object represents a successful validation, while the Invalid data class
 * represents a failed validation and includes a reason for the failure.
 *
 * The isValid and isInvalid properties can be used to easily check the result of the validation.
 */
sealed class ValidationResult {

    /**
     * A data class representing an invalid validation result.
     *
     * This class includes a reason property that should provide a clear explanation of why the validation failed.
     *
     * @property reason A string explaining why the validation failed.
     */
    data class Invalid(val reason: String) : ValidationResult()

    /**
     * An object representing a valid validation result.
     *
     * This object is used to represent a successful validation. It does not include any additional information.
     */
    data object Valid : ValidationResult()

    /**
     * A boolean property indicating whether the validation result is valid.
     *
     * This property is true if the ValidationResult is a Valid object, and false otherwise.
     */
    val isValid: Boolean
        get() = this is Valid

    /**
     * A boolean property indicating whether the validation result is invalid.
     *
     * This property is true if the ValidationResult is an Invalid object, and false otherwise.
     */
    val isInvalid: Boolean
        get() = this is Invalid
}
