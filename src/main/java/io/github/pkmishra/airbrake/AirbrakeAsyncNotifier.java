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

import java.util.concurrent.Executors;
/*
 Airbrake notified is synchronous call to airbrake api. This notifier is decorated Airbrake notifier with async support.
 */
class AirbrakeAsyncNotifier {

    private final AirbrakeNotifier airbrakeNotifier;

    public AirbrakeAsyncNotifier(AirbrakeNotifier airbrakeNotifier) {
        this.airbrakeNotifier = airbrakeNotifier;
    }

    public void notifyAsync(final AirbrakeNotice notice){
        Runnable runnable = () -> airbrakeNotifier.notify(notice);
        Executors.newSingleThreadExecutor().submit(runnable);
    }
    public int notify(AirbrakeNotice notice) {
        return airbrakeNotifier.notify(notice);
    }

}
