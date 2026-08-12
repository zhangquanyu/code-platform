package cn.zhangquanyu.metadata.dto.vo;

import lombok.Data;

import java.util.List;

@Data
public class MetadataDetailVO {

    private MetadataVO metadata;
    private List<MetadataItemVO> items;
    private List<MetadataRefVO> references;
}
