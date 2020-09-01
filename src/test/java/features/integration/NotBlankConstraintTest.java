package features.integration;

import features.Application;
import features.model.User;
import features.utils.ValidationErrors;
import org.junit.jupiter.api.*;
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
@DisplayName("Not Blank Constraint Test")
@ActiveProfiles(profiles = "test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NotBlankConstraintTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    private User.UserBuilder userBuilder;
    private User user;

    @BeforeEach
    public void userSetup() {
        userBuilder = User.builder().primaryEmail("email@example.com");
    }

    @Test
    @DisplayName("The annotated element must not be null")
    public void theAnnotatedElementMustNotBeNull() {
        user = userBuilder.build();
        ResponseEntity<ValidationErrors> response = testRestTemplate.postForEntity("/users", user, ValidationErrors.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).anyMatch(p ->
                Objects.equals(p.getEntity(), "User") && Objects.equals(p.getProperty(), "name") && p.getInvalidValue() == null
        );
    }

    @Test
    @DisplayName("The annotated element must not be empty")
    public void theAnnotatedElementMustNotBeEmpty() {
        user = userBuilder.name("").build();
        ResponseEntity<ValidationErrors> response = testRestTemplate.postForEntity("/users", user, ValidationErrors.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).anyMatch(p ->
                Objects.equals(p.getEntity(), "User") && Objects.equals(p.getProperty(), "name") && p.getInvalidValue().toString().isEmpty());
    }

    @Test
    @DisplayName("The annotated element must not contain only empty whitespaces")
    public void theAnnotatedElementMustNotContainOnlyWhitespaces() {
        user = userBuilder.name("    ").build();
        ResponseEntity<ValidationErrors> response = testRestTemplate.postForEntity("/users", user, ValidationErrors.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).anyMatch(p ->
                Objects.equals(p.getEntity(), "User") && Objects.equals(p.getProperty(), "name") && p.getInvalidValue().toString().trim().isEmpty()
        );
    }
}
