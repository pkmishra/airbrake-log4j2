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
import io.github.pkmishra.airbrake.AirbrakeLog4j2Appender.Notify;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class AirbrakeLog4j2AppenderTest {
    private final AirbrakeNotifier notifier;


    private AirbrakeLog4j2Appender getAppender() {
        return  (AirbrakeLog4j2Appender) logger.getAppenders().get("Airbrake");
    }

    private final AirbrakeLog4j2Appender appender;
    private final Logger logger;
    private final static String API_KEY = "test_key";

    private final ArgumentCaptor<AirbrakeNotice> noticeArgumentCaptor;

    public AirbrakeLog4j2AppenderTest() {
        noticeArgumentCaptor = ArgumentCaptor.forClass(AirbrakeNotice.class);
        notifier = Mockito.mock(AirbrakeNotifier.class);
        Mockito.when(notifier.notify(noticeArgumentCaptor.capture())).thenReturn(0);

        final LoggerContext context = (LoggerContext) LogManager.getContext(false);
        final Configuration config = context.getConfiguration();
        PatternLayout layout = PatternLayout.createDefaultLayout(config);
        ThresholdFilter filter = ThresholdFilter.createFilter(Level.ALL, null, null);
        appender = AirbrakeLog4j2Appender.createAppender("Airbrake", layout,
                filter, API_KEY, "test", true, true);
        appender.setAirbrakeNotifier(new AirbrakeAsyncNotifier(notifier));
        appender.start();
        logger = (Logger) LogManager.getLogger(getClass().getName());
        logger.addAppender(appender);
    }

    @Before
    public void before() {
        getAppender().setNotify(Notify.EXCEPTIONS);
        Mockito.reset(notifier);
        getAppender().setAirbrakeNotifier(new AirbrakeAsyncNotifier(notifier));

    }


    @Test
    public void testWhenExceptionIsLoggedCorrectNoticeIsSentToAirbrake() throws InterruptedException {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid Argument Message");
        logger.error("This is error message", exception);
        //Sleep is to take care of async processing
        Thread.sleep(1000);
        Mockito.verify(notifier).notify(noticeArgumentCaptor.capture());
        AirbrakeNotice notice = noticeArgumentCaptor.getValue();
        assertEquals(notice.errorClass(),exception.getClass().getName());
        assertEquals(notice.errorMessage(), exception.getMessage());

        String topTraceLine = notice.backtrace().iterator().next();
        assertTrue(topTraceLine.startsWith("at io.github.pkmishra.airbrake.AirbrakeLog4j2AppenderTest.testWhenExceptionIsLoggedCorrectNoticeIsSentToAirbrake"));

        assertEquals(notice.env(), getAppender().getEnv());
        assertEquals(notice.apiKey(), API_KEY);
        assertNull(notice.component());
        assertNull(notice.projectRoot());
    }

    @Test
    public void testWhenSimpleErrorWithoutThrowablesIsSentToAirbrake() throws InterruptedException {
        getAppender().setNotify(Notify.ALL);
        logger.error("This is error message");
        Thread.sleep(1000);

        Mockito.verify(notifier).notify(noticeArgumentCaptor.capture());
        AirbrakeNotice notice = noticeArgumentCaptor.getValue();

        assertNull(notice.errorClass());
        assertEquals(notice.errorMessage(), "This is error message");
        String topTraceLine = notice.backtrace().iterator().next();
        assertTrue(topTraceLine.startsWith("at io.github.pkmishra.airbrake.AirbrakeLog4j2AppenderTest.testWhenSimpleErrorWithoutThrowablesIsSentToAirbrake"));

    }

    @Test
    public void testWhenSetToReceiveExceptionsOnlyNonExceptionsAreNotSentToAirbrake() throws InterruptedException {
        getAppender().setNotify(Notify.EXCEPTIONS);
        logger.error("This is error message");
        Thread.sleep(1000);
        Mockito.verifyZeroInteractions(notifier);

        IllegalArgumentException exception = new IllegalArgumentException("This is exception message");
        logger.error("This is error message", exception);
        Thread.sleep(1000);

        Mockito.verify(notifier).notify(noticeArgumentCaptor.capture());
        AirbrakeNotice notice = noticeArgumentCaptor.getValue();

        assertEquals(notice.errorClass(), exception.getClass().getName());
        assertEquals(notice.errorMessage(), exception.getMessage());
    }

    @Test
    public void testWhenSetToOFFNoMessagesAreSentToAirbrake() {
        getAppender().setNotify(Notify.OFF);
        logger.error("This is error message");
        Mockito.verifyZeroInteractions(notifier);

        logger.error("This is error message", new IllegalArgumentException("This is exception message"));
        Mockito.verifyZeroInteractions(notifier);
    }

    @Test
    public void testAppenderEnableDisable() throws InterruptedException {
        getAppender().setEnabled(false);
        logger.error("This is error message");
        logger.error("This is error message", new NullPointerException("Test test test"));
        Thread.sleep(1000);
        Mockito.verifyZeroInteractions(notifier);

        getAppender().setEnabled(true);
        logger.error("This is error message", new NullPointerException("Test test test"));
        Thread.sleep(1000);
        Mockito.verify(notifier).notify(noticeArgumentCaptor.capture());
    }

}