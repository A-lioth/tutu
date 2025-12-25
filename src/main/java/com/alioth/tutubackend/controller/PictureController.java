package com.alioth.tutubackend.controller;

import com.alioth.tutubackend.annotation.AuthCheck;
import com.alioth.tutubackend.common.BaseResponse;
import com.alioth.tutubackend.common.ResultUtils;
import com.alioth.tutubackend.constant.UserConstant;
import com.alioth.tutubackend.model.dto.picture.PictureUploadRequest;
import com.alioth.tutubackend.model.entity.User;
import com.alioth.tutubackend.model.vo.PictureVO;
import com.alioth.tutubackend.service.PictureService;
import com.alioth.tutubackend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;

    /**
     * 上传图片（可重新上传）
     */
    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }
}
