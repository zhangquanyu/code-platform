package cn.zhangquanyu.service.dto.vo;

import lombok.Data;

@Data
public class OrchParamVO {

    private Long id;
    private String paramName;
    private String dataType;
    private Integer isRequired;
    private String paramComment;
    private String sourceNodeKey;
    private String sourceField;
}
