package cn.zhangquanyu.model.dto.cmd;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ModelCreateCmd {

    @NotNull(message = "所属微服务不能为空")
    private Long microserviceId;

    @NotBlank(message = "模型名称不能为空")
    @Size(max = 128)
    private String name;

    @NotBlank(message = "模型编码不能为空")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]{0,63}$", message = "模型编码格式不正确")
    private String code;

    @Size(max = 512)
    private String description;
}
