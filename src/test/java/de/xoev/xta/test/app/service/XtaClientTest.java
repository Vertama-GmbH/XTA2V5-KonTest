package de.xoev.xta.test.app.service;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import de.xoev.xta.test.app.TestingApplication;
import de.xoev.xta.test.app.exception.CancelScenarioExecution;
import de.xoev.xta.test.app.model.ExecutionType;
import de.xoev.xta.test.app.model.ScenarioEventDefinition;
import de.xoev.xta.test.app.model.ScenarioEventType;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { TestingApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)

@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
@DisabledIfEnvironmentVariable(named = "HTTP_TESTS_DISABLED", matches = "true")
class XtaClientTest {

    @SpyBean
    XtaClient xtaClient;

    @SpyBean
    private ConfigurationService configurationService;

    @LocalServerPort
    private int serverPort;
    private Path tempfile;

    @BeforeAll
    void setup() throws IOException {
//        try {
            tempfile = Files.createTempFile("client_", ".yaml");

            Files.copy(Paths.get("src/test/resources/configDefaultTest.yaml").toAbsolutePath(),
                    tempfile, StandardCopyOption.REPLACE_EXISTING);

            // doReturn("file:" + testfile.toAbsolutePath().toString()).when(configurationService).getConfigFileName();
            configurationService.configFileName = "file:" + tempfile.toAbsolutePath().toString();
            configurationService.resetXtaConfigBean();
//        } catch (IOException e) {
//            System.err.println(e);
//        }
    }

    @AfterAll
    void cleanup() throws IOException {
//        try {
            Files.deleteIfExists(tempfile);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    }

    @Test

    void testCheckAccountActive() throws Exception {
        // given
        ScenarioEventDefinition scenarioEventDefinition = ScenarioEventDefinition.builder()
                .eventType(ScenarioEventType.CHECK_ACCOUNT_ACTIVE)
                .multipleCallable(true)
                .executionType(ExecutionType.ACTIVE)
                .build();
        // ClientConnectionProperties servers = configurationService.getClientProperties().getServerUrl();
        // servers.setManagementPort("https://localhost:" + serverPort + 1 + "/services/XTAService/ManagementPort");

//        try {
            xtaClient.checkAccountActive();

//        } catch (CancelScenarioExecution e) {
//            fail("Execution failed: " + e.getMessage(), e);
//        } catch (Exception e) {
//            fail("Execution failed: " + e.getMessage(), e);
//        }
        // then

    }

}
