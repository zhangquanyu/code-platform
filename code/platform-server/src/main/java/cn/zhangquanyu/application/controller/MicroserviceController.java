package cn.zhangquanyu.application.controller;

import cn.zhangquanyu.application.application.MicroserviceAppService;
import cn.zhangquanyu.application.dto.cmd.MicroserviceCreateCmd;
import cn.zhangquanyu.application.dto.cmd.MicroserviceUpdateCmd;
import cn.zhangquanyu.application.dto.cmd.StatusCmd;
import cn.zhangquanyu.application.dto.query.MicroservicePageQuery;
import cn.zhangquanyu.application.dto.vo.MicroserviceSimpleVO;
import cn.zhangquanyu.application.dto.vo.MicroserviceSummaryVO;
import cn.zhangquanyu.application.dto.vo.MicroserviceVO;
import cn.zhangquanyu.shared.api.PageResult;
import cn.zhangquanyu.shared.api.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/microservices")
@RequiredArgsConstructor
public class MicroserviceController {

    private final MicroserviceAppService appService;

    @GetMapping
    public Result<PageResult<MicroserviceVO>> page(MicroservicePageQuery query) {
        return Result.ok(appService.page(query));
    }

    @GetMapping("/by-application/{applicationId}")
    public Result<List<MicroserviceSimpleVO>> listByApplication(@PathVariable Long applicationId) {
        return Result.ok(appService.listByApplication(applicationId));
    }

    @GetMapping("/{id}")
    public Result<MicroserviceVO> getById(@PathVariable Long id) {
        return Result.ok(appService.getById(id));
    }

    @GetMapping("/{id}/summary")
    public Result<MicroserviceSummaryVO> summary(@PathVariable Long id) {
        return Result.ok(appService.summary(id));
    }

    @PostMapping
    public Result<MicroserviceVO> create(@Valid @RequestBody MicroserviceCreateCmd cmd) {
        return Result.ok(appService.create(cmd));
    }

    @PutMapping("/{id}")
    public Result<MicroserviceVO> update(@PathVariable Long id,
                                         @Valid @RequestBody MicroserviceUpdateCmd cmd) {
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
}
