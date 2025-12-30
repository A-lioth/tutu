package com.alioth.tutubackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片批量上传请求
 *
 * @author Alioth
 */
@Data
public class PictureUploadByBatchRequest implements Serializable {

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer count = 10;

    /**
     * 名称前缀
     */
    private String namePrefix;

    private static final long serialVersionUID = 1L;
}
