package com.voxlearning.utopia.service.zone.api.entity.plot;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * @author : kai.sun
 * @version : 2018-11-09
 * @description :
 **/

@Getter
@Setter
@NoArgsConstructor
public class Popup implements Serializable {

    private static final long serialVersionUID = -6870453866600489667L;
    private String text;
    private String button1;
    private String button2;

}
