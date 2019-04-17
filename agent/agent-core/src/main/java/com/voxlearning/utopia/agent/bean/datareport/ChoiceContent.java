package com.voxlearning.utopia.agent.bean.datareport;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 数据报表选择的内容
 * Created by yaguang.wang on 2016/10/9.
 */
@Getter
@Setter
@NoArgsConstructor
public class ChoiceContent implements Serializable {
    private static final long serialVersionUID = 2021611016904862894L;
    private String name;
    private Long id;
}
