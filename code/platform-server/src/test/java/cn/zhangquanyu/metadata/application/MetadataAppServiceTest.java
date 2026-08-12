package cn.zhangquanyu.metadata.application;

import cn.zhangquanyu.application.domain.entity.Application;
import cn.zhangquanyu.application.domain.repository.ApplicationRepository;
import cn.zhangquanyu.metadata.converter.MetadataConverter;
import cn.zhangquanyu.metadata.domain.entity.Metadata;
import cn.zhangquanyu.metadata.domain.entity.MetadataItem;
import cn.zhangquanyu.metadata.domain.repository.MetadataItemRepository;
import cn.zhangquanyu.metadata.domain.repository.MetadataRepository;
import cn.zhangquanyu.metadata.dto.cmd.MetadataCreateCmd;
import cn.zhangquanyu.metadata.dto.cmd.MetadataItemBatchSaveCmd;
import cn.zhangquanyu.metadata.dto.cmd.MetadataUpdateCmd;
import cn.zhangquanyu.metadata.dto.query.MetadataPageQuery;
import cn.zhangquanyu.metadata.dto.vo.MetadataDetailVO;
import cn.zhangquanyu.metadata.dto.vo.MetadataItemVO;
import cn.zhangquanyu.metadata.dto.vo.MetadataSimpleVO;
import cn.zhangquanyu.metadata.dto.vo.MetadataVO;
import cn.zhangquanyu.model.domain.entity.ModelField;
import cn.zhangquanyu.model.domain.repository.ModelFieldRepository;
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
@DisplayName("元数据管理 Service 单元测试")
class MetadataAppServiceTest {

    @Mock
    private MetadataRepository metadataRepository;
    @Mock
    private MetadataItemRepository metadataItemRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private ModelFieldRepository modelFieldRepository;
    @Mock
    private MetadataConverter converter;

    @InjectMocks
    private MetadataAppService metadataService;

    private Metadata sampleMeta;
    private Application sampleApp;
    private MetadataItem sampleItem;

    @BeforeEach
    void setUp() {
        sampleApp = new Application();
        sampleApp.setId(1L);
        sampleApp.setName("订单中心");

        sampleMeta = new Metadata();
        sampleMeta.setId(50L);
        sampleMeta.setApplicationId(1L);
        sampleMeta.setName("订单状态");
        sampleMeta.setCode("ORDER_STATUS");
        sampleMeta.setDescription("订单生命周期状态");
        sampleMeta.setStatus(1);
        sampleMeta.setIsDeleted(0);
        sampleMeta.setCreateTime(LocalDateTime.now());

        sampleItem = new MetadataItem();
        sampleItem.setId(60L);
        sampleItem.setMetadataId(50L);
        sampleItem.setItemCode("PENDING");
        sampleItem.setItemName("待支付");
        sampleItem.setItemValue("0");
        sampleItem.setSortOrder(1);
        sampleItem.setStatus(1);
        sampleItem.setIsDeleted(0);
    }

    // ==================== 分页查询 ====================

    @Nested
    @DisplayName("分页查询 page()")
    class PageTest {

        @Test
        @DisplayName("正常分页查询 - 含项数统计")
        void page_normal() {
            MetadataPageQuery query = new MetadataPageQuery();
            query.setPageNum(1);
            query.setPageSize(10);

            MetadataVO vo = new MetadataVO();
            vo.setId(50L);
            vo.setName("订单状态");

            Page<Metadata> page = new PageImpl<>(List.of(sampleMeta), Pageable.ofSize(10), 1);
            when(metadataRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(converter.toVO(sampleMeta)).thenReturn(vo);
            when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));
            when(metadataItemRepository.countByMetadataIdAndIsDeleted(50L, 0)).thenReturn(5L);

            PageResult<MetadataVO> result = metadataService.page(query);

            assertThat(result.getList()).hasSize(1);
            assertThat(result.getList().get(0).getName()).isEqualTo("订单状态");
            assertThat(result.getList().get(0).getItemCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("按应用ID筛选分页查询")
        void page_byApplicationId() {
            MetadataPageQuery query = new MetadataPageQuery();
            query.setPageNum(1);
            query.setPageSize(10);
            query.setApplicationId(1L);

            Page<Metadata> page = new PageImpl<>(List.of(sampleMeta), Pageable.ofSize(10), 1);
            when(metadataRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(converter.toVO(sampleMeta)).thenReturn(new MetadataVO());
            when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));
            when(metadataItemRepository.countByMetadataIdAndIsDeleted(50L, 0)).thenReturn(3L);

            PageResult<MetadataVO> result = metadataService.page(query);

            assertThat(result.getList()).hasSize(1);
        }
    }

    // ==================== 按应用查询列表 ====================

    @Test
    @DisplayName("按应用查询启用元数据列表 listByApplication()")
    void listByApplication_normal() {
        MetadataSimpleVO simpleVO = new MetadataSimpleVO();
        simpleVO.setId(50L);
        simpleVO.setName("订单状态");

        when(metadataRepository.findByApplicationIdAndIsDeletedAndStatusOrderByCreateTimeDesc(1L, 0, 1))
                .thenReturn(List.of(sampleMeta));
        when(converter.toSimpleVO(sampleMeta)).thenReturn(simpleVO);

        List<MetadataSimpleVO> result = metadataService.listByApplication(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("订单状态");
    }

    // ==================== 查询详情 ====================

    @Nested
    @DisplayName("查询详情 getById()")
    class GetByIdTest {

        @Test
        @DisplayName("正常查询详情 - 含枚举项和引用")
        void getById_normal() {
            MetadataVO vo = new MetadataVO();
            vo.setId(50L);
            vo.setName("订单状态");

            MetadataItemVO itemVO = new MetadataItemVO();
            itemVO.setItemCode("PENDING");
            itemVO.setItemName("待支付");

            when(metadataRepository.findByIdAndIsDeleted(50L, 0)).thenReturn(Optional.of(sampleMeta));
            when(converter.toVO(sampleMeta)).thenReturn(vo);
            when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));
            when(metadataItemRepository.countByMetadataIdAndIsDeleted(50L, 0)).thenReturn(5L);
            when(metadataItemRepository.findByMetadataIdAndIsDeletedOrderBySortOrderAsc(50L, 0))
                    .thenReturn(List.of(sampleItem));
            when(converter.toItemVOList(anyList())).thenReturn(List.of(itemVO));
            when(modelFieldRepository.findByMetadataIdAndIsDeleted(50L, 0)).thenReturn(List.of());

            MetadataDetailVO result = metadataService.getById(50L);

            assertThat(result.getMetadata().getName()).isEqualTo("订单状态");
            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getItems().get(0).getItemName()).isEqualTo("待支付");
        }

        @Test
        @DisplayName("元数据不存在 - 抛出异常")
        void getById_notFound() {
            when(metadataRepository.findByIdAndIsDeleted(999L, 0)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> metadataService.getById(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("元数据不存在");
        }
    }

    // ==================== 创建元数据 ====================

    @Nested
    @DisplayName("创建元数据 create()")
    class CreateTest {

        @Test
        @DisplayName("正常创建元数据")
        void create_normal() {
            MetadataCreateCmd cmd = new MetadataCreateCmd();
            cmd.setApplicationId(1L);
            cmd.setName("支付方式");
            cmd.setCode("PAY_TYPE");

            MetadataVO vo = new MetadataVO();
            vo.setName("支付方式");

            when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));
            when(metadataRepository.existsByApplicationIdAndCodeAndIsDeleted(1L, "PAY_TYPE", 0)).thenReturn(false);
            when(converter.toEntity(cmd)).thenReturn(sampleMeta);
            when(metadataRepository.save(any(Metadata.class))).thenReturn(sampleMeta);
            when(converter.toVO(sampleMeta)).thenReturn(vo);
            when(metadataItemRepository.countByMetadataIdAndIsDeleted(50L, 0)).thenReturn(0L);

            MetadataVO result = metadataService.create(cmd);

            assertThat(result.getName()).isEqualTo("支付方式");
        }

        @Test
        @DisplayName("创建失败 - 应用不存在")
        void create_appNotFound() {
            MetadataCreateCmd cmd = new MetadataCreateCmd();
            cmd.setApplicationId(999L);
            cmd.setName("test");
            cmd.setCode("TEST");

            when(applicationRepository.findByIdAndIsDeleted(999L, 0)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> metadataService.create(cmd))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("应用不存在");
        }

        @Test
        @DisplayName("创建失败 - 编码已存在")
        void create_duplicateCode() {
            MetadataCreateCmd cmd = new MetadataCreateCmd();
            cmd.setApplicationId(1L);
            cmd.setName("订单状态");
            cmd.setCode("ORDER_STATUS");

            when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));
            when(metadataRepository.existsByApplicationIdAndCodeAndIsDeleted(1L, "ORDER_STATUS", 0)).thenReturn(true);

            assertThatThrownBy(() -> metadataService.create(cmd))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("元数据编码在该应用内已存在");
        }
    }

    // ==================== 更新元数据 ====================

    @Test
    @DisplayName("正常更新元数据 update()")
    void update_normal() {
        MetadataUpdateCmd cmd = new MetadataUpdateCmd();
        cmd.setName("订单状态V2");
        cmd.setDescription("更新描述");

        MetadataVO vo = new MetadataVO();
        vo.setName("订单状态V2");

        when(metadataRepository.findByIdAndIsDeleted(50L, 0)).thenReturn(Optional.of(sampleMeta));
        when(metadataRepository.save(any(Metadata.class))).thenReturn(sampleMeta);
        when(converter.toVO(sampleMeta)).thenReturn(vo);
        when(applicationRepository.findByIdAndIsDeleted(1L, 0)).thenReturn(Optional.of(sampleApp));
        when(metadataItemRepository.countByMetadataIdAndIsDeleted(50L, 0)).thenReturn(5L);

        MetadataVO result = metadataService.update(50L, cmd);

        assertThat(result.getName()).isEqualTo("订单状态V2");
        assertThat(sampleMeta.getName()).isEqualTo("订单状态V2");
    }

    // ==================== 删除元数据 ====================

    @Nested
    @DisplayName("删除元数据 delete()")
    class DeleteTest {

        @Test
        @DisplayName("正常删除元数据（软删除）")
        void delete_normal() {
            when(metadataRepository.findByIdAndIsDeleted(50L, 0)).thenReturn(Optional.of(sampleMeta));
            when(modelFieldRepository.countByMetadataIdAndIsDeleted(50L, 0)).thenReturn(0L);

            metadataService.delete(50L);

            verify(metadataRepository).softDelete(50L);
        }

        @Test
        @DisplayName("删除失败 - 被模型字段引用")
        void delete_referencedByModel() {
            when(metadataRepository.findByIdAndIsDeleted(50L, 0)).thenReturn(Optional.of(sampleMeta));
            when(modelFieldRepository.countByMetadataIdAndIsDeleted(50L, 0)).thenReturn(3L);

            assertThatThrownBy(() -> metadataService.delete(50L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("元数据已被模型字段引用");
            verify(metadataRepository, never()).softDelete(any());
        }
    }

    // ==================== 更新状态 ====================

    @Test
    @DisplayName("更新元数据状态 updateStatus()")
    void updateStatus_normal() {
        StatusCmd cmd = new StatusCmd();
        cmd.setStatus(0);

        when(metadataRepository.findByIdAndIsDeleted(50L, 0)).thenReturn(Optional.of(sampleMeta));
        when(metadataRepository.save(any(Metadata.class))).thenReturn(sampleMeta);

        metadataService.updateStatus(50L, cmd);

        assertThat(sampleMeta.getStatus()).isEqualTo(0);
    }

    // ==================== 批量保存枚举项 ====================

    @Nested
    @DisplayName("批量保存枚举项 batchSaveItems()")
    class BatchSaveItemsTest {

        @Test
        @DisplayName("正常批量保存枚举项 - 含新增和删除")
        void batchSave_normal() {
            MetadataItemBatchSaveCmd.ItemField item = new MetadataItemBatchSaveCmd.ItemField();
            item.setItemCode("SHIPPED");
            item.setItemName("已发货");
            item.setItemValue("2");
            item.setSortOrder(3);

            MetadataItemBatchSaveCmd cmd = new MetadataItemBatchSaveCmd();
            cmd.setItems(List.of(item));
            cmd.setDeletedItemIds(List.of(999L));

            when(metadataRepository.findByIdAndIsDeleted(50L, 0)).thenReturn(Optional.of(sampleMeta));
            when(metadataItemRepository.save(any(MetadataItem.class))).thenAnswer(inv -> inv.getArgument(0));
            when(converter.toItemVOList(anyList())).thenReturn(List.of(new MetadataItemVO()));

            var result = metadataService.batchSaveItems(50L, cmd);

            assertThat(result).isNotNull();
            verify(metadataItemRepository).softDelete(999L);
            verify(metadataItemRepository).save(any(MetadataItem.class));
        }

        @Test
        @DisplayName("批量保存失败 - 元数据不存在")
        void batchSave_notFound() {
            MetadataItemBatchSaveCmd cmd = new MetadataItemBatchSaveCmd();
            cmd.setItems(List.of());

            when(metadataRepository.findByIdAndIsDeleted(999L, 0)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> metadataService.batchSaveItems(999L, cmd))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("元数据不存在");
        }
    }
}
