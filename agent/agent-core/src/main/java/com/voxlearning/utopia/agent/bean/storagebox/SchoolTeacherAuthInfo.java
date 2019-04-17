package com.voxlearning.utopia.agent.bean.storagebox;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 学校中的老师认证信息
 * Created by yaguang.wang
 * on 2017/7/12.
 */
@Getter
@Setter
@NoArgsConstructor
public class SchoolTeacherAuthInfo {
    private String schoolName;
    private List<TeacherAuthInfo> authList;
}
