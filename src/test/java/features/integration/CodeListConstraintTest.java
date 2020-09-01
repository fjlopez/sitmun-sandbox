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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = RANDOM_PORT)
@DisplayName("CodeList Constraint Test")
@ActiveProfiles(profiles = "test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CodeListConstraintTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User.UserBuilder userBuilder;
    private User user;

    @BeforeEach
    public void userSetup() {
        userBuilder = User.builder().name("name").primaryEmail("email@example.com");
    }

    @Test
    @DisplayName("The annotated element doesn't allow unknown codes")
    public void theAnnotatedElementDoesntAllowUnknownCodes() {
        user = userBuilder.tag("no tags").build();
        ResponseEntity<ValidationErrors> response = testRestTemplate.postForEntity("/users", user, ValidationErrors.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).anyMatch(p ->
                Objects.equals(p.getEntity(), "User") && Objects.equals(p.getProperty(), "tag") && p.getInvalidValue().equals("no tags")
        );
    }

    @Test
    @DisplayName("The annotated element allows known codes")
    @Sql(
            statements = {"insert into stm_code(cod_id, cod_code_list, cod_code) values (1, 'tag', 'valid')"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    public void theAnnotatedElementAllowsKnownCodes() {
        user = userBuilder.tag("valid").build();
        ResponseEntity<User> response = testRestTemplate.postForEntity("/users", user, User.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @AfterEach
    public void deleteData() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "stm_user", "stm_code");
    }
}
