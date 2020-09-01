package features.constraints;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.regex.Pattern;

public class ProjectionListValidator implements ConstraintValidator<Projection, List<String>> {

    private static Pattern pattern = Pattern.compile("^[A-Z_\\-]+:\\d+$");

    @Override
    public void initialize(Projection constraintAnnotation) {

    }

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return value.stream().allMatch(it -> pattern.matcher(it).matches());
    }
}
