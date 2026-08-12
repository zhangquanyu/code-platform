package cn.zhangquanyu.application.dto.cmd;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MicroserviceCreateCmd {

    @NotNull(message = "所属应用不能为空")
    private Long applicationId;

    @NotBlank(message = "微服务名称不能为空")
    @Size(max = 128, message = "微服务名称最长128字符")
    private String name;

    @NotBlank(message = "微服务编码不能为空")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]{0,63}$", message = "微服务编码须以字母开头，仅含字母数字下划线，最长64")
    private String code;

    @Size(max = 32, message = "版本最长32字符")
    private String version;

    @Size(max = 512, message = "描述最长512字符")
    private String description;
}
