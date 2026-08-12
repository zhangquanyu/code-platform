package cn.zhangquanyu.service.dto.vo;

import lombok.Data;

import java.util.List;

@Data
public class OrchHealthVO {

    private boolean healthy;
    private List<Alert> alerts;

    @Data
    public static class Alert {
        private String nodeKey;
        private Long serviceId;
        private String reason;
    }
}
