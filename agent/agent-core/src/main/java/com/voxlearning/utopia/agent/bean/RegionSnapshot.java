package com.voxlearning.utopia.agent.bean;

import com.voxlearning.utopia.service.region.api.constant.RegionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Jia HuanYin
 * @since 2015/10/14
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegionSnapshot implements Serializable {
    private static final long serialVersionUID = 6861277242811243369L;

    private Integer code;
    private String name;
    private RegionType type;
}
