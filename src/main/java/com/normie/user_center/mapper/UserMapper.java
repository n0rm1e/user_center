package com.normie.user_center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.normie.user_center.model.User;
import org.mapstruct.Mapper;

/**
* @author 10377
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2024-03-08 20:23:59
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




