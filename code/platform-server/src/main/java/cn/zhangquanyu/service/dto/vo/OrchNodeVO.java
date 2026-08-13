package cn.zhangquanyu.service.dto.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class OrchNodeVO {

    private Long id;
    private String nodeKey;
    private String nodeType;
    private String nodeName;
    private Long serviceId;
    private String serviceName;
    private String configJson;
    private String txType;
    private Integer txTimeout;
    private Integer retryCount;
    private Integer retryInterval;
    private String exceptionStrategy;
    private String loopType;
    private String branchExpr;
    @JsonProperty("xPos")
    private Integer xPos;
    @JsonProperty("yPos")
    private Integer yPos;
    private Integer sortOrder;

    /** 服务节点的入参定义（仅 SERVICE/ACTION 节点，从 ServiceDef 填充） */
    private List<ServiceParamVO> serviceInputs;
    /** 服务节点的出参定义（仅 SERVICE/ACTION 节点，从 ServiceDef 填充） */
    private List<ServiceParamVO> serviceOutputs;
}
