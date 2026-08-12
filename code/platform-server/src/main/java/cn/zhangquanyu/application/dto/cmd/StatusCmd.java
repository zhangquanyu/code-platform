package cn.zhangquanyu.application.dto.cmd;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusCmd {

    @NotNull(message = "状态不能为空")
    private Integer status;
}
