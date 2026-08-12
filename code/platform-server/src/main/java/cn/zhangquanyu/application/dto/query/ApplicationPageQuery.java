package cn.zhangquanyu.application.dto.query;

import cn.zhangquanyu.shared.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApplicationPageQuery extends PageQuery {

    private Integer status;
}
