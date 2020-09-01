package features.repository;

import features.model.Translation;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "transaltions", path = "translations")
public interface TranslationRepository extends PagingAndSortingRepository<Translation, Long> {

    List<Translation> findByEntityIdAndLocale(Long entityId, String locale);
}

