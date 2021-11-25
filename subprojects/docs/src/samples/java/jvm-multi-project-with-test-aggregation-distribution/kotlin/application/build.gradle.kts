plugins {
    id("myproject.java-conventions")
    application
    id("test-report-aggregation") // <1>
}

dependencies {
    implementation(project(":list"))
    implementation(project(":utilities"))
}

application {
    mainClass.set("org.gradle.sample.Main")
}

// Optional: make aggregate test report generation part of the 'check' lifecycle phase
tasks.check {
    dependsOn(tasks.named<TestReport>("testAggregateTestReport"))
}
