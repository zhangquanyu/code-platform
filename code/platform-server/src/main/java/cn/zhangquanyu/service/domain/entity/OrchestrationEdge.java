package cn.zhangquanyu.service.domain.entity;

import cn.zhangquanyu.shared.domain.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dev_orch_edge",
        uniqueConstraints = @UniqueConstraint(name = "uk_dev_oe_orch_edgekey",
                columnNames = {"orchestration_id", "edge_key", "is_deleted"}))
public class OrchestrationEdge extends BaseEntity {

    @Id
    @Column(name = "id")
    @GenericGenerator(name = "snowflake", strategy = "cn.zhangquanyu.infrastructure.config.SnowflakeConfig")
    @GeneratedValue(generator = "snowflake")
    private Long id;

    @Column(name = "orchestration_id", nullable = false)
    private Long orchestrationId;

    @Column(name = "edge_key", nullable = false, length = 64)
    private String edgeKey;

    @Column(name = "from_node_key", nullable = false, length = 64)
    private String fromNodeKey;

    @Column(name = "to_node_key", nullable = false, length = 64)
    private String toNodeKey;

    @Column(name = "condition_expr", length = 1024)
    private String conditionExpr;

    @Column(name = "label_text", length = 256)
    private String labelText;
}
