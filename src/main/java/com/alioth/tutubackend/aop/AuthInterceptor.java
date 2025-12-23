package com.alioth.tutubackend.aop;

import com.alioth.tutubackend.annotation.AuthCheck;
import com.alioth.tutubackend.exception.ErrorCode;
import com.alioth.tutubackend.exception.ThrowUtils;
import com.alioth.tutubackend.model.entity.User;
import com.alioth.tutubackend.model.enums.UserRoleEnum;
import com.alioth.tutubackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 拦截所有需要权限的接口
     *
     * @param joinPoint 切点
     * @param authCheck 权限注解
     * @return 拦截结果
     * @throws Throwable 抛出异常
     */
    @Around("@annotation(authCheck)")
    public Object interceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        // * 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        // * 不需要权限则放行
        if (mustUserRoleEnum == null) {
            return joinPoint.proceed();
        }
        // * 必须是管理员或者普通用户才允许操作
        UserRoleEnum loginUserRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // * 没有权限不允许访问
        ThrowUtils.throwIf(loginUserRoleEnum == null, ErrorCode.NO_AUTH_ERROR);
        // * 必须是管理员
        ThrowUtils.throwIf(UserRoleEnum.ADMIN.equals(mustUserRoleEnum) && !UserRoleEnum.ADMIN.equals(loginUserRoleEnum), ErrorCode.NO_AUTH_ERROR);
        return joinPoint.proceed();
    }
}
