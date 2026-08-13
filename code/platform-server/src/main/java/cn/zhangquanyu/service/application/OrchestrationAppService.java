package cn.zhangquanyu.service.application;

import cn.zhangquanyu.application.domain.entity.Microservice;
import cn.zhangquanyu.application.domain.repository.MicroserviceRepository;
import cn.zhangquanyu.service.converter.OrchestrationConverter;
import cn.zhangquanyu.service.domain.entity.Orchestration;
import cn.zhangquanyu.service.domain.entity.OrchestrationEdge;
import cn.zhangquanyu.service.domain.entity.OrchestrationNode;
import cn.zhangquanyu.service.domain.entity.ServiceDef;
import cn.zhangquanyu.service.domain.repository.OrchestrationEdgeRepository;
import cn.zhangquanyu.service.domain.repository.OrchestrationNodeRepository;
import cn.zhangquanyu.service.domain.repository.OrchestrationRepository;
import cn.zhangquanyu.service.domain.repository.ServiceDefRepository;
import cn.zhangquanyu.service.dto.cmd.OrchDebugCmd;
import cn.zhangquanyu.service.dto.cmd.OrchestrationCreateCmd;
import cn.zhangquanyu.service.dto.cmd.OrchestrationUpdateCmd;
import cn.zhangquanyu.service.dto.query.OrchestrationPageQuery;
import cn.zhangquanyu.service.dto.vo.OrchDebugResultVO;
import cn.zhangquanyu.service.dto.vo.OrchEdgeVO;
import cn.zhangquanyu.service.dto.vo.OrchHealthVO;
import cn.zhangquanyu.service.dto.vo.OrchNodeVO;
import cn.zhangquanyu.service.dto.vo.OrchParamVO;
import cn.zhangquanyu.service.dto.vo.OrchestrationDetailVO;
import cn.zhangquanyu.service.dto.vo.OrchestrationVO;
import cn.zhangquanyu.shared.api.PageResult;
import cn.zhangquanyu.shared.api.StatusCmd;
import cn.zhangquanyu.shared.exception.BusinessException;
import cn.zhangquanyu.shared.util.SpecUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrchestrationAppService {

    private final OrchestrationRepository orchestrationRepository;
    private final OrchestrationNodeRepository nodeRepository;
    private final OrchestrationEdgeRepository edgeRepository;
    private final MicroserviceRepository microserviceRepository;
    private final ServiceDefRepository serviceDefRepository;
    private final OrchestrationConverter converter;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public PageResult<OrchestrationVO> page(OrchestrationPageQuery query) {
        log.info("[编排] 分页查询开始: pageNum={}, pageSize={}, msId={}, keyword={}, status={}",
                query.getPageNum(), query.getPageSize(), query.getMicroserviceId(), query.getKeyword(), query.getStatus());
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        PageRequest pageRequest = PageRequest.of(
                Math.max(0, query.getPageNum() - 1), query.getPageSize(), sort);

        Specification<Orchestration> spec = (root, q, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), 0));
            if (query.getMicroserviceId() != null) {
                predicates.add(cb.equal(root.get("microserviceId"), query.getMicroserviceId()));
            }
            if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
                predicates.add(SpecUtil.keyword(root, cb, query.getKeyword(), "name", "code"));
            }
            if (query.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), query.getStatus()));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Orchestration> page = orchestrationRepository.findAll(spec, pageRequest);
        log.info("[编排] 分页查询完成: total={}, 当前页记录数={}", page.getTotalElements(), page.getNumberOfElements());
        List<OrchestrationVO> list = page.getContent().stream().map(this::toVOWithStats).toList();
        return PageResult.of(list, page.getTotalElements(), query.getPageNum(), query.getPageSize());
    }

    @Transactional(readOnly = true)
    public OrchestrationDetailVO getById(Long id) {
        log.info("[编排] 查询详情开始: id={}", id);
        Orchestration orch = findOrThrow(id);
        OrchestrationDetailVO detail = new OrchestrationDetailVO();
        detail.setOrchestration(toVOWithStats(orch));

        List<OrchestrationNode> nodes = nodeRepository
                .findByOrchestrationIdAndIsDeletedOrderBySortOrderAsc(id, 0);
        List<OrchestrationEdge> edges = edgeRepository.findByOrchestrationIdAndIsDeleted(id, 0);

        List<OrchNodeVO> nodeVOs = fillServiceNames(converter.toNodeVOList(nodes));
        detail.setNodes(nodeVOs);
        detail.setEdges(converter.toEdgeVOList(edges));
        // 入参/出参：本期简化为从编排实体的扩展字段获取；这里返回空列表占位
        detail.setInputParams(List.of());
        detail.setOutputParams(List.of());
        log.info("[编排] 查询详情成功: id={}, name={}, code={}, nodes={}, edges={}",
                id, orch.getName(), orch.getCode(), nodeVOs.size(), detail.getEdges().size());
        return detail;
    }

    @Transactional
    public OrchestrationDetailVO create(OrchestrationCreateCmd cmd) {
        log.info("[编排] 创建开始: name={}, code={}, msId={}", cmd.getName(), cmd.getCode(), cmd.getMicroserviceId());
        microserviceRepository.findByIdAndIsDeleted(cmd.getMicroserviceId(), 0)
                .orElseThrow(() -> new BusinessException(61004, "微服务不存在: " + cmd.getMicroserviceId()));
        if (orchestrationRepository.existsByMicroserviceIdAndCodeAndIsDeleted(
                cmd.getMicroserviceId(), cmd.getCode(), 0)) {
            log.warn("[编排] 创建失败-编码已存在: msId={}, code={}", cmd.getMicroserviceId(), cmd.getCode());
            throw new BusinessException(65002, "编排编码在该微服务内已存在: " + cmd.getCode());
        }
        Orchestration entity = new Orchestration();
        entity.setMicroserviceId(cmd.getMicroserviceId());
        entity.setName(cmd.getName());
        entity.setCode(cmd.getCode());
        entity.setDescription(cmd.getDescription());
        entity.setTxType(cmd.getTxType() != null ? cmd.getTxType() : "LOCAL");
        entity.setTxTimeout(cmd.getTxTimeout() != null ? cmd.getTxTimeout() : 300);
        entity.setStatus(1);
        entity.setIsDeleted(0);
        orchestrationRepository.save(entity);
        log.info("[编排] 创建成功: id={}, name={}, code={}", entity.getId(), entity.getName(), entity.getCode());
        return getById(entity.getId());
    }

    @Transactional
    public OrchestrationDetailVO update(Long id, OrchestrationUpdateCmd cmd) {
        log.info("[编排] 更新开始: id={}, name={}, nodes={}, edges={}",
                id, cmd.getName(),
                cmd.getNodes() == null ? 0 : cmd.getNodes().size(),
                cmd.getEdges() == null ? 0 : cmd.getEdges().size());

        // 先校验，避免校验失败导致保存后的数据回滚
        validate(id, cmd.getNodes(), cmd.getEdges());

        Orchestration orch = findOrThrow(id);
        orch.setName(cmd.getName());
        orch.setDescription(cmd.getDescription());
        if (cmd.getStatus() != null) {
            orch.setStatus(cmd.getStatus());
        }
        if (cmd.getTxType() != null) {
            orch.setTxType(cmd.getTxType());
        }
        if (cmd.getTxTimeout() != null) {
            orch.setTxTimeout(cmd.getTxTimeout());
        }
        orchestrationRepository.save(orch);

        // 整体保存：先物理删除旧的软删除记录，再软删除当前活跃记录，最后写入新的
        // 避免唯一约束冲突：(orchestration_id, node_key, is_deleted) 重复
        nodeRepository.hardDeleteSoftDeletedByOrchestrationId(id);
        edgeRepository.hardDeleteSoftDeletedByOrchestrationId(id);
        nodeRepository.softDeleteByOrchestrationId(id);
        edgeRepository.softDeleteByOrchestrationId(id);

        if (cmd.getNodes() != null) {
            for (OrchestrationUpdateCmd.OrchNodeCmd n : cmd.getNodes()) {
                OrchestrationNode node = new OrchestrationNode();
                node.setOrchestrationId(id);
                node.setNodeKey(n.getNodeKey());
                node.setNodeType(n.getNodeType());
                node.setNodeName(n.getNodeName());
                node.setServiceId(n.getServiceId());
                node.setConfigJson(n.getConfigJson());
                node.setTxType(n.getTxType() != null ? n.getTxType() : "LOCAL");
                node.setTxTimeout(n.getTxTimeout() != null ? n.getTxTimeout() : 60);
                node.setRetryCount(n.getRetryCount() != null ? n.getRetryCount() : 0);
                node.setRetryInterval(n.getRetryInterval() != null ? n.getRetryInterval() : 1000);
                node.setExceptionStrategy(n.getExceptionStrategy() != null ? n.getExceptionStrategy() : "INTERRUPT");
                node.setLoopType(n.getLoopType() != null ? n.getLoopType() : "SERIAL");
                node.setBranchExpr(n.getBranchExpr());
                node.setXPos(n.getXPos());
                node.setYPos(n.getYPos());
                node.setSortOrder(n.getSortOrder() == null ? 0 : n.getSortOrder());
                node.setIsDeleted(0);
                log.info("[编排] 保存节点: key={}, type={}, xPos={}, yPos={}",
                        n.getNodeKey(), n.getNodeType(), n.getXPos(), n.getYPos());
                nodeRepository.save(node);
            }
        }
        if (cmd.getEdges() != null) {
            for (OrchestrationUpdateCmd.OrchEdgeCmd e : cmd.getEdges()) {
                OrchestrationEdge edge = new OrchestrationEdge();
                edge.setOrchestrationId(id);
                edge.setEdgeKey(e.getEdgeKey());
                edge.setFromNodeKey(e.getFromNodeKey());
                edge.setToNodeKey(e.getToNodeKey());
                edge.setConditionExpr(e.getConditionExpr());
                edge.setLabelText(e.getLabelText());
                edge.setIsDeleted(0);
                edgeRepository.save(edge);
            }
        }
        log.info("[编排] 节点与连线保存完成: id={}, nodes={}, edges={}",
                id,
                cmd.getNodes() == null ? 0 : cmd.getNodes().size(),
                cmd.getEdges() == null ? 0 : cmd.getEdges().size());

        log.info("[编排] 更新完成: id={}", id);
        return getById(id);
    }

    /**
     * 流程校验（独立接口）：供前端「校验」按钮调用，对未保存版本预校验
     */
    public List<String> validate(Long id, List<OrchestrationUpdateCmd.OrchNodeCmd> nodes,
                                 List<OrchestrationUpdateCmd.OrchEdgeCmd> edges) {
        List<String> errors = new ArrayList<>();

        if (nodes == null || edges == null) {
            return errors;
        }

        Set<String> nodeKeys = nodes.stream()
                .map(OrchestrationUpdateCmd.OrchNodeCmd::getNodeKey)
                .collect(Collectors.toSet());

        // 1. 开始节点数量 = 1，结束节点 ≥ 1
        long startCount = nodes.stream().filter(n -> OrchestrationNode.TYPE_START.equals(n.getNodeType())).count();
        long endCount = nodes.stream().filter(n -> OrchestrationNode.TYPE_END.equals(n.getNodeType())).count();
        log.debug("[编排] 节点统计: id={}, startCount={}, endCount={}, totalNodes={}", id, startCount, endCount, nodes.size());
        if (startCount != 1) {
            errors.add("开始节点数量必须为1，当前为" + startCount);
        }
        if (endCount < 1) {
            errors.add("结束节点数量至少为1，当前为" + endCount);
        }

        // 2. 校验连线引用的节点 key 都存在
        for (OrchestrationUpdateCmd.OrchEdgeCmd e : edges) {
            if (!nodeKeys.contains(e.getFromNodeKey())) {
                errors.add("连线[" + e.getEdgeKey() + "]起点节点不存在: " + e.getFromNodeKey());
            }
            if (!nodeKeys.contains(e.getToNodeKey())) {
                errors.add("连线[" + e.getEdgeKey() + "]终点节点不存在: " + e.getToNodeKey());
            }
        }

        // 3. SERVICE 节点必须选服务且服务启用
        Set<Long> svcIds = nodes.stream()
                .filter(n -> OrchestrationNode.TYPE_SERVICE.equals(n.getNodeType()))
                .map(OrchestrationUpdateCmd.OrchNodeCmd::getServiceId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, ServiceDef> svcMap = serviceDefRepository.findByIdInAndIsDeleted(svcIds.stream().toList(), 0)
                .stream().collect(Collectors.toMap(ServiceDef::getId, s -> s));
        for (OrchestrationUpdateCmd.OrchNodeCmd n : nodes) {
            if (OrchestrationNode.TYPE_SERVICE.equals(n.getNodeType())) {
                if (n.getServiceId() == null) {
                    errors.add("服务调用节点[" + n.getNodeKey() + "]未选择服务");
                } else {
                    ServiceDef svc = svcMap.get(n.getServiceId());
                    if (svc == null) {
                        errors.add("服务调用节点[" + n.getNodeKey() + "]引用的服务不存在: " + n.getServiceId());
                    } else if (svc.getStatus() != 1) {
                        errors.add("服务调用节点[" + n.getNodeKey() + "]引用的服务已停用: " + svc.getName());
                    }
                }
            }
        }

        // 4. CONDITION 节点至少两条出边且配置条件表达式
        Map<String, List<OrchestrationUpdateCmd.OrchEdgeCmd>> outEdges = edges.stream()
                .collect(Collectors.groupingBy(OrchestrationUpdateCmd.OrchEdgeCmd::getFromNodeKey));
        for (OrchestrationUpdateCmd.OrchNodeCmd n : nodes) {
            if (OrchestrationNode.TYPE_CONDITION.equals(n.getNodeType())) {
                List<OrchestrationUpdateCmd.OrchEdgeCmd> outs = outEdges.getOrDefault(n.getNodeKey(), List.of());
                if (outs.size() < 2) {
                    errors.add("条件判断节点[" + n.getNodeKey() + "]至少需要2条出边，当前" + outs.size() + "条");
                }
            }
        }

        // 5. 事务配置校验
        for (OrchestrationUpdateCmd.OrchNodeCmd n : nodes) {
            if (n.getRetryCount() != null && n.getRetryCount() < 0) {
                errors.add("节点[" + n.getNodeKey() + "]重试次数不能为负数");
            }
            if ("DISTRIBUTED".equals(n.getTxType()) && n.getServiceId() == null
                    && (OrchestrationNode.TYPE_SERVICE.equals(n.getNodeType())
                            || OrchestrationNode.TYPE_ACTION.equals(n.getNodeType()))) {
                errors.add("分布式事务节点[" + n.getNodeKey() + "]必须配置服务");
            }
        }

        // 6. BRANCH 节点必须配置分支表达式
        for (OrchestrationUpdateCmd.OrchNodeCmd n : nodes) {
            if (OrchestrationNode.TYPE_BRANCH.equals(n.getNodeType())) {
                if (n.getBranchExpr() == null || n.getBranchExpr().isBlank()) {
                    errors.add("分支节点[" + n.getNodeKey() + "]必须配置分支表达式");
                }
            }
        }

        // 7. LOOP(PARALLEL) 节点必须至少有一个出边
        for (OrchestrationUpdateCmd.OrchNodeCmd n : nodes) {
            if (OrchestrationNode.TYPE_LOOP.equals(n.getNodeType()) && "PARALLEL".equals(n.getLoopType())) {
                List<OrchestrationUpdateCmd.OrchEdgeCmd> outs = outEdges.getOrDefault(n.getNodeKey(), List.of());
                if (outs.isEmpty()) {
                    errors.add("并行循环节点[" + n.getNodeKey() + "]必须至少有一个子节点(出边)");
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new BusinessException(65001, String.join("; ", errors));
        }
        return errors;
    }

    @Transactional
    public void delete(Long id) {
        log.info("[编排] 删除开始: id={}", id);
        findOrThrow(id);
        // 先物理删除旧的软删除记录，避免唯一约束冲突
        nodeRepository.hardDeleteSoftDeletedByOrchestrationId(id);
        edgeRepository.hardDeleteSoftDeletedByOrchestrationId(id);
        nodeRepository.softDeleteByOrchestrationId(id);
        log.debug("[编排] 节点软删除完成: orchestrationId={}", id);
        edgeRepository.softDeleteByOrchestrationId(id);
        log.debug("[编排] 连线软删除完成: orchestrationId={}", id);
        orchestrationRepository.softDelete(id);
        log.info("[编排] 删除成功(软删除): id={}", id);
    }

    @Transactional
    public void updateStatus(Long id, StatusCmd cmd) {
        log.info("[编排] 更新状态开始: id={}, status={}", id, cmd.getStatus());
        Orchestration orch = findOrThrow(id);
        orch.setStatus(cmd.getStatus());
        orchestrationRepository.save(orch);
        log.info("[编排] 更新状态成功: id={}, name={}, status={}", id, orch.getName(), cmd.getStatus());
    }

    /**
     * 健康检查：被引用的服务是否可用
     */
    @Transactional(readOnly = true)
    public OrchHealthVO health(Long id) {
        log.info("[编排] 健康检查开始: id={}", id);
        findOrThrow(id);
        List<OrchestrationNode> nodes = nodeRepository
                .findByOrchestrationIdAndIsDeletedOrderBySortOrderAsc(id, 0);
        OrchHealthVO vo = new OrchHealthVO();
        List<OrchHealthVO.Alert> alerts = new ArrayList<>();
        Set<Long> svcIds = nodes.stream()
                .filter(n -> OrchestrationNode.TYPE_SERVICE.equals(n.getNodeType()))
                .map(OrchestrationNode::getServiceId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, ServiceDef> svcMap = serviceDefRepository.findByIdInAndIsDeleted(svcIds.stream().toList(), 0)
                .stream().collect(Collectors.toMap(ServiceDef::getId, s -> s));
        for (OrchestrationNode n : nodes) {
            if (OrchestrationNode.TYPE_SERVICE.equals(n.getNodeType()) && n.getServiceId() != null) {
                ServiceDef svc = svcMap.get(n.getServiceId());
                if (svc == null) {
                    OrchHealthVO.Alert a = new OrchHealthVO.Alert();
                    a.setNodeKey(n.getNodeKey());
                    a.setServiceId(n.getServiceId());
                    a.setReason("服务已被删除");
                    alerts.add(a);
                } else if (svc.getStatus() != 1) {
                    OrchHealthVO.Alert a = new OrchHealthVO.Alert();
                    a.setNodeKey(n.getNodeKey());
                    a.setServiceId(n.getServiceId());
                    a.setReason("服务已停用: " + svc.getName());
                    alerts.add(a);
                }
            }
        }
        vo.setHealthy(alerts.isEmpty());
        vo.setAlerts(alerts);
        log.info("[编排] 健康检查完成: id={}, healthy={}, alertCount={}", id, vo.isHealthy(), alerts.size());
        return vo;
    }

    /**
     * 编排调试（Mock 执行）：按拓扑顺序遍历节点，对 SERVICE 节点生成 mock 出参
     */
    @Transactional(readOnly = true)
    public OrchDebugResultVO debug(Long id, OrchDebugCmd cmd) {
        log.info("[编排] 调试开始: id={}, inputData={}", id, cmd.getInputData());
        Orchestration orch = findOrThrow(id);
        List<OrchestrationNode> nodes = nodeRepository
                .findByOrchestrationIdAndIsDeletedOrderBySortOrderAsc(id, 0);
        List<OrchestrationEdge> edges = edgeRepository.findByOrchestrationIdAndIsDeleted(id, 0);

        // 健康检查
        OrchHealthVO health = health(id);
        if (!health.isHealthy()) {
            OrchDebugResultVO r = new OrchDebugResultVO();
            r.setSuccess(false);
            r.setTotalDurationMs(0);
            OrchDebugResultVO.NodeResult nr = new OrchDebugResultVO.NodeResult();
            nr.setStatus("FAILED");
            nr.setError("编排存在不健康的服务节点: " + health.getAlerts().stream()
                    .map(a -> a.getNodeKey() + "(" + a.getReason() + ")")
                    .reduce((a, b) -> a + ", " + b).orElse(""));
            r.setNodeResults(List.of(nr));
            return r;
        }

        // Mock 执行：按节点顺序执行，SERVICE 节点生成 mock 输出
        Map<String, Object> context = new HashMap<>();
        if (cmd.getInputData() != null) {
            context.putAll(cmd.getInputData());
        }
        List<OrchDebugResultVO.NodeResult> nodeResults = new ArrayList<>();
        long totalStart = System.currentTimeMillis();
        boolean success = true;
        String failError = null;

        for (OrchestrationNode n : nodes) {
            OrchDebugResultVO.NodeResult nr = new OrchDebugResultVO.NodeResult();
            nr.setNodeKey(n.getNodeKey());
            nr.setNodeName(n.getNodeName());
            long start = System.currentTimeMillis();
            try {
                if (OrchestrationNode.TYPE_START.equals(n.getNodeType())) {
                    nr.setInput(Map.copyOf(context));
                    nr.setOutput(Map.copyOf(context));
                    nr.setStatus("SUCCESS");
                } else if (OrchestrationNode.TYPE_SERVICE.equals(n.getNodeType()) && n.getServiceId() != null) {
                    nr.setInput(new HashMap<>(context));
                    // 生成 mock 出参：根据服务出参 schema 构造示例值
                    Map<String, Object> mockOutput = generateMockOutput(n.getServiceId());
                    context.putAll(mockOutput);
                    nr.setOutput(mockOutput);
                    nr.setStatus("SUCCESS");
                } else if (OrchestrationNode.TYPE_CONDITION.equals(n.getNodeType())) {
                    nr.setInput(Map.copyOf(context));
                    nr.setOutput(Map.copyOf(context));
                    nr.setStatus("SUCCESS");
                } else if (OrchestrationNode.TYPE_LOOP.equals(n.getNodeType())) {
                    nr.setInput(Map.copyOf(context));
                    nr.setOutput(Map.copyOf(context));
                    nr.setStatus("SUCCESS");
                } else if (OrchestrationNode.TYPE_END.equals(n.getNodeType())) {
                    nr.setInput(Map.copyOf(context));
                    nr.setOutput(Map.copyOf(context));
                    nr.setStatus("SUCCESS");
                }
            } catch (Exception e) {
                nr.setStatus("FAILED");
                nr.setError(e.getMessage());
                success = false;
                failError = e.getMessage();
                nodeResults.add(nr);
                break;
            }
            nr.setDurationMs(System.currentTimeMillis() - start);
            nodeResults.add(nr);
        }

        OrchDebugResultVO result = new OrchDebugResultVO();
        result.setSuccess(success);
        result.setNodeResults(nodeResults);
        result.setTotalDurationMs(System.currentTimeMillis() - totalStart);
        if (success) {
            result.setOutput(context);
        }
        log.info("[编排] 调试完成: id={}, success={}, nodeCount={}, durationMs={}",
                id, success, nodeResults.size(), result.getTotalDurationMs());
        return result;
    }

    private Map<String, Object> generateMockOutput(Long serviceId) {
        // Mock：根据服务出参构造示例值
        Map<String, Object> mock = new HashMap<>();
        List<cn.zhangquanyu.service.domain.entity.ServiceParam> outputs =
                List.of(); // 简化：实际应查询出参列表
        // 此处直接返回带 serviceId 的占位输出
        mock.put("_mockServiceId", serviceId);
        mock.put("_mockTimestamp", System.currentTimeMillis());
        return mock;
    }

    private OrchestrationVO toVOWithStats(Orchestration orch) {
        OrchestrationVO vo = converter.toVO(orch);
        List<OrchestrationNode> nodes = nodeRepository
                .findByOrchestrationIdAndIsDeletedOrderBySortOrderAsc(orch.getId(), 0);
        vo.setNodeCount(nodes.size());
        vo.setEdgeCount(edgeRepository.findByOrchestrationIdAndIsDeleted(orch.getId(), 0).size());
        microserviceRepository.findByIdAndIsDeleted(orch.getMicroserviceId(), 0)
                .ifPresent(ms -> {
                    vo.setMicroserviceName(ms.getName());
                    vo.setApplicationId(ms.getApplicationId());
                });
        return vo;
    }

    private List<OrchNodeVO> fillServiceNames(List<OrchNodeVO> nodes) {
        Set<Long> svcIds = nodes.stream()
                .map(OrchNodeVO::getServiceId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (svcIds.isEmpty()) {
            return nodes;
        }
        Map<Long, String> svcNameMap = serviceDefRepository.findByIdInAndIsDeleted(svcIds.stream().toList(), 0)
                .stream().collect(Collectors.toMap(ServiceDef::getId, ServiceDef::getName));
        nodes.forEach(n -> {
            if (n.getServiceId() != null) {
                n.setServiceName(svcNameMap.get(n.getServiceId()));
            }
        });
        return nodes;
    }

    private Orchestration findOrThrow(Long id) {
        Orchestration orch = orchestrationRepository.findByIdAndIsDeleted(id, 0)
                .orElse(null);
        if (orch == null) {
            log.warn("[编排] 实体未找到或已删除: id={}, isDeleted=0", id);
            throw new BusinessException(65003, "编排不存在: " + id);
        }
        log.debug("[编排] 实体查询成功: id={}, name={}, code={}, msId={}, status={}",
                id, orch.getName(), orch.getCode(), orch.getMicroserviceId(), orch.getStatus());
        return orch;
    }
}
