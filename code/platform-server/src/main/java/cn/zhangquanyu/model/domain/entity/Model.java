package cn.zhangquanyu.model.domain.entity;

import cn.zhangquanyu.shared.domain.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dev_model",
        uniqueConstraints = @UniqueConstraint(name = "uk_dev_model_ms_code",
                columnNames = {"microservice_id", "code", "is_deleted"}))
public class Model extends BaseEntity {

    @Id
    @Column(name = "id")
    @GenericGenerator(name = "snowflake", strategy = "cn.zhangquanyu.infrastructure.config.SnowflakeConfig")
    @GeneratedValue(generator = "snowflake")
    private Long id;

    @Column(name = "microservice_id", nullable = false)
    private Long microserviceId;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "description", length = 512)
    private String description;
}
