package com.voxlearning.utopia.agent.bean.resource;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ClazzResource implements Serializable {

    private Long clazzId;
    private List<Long> groupList;
    private String clazzName;
    private String shortName;
    private Boolean withMath;
    private Boolean withChinese;
    private Boolean withEnglish;
    private Boolean matHwAssigned;
    private Boolean engHwAssigned;
    private Boolean chnHwAssigned;
    private Boolean hwAssigned;                    // 本月是否布置过作业
    private Map<String, List<ClazzTeacherResource>> resourceMap;

    private Long gradeScale = 0L;                  // 年级规模             2017/5/22
    private Integer registNum = 0;                  //班级注册量
    private Integer authNum = 0;                   // 年级认证学生数量      2017/5/22
    private Integer monthActive = 0;               // 年级月活             2017/5/22
    private Integer tmCsAnshEq2StuCount = 0;       // 年级数扫              2017/5/22

    private Boolean mathAssSHWed = false;               // 是否布置数学暑期作业
    private Boolean engAssSHWed = false;                // 是否布置英语暑期作业
    private Boolean chnAssSHWed = false;                // 是否布置语文暑期作业

    //=========快乐学
    private Integer stuKlxTnCount; //考号数
}
