package cn.zhangquanyu.model.dto.vo;

import lombok.Data;

@Data
public class ModelFieldVO {

    private Long id;
    private Long modelId;
    private String name;
    private String displayName;
    private String fieldType;
    private Integer length;
    private Integer precision;
    private Integer isRequired;
    private Integer isPrimary;
    private Integer isIndex;
    private String defaultValue;
    private Long metadataId;
    private String metadataName;
    private Integer sortOrder;
    private String fieldComment;
}
