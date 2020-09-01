package features.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class ProjectionValidator implements ConstraintValidator<Projection, String> {

    private static Pattern pattern = Pattern.compile("^[A-Z\\-]+:\\d+$");

    @Override
    public void initialize(Projection constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return pattern.matcher(value).matches();
    }
}
