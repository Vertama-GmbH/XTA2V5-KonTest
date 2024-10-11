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

import lombok.Getter;

public enum ScenarioRole {

    AUTOR_ASYNCHRON(Role.AUTOR, RoleType.ASYNCHRON),
    AUTOR_SYNCHRON(Role.AUTOR, RoleType.SYNCHRON),
    SENDER_ASYNCHRON(Role.SENDER, RoleType.ASYNCHRON),
    SENDER_SYNCHRON(Role.SENDER, RoleType.SYNCHRON),
    EMPFAENGER_ASYNCHRON(Role.EMPFAENGER, RoleType.ASYNCHRON),
    EMPFAENGER_SYNCHRON(Role.EMPFAENGER, RoleType.SYNCHRON),
    LESER_ASYNCHRON(Role.LESER, RoleType.ASYNCHRON),
    LESER_SYNCHRON(Role.LESER, RoleType.SYNCHRON);

    @Getter
    private Role role;
    @Getter
    private RoleType roleType;

    ScenarioRole(final Role role, final RoleType roleType) {
        this.role = role;
        this.roleType = roleType;
    }

    public Role getRoleTestumgebung() {
        switch (role) {
        case AUTOR:
            return Role.SENDER;
        case SENDER:
            return Role.AUTOR;
        case EMPFAENGER:
            return Role.LESER;
        case LESER:
            return Role.EMPFAENGER;
        default:
            return Role.LESER;
        }
    }
}
