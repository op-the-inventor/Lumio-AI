#!/bin/bash
sed -i '/alias(libs.plugins.google.devtools.ksp)/a \  kotlin("plugin.serialization") version "2.0.0"' app/build.gradle.kts
sed -i '/implementation("io.ktor:ktor-client-android:2.3.11")/a \  implementation("io.ktor:ktor-client-content-negotiation:2.3.11")\n  implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")' app/build.gradle.kts
