package com.alioth.tutubackend.service;

import com.alioth.tutubackend.model.entity.User;
import com.alioth.tutubackend.model.vo.UserLoginVO;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Alioth
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-12-23 19:20:54
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      请求
     * @return 脱敏后的用户信息
     */
    UserLoginVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    UserLoginVO getUserLoginVO(User user);

    /**
     * 获取加密密码
     *
     * @param password 密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String password);
}
