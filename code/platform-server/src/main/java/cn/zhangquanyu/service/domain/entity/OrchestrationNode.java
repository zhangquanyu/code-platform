package cn.zhangquanyu.service.domain.entity;

import cn.zhangquanyu.shared.domain.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dev_orch_node",
        uniqueConstraints = @UniqueConstraint(name = "uk_dev_on_orch_key",
                columnNames = {"orchestration_id", "node_key", "is_deleted"}))
public class OrchestrationNode extends BaseEntity {

    public static final String TYPE_START = "START";
    public static final String TYPE_SERVICE = "SERVICE";
    public static final String TYPE_ACTION = "ACTION";
    public static final String TYPE_CONDITION = "CONDITION";
    public static final String TYPE_LOOP = "LOOP";
    public static final String TYPE_BRANCH = "BRANCH";
    public static final String TYPE_END = "END";

    @Id
    @Column(name = "id")
    @GenericGenerator(name = "snowflake", strategy = "cn.zhangquanyu.infrastructure.config.SnowflakeConfig")
    @GeneratedValue(generator = "snowflake")
    private Long id;

    @Column(name = "orchestration_id", nullable = false)
    private Long orchestrationId;

    @Column(name = "node_key", nullable = false, length = 64)
    private String nodeKey;

    @Column(name = "node_type", nullable = false, length = 32)
    private String nodeType;

    @Column(name = "node_name", length = 128)
    private String nodeName;

    @Column(name = "service_id")
    private Long serviceId;

    @Lob
    @Column(name = "config_json")
    private String configJson;

    @Column(name = "tx_type", length = 20)
    private String txType = "LOCAL";

    @Column(name = "tx_timeout")
    private Integer txTimeout = 60;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "retry_interval")
    private Integer retryInterval = 1000;

    @Column(name = "exception_strategy", length = 20)
    private String exceptionStrategy = "INTERRUPT";

    @Column(name = "loop_type", length = 20)
    private String loopType = "SERIAL";

    @Column(name = "branch_expr", length = 1024)
    private String branchExpr;

    @Column(name = "x_pos")
    private Integer xPos;

    @Column(name = "y_pos")
    private Integer yPos;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
