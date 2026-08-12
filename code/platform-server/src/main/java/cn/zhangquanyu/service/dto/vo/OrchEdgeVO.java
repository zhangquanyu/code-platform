package cn.zhangquanyu.service.dto.vo;

import lombok.Data;

@Data
public class OrchEdgeVO {

    private Long id;
    private String edgeKey;
    private String fromNodeKey;
    private String toNodeKey;
    private String conditionExpr;
    private String labelText;
}
