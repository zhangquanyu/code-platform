package cn.zhangquanyu.service.dto.cmd;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ServiceCreateCmd {

    @NotNull(message = "所属微服务不能为空")
    private Long microserviceId;

    @NotBlank(message = "服务名称不能为空")
    @Size(max = 128)
    private String name;

    @NotBlank(message = "服务编码不能为空")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]{0,63}$", message = "服务编码格式不正确")
    private String code;

    @Size(max = 1024)
    private String description;

    @NotBlank(message = "请求方式不能为空")
    private String httpMethod;

    @NotBlank(message = "服务路径不能为空")
    @Size(max = 256)
    private String servicePath;

    @Size(max = 64)
    private String category;

    private Integer status = 1;

    @Valid
    private List<ParamItem> inputs;

    @Valid
    private List<ParamItem> outputs;

    @Data
    public static class ParamItem {
        @NotBlank(message = "参数名不能为空")
        @Size(max = 64)
        private String paramName;
        @NotBlank(message = "数据类型不能为空")
        private String dataType;
        private Integer isRequired = 1;
        private String defaultValue;
        private Long modelFieldId;
        private Integer sortOrder = 0;
        @Size(max = 512)
        private String paramComment;
    }
}
