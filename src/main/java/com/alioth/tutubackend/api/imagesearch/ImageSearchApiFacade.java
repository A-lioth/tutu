package com.alioth.tutubackend.api.imagesearch;

import com.alioth.tutubackend.api.imagesearch.model.ImageSearchResult;
import com.alioth.tutubackend.api.imagesearch.sub.GetImageListApi;
import com.alioth.tutubackend.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 图片搜索接口
 */
@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片
     *
     * @param imageUrl 需要以图搜图的图片地址
     * @param start    开始下表
     * @return 图片搜索结果列表
     */
    public static List<ImageSearchResult> searchImage(String imageUrl, Integer start) {
        String soImageUrl = GetImagePageUrlApi.getSoImageUrl(imageUrl);
        return GetImageListApi.getImageList(soImageUrl, start);
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        // String imageUrl = "https://baolong-picture-1259638363.cos.ap-shanghai.myqcloud.com//public/10000000/2025-02-15_lzn23PuxZqt8CPB1.";
        String imageUrl = "https://baolong-picture-1259638363.cos.ap-shanghai.myqcloud.com//public/10000000/2025-02-15_lzn23PuxZqt8CPB1.";
        List<ImageSearchResult> resultList = searchImage(imageUrl, 0);
        System.out.println("结果列表" + resultList);
    }
}
