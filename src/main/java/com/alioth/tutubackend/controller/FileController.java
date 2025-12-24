package com.alioth.tutubackend.controller;

import com.alioth.tutubackend.annotation.AuthCheck;
import com.alioth.tutubackend.common.BaseResponse;
import com.alioth.tutubackend.common.ResultUtils;
import com.alioth.tutubackend.constant.UserConstant;
import com.alioth.tutubackend.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    /**
     * 文件上传测试
     *
     * @param multipartFile 文件
     * @return 文件名
     */
    @PostMapping("/test/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        // 获取原始文件名
        String originalFilename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s", originalFilename);
        File file = null;
        try {
            // * 创建临时文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // * 返回文件地址
            return ResultUtils.success(filepath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (file != null) {
                boolean result = file.delete();
                if (!result) {
                    log.error("临时文件删除结果：{}", filepath);
                }
            }
        }
    }
}
