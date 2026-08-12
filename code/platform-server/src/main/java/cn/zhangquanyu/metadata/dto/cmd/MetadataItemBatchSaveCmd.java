package cn.zhangquanyu.metadata.dto.cmd;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class MetadataItemBatchSaveCmd {

    @Valid
    @NotNull(message = "元数据项列表不能为空")
    private List<ItemField> items;

    private List<Long> deletedItemIds;

    @Data
    public static class ItemField {
        private Long id;
        @NotBlank(message = "元数据项编码不能为空")
        @Size(max = 64)
        private String itemCode;
        @NotBlank(message = "元数据项名称不能为空")
        @Size(max = 128)
        private String itemName;
        @Size(max = 256)
        private String itemValue;
        private Integer sortOrder = 0;
        private Integer status = 1;
    }
}
