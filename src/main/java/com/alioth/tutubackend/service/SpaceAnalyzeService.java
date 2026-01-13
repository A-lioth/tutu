package com.alioth.tutubackend.service;

import com.alioth.tutubackend.model.dto.space.analyze.SpaceCategoryAnalyzeRequest;
import com.alioth.tutubackend.model.dto.space.analyze.SpaceTagAnalyzeRequest;
import com.alioth.tutubackend.model.dto.space.analyze.SpaceUsageAnalyzeRequest;
import com.alioth.tutubackend.model.entity.User;
import com.alioth.tutubackend.model.vo.space.analyze.SpaceCategoryAnalyzeResponse;
import com.alioth.tutubackend.model.vo.space.analyze.SpaceTagAnalyzeResponse;
import com.alioth.tutubackend.model.vo.space.analyze.SpaceUsageAnalyzeResponse;

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
}
