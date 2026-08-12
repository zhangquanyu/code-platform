package cn.zhangquanyu.application.controller;

import cn.zhangquanyu.application.application.ApplicationAppService;
import cn.zhangquanyu.application.dto.cmd.ApplicationCreateCmd;
import cn.zhangquanyu.application.dto.cmd.ApplicationUpdateCmd;
import cn.zhangquanyu.application.dto.cmd.StatusCmd;
import cn.zhangquanyu.application.dto.query.ApplicationPageQuery;
import cn.zhangquanyu.application.dto.vo.ApplicationSimpleVO;
import cn.zhangquanyu.application.dto.vo.ApplicationVO;
import cn.zhangquanyu.application.dto.vo.MicroserviceVO;
import cn.zhangquanyu.application.converter.MicroserviceConverter;
import cn.zhangquanyu.shared.api.PageResult;
import cn.zhangquanyu.shared.api.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationAppService appService;
    private final MicroserviceConverter microserviceConverter;

    @GetMapping
    public Result<PageResult<ApplicationVO>> page(ApplicationPageQuery query) {
        return Result.ok(appService.page(query));
    }

    @GetMapping("/simple")
    public Result<List<ApplicationSimpleVO>> listSimple() {
        return Result.ok(appService.listSimple());
    }

    @GetMapping("/{id}")
    public Result<ApplicationVO> getById(@PathVariable Long id) {
        return Result.ok(appService.getById(id));
    }

    @GetMapping("/{id}/microservices")
    public Result<List<MicroserviceVO>> listMicroservices(@PathVariable Long id) {
        return Result.ok(microserviceConverter.toVOList(appService.listMicroservices(id)));
    }

    @PostMapping
    public Result<ApplicationVO> create(@Valid @RequestBody ApplicationCreateCmd cmd) {
        return Result.ok(appService.create(cmd));
    }

    @PutMapping("/{id}")
    public Result<ApplicationVO> update(@PathVariable Long id,
                                        @Valid @RequestBody ApplicationUpdateCmd cmd) {
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
