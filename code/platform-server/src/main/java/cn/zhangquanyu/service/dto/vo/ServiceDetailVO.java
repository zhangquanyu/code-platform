package cn.zhangquanyu.service.dto.vo;

import lombok.Data;

import java.util.List;

@Data
public class ServiceDetailVO {

    private ServiceVO service;
    private List<ServiceParamVO> inputs;
    private List<ServiceParamVO> outputs;
}
