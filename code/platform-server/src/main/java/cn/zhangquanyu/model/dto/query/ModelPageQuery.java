package cn.zhangquanyu.model.dto.query;

import cn.zhangquanyu.shared.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ModelPageQuery extends PageQuery {

    private Long microserviceId;
    private Long applicationId;
}
