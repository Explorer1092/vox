package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * Created by alex on 2016/2/22.
 */
@Data
public class BulkSchoolInfoMapper  implements Serializable {

    private Integer regionCode;
    private String schoolName;
    private String shortName;
    private String schoolLevel;
    private String schoolType;
    private String authState;
    private String authSource;
    private String vip;

    public boolean isAllEmpty() {
        return regionCode == null
                && StringUtils.isBlank(schoolName)
                && StringUtils.isBlank(shortName)
                && StringUtils.isBlank(schoolLevel)
                && StringUtils.isBlank(schoolType)
                && StringUtils.isBlank(authState)
                && StringUtils.isBlank(authSource)
                && StringUtils.isBlank(vip);
    }
}
