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

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProtocolMetadata {

    @Builder.Default
    private String softwareManufacturer = "Nortal AG";
    @Builder.Default
    private String softwareName = "Dummy Produkt";
    @Builder.Default
    private String softwareVersion = "1.0.0";

    @Builder.Default
    private String street = "Knesebeckstraße";
    @Builder.Default
    private String streetNo = "1";
    @Builder.Default
    private String zipCode = "10623";
    @Builder.Default
    private String city = "Berlin";
    private String addressAddition;

    @JsonIgnore
    public List<String> getAddressAdditionAsList() {
        if (addressAddition != null) {
            String[] split = addressAddition.split("\r\n|\n|\r");
            return List.of(split);
        }
        return Collections.emptyList();
    }
}
