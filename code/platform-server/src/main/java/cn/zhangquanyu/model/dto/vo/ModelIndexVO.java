package cn.zhangquanyu.model.dto.vo;

import lombok.Data;

import java.util.List;

@Data
public class ModelIndexVO {

    private Long id;
    private Long modelId;
    private String indexName;
    private String indexType;
    private List<Long> fieldIds;
    private String fieldNames;
}
