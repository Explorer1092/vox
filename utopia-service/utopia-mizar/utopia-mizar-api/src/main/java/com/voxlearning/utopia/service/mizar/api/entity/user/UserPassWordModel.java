package com.voxlearning.utopia.service.mizar.api.entity.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by wangshichao on 16/9/6.
 */

@Getter
@Setter
@NoArgsConstructor
public class UserPassWordModel {


    private Long userId;

    private String  mobile;

    private  String  oldPassword;

    private  String   newPassWord;
}
