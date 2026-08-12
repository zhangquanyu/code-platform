package cn.zhangquanyu.service.dto.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OrchDebugResultVO {

    private boolean success;
    private Map<String, Object> output;
    private List<NodeResult> nodeResults;
    private long totalDurationMs;

    @Data
    public static class NodeResult {
        private String nodeKey;
        private String nodeName;
        private String status;
        private long durationMs;
        private Map<String, Object> input;
        private Map<String, Object> output;
        private String error;
    }
}
