package cn.zhangquanyu.metadata.dto.vo;

import lombok.Data;

@Data
public class MetadataItemVO {

    private Long id;
    private Long metadataId;
    private String itemCode;
    private String itemName;
    private String itemValue;
    private Integer sortOrder;
    private Integer status;
}
