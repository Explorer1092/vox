package com.voxlearning.utopia.agent.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yaguang.wang on 2016/5/18.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SchoolShortInfo implements Serializable {
    private static final long serialVersionUID = 5761928705782488152L;
    private Long schoolId;   //学校ID
    private String schoolName;  //学校名称
    private String regionName;  //学校地址
    private String gradeDistribution; //年级分布

    private Integer level; //1.小学，2中学, 4 高中
    private String headName; //负责人姓名
    private Long headId; //负责人Id

    private Boolean theUserIsManageAble; //是否可以管理这所学校

    /**
     * 创建时间
     */
    private Date createTime;
}
