plugins {
    id("myproject.java-conventions")
    application
    id("jacoco-report-aggregation")
}

dependencies {
    implementation(project(":list"))
    implementation(project(":utilities"))
}

application {
    mainClass.set("org.gradle.sample.Main")
}

// Make JaCoCo aggregate report generation part of the 'check' lifecycle phase
tasks.check {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
}
