package cn.zhangquanyu.service.controller;

import cn.zhangquanyu.service.application.ServiceAppService;
import cn.zhangquanyu.service.dto.cmd.ServiceCreateCmd;
import cn.zhangquanyu.service.dto.query.ServicePageQuery;
import cn.zhangquanyu.service.dto.vo.ServiceDetailVO;
import cn.zhangquanyu.service.dto.vo.ServiceSimpleVO;
import cn.zhangquanyu.service.dto.vo.ServiceVO;
import cn.zhangquanyu.shared.api.PageResult;
import cn.zhangquanyu.shared.api.Result;
import cn.zhangquanyu.shared.api.StatusCmd;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceAppService appService;

    @GetMapping
    public Result<PageResult<ServiceVO>> page(ServicePageQuery query) {
        return Result.ok(appService.page(query));
    }

    @GetMapping("/by-microservice/{microserviceId}")
    public Result<List<ServiceSimpleVO>> listByMicroservice(@PathVariable Long microserviceId) {
        return Result.ok(appService.listByMicroservice(microserviceId));
    }

    @GetMapping("/{id}")
    public Result<ServiceDetailVO> getById(@PathVariable Long id) {
        return Result.ok(appService.getById(id));
    }

    @PostMapping
    public Result<ServiceDetailVO> create(@Valid @RequestBody ServiceCreateCmd cmd) {
        return Result.ok(appService.create(cmd));
    }

    @PutMapping("/{id}")
    public Result<ServiceDetailVO> update(@PathVariable Long id, @Valid @RequestBody ServiceCreateCmd cmd) {
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
