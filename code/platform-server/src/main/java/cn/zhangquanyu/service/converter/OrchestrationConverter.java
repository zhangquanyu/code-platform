package cn.zhangquanyu.service.converter;

import cn.zhangquanyu.service.domain.entity.Orchestration;
import cn.zhangquanyu.service.domain.entity.OrchestrationEdge;
import cn.zhangquanyu.service.domain.entity.OrchestrationNode;
import cn.zhangquanyu.service.dto.vo.OrchEdgeVO;
import cn.zhangquanyu.service.dto.vo.OrchNodeVO;
import cn.zhangquanyu.service.dto.vo.OrchestrationVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrchestrationConverter {

    @Mapping(target = "microserviceName", ignore = true)
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "nodeCount", ignore = true)
    @Mapping(target = "edgeCount", ignore = true)
    OrchestrationVO toVO(Orchestration entity);

    @Mapping(target = "serviceName", ignore = true)
    @Mapping(target = "serviceInputs", ignore = true)
    @Mapping(target = "serviceOutputs", ignore = true)
    OrchNodeVO toNodeVO(OrchestrationNode entity);

    List<OrchNodeVO> toNodeVOList(List<OrchestrationNode> entities);

    OrchEdgeVO toEdgeVO(OrchestrationEdge entity);

    List<OrchEdgeVO> toEdgeVOList(List<OrchestrationEdge> entities);
}
