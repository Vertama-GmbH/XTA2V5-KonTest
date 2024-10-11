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
package de.xoev.xta.test.app.model;

import java.nio.file.Path;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(value = JsonInclude.Include.ALWAYS)
public class ContainerCustomization {

    private Boolean useCustomContainer;

    private Boolean useEncyptedContainer;

    private String contentDescription;

    @Builder.Default
    private String contentType = "application/octet-stream";

    @Builder.Default
    private String encoding = "UTF-8";

    private String filename;

    private Path tempFile;

    @Builder.Default
    private String id = "_" + UUID.randomUUID().toString();

    @Builder.Default
    private String lang = "de";

    @Builder.Default
    private long size = 0;

    private String encodedXMLContent;

}
