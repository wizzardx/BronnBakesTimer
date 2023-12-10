package com.example.bronnbakestimer

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Customized Text Field composable used by the BronnBakesTimer app.
 *
 * This composable function is responsible for rendering a customized text field within the app's UI.
 * It allows for displaying an optional error message, specifying the current text value, handling text
 * changes, providing a label, and enabling/disabling the text field.
 *
 * @param params A [InputTextFieldParams] object containing configuration parameters for the text field.
 *
 * @see InputTextFieldParams
 */
@Composable
fun InputTextField(params: InputTextFieldParams) {
    val (supportingText, isError) = getErrorInfoFor(params.errorMessage)
    TextField(
        value = params.value,
        onValueChange = params.onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = params.keyboardType),
        singleLine = true,
        label = { Text(text = params.labelText) },
        modifier = params.modifier.padding(top = 8.dp),
        enabled = params.enabled,
        supportingText = supportingText,
        isError = isError,
    )
}
