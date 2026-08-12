package cn.zhangquanyu.service.domain.repository;

import cn.zhangquanyu.service.domain.entity.OrchestrationEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrchestrationEdgeRepository extends JpaRepository<OrchestrationEdge, Long> {

    List<OrchestrationEdge> findByOrchestrationIdAndIsDeleted(Long orchestrationId, Integer isDeleted);

    @Modifying
    @Query("update OrchestrationEdge e set e.isDeleted = 1 where e.orchestrationId = :orchestrationId and e.isDeleted = 0")
    int softDeleteByOrchestrationId(@Param("orchestrationId") Long orchestrationId);
}
