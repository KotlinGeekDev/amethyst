/**
 * Copyright (c) 2024 Vitor Pamplona
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.vitorpamplona.amethyst.ui.note.creators.secretEmoji

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assistant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.commons.emojicoder.EmojiCoder
import com.vitorpamplona.amethyst.ui.stringRes
import com.vitorpamplona.amethyst.ui.theme.DividerThickness
import com.vitorpamplona.amethyst.ui.theme.QuoteBorder
import com.vitorpamplona.amethyst.ui.theme.Size20Modifier
import com.vitorpamplona.amethyst.ui.theme.placeholderText

@Composable
fun SecretEmojiRequest(onSuccess: (String) -> Unit) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Assistant,
                null,
                modifier = Size20Modifier,
                tint = Color.Unspecified,
            )

            Text(
                text = stringRes(R.string.secret_emoji_maker),
                fontSize = 20.sp,
                fontWeight = FontWeight.W500,
                modifier = Modifier.padding(start = 10.dp),
            )
        }

        HorizontalDivider(thickness = DividerThickness)

        var secretMessage by remember { mutableStateOf("") }
        var publicPrefix by remember { mutableStateOf("") }

        OutlinedTextField(
            label = { Text(text = stringRes(R.string.secret_note_to_receiver)) },
            modifier = Modifier.fillMaxWidth(),
            value = secretMessage,
            onValueChange = { secretMessage = it },
            placeholder = {
                Text(
                    text = stringRes(R.string.secret_note_to_receiver_placeholder),
                    color = MaterialTheme.colorScheme.placeholderText,
                )
            },
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
            singleLine = true,
        )

        OutlinedTextField(
            label = { Text(text = stringRes(R.string.secret_visible_text)) },
            modifier = Modifier.fillMaxWidth(),
            value = publicPrefix,
            onValueChange = { publicPrefix = it },
            placeholder = {
                Text(
                    text = stringRes(R.string.secret_visible_text_placeholder),
                    color = MaterialTheme.colorScheme.placeholderText,
                )
            },
            singleLine = true,
        )

        Button(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            onClick = {
                onSuccess(EmojiCoder.encode(publicPrefix, secretMessage))
            },
            shape = QuoteBorder,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
        ) {
            Text(
                text = stringRes(R.string.secret_add_to_text),
                color = Color.White,
                fontSize = 20.sp,
            )
        }
    }
}
