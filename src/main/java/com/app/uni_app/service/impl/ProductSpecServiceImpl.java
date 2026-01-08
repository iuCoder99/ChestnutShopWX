package com.app.uni_app.service.impl;

import com.app.uni_app.pojo.entity.ProductSpec;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.uni_app.service.ProductSpecService;
import com.app.uni_app.mapper.ProductSpecMapper;
import org.springframework.stereotype.Service;

/**
* @author 20589
* @description 针对表【product_spec(商品规格表)】的数据库操作Service实现
* @createDate 2025-12-29 11:00:59
*/
@Service
public class ProductSpecServiceImpl extends ServiceImpl<ProductSpecMapper, ProductSpec>
    implements ProductSpecService{

}




