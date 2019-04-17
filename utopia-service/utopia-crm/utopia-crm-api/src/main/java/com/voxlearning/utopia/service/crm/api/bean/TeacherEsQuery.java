package com.voxlearning.utopia.service.crm.api.bean;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * TeacherEsQuery
 *
 * @author song.wang
 * @date 2018/7/18
 */
@Getter
@Setter
public class TeacherEsQuery implements Serializable{

    private static final long serialVersionUID = 2466227064053285666L;

    private String keywords;
    private Collection<Long> schoolIds;
    private List<SchoolLevel> schoolLevelList;

    private Collection<Integer> provinceCodes;
    private Collection<Integer> cityCodes;
    private Collection<Integer> countyCodes;

    private Double latitude;                           // 纬度
    private Double longitude;                          // 经度
    private Integer pageNo = 1;
    private Integer pageSize = 20;

}
