package cn.zhangquanyu.shared.api;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 状态切换命令（启用/停用）
 */
@Data
public class StatusCmd {

    @NotNull(message = "状态不能为空")
    private Integer status;
}
