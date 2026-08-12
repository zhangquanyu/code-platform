package cn.zhangquanyu.service.domain.repository;

import cn.zhangquanyu.service.domain.entity.Orchestration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrchestrationRepository extends JpaRepository<Orchestration, Long>,
        JpaSpecificationExecutor<Orchestration> {

    Optional<Orchestration> findByIdAndIsDeleted(Long id, Integer isDeleted);

    boolean existsByMicroserviceIdAndCodeAndIsDeleted(Long microserviceId, String code, Integer isDeleted);

    long countByMicroserviceIdAndIsDeleted(Long microserviceId, Integer isDeleted);

    @Modifying
    @Query("update Orchestration o set o.isDeleted = 1 where o.id = :id")
    int softDelete(@Param("id") Long id);
}
