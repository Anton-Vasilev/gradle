plugins {
    base
    id("jacoco-report-aggregation")
}

repositories {
    mavenCentral()
}

dependencies {
    jacocoAggregation(project(":application"))
}

reporting {
    reports {
        val testCodeCoverageReport by registering(JacocoCoverageReport::class) {
            testType.set(TestType.UNIT_TESTS)
        }
    }
}

// Make JaCoCo aggregate report generation part of the 'check' lifecycle phase
tasks.check {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
}
