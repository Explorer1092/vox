package com.voxlearning.utopia.agent.bean.tag;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.utopia.agent.bean.export.ExportAble;
import com.voxlearning.utopia.agent.constants.AgentTagType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 标签与服务对象
 * @author deliang.che
 * @since 2019/3/21
 */
@Getter
@Setter
public class AgentTagTargetData implements ExportAble {

    private static final long serialVersionUID = 7551031587657513276L;
    private String id;
    private AgentTagType tagType;
    private String tagName;
    private Long schoolId;
    private String schoolName;

    private Long teacherId;
    private String teacherName;

    private SchoolLevel schoolLevel;

    private Integer provinceCode;
    private String provinceName;
    private Integer cityCode;
    private String cityName;
    private Integer countyCode;
    private String countyName;

    private Long groupId;
    private String groupName;

    private Long userId;
    private String userName;

    public List<Object> getExportAbleData() {
        List<Object> result = new ArrayList<>();
        result.add(this.getTagType() == null ? "" : this.getTagType().getDesc());
        result.add(this.getTagName());
        result.add(this.getSchoolId());
        result.add(this.getSchoolName());

        result.add(this.getTeacherId());
        result.add(this.getTeacherName());

        result.add(this.getSchoolLevel() == null ? "" : this.getSchoolLevel().getDescription());

        result.add(this.getProvinceName());
        result.add(this.getCityName());
        result.add(this.getCountyName());

        result.add(this.getGroupName());
        result.add(this.getUserName());

        return result;
    }
}
