package com.voxlearning.washington.mapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author malong
 * @since 2017/4/26
 */
@NoArgsConstructor
@Getter
@Setter
public class EaseMobBottomMenuConfig implements Serializable {
    private static final long serialVersionUID = -4744753819897417942L;

    private String type;
    private String name;
    private String desc;
    private String url;
    private String version;
}
