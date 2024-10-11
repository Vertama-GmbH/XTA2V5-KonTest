/*
 * Created 2022-02-06
 */
package de.xoev.xta.test.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

@Profile({ "test" })
@SpringBootApplication
public class TestingApplication {

    public static void main(final String[] args) {
        SpringApplication.run(TestingApplication.class, args);
    }
}
