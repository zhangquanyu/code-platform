package cn.zhangquanyu.service.dto.vo;

import lombok.Data;

@Data
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
    private Integer xPos;
    private Integer yPos;
    private Integer sortOrder;
}
