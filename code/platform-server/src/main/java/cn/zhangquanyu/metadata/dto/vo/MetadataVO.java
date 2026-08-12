package cn.zhangquanyu.metadata.dto.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MetadataVO {

    private Long id;
    private Long applicationId;
    private String applicationName;
    private String name;
    private String code;
    private String description;
    private Integer status;
    private long itemCount;
    private LocalDateTime createTime;
}
