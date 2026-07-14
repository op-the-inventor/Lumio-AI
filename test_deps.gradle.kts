repositories {
    mavenCentral()
    google()
    maven { url = uri("https://jitpack.io") }
}

configurations {
    create("testConfig")
}

dependencies {
    "testConfig"("de.kherud.llama:llama4j:2.3.1")
}

tasks.register("resolveDependencies") {
    doLast {
        configurations.getByName("testConfig").resolve().forEach { println(it) }
    }
}
