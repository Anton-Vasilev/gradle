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

package org.gradle.configurationcache

import org.gradle.internal.build.ExecutionResult
import org.gradle.internal.buildtree.BuildTreeWorkGraph
import org.gradle.internal.buildtree.BuildTreeWorkGraphDecorator
import java.util.function.Consumer

class ConfigurationCacheAwareBuildTreeWorkGraphDecorator(
    private val inputAccessListener: InstrumentedInputAccessListener
) : BuildTreeWorkGraphDecorator {
    override fun decorateWorkGraph(original: BuildTreeWorkGraph): BuildTreeWorkGraph {
        return ListeningWorkGraph(original)
    }


    private inner class ListeningWorkGraph(private val delegate: BuildTreeWorkGraph) : BuildTreeWorkGraph {
        override fun scheduleWork(action: Consumer<in BuildTreeWorkGraph.Builder>?) {
            delegate.scheduleWork(action)
        }

        override fun runWork(): ExecutionResult<Void> {
            val wasTrackingEnabled = inputAccessListener.externalProcessTrackingEnabled
            inputAccessListener.externalProcessTrackingEnabled = false
            try {
                return delegate.runWork()
            } finally {
                inputAccessListener.externalProcessTrackingEnabled = wasTrackingEnabled
            }
        }
    }
}
