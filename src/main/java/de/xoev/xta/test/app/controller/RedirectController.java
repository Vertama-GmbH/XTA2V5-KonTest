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
package de.xoev.xta.test.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/")
public class RedirectController {

    @GetMapping("")
    public RedirectView redirectWithUsingRedirectView(final RedirectAttributes attributes) {
        return new RedirectView("/view/home");
    }
}