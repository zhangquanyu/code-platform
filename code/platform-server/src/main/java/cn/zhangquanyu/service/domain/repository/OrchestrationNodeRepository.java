package cn.zhangquanyu.service.domain.repository;

import cn.zhangquanyu.service.domain.entity.OrchestrationNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrchestrationNodeRepository extends JpaRepository<OrchestrationNode, Long> {

    List<OrchestrationNode> findByOrchestrationIdAndIsDeletedOrderBySortOrderAsc(Long orchestrationId, Integer isDeleted);

    /**
     * 编排节点引用某服务的数量（用于服务删除前校验）
     */
    long countByServiceIdAndIsDeleted(Long serviceId, Integer isDeleted);

    @Modifying
    @Query("update OrchestrationNode n set n.isDeleted = 1 where n.orchestrationId = :orchestrationId and n.isDeleted = 0")
    int softDeleteByOrchestrationId(@Param("orchestrationId") Long orchestrationId);
}
