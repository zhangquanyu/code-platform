package cn.zhangquanyu.shared.domain;

/**
 * 启用/停用状态枚举
 */
public enum StatusEnum {

    DISABLED(0, "停用"),
    ENABLED(1, "启用");

    private final int value;
    private final String label;

    StatusEnum(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static StatusEnum of(Integer value) {
        if (value == null) {
            return null;
        }
        for (StatusEnum e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        return null;
    }
}
