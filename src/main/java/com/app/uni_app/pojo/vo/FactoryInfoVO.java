package com.app.uni_app.pojo.vo;

import lombok.Data;

@Data
public class FactoryInfoVO {

    /**
     * 单张背景图
     */
    private String image;

    /**
     * 工厂名称
     */
    private String factoryName;

    /**
     * 工厂简介
     */
    private String introduction;

    /**
     * 服务热线
     */
    private String serviceHotline;

    /**
     * 官方微信
     */
    private String officialWechat;

    /**
     * 工厂地址
     */
    private String address;

    /**
     * 版权信息
     */
    private String copyrightInfo;

}
