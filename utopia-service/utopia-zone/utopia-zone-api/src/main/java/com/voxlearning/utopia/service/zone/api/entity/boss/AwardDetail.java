package com.voxlearning.utopia.service.zone.api.entity.boss;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author : kai.sun
 * @version : 2018-11-06
 * @description :
 **/
@Getter
@Setter
public class AwardDetail implements Serializable {
    private static final long serialVersionUID = -208884423490733002L;
    String name;
    String pic;
    String num;
    //1普通宝箱、2中级宝箱 、3高级宝箱 、4竞技卡、5、吃鸡
    int type;
}
