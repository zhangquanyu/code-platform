package cn.zhangquanyu.service.dto.cmd;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrchestrationUpdateCmd {

    @NotBlank(message = "编排名称不能为空")
    @Size(max = 128)
    private String name;

    @Size(max = 1024)
    private String description;

    private Integer status;

    private String txType;

    private Integer txTimeout;

    @Valid
    private List<OrchParamCmd> inputParams;

    @Valid
    private List<OrchParamCmd> outputParams;

    @Valid
    private List<OrchNodeCmd> nodes;

    @Valid
    private List<OrchEdgeCmd> edges;

    @Data
    public static class OrchParamCmd {
        private Long id;
        @NotBlank(message = "参数名不能为空")
        private String paramName;
        @NotBlank(message = "数据类型不能为空")
        private String dataType;
        private Integer isRequired = 1;
        private String paramComment;
        // 出参来源
        private String sourceNodeKey;
        private String sourceField;
    }

    @Data
    public static class OrchNodeCmd {
        private Long id;
        @NotBlank(message = "节点key不能为空")
        private String nodeKey;
        @NotBlank(message = "节点类型不能为空")
        private String nodeType;
        private String nodeName;
        private Long serviceId;
        private String configJson;
        private String txType;
        private Integer txTimeout;
        private Integer retryCount;
        private Integer retryInterval;
        private String exceptionStrategy;
        private String loopType;
        private String branchExpr;
        @JsonProperty("xPos")
        private Integer xPos;
        @JsonProperty("yPos")
        private Integer yPos;
        private Integer sortOrder = 0;
    }

    @Data
    public static class OrchEdgeCmd {
        private Long id;
        @NotBlank(message = "连线key不能为空")
        private String edgeKey;
        @NotBlank(message = "起点不能为空")
        private String fromNodeKey;
        @NotBlank(message = "终点不能为空")
        private String toNodeKey;
        private String conditionExpr;
        private String labelText;
    }
}
