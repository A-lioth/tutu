package com.alioth.tutubackend.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.alioth.tutubackend.exception.BusinessException;
import com.alioth.tutubackend.exception.ErrorCode;
import com.alioth.tutubackend.exception.ThrowUtils;
import com.alioth.tutubackend.mapper.SpaceMapper;
import com.alioth.tutubackend.model.dto.space.analyze.*;
import com.alioth.tutubackend.model.entity.Picture;
import com.alioth.tutubackend.model.entity.Space;
import com.alioth.tutubackend.model.entity.User;
import com.alioth.tutubackend.model.vo.space.analyze.*;
import com.alioth.tutubackend.service.PictureService;
import com.alioth.tutubackend.service.SpaceAnalyzeService;
import com.alioth.tutubackend.service.SpaceService;
import com.alioth.tutubackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Alioth
 * @createDate 2026-1-13 17:32:05
 */
@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceAnalyzeService {

    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;
    @Resource
    private PictureService pictureService;

    /**
     * 获取空间分析结果
     *
     * @param spaceUsageAnalyzeRequest 空间使用分析请求
     * @return 空间使用分析响应
     */
    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser) {
        // * 校验参数
        // * 全空间或公共空间分析，从 Picture 表查询
        if (spaceUsageAnalyzeRequest.isQueryAll() || spaceUsageAnalyzeRequest.isQueryPublic()) {
            // * 校验权限
            checkSpaceAnalyzeAuth(spaceUsageAnalyzeRequest, loginUser);
            // * 查询空间使用情况
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("picSize");
            fillAnalyzeQueryWrapper(spaceUsageAnalyzeRequest, queryWrapper);
            List<Object> pictureObjList = pictureService.getBaseMapper().selectObjs(queryWrapper);
            long totalSize = pictureObjList.stream().mapToLong(obj -> (Long) obj).sum();
            long totalCount = pictureObjList.size();
            // * 封装返回结果
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(totalSize);
            spaceUsageAnalyzeResponse.setUsedCount(totalCount);
            // * 公共图库，无上限
            spaceUsageAnalyzeResponse.setMaxSize(null);
            spaceUsageAnalyzeResponse.setMaxCount(null);
            spaceUsageAnalyzeResponse.setSizeUsageRatio(null);
            spaceUsageAnalyzeResponse.setCountUsageRatio(null);
            return spaceUsageAnalyzeResponse;
        } else {
            // * 私有空间，从 space 表中获取空间信息
            Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // * 校验权限
            checkSpaceAnalyzeAuth(spaceUsageAnalyzeRequest, loginUser);
            // * 封装返回结果
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(space.getTotalSize());
            spaceUsageAnalyzeResponse.setUsedCount(space.getTotalCount());
            spaceUsageAnalyzeResponse.setMaxSize(space.getMaxSize());
            spaceUsageAnalyzeResponse.setMaxCount(space.getMaxCount());
            double sizeUsageRatio = NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue();
            spaceUsageAnalyzeResponse.setSizeUsageRatio(sizeUsageRatio);
            double countUsageRatio = NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue();
            spaceUsageAnalyzeResponse.setCountUsageRatio(countUsageRatio);
            return spaceUsageAnalyzeResponse;
        }
    }

    /**
     * 获取空间分类分析结果
     *
     * @param spaceCategoryAnalyzeRequest 空间分类分析请求
     * @param loginUser                   登录用户
     */
    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // * 检查权限
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest, loginUser);
        // * 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // * 根据分析范围补充查询条件
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);
        // * 使用 MyBatis-Plus 分组查询
        // * select category as category, count(*) as count, sum(picSize) as totalSize from picture group by category
        queryWrapper.select("category as category", "count(*) as count", "sum(picSize) as totalSize").groupBy("category");
        // * 查询并转换结果
        return pictureService.getBaseMapper().selectMaps(queryWrapper).stream().map(result -> {
            String category = result.get("category") != null ? result.get("category").toString() : "未分类";
            Long count = ((Number) result.get("count")).longValue();
            Long totalSize = ((Number) result.get("totalSize")).longValue();
            return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
        }).collect(Collectors.toList());
    }

    /**
     * 获取空间标签分析结果
     *
     * @param spaceTagAnalyzeRequest 空间标签分析请求
     * @return 空间标签分析响应
     */
    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser) {
        // * 校验参数
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // * 校验权限
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);
        // * 获取查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);
        queryWrapper.select("tags");
        List<Object> tagsJsonList = pictureService.getBaseMapper().selectObjs(queryWrapper).stream().filter(ObjUtil::isNotNull).map(Object::toString).collect(Collectors.toList());
        // * 获取标签并统计
        Map<String, Long> tagCountMap = tagsJsonList.stream().flatMap(tagsJson -> JSONUtil.toList(tagsJson.toString(), String.class).stream()).collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));
        // * 封装响应结果，按照标签使用次数降序排序
        return tagCountMap.entrySet().stream().sorted((entry1, entry2) -> Long.compare(entry2.getValue(), entry1.getValue())).map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    /**
     * 获取空间大小分析结果
     *
     * @param spaceSizeAnalyzeRequest 空间大小分析请求
     * @param loginUser               登录用户
     * @return 空间大小分析结果
     */
    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // * 检查权限
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);
        // * 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);
        // * 查询所有符合条件的图片大小
        queryWrapper.select("picSize");
        List<Long> picSizeList = pictureService.getBaseMapper().selectObjs(queryWrapper).stream().map(size -> ((Number) size).longValue()).collect(Collectors.toList());
        // * 定义分段范围，使用有序 Map
        Map<String, Long> sizeRanges = new LinkedHashMap<>();
        sizeRanges.put("<100KB", picSizeList.stream().filter(size -> size < 100 * 1024).count());
        sizeRanges.put("100KB-500KB", picSizeList.stream().filter(size -> size >= 100 * 1024 && size < 500 * 1024).count());
        sizeRanges.put("500KB-1MB", picSizeList.stream().filter(size -> size >= 500 * 1024 && size < 1024 * 1024).count());
        sizeRanges.put(">1MB", picSizeList.stream().filter(size -> size >= 1024 * 1024).count());
        // * 转换为响应对象
        return sizeRanges.entrySet().stream().map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    /**
     * 获取空间用户行为分析
     *
     * @param spaceUserAnalyzeRequest 空间用户行为分析请求
     * @param loginUser               登录用户
     * @return 空间用户行为分析结果
     */
    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // * 检查权限
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);
        // * 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        Long userId = spaceUserAnalyzeRequest.getUserId();
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, queryWrapper);
        // * 分析维度：每日、每周、每月
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            // * select DATE_FORMAT(createTime, '%Y-%m-%d') AS period, COUNT(*) AS count from picture
            // * where userId = ? and createTime >= ? and createTime < ? group by period order by period asc
            case "day":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m-%d') AS period", "COUNT(*) AS count");
                break;
            // * select YEARWEEK(createTime) AS period, COUNT(*) AS count from picture
            // * where userId = ? and createTime >= ? and createTime < ? group by period order by period asc
            case "week":
                queryWrapper.select("YEARWEEK(createTime) AS period", "COUNT(*) AS count");
                break;
            // * select DATE_FORMAT(createTime, '%Y-%m') AS period, COUNT(*) AS count from picture
            // * where userId = ? and createTime >= ? and createTime < ? group by period order by period asc
            case "month":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m') AS period", "COUNT(*) AS count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的时间维度");
        }
        // * 分组和排序
        queryWrapper.groupBy("period").orderByAsc("period");
        // * 查询结果并转换
        return pictureService.getBaseMapper().selectMaps(queryWrapper).stream().map(result -> {
            String period = result.get("period").toString();
            Long count = ((Number) result.get("count")).longValue();
            return new SpaceUserAnalyzeResponse(period, count);
        }).collect(Collectors.toList());
    }

    /**
     * 获取空间排行分析
     *
     * @param spaceRankAnalyzeRequest 空间排行分析请求
     * @param loginUser               登录用户
     * @return 空间排行分析结果
     */
    @Override
    public List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // * 仅管理员可查看空间排行
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "没有权限");
        // * 构造查询条件
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .select("id", "spaceName", "userId", "totalSize")
                .orderByDesc("totalSize")
                .last("LIMIT " + spaceRankAnalyzeRequest.getTopN());
        // * 查询结果
        return spaceService.list(queryWrapper);
    }

    /**
     * 检查空间分析权限
     *
     * @param spaceAnalyzeRequest 空间分析请求
     * @param loginUser           登录用户
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        // * 检查权限
        if (spaceAnalyzeRequest.isQueryAll() || spaceAnalyzeRequest.isQueryPublic()) {
            // * 全空间分析或者公共图库权限校验：仅管理员可访问
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问公共图库");
        } else {
            // * 私有空间权限校验
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            spaceService.checkSpaceAuth(loginUser, space);
        }
    }

    /**
     * 填充查询条件
     *
     * @param spaceAnalyzeRequest 空间分析请求
     * @param queryWrapper        查询条件构造器
     */
    private void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        // * 全空间分析
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        if (queryAll) {
            return;
        }
        // * 公共图库
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        if (queryPublic) {
            queryWrapper.isNull("space_id");
            return;
        }
        // * 私有空间
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("space_id", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }
}
