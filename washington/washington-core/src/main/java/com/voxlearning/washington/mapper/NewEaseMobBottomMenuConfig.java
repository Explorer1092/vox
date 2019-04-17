package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author malong
 * @since 2017/6/1
 */
@NoArgsConstructor
@Getter
@Setter
public class NewEaseMobBottomMenuConfig implements Serializable {
    private static final long serialVersionUID = 6373542581111537997L;

    private Map<String, Object> firstLevel; //一级菜单
    private List<Map<String, Object>> secondLevel;  //二级菜单
    private String subject;
    private String version;
}
