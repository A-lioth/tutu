package com.alioth.tutubackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alioth.tutubackend.model.entity.User;
import com.alioth.tutubackend.service.UserService;
import com.alioth.tutubackend.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author Alioth
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-12-23 19:20:54
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




