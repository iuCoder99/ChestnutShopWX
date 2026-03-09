package com.app.uni_app.pojo.emums;

import com.app.uni_app.pojo.entity.Product;
import lombok.Getter;

@Getter
public enum ProductSortType {


    // default(默认), priceAsc(价格升序), priceDesc(价格降序), newest(最新上架)
    DEFAULT("default", "sales_count DESC", "默认按照销量排行"),
    PRICE_ASC("priceAsc", "price ASC", "价格升序"),
    PRICE_DESC("priceDesc", "price DESC", "价格降序"),
    NEWEST("newest", "update_time DESC", "最新上架");
    /**
     * 前端传的 String
     */
    private final String value;
    /**
     * 拼接的 sql 字段
     */
    private final String dbValue;


    private final String desc;

    ProductSortType(String value, String dbValue, String desc) {
        this.value = value;
        this.dbValue = dbValue;
        this.desc = desc;
    }


    /**
     * static 根据 value 返回枚举
     *
     * @param value
     * @return
     */
    public static ProductSortType getByValue(String value) {
        for (ProductSortType productSortType : values()) {
            if (productSortType.value.equals(value)) {
                return productSortType;
            }
        }
        throw new IllegalArgumentException("无效的ProductSortType.value:" + value);
    }

    public static int compare(Product p1, Product p2, ProductSortType productSortType) {
        switch (productSortType) {
            case DEFAULT -> {
                return p2.getSalesCount().compareTo(p1.getSalesCount());
            }
            case PRICE_ASC -> {
                return p1.getPrice().compareTo(p2.getPrice());
            }
            case PRICE_DESC -> {
                return p2.getPrice().compareTo(p1.getPrice());
            }
            case NEWEST -> {
                return p2.getUpdateTime().compareTo(p1.getUpdateTime());
            }
        }
        throw new IllegalArgumentException("无效的ProductSortType: " + productSortType);
    }


}
