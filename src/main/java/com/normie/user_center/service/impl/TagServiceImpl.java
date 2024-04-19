package com.normie.user_center.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.normie.user_center.model.Tag;
import com.normie.user_center.service.TagService;
import com.normie.user_center.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author 10377
* @description 针对表【tag】的数据库操作Service实现
* @createDate 2024-04-02 22:30:12
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




