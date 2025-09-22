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
package com.vitorpamplona.amethyst.service.location

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.LruCache
import com.vitorpamplona.quartz.utils.Log

/**
 * Maintains a cache of geohashes and city,country pairs.
 */
object CachedReversedGeoLocations {
    val locationNames = LruCache<String, String>(20)

    fun cached(geoHashStr: String): String? = locationNames[geoHashStr]

    fun geoLocate(
        geoHashStr: String,
        location: Location,
        context: Context,
        onReady: (String?) -> Unit,
    ) {
        locationNames[geoHashStr]?.let {
            onReady(it)
        }

        if (Geocoder.isPresent()) {
            ReverseGeolocation.execute(location, context) { cityNames ->
                if (cityNames != null) {
                    val cityName =
                        cityNames.firstNotNullOfOrNull {
                            val name = it.toCityCountry()
                            if (!name.isBlank()) {
                                name
                            } else {
                                null
                            }
                        }
                    if (cityName != null) {
                        locationNames.put(geoHashStr, cityName)
                        onReady(cityName)
                    }
                } else {
                    // error
                    onReady(null)
                }
            }
        } else {
            Log.d("ReverseGeoLocation", "Geocoder not present")
        }
    }
}
