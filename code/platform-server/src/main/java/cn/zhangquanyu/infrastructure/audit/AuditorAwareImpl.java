package cn.zhangquanyu.infrastructure.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 审计字段填充（本期 Mock 用户 ID=1）
 */
@Component("auditorAwareImpl")
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        // TODO 后续接入鉴权后从 SecurityContext 获取真实用户 ID
        return Optional.of(1L);
    }
}
