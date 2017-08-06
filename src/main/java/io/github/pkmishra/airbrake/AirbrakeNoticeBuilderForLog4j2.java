/**
 * Copyright (C) 2017 Pradeep Mishra (https://pkmishra.github.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.pkmishra.airbrake;

import airbrake.AirbrakeNoticeBuilder;
import airbrake.Backtrace;
import airbrake.BacktraceLine;
import org.apache.logging.log4j.ThreadContext;

import java.util.LinkedList;
import java.util.Map;

class AirbrakeNoticeBuilderForLog4j2 extends AirbrakeNoticeBuilder {

	AirbrakeNoticeBuilderForLog4j2(final String apiKey, final Throwable throwable, final String environment) {
		super(apiKey,new Backtrace(new LinkedList<>()),throwable, environment);
		setCommonFilters();
	}

	AirbrakeNoticeBuilderForLog4j2(final String apiKey, final String errorMessage,
								   StackTraceElement stackTrace, final String environment) {
		super(apiKey, errorMessage, environment);
		if (stackTrace != null) {
			LinkedList<String> list = new LinkedList<>();
			list.add(new BacktraceLine(stackTrace.getClassName(), stackTrace.getFileName(), stackTrace.getLineNumber(), stackTrace
					.getMethodName()).toString());
			backtrace(new Backtrace(list));
		}
		setCommonFilters();
	}

	private void setCommonFilters(){
		environment(System.getProperties());
		addMDCToSession();
		standardEnvironmentFilters();
		ec2EnvironmentFilters();
	}

	private void addMDCToSession() {
		Map<String, String> contextMap = ThreadContext.getContext();
		if (contextMap != null) {
			addSessionKey(":key", Integer.toString(contextMap.hashCode()));
			addSessionKey(":data", contextMap);
		}
	}

}
