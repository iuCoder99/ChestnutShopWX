package com.app.uni_app.pojo.dto;

import com.app.uni_app.pojo.emums.CommonDefault;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressDTO {
    private String id;
    @Size(min = 1, max = 20, message = "收件人姓名必须在1~20个字符之间")
    private String receiver;
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号格式")
    private String phone;
    @Size(min = 1, message = "省份不能为空")
    private String province;
    @Size(min = 1, message = "城市不能为空")
    private String city;
    @Size(min = 1, message = "区县不能为空")
    private String district;
    @Size(min = 1, max = 200, message = "详细地址必须在1~200个字符之间")
    private String detailAddress;

    private CommonDefault isDefault;
}
