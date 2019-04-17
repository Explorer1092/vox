package com.voxlearning.utopia.agent.bean.resource;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 作业
 * Created by yaguang.wang on 2016/12/1.
 */
@Getter
@Setter
@NoArgsConstructor
public class HomeWorkListInfo implements Serializable {
    private static final long serialVersionUID = 8185843034389607535L;
    private String monthGroupName;              // 月组名
    private List<HomeWorkInfo> hwList;          // 本月下的作业记录列表
}
