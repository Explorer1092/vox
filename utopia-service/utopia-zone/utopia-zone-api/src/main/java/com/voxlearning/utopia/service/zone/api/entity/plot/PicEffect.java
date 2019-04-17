package com.voxlearning.utopia.service.zone.api.entity.plot;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * @author : kai.sun
 * @version : 2018-11-13
 * @description :
 **/

@Getter
@Setter
@NoArgsConstructor
public class PicEffect implements Serializable {

    private static final long serialVersionUID = 4595679638963747767L;
    private String url;

    private Map<String,Double> coordinate;

}
