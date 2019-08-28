/*
 * Copyright (C) 2017-2019 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

package com.here.ort.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode

import com.here.ort.spdx.LicenseFileMatcher

/**
 * The result of a single scan of a single package.
 */
data class ScanResult(
    /**
     * Provenance information about the scanned source code.
     */
    val provenance: Provenance,

    /**
     * Details about the used scanner.
     */
    val scanner: ScannerDetails,

    /**
     * A summary of the scan results.
     */
    val summary: ScanSummary,

    /**
     * The raw output of the scanner. Can be null if the raw result shall not be included. If the raw result is
     * empty it must not be null but [EMPTY_JSON_NODE].
     */
    @JsonAlias("rawResult")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val rawResult: JsonNode? = null
) {
    /**
     * Filter all detected licenses and copyrights from the [summary] which are underneath [path], and set the [path]
     * for [provenance]. Findings which are matched by [LicenseFileMatcher.DEFAULT_MATCHER] are also kept.
     */
    fun filterPath(path: String): ScanResult {
        fun TextLocation.matchesPath() =
            this.path.startsWith("$path/") || LicenseFileMatcher.DEFAULT_MATCHER.matches(this.path)

        val newProvenance = provenance.copy(
            vcsInfo = provenance.vcsInfo?.copy(path = path),
            originalVcsInfo = provenance.originalVcsInfo?.copy(path = path)
        )

        val licenseFindings = summary.licenseFindings.filter { it.location.matchesPath() }.toSortedSet()
        val copyrightFindings = summary.copyrightFindings.filter { it.location.matchesPath() }.toSortedSet()
        val fileCount = (licenseFindings.map { it.location.path } + copyrightFindings.map { it.location.path })
            .distinct().size

        val summary = summary.copy(
            fileCount = fileCount,
            licenseFindings = licenseFindings,
            copyrightFindings = copyrightFindings
        )

        return ScanResult(newProvenance, scanner, summary)
    }
}
