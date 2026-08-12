package cn.zhangquanyu.metadata.dto.cmd;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MetadataUpdateCmd {

    @NotBlank(message = "元数据名称不能为空")
    @Size(max = 128)
    private String name;

    @Size(max = 512)
    private String description;
}
