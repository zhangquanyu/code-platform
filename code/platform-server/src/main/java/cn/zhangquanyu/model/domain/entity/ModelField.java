package cn.zhangquanyu.model.domain.entity;

import cn.zhangquanyu.shared.domain.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dev_model_field",
        uniqueConstraints = @UniqueConstraint(name = "uk_dev_mf_model_name",
                columnNames = {"model_id", "name", "is_deleted"}))
public class ModelField extends BaseEntity {

    @Id
    @Column(name = "id")
    @GenericGenerator(name = "snowflake", strategy = "cn.zhangquanyu.infrastructure.config.SnowflakeConfig")
    @GeneratedValue(generator = "snowflake")
    private Long id;

    @Column(name = "model_id", nullable = false)
    private Long modelId;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "display_name", nullable = false, length = 128)
    private String displayName;

    @Column(name = "field_type", nullable = false, length = 32)
    private String fieldType;

    @Column(name = "length")
    private Integer length;

    @Column(name = "`precision`")
    private Integer precision;

    @Column(name = "is_required", nullable = false)
    private Integer isRequired = 0;

    @Column(name = "is_primary", nullable = false)
    private Integer isPrimary = 0;

    @Column(name = "is_unique", nullable = false)
    private Integer isUnique = 0;

    @Column(name = "is_index", nullable = false)
    private Integer isIndex = 0;

    @Column(name = "default_value", length = 256)
    private String defaultValue;

    @Column(name = "metadata_id")
    private Long metadataId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "field_comment", length = 512)
    private String fieldComment;
}
