package cn.zhangquanyu.metadata.dto.query;

import cn.zhangquanyu.shared.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MetadataPageQuery extends PageQuery {

    private Long applicationId;
    private Integer status;
}
