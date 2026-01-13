package com.alioth.tutubackend.service;

import com.alioth.tutubackend.model.dto.space.analyze.SpaceUsageAnalyzeRequest;
import com.alioth.tutubackend.model.entity.User;
import com.alioth.tutubackend.model.vo.space.analyze.SpaceUsageAnalyzeResponse;

public interface SpaceAnalyzeService {
    /**
     * 获取空间使用分析结果
     *
     * @param spaceUsageAnalyzeRequest 空间使用分析请求
     * @return 空间使用分析响应
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);
}
