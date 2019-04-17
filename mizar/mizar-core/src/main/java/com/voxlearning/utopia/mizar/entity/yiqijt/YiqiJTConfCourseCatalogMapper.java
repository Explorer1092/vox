package com.voxlearning.utopia.mizar.entity.yiqijt;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class YiqiJTConfCourseCatalogMapper {
    private Long id;
    private Long courseId;
    private String timeNode;
    private Integer timeNodeSec;
    private String catalogDescribe;
    public Integer getTimeNodeSec() {
        if (StringUtils.isBlank(timeNode)) {
            return 0;
        }
        timeNodeSec = new Integer(0);
        //转00:00:00格式为秒
        String[] strArr = timeNode.split(":");
        timeNodeSec += NumberUtils.toInt(strArr[0]) * 60 * 60;
        timeNodeSec += NumberUtils.toInt(strArr[1]) * 60;
        timeNodeSec += NumberUtils.toInt(strArr[2]);
        return timeNodeSec;
    }
}
