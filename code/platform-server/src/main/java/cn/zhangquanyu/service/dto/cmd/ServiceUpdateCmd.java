package cn.zhangquanyu.service.dto.cmd;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceUpdateCmd extends ServiceCreateCmd {
    // 更新体与创建体结构一致（整体写入）
}
