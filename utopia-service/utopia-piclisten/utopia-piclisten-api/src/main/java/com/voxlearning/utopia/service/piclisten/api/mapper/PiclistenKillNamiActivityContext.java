package com.voxlearning.utopia.service.piclisten.api.mapper;

import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.Data;

import java.io.Serializable;

/**
 * @author jiangpeng
 * @since 2018-08-23 上午11:09
 **/
@Data
public class PiclistenKillNamiActivityContext implements Serializable {

    private static final long serialVersionUID = -8940936475817982952L;

    private User loginUser;
    private StudentDetail studentDetail;
    private String sys;
    private String imgCdn;

    public boolean isLogin(){
        return loginUser != null;
    }

    public boolean loginUserIsParent(){
        return isLogin() && loginUser.isParent();
    }


    public PiclistenKillNamiActivityContext(User loginUser, StudentDetail studentDetail, String sys, String imgCdn) {
        this.loginUser = loginUser;
        this.studentDetail = studentDetail;
        this.sys = sys;
        this.imgCdn = imgCdn;
    }

    public PiclistenKillNamiActivityContext() {
    }
}
