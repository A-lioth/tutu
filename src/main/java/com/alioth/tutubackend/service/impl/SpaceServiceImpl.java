package com.alioth.tutubackend.service.impl;

import com.alioth.tutubackend.model.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alioth.tutubackend.model.entity.Space;
import com.alioth.tutubackend.mapper.SpaceMapper;
import org.springframework.stereotype.Service;

/**
* @author Alioth
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2026-01-05 19:35:28
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements UserVO.SpaceService {

}
