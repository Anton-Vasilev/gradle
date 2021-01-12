/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.integtests

import org.gradle.integtests.fixtures.CrossVersionIntegrationSpec
import org.gradle.integtests.fixtures.TargetVersions
import org.gradle.util.GradleVersion
import org.junit.Assume

@TargetVersions(["6.8"])
class ArchiveTaskPluginCompatibilityCrossVersionTest extends CrossVersionIntegrationSpec {

    def "API methods removed from AbstractArchiveTask are still available for plugins"(String type) {
        setup:
        Assume.assumeTrue(previous.version.baseVersion <= GradleVersion.version("6.8") &&
                          current.version.baseVersion >= GradleVersion.version("7.0"))

        file("plugin/build.gradle") << """
            plugins {
                id 'java-gradle-plugin'
                id 'maven-publish'
            }

            group = 'org.example.plugin'
            version = '0.1'

            repositories {
                jcenter()
            }

            dependencies {
                testImplementation 'junit:junit:4.12'
            }

            gradlePlugin {
                plugins {
                    myplugin {
                        id = 'my.plugin'
                        implementationClass = 'org.example.MyPlugin'
                    }
                }
            }

            publishing {
                repositories {
                    maven {
                        name = "BuildRepo"
                        url = "file://\$project.projectDir/build/repo"
                    }
                }
            }
        """

        file('plugin/src/main/java/org/example/MyPlugin.java') << """
            package org.example;

            import java.io.File;
            import org.gradle.api.*;
            import org.gradle.api.tasks.bundling.*;
            import org.gradle.plugins.ear.*;

            public class MyPlugin implements Plugin<Project> {
                public void apply(Project project) {
                    project.getTasks().register("customArchive", ${type}.class, new Action<$type>(){
                        @Override
                        public void execute($type task) {
                            configureAndPrintValues(task);
                        }
                    });
                }

                static void configureAndPrintValues(AbstractArchiveTask task) {
                    task.setArchiveName("archiveName");
                    System.out.println("archiveName=" + task.getArchiveName());

                    task.setDestinationDir(new File("destinationDir"));
                    System.out.println("destinationDir=" + task.getDestinationDir());

                    task.setBaseName("baseName");
                    System.out.println("baseName=" + task.getBaseName());

                    task.setAppendix("appendix");
                    System.out.println("appendix=" + task.getAppendix());

                    task.setVersion("version");
                    System.out.println("version=" + task.getVersion());

                    task.setExtension("extension");
                    System.out.println("extension=" + task.getExtension());

                    task.setClassifier("classifier");
                    System.out.println("classifier=" + task.getClassifier());

                    System.out.println("archivePath=" + task.getArchivePath());
                }
            }
        """
        file('client/build.gradle') << """
            plugins {
                id 'java-library'
                id 'my.plugin' version '0.1'
            }

            repositories {
                jcenter()
            }
        """
        file('client/settings.gradle') << """
            pluginManagement {
                repositories {
                    maven {
                        url '../plugin/build/repo'
                    }
                    gradlePluginPortal()
                }
            }
        """

        when:
        version previous withTasks 'publish' inDirectory(file("plugin")) run()
        def result = version current requireDaemon() requireIsolatedDaemons() withTasks 'customArchive' inDirectory(file('client')) run()

        then:
        result.output.contains "archiveName=archiveName"
        result.output.contains "destinationDir=${file('client/destinationDir').absolutePath}"
        result.output.contains "baseName=baseName"
        result.output.contains "appendix=appendix"
        result.output.contains "version=version"
        result.output.contains "extension=extension"
        result.output.contains "classifier=classifier"
        result.output.contains "archivePath=${file('client/destinationDir/archiveName').absolutePath}"

        where:
        type << ["Zip", "Tar", "Jar", "War", "Ear", "org.gradle.jvm.tasks.Jar"]
    }
}