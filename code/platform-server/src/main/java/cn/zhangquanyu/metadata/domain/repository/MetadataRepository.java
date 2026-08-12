package cn.zhangquanyu.metadata.domain.repository;

import cn.zhangquanyu.metadata.domain.entity.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MetadataRepository extends JpaRepository<Metadata, Long>,
        JpaSpecificationExecutor<Metadata> {

    Optional<Metadata> findByIdAndIsDeleted(Long id, Integer isDeleted);

    boolean existsByApplicationIdAndCodeAndIsDeleted(Long applicationId, String code, Integer isDeleted);

    long countByApplicationIdAndIsDeleted(Long applicationId, Integer isDeleted);

    List<Metadata> findByApplicationIdAndIsDeletedAndStatusOrderByCreateTimeDesc(
            Long applicationId, Integer isDeleted, Integer status);

    List<Metadata> findByApplicationIdAndIsDeletedOrderByCreateTimeDesc(Long applicationId, Integer isDeleted);

    @Modifying
    @Query("update Metadata m set m.isDeleted = 1 where m.id = :id")
    int softDelete(@Param("id") Long id);
}
