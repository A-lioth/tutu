package com.alioth.tutubackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alioth.tutubackend.model.entity.Picture;
import com.alioth.tutubackend.service.PictureService;
import com.alioth.tutubackend.mapper.PictureMapper;
import org.springframework.stereotype.Service;

/**
* @author Alioth
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-12-24 22:25:05
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

}
