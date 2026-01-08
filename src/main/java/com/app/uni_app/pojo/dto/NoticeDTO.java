package com.app.uni_app.pojo.dto;

import com.app.uni_app.pojo.emums.CommonStatus;
import lombok.Data;

@Data
public class NoticeDTO {

    private String id;
    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;


    private CommonStatus status;
}
