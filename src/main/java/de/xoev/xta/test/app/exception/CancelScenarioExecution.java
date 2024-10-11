/*
* @formatter:off
* 
* Copyright 2021-2022  Koordinierungsstelle für IT-Standards (KoSIT)
*
* Licensed under the European Public License, Version 1.2 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     https://opensource.org/licenses/EUPL-1.2
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* @formatter:on
*/
package de.xoev.xta.test.app.exception;

public class CancelScenarioExecution extends RuntimeException {

    private static final long serialVersionUID = 4151823081812629009L;

    public CancelScenarioExecution(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CancelScenarioExecution(final String message) {
        super(message);
    }

    public CancelScenarioExecution(final Throwable cause) {
        super(cause);
    }
}
