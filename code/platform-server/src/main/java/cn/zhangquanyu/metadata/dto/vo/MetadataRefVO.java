package cn.zhangquanyu.metadata.dto.vo;

import lombok.Data;

@Data
public class MetadataRefVO {

    private Long modelId;
    private String modelName;
    private Long fieldId;
    private String fieldName;
    private String displayName;
    private String microserviceName;
}
