package cn.zhangquanyu.model.domain.repository;

import cn.zhangquanyu.model.domain.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ModelRepository extends JpaRepository<Model, Long>,
        JpaSpecificationExecutor<Model> {

    Optional<Model> findByIdAndIsDeleted(Long id, Integer isDeleted);

    boolean existsByMicroserviceIdAndCodeAndIsDeleted(Long microserviceId, String code, Integer isDeleted);

    long countByMicroserviceIdAndIsDeleted(Long microserviceId, Integer isDeleted);

    List<Model> findByMicroserviceIdAndIsDeletedOrderByCreateTimeDesc(Long microserviceId, Integer isDeleted);

    @Modifying
    @Query("update Model m set m.isDeleted = 1 where m.id = :id")
    int softDelete(@Param("id") Long id);
}
