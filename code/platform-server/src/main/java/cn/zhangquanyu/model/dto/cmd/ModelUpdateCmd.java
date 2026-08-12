package cn.zhangquanyu.model.dto.cmd;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ModelUpdateCmd {

    @NotBlank(message = "模型名称不能为空")
    @Size(max = 128)
    private String name;

    @Size(max = 512)
    private String description;
}
