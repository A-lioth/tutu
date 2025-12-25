package com.alioth.tutubackend.service;

import com.alioth.tutubackend.model.dto.user.UserAddRequest;
import com.alioth.tutubackend.model.dto.user.UserQueryRequest;
import com.alioth.tutubackend.model.entity.User;
import com.alioth.tutubackend.model.vo.UserLoginVO;
import com.alioth.tutubackend.model.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
     * 获取脱敏后的登录用户信息
     *
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    UserLoginVO getLoginUserVO(User user);

    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户信息列表
     *
     * @param userList 用户信息列表
     * @return 脱敏后的用户信息列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取加密密码
     *
     * @param password 密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String password);

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户登出
     *
     * @param request 请求
     * @return 登出结果
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 添加用户
     *
     * @param userAddRequest 用户添加请求
     * @return 新用户 id
     */
    long addUser(UserAddRequest userAddRequest);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 用户查询请求
     * @return 查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 分页获取用户列表
     *
     * @param userPage 分页参数
     * @return 用户列表
     */
    Page<UserVO> listUserVOByPage(Page<User> userPage);

    /**
     * 是否为管理员
     *
     * @param user 用户信息
     * @return 是否为管理员
     */
    boolean isAdmin(User user);
}
