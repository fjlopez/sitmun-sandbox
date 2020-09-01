package features.repository;

import features.model.Code;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "codes", path = "codes")
public interface CodeRepository extends PagingAndSortingRepository<Code, Long> {

    boolean existsByCodeListAndCode(String codeList, String code);
}

