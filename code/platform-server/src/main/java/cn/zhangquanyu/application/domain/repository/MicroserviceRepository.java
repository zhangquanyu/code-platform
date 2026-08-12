package cn.zhangquanyu.application.domain.repository;

import cn.zhangquanyu.application.domain.entity.Microservice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MicroserviceRepository extends JpaRepository<Microservice, Long>,
        JpaSpecificationExecutor<Microservice> {

    Optional<Microservice> findByIdAndIsDeleted(Long id, Integer isDeleted);

    boolean existsByApplicationIdAndCodeAndIsDeleted(Long applicationId, String code, Integer isDeleted);

    List<Microservice> findByApplicationIdAndIsDeletedAndStatusOrderByCreateTimeDesc(
            Long applicationId, Integer isDeleted, Integer status);

    List<Microservice> findByApplicationIdAndIsDeletedOrderByCreateTimeDesc(Long applicationId, Integer isDeleted);

    long countByApplicationIdAndIsDeleted(Long applicationId, Integer isDeleted);

    @Modifying
    @Query("update Microservice m set m.isDeleted = 1 where m.id = :id")
    int softDelete(@Param("id") Long id);
}
