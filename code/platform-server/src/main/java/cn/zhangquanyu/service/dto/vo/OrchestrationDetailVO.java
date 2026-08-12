package cn.zhangquanyu.service.dto.vo;

import lombok.Data;

import java.util.List;

@Data
public class OrchestrationDetailVO {

    private OrchestrationVO orchestration;
    private List<OrchParamVO> inputParams;
    private List<OrchParamVO> outputParams;
    private List<OrchNodeVO> nodes;
    private List<OrchEdgeVO> edges;
}
