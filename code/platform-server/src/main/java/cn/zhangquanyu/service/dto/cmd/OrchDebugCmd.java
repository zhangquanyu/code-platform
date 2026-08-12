package cn.zhangquanyu.service.dto.cmd;

import lombok.Data;

import java.util.Map;

@Data
public class OrchDebugCmd {

    private Map<String, Object> inputData;
}
