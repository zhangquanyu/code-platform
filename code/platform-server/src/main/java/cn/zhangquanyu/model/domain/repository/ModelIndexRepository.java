package cn.zhangquanyu.model.domain.repository;

import cn.zhangquanyu.model.domain.entity.ModelIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ModelIndexRepository extends JpaRepository<ModelIndex, Long> {

    List<ModelIndex> findByModelIdAndIsDeletedOrderByIdAsc(Long modelId, Integer isDeleted);

    @Modifying
    @Query("update ModelIndex i set i.isDeleted = 1 where i.modelId = :modelId")
    int softDeleteByModelId(@Param("modelId") Long modelId);

    @Modifying
    @Query("update ModelIndex i set i.isDeleted = 1 where i.id = :id")
    int softDelete(@Param("id") Long id);
}
