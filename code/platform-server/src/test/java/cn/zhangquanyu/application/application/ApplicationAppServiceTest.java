package cn.zhangquanyu.application.application;

import cn.zhangquanyu.application.converter.ApplicationConverter;
import cn.zhangquanyu.application.domain.entity.Application;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("应用管理 Service 单元测试")
class ApplicationAppServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private MicroserviceRepository microserviceRepository;
    @Mock
    private MetadataRepository metadataRepository;
    @Mock
    private ApplicationConverter converter;

    @InjectMocks
    private ApplicationAppService appService;

    private Application sampleApp;

    @BeforeEach
    void setUp() {
        sampleApp = new Application();
        sampleApp.setId(1L);
        sampleApp.setName("订单中心");
        sampleApp.setCode("ORDER_CENTER");
        sampleApp.setDescription("统一订单管理");
        sampleApp.setVersion("1.0.0");
        sampleApp.setStatus(1);
        sampleApp.setIsDeleted(0);
        sampleApp.setCreateTime(LocalDateTime.now());
        sampleApp.setUpdateTime(LocalDateTime.now());
    }

    // ==================== 分页查询 ====================

    @Nested
    @DisplayName("分页查询 page()")
    class PageTest {

        @Test
        @DisplayName("正常分页查询 - 返回应用列表")
        void page_normal() {
            ApplicationPageQuery query = new ApplicationPageQuery();
            query.setPageNum(1);
            query.setPageSize(10);

            ApplicationVO vo = new ApplicationVO();
            vo.setId(1L);
            vo.setName("订单中心");

            Page<Application> page = new PageImpl<>(List.of(sampleApp), Pageable.ofSize(10), 1);
            when(applicationRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(converter.toVOList(anyList())).thenReturn(List.of(vo));

            PageResult<ApplicationVO> result = appService.page(query);

            assertThat(result.getList()).hasSize(1);
            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getList().get(0).getName()).isEqualTo("订单中心");
            verify(applicationRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("空数据分页查询")
        void page_empty() {
            ApplicationPageQuery query = new ApplicationPageQuery();
            query.setPageNum(1);
            query.setPageSize(10);

            Page<Application> emptyPage = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);
            when(applicationRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);
            when(converter.toVOList(anyList())).thenReturn(List.of());

            PageResult<ApplicationVO> result = appService.page(query);

            assertThat(result.getList()).isEmpty();
            assertThat(result.getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("带关键字分页查询")
        void page_withKeyword() {
            ApplicationPageQuery query = new ApplicationPageQuery();
            query.setPageNum(1);
            query.setPageSize(10);
            query.setKeyword("订单");

            Page<Application> page = new PageImpl<>(List.of(sampleApp), Pageable.ofSize(10), 1);
            when(applicationRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(converter.toVOList(anyList())).thenReturn(List.of(new ApplicationVO()));

            PageResult<ApplicationVO> result = appService.page(query);

            assertThat(result.getList()).hasSize(1);
        }
    }

    // ==================== 查询详情 ====================

    @Nested
    @DisplayName("查询详情 getById()")
    class GetByIdTest {

        @Test
        @DisplayName("正常查询详情")
        void getById_normal() {
            ApplicationVO vo = new ApplicationVO();
            vo.setId(1L);
            vo.setName("订单中心");
            when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));
            when(converter.toVO(sampleApp)).thenReturn(vo);

            ApplicationVO result = appService.getById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("订单中心");
        }

        @Test
        @DisplayName("应用不存在 - 抛出业务异常")
        void getById_notFound() {
            when(applicationRepository.findByIdAndIsDeleted(999L, 0)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> appService.getById(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("应用不存在");
        }
    }

    // ==================== 简易列表 ====================

    @Nested
    @DisplayName("简易列表 listSimple()")
    class ListSimpleTest {

        @Test
        @DisplayName("查询启用中的应用列表")
        void listSimple_normal() {
            ApplicationSimpleVO simpleVO = new ApplicationSimpleVO();
            simpleVO.setId(1L);
            simpleVO.setName("订单中心");
            when(applicationRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(sampleApp));
            when(converter.toSimpleVO(sampleApp)).thenReturn(simpleVO);

            List<ApplicationSimpleVO> result = appService.listSimple();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("订单中心");
        }
    }

    // ==================== 创建应用 ====================

    @Nested
    @DisplayName("创建应用 create()")
    class CreateTest {

        @Test
        @DisplayName("正常创建应用")
        void create_normal() {
            ApplicationCreateCmd cmd = new ApplicationCreateCmd();
            cmd.setName("用户中心");
            cmd.setCode("USER_CENTER");
            cmd.setDescription("用户管理");

            ApplicationVO vo = new ApplicationVO();
            vo.setId(2L);
            vo.setName("用户中心");

            when(applicationRepository.existsByCodeAndIsDeleted("USER_CENTER", 0)).thenReturn(false);
            when(converter.toEntity(cmd)).thenReturn(sampleApp);
            when(applicationRepository.save(any(Application.class))).thenReturn(sampleApp);
            when(converter.toVO(any(Application.class))).thenReturn(vo);

            ApplicationVO result = appService.create(cmd);

            assertThat(result.getName()).isEqualTo("用户中心");
            verify(applicationRepository).save(any(Application.class));
        }

        @Test
        @DisplayName("创建失败 - 编码已存在")
        void create_duplicateCode() {
            ApplicationCreateCmd cmd = new ApplicationCreateCmd();
            cmd.setName("订单中心");
            cmd.setCode("ORDER_CENTER");

            when(applicationRepository.existsByCodeAndIsDeleted("ORDER_CENTER", 0)).thenReturn(true);

            assertThatThrownBy(() -> appService.create(cmd))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("应用编码已存在");
            verify(applicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("创建应用 - version为null时默认1.0.0")
        void create_defaultVersion() {
            ApplicationCreateCmd cmd = new ApplicationCreateCmd();
            cmd.setName("新应用");
            cmd.setCode("NEW_APP");
            // version 未设置

            Application entity = new Application();
            entity.setVersion(null);

            when(applicationRepository.existsByCodeAndIsDeleted("NEW_APP", 0)).thenReturn(false);
            when(converter.toEntity(cmd)).thenReturn(entity);
            when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> {
                Application a = inv.getArgument(0);
                a.setId(10L);
                return a;
            });
            when(converter.toVO(any(Application.class))).thenReturn(new ApplicationVO());

            appService.create(cmd);

            assertThat(entity.getVersion()).isEqualTo("1.0.0");
            assertThat(entity.getStatus()).isEqualTo(1);
            assertThat(entity.getIsDeleted()).isEqualTo(0);
        }
    }

    // ==================== 更新应用 ====================

    @Nested
    @DisplayName("更新应用 update()")
    class UpdateTest {

        @Test
        @DisplayName("正常更新应用")
        void update_normal() {
            ApplicationUpdateCmd cmd = new ApplicationUpdateCmd();
            cmd.setName("订单中心V2");
            cmd.setVersion("2.0.0");
            cmd.setDescription("升级版");

            ApplicationVO vo = new ApplicationVO();
            vo.setName("订单中心V2");

            when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));
            when(applicationRepository.save(any(Application.class))).thenReturn(sampleApp);
            when(converter.toVO(sampleApp)).thenReturn(vo);

            ApplicationVO result = appService.update(1L, cmd);

            assertThat(result.getName()).isEqualTo("订单中心V2");
            assertThat(sampleApp.getName()).isEqualTo("订单中心V2");
            assertThat(sampleApp.getVersion()).isEqualTo("2.0.0");
        }

        @Test
        @DisplayName("更新失败 - 应用不存在")
        void update_notFound() {
            ApplicationUpdateCmd cmd = new ApplicationUpdateCmd();
            cmd.setName("test");

            when(applicationRepository.findByIdAndIsDeleted(999L, 0)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> appService.update(999L, cmd))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("应用不存在");
        }
    }

    // ==================== 删除应用 ====================

    @Nested
    @DisplayName("删除应用 delete()")
    class DeleteTest {

        @Test
        @DisplayName("正常删除应用（软删除）")
        void delete_normal() {
            when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));
            when(microserviceRepository.countByApplicationIdAndIsDeleted(1L, 0)).thenReturn(0L);
            when(metadataRepository.countByApplicationIdAndIsDeleted(1L, 0)).thenReturn(0L);

            appService.delete(1L);

            verify(applicationRepository).softDelete(1L);
        }

        @Test
        @DisplayName("删除失败 - 存在关联微服务")
        void delete_hasMicroservices() {
            when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));
            when(microserviceRepository.countByApplicationIdAndIsDeleted(1L, 0)).thenReturn(3L);
            when(metadataRepository.countByApplicationIdAndIsDeleted(1L, 0)).thenReturn(0L);

            assertThatThrownBy(() -> appService.delete(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("存在关联资源");
            verify(applicationRepository, never()).softDelete(any());
        }

        @Test
        @DisplayName("删除失败 - 存在关联元数据")
        void delete_hasMetadata() {
            when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));
            when(microserviceRepository.countByApplicationIdAndIsDeleted(1L, 0)).thenReturn(0L);
            when(metadataRepository.countByApplicationIdAndIsDeleted(1L, 0)).thenReturn(2L);

            assertThatThrownBy(() -> appService.delete(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("存在关联资源");
            verify(applicationRepository, never()).softDelete(any());
        }
    }

    // ==================== 更新状态 ====================

    @Nested
    @DisplayName("更新状态 updateStatus()")
    class UpdateStatusTest {

        @Test
        @DisplayName("正常停用应用")
        void updateStatus_disable() {
            StatusCmd cmd = new StatusCmd();
            cmd.setStatus(0);

            when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));
            when(applicationRepository.save(any(Application.class))).thenReturn(sampleApp);

            appService.updateStatus(1L, cmd);

            assertThat(sampleApp.getStatus()).isEqualTo(0);
            verify(applicationRepository).save(sampleApp);
        }

        @Test
        @DisplayName("更新状态失败 - 应用不存在")
        void updateStatus_notFound() {
            StatusCmd cmd = new StatusCmd();
            cmd.setStatus(1);

            when(applicationRepository.findByIdAndIsDeleted(999L, 0)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> appService.updateStatus(999L, cmd))
                    .isInstanceOf(BusinessException.class);
        }
    }

    // ==================== 应用下微服务列表 ====================

    @Nested
    @DisplayName("应用下微服务列表 listMicroservices()")
    class ListMicroservicesTest {

        @Test
        @DisplayName("查询应用下的微服务列表")
        void listMicroservices_normal() {
            when(microserviceRepository.findByApplicationIdAndIsDeletedOrderByCreateTimeDesc(1L, 0))
                    .thenReturn(List.of());

            var result = appService.listMicroservices(1L);

            assertThat(result).isNotNull();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> anyList() {
        return org.mockito.ArgumentMatchers.anyList();
    }
}
