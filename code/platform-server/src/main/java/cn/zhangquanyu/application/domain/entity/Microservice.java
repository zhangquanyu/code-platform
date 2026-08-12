package cn.zhangquanyu.application.domain.entity;

import cn.zhangquanyu.shared.domain.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dev_microservice",
        uniqueConstraints = @UniqueConstraint(name = "uk_dev_ms_app_code",
                columnNames = {"application_id", "code", "is_deleted"}))
public class Microservice extends BaseEntity {

    @Id
    @Column(name = "id")
    @GenericGenerator(name = "snowflake", strategy = "cn.zhangquanyu.infrastructure.config.SnowflakeConfig")
    @GeneratedValue(generator = "snowflake")
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "version", length = 32)
    private String version = "1.0.0";

    @Column(name = "status", nullable = false)
    private Integer status = 1;
}
