package cn.zhangquanyu.model.application;

import cn.zhangquanyu.application.domain.entity.Microservice;
import cn.zhangquanyu.application.domain.repository.MicroserviceRepository;
import cn.zhangquanyu.metadata.domain.repository.MetadataRepository;
import cn.zhangquanyu.model.converter.ModelConverter;
import cn.zhangquanyu.model.domain.entity.Model;
import cn.zhangquanyu.model.domain.entity.ModelField;
import cn.zhangquanyu.model.domain.repository.ModelFieldRepository;
import cn.zhangquanyu.model.domain.repository.ModelIndexRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("模型管理 Service 单元测试")
class ModelAppServiceTest {

    @Mock
    private ModelRepository modelRepository;
    @Mock
    private ModelFieldRepository modelFieldRepository;
    @Mock
    private ModelIndexRepository modelIndexRepository;
    @Mock
    private MicroserviceRepository microserviceRepository;
    @Mock
    private ServiceParamRepository serviceParamRepository;
    @Mock
    private MetadataRepository metadataRepository;
    @Mock
    private ModelConverter converter;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ModelAppService modelService;

    private Model sampleModel;
    private Microservice sampleMs;
    private ModelField sampleField;

    @BeforeEach
    void setUp() {
        sampleMs = new Microservice();
        sampleMs.setId(10L);
        sampleMs.setName("订单服务");
        sampleMs.setApplicationId(1L);

        sampleModel = new Model();
        sampleModel.setId(100L);
        sampleModel.setMicroserviceId(10L);
        sampleModel.setName("订单");
        sampleModel.setCode("Order");
        sampleModel.setDescription("订单模型");
        sampleModel.setIsDeleted(0);
        sampleModel.setCreateTime(LocalDateTime.now());

        sampleField = new ModelField();
        sampleField.setId(200L);
        sampleField.setModelId(100L);
        sampleField.setName("id");
        sampleField.setDisplayName("主键ID");
        sampleField.setFieldType("BIGINT");
        sampleField.setIsPrimary(1);
        sampleField.setIsRequired(1);
        sampleField.setSortOrder(1);
        sampleField.setIsDeleted(0);
    }

    // ==================== 分页查询 ====================

    @Nested
    @DisplayName("分页查询 page()")
    class PageTest {

        @Test
        @DisplayName("正常分页查询 - 含字段数统计")
        void page_normal() {
            ModelPageQuery query = new ModelPageQuery();
            query.setPageNum(1);
            query.setPageSize(10);

            ModelVO vo = new ModelVO();
            vo.setId(100L);
            vo.setName("订单");

            Page<Model> page = new PageImpl<>(List.of(sampleModel), Pageable.ofSize(10), 1);
            when(modelRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(converter.toVO(sampleModel)).thenReturn(vo);
            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(modelFieldRepository.countByModelIdAndIsDeleted(100L, 0)).thenReturn(6L);

            PageResult<ModelVO> result = modelService.page(query);

            assertThat(result.getList()).hasSize(1);
            assertThat(result.getList().get(0).getName()).isEqualTo("订单");
            assertThat(result.getList().get(0).getFieldCount()).isEqualTo(6);
        }

        @Test
        @DisplayName("空数据分页查询")
        void page_empty() {
            ModelPageQuery query = new ModelPageQuery();
            query.setPageNum(1);
            query.setPageSize(10);

            Page<Model> emptyPage = new PageImpl<>(List.of(), Pageable.ofSize(10), 0);
            when(modelRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

            PageResult<ModelVO> result = modelService.page(query);

            assertThat(result.getList()).isEmpty();
            assertThat(result.getTotal()).isEqualTo(0);
        }
    }

    // ==================== 查询详情 ====================

    @Nested
    @DisplayName("查询详情 getById()")
    class GetByIdTest {

        @Test
        @DisplayName("正常查询详情 - 含字段列表")
        void getById_normal() {
            ModelVO vo = new ModelVO();
            vo.setId(100L);
            vo.setName("订单");

            ModelFieldVO fieldVO = new ModelFieldVO();
            fieldVO.setName("id");
            fieldVO.setDisplayName("主键ID");

            when(modelRepository.findByIdAndIsDeleted(100L, 0)).thenReturn(Optional.of(sampleModel));
            when(converter.toVO(sampleModel)).thenReturn(vo);
            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(modelFieldRepository.countByModelIdAndIsDeleted(100L, 0)).thenReturn(6L);
            when(modelFieldRepository.findByModelIdAndIsDeletedOrderBySortOrderAsc(100L, 0))
                    .thenReturn(List.of(sampleField));
            when(modelIndexRepository.findByModelIdAndIsDeletedOrderByIdAsc(100L, 0))
                    .thenReturn(List.of());
            when(converter.toFieldVOList(anyList())).thenReturn(List.of(fieldVO));

            ModelDetailVO result = modelService.getById(100L);

            assertThat(result.getModel().getName()).isEqualTo("订单");
            assertThat(result.getFields()).hasSize(1);
            assertThat(result.getFields().get(0).getName()).isEqualTo("id");
        }

        @Test
        @DisplayName("模型不存在 - 抛出异常")
        void getById_notFound() {
            when(modelRepository.findByIdAndIsDeleted(999L, 0)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> modelService.getById(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("模型不存在");
        }
    }

    // ==================== 按微服务查询列表 ====================

    @Test
    @DisplayName("按微服务查询模型列表 listByMicroservice()")
    void listByMicroservice_normal() {
        ModelSimpleVO simpleVO = new ModelSimpleVO();
        simpleVO.setId(100L);
        simpleVO.setName("订单");

        when(modelRepository.findByMicroserviceIdAndIsDeletedOrderByCreateTimeDesc(10L, 0))
                .thenReturn(List.of(sampleModel));
        when(converter.toSimpleVO(sampleModel)).thenReturn(simpleVO);

        List<ModelSimpleVO> result = modelService.listByMicroservice(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("订单");
    }

    // ==================== 创建模型 ====================

    @Nested
    @DisplayName("创建模型 create()")
    class CreateTest {

        @Test
        @DisplayName("正常创建模型")
        void create_normal() {
            ModelCreateCmd cmd = new ModelCreateCmd();
            cmd.setMicroserviceId(10L);
            cmd.setName("订单");
            cmd.setCode("Order");

            ModelVO vo = new ModelVO();
            vo.setName("订单");

            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(modelRepository.existsByMicroserviceIdAndCodeAndIsDeleted(10L, "Order", 0)).thenReturn(false);
            when(converter.toEntity(cmd)).thenReturn(sampleModel);
            when(modelRepository.save(any(Model.class))).thenReturn(sampleModel);
            when(converter.toVO(sampleModel)).thenReturn(vo);
            when(modelFieldRepository.countByModelIdAndIsDeleted(100L, 0)).thenReturn(0L);

            ModelVO result = modelService.create(cmd);

            assertThat(result.getName()).isEqualTo("订单");
        }

        @Test
        @DisplayName("创建失败 - 微服务不存在")
        void create_msNotFound() {
            ModelCreateCmd cmd = new ModelCreateCmd();
            cmd.setMicroserviceId(999L);
            cmd.setName("test");
            cmd.setCode("Test");

            when(microserviceRepository.findByIdAndIsDeleted(999L, 0)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> modelService.create(cmd))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("微服务不存在");
        }

        @Test
        @DisplayName("创建失败 - 编码已存在")
        void create_duplicateCode() {
            ModelCreateCmd cmd = new ModelCreateCmd();
            cmd.setMicroserviceId(10L);
            cmd.setName("订单");
            cmd.setCode("Order");

            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(modelRepository.existsByMicroserviceIdAndCodeAndIsDeleted(10L, "Order", 0)).thenReturn(true);

            assertThatThrownBy(() -> modelService.create(cmd))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("模型编码在该微服务内已存在");
        }
    }

    // ==================== 更新模型 ====================

    @Test
    @DisplayName("正常更新模型 update()")
    void update_normal() {
        ModelUpdateCmd cmd = new ModelUpdateCmd();
        cmd.setName("订单V2");
        cmd.setDescription("升级版模型");

        ModelVO vo = new ModelVO();
        vo.setName("订单V2");

        when(modelRepository.findByIdAndIsDeleted(100L, 0)).thenReturn(Optional.of(sampleModel));
        when(modelRepository.save(any(Model.class))).thenReturn(sampleModel);
        when(converter.toVO(sampleModel)).thenReturn(vo);
        when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
        when(modelFieldRepository.countByModelIdAndIsDeleted(100L, 0)).thenReturn(6L);

        ModelVO result = modelService.update(100L, cmd);

        assertThat(result.getName()).isEqualTo("订单V2");
        assertThat(sampleModel.getName()).isEqualTo("订单V2");
    }

    // ==================== 删除模型 ====================

    @Nested
    @DisplayName("删除模型 delete()")
    class DeleteTest {

        @Test
        @DisplayName("正常删除模型（级联软删除字段）")
        void delete_normal() {
            when(modelRepository.findByIdAndIsDeleted(100L, 0)).thenReturn(Optional.of(sampleModel));
            when(modelFieldRepository.findByMetadataIdAndIsDeleted(100L, 0)).thenReturn(List.of());
            when(serviceParamRepository.countByModelFieldIn(anyList())).thenReturn(0L);

            modelService.delete(100L);

            verify(modelFieldRepository).softDeleteByModelId(100L);
            verify(modelIndexRepository).softDeleteByModelId(100L);
            verify(modelRepository).softDelete(100L);
        }

        @Test
        @DisplayName("删除失败 - 被服务参数引用")
        void delete_referencedByService() {
            when(modelRepository.findByIdAndIsDeleted(100L, 0)).thenReturn(Optional.of(sampleModel));
            when(modelFieldRepository.findByMetadataIdAndIsDeleted(100L, 0)).thenReturn(List.of(sampleField));
            when(serviceParamRepository.countByModelFieldIn(anyList())).thenReturn(2L);

            assertThatThrownBy(() -> modelService.delete(100L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("模型已被服务参数引用");
            verify(modelRepository, never()).softDelete(any());
        }
    }

    // ==================== 批量保存字段 ====================

    @Nested
    @DisplayName("批量保存字段 batchSaveFields()")
    class BatchSaveFieldsTest {

        @Test
        @DisplayName("正常批量保存字段 - 含新增和删除")
        void batchSave_normal() {
            ModelFieldBatchSaveCmd.FieldItem item = new ModelFieldBatchSaveCmd.FieldItem();
            item.setName("amount");
            item.setDisplayName("金额");
            item.setFieldType("DECIMAL");
            item.setLength(12);
            item.setPrecision(2);
            item.setIsPrimary(1);
            item.setSortOrder(1);

            ModelFieldBatchSaveCmd cmd = new ModelFieldBatchSaveCmd();
            cmd.setFields(List.of(item));
            cmd.setDeletedFieldIds(List.of(999L));

            when(modelRepository.findByIdAndIsDeleted(100L, 0)).thenReturn(Optional.of(sampleModel));
            when(microserviceRepository.findByIdAndIsDeleted(10L, 0)).thenReturn(Optional.of(sampleMs));
            when(modelFieldRepository.save(any(ModelField.class))).thenAnswer(inv -> inv.getArgument(0));
            when(converter.toFieldVOList(anyList())).thenReturn(List.of(new ModelFieldVO()));

            var result = modelService.batchSaveFields(100L, cmd);

            assertThat(result).isNotNull();
            verify(modelFieldRepository).softDelete(999L);
            verify(modelFieldRepository).save(any(ModelField.class));
        }

        @Test
        @DisplayName("批量保存字段 - 模型不存在")
        void batchSave_modelNotFound() {
            ModelFieldBatchSaveCmd cmd = new ModelFieldBatchSaveCmd();
            cmd.setFields(List.of());

            when(modelRepository.findByIdAndIsDeleted(999L, 0)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> modelService.batchSaveFields(999L, cmd))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("模型不存在");
        }
    }
}
