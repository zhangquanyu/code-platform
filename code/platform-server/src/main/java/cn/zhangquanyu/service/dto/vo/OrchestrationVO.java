package cn.zhangquanyu.service.dto.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrchestrationVO {

    private Long id;
    private Long microserviceId;
    private String microserviceName;
    private Long applicationId;
    private String name;
    private String code;
    private String description;
    private Integer status;
    private String txType;
    private Integer txTimeout;
    private long nodeCount;
    private long edgeCount;
    private LocalDateTime createTime;
}
