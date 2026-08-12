package cn.zhangquanyu.metadata.controller;

import cn.zhangquanyu.metadata.application.MetadataAppService;
import cn.zhangquanyu.metadata.dto.cmd.MetadataCreateCmd;
import cn.zhangquanyu.metadata.dto.cmd.MetadataItemBatchSaveCmd;
import cn.zhangquanyu.metadata.dto.cmd.MetadataUpdateCmd;
import cn.zhangquanyu.shared.api.StatusCmd;
import cn.zhangquanyu.metadata.dto.query.MetadataPageQuery;
import cn.zhangquanyu.metadata.dto.vo.MetadataDetailVO;
import cn.zhangquanyu.metadata.dto.vo.MetadataItemVO;
import cn.zhangquanyu.metadata.dto.vo.MetadataRefVO;
import cn.zhangquanyu.metadata.dto.vo.MetadataSimpleVO;
import cn.zhangquanyu.metadata.dto.vo.MetadataVO;
import cn.zhangquanyu.shared.api.PageResult;
import cn.zhangquanyu.shared.api.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final MetadataAppService appService;

    @GetMapping
    public Result<PageResult<MetadataVO>> page(MetadataPageQuery query) {
        return Result.ok(appService.page(query));
    }

    @GetMapping("/by-application/{applicationId}")
    public Result<List<MetadataSimpleVO>> listByApplication(@PathVariable Long applicationId) {
        return Result.ok(appService.listByApplication(applicationId));
    }

    @GetMapping("/{id}")
    public Result<MetadataDetailVO> getById(@PathVariable Long id) {
        return Result.ok(appService.getById(id));
    }

    @GetMapping("/{id}/references")
    public Result<List<MetadataRefVO>> listReferences(@PathVariable Long id) {
        return Result.ok(appService.listReferences(id));
    }

    @PostMapping
    public Result<MetadataVO> create(@Valid @RequestBody MetadataCreateCmd cmd) {
        return Result.ok(appService.create(cmd));
    }

    @PutMapping("/{id}")
    public Result<MetadataVO> update(@PathVariable Long id, @Valid @RequestBody MetadataUpdateCmd cmd) {
        return Result.ok(appService.update(id, cmd));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        appService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusCmd cmd) {
        appService.updateStatus(id, cmd);
        return Result.ok();
    }

    @PostMapping("/{id}/items/batch-save")
    public Result<List<MetadataItemVO>> batchSaveItems(@PathVariable Long id,
                                                       @Valid @RequestBody MetadataItemBatchSaveCmd cmd) {
        return Result.ok(appService.batchSaveItems(id, cmd));
    }
}
