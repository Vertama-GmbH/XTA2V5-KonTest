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
package de.xoev.xta.test.app.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;

@Data
public class XtaId {

    public static final Pattern PATTERN = Pattern.compile("(\\w+):(.+):([^:]+):([^:]+)");
    private final String schema;
    private final String namespace;
    private final String application; // prefix
    private final String uuid; // identifier

    public XtaId(final String schema, final String namespace, final String application, final String uuid) {
        this.schema = schema;
        this.namespace = namespace;
        this.application = application;
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return schema + ":" + namespace + ":" + application + ":" + uuid;
    }

    public static Optional<XtaId> parse(final String messageId) {
        // page 119; structure: "urn:de:xta:messageid:<Präfix>:<Identifikator>"
        Matcher matcher = PATTERN.matcher(messageId);
        if (matcher.matches()) {
            return Optional
                    .of(new XtaId(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4)));
        }
        return Optional.empty();
    }
}
