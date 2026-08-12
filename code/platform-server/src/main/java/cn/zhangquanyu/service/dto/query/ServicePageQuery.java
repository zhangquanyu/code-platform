package cn.zhangquanyu.service.dto.query;

import cn.zhangquanyu.shared.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServicePageQuery extends PageQuery {

    private Long microserviceId;
    private Long applicationId;
    private String category;
    private Integer status;
}
