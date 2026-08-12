package cn.zhangquanyu.service.dto.vo;

import lombok.Data;

@Data
public class ServiceParamVO {

    private Long id;
    private Long serviceId;
    private Integer paramType;
    private String paramName;
    private String dataType;
    private Integer isRequired;
    private String defaultValue;
    private Long modelFieldId;
    private Integer sortOrder;
    private String paramComment;
}
