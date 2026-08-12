package cn.zhangquanyu.model.dto.vo;

import lombok.Data;

import java.util.List;

@Data
public class ModelDetailVO {

    private ModelVO model;
    private List<ModelFieldVO> fields;
}
