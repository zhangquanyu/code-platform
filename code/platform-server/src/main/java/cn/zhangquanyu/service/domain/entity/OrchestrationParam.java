package cn.zhangquanyu.service.domain.entity;

import cn.zhangquanyu.shared.domain.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dev_orch_param",
        uniqueConstraints = @UniqueConstraint(name = "uk_dev_op_orch_scope_name",
                columnNames = {"orchestration_id", "param_scope", "param_name", "is_deleted"}))
public class OrchestrationParam extends BaseEntity {

    public static final String SCOPE_INPUT = "INPUT";
    public static final String SCOPE_OUTPUT = "OUTPUT";

    @Id
    @Column(name = "id")
    @GenericGenerator(name = "snowflake", strategy = "cn.zhangquanyu.infrastructure.config.SnowflakeConfig")
    @GeneratedValue(generator = "snowflake")
    private Long id;

    @Column(name = "orchestration_id", nullable = false)
    private Long orchestrationId;

    @Column(name = "param_scope", nullable = false, length = 16)
    private String paramScope;

    @Column(name = "param_name", nullable = false, length = 64)
    private String paramName;

    @Column(name = "data_type", nullable = false, length = 32)
    private String dataType;

    @Column(name = "is_required", nullable = false)
    private Integer isRequired = 1;

    @Column(name = "param_comment", length = 512)
    private String paramComment;

    @Column(name = "source_node_key", length = 64)
    private String sourceNodeKey;

    @Column(name = "source_field", length = 128)
    private String sourceField;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
