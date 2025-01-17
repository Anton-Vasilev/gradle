plugins {
    // because this snippet is used in TestReportIntegrationTest which rewrites build files for different JUnit flavors
    java
}

// tag::test-report[]
val testReportData by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named<Category>(Category.DOCUMENTATION))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named<DocsType>("test-report-data"))
    }
}

dependencies {
    testReportData(project(":core"))
    testReportData(project(":util"))
}

tasks.register<TestReport>("testReport") {
    destinationDirectory.set(reporting.baseDirectory.dir("allTests"))
    // Use test results from testReportData configuration
    testResults.from(testReportData)
}
// end::test-report[]
