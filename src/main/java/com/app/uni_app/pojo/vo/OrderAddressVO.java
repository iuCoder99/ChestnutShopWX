package com.app.uni_app.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OrderAddressVO {

    /**
     * 收件人姓名
     */
    @Schema(name = "收件人姓名")
    private String receiver;

    /**
     * 收件人手机号
     */
    @Schema(name = "收件人手机号")
    private String phone;

    /**
     * 省份
     */
    @Schema(name = "省份")
    private String province;

    /**
     * 城市
     */
    @Schema(name = "城市")
    private String city;

    /**
     * 区县
     */
    @Schema(name = "区县")
    private String district;

    /**
     * 详细地址（街道/门牌号）
     */
    @Schema(name = "详细地址")
    private String detailAddress;

}
