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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("微服务管理 Service 单元测试")
class MicroserviceAppServiceTest {

    @Mock
    private MicroserviceRepository microserviceRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private ModelRepository modelRepository;
    @Mock
    private ServiceDefRepository serviceDefRepository;
    @Mock
    private OrchestrationRepository orchestrationRepository;
    @Mock
    private MicroserviceConverter converter;

    @InjectMocks
    private MicroserviceAppService msService;

    private Microservice sampleMs;
    private Application sampleApp;

    @BeforeEach
    void setUp() {
        sampleApp = new Application();
        sampleApp.setId(1L);
        sampleApp.setName("订单中心");
        sampleApp.setCode("ORDER_CENTER");

        sampleMs = new Microservice();
        sampleMs.setId(10L);
        sampleMs.setApplicationId(1L);
        sampleMs.setName("订单服务");
        sampleMs.setCode("ORDER_SERVICE");
        sampleMs.setVersion("1.0.0");
        sampleMs.setStatus(1);
        sampleMs.setIsDeleted(0);
        sampleMs.setCreateTime(LocalDateTime.now());
        sampleMs.setUpdateTime(LocalDateTime.now());
    }

    // ==================== 分页查询 ====================

    @Nested
    @DisplayName("分页查询 page()")
    class PageTest {

        @Test
        @DisplayName("正常分页查询")
        void page_normal() {
            MicroservicePageQuery query = new MicroservicePageQuery();
            query.setPageNum(1);
            query.setPageSize(10);

            MicroserviceVO vo = new MicroserviceVO();
            vo.setId(10L);
            vo.setName("订单服务");

            Page<Microservice> page = new PageImpl<>(List.of(sampleMs), Pageable.ofSize(10), 1);
            when(microserviceRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(converter.toVO(sampleMs)).thenReturn(vo);
            when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));

            PageResult<MicroserviceVO> result = msService.page(query);

            assertThat(result.getList()).hasSize(1);
            assertThat(result.getList().get(0).getName()).isEqualTo("订单服务");
        }

        @Test
        @DisplayName("按应用ID筛选分页查询")
        void page_byApplicationId() {
            MicroservicePageQuery query = new MicroservicePageQuery();
            query.setPageNum(1);
            query.setPageSize(10);
            query.setApplicationId(1L);

            Page<Microservice> page = new PageImpl<>(List.of(sampleMs), Pageable.ofSize(10), 1);
            when(microserviceRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(converter.toVO(sampleMs)).thenReturn(new MicroserviceVO());
            when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));

            PageResult<MicroserviceVO> result = msService.page(query);

            assertThat(result.getList()).hasSize(1);
        }
    }

    // ==================== 查询详情 ====================

    @Nested
    @DisplayName("查询详情 getById()")
    class GetByIdTest {

        @Test
        @DisplayName("正常查询详情 - 填充应用名")
        void getById_normal() {
            MicroserviceVO vo = new MicroserviceVO();
            vo.setId(10L);
            vo.setName("订单服务");

            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(converter.toVO(sampleMs)).thenReturn(vo);
            when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));

            MicroserviceVO result = msService.getById(10L);

            assertThat(result.getName()).isEqualTo("订单服务");
            assertThat(result.getApplicationName()).isEqualTo("订单中心");
        }

        @Test
        @DisplayName("微服务不存在 - 抛出异常")
        void getById_notFound() {
            when(microserviceRepository.findByIdAndIsDeleted(999L, 0)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> msService.getById(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("微服务不存在");
        }
    }

    // ==================== 按应用查询列表 ====================

    @Test
    @DisplayName("按应用查询启用微服务列表 listByApplication()")
    void listByApplication_normal() {
        MicroserviceSimpleVO simpleVO = new MicroserviceSimpleVO();
        simpleVO.setId(10L);
        simpleVO.setName("订单服务");

        when(microserviceRepository.findByApplicationIdAndIsDeletedAndStatusOrderByCreateTimeDesc(1L, 0, 1))
                .thenReturn(List.of(sampleMs));
        when(converter.toSimpleVO(sampleMs)).thenReturn(simpleVO);

        List<MicroserviceSimpleVO> result = msService.listByApplication(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("订单服务");
    }

    // ==================== 资源汇总 ====================

    @Test
    @DisplayName("查询资源汇总 summary()")
    void summary_normal() {
        when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
        when(modelRepository.countByMicroserviceIdAndIsDeleted(10L, 0)).thenReturn(5L);
        when(serviceDefRepository.countByMicroserviceIdAndIsDeleted(10L, 0)).thenReturn(3L);
        when(orchestrationRepository.countByMicroserviceIdAndIsDeleted(10L, 0)).thenReturn(2L);

        MicroserviceSummaryVO result = msService.summary(10L);

        assertThat(result.getModelCount()).isEqualTo(5);
        assertThat(result.getServiceCount()).isEqualTo(3);
        assertThat(result.getOrchestrationCount()).isEqualTo(2);
    }

    // ==================== 创建微服务 ====================

    @Nested
    @DisplayName("创建微服务 create()")
    class CreateTest {

        @Test
        @DisplayName("正常创建微服务")
        void create_normal() {
            MicroserviceCreateCmd cmd = new MicroserviceCreateCmd();
            cmd.setApplicationId(1L);
            cmd.setName("支付服务");
            cmd.setCode("PAY_SERVICE");

            MicroserviceVO vo = new MicroserviceVO();
            vo.setName("支付服务");

            when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));
            when(microserviceRepository.existsByApplicationIdAndCodeAndIsDeleted(1L, "PAY_SERVICE", 0)).thenReturn(false);
            when(converter.toEntity(cmd)).thenReturn(sampleMs);
            when(microserviceRepository.save(any(Microservice.class))).thenReturn(sampleMs);
            when(converter.toVO(sampleMs)).thenReturn(vo);

            MicroserviceVO result = msService.create(cmd);

            assertThat(result.getName()).isEqualTo("支付服务");
            assertThat(result.getApplicationName()).isEqualTo("订单中心");
        }

        @Test
        @DisplayName("创建失败 - 应用不存在")
        void create_appNotFound() {
            MicroserviceCreateCmd cmd = new MicroserviceCreateCmd();
            cmd.setApplicationId(999L);
            cmd.setName("test");
            cmd.setCode("TEST");

            when(applicationRepository.findByIdAndIsDeleted(999L, 0)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> msService.create(cmd))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("应用不存在");
        }

        @Test
        @DisplayName("创建失败 - 编码已存在")
        void create_duplicateCode() {
            MicroserviceCreateCmd cmd = new MicroserviceCreateCmd();
            cmd.setApplicationId(1L);
            cmd.setName("订单服务");
            cmd.setCode("ORDER_SERVICE");

            when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));
            when(microserviceRepository.existsByApplicationIdAndCodeAndIsDeleted(1L, "ORDER_SERVICE", 0)).thenReturn(true);

            assertThatThrownBy(() -> msService.create(cmd))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("微服务编码在该应用内已存在");
        }
    }

    // ==================== 更新微服务 ====================

    @Test
    @DisplayName("正常更新微服务 update()")
    void update_normal() {
        MicroserviceUpdateCmd cmd = new MicroserviceUpdateCmd();
        cmd.setName("订单服务V2");
        cmd.setVersion("2.0.0");

        MicroserviceVO vo = new MicroserviceVO();
        vo.setName("订单服务V2");

        when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
        when(microserviceRepository.save(any(Microservice.class))).thenReturn(sampleMs);
        when(converter.toVO(sampleMs)).thenReturn(vo);
        when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));

        MicroserviceVO result = msService.update(10L, cmd);

        assertThat(result.getName()).isEqualTo("订单服务V2");
        assertThat(sampleMs.getName()).isEqualTo("订单服务V2");
        assertThat(sampleMs.getVersion()).isEqualTo("2.0.0");
    }

    // ==================== 删除微服务 ====================

    @Nested
    @DisplayName("删除微服务 delete()")
    class DeleteTest {

        @Test
        @DisplayName("正常删除微服务（软删除）")
        void delete_normal() {
            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(modelRepository.countByMicroserviceIdAndIsDeleted(10L, 0)).thenReturn(0L);
            when(serviceDefRepository.countByMicroserviceIdAndIsDeleted(10L, 0)).thenReturn(0L);
            when(orchestrationRepository.countByMicroserviceIdAndIsDeleted(10L, 0)).thenReturn(0L);

            msService.delete(10L);

            verify(microserviceRepository).softDelete(10L);
        }

        @Test
        @DisplayName("删除失败 - 存在关联模型")
        void delete_hasModels() {
            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(modelRepository.countByMicroserviceIdAndIsDeleted(10L, 0)).thenReturn(2L);

            assertThatThrownBy(() -> msService.delete(10L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("存在关联资源");
            verify(microserviceRepository, never()).softDelete(any());
        }

        @Test
        @DisplayName("删除失败 - 存在关联服务")
        void delete_hasServices() {
            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(modelRepository.countByMicroserviceIdAndIsDeleted(10L, 0)).thenReturn(0L);
            when(serviceDefRepository.countByMicroserviceIdAndIsDeleted(10L, 0)).thenReturn(3L);

            assertThatThrownBy(() -> msService.delete(10L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("存在关联资源");
        }
    }

    // ==================== 更新状态 ====================

    @Test
    @DisplayName("更新微服务状态 updateStatus()")
    void updateStatus_normal() {
        StatusCmd cmd = new StatusCmd();
        cmd.setStatus(0);

        when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
        when(microserviceRepository.save(any(Microservice.class))).thenReturn(sampleMs);

        msService.updateStatus(10L, cmd);

        assertThat(sampleMs.getStatus()).isEqualTo(0);
    }
}
