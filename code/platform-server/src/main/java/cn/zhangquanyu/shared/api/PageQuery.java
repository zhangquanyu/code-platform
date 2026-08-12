package cn.zhangquanyu.shared.api;

import lombok.Data;

/**
 * 分页查询基类
 */
@Data
public class PageQuery {

    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private String keyword;
    private String sortField;
    private String sortOrder;
}
