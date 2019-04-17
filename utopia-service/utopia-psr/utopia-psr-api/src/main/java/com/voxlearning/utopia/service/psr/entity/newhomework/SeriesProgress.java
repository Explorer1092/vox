package com.voxlearning.utopia.service.psr.entity.newhomework;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/7/18
 * Time: 20:39
 * To change this template use File | Settings | File Templates.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class SeriesProgress implements Serializable {
    private static final long serialVersionUID = -5853833276836456364L;

    private String series_id;
    private long progress_id;

}
