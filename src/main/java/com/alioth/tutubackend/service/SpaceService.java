package com.alioth.tutubackend.service;

import com.alioth.tutubackend.model.dto.space.SpaceAddRequest;
import com.alioth.tutubackend.model.dto.space.SpaceQueryRequest;
import com.alioth.tutubackend.model.entity.Space;
import com.alioth.tutubackend.model.entity.User;
import com.alioth.tutubackend.model.vo.SpaceVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Alioth
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2026-01-05 19:35:28
 */
public interface SpaceService extends IService<Space> {

    /**
     * 创建空间
     *
     * @param spaceAddRequest 创建空间请求
     * @param loginUser       登录用户
     * @return 创建的空间 ID
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 获取查询条件
     *
     * @param spaceQueryRequest 空间查询请求
     * @return 查询条件
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取空间信息包装类
     *
     * @param space 空间信息
     * @return 空间信息包装类
     */
    SpaceVO getSpaceVO(Space space);

    /**
     * 分页获取空间列表包装类
     *
     * @param spacePage 空间列表
     * @return 空间列表包装类
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage);

    /**
     * 校验空间信息
     *
     * @param space 空间信息
     * @param add   是否为创建校验
     */
    void validSpace(Space space, boolean add);

    /**
     * 根据空间级别填充空间信息
     *
     * @param space 空间信息
     */
    void fillSpaceBySpaceLevel(Space space);
}
