package cn.zhangquanyu.model.application;

import cn.zhangquanyu.application.domain.entity.Microservice;
import cn.zhangquanyu.application.domain.repository.MicroserviceRepository;
import cn.zhangquanyu.metadata.domain.entity.Metadata;
import cn.zhangquanyu.metadata.domain.repository.MetadataRepository;
import cn.zhangquanyu.model.converter.ModelConverter;
import cn.zhangquanyu.model.domain.entity.Model;
import cn.zhangquanyu.model.domain.entity.ModelField;
import cn.zhangquanyu.model.domain.repository.ModelFieldRepository;
import cn.zhangquanyu.model.domain.repository.ModelRepository;
import cn.zhangquanyu.model.dto.cmd.ModelCreateCmd;
import cn.zhangquanyu.model.dto.cmd.ModelFieldBatchSaveCmd;
import cn.zhangquanyu.model.dto.cmd.ModelUpdateCmd;
import cn.zhangquanyu.model.dto.query.ModelPageQuery;
import cn.zhangquanyu.model.dto.vo.ModelDetailVO;
import cn.zhangquanyu.model.dto.vo.ModelFieldVO;
import cn.zhangquanyu.model.dto.vo.ModelSimpleVO;
import cn.zhangquanyu.model.dto.vo.ModelVO;
import cn.zhangquanyu.service.domain.repository.ServiceParamRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelAppService {

    private final ModelRepository modelRepository;
    private final ModelFieldRepository modelFieldRepository;
    private final MicroserviceRepository microserviceRepository;
    private final MetadataRepository metadataRepository;
    private final ServiceParamRepository serviceParamRepository;
    private final ModelConverter converter;

    @Transactional(readOnly = true)
    public PageResult<ModelVO> page(ModelPageQuery query) {
        log.info("[模型] 分页查询开始: pageNum={}, pageSize={}, msId={}, keyword={}",
                query.getPageNum(), query.getPageSize(), query.getMicroserviceId(), query.getKeyword());
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        PageRequest pageRequest = PageRequest.of(
                Math.max(0, query.getPageNum() - 1), query.getPageSize(), sort);

        Specification<Model> spec = (root, q, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), 0));
            if (query.getMicroserviceId() != null) {
                predicates.add(cb.equal(root.get("microserviceId"), query.getMicroserviceId()));
            }
            if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
                predicates.add(SpecUtil.keyword(root, cb, query.getKeyword(), "name", "code"));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Model> page = modelRepository.findAll(spec, pageRequest);
        log.info("[模型] 分页查询完成: total={}, 当前页记录数={}", page.getTotalElements(), page.getNumberOfElements());
        List<ModelVO> list = page.getContent().stream().map(this::toVOWithStats).toList();
        return PageResult.of(list, page.getTotalElements(), query.getPageNum(), query.getPageSize());
    }

    @Transactional(readOnly = true)
    public ModelDetailVO getById(Long id) {
        log.info("[模型] 查询详情开始: id={}", id);
        Model model = findOrThrow(id);
        ModelVO vo = toVOWithStats(model);
        List<ModelField> fields = modelFieldRepository
                .findByModelIdAndIsDeletedOrderBySortOrderAsc(id, 0);
        List<ModelFieldVO> fieldVOs = fillMetadataNames(converter.toFieldVOList(fields));
        ModelDetailVO detail = new ModelDetailVO();
        detail.setModel(vo);
        detail.setFields(fieldVOs);
        log.info("[模型] 查询详情成功: id={}, name={}, code={}, fieldCount={}", id, model.getName(), model.getCode(), fieldVOs.size());
        return detail;
    }

    @Transactional(readOnly = true)
    public List<ModelSimpleVO> listByMicroservice(Long microserviceId) {
        log.info("[模型] 按微服务查询列表: microserviceId={}", microserviceId);
        List<Model> list = modelRepository
                .findByMicroserviceIdAndIsDeletedOrderByCreateTimeDesc(microserviceId, 0);
        log.info("[模型] 按微服务查询完成: microserviceId={}, count={}", microserviceId, list.size());
        return list.stream().map(converter::toSimpleVO).toList();
    }

    @Transactional
    public ModelVO create(ModelCreateCmd cmd) {
        log.info("[模型] 创建开始: name={}, code={}, microserviceId={}", cmd.getName(), cmd.getCode(), cmd.getMicroserviceId());
        microserviceRepository.findByIdAndIsDeleted(cmd.getMicroserviceId(), 0)
                .orElseThrow(() -> new BusinessException(62003, "微服务不存在: " + cmd.getMicroserviceId()));
        if (modelRepository.existsByMicroserviceIdAndCodeAndIsDeleted(
                cmd.getMicroserviceId(), cmd.getCode(), 0)) {
            log.warn("[模型] 创建失败-编码已存在: msId={}, code={}", cmd.getMicroserviceId(), cmd.getCode());
            throw new BusinessException(62004, "模型编码在该微服务内已存在: " + cmd.getCode());
        }
        Model entity = converter.toEntity(cmd);
        entity.setIsDeleted(0);
        modelRepository.save(entity);
        log.info("[模型] 创建成功: id={}, name={}, code={}", entity.getId(), entity.getName(), entity.getCode());
        return toVOWithStats(entity);
    }

    @Transactional
    public ModelVO update(Long id, ModelUpdateCmd cmd) {
        log.info("[模型] 更新开始: id={}, name={}", id, cmd.getName());
        Model model = findOrThrow(id);
        model.setName(cmd.getName());
        model.setDescription(cmd.getDescription());
        modelRepository.save(model);
        log.info("[模型] 更新成功: id={}, name={}", id, model.getName());
        return toVOWithStats(model);
    }

    @Transactional
    public void delete(Long id) {
        log.info("[模型] 删除开始: id={}", id);
        findOrThrow(id);
        // 校验是否被服务参数引用
        long refCount = serviceParamRepository.countByModelFieldIn(
                modelFieldRepository.findByMetadataIdAndIsDeleted(id, 0)
                        .stream().map(ModelField::getId).toList());
        log.debug("[模型] 删除校验: id={}, 被服务参数引用次数={}", id, refCount);
        if (refCount > 0) {
            log.warn("[模型] 删除失败-被服务参数引用: id={}, refCount={}", id, refCount);
            throw new BusinessException(62001, "模型已被服务参数引用，无法删除");
        }
        modelFieldRepository.softDeleteByModelId(id);
        log.debug("[模型] 字段软删除完成: modelId={}", id);
        modelRepository.softDelete(id);
        log.info("[模型] 删除成功(软删除): id={}", id);
    }

    /**
     * 批量保存模型字段
     */
    @Transactional
    public List<ModelFieldVO> batchSaveFields(Long modelId, ModelFieldBatchSaveCmd cmd) {
        log.info("[模型] 批量保存字段开始: modelId={}, fieldCount={}, deletedIds={}",
                modelId, cmd.getFields() == null ? 0 : cmd.getFields().size(), cmd.getDeletedFieldIds());
        findOrThrow(modelId);
        List<ModelFieldBatchSaveCmd.FieldItem> items = cmd.getFields();

        // 1. 主键校验：至少一个主键字段
        boolean hasPrimary = items.stream().anyMatch(i -> i.getIsPrimary() != null && i.getIsPrimary() == 1);
        if (!hasPrimary) {
            log.warn("[模型] 批量保存失败-缺少主键字段: modelId={}", modelId);
            throw new BusinessException(62002, "模型必须包含至少一个主键字段");
        }
        log.debug("[模型] 主键校验通过: modelId={}", modelId);

        // 2. 字段名唯一性校验
        Set<String> names = new HashSet<>();
        for (ModelFieldBatchSaveCmd.FieldItem i : items) {
            if (!names.add(i.getName())) {
                log.warn("[模型] 批量保存失败-字段名重复: modelId={}, fieldName={}", modelId, i.getName());
                throw new BusinessException(62005, "字段名重复: " + i.getName());
            }
        }
        log.debug("[模型] 字段名唯一性校验通过: modelId={}, fieldCount={}", modelId, items.size());

        // 3. 枚举字段必须关联元数据，且元数据属于上层应用
        Microservice ms = microserviceRepository.findByIdAndIsDeleted(
                modelRepository.findByIdAndIsDeleted(modelId, 0)
                        .map(Model::getMicroserviceId).orElseThrow(), 0)
                .orElseThrow(() -> new BusinessException(61004, "微服务不存在"));
        for (ModelFieldBatchSaveCmd.FieldItem i : items) {
            if ("ENUM".equalsIgnoreCase(i.getFieldType())) {
                if (i.getMetadataId() == null) {
                    throw new BusinessException(62006, "枚举字段[" + i.getName() + "]必须关联元数据");
                }
                Metadata meta = metadataRepository.findByIdAndIsDeleted(i.getMetadataId(), 0)
                        .orElseThrow(() -> new BusinessException(63003, "元数据不存在: " + i.getMetadataId()));
                if (!meta.getApplicationId().equals(ms.getApplicationId())) {
                    throw new BusinessException(62007, "字段[" + i.getName() + "]关联的元数据不属于当前应用");
                }
            }
        }

        // 4. 软删除被移除的字段
        if (cmd.getDeletedFieldIds() != null) {
            log.debug("[模型] 软删除字段: modelId={}, deletedFieldIds={}", modelId, cmd.getDeletedFieldIds());
            for (Long fid : cmd.getDeletedFieldIds()) {
                modelFieldRepository.softDelete(fid);
            }
        }

        // 5. upsert 字段
        List<ModelField> saved = new ArrayList<>();
        for (ModelFieldBatchSaveCmd.FieldItem i : items) {
            ModelField field;
            if (i.getId() != null) {
                field = modelFieldRepository.findById(i.getId())
                        .orElseGet(ModelField::new);
                log.debug("[模型] 更新已有字段: fieldId={}, name={}", i.getId(), i.getName());
            } else {
                field = new ModelField();
                field.setModelId(modelId);
                field.setIsDeleted(0);
                log.debug("[模型] 新增字段: name={}, type={}", i.getName(), i.getFieldType());
            }
            field.setName(i.getName());
            field.setDisplayName(i.getDisplayName());
            field.setFieldType(i.getFieldType());
            field.setLength(i.getLength());
            field.setPrecision(i.getPrecision());
            field.setIsRequired(i.getIsRequired() == null ? 0 : i.getIsRequired());
            field.setIsPrimary(i.getIsPrimary() == null ? 0 : i.getIsPrimary());
            field.setIsUnique(i.getIsUnique() == null ? 0 : i.getIsUnique());
            field.setIsIndex(i.getIsIndex() == null ? 0 : i.getIsIndex());
            field.setDefaultValue(i.getDefaultValue());
            field.setMetadataId(i.getMetadataId());
            field.setSortOrder(i.getSortOrder() == null ? 0 : i.getSortOrder());
            field.setFieldComment(i.getFieldComment());
            saved.add(modelFieldRepository.save(field));
        }
        log.info("[模型] 批量保存字段完成: modelId={}, savedCount={}", modelId, saved.size());
        return fillMetadataNames(converter.toFieldVOList(saved));
    }

    private ModelVO toVOWithStats(Model model) {
        ModelVO vo = converter.toVO(model);
        vo.setFieldCount(modelFieldRepository.countByModelIdAndIsDeleted(model.getId(), 0));
        microserviceRepository.findByIdAndIsDeleted(model.getMicroserviceId(), 0)
                .ifPresent(ms -> {
                    vo.setMicroserviceName(ms.getName());
                    vo.setApplicationId(ms.getApplicationId());
                });
        return vo;
    }

    private List<ModelFieldVO> fillMetadataNames(List<ModelFieldVO> fields) {
        Set<Long> metaIds = fields.stream()
                .map(ModelFieldVO::getMetadataId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (metaIds.isEmpty()) {
            return fields;
        }
        Map<Long, String> metaNameMap = metadataRepository.findAllById(metaIds).stream()
                .collect(Collectors.toMap(Metadata::getId, Metadata::getName));
        fields.forEach(f -> {
            if (f.getMetadataId() != null) {
                f.setMetadataName(metaNameMap.get(f.getMetadataId()));
            }
        });
        return fields;
    }

    private Model findOrThrow(Long id) {
        Model model = modelRepository.findByIdAndIsDeleted(id, 0)
                .orElse(null);
        if (model == null) {
            log.warn("[模型] 实体未找到或已删除: id={}, isDeleted=0", id);
            throw new BusinessException(62008, "模型不存在: " + id);
        }
        log.debug("[模型] 实体查询成功: id={}, name={}, code={}, msId={}",
                id, model.getName(), model.getCode(), model.getMicroserviceId());
        return model;
    }
}
