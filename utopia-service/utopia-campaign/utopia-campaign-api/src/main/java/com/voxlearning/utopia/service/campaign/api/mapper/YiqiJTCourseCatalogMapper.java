package com.voxlearning.utopia.service.campaign.api.mapper;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class YiqiJTCourseCatalogMapper implements java.io.Serializable {
    private static final long serialVersionUID = -1265676885288478120L;

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
