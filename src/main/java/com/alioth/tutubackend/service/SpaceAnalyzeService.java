package com.alioth.tutubackend.service;

import com.alioth.tutubackend.model.dto.space.analyze.*;
import com.alioth.tutubackend.model.entity.Space;
import com.alioth.tutubackend.model.entity.User;
import com.alioth.tutubackend.model.vo.space.analyze.*;

import java.util.List;

public interface SpaceAnalyzeService {
    /**
     * 获取空间使用分析结果
     *
     * @param spaceUsageAnalyzeRequest 空间使用分析请求
     * @return 空间使用分析响应
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    /**
     * 获取空间分类使用分析结果
     *
     * @param spaceCategoryAnalyzeRequest 空间分类使用分析请求
     * @return 空间分类使用分析响应
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    /**
     * 获取空间标签使用分析结果
     *
     * @param spaceTagAnalyzeRequest 空间标签使用分析请求
     * @return 空间标签使用分析响应
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 获取空间大小使用分析结果
     *
     * @param spaceSizeAnalyzeRequest 空间大小使用分析请求
     * @return 空间大小使用分析响应
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    /**
     * 获取空间用户使用分析结果
     *
     * @param spaceUserAnalyzeRequest 空间用户使用分析请求
     * @return 空间用户使用分析响应
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    /**
     * 获取空间使用排行
     *
     * @param spaceRankAnalyzeRequest 空间使用排行请求
     * @return 空间使用排行响应
     */
    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
