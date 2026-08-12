package cn.zhangquanyu.service.domain.entity;

import cn.zhangquanyu.shared.domain.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dev_service_param",
        uniqueConstraints = @UniqueConstraint(name = "uk_dev_sp_svc_type_name",
                columnNames = {"service_id", "param_type", "param_name", "is_deleted"}))
public class ServiceParam extends BaseEntity {

    public static final int TYPE_INPUT = 1;
    public static final int TYPE_OUTPUT = 2;

    @Id
    @Column(name = "id")
    @GenericGenerator(name = "snowflake", strategy = "cn.zhangquanyu.infrastructure.config.SnowflakeConfig")
    @GeneratedValue(generator = "snowflake")
    private Long id;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "param_type", nullable = false)
    private Integer paramType;

    @Column(name = "param_name", nullable = false, length = 64)
    private String paramName;

    @Column(name = "data_type", nullable = false, length = 32)
    private String dataType;

    @Column(name = "is_required", nullable = false)
    private Integer isRequired = 1;

    @Column(name = "default_value", length = 256)
    private String defaultValue;

    @Column(name = "model_field_id")
    private Long modelFieldId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "param_comment", length = 512)
    private String paramComment;
}
