package com.voxlearning.utopia.agent.bean.resource;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 单科作答情况
 *
 * @author chunlin.yu
 * @create 2017-08-29 11:05
 **/
@Getter
@Setter
@NoArgsConstructor
public class SingleSubjectAnsh {

    private String subjectName;

    private Integer anshEq1StuCount;

    private Integer anshGte2StuCount;

    private Integer anshGte2IncStuCount;

    private Integer anshGte2BfStuCount;




}
