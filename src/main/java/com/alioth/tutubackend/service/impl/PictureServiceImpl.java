package com.alioth.tutubackend.service.impl;

import com.alioth.tutubackend.exception.ErrorCode;
import com.alioth.tutubackend.exception.ThrowUtils;
import com.alioth.tutubackend.manager.FileManager;
import com.alioth.tutubackend.mapper.PictureMapper;
import com.alioth.tutubackend.model.dto.file.UploadPictureResult;
import com.alioth.tutubackend.model.dto.picture.PictureUploadRequest;
import com.alioth.tutubackend.model.entity.Picture;
import com.alioth.tutubackend.model.entity.User;
import com.alioth.tutubackend.model.vo.PictureVO;
import com.alioth.tutubackend.service.PictureService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Alioth
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-12-24 22:25:05
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private FileManager fileManager;

    /**
     * 上传图片
     *
     * @param multipartFile        文件
     * @param pictureUploadRequest 上传参数
     * @param loginUser            登录用户
     * @return 上传结果
     */
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // * 校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // * 判断新增还是更新
        Long pictureId = pictureUploadRequest != null ? pictureUploadRequest.getId() : null;
        // * 新增前判断图片是否存在
        if (pictureId != null) {
            boolean exists = lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR);
        }
        // * 上传图片（根据用户 id 划分目录）
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        // * 构造 picture 对象
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        // * pictureId 不为空，则更新
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean result = saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "上传图片失败");
        // * 封装返回结果
        return PictureVO.objToVo(picture);
    }
}
