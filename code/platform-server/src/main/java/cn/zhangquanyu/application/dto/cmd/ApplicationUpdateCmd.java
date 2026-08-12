package cn.zhangquanyu.application.dto.cmd;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApplicationUpdateCmd {

    @NotBlank(message = "应用名称不能为空")
    @Size(max = 128, message = "应用名称最长128字符")
    private String name;

    @Size(max = 32, message = "版本最长32字符")
    private String version;

    @Size(max = 512, message = "描述最长512字符")
    private String description;
}
