package com.voxlearning.utopia.agent.bean.resource;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by tao.zang on 2017/4/5.
 * 年级信息及当前老师是否是负责的学科组组长
 */
@Getter
@Setter
public class GradeDetailAndIfKlxSubjectLeaderInfo implements Serializable {
    private ClazzLevel clazzLevel;//年级
    private boolean ifKlxSubjectLeader;//是否是该年级的学科组组长
    private String clazzDescription;//年级 描述
    private int level;
}
