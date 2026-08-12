package cn.zhangquanyu.service.converter;

import cn.zhangquanyu.service.domain.entity.ServiceDef;
import cn.zhangquanyu.service.domain.entity.ServiceParam;
import cn.zhangquanyu.service.dto.vo.ServiceParamVO;
import cn.zhangquanyu.service.dto.vo.ServiceSimpleVO;
import cn.zhangquanyu.service.dto.vo.ServiceVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ServiceConverter {

    @Mapping(target = "microserviceName", ignore = true)
    @Mapping(target = "inputCount", ignore = true)
    @Mapping(target = "outputCount", ignore = true)
    ServiceVO toVO(ServiceDef entity);

    ServiceParamVO toParamVO(ServiceParam entity);

    List<ServiceParamVO> toParamVOList(List<ServiceParam> entities);

    ServiceSimpleVO toSimpleVO(ServiceDef entity);
}
