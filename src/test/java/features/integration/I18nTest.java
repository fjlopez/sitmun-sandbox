package features.integration;

import features.Application;
import features.model.User;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = RANDOM_PORT)
@DisplayName("Internationalization Integration Test")
@ActiveProfiles(profiles = "test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class I18nTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * Translation is a feature on demand.
     */
    @Test
    @DisplayName("Translation is enabled by a query parameter")
    @Sql(
            statements = {
                    "insert into stm_user(usr_id, usr_name) values (1, 'name')",
                    "insert into stm_translation(tra_id, tra_entity_id, tra_locale, tra_key, tra_content) values (1, 1, 'xx', 'features.model.User.name', 'i18n')",
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    public void translationIsEnabledByAQueryParameter() {
        ResponseEntity<User> response = testRestTemplate.getForEntity("/users/1?lang=xx", User.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("i18n");
    }


    @AfterEach
    public void deleteData() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "stm_user", "stm_translation");
    }
}

