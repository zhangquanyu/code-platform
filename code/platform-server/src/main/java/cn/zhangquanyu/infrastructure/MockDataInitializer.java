package cn.zhangquanyu.infrastructure;

import cn.zhangquanyu.application.domain.entity.Application;
import cn.zhangquanyu.application.domain.entity.Microservice;
import cn.zhangquanyu.application.domain.repository.ApplicationRepository;
import cn.zhangquanyu.application.domain.repository.MicroserviceRepository;
import cn.zhangquanyu.metadata.domain.entity.Metadata;
import cn.zhangquanyu.metadata.domain.entity.MetadataItem;
import cn.zhangquanyu.metadata.domain.repository.MetadataItemRepository;
import cn.zhangquanyu.metadata.domain.repository.MetadataRepository;
import cn.zhangquanyu.model.domain.entity.Model;
import cn.zhangquanyu.model.domain.entity.ModelField;
import cn.zhangquanyu.model.domain.repository.ModelFieldRepository;
import cn.zhangquanyu.model.domain.repository.ModelRepository;
import cn.zhangquanyu.service.domain.entity.Orchestration;
import cn.zhangquanyu.service.domain.entity.OrchestrationEdge;
import cn.zhangquanyu.service.domain.entity.OrchestrationNode;
import cn.zhangquanyu.service.domain.entity.ServiceDef;
import cn.zhangquanyu.service.domain.entity.ServiceParam;
import cn.zhangquanyu.service.domain.repository.OrchestrationEdgeRepository;
import cn.zhangquanyu.service.domain.repository.OrchestrationNodeRepository;
import cn.zhangquanyu.service.domain.repository.OrchestrationRepository;
import cn.zhangquanyu.service.domain.repository.ServiceDefRepository;
import cn.zhangquanyu.service.domain.repository.ServiceParamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock 数据初始化器：仅在 h2 profile 下启动时执行，为全部 6 大模块种子化测试数据。
 * 启动方式：mvn spring-boot:run -Dspring-boot.run.profiles=h2
 */
@Slf4j
@Component
@Profile("h2")
@Order(1)
@RequiredArgsConstructor
public class MockDataInitializer implements CommandLineRunner {

    private final ApplicationRepository applicationRepository;
    private final MicroserviceRepository microserviceRepository;
    private final ModelRepository modelRepository;
    private final ModelFieldRepository modelFieldRepository;
    private final MetadataRepository metadataRepository;
    private final MetadataItemRepository metadataItemRepository;
    private final ServiceDefRepository serviceDefRepository;
    private final ServiceParamRepository serviceParamRepository;
    private final OrchestrationRepository orchestrationRepository;
    private final OrchestrationNodeRepository orchestrationNodeRepository;
    private final OrchestrationEdgeRepository orchestrationEdgeRepository;

    /** 保存应用 ID -> 应用名映射，方便后续引用 */
    private final Map<String, Long> appIds = new HashMap<>();
    /** 保存微服务 ID -> 微服务名映射 */
    private final Map<String, Long> msIds = new HashMap<>();
    /** 保存模型 ID -> 模型名映射 */
    private final Map<String, Long> modelIds = new HashMap<>();
    /** 保存元数据 ID -> 元数据名映射 */
    private final Map<String, Long> metaIds = new HashMap<>();
    /** 保存服务 ID -> 服务名映射 */
    private final Map<String, Long> svcIds = new HashMap<>();

    @Override
    @Transactional
    public void run(String... args) {
        log.info("========== 开始初始化 Mock 数据 ==========");

        seedApplications();
        seedMicroservices();
        seedMetadata();
        seedModels();
        seedServices();
        seedOrchestrations();

        log.info("========== Mock 数据初始化完成 ==========");
        log.info("应用数={}, 微服务数={}, 模型数={}, 元数据数={}, 服务数={}, 编排数={}",
                appIds.size(), msIds.size(), modelIds.size(),
                metaIds.size(), svcIds.size(), 1);
    }

    private void seedApplications() {
        log.info("[Mock] 种子化应用数据...");
        appIds.put("order", createApp("订单中心", "ORDER_CENTER", "统一订单管理平台", "1.0.0", 1));
        appIds.put("user", createApp("用户中心", "USER_CENTER", "用户账号与权限管理", "1.2.0", 1));
        appIds.put("product", createApp("商品中心", "PRODUCT_CENTER", "商品与库存管理", "0.9.0", 0));
        log.info("[Mock] 应用数据种子化完成: {}", appIds);
    }

    private Long createApp(String name, String code, String desc, String version, int status) {
        Application app = new Application();
        app.setName(name);
        app.setCode(code);
        app.setDescription(desc);
        app.setVersion(version);
        app.setStatus(status);
        app.setIsDeleted(0);
        return applicationRepository.save(app).getId();
    }

    private void seedMicroservices() {
        log.info("[Mock] 种子化微服务数据...");
        msIds.put("order-svc", createMs(appIds.get("order"), "订单服务", "ORDER_SERVICE", "订单核心微服务", "1.0.0", 1));
        msIds.put("pay-svc", createMs(appIds.get("order"), "支付服务", "PAY_SERVICE", "支付通道与对账", "1.0.0", 1));
        msIds.put("user-svc", createMs(appIds.get("user"), "用户服务", "USER_SERVICE", "用户注册/登录/资料", "1.2.0", 1));
        msIds.put("product-svc", createMs(appIds.get("product"), "商品服务", "PRODUCT_SERVICE", "商品上下架与查询", "0.9.0", 1));
        log.info("[Mock] 微服务数据种子化完成: {}", msIds);
    }

    private Long createMs(Long appId, String name, String code, String desc, String version, int status) {
        Microservice ms = new Microservice();
        ms.setApplicationId(appId);
        ms.setName(name);
        ms.setCode(code);
        ms.setDescription(desc);
        ms.setVersion(version);
        ms.setStatus(status);
        ms.setIsDeleted(0);
        return microserviceRepository.save(ms).getId();
    }

    private void seedMetadata() {
        log.info("[Mock] 种子化元数据数据...");
        // 订单状态
        Long orderStatusId = createMeta(appIds.get("order"), "订单状态", "ORDER_STATUS", "订单生命周期状态枚举");
        metaIds.put("order-status", orderStatusId);
        createItem(orderStatusId, "PENDING", "待支付", "0", 1, 1);
        createItem(orderStatusId, "PAID", "已支付", "1", 2, 1);
        createItem(orderStatusId, "SHIPPED", "已发货", "2", 3, 1);
        createItem(orderStatusId, "COMPLETED", "已完成", "3", 4, 1);
        createItem(orderStatusId, "CANCELLED", "已取消", "-1", 5, 1);

        // 用户类型
        Long userTypeId = createMeta(appIds.get("user"), "用户类型", "USER_TYPE", "用户分类枚举");
        metaIds.put("user-type", userTypeId);
        createItem(userTypeId, "NORMAL", "普通用户", "0", 1, 1);
        createItem(userTypeId, "VIP", "VIP用户", "1", 2, 1);
        createItem(userTypeId, "ADMIN", "管理员", "9", 3, 1);

        log.info("[Mock] 元数据数据种子化完成: {}", metaIds);
    }

    private Long createMeta(Long appId, String name, String code, String desc) {
        Metadata meta = new Metadata();
        meta.setApplicationId(appId);
        meta.setName(name);
        meta.setCode(code);
        meta.setDescription(desc);
        meta.setStatus(1);
        meta.setIsDeleted(0);
        return metadataRepository.save(meta).getId();
    }

    private void createItem(Long metaId, String code, String name, String value, int sort, int status) {
        MetadataItem item = new MetadataItem();
        item.setMetadataId(metaId);
        item.setItemCode(code);
        item.setItemName(name);
        item.setItemValue(value);
        item.setSortOrder(sort);
        item.setStatus(status);
        item.setIsDeleted(0);
        metadataItemRepository.save(item);
    }

    private void seedModels() {
        log.info("[Mock] 种子化模型数据...");
        // 订单模型
        Long orderModelId = createModel(msIds.get("order-svc"), "订单", "Order", "订单主模型");
        modelIds.put("order", orderModelId);
        createField(orderModelId, "id", "主键ID", "BIGINT", null, null, 1, 1, 1, 1, null, null, 1, "主键");
        createField(orderModelId, "orderNo", "订单号", "VARCHAR", 64, null, 1, 0, 1, 1, null, null, 2, "唯一订单编号");
        createField(orderModelId, "userId", "用户ID", "BIGINT", null, null, 1, 0, 0, 1, null, null, 3, "下单用户");
        createField(orderModelId, "amount", "订单金额", "DECIMAL", 12, 2, 1, 0, 0, 0, null, null, 4, "总金额");
        createField(orderModelId, "status", "订单状态", "ENUM", null, null, 1, 0, 0, 0, null, metaIds.get("order-status"), 5, "关联订单状态元数据");
        createField(orderModelId, "createTime", "创建时间", "DATETIME", null, null, 0, 0, 0, 0, null, null, 6, "下单时间");

        // 用户模型
        Long userModelId = createModel(msIds.get("user-svc"), "用户", "User", "用户主模型");
        modelIds.put("user", userModelId);
        createField(userModelId, "id", "主键ID", "BIGINT", null, null, 1, 1, 1, 1, null, null, 1, "主键");
        createField(userModelId, "username", "用户名", "VARCHAR", 64, null, 1, 0, 1, 1, null, null, 2, "登录用户名");
        createField(userModelId, "phone", "手机号", "VARCHAR", 20, null, 0, 0, 0, 1, null, null, 3, "联系电话");
        createField(userModelId, "userType", "用户类型", "ENUM", null, null, 0, 0, 0, 0, null, metaIds.get("user-type"), 4, "关联用户类型元数据");
        createField(userModelId, "createTime", "注册时间", "DATETIME", null, null, 0, 0, 0, 0, null, null, 5, "注册时间");

        log.info("[Mock] 模型数据种子化完成: {}", modelIds);
    }

    private Long createModel(Long msId, String name, String code, String desc) {
        Model model = new Model();
        model.setMicroserviceId(msId);
        model.setName(name);
        model.setCode(code);
        model.setDescription(desc);
        model.setIsDeleted(0);
        return modelRepository.save(model).getId();
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private void createField(Long modelId, String name, String displayName, String fieldType,
                             Integer length, Integer precision, int isRequired, int isPrimary,
                             int isUnique, int isIndex, String defaultValue, Long metadataId,
                             int sortOrder, String comment) {
        ModelField field = new ModelField();
        field.setModelId(modelId);
        field.setName(name);
        field.setDisplayName(displayName);
        field.setFieldType(fieldType);
        field.setLength(length);
        field.setPrecision(precision);
        field.setIsRequired(isRequired);
        field.setIsPrimary(isPrimary);
        field.setIsUnique(isUnique);
        field.setIsIndex(isIndex);
        field.setDefaultValue(defaultValue);
        field.setMetadataId(metadataId);
        field.setSortOrder(sortOrder);
        field.setFieldComment(comment);
        field.setIsDeleted(0);
        modelFieldRepository.save(field);
    }

    private void seedServices() {
        log.info("[Mock] 种子化服务数据...");
        // 创建订单服务
        Long createOrderSvcId = createService(msIds.get("order-svc"), "创建订单", "CREATE_ORDER",
                "根据用户ID和商品信息创建订单", "POST", "/api/order/create", "ORDER", 1);
        svcIds.put("create-order", createOrderSvcId);
        createParam(createOrderSvcId, 1, "userId", "LONG", 1, null, null, 1, "用户ID");
        createParam(createOrderSvcId, 1, "productId", "LONG", 1, null, null, 2, "商品ID");
        createParam(createOrderSvcId, 1, "quantity", "INT", 1, "1", null, 3, "购买数量");
        createParam(createOrderSvcId, 2, "orderId", "LONG", 1, null, null, 1, "订单ID");
        createParam(createOrderSvcId, 2, "orderNo", "STRING", 1, null, null, 2, "订单编号");
        createParam(createOrderSvcId, 2, "amount", "DECIMAL", 1, null, null, 3, "订单金额");

        // 查询订单服务
        Long queryOrderSvcId = createService(msIds.get("order-svc"), "查询订单", "QUERY_ORDER",
                "根据订单ID查询订单详情", "GET", "/api/order/query", "ORDER", 1);
        svcIds.put("query-order", queryOrderSvcId);
        createParam(queryOrderSvcId, 1, "orderId", "LONG", 1, null, null, 1, "订单ID");
        createParam(queryOrderSvcId, 2, "orderNo", "STRING", 0, null, null, 1, "订单编号");
        createParam(queryOrderSvcId, 2, "userId", "LONG", 0, null, null, 2, "用户ID");
        createParam(queryOrderSvcId, 2, "amount", "DECIMAL", 0, null, null, 3, "订单金额");
        createParam(queryOrderSvcId, 2, "status", "STRING", 0, null, null, 4, "订单状态");

        // 查询用户服务
        Long queryUserSvcId = createService(msIds.get("user-svc"), "查询用户", "QUERY_USER",
                "根据用户ID查询用户信息", "GET", "/api/user/query", "USER", 1);
        svcIds.put("query-user", queryUserSvcId);
        createParam(queryUserSvcId, 1, "userId", "LONG", 1, null, null, 1, "用户ID");
        createParam(queryUserSvcId, 2, "username", "STRING", 0, null, null, 1, "用户名");
        createParam(queryUserSvcId, 2, "phone", "STRING", 0, null, null, 2, "手机号");
        createParam(queryUserSvcId, 2, "userType", "STRING", 0, null, null, 3, "用户类型");

        log.info("[Mock] 服务数据种子化完成: {}", svcIds);
    }

    private Long createService(Long msId, String name, String code, String desc,
                               String httpMethod, String path, String category, int status) {
        ServiceDef svc = new ServiceDef();
        svc.setMicroserviceId(msId);
        svc.setName(name);
        svc.setCode(code);
        svc.setDescription(desc);
        svc.setHttpMethod(httpMethod);
        svc.setServicePath(path);
        svc.setCategory(category);
        svc.setStatus(status);
        svc.setIsDeleted(0);
        return serviceDefRepository.save(svc).getId();
    }

    private void createParam(Long svcId, int paramType, String name, String dataType,
                             int isRequired, String defaultValue, Long modelFieldId,
                             int sortOrder, String comment) {
        ServiceParam p = new ServiceParam();
        p.setServiceId(svcId);
        p.setParamType(paramType);
        p.setParamName(name);
        p.setDataType(dataType);
        p.setIsRequired(isRequired);
        p.setDefaultValue(defaultValue);
        p.setModelFieldId(modelFieldId);
        p.setSortOrder(sortOrder);
        p.setParamComment(comment);
        p.setIsDeleted(0);
        serviceParamRepository.save(p);
    }

    private void seedOrchestrations() {
        log.info("[Mock] 种子化服务编排数据...");
        // 订单创建流程编排
        Long orchId = createOrchestration(msIds.get("order-svc"), "订单创建流程", "ORDER_CREATE_FLOW",
                "查询用户后创建订单的编排流程", 1);

        // 节点：START -> QUERY_USER(SERVICE) -> CREATE_ORDER(SERVICE) -> END
        createNode(orchId, "start_1", "START", "开始", null, 80, 200, 1);
        createNode(orchId, "service_1", "SERVICE", "查询用户", svcIds.get("query-user"), 300, 100, 2);
        createNode(orchId, "service_2", "SERVICE", "创建订单", svcIds.get("create-order"), 520, 300, 3);
        createNode(orchId, "end_1", "END", "结束", null, 740, 200, 4);

        // 连线
        createEdge(orchId, "edge_1", "start_1", "service_1", null, "开始->查询用户");
        createEdge(orchId, "edge_2", "service_1", "service_2", null, "查询用户->创建订单");
        createEdge(orchId, "edge_3", "service_2", "end_1", null, "创建订单->结束");

        log.info("[Mock] 服务编排数据种子化完成: orchId={}", orchId);
    }

    private Long createOrchestration(Long msId, String name, String code, String desc, int status) {
        Orchestration orch = new Orchestration();
        orch.setMicroserviceId(msId);
        orch.setName(name);
        orch.setCode(code);
        orch.setDescription(desc);
        orch.setStatus(status);
        orch.setIsDeleted(0);
        return orchestrationRepository.save(orch).getId();
    }

    private void createNode(Long orchId, String key, String type, String name,
                            Long svcId, int x, int y, int sort) {
        OrchestrationNode node = new OrchestrationNode();
        node.setOrchestrationId(orchId);
        node.setNodeKey(key);
        node.setNodeType(type);
        node.setNodeName(name);
        node.setServiceId(svcId);
        node.setXPos(x);
        node.setYPos(y);
        node.setSortOrder(sort);
        node.setIsDeleted(0);
        orchestrationNodeRepository.save(node);
    }

    private void createEdge(Long orchId, String key, String from, String to,
                            String condition, String label) {
        OrchestrationEdge edge = new OrchestrationEdge();
        edge.setOrchestrationId(orchId);
        edge.setEdgeKey(key);
        edge.setFromNodeKey(from);
        edge.setToNodeKey(to);
        edge.setConditionExpr(condition);
        edge.setLabelText(label);
        edge.setIsDeleted(0);
        orchestrationEdgeRepository.save(edge);
    }
}
