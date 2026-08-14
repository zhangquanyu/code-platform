package cn.zhangquanyu.model.dto.cmd;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ModelIndexBatchSaveCmd {

    @Valid
    @NotEmpty(message = "索引列表不能为空")
    private List<IndexItem> indexes;

    private List<Long> deletedIndexIds;

    @Data
    public static class IndexItem {
        private Long id;
        @NotBlank(message = "索引名称不能为空")
        @Size(max = 64)
        private String indexName;
        @NotBlank(message = "索引类型不能为空")
        private String indexType;
        @NotEmpty(message = "索引字段不能为空")
        private List<Long> fieldIds;
    }
}
