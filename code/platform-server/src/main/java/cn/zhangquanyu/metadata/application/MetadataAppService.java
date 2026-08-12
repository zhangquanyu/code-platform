package cn.zhangquanyu.metadata.application;

import cn.zhangquanyu.application.domain.entity.Application;
import cn.zhangquanyu.application.domain.entity.Microservice;
import cn.zhangquanyu.application.domain.repository.ApplicationRepository;
import cn.zhangquanyu.application.domain.repository.MicroserviceRepository;
import cn.zhangquanyu.metadata.converter.MetadataConverter;
import cn.zhangquanyu.metadata.domain.entity.Metadata;
import cn.zhangquanyu.metadata.domain.entity.MetadataItem;
import cn.zhangquanyu.metadata.domain.repository.MetadataItemRepository;
import cn.zhangquanyu.metadata.domain.repository.MetadataRepository;
import cn.zhangquanyu.metadata.dto.cmd.MetadataCreateCmd;
import cn.zhangquanyu.metadata.dto.cmd.MetadataItemBatchSaveCmd;
import cn.zhangquanyu.metadata.dto.cmd.MetadataUpdateCmd;
import cn.zhangquanyu.shared.api.StatusCmd;
import cn.zhangquanyu.metadata.dto.query.MetadataPageQuery;
import cn.zhangquanyu.metadata.dto.vo.MetadataDetailVO;
import cn.zhangquanyu.metadata.dto.vo.MetadataItemVO;
import cn.zhangquanyu.metadata.dto.vo.MetadataRefVO;
import cn.zhangquanyu.metadata.dto.vo.MetadataSimpleVO;
import cn.zhangquanyu.metadata.dto.vo.MetadataVO;
import cn.zhangquanyu.model.domain.entity.Model;
import cn.zhangquanyu.model.domain.entity.ModelField;
import cn.zhangquanyu.model.domain.repository.ModelFieldRepository;
import cn.zhangquanyu.model.domain.repository.ModelRepository;
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
public class MetadataAppService {

    private final MetadataRepository metadataRepository;
    private final MetadataItemRepository metadataItemRepository;
    private final ApplicationRepository applicationRepository;
    private final ModelFieldRepository modelFieldRepository;
    private final ModelRepository modelRepository;
    private final MicroserviceRepository microserviceRepository;
    private final MetadataConverter converter;

    @Transactional(readOnly = true)
    public PageResult<MetadataVO> page(MetadataPageQuery query) {
        log.info("[元数据] 分页查询开始: pageNum={}, pageSize={}, appId={}, keyword={}, status={}",
                query.getPageNum(), query.getPageSize(), query.getApplicationId(), query.getKeyword(), query.getStatus());
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        PageRequest pageRequest = PageRequest.of(
                Math.max(0, query.getPageNum() - 1), query.getPageSize(), sort);

        Specification<Metadata> spec = (root, q, cb) -> {
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

        Page<Metadata> page = metadataRepository.findAll(spec, pageRequest);
        log.info("[元数据] 分页查询完成: total={}, 当前页记录数={}", page.getTotalElements(), page.getNumberOfElements());
        List<MetadataVO> list = page.getContent().stream().map(this::toVOWithStats).toList();
        return PageResult.of(list, page.getTotalElements(), query.getPageNum(), query.getPageSize());
    }

    @Transactional(readOnly = true)
    public List<MetadataSimpleVO> listByApplication(Long applicationId) {
        log.info("[元数据] 按应用查询启用列表: applicationId={}", applicationId);
        List<Metadata> list = metadataRepository
                .findByApplicationIdAndIsDeletedAndStatusOrderByCreateTimeDesc(applicationId, 0, 1);
        log.info("[元数据] 按应用查询完成: applicationId={}, count={}", applicationId, list.size());
        return list.stream().map(converter::toSimpleVO).toList();
    }

    @Transactional(readOnly = true)
    public MetadataDetailVO getById(Long id) {
        log.info("[元数据] 查询详情开始: id={}", id);
        Metadata meta = findOrThrow(id);
        MetadataVO vo = toVOWithStats(meta);
        List<MetadataItem> items = metadataItemRepository
                .findByMetadataIdAndIsDeletedOrderBySortOrderAsc(id, 0);
        MetadataDetailVO detail = new MetadataDetailVO();
        detail.setMetadata(vo);
        detail.setItems(converter.toItemVOList(items));
        detail.setReferences(listReferences(id));
        log.info("[元数据] 查询详情成功: id={}, name={}, code={}, itemCount={}, refCount={}",
                id, meta.getName(), meta.getCode(), items.size(), detail.getReferences().size());
        return detail;
    }

    @Transactional(readOnly = true)
    public List<MetadataRefVO> listReferences(Long metadataId) {
        log.info("[元数据] 查询引用关系: metadataId={}", metadataId);
        List<ModelField> fields = modelFieldRepository
                .findByMetadataIdAndIsDeleted(metadataId, 0);
        if (fields.isEmpty()) {
            return List.of();
        }
        Set<Long> modelIds = fields.stream().map(ModelField::getModelId).collect(Collectors.toSet());
        Map<Long, Model> modelMap = modelRepository.findAllById(modelIds).stream()
                .collect(Collectors.toMap(Model::getId, m -> m));
        Set<Long> msIds = modelMap.values().stream().map(Model::getMicroserviceId).collect(Collectors.toSet());
        Map<Long, Microservice> msMap = microserviceRepository.findAllById(msIds).stream()
                .collect(Collectors.toMap(Microservice::getId, m -> m));

        return fields.stream().map(f -> {
            MetadataRefVO ref = new MetadataRefVO();
            ref.setFieldId(f.getId());
            ref.setFieldName(f.getName());
            ref.setDisplayName(f.getDisplayName());
            Model model = modelMap.get(f.getModelId());
            if (model != null) {
                ref.setModelId(model.getId());
                ref.setModelName(model.getName());
                Microservice ms = msMap.get(model.getMicroserviceId());
                if (ms != null) {
                    ref.setMicroserviceName(ms.getName());
                }
            }
            return ref;
        }).toList();
    }

    @Transactional
    public MetadataVO create(MetadataCreateCmd cmd) {
        log.info("[元数据] 创建开始: name={}, code={}, applicationId={}", cmd.getName(), cmd.getCode(), cmd.getApplicationId());
        applicationRepository.findByIdAndIsDeleted(cmd.getApplicationId(), 0)
                .orElseThrow(() -> new BusinessException(60003, "应用不存在: " + cmd.getApplicationId()));
        if (metadataRepository.existsByApplicationIdAndCodeAndIsDeleted(
                cmd.getApplicationId(), cmd.getCode(), 0)) {
            log.warn("[元数据] 创建失败-编码已存在: appId={}, code={}", cmd.getApplicationId(), cmd.getCode());
            throw new BusinessException(63001, "元数据编码在该应用内已存在: " + cmd.getCode());
        }
        Metadata entity = converter.toEntity(cmd);
        entity.setStatus(1);
        entity.setIsDeleted(0);
        metadataRepository.save(entity);
        log.info("[元数据] 创建成功: id={}, name={}, code={}", entity.getId(), entity.getName(), entity.getCode());
        return toVOWithStats(entity);
    }

    @Transactional
    public MetadataVO update(Long id, MetadataUpdateCmd cmd) {
        log.info("[元数据] 更新开始: id={}, name={}", id, cmd.getName());
        Metadata meta = findOrThrow(id);
        meta.setName(cmd.getName());
        meta.setDescription(cmd.getDescription());
        metadataRepository.save(meta);
        log.info("[元数据] 更新成功: id={}, name={}", id, meta.getName());
        return toVOWithStats(meta);
    }

    @Transactional
    public void delete(Long id) {
        log.info("[元数据] 删除开始: id={}", id);
        findOrThrow(id);
        long refCount = modelFieldRepository.countByMetadataIdAndIsDeleted(id, 0);
        log.debug("[元数据] 删除校验: id={}, 被模型字段引用次数={}", id, refCount);
        if (refCount > 0) {
            log.warn("[元数据] 删除失败-被模型字段引用: id={}, refCount={}", id, refCount);
            throw new BusinessException(63002, "元数据已被模型字段引用，无法删除");
        }
        metadataRepository.softDelete(id);
        log.info("[元数据] 删除成功(软删除): id={}", id);
    }

    @Transactional
    public void updateStatus(Long id, StatusCmd cmd) {
        log.info("[元数据] 更新状态开始: id={}, status={}", id, cmd.getStatus());
        Metadata meta = findOrThrow(id);
        meta.setStatus(cmd.getStatus());
        metadataRepository.save(meta);
        log.info("[元数据] 更新状态成功: id={}, name={}, status={}", id, meta.getName(), cmd.getStatus());
    }

    @Transactional
    public List<MetadataItemVO> batchSaveItems(Long metadataId, MetadataItemBatchSaveCmd cmd) {
        log.info("[元数据] 批量保存项开始: metadataId={}, itemCount={}, deletedIds={}",
                metadataId, cmd.getItems() == null ? 0 : cmd.getItems().size(), cmd.getDeletedItemIds());
        findOrThrow(metadataId);
        List<MetadataItemBatchSaveCmd.ItemField> items = cmd.getItems();

        // 编码唯一性
        Set<String> codes = new HashSet<>();
        for (MetadataItemBatchSaveCmd.ItemField i : items) {
            if (!codes.add(i.getItemCode())) {
                log.warn("[元数据] 批量保存失败-编码重复: metadataId={}, itemCode={}", metadataId, i.getItemCode());
                throw new BusinessException(63004, "元数据项编码重复: " + i.getItemCode());
            }
        }
        log.debug("[元数据] 编码唯一性校验通过: metadataId={}, itemCount={}", metadataId, items.size());

        // 软删除被移除项
        if (cmd.getDeletedItemIds() != null) {
            log.debug("[元数据] 软删除项: metadataId={}, deletedItemIds={}", metadataId, cmd.getDeletedItemIds());
            for (Long iid : cmd.getDeletedItemIds()) {
                metadataItemRepository.softDelete(iid);
            }
        }

        List<MetadataItem> saved = new ArrayList<>();
        for (MetadataItemBatchSaveCmd.ItemField i : items) {
            MetadataItem item;
            if (i.getId() != null) {
                item = metadataItemRepository.findById(i.getId())
                        .orElseGet(MetadataItem::new);
                log.debug("[元数据] 更新已有项: itemId={}, itemCode={}", i.getId(), i.getItemCode());
            } else {
                item = new MetadataItem();
                item.setMetadataId(metadataId);
                item.setIsDeleted(0);
                log.debug("[元数据] 新增项: itemCode={}, itemName={}", i.getItemCode(), i.getItemName());
            }
            item.setItemCode(i.getItemCode());
            item.setItemName(i.getItemName());
            item.setItemValue(i.getItemValue());
            item.setSortOrder(i.getSortOrder() == null ? 0 : i.getSortOrder());
            item.setStatus(i.getStatus() == null ? 1 : i.getStatus());
            saved.add(metadataItemRepository.save(item));
        }
        log.info("[元数据] 批量保存项完成: metadataId={}, savedCount={}", metadataId, saved.size());
        return converter.toItemVOList(saved);
    }

    private MetadataVO toVOWithStats(Metadata meta) {
        MetadataVO vo = converter.toVO(meta);
        vo.setItemCount(metadataItemRepository.countByMetadataIdAndIsDeleted(meta.getId(), 0));
        if (meta.getApplicationId() != null) {
            applicationRepository.findByIdAndIsDeleted(meta.getApplicationId(), 0)
                    .ifPresent(a -> vo.setApplicationName(a.getName()));
        }
        return vo;
    }

    private Metadata findOrThrow(Long id) {
        Metadata meta = metadataRepository.findByIdAndIsDeleted(id, 0)
                .orElse(null);
        if (meta == null) {
            log.warn("[元数据] 实体未找到或已删除: id={}, isDeleted=0", id);
            throw new BusinessException(63003, "元数据不存在: " + id);
        }
        log.debug("[元数据] 实体查询成功: id={}, name={}, code={}, appId={}, status={}",
                id, meta.getName(), meta.getCode(), meta.getApplicationId(), meta.getStatus());
        return meta;
    }
}
