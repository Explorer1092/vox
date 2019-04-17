package com.voxlearning.utopia.service.crm.api.bean;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 统考学校信息
 * Created by tao.zang on 2017/4/17.
 */
@Getter
@Setter
public class UnifiedExamSchoolInfo implements Serializable {
    private Long  schoolId;
    private String schoolName;
    private SchoolLevel schoolLevel; //学校等级

}
