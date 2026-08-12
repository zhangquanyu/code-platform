package cn.zhangquanyu.application.dto.vo;

import lombok.Data;

@Data
public class MicroserviceSummaryVO {

    private long modelCount;
    private long serviceCount;
    private long orchestrationCount;
}
