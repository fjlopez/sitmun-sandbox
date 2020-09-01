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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = RANDOM_PORT)
@DisplayName("Projection Constraint Test")
@ActiveProfiles(profiles = "test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectionConstraintTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User.UserBuilder userBuilder;

    @BeforeEach
    public void userSetup() {
        userBuilder = User.builder().name("name").primaryEmail("email@example.com");
    }

    @Test
    @DisplayName("Single projection pass")
    public void singleProjectionPass() {
        User user = userBuilder.projection("EPSG:1").build();
        ResponseEntity<User> response = testRestTemplate.postForEntity("/users", user, User.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("Single other value fail")
    public void singleOtherValueFail() {
        User user = userBuilder.projection("other").build();
        ResponseEntity<ValidationErrors> response = testRestTemplate.postForEntity("/users", user, ValidationErrors.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).anyMatch(p ->
                Objects.equals(p.getEntity(), "User") && Objects.equals(p.getProperty(), "projection") && p.getInvalidValue().equals("other")
        );
    }

    @Test
    @DisplayName("Multiple projections pass")
    public void multipleProjectionPass() {
        User user = userBuilder.projections(Arrays.asList("EPSG:1", "EPSG:2")).build();
        ResponseEntity<User> response = testRestTemplate.postForEntity("/users", user, User.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("Multiple projections with other value fail")
    public void multipleProjectionsWithOtherValueFail() {
        List<String> data = Arrays.asList("other", "EPSG:2");
        User user = userBuilder.projections(data).build();
        ResponseEntity<ValidationErrors> response = testRestTemplate.postForEntity("/users", user, ValidationErrors.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).anyMatch(p ->
                Objects.equals(p.getEntity(), "User") && Objects.equals(p.getProperty(), "projections") && p.getInvalidValue().equals(data)
        );
    }

    @AfterEach
    public void deleteData() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "stm_user");
    }
}
