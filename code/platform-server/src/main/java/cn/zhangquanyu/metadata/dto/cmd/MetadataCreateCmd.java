package cn.zhangquanyu.metadata.dto.cmd;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MetadataCreateCmd {

    @NotNull(message = "所属应用不能为空")
    private Long applicationId;

    @NotBlank(message = "元数据名称不能为空")
    @Size(max = 128)
    private String name;

    @NotBlank(message = "元数据编码不能为空")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]{0,63}$", message = "元数据编码格式不正确")
    private String code;

    @Size(max = 512)
    private String description;
}
