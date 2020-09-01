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
@DisplayName("URL Constraint Test")
@ActiveProfiles(profiles = "test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UrlConstraintTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    private User.UserBuilder userBuilder;
    private User user;

    @BeforeEach
    public void userSetup() {
        userBuilder = User.builder().name("name").primaryEmail("email@example.com");
    }

    @Test
    @DisplayName("The annotated element doesn't allow relative urls")
    public void theAnnotatedElementDontAllowRelativeUrls() {
        user = userBuilder.homepage("relative/url").build();
        ResponseEntity<ValidationErrors> response = testRestTemplate.postForEntity("/users", user, ValidationErrors.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).anyMatch(p ->
                Objects.equals(p.getEntity(), "User") && Objects.equals(p.getProperty(), "homepage") && p.getInvalidValue().equals("relative/url")
        );
    }

    @Test
    @DisplayName("The annotated element allows http urls (using regex)")
    public void theAnnotatedElementAllowHttpUrls() {
        user = userBuilder.homepage("http://example.com/path").build();
        ResponseEntity<ValidationErrors> response = testRestTemplate.postForEntity("/users", user, ValidationErrors.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("The annotated element allows https urls (using regex)")
    public void theAnnotatedElementAllowHttpsUrls() {
        user = userBuilder.homepage("https://example.com/path").build();
        ResponseEntity<ValidationErrors> response = testRestTemplate.postForEntity("/users", user, ValidationErrors.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("The annotated element must also match the regex if present")
    public void theAnnotatedElementDoesNotMatchOnlyTheRegex() {
        user = userBuilder.homepage("file://dangerous").build();
        ResponseEntity<ValidationErrors> response = testRestTemplate.postForEntity("/users", user, ValidationErrors.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).anyMatch(p ->
                Objects.equals(p.getEntity(), "User") && Objects.equals(p.getProperty(), "homepage") && p.getInvalidValue().equals("file://dangerous")
        );
    }
}
