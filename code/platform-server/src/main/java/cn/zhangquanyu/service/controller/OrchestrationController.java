package cn.zhangquanyu.service.controller;

import cn.zhangquanyu.service.application.OrchestrationAppService;
import cn.zhangquanyu.service.dto.cmd.OrchDebugCmd;
import cn.zhangquanyu.service.dto.cmd.OrchestrationCreateCmd;
import cn.zhangquanyu.service.dto.cmd.OrchestrationUpdateCmd;
import cn.zhangquanyu.service.dto.query.OrchestrationPageQuery;
import cn.zhangquanyu.service.dto.vo.OrchDebugResultVO;
import cn.zhangquanyu.service.dto.vo.OrchHealthVO;
import cn.zhangquanyu.service.dto.vo.OrchestrationDetailVO;
import cn.zhangquanyu.service.dto.vo.OrchestrationVO;
import cn.zhangquanyu.shared.api.PageResult;
import cn.zhangquanyu.shared.api.Result;
import cn.zhangquanyu.shared.api.StatusCmd;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orchestrations")
@RequiredArgsConstructor
public class OrchestrationController {

    private final OrchestrationAppService appService;

    @GetMapping
    public Result<PageResult<OrchestrationVO>> page(OrchestrationPageQuery query) {
        return Result.ok(appService.page(query));
    }

    @GetMapping("/{id}")
    public Result<OrchestrationDetailVO> getById(@PathVariable Long id) {
        return Result.ok(appService.getById(id));
    }

    @GetMapping("/{id}/health")
    public Result<OrchHealthVO> health(@PathVariable Long id) {
        return Result.ok(appService.health(id));
    }

    @PostMapping
    public Result<OrchestrationDetailVO> create(@Valid @RequestBody OrchestrationCreateCmd cmd) {
        return Result.ok(appService.create(cmd));
    }

    @PutMapping("/{id}")
    public Result<OrchestrationDetailVO> update(@PathVariable Long id,
                                                @Valid @RequestBody OrchestrationUpdateCmd cmd) {
        return Result.ok(appService.update(id, cmd));
    }

    @PostMapping("/{id}/validate")
    public Result<List<String>> validate(@PathVariable Long id,
                                         @RequestBody OrchestrationUpdateCmd cmd) {
        return Result.ok(appService.validate(id, cmd.getNodes(), cmd.getEdges()));
    }

    @PostMapping("/{id}/debug")
    public Result<OrchDebugResultVO> debug(@PathVariable Long id,
                                           @RequestBody OrchDebugCmd cmd) {
        return Result.ok(appService.debug(id, cmd));
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
}
