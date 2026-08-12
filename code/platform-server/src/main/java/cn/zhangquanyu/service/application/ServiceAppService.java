package cn.zhangquanyu.service.application;

import cn.zhangquanyu.application.domain.entity.Microservice;
import cn.zhangquanyu.application.domain.repository.MicroserviceRepository;
import cn.zhangquanyu.service.converter.ServiceConverter;
import cn.zhangquanyu.service.domain.entity.ServiceDef;
import cn.zhangquanyu.service.domain.entity.ServiceParam;
import cn.zhangquanyu.service.domain.repository.OrchestrationNodeRepository;
import cn.zhangquanyu.service.domain.repository.ServiceDefRepository;
import cn.zhangquanyu.service.domain.repository.ServiceParamRepository;
import cn.zhangquanyu.service.dto.cmd.ServiceCreateCmd;
import cn.zhangquanyu.service.dto.query.ServicePageQuery;
import cn.zhangquanyu.service.dto.vo.ServiceDetailVO;
import cn.zhangquanyu.service.dto.vo.ServiceParamVO;
import cn.zhangquanyu.service.dto.vo.ServiceSimpleVO;
import cn.zhangquanyu.service.dto.vo.ServiceVO;
import cn.zhangquanyu.shared.api.PageResult;
import cn.zhangquanyu.shared.api.StatusCmd;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceAppService {

    private final ServiceDefRepository serviceDefRepository;
    private final ServiceParamRepository serviceParamRepository;
    private final MicroserviceRepository microserviceRepository;
    private final OrchestrationNodeRepository orchestrationNodeRepository;
    private final ServiceConverter converter;

    @Transactional(readOnly = true)
    public PageResult<ServiceVO> page(ServicePageQuery query) {
        log.info("[服务] 分页查询开始: pageNum={}, pageSize={}, msId={}, keyword={}, category={}, status={}",
                query.getPageNum(), query.getPageSize(), query.getMicroserviceId(),
                query.getKeyword(), query.getCategory(), query.getStatus());
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        PageRequest pageRequest = PageRequest.of(
                Math.max(0, query.getPageNum() - 1), query.getPageSize(), sort);

        Specification<ServiceDef> spec = (root, q, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), 0));
            if (query.getMicroserviceId() != null) {
                predicates.add(cb.equal(root.get("microserviceId"), query.getMicroserviceId()));
            }
            if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
                predicates.add(SpecUtil.keyword(root, cb, query.getKeyword(), "name", "code", "servicePath"));
            }
            if (query.getCategory() != null && !query.getCategory().isBlank()) {
                predicates.add(cb.equal(root.get("category"), query.getCategory()));
            }
            if (query.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), query.getStatus()));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<ServiceDef> page = serviceDefRepository.findAll(spec, pageRequest);
        log.info("[服务] 分页查询完成: total={}, 当前页记录数={}", page.getTotalElements(), page.getNumberOfElements());
        List<ServiceVO> list = page.getContent().stream().map(this::toVOWithStats).toList();
        return PageResult.of(list, page.getTotalElements(), query.getPageNum(), query.getPageSize());
    }

    @Transactional(readOnly = true)
    public ServiceDetailVO getById(Long id) {
        log.info("[服务] 查询详情开始: id={}", id);
        ServiceDef svc = findOrThrow(id);
        ServiceDetailVO detail = new ServiceDetailVO();
        detail.setService(toVOWithStats(svc));
        List<ServiceParam> params = serviceParamRepository
                .findByServiceIdAndIsDeletedOrderBySortOrderAsc(id, 0);
        List<ServiceParamVO> inputs = converter.toParamVOList(params.stream()
                .filter(p -> p.getParamType() == ServiceParam.TYPE_INPUT).toList());
        List<ServiceParamVO> outputs = converter.toParamVOList(params.stream()
                .filter(p -> p.getParamType() == ServiceParam.TYPE_OUTPUT).toList());
        detail.setInputs(inputs);
        detail.setOutputs(outputs);
        log.info("[服务] 查询详情成功: id={}, name={}, code={}, inputs={}, outputs={}",
                id, svc.getName(), svc.getCode(), inputs.size(), outputs.size());
        return detail;
    }

    @Transactional(readOnly = true)
    public List<ServiceSimpleVO> listByMicroservice(Long microserviceId) {
        log.info("[服务] 按微服务查询启用列表: microserviceId={}", microserviceId);
        List<ServiceDef> list = serviceDefRepository
                .findByMicroserviceIdAndIsDeletedAndStatusOrderByCreateTimeDesc(microserviceId, 0, 1);
        log.info("[服务] 按微服务查询完成: microserviceId={}, count={}", microserviceId, list.size());
        return list.stream().map(converter::toSimpleVO).toList();
    }

    @Transactional
    public ServiceDetailVO create(ServiceCreateCmd cmd) {
        log.info("[服务] 创建开始: name={}, code={}, msId={}, method={}, path={}, category={}",
                cmd.getName(), cmd.getCode(), cmd.getMicroserviceId(),
                cmd.getHttpMethod(), cmd.getServicePath(), cmd.getCategory());
        microserviceRepository.findByIdAndIsDeleted(cmd.getMicroserviceId(), 0)
                .orElseThrow(() -> new BusinessException(61004, "微服务不存在: " + cmd.getMicroserviceId()));
        if (serviceDefRepository.existsByMicroserviceIdAndCodeAndIsDeleted(
                cmd.getMicroserviceId(), cmd.getCode(), 0)) {
            log.warn("[服务] 创建失败-编码已存在: msId={}, code={}", cmd.getMicroserviceId(), cmd.getCode());
            throw new BusinessException(64002, "服务编码在该微服务内已存在: " + cmd.getCode());
        }
        if (serviceDefRepository.existsByMicroserviceIdAndServicePathAndIsDeleted(
                cmd.getMicroserviceId(), cmd.getServicePath(), 0)) {
            log.warn("[服务] 创建失败-路径已存在: msId={}, path={}", cmd.getMicroserviceId(), cmd.getServicePath());
            throw new BusinessException(64003, "服务路径在该微服务内已存在: " + cmd.getServicePath());
        }
        ServiceDef entity = new ServiceDef();
        entity.setMicroserviceId(cmd.getMicroserviceId());
        entity.setName(cmd.getName());
        entity.setCode(cmd.getCode());
        entity.setDescription(cmd.getDescription());
        entity.setHttpMethod(cmd.getHttpMethod());
        entity.setServicePath(cmd.getServicePath());
        entity.setCategory(cmd.getCategory());
        entity.setStatus(cmd.getStatus() == null ? 1 : cmd.getStatus());
        entity.setIsDeleted(0);
        log.debug("[服务] 准备保存主体: name={}, code={}, method={}, path={}",
                entity.getName(), entity.getCode(), entity.getHttpMethod(), entity.getServicePath());
        serviceDefRepository.save(entity);
        log.info("[服务] 服务主体创建成功: id={}, name={}, code={}", entity.getId(), entity.getName(), entity.getCode());

        log.debug("[服务] 开始保存参数: serviceId={}, inputCount={}, outputCount={}",
                entity.getId(),
                cmd.getInputs() == null ? 0 : cmd.getInputs().size(),
                cmd.getOutputs() == null ? 0 : cmd.getOutputs().size());
        saveParams(entity.getId(), cmd.getInputs(), cmd.getOutputs());
        log.info("[服务] 创建完成(含参数): id={}", entity.getId());
        return getById(entity.getId());
    }

    @Transactional
    public ServiceDetailVO update(Long id, ServiceCreateCmd cmd) {
        log.info("[服务] 更新开始: id={}, name={}, method={}, path={}", id, cmd.getName(), cmd.getHttpMethod(), cmd.getServicePath());
        ServiceDef svc = findOrThrow(id);
        svc.setName(cmd.getName());
        svc.setDescription(cmd.getDescription());
        svc.setHttpMethod(cmd.getHttpMethod());
        svc.setServicePath(cmd.getServicePath());
        svc.setCategory(cmd.getCategory());
        if (cmd.getStatus() != null) {
            svc.setStatus(cmd.getStatus());
        }
        serviceDefRepository.save(svc);

        // 整体重写参数：先软删除旧的，再写入新的
        serviceParamRepository.softDeleteByServiceId(id);
        saveParams(id, cmd.getInputs(), cmd.getOutputs());
        log.info("[服务] 更新完成(含参数重写): id={}", id);
        return getById(id);
    }

    @Transactional
    public void delete(Long id) {
        log.info("[服务] 删除开始: id={}", id);
        findOrThrow(id);
        long refCount = orchestrationNodeRepository.countByServiceIdAndIsDeleted(id, 0);
        log.debug("[服务] 删除校验: id={}, 被编排节点引用次数={}", id, refCount);
        if (refCount > 0) {
            log.warn("[服务] 删除失败-被编排节点引用: id={}, refCount={}", id, refCount);
            throw new BusinessException(64001, "服务已被编排节点引用，无法删除");
        }
        serviceParamRepository.softDeleteByServiceId(id);
        log.debug("[服务] 参数软删除完成: serviceId={}", id);
        serviceDefRepository.softDelete(id);
        log.info("[服务] 删除成功(软删除): id={}", id);
    }

    @Transactional
    public void updateStatus(Long id, StatusCmd cmd) {
        log.info("[服务] 更新状态开始: id={}, status={}", id, cmd.getStatus());
        ServiceDef svc = findOrThrow(id);
        svc.setStatus(cmd.getStatus());
        serviceDefRepository.save(svc);
        log.info("[服务] 更新状态成功: id={}, name={}, status={}", id, svc.getName(), cmd.getStatus());
    }

    private void saveParams(Long serviceId,
                            List<ServiceCreateCmd.ParamItem> inputs,
                            List<ServiceCreateCmd.ParamItem> outputs) {
        Set<String> inputNames = new HashSet<>();
        Set<String> outputNames = new HashSet<>();
        saveParamItems(serviceId, ServiceParam.TYPE_INPUT, inputs, inputNames);
        saveParamItems(serviceId, ServiceParam.TYPE_OUTPUT, outputs, outputNames);
    }

    private void saveParamItems(Long serviceId, int paramType,
                                List<ServiceCreateCmd.ParamItem> items, Set<String> nameSet) {
        if (items == null) {
            return;
        }
        for (ServiceCreateCmd.ParamItem i : items) {
            if (!nameSet.add(i.getParamName())) {
                String type = paramType == ServiceParam.TYPE_INPUT ? "入参" : "出参";
                throw new BusinessException(64004, type + "名重复: " + i.getParamName());
            }
            ServiceParam p = new ServiceParam();
            p.setServiceId(serviceId);
            p.setParamType(paramType);
            p.setParamName(i.getParamName());
            p.setDataType(i.getDataType());
            p.setIsRequired(i.getIsRequired() == null ? 1 : i.getIsRequired());
            p.setDefaultValue(i.getDefaultValue());
            p.setModelFieldId(i.getModelFieldId());
            p.setSortOrder(i.getSortOrder() == null ? 0 : i.getSortOrder());
            p.setParamComment(i.getParamComment());
            p.setIsDeleted(0);
            serviceParamRepository.save(p);
        }
    }

    private ServiceVO toVOWithStats(ServiceDef svc) {
        ServiceVO vo = converter.toVO(svc);
        List<ServiceParam> params = serviceParamRepository
                .findByServiceIdAndIsDeletedOrderBySortOrderAsc(svc.getId(), 0);
        vo.setInputCount(params.stream().filter(p -> p.getParamType() == ServiceParam.TYPE_INPUT).count());
        vo.setOutputCount(params.stream().filter(p -> p.getParamType() == ServiceParam.TYPE_OUTPUT).count());
        microserviceRepository.findByIdAndIsDeleted(svc.getMicroserviceId(), 0)
                .ifPresent(ms -> vo.setMicroserviceName(ms.getName()));
        return vo;
    }

    private ServiceDef findOrThrow(Long id) {
        ServiceDef svc = serviceDefRepository.findByIdAndIsDeleted(id, 0)
                .orElse(null);
        if (svc == null) {
            log.warn("[服务] 实体未找到或已删除: id={}, isDeleted=0", id);
            throw new BusinessException(64005, "服务不存在: " + id);
        }
        log.debug("[服务] 实体查询成功: id={}, name={}, code={}, msId={}, method={}, path={}, status={}",
                id, svc.getName(), svc.getCode(), svc.getMicroserviceId(),
                svc.getHttpMethod(), svc.getServicePath(), svc.getStatus());
        return svc;
    }
}
