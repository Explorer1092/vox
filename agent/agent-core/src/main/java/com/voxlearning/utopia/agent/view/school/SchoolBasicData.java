package com.voxlearning.utopia.agent.view.school;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.EduSystemType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 * @author song.wang
 * @date 2018/6/28
 */
@Getter
@Setter
public class SchoolBasicData {
    private Long schoolId;
    private String cmainName;                    // 学校主干名
    private String schoolDistrict;               // 校区信息
    private Integer schoolLevel;
    private String schoolLevelDesc;
    private Integer regionCode;
    private String regionName;


}
