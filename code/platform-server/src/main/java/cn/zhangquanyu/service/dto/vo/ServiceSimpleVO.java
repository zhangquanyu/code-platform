package cn.zhangquanyu.service.dto.vo;

import lombok.Data;

@Data
public class ServiceSimpleVO {

    private Long id;
    private Long microserviceId;
    private String name;
    private String code;
    private String httpMethod;
    private String servicePath;
}
