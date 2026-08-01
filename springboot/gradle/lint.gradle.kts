// Apply this in build.gradle.kts:
//   apply(from = "gradle/lint.gradle.kts")

apply(plugin = "checkstyle")
apply(plugin = "com.diffplug.spotless")

// Checkstyle - static analysis
configure<CheckstyleExtension> {
    toolVersion = "10.21.4"
    configFile = file("config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
    maxWarnings = 0
}

// Spotless - code formatting
configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    java {
        importOrder(file("config/spotless/java.importorder"))
        removeUnusedImports()
        googleJavaFormat("1.25.2").aosp()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
