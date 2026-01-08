package com.alioth.tutubackend.service;

import com.alioth.tutubackend.model.dto.picture.*;
import com.alioth.tutubackend.model.entity.Picture;
import com.alioth.tutubackend.model.entity.User;
import com.alioth.tutubackend.model.vo.PictureVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

/**
 * @author Alioth
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-12-24 22:25:05
 */
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     *
     * @param inputSource          文件对象
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser            登录用户
     * @return 图片信息
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 删除图片
     *
     * @param pictureId 图片 ID
     * @param loginUser 登录用户
     */
    void deletePicture(long pictureId, User loginUser);

    /**
     * 编辑图片
     *
     * @param pictureEditRequest 图片编辑请求
     * @param loginUser          登录用户
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 获取查询条件
     *
     * @param pictureQueryRequest 图片查询请求
     * @return 查询条件
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片信息包装类
     *
     * @param picture 图片信息
     * @return 图片信息包装类
     */
    public PictureVO getPictureVO(Picture picture);

    /**
     * 分页获取图片列表包装类
     *
     * @param picturePage 图片列表
     * @return 图片列表包装类
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage);

    /**
     * 校验图片信息
     *
     * @param picture 图片信息
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核请求
     * @param loginUser            登录用户
     */
    void pictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数
     *
     * @param picture   图片信息
     * @param loginUser 登录用户
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest 图片批量上传请求
     * @param loginUser                   登录用户
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

    /**
     * 清理图片文件
     *
     * @param oldPicture 旧图片信息
     */
    @Async
    void clearPictureFile(Picture oldPicture);

    /**
     * 检查图片权限
     *
     * @param loginUser 登录用户
     * @param picture   图片信息
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 根据颜色搜索图片
     *
     * @param spaceId   空间 ID
     * @param picColor  图片颜色
     * @param loginUser 登录用户
     * @return 图片列表包装类
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);
}
