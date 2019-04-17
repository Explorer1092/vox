package com.voxlearning.wechat.support.mapper.teacher;

import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.ExClazz;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by xinxin on 19/1/2016.
 */
@Getter
@Setter
public class ClazzSummaryMapper implements Serializable {

    private static final long serialVersionUID = 5606237063936997508L;

    private Long clazzId;
    private String clazzName;
    private Integer clazzLevel;

    private Long groupId; //班级不可布置作业时为空
    private boolean assignable; //是否可布置作业

    public ClazzSummaryMapper(Clazz clazz) {
        this.clazzId = clazz.getId();
        this.clazzName = clazz.getClassName();
        this.clazzLevel = ConversionUtils.toInt(clazz.getClassLevel());

        if (clazz instanceof ExClazz && ((ExClazz) clazz).getCurTeacherArrangeableGroups().size() > 0) {
            groupId = ((ExClazz) clazz).getCurTeacherArrangeableGroups().get(0).getId();
        }
    }
}
