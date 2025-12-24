package com.alioth.tutubackend.controller;

import com.alioth.tutubackend.annotation.AuthCheck;
import com.alioth.tutubackend.common.BaseResponse;
import com.alioth.tutubackend.common.ResultUtils;
import com.alioth.tutubackend.constant.UserConstant;
import com.alioth.tutubackend.exception.BusinessException;
import com.alioth.tutubackend.exception.ErrorCode;
import com.alioth.tutubackend.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
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

    /**
     * 文件下载测试
     *
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @GetMapping("/test/download/")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }
}
