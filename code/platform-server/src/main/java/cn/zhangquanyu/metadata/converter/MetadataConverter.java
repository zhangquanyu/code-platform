package cn.zhangquanyu.metadata.converter;

import cn.zhangquanyu.metadata.domain.entity.Metadata;
import cn.zhangquanyu.metadata.domain.entity.MetadataItem;
import cn.zhangquanyu.metadata.dto.cmd.MetadataCreateCmd;
import cn.zhangquanyu.metadata.dto.vo.MetadataItemVO;
import cn.zhangquanyu.metadata.dto.vo.MetadataSimpleVO;
import cn.zhangquanyu.metadata.dto.vo.MetadataVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MetadataConverter {

    @Mapping(target = "applicationName", ignore = true)
    @Mapping(target = "itemCount", ignore = true)
    MetadataVO toVO(Metadata entity);

    MetadataItemVO toItemVO(MetadataItem entity);

    List<MetadataItemVO> toItemVOList(List<MetadataItem> entities);

    MetadataSimpleVO toSimpleVO(Metadata entity);

    Metadata toEntity(MetadataCreateCmd cmd);
}
