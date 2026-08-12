package cn.zhangquanyu.shared.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * JPA Specification / Predicate 工具
 */
public class SpecUtil {

    /**
     * 构建关键字模糊匹配 Predicate（OR 连接多个字段）
     *
     * @param root       实体 Root
     * @param cb         CriteriaBuilder
     * @param keyword    关键字（为空时返回 conjunction，等价无约束）
     * @param fieldNames 需要模糊匹配的字段名
     * @return 匹配关键字的条件 Predicate
     */
    public static <T> Predicate keyword(Root<T> root, CriteriaBuilder cb,
                                        String keyword, String... fieldNames) {
        if (keyword == null || keyword.isBlank()) {
            return cb.conjunction();
        }
        String like = "%" + keyword.trim() + "%";
        Predicate[] ors = new Predicate[fieldNames.length];
        for (int i = 0; i < fieldNames.length; i++) {
            ors[i] = cb.like(root.get(fieldNames[i]), like);
        }
        return cb.or(ors);
    }
}
