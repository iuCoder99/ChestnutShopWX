package com.app.uni_app.pojo.dto;

import com.app.uni_app.pojo.emums.BannerStatus;
import lombok.Data;

@Data
public class BannerStatusDTO {

    String id;

    BannerStatus status;
}
