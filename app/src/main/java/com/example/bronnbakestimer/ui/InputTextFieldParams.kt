package com.example.bronnbakestimer.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.KeyboardType

/**
 * Data class representing parameters for the InputTextField composable function.
 *
 * This class serves as a container for various parameters used to configure the appearance and behavior of
 * InputTextField, a customizable text field component in the UI. It includes parameters for handling text values,
 * error states, UI modifications, input methods, and focus management.
 *
 * @property errorMessage An optional String representing an error message. If non-null, it indicates that the TextField
 *                        is in an error state and this message should be displayed.
 * @property value A String that holds the current text displayed in the TextField.
 * @property onValueChange A lambda function that is called when the text in the TextField changes. It takes the new
 *                         text value as its parameter.
 * @property labelText A String specifying the label text to be shown above the TextField, aiding in user input
 *                     identification.
 * @property modifier An instance of Modifier used for styling and arranging the TextField within the Compose layout.
 * @property enabled A Boolean flag indicating whether the TextField is interactive (true) or disabled (false).
 * @property keyboardType An instance of KeyboardType specifying the type of keyboard to be shown for text input,
 *                        allowing customization based on the expected input (like number, text, etc.).
 * @property focusRequester An optional FocusRequester, used for programmatic focus control of the TextField. It's
 *                          nullable, indicating its use is optional based on UI requirements.
 * @property bringIntoViewRequester An optional BringIntoViewRequester, used for automatically scrolling the TextField
 *                                  into view when it gains focus. It's nullable, signifying its optional utilization
 *                                  in different UI contexts.
 */
data class InputTextFieldParams
@OptIn(ExperimentalFoundationApi::class)
constructor(
    val errorMessage: String?,
    val value: String,
    val onValueChange: (String) -> Unit,
    val labelText: String,
    val modifier: Modifier = Modifier,
    val enabled: Boolean,
    val keyboardType: KeyboardType,
    val focusRequester: FocusRequester?,
    val bringIntoViewRequester: BringIntoViewRequester?,
)
