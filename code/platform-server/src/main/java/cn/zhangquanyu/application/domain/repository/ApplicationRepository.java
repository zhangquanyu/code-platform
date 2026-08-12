package cn.zhangquanyu.application.domain.repository;

import cn.zhangquanyu.application.domain.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long>,
        JpaSpecificationExecutor<Application> {

    Optional<Application> findByIdAndIsDeleted(Long id, Integer isDeleted);

    boolean existsByCodeAndIsDeleted(String code, Integer isDeleted);

    @Modifying
    @Query("update Application a set a.isDeleted = 1 where a.id = :id")
    int softDelete(@Param("id") Long id);
}
