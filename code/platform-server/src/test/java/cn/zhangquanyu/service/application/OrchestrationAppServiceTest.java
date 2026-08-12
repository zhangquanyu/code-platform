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
import cn.zhangquanyu.service.dto.vo.OrchestrationDetailVO;
import cn.zhangquanyu.service.dto.vo.OrchestrationVO;
import cn.zhangquanyu.shared.api.PageResult;
import cn.zhangquanyu.shared.api.StatusCmd;
import cn.zhangquanyu.shared.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("服务编排 Service 单元测试")
class OrchestrationAppServiceTest {

    @Mock
    private OrchestrationRepository orchestrationRepository;
    @Mock
    private OrchestrationNodeRepository nodeRepository;
    @Mock
    private OrchestrationEdgeRepository edgeRepository;
    @Mock
    private MicroserviceRepository microserviceRepository;
    @Mock
    private ServiceDefRepository serviceDefRepository;
    @Mock
    private OrchestrationConverter converter;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrchestrationAppService orchService;

    private Orchestration sampleOrch;
    private Microservice sampleMs;
    private OrchestrationNode startNode;
    private OrchestrationNode serviceNode;
    private OrchestrationNode endNode;
    private OrchestrationEdge edge1;
    private OrchestrationEdge edge2;
    private ServiceDef sampleService;

    @BeforeEach
    void setUp() {
        sampleMs = new Microservice();
        sampleMs.setId(10L);
        sampleMs.setName("订单服务");
        sampleMs.setApplicationId(1L);

        sampleOrch = new Orchestration();
        sampleOrch.setId(500L);
        sampleOrch.setMicroserviceId(10L);
        sampleOrch.setName("订单创建流程");
        sampleOrch.setCode("ORDER_CREATE_FLOW");
        sampleOrch.setDescription("查询用户后创建订单");
        sampleOrch.setStatus(1);
        sampleOrch.setIsDeleted(0);
        sampleOrch.setCreateTime(LocalDateTime.now());

        startNode = new OrchestrationNode();
        startNode.setId(601L);
        startNode.setOrchestrationId(500L);
        startNode.setNodeKey("start_1");
        startNode.setNodeType("START");
        startNode.setNodeName("开始");
        startNode.setSortOrder(1);
        startNode.setXPos(80);
        startNode.setYPos(200);
        startNode.setIsDeleted(0);

        serviceNode = new OrchestrationNode();
        serviceNode.setId(602L);
        serviceNode.setOrchestrationId(500L);
        serviceNode.setNodeKey("service_1");
        serviceNode.setNodeType("SERVICE");
        serviceNode.setNodeName("创建订单");
        serviceNode.setServiceId(300L);
        serviceNode.setSortOrder(2);
        serviceNode.setXPos(300);
        serviceNode.setYPos(200);
        serviceNode.setIsDeleted(0);

        endNode = new OrchestrationNode();
        endNode.setId(603L);
        endNode.setOrchestrationId(500L);
        endNode.setNodeKey("end_1");
        endNode.setNodeType("END");
        endNode.setNodeName("结束");
        endNode.setSortOrder(3);
        endNode.setXPos(520);
        endNode.setYPos(200);
        endNode.setIsDeleted(0);

        edge1 = new OrchestrationEdge();
        edge1.setId(701L);
        edge1.setOrchestrationId(500L);
        edge1.setEdgeKey("edge_1");
        edge1.setFromNodeKey("start_1");
        edge1.setToNodeKey("service_1");
        edge1.setIsDeleted(0);

        edge2 = new OrchestrationEdge();
        edge2.setId(702L);
        edge2.setOrchestrationId(500L);
        edge2.setEdgeKey("edge_2");
        edge2.setFromNodeKey("service_1");
        edge2.setToNodeKey("end_1");
        edge2.setIsDeleted(0);

        sampleService = new ServiceDef();
        sampleService.setId(300L);
        sampleService.setName("创建订单");
        sampleService.setCode("CREATE_ORDER");
        sampleService.setMicroserviceId(10L);
        sampleService.setStatus(1);
        sampleService.setIsDeleted(0);
    }

    // ==================== 分页查询 ====================

    @Nested
    @DisplayName("分页查询 page()")
    class PageTest {

        @Test
        @DisplayName("正常分页查询 - 含节点/连线数")
        void page_normal() {
            OrchestrationPageQuery query = new OrchestrationPageQuery();
            query.setPageNum(1);
            query.setPageSize(10);

            OrchestrationVO vo = new OrchestrationVO();
            vo.setId(500L);
            vo.setName("订单创建流程");

            Page<Orchestration> page = new PageImpl<>(List.of(sampleOrch), Pageable.ofSize(10), 1);
            when(orchestrationRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(converter.toVO(sampleOrch)).thenReturn(vo);
            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(nodeRepository.findByOrchestrationIdAndIsDeletedOrderBySortOrderAsc(any(), eq(0)))
                    .thenReturn(List.of(startNode, serviceNode, endNode));
            when(edgeRepository.findByOrchestrationIdAndIsDeleted(any(Long.class), any(Integer.class))).thenReturn(List.of(edge1, edge2));

            PageResult<OrchestrationVO> result = orchService.page(query);

            assertThat(result.getList()).hasSize(1);
            assertThat(result.getList().get(0).getName()).isEqualTo("订单创建流程");
        }

        @Test
        @DisplayName("空数据分页查询")
        void page_empty() {
            OrchestrationPageQuery query = new OrchestrationPageQuery();
            query.setPageNum(1);
            query.setPageSize(10);

            Page<Orchestration> emptyPage = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);
            when(orchestrationRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

            PageResult<OrchestrationVO> result = orchService.page(query);

            assertThat(result.getList()).isEmpty();
        }
    }

    // ==================== 查询详情 ====================

    @Nested
    @DisplayName("查询详情 getById()")
    class GetByIdTest {

        @Test
        @DisplayName("正常查询详情 - 含节点和连线")
        void getById_normal() {
            OrchestrationVO vo = new OrchestrationVO();
            vo.setId(500L);
            vo.setName("订单创建流程");

            OrchNodeVO startVO = new OrchNodeVO();
            startVO.setNodeKey("start_1");
            startVO.setNodeType("START");

            OrchNodeVO serviceVO = new OrchNodeVO();
            serviceVO.setNodeKey("service_1");
            serviceVO.setNodeType("SERVICE");
            serviceVO.setServiceId(300L);

            OrchEdgeVO edgeVO = new OrchEdgeVO();
            edgeVO.setEdgeKey("edge_1");

            when(orchestrationRepository.findByIdAndIsDeleted(500L, 0)).thenReturn(Optional.of(sampleOrch));
            when(converter.toVO(sampleOrch)).thenReturn(vo);
            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(nodeRepository.findByOrchestrationIdAndIsDeletedOrderBySortOrderAsc(500L, 0))
                    .thenReturn(List.of(startNode, serviceNode, endNode));
            when(edgeRepository.findByOrchestrationIdAndIsDeleted(500L, 0))
                    .thenReturn(List.of(edge1, edge2));
            when(converter.toNodeVOList(anyList())).thenReturn(List.of(startVO, serviceVO));
            when(converter.toEdgeVOList(anyList())).thenReturn(List.of(edgeVO));
            when(serviceDefRepository.findByIdInAndIsDeleted(anyList(), eq(0))).thenReturn(List.of(sampleService));

            OrchestrationDetailVO result = orchService.getById(500L);

            assertThat(result.getOrchestration().getName()).isEqualTo("订单创建流程");
            assertThat(result.getNodes()).isNotNull();
            assertThat(result.getEdges()).isNotNull();
        }

        @Test
        @DisplayName("编排不存在 - 抛出异常")
        void getById_notFound() {
            when(orchestrationRepository.findByIdAndIsDeleted(999L, 0)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orchService.getById(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("编排不存在");
        }
    }

    // ==================== 创建编排 ====================

    @Nested
    @DisplayName("创建编排 create()")
    class CreateTest {

        @Test
        @DisplayName("正常创建编排")
        void create_normal() {
            OrchestrationCreateCmd cmd = new OrchestrationCreateCmd();
            cmd.setMicroserviceId(10L);
            cmd.setName("支付流程");
            cmd.setCode("PAY_FLOW");
            cmd.setDescription("支付编排");

            OrchestrationVO vo = new OrchestrationVO();
            vo.setName("支付流程");

            when(orchestrationRepository.existsByMicroserviceIdAndCodeAndIsDeleted(10L, "PAY_FLOW", 0)).thenReturn(false);
            when(orchestrationRepository.save(any(Orchestration.class))).thenAnswer(inv -> {
                Orchestration o = inv.getArgument(0);
                o.setId(501L);
                return o;
            });
            // getById mocks
            when(orchestrationRepository.findByIdAndIsDeleted(501L, 0)).thenReturn(Optional.of(sampleOrch));
            when(converter.toVO(any(Orchestration.class))).thenReturn(vo);
            when(microserviceRepository.findByIdAndIsDeleted(any(), eq(0))).thenReturn(Optional.of(sampleMs));
            when(nodeRepository.findByOrchestrationIdAndIsDeletedOrderBySortOrderAsc(any(), eq(0)))
                    .thenReturn(List.of());
            when(edgeRepository.findByOrchestrationIdAndIsDeleted(any(Long.class), any(Integer.class))).thenReturn(List.of());
            when(converter.toNodeVOList(anyList())).thenReturn(List.of());
            when(converter.toEdgeVOList(anyList())).thenReturn(List.of());

            OrchestrationDetailVO result = orchService.create(cmd);

            assertThat(result.getOrchestration().getName()).isEqualTo("支付流程");
            verify(orchestrationRepository).save(any(Orchestration.class));
        }

        @Test
        @DisplayName("创建失败 - 微服务不存在")
        void create_msNotFound() {
            OrchestrationCreateCmd cmd = new OrchestrationCreateCmd();
            cmd.setMicroserviceId(999L);
            cmd.setName("test");
            cmd.setCode("TEST");

            when(microserviceRepository.findByIdAndIsDeleted(999L, 0)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orchService.create(cmd))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("微服务不存在");
        }

        @Test
        @DisplayName("创建失败 - 编码已存在")
        void create_duplicateCode() {
            OrchestrationCreateCmd cmd = new OrchestrationCreateCmd();
            cmd.setMicroserviceId(10L);
            cmd.setName("订单创建流程");
            cmd.setCode("ORDER_CREATE_FLOW");

            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(orchestrationRepository.existsByMicroserviceIdAndCodeAndIsDeleted(10L, "ORDER_CREATE_FLOW", 0)).thenReturn(true);

            assertThatThrownBy(() -> orchService.create(cmd))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("编排编码在该微服务内已存在");
        }
    }

    // ==================== 更新编排 ====================

    @Nested
    @DisplayName("更新编排 update()")
    class UpdateTest {

        @Test
        @DisplayName("正常更新编排 - 含节点和连线重写")
        void update_normal() {
            OrchestrationUpdateCmd.OrchNodeCmd startNodeCmd = new OrchestrationUpdateCmd.OrchNodeCmd();
            startNodeCmd.setNodeKey("start_1");
            startNodeCmd.setNodeType("START");
            startNodeCmd.setNodeName("开始");
            startNodeCmd.setSortOrder(1);
            startNodeCmd.setXPos(80);
            startNodeCmd.setYPos(200);

            OrchestrationUpdateCmd.OrchNodeCmd endNodeCmd = new OrchestrationUpdateCmd.OrchNodeCmd();
            endNodeCmd.setNodeKey("end_1");
            endNodeCmd.setNodeType("END");
            endNodeCmd.setNodeName("结束");
            endNodeCmd.setSortOrder(2);
            endNodeCmd.setXPos(400);
            endNodeCmd.setYPos(200);

            OrchestrationUpdateCmd.OrchEdgeCmd edgeCmd = new OrchestrationUpdateCmd.OrchEdgeCmd();
            edgeCmd.setEdgeKey("edge_1");
            edgeCmd.setFromNodeKey("start_1");
            edgeCmd.setToNodeKey("end_1");

            OrchestrationUpdateCmd cmd = new OrchestrationUpdateCmd();
            cmd.setName("订单创建流程V2");
            cmd.setNodes(List.of(startNodeCmd, endNodeCmd));
            cmd.setEdges(List.of(edgeCmd));

            OrchestrationVO vo = new OrchestrationVO();
            vo.setName("订单创建流程V2");

            when(orchestrationRepository.findByIdAndIsDeleted(500L, 0)).thenReturn(Optional.of(sampleOrch));
            when(orchestrationRepository.save(any(Orchestration.class))).thenReturn(sampleOrch);
            // getById mocks
            when(converter.toVO(any(Orchestration.class))).thenReturn(vo);
            when(microserviceRepository.findByIdAndIsDeleted(any(), eq(0))).thenReturn(Optional.of(sampleMs));
            when(nodeRepository.findByOrchestrationIdAndIsDeletedOrderBySortOrderAsc(any(), eq(0)))
                    .thenReturn(List.of(startNode));
            when(edgeRepository.findByOrchestrationIdAndIsDeleted(any(Long.class), any(Integer.class))).thenReturn(List.of(edge1));
            when(converter.toNodeVOList(anyList())).thenReturn(List.of(new OrchNodeVO()));
            when(converter.toEdgeVOList(anyList())).thenReturn(List.of(new OrchEdgeVO()));
            when(serviceDefRepository.findByIdInAndIsDeleted(anyList(), eq(0))).thenReturn(List.of());

            OrchestrationDetailVO result = orchService.update(500L, cmd);

            assertThat(result.getOrchestration().getName()).isEqualTo("订单创建流程V2");
            verify(nodeRepository).softDeleteByOrchestrationId(500L);
            verify(edgeRepository).softDeleteByOrchestrationId(500L);
            verify(nodeRepository, times(2)).save(any(OrchestrationNode.class));
            verify(edgeRepository).save(any(OrchestrationEdge.class));
        }

        @Test
        @DisplayName("更新失败 - 编排不存在")
        void update_notFound() {
            OrchestrationUpdateCmd cmd = new OrchestrationUpdateCmd();
            cmd.setName("test");

            when(orchestrationRepository.findByIdAndIsDeleted(999L, 0)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orchService.update(999L, cmd))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("编排不存在");
        }
    }

    // ==================== 删除编排 ====================

    @Test
    @DisplayName("正常删除编排（级联软删除节点和连线） delete()")
    void delete_normal() {
        when(orchestrationRepository.findByIdAndIsDeleted(500L, 0)).thenReturn(Optional.of(sampleOrch));

        orchService.delete(500L);

        verify(nodeRepository).softDeleteByOrchestrationId(500L);
        verify(edgeRepository).softDeleteByOrchestrationId(500L);
        verify(orchestrationRepository).softDelete(500L);
    }

    // ==================== 更新状态 ====================

    @Test
    @DisplayName("更新编排状态 updateStatus()")
    void updateStatus_normal() {
        StatusCmd cmd = new StatusCmd();
        cmd.setStatus(0);

        when(orchestrationRepository.findByIdAndIsDeleted(500L, 0)).thenReturn(Optional.of(sampleOrch));
        when(orchestrationRepository.save(any(Orchestration.class))).thenReturn(sampleOrch);

        orchService.updateStatus(500L, cmd);

        assertThat(sampleOrch.getStatus()).isEqualTo(0);
    }

    // ==================== 健康检查 ====================

    @Nested
    @DisplayName("健康检查 health()")
    class HealthTest {

        @Test
        @DisplayName("健康检查通过 - 所有服务可用")
        void health_healthy() {
            when(orchestrationRepository.findByIdAndIsDeleted(500L, 0)).thenReturn(Optional.of(sampleOrch));
            when(nodeRepository.findByOrchestrationIdAndIsDeletedOrderBySortOrderAsc(500L, 0))
                    .thenReturn(List.of(startNode, serviceNode, endNode));
            when(serviceDefRepository.findByIdInAndIsDeleted(anyList(), eq(0)))
                    .thenReturn(List.of(sampleService));

            OrchHealthVO result = orchService.health(500L);

            assertThat(result.isHealthy()).isTrue();
            assertThat(result.getAlerts()).isEmpty();
        }

        @Test
        @DisplayName("健康检查不通过 - 服务已停用")
        void health_unhealthy() {
            ServiceDef stoppedService = new ServiceDef();
            stoppedService.setId(300L);
            stoppedService.setName("创建订单");
            stoppedService.setStatus(0);
            stoppedService.setIsDeleted(0);

            when(orchestrationRepository.findByIdAndIsDeleted(500L, 0)).thenReturn(Optional.of(sampleOrch));
            when(nodeRepository.findByOrchestrationIdAndIsDeletedOrderBySortOrderAsc(500L, 0))
                    .thenReturn(List.of(startNode, serviceNode, endNode));
            when(serviceDefRepository.findByIdInAndIsDeleted(anyList(), eq(0)))
                    .thenReturn(List.of(stoppedService));

            OrchHealthVO result = orchService.health(500L);

            assertThat(result.isHealthy()).isFalse();
            assertThat(result.getAlerts()).isNotEmpty();
        }
    }

    // ==================== 编排调试 ====================

    @Nested
    @DisplayName("编排调试 debug()")
    class DebugTest {

        @Test
        @DisplayName("调试成功 - 节点全部执行通过")
        void debug_success() {
            OrchDebugCmd cmd = new OrchDebugCmd();
            Map<String, Object> inputData = new HashMap<>();
            inputData.put("userId", 12345);
            cmd.setInputData(inputData);

            when(orchestrationRepository.findByIdAndIsDeleted(500L, 0)).thenReturn(Optional.of(sampleOrch));
            when(nodeRepository.findByOrchestrationIdAndIsDeletedOrderBySortOrderAsc(500L, 0))
                    .thenReturn(List.of(startNode, serviceNode, endNode));
            when(edgeRepository.findByOrchestrationIdAndIsDeleted(500L, 0))
                    .thenReturn(List.of(edge1, edge2));
            when(serviceDefRepository.findByIdInAndIsDeleted(anyList(), eq(0)))
                    .thenReturn(List.of(sampleService));

            OrchDebugResultVO result = orchService.debug(500L, cmd);

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getNodeResults()).hasSize(3);
            assertThat(result.getOutput()).isNotNull();
        }

        @Test
        @DisplayName("调试失败 - 编排不存在")
        void debug_notFound() {
            OrchDebugCmd cmd = new OrchDebugCmd();
            cmd.setInputData(new HashMap<>());

            when(orchestrationRepository.findByIdAndIsDeleted(999L, 0)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orchService.debug(999L, cmd))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("编排不存在");
        }
    }

    private static int anyInt() {
        return org.mockito.ArgumentMatchers.anyInt();
    }

    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
