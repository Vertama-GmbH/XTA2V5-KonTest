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

import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import de.xoev.xta.test.app.model.CustomResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Log4j2
public class ExceptionHandlerAdvice {

    private static final String ERROR = "Fehler beim Verarbeiten: ";

    @ExceptionHandler(SizeLimitExceededException.class)
    @ResponseBody
    ResponseEntity<CustomResponseEntity> handleSizeLimitExceededException(final SizeLimitExceededException exc) {
        // return the BaseHttpStatusException as is

        final String errormessage = exc.getMessage();
        return ResponseEntity.internalServerError()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CustomResponseEntity().setError(true)
                        .setMessage(ERROR + errormessage));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public ResponseEntity<CustomResponseEntity> handleMaxSizeException(
            final MaxUploadSizeExceededException exc,
            final HttpServletRequest request,
            final HttpServletResponse response) {
        final String errormessage = "Die Datei überschreitet die Größenbeschränkung!";

        return ResponseEntity.internalServerError()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CustomResponseEntity().setError(true)
                        .setMessage(ERROR + errormessage));
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    ResponseEntity<CustomResponseEntity> handleGeneralException(final Exception exc) {
        final String errorMessage = exc.getMessage();
        log.error(errorMessage, exc);

        return ResponseEntity.internalServerError()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CustomResponseEntity().setError(true)
                        .setMessage(ERROR + errorMessage));
    }
}
