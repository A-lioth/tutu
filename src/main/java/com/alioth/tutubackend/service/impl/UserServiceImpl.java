package com.alioth.tutubackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alioth.tutubackend.constant.UserConstant;
import com.alioth.tutubackend.exception.ErrorCode;
import com.alioth.tutubackend.exception.ThrowUtils;
import com.alioth.tutubackend.mapper.UserMapper;
import com.alioth.tutubackend.model.entity.User;
import com.alioth.tutubackend.model.enums.UserRoleEnum;
import com.alioth.tutubackend.model.vo.UserLoginVO;
import com.alioth.tutubackend.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Alioth
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-12-23 19:20:54
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @return 用户 ID
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // * 检验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "参数错误");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账户过短");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        // * 加密密码
        String encryptPassword = getEncryptPassword(userPassword);
        // * 是否重复
        Long count = lambdaQuery()
                .eq(User::getUserAccount, userAccount)
                .count();
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "用户已存在");
        // * 保存用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = save(user);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "保存用户失败");
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      请求
     * @return 脱敏后的用户信息
     */
    @Override
    public UserLoginVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // * 检验参数
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "参数错误");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "账户错误");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAMS_ERROR, "密码错误");
        // * 加密密码
        String encryptPassword = getEncryptPassword(userPassword);
        // * 是否存在
        User user = lambdaQuery()
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptPassword)
                .one();
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        // * 保存用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return getUserLoginVO(user);
    }

    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    @Override
    public UserLoginVO getUserLoginVO(User user) {
        // * 登录用户是否为空
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
        return userLoginVO;
    }

    /**
     * 获取加密密码
     *
     * @param userPassword 密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // * 加盐
        final String SALT = "tutu";
        return DigestUtils.md5DigestAsHex((userPassword + SALT).getBytes());
    }
}




