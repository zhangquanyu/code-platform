package cn.zhangquanyu.metadata.domain.repository;

import cn.zhangquanyu.metadata.domain.entity.MetadataItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MetadataItemRepository extends JpaRepository<MetadataItem, Long> {

    List<MetadataItem> findByMetadataIdAndIsDeletedOrderBySortOrderAsc(Long metadataId, Integer isDeleted);

    long countByMetadataIdAndIsDeleted(Long metadataId, Integer isDeleted);

    @Modifying
    @Query("update MetadataItem i set i.isDeleted = 1 where i.id = :id")
    int softDelete(@Param("id") Long id);
}
