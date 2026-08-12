package cn.zhangquanyu.service.dto.cmd;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrchestrationCreateCmd {

    @NotNull(message = "所属微服务不能为空")
    private Long microserviceId;

    @NotBlank(message = "编排名称不能为空")
    @Size(max = 128)
    private String name;

    @NotBlank(message = "编排编码不能为空")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]{0,63}$", message = "编排编码格式不正确")
    private String code;

    @Size(max = 1024)
    private String description;

    private String txType = "LOCAL";

    private Integer txTimeout = 300;
}
