package cn.zhangquanyu.service.domain.repository;

import cn.zhangquanyu.service.domain.entity.OrchestrationParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrchestrationParamRepository extends JpaRepository<OrchestrationParam, Long> {

    List<OrchestrationParam> findByOrchestrationIdAndParamScopeAndIsDeletedOrderBySortOrderAsc(
            Long orchestrationId, String paramScope, Integer isDeleted);

    List<OrchestrationParam> findByOrchestrationIdAndIsDeletedOrderBySortOrderAsc(
            Long orchestrationId, Integer isDeleted);

    @Modifying
    @Query("update OrchestrationParam p set p.isDeleted = 1 where p.orchestrationId = :orchestrationId and p.isDeleted = 0")
    int softDeleteByOrchestrationId(@Param("orchestrationId") Long orchestrationId);

    @Modifying
    @Query("delete from OrchestrationParam p where p.orchestrationId = :orchestrationId and p.isDeleted = 1")
    int hardDeleteSoftDeletedByOrchestrationId(@Param("orchestrationId") Long orchestrationId);
}
