package cn.zhangquanyu.service.domain.repository;

import cn.zhangquanyu.service.domain.entity.ServiceDef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceDefRepository extends JpaRepository<ServiceDef, Long>,
        JpaSpecificationExecutor<ServiceDef> {

    Optional<ServiceDef> findByIdAndIsDeleted(Long id, Integer isDeleted);

    boolean existsByMicroserviceIdAndCodeAndIsDeleted(Long microserviceId, String code, Integer isDeleted);

    boolean existsByMicroserviceIdAndServicePathAndIsDeleted(Long microserviceId, String servicePath, Integer isDeleted);

    long countByMicroserviceIdAndIsDeleted(Long microserviceId, Integer isDeleted);

    List<ServiceDef> findByMicroserviceIdAndIsDeletedAndStatusOrderByCreateTimeDesc(
            Long microserviceId, Integer isDeleted, Integer status);

    List<ServiceDef> findByIdInAndIsDeleted(List<Long> ids, Integer isDeleted);

    @Modifying
    @Query("update ServiceDef s set s.isDeleted = 1 where s.id = :id")
    int softDelete(@Param("id") Long id);
}
