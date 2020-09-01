package features.integration;

import features.Application;
import features.model.User;
import features.utils.ValidationErrors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = RANDOM_PORT)
@DisplayName("Email Constraint Test")
@ActiveProfiles(profiles = "test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EmailConstraintTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    @DisplayName("The annotated element must be a valid email")
    public void theAnnotatedElementMustBeAValidEmail() {
        User user = User.builder().name("name").primaryEmail("email@example.com").secondaryEmail("some fake email").build();
        ResponseEntity<ValidationErrors> response = testRestTemplate.postForEntity("/users", user, ValidationErrors.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).anyMatch(p ->
                Objects.equals(p.getEntity(), "User") && Objects.equals(p.getProperty(), "secondaryEmail") && p.getInvalidValue().equals("some fake email")
        );
    }
}
