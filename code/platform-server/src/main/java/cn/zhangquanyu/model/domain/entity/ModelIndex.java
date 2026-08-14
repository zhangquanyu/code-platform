package cn.zhangquanyu.model.domain.entity;

import cn.zhangquanyu.shared.domain.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dev_model_index",
        uniqueConstraints = @UniqueConstraint(name = "uk_dev_mi_model_name",
                columnNames = {"model_id", "index_name", "is_deleted"}))
public class ModelIndex extends BaseEntity {

    @Id
    @Column(name = "id")
    @GenericGenerator(name = "snowflake", strategy = "cn.zhangquanyu.infrastructure.config.SnowflakeConfig")
    @GeneratedValue(generator = "snowflake")
    private Long id;

    @Column(name = "model_id", nullable = false)
    private Long modelId;

    @Column(name = "index_name", nullable = false, length = 64)
    private String indexName;

    @Column(name = "index_type", nullable = false, length = 20)
    private String indexType = "NORMAL";

    @Column(name = "field_ids", nullable = false, length = 1024)
    private String fieldIds;
}
