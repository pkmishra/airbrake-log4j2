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

import airbrake.AirbrakeNotice;
import airbrake.AirbrakeNotifier;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;

@Plugin(name="AirbrakeLog4j2Appender", category="Core", elementType="appender", printObject=true)
public class AirbrakeLog4j2Appender extends AbstractAppender {

    private AirbrakeAsyncNotifier airbrakeNotifier;
    private final Boolean async;

    private AirbrakeLog4j2Appender(String name, Filter filter,
                                   Layout<? extends Serializable> layout, AirbrakeAsyncNotifier airbrakeNotifier, String apiKey, String env, Boolean enabled, Boolean async) {
        super(name, filter, layout, false);
        this.apiKey = apiKey;
        this.env = env;
        this.enabled = enabled;
        this.airbrakeNotifier = airbrakeNotifier;
        this.async = async;
    }

    public void append(LogEvent logEvent) {
        if (!enabled || notify == Notify.OFF) {
            return;
        }
        ThrowableProxy proxy;
        AirbrakeNotice notice = null;
        if ((proxy = logEvent.getThrownProxy()) != null) {
            Throwable throwable = proxy.getThrowable();
            notice = new AirbrakeNoticeBuilderForLog4j2(apiKey, throwable, env).newNotice();

        } else if (notify == Notify.ALL) {
            StackTraceElement stackTrace = logEvent.getSource();
            notice = new AirbrakeNoticeBuilderForLog4j2(apiKey, logEvent.getMessage().getFormattedMessage(), stackTrace, env).newNotice();

        }
        if(notice != null ) {
            if (this.async)
                airbrakeNotifier.notifyAsync(notice);
            else
                airbrakeNotifier.notify(notice);
        }

    }

    public enum Notify {
        ALL, EXCEPTIONS, OFF
    }

    void setAirbrakeNotifier(AirbrakeAsyncNotifier airbrakeNotifier) {
        this.airbrakeNotifier = airbrakeNotifier;
    }
    private final String apiKey;

    private final String env;

    private Notify notify = Notify.EXCEPTIONS; // default compatible with airbrake-java

    private boolean enabled = true;

    String getEnv() {
        return env;
    }

    void setNotify(Notify notify) {
        this.notify = notify;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @PluginFactory
    public static AirbrakeLog4j2Appender createAppender(
            @PluginAttribute("name") @Required(message = "Name must be provided for appender AirbrakeLog4j2Appender") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter,
            @PluginAttribute("apiKey")@Required(message = "apiKey must be specified to use AirbrakeLog4j2Appender")  String apiKey,
            @PluginAttribute("env") @Required(message = "Environment must be specified e.g. development, test, production") String env,
            @PluginAttribute("enabled") @Required(message = "Enabled must be set explicitly to use AirbrakeLog4j2Appender") Boolean enabled,
            @PluginAttribute(value = "async", defaultBoolean = true) Boolean async
            ) {

        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        return new AirbrakeLog4j2Appender(name, filter, layout, new AirbrakeAsyncNotifier(new AirbrakeNotifier()), apiKey,env,enabled, async);
    }
}
