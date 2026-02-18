package com.app.uni_app.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginInfo {
    String accessToken;
    String refreshToken;
    Object userInfo;
}
