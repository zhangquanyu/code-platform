package cn.zhangquanyu.model.converter;

import cn.zhangquanyu.model.domain.entity.Model;
import cn.zhangquanyu.model.domain.entity.ModelField;
import cn.zhangquanyu.model.dto.cmd.ModelCreateCmd;
import cn.zhangquanyu.model.dto.vo.ModelFieldVO;
import cn.zhangquanyu.model.dto.vo.ModelSimpleVO;
import cn.zhangquanyu.model.dto.vo.ModelVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ModelConverter {

    @Mapping(target = "microserviceName", ignore = true)
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "fieldCount", ignore = true)
    ModelVO toVO(Model entity);

    @Mapping(target = "metadataName", ignore = true)
    ModelFieldVO toFieldVO(ModelField entity);

    List<ModelFieldVO> toFieldVOList(List<ModelField> entities);

    ModelSimpleVO toSimpleVO(Model entity);

    Model toEntity(ModelCreateCmd cmd);
}
