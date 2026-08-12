package cn.zhangquanyu.infrastructure.config;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 雪花算法主键生成器（简化版，生产建议使用完整雪花实现或分布式 ID 服务）
 * 本期为单机自增 + 时间戳偏移，保证唯一即可。
 */
@Component("snowflake")
public class SnowflakeConfig implements IdentifierGenerator {

    private static final AtomicLong COUNTER = new AtomicLong(0);
    private static final long BASE = 1700000000000L; // 2023-11-14

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        long time = System.currentTimeMillis() - BASE;
        long count = COUNTER.incrementAndGet() % 100000;
        return time * 100000 + count;
    }
}
