package com.voxlearning.utopia.service.psr.entity.mathnewkp;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/10/20.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class MathQuestion implements Serializable{
    private static final long serialVersionUID = -1933480735866093621L;

    String questionId;
    Integer contentTypeId; //题型
}
