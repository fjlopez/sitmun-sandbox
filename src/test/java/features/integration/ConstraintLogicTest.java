package features.integration;

import features.Application;
import features.config.RepositoryRestConfig;
import features.model.User;
import features.utils.ValidationErrors;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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
@DisplayName("Constraint Integration Test")
@ActiveProfiles(profiles = "test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConstraintLogicTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    @DisplayName("Canary test")
    @Order(1)
    public void canaryTest() {
        User user = User.builder().name("name").primaryEmail("email@example.com").build();
        ResponseEntity<User> response = testRestTemplate.postForEntity("/users", user, User.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("name");
        assertThat(response.getBody().getPrimaryEmail()).isEqualTo("email@example.com");
        assertThat(response.getBody().getId()).isNotNull().isNotEqualTo(0L);
    }

    /**
     * REST Repository does not emit events when an entity is retrieved.
     * Hence, buggy legacy data is never validated when an entity is retrieved.
     */
    @Test
    @DisplayName("Buggy legacy data is never validated when retrieved")
    @Order(2)
    @Sql(
            statements = {"insert into stm_user(usr_id, usr_primary_email) values (1, 'this is not an email')"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    public void buggyLegacyDataIsNeverValidatedWhenRetrieved() throws JSONException {
        ResponseEntity<User> response = testRestTemplate.getForEntity("/users/1", User.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isNullOrEmpty();
        assertThat(response.getBody().getPrimaryEmail()).isEqualTo("this is not an email");
        assertThat(response.getBody().getId()).isNotNull().isNotEqualTo(0L);
    }

    /**
     * {@link RepositoryRestConfig} configures the validating listener to handle before create events with the
     * validation annotations.
     */
    @Test
    @DisplayName("Constraint raises a validation error on PUT")
    public void notBlankRaisesAValidationErrorOnPut() {
        User user = User.builder().name("name").build();

        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> userUpdate = new HttpEntity<>(user, reqHeaders);

        ResponseEntity<ValidationErrors> response = testRestTemplate.exchange("/users/1", HttpMethod.PUT, userUpdate, ValidationErrors.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).anyMatch(p ->
                Objects.equals(p.getEntity(), "User") && Objects.equals(p.getProperty(), "primaryEmail") && p.getInvalidValue() == null
        );
    }

    /**
     * {@link RepositoryRestConfig} configures the validating listener to handle before save events with the
     * validation annotations.
     */
    @Test
    @DisplayName("Constraint raises a validation error on PATCH")
    @Sql(
            statements = {"insert into stm_user(usr_id, usr_name, usr_primary_email) values (1, 'name', 'email@example.com')"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    public void notBlankRaisesAValidationErrorOnPatch() throws JSONException {

        JSONObject updateBody = new JSONObject();
        updateBody.put("primaryEmail", JSONObject.NULL);
        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> userUpdate = new HttpEntity<>(updateBody.toString(), reqHeaders);

        ResponseEntity<ValidationErrors> response = testRestTemplate.exchange("/users/1", HttpMethod.PATCH, userUpdate, ValidationErrors.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).anyMatch(p ->
                Objects.equals(p.getEntity(), "User") && Objects.equals(p.getProperty(), "primaryEmail") && p.getInvalidValue() == null
        );
    }

    /**
     * {@link RepositoryRestConfig} configures the validating listener to handle before save events with the
     * validation annotations. All validations are triggered even when the change does not affects the field
     * associated to the validation. That is, if buggy legacy data is present, the entity cannot be modified unless
     * the modification fixes all legacy data issues.
     */
    @Test
    @DisplayName("Constraint raises a validation error on PATCH even when buggy legacy data is not involved")
    @Sql(
            statements = {"insert into stm_user(usr_id, usr_primary_email) values (1, 'email@example.com')"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    public void notBlankRaisesAValidationErrorOnPatchEvenWhenBuggyLegacyDataIsNotInvolved() throws JSONException {

        JSONObject updateBody = new JSONObject();
        updateBody.put("primaryEmail", "new_email@example.com");
        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> userUpdate = new HttpEntity<>(updateBody.toString(), reqHeaders);

        ResponseEntity<ValidationErrors> response = testRestTemplate.exchange("/users/1", HttpMethod.PATCH, userUpdate, ValidationErrors.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).anyMatch(p ->
                Objects.equals(p.getEntity(), "User") && Objects.equals(p.getProperty(), "name") && p.getInvalidValue() == null
        );
    }

    /**
     * Buggy legacy data can be fixed with PATCH. Validators are evaluated after applying the patch to a retrieved
     * copy of the entity and before saving it. If the entity has multiple fields with legacy errors, they must all be
     * fixed simultaneously.
     */
    @Test
    @DisplayName("Constraint with buggy legacy data can be fixed with PATCH")
    @Sql(
            statements = {"insert into stm_user(usr_id, usr_primary_email) values (1, 'email@example.com')"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    public void notBlankWithBuggyLegacyDataCanBeFixedWithPatch() throws JSONException {

        JSONObject updateBody = new JSONObject();
        updateBody.put("name", "name");
        HttpHeaders reqHeaders = new HttpHeaders();
        reqHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> userUpdate = new HttpEntity<>(updateBody.toString(), reqHeaders);

        ResponseEntity<User> response = testRestTemplate.exchange("/users/1", HttpMethod.PATCH, userUpdate, User.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("name");
        assertThat(response.getBody().getPrimaryEmail()).isEqualTo("email@example.com");
        assertThat(response.getBody().getId()).isEqualTo(1L);
    }

    /**
     * {@link RepositoryRestConfig} configures the validating listener to handle before create events with the
     * validation annotations.
     */
    @Test
    @DisplayName("Constraint raises a validation error on POST")
    public void notBlankRaisesAValidationErrorOnPost() {
        User user = User.builder().name("name").build();
        ResponseEntity<ValidationErrors> response = testRestTemplate.postForEntity("/users", user, ValidationErrors.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).anyMatch(p ->
                Objects.equals(p.getEntity(), "User") && Objects.equals(p.getProperty(), "primaryEmail") && p.getInvalidValue() == null
        );
    }

    @BeforeEach
    public void enablePatchSupportToRestTemplate() {
        testRestTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @AfterEach
    public void deleteData() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "stm_user");
    }
}

