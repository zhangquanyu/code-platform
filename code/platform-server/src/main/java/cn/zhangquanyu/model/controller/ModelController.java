package cn.zhangquanyu.model.controller;

import cn.zhangquanyu.model.application.ModelAppService;
import cn.zhangquanyu.model.dto.cmd.ModelCreateCmd;
import cn.zhangquanyu.model.dto.cmd.ModelFieldBatchSaveCmd;
import cn.zhangquanyu.model.dto.cmd.ModelIndexBatchSaveCmd;
import cn.zhangquanyu.model.dto.cmd.ModelUpdateCmd;
import cn.zhangquanyu.model.dto.query.ModelPageQuery;
import cn.zhangquanyu.model.dto.vo.ModelDetailVO;
import cn.zhangquanyu.model.dto.vo.ModelFieldVO;
import cn.zhangquanyu.model.dto.vo.ModelIndexVO;
import cn.zhangquanyu.model.dto.vo.ModelSimpleVO;
import cn.zhangquanyu.model.dto.vo.ModelVO;
import cn.zhangquanyu.shared.api.PageResult;
import cn.zhangquanyu.shared.api.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
public class ModelController {

    private final ModelAppService appService;

    @GetMapping
    public Result<PageResult<ModelVO>> page(ModelPageQuery query) {
        return Result.ok(appService.page(query));
    }

    @GetMapping("/by-microservice/{microserviceId}")
    public Result<List<ModelSimpleVO>> listByMicroservice(@PathVariable Long microserviceId) {
        return Result.ok(appService.listByMicroservice(microserviceId));
    }

    @GetMapping("/{id}")
    public Result<ModelDetailVO> getById(@PathVariable Long id) {
        return Result.ok(appService.getById(id));
    }

    @PostMapping
    public Result<ModelVO> create(@Valid @RequestBody ModelCreateCmd cmd) {
        return Result.ok(appService.create(cmd));
    }

    @PutMapping("/{id}")
    public Result<ModelVO> update(@PathVariable Long id, @Valid @RequestBody ModelUpdateCmd cmd) {
        return Result.ok(appService.update(id, cmd));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        appService.delete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/fields/batch-save")
    public Result<List<ModelFieldVO>> batchSaveFields(@PathVariable Long id,
                                                      @Valid @RequestBody ModelFieldBatchSaveCmd cmd) {
        return Result.ok(appService.batchSaveFields(id, cmd));
    }

    @PostMapping("/{id}/indexes/batch-save")
    public Result<List<ModelIndexVO>> batchSaveIndexes(@PathVariable Long id,
                                                        @Valid @RequestBody ModelIndexBatchSaveCmd cmd) {
        return Result.ok(appService.batchSaveIndexes(id, cmd));
    }
}
