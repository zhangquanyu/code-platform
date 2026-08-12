package cn.zhangquanyu.service.domain.repository;

import cn.zhangquanyu.service.domain.entity.ServiceParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceParamRepository extends JpaRepository<ServiceParam, Long> {

    List<ServiceParam> findByServiceIdAndIsDeletedOrderBySortOrderAsc(Long serviceId, Integer isDeleted);

    List<ServiceParam> findByServiceIdAndParamTypeAndIsDeletedOrderBySortOrderAsc(
            Long serviceId, Integer paramType, Integer isDeleted);

    @Modifying
    @Query("update ServiceParam p set p.isDeleted = 1 where p.serviceId = :serviceId")
    int softDeleteByServiceId(@Param("serviceId") Long serviceId);

    /**
     * 统计被服务参数引用的模型字段数量（用于模型删除前校验）
     */
    @Query("select count(p) from ServiceParam p where p.modelFieldId in :fieldIds and p.isDeleted = 0")
    long countByModelFieldIn(@Param("fieldIds") List<Long> fieldIds);
}
