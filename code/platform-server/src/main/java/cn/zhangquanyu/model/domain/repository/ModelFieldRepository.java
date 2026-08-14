package cn.zhangquanyu.model.domain.repository;

import cn.zhangquanyu.model.domain.entity.ModelField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ModelFieldRepository extends JpaRepository<ModelField, Long> {

    List<ModelField> findByModelIdAndIsDeletedOrderBySortOrderAsc(Long modelId, Integer isDeleted);

    long countByModelIdAndIsDeleted(Long modelId, Integer isDeleted);

    long countByMetadataIdAndIsDeleted(Long metadataId, Integer isDeleted);

    List<ModelField> findByMetadataIdAndIsDeleted(Long metadataId, Integer isDeleted);

    @Modifying
    @Query("update ModelField f set f.isDeleted = 1 where f.modelId = :modelId")
    int softDeleteByModelId(@Param("modelId") Long modelId);

    @Modifying
    @Query("update ModelField f set f.isDeleted = 1 where f.id = :id")
    int softDelete(@Param("id") Long id);

    java.util.Optional<ModelField> findByModelIdAndNameAndIsDeleted(Long modelId, String name, Integer isDeleted);
}
