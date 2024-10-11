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
package de.xoev.xta.test.app.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Validated
public class ClientProperties {

    private ClientConnectionProperties serverUrl;

    private boolean checkHostnameInCertificate = true;

    @NotNull
    private String keyStore;

    @NotNull
    private String keyStorePassword;

    @NotNull
    private String keyPassword;

    private String keyAlias;

    @NotNull
    private String trustStore;

    @NotNull
    private String trustStorePassword;

    public ClientProperties() {
    }

    public ClientProperties(final ClientProperties cp) {
        serverUrl = new ClientConnectionProperties(cp.getServerUrl());
        checkHostnameInCertificate = cp.isCheckHostnameInCertificate();
        keyStore = cp.getKeyStore();
        keyStorePassword = cp.getKeyStorePassword();
        keyPassword = cp.getKeyPassword();
        keyAlias = cp.getKeyAlias();
        trustStore = cp.getTrustStore();
        trustStorePassword = cp.getTrustStorePassword();
    }

    @JsonIgnore
    public Resource getKeyStoreAsResource() {
        return getResource(keyStore);
    }

    @JsonIgnore
    public Resource getTrustStoreAsResource() {
        return getResource(trustStore);
    }

    private Resource getResource(final String resourceLocation) {
        if (resourceLocation.startsWith("classpath:")) {
            return new ClassPathResource(resourceLocation.substring("classpath:".length()));
        }
        if (resourceLocation.startsWith("file:")) {
            return new FileSystemResource(resourceLocation.substring("file:".length()));
        }
        return new FileSystemResource(resourceLocation);
    }
}
