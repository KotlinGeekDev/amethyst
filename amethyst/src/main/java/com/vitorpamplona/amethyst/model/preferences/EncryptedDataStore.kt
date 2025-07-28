/**
 * Copyright (c) 2025 Vitor Pamplona
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
package com.vitorpamplona.amethyst.model.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.vitorpamplona.amethyst.ui.navigation.navs.EmptyNav.scope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Base64

class EncryptedDataStore(
    private val store: DataStore<Preferences>,
    private val encryption: KeyStoreEncryption = KeyStoreEncryption(),
) {
    private fun decode(str: String): ByteArray = Base64.getDecoder().decode(str)

    private fun encode(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    private fun encrypt(value: String): String = encode(encryption.encrypt(value.toByteArray()))

    private fun decrypt(value: String): String = encryption.decrypt(decode(value)).contentToString()

    suspend fun remove(key: Preferences.Key<String>) {
        store.edit { prefs ->
            prefs.remove(key)
        }
    }

    suspend fun save(
        key: Preferences.Key<String>,
        value: String,
    ) {
        store.edit { prefs ->
            prefs[key] = encrypt(value)
        }
    }

    suspend fun get(key: Preferences.Key<String>): String? =
        store.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences()) else throw e
            }.firstOrNull()
            ?.get(key)
            ?.let { decrypt(it) }

    suspend fun <T> getProperty(
        key: Preferences.Key<String>,
        parser: (String) -> T,
        serializer: (T) -> String,
    ): UpdatablePropertyFlow<T> =
        UpdatablePropertyFlow<T>(
            flow =
                store.data
                    .catch { e ->
                        if (e is IOException) emit(emptyPreferences()) else throw e
                    }.map { prefs ->
                        val value = prefs[key]
                        if (value != null) {
                            val decrypted = decrypt(value)
                            if (decrypted.isNotBlank()) {
                                parser(decrypted)
                            } else {
                                null
                            }
                        } else {
                            null
                        }
                    },
            update = { newValue ->
                if (newValue != null) {
                    val serialized = serializer(newValue)
                    if (serialized.isNotBlank()) {
                        save(key, serialized)
                    } else {
                        remove(key)
                    }
                } else {
                    remove(key)
                }
            },
            scope = scope,
        )
}
