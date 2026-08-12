package cn.zhangquanyu.service.domain.entity;

import cn.zhangquanyu.shared.domain.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dev_service",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_dev_svc_ms_code",
                        columnNames = {"microservice_id", "code", "is_deleted"}),
                @UniqueConstraint(name = "uk_dev_svc_ms_path",
                        columnNames = {"microservice_id", "service_path", "is_deleted"})
        })
public class ServiceDef extends BaseEntity {

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

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "http_method", nullable = false, length = 16)
    private String httpMethod = "POST";

    @Column(name = "service_path", nullable = false, length = 256)
    private String servicePath;

    @Column(name = "category", length = 64)
    private String category;

    @Column(name = "status", nullable = false)
    private Integer status = 1;
}
