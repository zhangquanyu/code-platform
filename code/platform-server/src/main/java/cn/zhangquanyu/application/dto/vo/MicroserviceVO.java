package cn.zhangquanyu.application.dto.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MicroserviceVO {

    private Long id;
    private Long applicationId;
    private String applicationName;
    private String name;
    private String code;
    private String version;
    private Integer status;
    private String description;
    private LocalDateTime createTime;
}
