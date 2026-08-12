package cn.zhangquanyu.application.converter;

import cn.zhangquanyu.application.domain.entity.Application;
import cn.zhangquanyu.application.dto.cmd.ApplicationCreateCmd;
import cn.zhangquanyu.application.dto.vo.ApplicationSimpleVO;
import cn.zhangquanyu.application.dto.vo.ApplicationVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApplicationConverter {

    ApplicationVO toVO(Application entity);

    ApplicationSimpleVO toSimpleVO(Application entity);

    List<ApplicationVO> toVOList(List<Application> entities);

    Application toEntity(ApplicationCreateCmd cmd);

    void updateEntity(ApplicationCreateCmd cmd, @MappingTarget Application entity);
}
