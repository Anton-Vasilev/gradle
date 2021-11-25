plugins {
    base
    id("test-report-aggregation")
}

//repositories {
//    mavenCentral()
//}

dependencies {
    testReportAggregation(project(":application")) // <1>
}

reporting {
    reports {
        val testAggregateTestReport by creating(AggregateTestReport::class) { // <2>
            testType.set(TestType.UNIT_TESTS)
        }
    }
}

// Optional: make aggregate test report generation part of the 'check' lifecycle phase
tasks.check {
    dependsOn(tasks.named<TestReport>("testAggregateTestReport"))
}
