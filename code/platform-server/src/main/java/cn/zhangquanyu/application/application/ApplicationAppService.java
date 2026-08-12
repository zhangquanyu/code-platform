package cn.zhangquanyu.application.application;

import cn.zhangquanyu.application.converter.ApplicationConverter;
import cn.zhangquanyu.application.domain.entity.Application;
import cn.zhangquanyu.application.domain.entity.Microservice;
import cn.zhangquanyu.application.domain.repository.ApplicationRepository;
import cn.zhangquanyu.application.domain.repository.MicroserviceRepository;
import cn.zhangquanyu.application.dto.cmd.ApplicationCreateCmd;
import cn.zhangquanyu.application.dto.cmd.ApplicationUpdateCmd;
import cn.zhangquanyu.application.dto.cmd.StatusCmd;
import cn.zhangquanyu.application.dto.query.ApplicationPageQuery;
import cn.zhangquanyu.application.dto.vo.ApplicationSimpleVO;
import cn.zhangquanyu.application.dto.vo.ApplicationVO;
import cn.zhangquanyu.metadata.domain.repository.MetadataRepository;
import cn.zhangquanyu.shared.api.PageResult;
import cn.zhangquanyu.shared.exception.BusinessException;
import cn.zhangquanyu.shared.util.SpecUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationAppService {

    private final ApplicationRepository applicationRepository;
    private final MicroserviceRepository microserviceRepository;
    private final MetadataRepository metadataRepository;
    private final ApplicationConverter converter;

    @Transactional(readOnly = true)
    public PageResult<ApplicationVO> page(ApplicationPageQuery query) {
        log.info("[应用] 分页查询开始: pageNum={}, pageSize={}, keyword={}, status={}",
                query.getPageNum(), query.getPageSize(), query.getKeyword(), query.getStatus());
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        PageRequest pageRequest = PageRequest.of(
                Math.max(0, query.getPageNum() - 1), query.getPageSize(), sort);

        Specification<Application> spec = (root, q, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), 0));
            if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
                predicates.add(SpecUtil.keyword(root, cb, query.getKeyword(), "name", "code"));
            }
            if (query.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), query.getStatus()));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Application> page = applicationRepository.findAll(spec, pageRequest);
        log.info("[应用] 分页查询完成: total={}, 当前页记录数={}", page.getTotalElements(), page.getNumberOfElements());
        return PageResult.of(converter.toVOList(page.getContent()),
                page.getTotalElements(), query.getPageNum(), query.getPageSize());
    }

    @Transactional(readOnly = true)
    public ApplicationVO getById(Long id) {
        log.info("[应用] 查询详情开始: id={}", id);
        Application app = findOrThrow(id);
        log.info("[应用] 查询详情成功: id={}, name={}, code={}", id, app.getName(), app.getCode());
        return converter.toVO(app);
    }

    @Transactional(readOnly = true)
    public List<ApplicationSimpleVO> listSimple() {
        log.info("[应用] 查询启用中的简易列表开始");
        List<Application> list = applicationRepository.findAll(
                (root, q, cb) -> cb.and(
                        cb.equal(root.get("isDeleted"), 0),
                        cb.equal(root.get("status"), 1)),
                Sort.by(Sort.Direction.DESC, "createTime"));
        log.info("[应用] 简易列表查询完成: count={}", list.size());
        return list.stream().map(converter::toSimpleVO).toList();
    }

    @Transactional
    public ApplicationVO create(ApplicationCreateCmd cmd) {
        log.info("[应用] 创建开始: name={}, code={}, version={}, description={}",
                cmd.getName(), cmd.getCode(), cmd.getVersion(), cmd.getDescription());
        if (applicationRepository.existsByCodeAndIsDeleted(cmd.getCode(), 0)) {
            log.warn("[应用] 创建失败-编码已存在: code={}", cmd.getCode());
            throw new BusinessException(60001, "应用编码已存在: " + cmd.getCode());
        }
        Application entity = converter.toEntity(cmd);
        if (entity.getVersion() == null) {
            entity.setVersion("1.0.0");
        }
        entity.setStatus(1);
        entity.setIsDeleted(0);
        log.debug("[应用] 准备保存实体: name={}, code={}, version={}, status={}",
                entity.getName(), entity.getCode(), entity.getVersion(), entity.getStatus());
        applicationRepository.save(entity);
        log.info("[应用] 创建成功: id={}, name={}, code={}", entity.getId(), entity.getName(), entity.getCode());
        return converter.toVO(entity);
    }

    @Transactional
    public ApplicationVO update(Long id, ApplicationUpdateCmd cmd) {
        log.info("[应用] 更新开始: id={}, name={}, version={}, description={}",
                id, cmd.getName(), cmd.getVersion(), cmd.getDescription());
        Application app = findOrThrow(id);
        log.debug("[应用] 更新前数据: id={}, name={}, version={}, description={}",
                id, app.getName(), app.getVersion(), app.getDescription());
        app.setName(cmd.getName());
        if (cmd.getVersion() != null) {
            app.setVersion(cmd.getVersion());
        }
        app.setDescription(cmd.getDescription());
        applicationRepository.save(app);
        log.info("[应用] 更新成功: id={}, name={}, version={}", id, app.getName(), app.getVersion());
        return converter.toVO(app);
    }

    @Transactional
    public void delete(Long id) {
        log.info("[应用] 删除开始: id={}", id);
        findOrThrow(id);
        long msCount = microserviceRepository.countByApplicationIdAndIsDeleted(id, 0);
        long metaCount = metadataRepository.countByApplicationIdAndIsDeleted(id, 0);
        log.debug("[应用] 关联资源统计: id={}, microserviceCount={}, metadataCount={}", id, msCount, metaCount);
        if (msCount > 0 || metaCount > 0) {
            log.warn("[应用] 删除失败-存在关联资源: id={}, msCount={}, metaCount={}", id, msCount, metaCount);
            throw new BusinessException(60002, "存在关联资源（微服务或元数据），无法删除");
        }
        applicationRepository.softDelete(id);
        log.info("[应用] 删除成功(软删除): id={}", id);
    }

    @Transactional
    public void updateStatus(Long id, StatusCmd cmd) {
        log.info("[应用] 更新状态开始: id={}, status={}", id, cmd.getStatus());
        Application app = findOrThrow(id);
        app.setStatus(cmd.getStatus());
        applicationRepository.save(app);
        log.info("[应用] 更新状态成功: id={}, name={}, status={}", id, app.getName(), cmd.getStatus());
    }

    private Application findOrThrow(Long id) {
        Application app = applicationRepository.findByIdAndIsDeleted(id, 0)
                .orElse(null);
        if (app == null) {
            log.warn("[应用] 实体未找到或已删除: id={}, isDeleted=0", id);
            throw new BusinessException(60003, "应用不存在: " + id);
        }
        log.debug("[应用] 实体查询成功: id={}, name={}, code={}, status={}, version={}",
                id, app.getName(), app.getCode(), app.getStatus(), app.getVersion());
        return app;
    }

    /**
     * 应用名查询（供微服务模块填充 applicationName）
     */
    @Transactional(readOnly = true)
    public String getNameById(Long applicationId) {
        return applicationRepository.findByIdAndIsDeleted(applicationId, 0)
                .map(Application::getName).orElse(null);
    }

    /**
     * 应用下微服务列表（详情页 Tab）
     */
    @Transactional(readOnly = true)
    public List<Microservice> listMicroservices(Long applicationId) {
        return microserviceRepository.findByApplicationIdAndIsDeletedOrderByCreateTimeDesc(applicationId, 0);
    }
}
