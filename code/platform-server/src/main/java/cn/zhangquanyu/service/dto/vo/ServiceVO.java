package cn.zhangquanyu.service.dto.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceVO {

    private Long id;
    private Long microserviceId;
    private String microserviceName;
    private String name;
    private String code;
    private String description;
    private String httpMethod;
    private String servicePath;
    private String category;
    private Integer status;
    private long inputCount;
    private long outputCount;
    private LocalDateTime createTime;
}
