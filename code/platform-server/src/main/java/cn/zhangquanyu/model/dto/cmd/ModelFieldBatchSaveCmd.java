package cn.zhangquanyu.model.dto.cmd;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ModelFieldBatchSaveCmd {

    @Valid
    @NotNull(message = "字段列表不能为空")
    private List<FieldItem> fields;

    private List<Long> deletedFieldIds;

    @Data
    public static class FieldItem {
        private Long id;
        @NotBlank(message = "字段名不能为空")
        @Size(max = 64)
        private String name;
        @NotBlank(message = "显示名不能为空")
        @Size(max = 128)
        private String displayName;
        @NotBlank(message = "数据类型不能为空")
        private String fieldType;
        private Integer length;
        private Integer precision;
        private Integer isRequired = 0;
        private Integer isPrimary = 0;
        private Integer isIndex = 0;
        private String defaultValue;
        private Long metadataId;
        private Integer sortOrder = 0;
        @Size(max = 512)
        private String fieldComment;
    }
}
