package features.model;

import features.constraints.CodeList;
import features.constraints.Projection;
import features.converters.StringListAttributeConverter;
import features.i18n.InternationalizationListener;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Sandbox for the testing of validators.
 * <p>
 * List of validators
 * <ul>
 *     <li>{@link NotNull}. The annotated element must not be {@code null}.</li>
 *     <li>{@link NotBlank}. The annotated element must not be {@code null} and must contain at least one non-whitespace
 *     character. Accepts {@code CharSequence}.</li>
 *     <li>{@link Email}. The string has to be a well-formed email address. Exact semantics of what makes up a valid
 *     email address are left to Jakarta Bean Validation providers. Accepts {@code CharSequence}. {@code null} elements
 *     are considered valid.</li>
 *     <li>{@link URL}. The string has to be a well-formed URL {@code null} elements are considered valid. Validates
 *     http, https, ftp, file, and jar protocols</li>
 * </ul>
 */
@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@EntityListeners(InternationalizationListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    private String name;

    /**
     * Primary Email must be a mandatory email.
     */
    @Email
    @NotNull
    private String primaryEmail;

    /**
     * Secondary Email is an optional email.
     */
    @Email
    private String secondaryEmail;

    /**
     * Homepage is optional and should be http or https.
     */
    @URL(regexp = "^(http|https).*")
    private String homepage;

    /**
     * Tag is optional and should be from a controlled code list.
     */
    @CodeList("tag")
    private String tag;

    /**
     * Storing a list as a string with comma separated items
     */
    @Convert(converter = StringListAttributeConverter.class)
    private List<String> tags;

    /**
     * Test custom annotation
     */
    @Projection
    private String projection;

    /**
     * Test custom annotation to a list
     */
    @Projection
    @Convert(converter = StringListAttributeConverter.class)
    private List<String> projections;
}