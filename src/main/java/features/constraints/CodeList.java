package features.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

@Constraint(validatedBy = {CodeListValidator.class})
@Target({FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CodeList {
    String value();

    /**
     * Required for a Constraint annotation.
     */
    String message() default "Invalid value";

    /**
     * Required for a Constraint annotation.
     */
    Class<?>[] groups() default {};

    /**
     * Required for a Constraint annotation.
     */
    Class<? extends Payload>[] payload() default {};
}
