package features.i18n;

import features.model.Translation;
import features.repository.TranslationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.persistence.Id;
import javax.persistence.PostLoad;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Component
@Slf4j
public class InternationalizationListener<T> {

    @Autowired
    private TranslationRepository translationRepository;

    @PostLoad
    public void updateInternationalization(T target) {
        Arrays.stream(target.getClass().getDeclaredFields())
                .filter(it -> it.isAnnotationPresent(Id.class))
                .findFirst().ifPresent(idField -> {
            idField.setAccessible(true);
            try {
                translateFields(target, (Long) idField.get(target));
            } catch (IllegalAccessException e) {
                log.error("Can't access the field id", e);
            }
        });
    }

    private void translateFields(T target, Long entityId) {
        String locale = LocaleContextHolder.getLocale().toLanguageTag();
        List<Translation> translations = translationRepository.findByEntityIdAndLocale(entityId, locale);
        Arrays.stream(target.getClass().getDeclaredFields())
                .forEach(processI18nField(target, translations));
    }

    private Consumer<Field> processI18nField(T target, List<Translation> translations) {
        return field -> {
            String key = field.getDeclaringClass().getCanonicalName() + "." + field.getName();
            translations.stream()
                    .filter(it -> key.equals(it.getKey()))
                    .findFirst()
                    .ifPresent(translateIfPossible(target, field));
        };
    }

    private Consumer<Translation> translateIfPossible(T target, Field field) {
        return translation -> {
            if (Strings.isNotBlank(translation.getContent())) {
                field.setAccessible(true);
                try {
                    field.set(target, translation.getContent());
                } catch (IllegalAccessException e) {
                    log.error("Can't set the i18n field", e);
                }
            }
        };
    }
}

