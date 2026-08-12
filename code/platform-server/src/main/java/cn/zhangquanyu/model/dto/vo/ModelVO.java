package cn.zhangquanyu.model.dto.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ModelVO {

    private Long id;
    private Long microserviceId;
    private String microserviceName;
    private Long applicationId;
    private String name;
    private String code;
    private String description;
    private long fieldCount;
    private LocalDateTime createTime;
}
