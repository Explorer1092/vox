package com.voxlearning.utopia.service.piclisten.api.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: wei.jiang
 * @Date: Created on 2018/1/12
 * 点读机用户设备
 */
@Data
public class PicListenBookUserDev implements Serializable {

    private static final long serialVersionUID = -7487028964920295528L;

    private Long userId;
    private List<DevInfo> userDevInfo;


    @Getter
    @Setter
    public static class DevInfo implements Serializable {

        private static final long serialVersionUID = 8505415670222875650L;

        private String devId;
        private String devName;
        private String createTime;
    }
}
