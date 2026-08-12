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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("服务管理 Service 单元测试")
class ServiceAppServiceTest {

    @Mock
    private ServiceDefRepository serviceDefRepository;
    @Mock
    private ServiceParamRepository serviceParamRepository;
    @Mock
    private MicroserviceRepository microserviceRepository;
    @Mock
    private OrchestrationNodeRepository orchestrationNodeRepository;
    @Mock
    private ServiceConverter converter;

    @InjectMocks
    private ServiceAppService serviceService;

    private ServiceDef sampleService;
    private Microservice sampleMs;
    private ServiceParam inputParam;
    private ServiceParam outputParam;

    @BeforeEach
    void setUp() {
        sampleMs = new Microservice();
        sampleMs.setId(10L);
        sampleMs.setName("订单服务");
        sampleMs.setApplicationId(1L);

        sampleService = new ServiceDef();
        sampleService.setId(300L);
        sampleService.setMicroserviceId(10L);
        sampleService.setName("创建订单");
        sampleService.setCode("CREATE_ORDER");
        sampleService.setHttpMethod("POST");
        sampleService.setServicePath("/api/order/create");
        sampleService.setCategory("ORDER");
        sampleService.setStatus(1);
        sampleService.setIsDeleted(0);
        sampleService.setCreateTime(LocalDateTime.now());

        inputParam = new ServiceParam();
        inputParam.setId(400L);
        inputParam.setServiceId(300L);
        inputParam.setParamType(ServiceParam.TYPE_INPUT);
        inputParam.setParamName("userId");
        inputParam.setDataType("LONG");
        inputParam.setIsRequired(1);
        inputParam.setSortOrder(1);
        inputParam.setIsDeleted(0);

        outputParam = new ServiceParam();
        outputParam.setId(401L);
        outputParam.setServiceId(300L);
        outputParam.setParamType(ServiceParam.TYPE_OUTPUT);
        outputParam.setParamName("orderId");
        outputParam.setDataType("LONG");
        outputParam.setIsRequired(1);
        outputParam.setSortOrder(1);
        outputParam.setIsDeleted(0);
    }

    // ==================== 分页查询 ====================

    @Nested
    @DisplayName("分页查询 page()")
    class PageTest {

        @Test
        @DisplayName("正常分页查询 - 含入参出参数量")
        void page_normal() {
            ServicePageQuery query = new ServicePageQuery();
            query.setPageNum(1);
            query.setPageSize(10);

            ServiceVO vo = new ServiceVO();
            vo.setId(300L);
            vo.setName("创建订单");

            Page<ServiceDef> page = new PageImpl<>(List.of(sampleService), Pageable.ofSize(10), 1);
            when(serviceDefRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(converter.toVO(sampleService)).thenReturn(vo);
            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(serviceParamRepository.findByServiceIdAndIsDeletedOrderBySortOrderAsc(300L, 0))
                    .thenReturn(List.of(inputParam, outputParam));

            PageResult<ServiceVO> result = serviceService.page(query);

            assertThat(result.getList()).hasSize(1);
            assertThat(result.getList().get(0).getName()).isEqualTo("创建订单");
            assertThat(result.getList().get(0).getInputCount()).isEqualTo(1);
            assertThat(result.getList().get(0).getOutputCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("空数据分页查询")
        void page_empty() {
            ServicePageQuery query = new ServicePageQuery();
            query.setPageNum(1);
            query.setPageSize(10);

            Page<ServiceDef> emptyPage = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);
            when(serviceDefRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

            PageResult<ServiceVO> result = serviceService.page(query);

            assertThat(result.getList()).isEmpty();
        }
    }

    // ==================== 查询详情 ====================

    @Nested
    @DisplayName("查询详情 getById()")
    class GetByIdTest {

        @Test
        @DisplayName("正常查询详情 - 含入参出参列表")
        void getById_normal() {
            ServiceVO vo = new ServiceVO();
            vo.setId(300L);
            vo.setName("创建订单");

            ServiceParamVO inputVO = new ServiceParamVO();
            inputVO.setParamName("userId");
            ServiceParamVO outputVO = new ServiceParamVO();
            outputVO.setParamName("orderId");

            when(serviceDefRepository.findByIdAndIsDeleted(300L, 0)).thenReturn(Optional.of(sampleService));
            when(converter.toVO(sampleService)).thenReturn(vo);
            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(serviceParamRepository.findByServiceIdAndIsDeletedOrderBySortOrderAsc(300L, 0))
                    .thenReturn(List.of(inputParam, outputParam));
            when(converter.toParamVOList(anyList())).thenReturn(List.of(inputVO), List.of(outputVO));

            ServiceDetailVO result = serviceService.getById(300L);

            assertThat(result.getService().getName()).isEqualTo("创建订单");
            assertThat(result.getInputs()).hasSize(1);
            assertThat(result.getOutputs()).hasSize(1);
            assertThat(result.getInputs().get(0).getParamName()).isEqualTo("userId");
        }

        @Test
        @DisplayName("服务不存在 - 抛出异常")
        void getById_notFound() {
            when(serviceDefRepository.findByIdAndIsDeleted(999L, 0)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceService.getById(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("服务不存在");
        }
    }

    // ==================== 按微服务查询列表 ====================

    @Test
    @DisplayName("按微服务查询启用服务列表 listByMicroservice()")
    void listByMicroservice_normal() {
        ServiceSimpleVO simpleVO = new ServiceSimpleVO();
        simpleVO.setId(300L);
        simpleVO.setName("创建订单");

        when(serviceDefRepository.findByMicroserviceIdAndIsDeletedAndStatusOrderByCreateTimeDesc(10L, 0, 1))
                .thenReturn(List.of(sampleService));
        when(converter.toSimpleVO(sampleService)).thenReturn(simpleVO);

        List<ServiceSimpleVO> result = serviceService.listByMicroservice(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("创建订单");
    }

    // ==================== 创建服务 ====================

    @Nested
    @DisplayName("创建服务 create()")
    class CreateTest {

        @Test
        @DisplayName("正常创建服务 - 含入参出参")
        void create_normal() {
            ServiceCreateCmd.ParamItem input = new ServiceCreateCmd.ParamItem();
            input.setParamName("userId");
            input.setDataType("LONG");
            input.setIsRequired(1);
            input.setSortOrder(1);

            ServiceCreateCmd cmd = new ServiceCreateCmd();
            cmd.setMicroserviceId(10L);
            cmd.setName("查询订单");
            cmd.setCode("QUERY_ORDER");
            cmd.setHttpMethod("GET");
            cmd.setServicePath("/api/order/query");
            cmd.setCategory("ORDER");
            cmd.setInputs(List.of(input));
            cmd.setOutputs(List.of());

            ServiceVO vo = new ServiceVO();
            vo.setId(301L);
            vo.setName("查询订单");

            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(serviceDefRepository.existsByMicroserviceIdAndCodeAndIsDeleted(10L, "QUERY_ORDER", 0)).thenReturn(false);
            when(serviceDefRepository.existsByMicroserviceIdAndServicePathAndIsDeleted(10L, "/api/order/query", 0)).thenReturn(false);
            when(serviceDefRepository.save(any(ServiceDef.class))).thenAnswer(inv -> {
                ServiceDef s = inv.getArgument(0);
                s.setId(301L);
                return s;
            });
            // getById mocks
            when(serviceDefRepository.findByIdAndIsDeleted(301L, 0)).thenReturn(Optional.of(sampleService));
            when(converter.toVO(any(ServiceDef.class))).thenReturn(vo);
            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(serviceParamRepository.findByServiceIdAndIsDeletedOrderBySortOrderAsc(any(), anyInt()))
                    .thenReturn(List.of());
            when(converter.toParamVOList(anyList())).thenReturn(List.of());

            ServiceDetailVO result = serviceService.create(cmd);

            assertThat(result.getService().getName()).isEqualTo("查询订单");
            verify(serviceDefRepository).save(any(ServiceDef.class));
            verify(serviceParamRepository).save(any(ServiceParam.class));
        }

        @Test
        @DisplayName("创建失败 - 微服务不存在")
        void create_msNotFound() {
            ServiceCreateCmd cmd = new ServiceCreateCmd();
            cmd.setMicroserviceId(999L);
            cmd.setName("test");
            cmd.setCode("TEST");
            cmd.setHttpMethod("GET");
            cmd.setServicePath("/api/test");

            when(microserviceRepository.findByIdAndIsDeleted(999L, 0)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceService.create(cmd))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("微服务不存在");
        }

        @Test
        @DisplayName("创建失败 - 编码已存在")
        void create_duplicateCode() {
            ServiceCreateCmd cmd = new ServiceCreateCmd();
            cmd.setMicroserviceId(10L);
            cmd.setName("创建订单");
            cmd.setCode("CREATE_ORDER");
            cmd.setHttpMethod("POST");
            cmd.setServicePath("/api/order/create2");

            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(serviceDefRepository.existsByMicroserviceIdAndCodeAndIsDeleted(10L, "CREATE_ORDER", 0)).thenReturn(true);

            assertThatThrownBy(() -> serviceService.create(cmd))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("服务编码在该微服务内已存在");
        }

        @Test
        @DisplayName("创建失败 - 路径已存在")
        void create_duplicatePath() {
            ServiceCreateCmd cmd = new ServiceCreateCmd();
            cmd.setMicroserviceId(10L);
            cmd.setName("创建订单");
            cmd.setCode("CREATE_ORDER2");
            cmd.setHttpMethod("POST");
            cmd.setServicePath("/api/order/create");

            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(serviceDefRepository.existsByMicroserviceIdAndCodeAndIsDeleted(10L, "CREATE_ORDER2", 0)).thenReturn(false);
            when(serviceDefRepository.existsByMicroserviceIdAndServicePathAndIsDeleted(10L, "/api/order/create", 0)).thenReturn(true);

            assertThatThrownBy(() -> serviceService.create(cmd))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("服务路径在该微服务内已存在");
        }
    }

    // ==================== 更新服务 ====================

    @Test
    @DisplayName("正常更新服务 - 含参数重写 update()")
    void update_normal() {
        ServiceCreateCmd cmd = new ServiceCreateCmd();
        cmd.setMicroserviceId(10L);
        cmd.setName("创建订单V2");
        cmd.setCode("CREATE_ORDER");
        cmd.setHttpMethod("POST");
        cmd.setServicePath("/api/order/create");
        cmd.setCategory("ORDER");
        cmd.setInputs(List.of());
        cmd.setOutputs(List.of());

        ServiceVO vo = new ServiceVO();
        vo.setName("创建订单V2");

        when(serviceDefRepository.findByIdAndIsDeleted(300L, 0)).thenReturn(Optional.of(sampleService));
        when(serviceDefRepository.save(any(ServiceDef.class))).thenReturn(sampleService);
        // getById mocks for return
        when(converter.toVO(any(ServiceDef.class))).thenReturn(vo);
            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(serviceParamRepository.findByServiceIdAndIsDeletedOrderBySortOrderAsc(any(), anyInt()))
                    .thenReturn(List.of());
            when(converter.toParamVOList(anyList())).thenReturn(List.of());

            ServiceDetailVO result = serviceService.update(300L, cmd);

        assertThat(result.getService().getName()).isEqualTo("创建订单V2");
        verify(serviceParamRepository).softDeleteByServiceId(300L);
    }

    // ==================== 删除服务 ====================

    @Nested
    @DisplayName("删除服务 delete()")
    class DeleteTest {

        @Test
        @DisplayName("正常删除服务（软删除 + 清理参数）")
        void delete_normal() {
            when(serviceDefRepository.findByIdAndIsDeleted(300L, 0)).thenReturn(Optional.of(sampleService));
            when(orchestrationNodeRepository.countByServiceIdAndIsDeleted(300L, 0)).thenReturn(0L);

            serviceService.delete(300L);

            verify(serviceParamRepository).softDeleteByServiceId(300L);
            verify(serviceDefRepository).softDelete(300L);
        }

        @Test
        @DisplayName("删除失败 - 被编排节点引用")
        void delete_referencedByOrch() {
            when(serviceDefRepository.findByIdAndIsDeleted(300L, 0)).thenReturn(Optional.of(sampleService));
            when(orchestrationNodeRepository.countByServiceIdAndIsDeleted(300L, 0)).thenReturn(2L);

            assertThatThrownBy(() -> serviceService.delete(300L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("服务已被编排节点引用");
            verify(serviceDefRepository, never()).softDelete(any());
        }
    }

    // ==================== 更新状态 ====================

    @Test
    @DisplayName("更新服务状态 updateStatus()")
    void updateStatus_normal() {
        StatusCmd cmd = new StatusCmd();
        cmd.setStatus(0);

        when(serviceDefRepository.findByIdAndIsDeleted(300L, 0)).thenReturn(Optional.of(sampleService));
        when(serviceDefRepository.save(any(ServiceDef.class))).thenReturn(sampleService);

        serviceService.updateStatus(300L, cmd);

        assertThat(sampleService.getStatus()).isEqualTo(0);
    }

    private static int anyInt() {
        return org.mockito.ArgumentMatchers.anyInt();
    }
}
