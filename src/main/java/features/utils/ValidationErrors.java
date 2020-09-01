package features.utils;

import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.List;

@Value
@NoArgsConstructor
public class ValidationErrors {

    List<ValidationError> errors = Collections.emptyList();

    @Value
    public static class ValidationError {
        String entity, property;
        Object invalidValue;
        String message;
    }
}