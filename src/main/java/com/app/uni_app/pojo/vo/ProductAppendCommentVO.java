package com.app.uni_app.pojo.vo;

import com.app.uni_app.common.util.BusinessTimeFormatUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "追评 VO")
public class ProductAppendCommentVO {

    @Schema(description = "追评 id")
    private String id;

    @Schema(description = "用户 id")
    private String userId;

    @Schema(description = "回复内容")
    private String content;

    @Schema(description = "图片格式")
    private String imageUrls;

    private LocalDateTime createTime;

    @Schema(description = "时间说明,前端直接展示")
    private String createTimeBusinessText;

    public String getCreateTimeBusinessText() {
        return BusinessTimeFormatUtils.formatTimeDiffWithNow(createTime);
    }

}
