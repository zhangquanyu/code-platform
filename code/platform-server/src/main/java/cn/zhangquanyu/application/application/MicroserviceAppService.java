package cn.zhangquanyu.application.application;

import cn.zhangquanyu.application.converter.MicroserviceConverter;
import cn.zhangquanyu.application.domain.entity.Application;
import cn.zhangquanyu.application.domain.entity.Microservice;
import cn.zhangquanyu.application.domain.repository.ApplicationRepository;
import cn.zhangquanyu.application.domain.repository.MicroserviceRepository;
import cn.zhangquanyu.application.dto.cmd.MicroserviceCreateCmd;
import cn.zhangquanyu.application.dto.cmd.MicroserviceUpdateCmd;
import cn.zhangquanyu.application.dto.cmd.StatusCmd;
import cn.zhangquanyu.application.dto.query.MicroservicePageQuery;
import cn.zhangquanyu.application.dto.vo.MicroserviceSimpleVO;
import cn.zhangquanyu.application.dto.vo.MicroserviceSummaryVO;
import cn.zhangquanyu.application.dto.vo.MicroserviceVO;
import cn.zhangquanyu.model.domain.repository.ModelRepository;
import cn.zhangquanyu.service.domain.repository.OrchestrationRepository;
import cn.zhangquanyu.service.domain.repository.ServiceDefRepository;
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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MicroserviceAppService {

    private final MicroserviceRepository microserviceRepository;
    private final ApplicationRepository applicationRepository;
    private final ModelRepository modelRepository;
    private final ServiceDefRepository serviceDefRepository;
    private final OrchestrationRepository orchestrationRepository;
    private final MicroserviceConverter converter;

    @Transactional(readOnly = true)
    public PageResult<MicroserviceVO> page(MicroservicePageQuery query) {
        log.info("[微服务] 分页查询开始: pageNum={}, pageSize={}, appId={}, keyword={}, status={}",
                query.getPageNum(), query.getPageSize(), query.getApplicationId(), query.getKeyword(), query.getStatus());
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        PageRequest pageRequest = PageRequest.of(
                Math.max(0, query.getPageNum() - 1), query.getPageSize(), sort);

        Specification<Microservice> spec = (root, q, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), 0));
            if (query.getApplicationId() != null) {
                predicates.add(cb.equal(root.get("applicationId"), query.getApplicationId()));
            }
            if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
                predicates.add(SpecUtil.keyword(root, cb, query.getKeyword(), "name", "code"));
            }
            if (query.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), query.getStatus()));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Microservice> page = microserviceRepository.findAll(spec, pageRequest);
        log.info("[微服务] 分页查询完成: total={}, 当前页记录数={}", page.getTotalElements(), page.getNumberOfElements());
        List<MicroserviceVO> list = page.getContent().stream().map(this::fillAppName).toList();
        return PageResult.of(list, page.getTotalElements(), query.getPageNum(), query.getPageSize());
    }

    @Transactional(readOnly = true)
    public MicroserviceVO getById(Long id) {
        log.info("[微服务] 查询详情开始: id={}", id);
        Microservice ms = findOrThrow(id);
        log.info("[微服务] 查询详情成功: id={}, name={}, code={}, appId={}", id, ms.getName(), ms.getCode(), ms.getApplicationId());
        return fillAppName(ms);
    }

    @Transactional(readOnly = true)
    public List<MicroserviceSimpleVO> listByApplication(Long applicationId) {
        log.info("[微服务] 按应用查询启用列表: applicationId={}", applicationId);
        List<Microservice> list = microserviceRepository
                .findByApplicationIdAndIsDeletedAndStatusOrderByCreateTimeDesc(applicationId, 0, 1);
        log.info("[微服务] 按应用查询完成: applicationId={}, count={}", applicationId, list.size());
        return list.stream().map(converter::toSimpleVO).toList();
    }

    @Transactional(readOnly = true)
    public MicroserviceSummaryVO summary(Long id) {
        log.info("[微服务] 查询资源汇总: id={}", id);
        findOrThrow(id);
        MicroserviceSummaryVO vo = new MicroserviceSummaryVO();
        vo.setModelCount(modelRepository.countByMicroserviceIdAndIsDeleted(id, 0));
        vo.setServiceCount(serviceDefRepository.countByMicroserviceIdAndIsDeleted(id, 0));
        vo.setOrchestrationCount(orchestrationRepository.countByMicroserviceIdAndIsDeleted(id, 0));
        log.info("[微服务] 资源汇总完成: id={}, models={}, services={}, orchestrations={}",
                id, vo.getModelCount(), vo.getServiceCount(), vo.getOrchestrationCount());
        return vo;
    }

    @Transactional
    public MicroserviceVO create(MicroserviceCreateCmd cmd) {
        log.info("[微服务] 创建开始: name={}, code={}, applicationId={}, description={}",
                cmd.getName(), cmd.getCode(), cmd.getApplicationId(), cmd.getDescription());
        Application app = applicationRepository.findByIdAndIsDeleted(cmd.getApplicationId(), 0)
                .orElse(null);
        if (app == null) {
            log.warn("[微服务] 创建失败-应用不存在: applicationId={}", cmd.getApplicationId());
            throw new BusinessException(61002, "应用不存在: " + cmd.getApplicationId());
        }
        log.debug("[微服务] 所属应用校验通过: appId={}, appName={}", app.getId(), app.getName());
        if (microserviceRepository.existsByApplicationIdAndCodeAndIsDeleted(
                cmd.getApplicationId(), cmd.getCode(), 0)) {
            log.warn("[微服务] 创建失败-编码已存在: applicationId={}, code={}", cmd.getApplicationId(), cmd.getCode());
            throw new BusinessException(61001, "微服务编码在该应用内已存在: " + cmd.getCode());
        }
        Microservice entity = converter.toEntity(cmd);
        if (entity.getVersion() == null) {
            entity.setVersion("1.0.0");
        }
        entity.setStatus(1);
        entity.setIsDeleted(0);
        log.debug("[微服务] 准备保存实体: name={}, code={}, appId={}, version={}",
                entity.getName(), entity.getCode(), entity.getApplicationId(), entity.getVersion());
        microserviceRepository.save(entity);
        log.info("[微服务] 创建成功: id={}, name={}, code={}, appId={}",
                entity.getId(), entity.getName(), entity.getCode(), entity.getApplicationId());
        MicroserviceVO vo = converter.toVO(entity);
        vo.setApplicationName(app.getName());
        return vo;
    }

    @Transactional
    public MicroserviceVO update(Long id, MicroserviceUpdateCmd cmd) {
        log.info("[微服务] 更新开始: id={}, name={}, version={}", id, cmd.getName(), cmd.getVersion());
        Microservice ms = findOrThrow(id);
        ms.setName(cmd.getName());
        if (cmd.getVersion() != null) {
            ms.setVersion(cmd.getVersion());
        }
        ms.setDescription(cmd.getDescription());
        microserviceRepository.save(ms);
        log.info("[微服务] 更新成功: id={}, name={}", id, ms.getName());
        return fillAppName(ms);
    }

    @Transactional
    public void delete(Long id) {
        log.info("[微服务] 删除开始: id={}", id);
        findOrThrow(id);
        long modelCount = modelRepository.countByMicroserviceIdAndIsDeleted(id, 0);
        long svcCount = serviceDefRepository.countByMicroserviceIdAndIsDeleted(id, 0);
        long orchCount = orchestrationRepository.countByMicroserviceIdAndIsDeleted(id, 0);
        log.debug("[微服务] 关联资源统计: id={}, modelCount={}, serviceCount={}, orchestrationCount={}",
                id, modelCount, svcCount, orchCount);
        if (modelCount > 0 || svcCount > 0 || orchCount > 0) {
            log.warn("[微服务] 删除失败-存在关联资源: id={}, models={}, services={}, orchestrations={}",
                    id, modelCount, svcCount, orchCount);
            throw new BusinessException(61003, "存在关联资源（模型/服务/编排），无法删除");
        }
        microserviceRepository.softDelete(id);
        log.info("[微服务] 删除成功(软删除): id={}", id);
    }

    @Transactional
    public void updateStatus(Long id, StatusCmd cmd) {
        log.info("[微服务] 更新状态开始: id={}, status={}", id, cmd.getStatus());
        Microservice ms = findOrThrow(id);
        ms.setStatus(cmd.getStatus());
        microserviceRepository.save(ms);
        log.info("[微服务] 更新状态成功: id={}, name={}, status={}", id, ms.getName(), cmd.getStatus());
    }

    private Microservice findOrThrow(Long id) {
        Microservice ms = microserviceRepository.findByIdAndIsDeleted(id, 0)
                .orElse(null);
        if (ms == null) {
            log.warn("[微服务] 实体未找到或已删除: id={}, isDeleted=0", id);
            throw new BusinessException(61004, "微服务不存在: " + id);
        }
        log.debug("[微服务] 实体查询成功: id={}, name={}, code={}, appId={}, status={}",
                id, ms.getName(), ms.getCode(), ms.getApplicationId(), ms.getStatus());
        return ms;
    }

    private MicroserviceVO fillAppName(Microservice ms) {
        MicroserviceVO vo = converter.toVO(ms);
        if (ms.getApplicationId() != null) {
            applicationRepository.findByIdAndIsDeleted(ms.getApplicationId(), 0)
                    .ifPresent(a -> {
                        vo.setApplicationName(a.getName());
                        log.debug("[微服务] 填充应用名: msId={}, appId={}, appName={}",
                                ms.getId(), a.getId(), a.getName());
                    });
        }
        return vo;
    }
}
