package cn.zhangquanyu.application.converter;

import cn.zhangquanyu.application.domain.entity.Microservice;
import cn.zhangquanyu.application.dto.cmd.MicroserviceCreateCmd;
import cn.zhangquanyu.application.dto.vo.MicroserviceSimpleVO;
import cn.zhangquanyu.application.dto.vo.MicroserviceVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MicroserviceConverter {

    @Mapping(target = "applicationName", ignore = true)
    MicroserviceVO toVO(Microservice entity);

    MicroserviceSimpleVO toSimpleVO(Microservice entity);

    List<MicroserviceVO> toVOList(List<Microservice> entities);

    Microservice toEntity(MicroserviceCreateCmd cmd);
}
