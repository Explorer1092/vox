package com.voxlearning.utopia.agent.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/4/18.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class marketfeeData implements Serializable {
    private static final long serialVersionUID = 6236122480545106924L;
    private Integer mouthInteger;
    private String  mouthStr;
}
