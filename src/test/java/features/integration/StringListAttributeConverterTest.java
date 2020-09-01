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

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = RANDOM_PORT)
@DisplayName("String List Attribute Converter Test")
@ActiveProfiles(profiles = "test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StringListAttributeConverterTest {

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
    @DisplayName("List is converted to string")
    public void listIsConvertedToString() {
        User user = userBuilder.tags(Arrays.asList("tag1", "tag2")).build();
        ResponseEntity<User> response = testRestTemplate.postForEntity("/users", user, User.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Integer count = jdbcTemplate.queryForObject("select count(*) from stm_user where usr_tags = 'tag1,tag2'", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("String is converted to list")
    @Sql(statements = {"insert into stm_user(usr_id, usr_name, usr_primary_email, usr_tags) values (1, 'name', 'email@example.com', 'tag3,tag4')"})
    public void stringIsConvertedToList() {
        ResponseEntity<User> response = testRestTemplate.getForEntity("/users/1", User.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTags()).containsExactly("tag3", "tag4");
    }

    @AfterEach
    public void deleteData() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "stm_user");
    }
}
