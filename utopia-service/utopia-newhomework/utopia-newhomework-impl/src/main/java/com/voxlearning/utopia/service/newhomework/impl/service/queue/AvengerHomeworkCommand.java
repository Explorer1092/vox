package com.voxlearning.utopia.service.newhomework.impl.service.queue;

import com.voxlearning.utopia.service.newhomework.api.mapper.avenger.AvengerHomework;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 目前不需要自己消费，以后谁知道呢
 *
 * @author xuesong.zhang
 * @since 2017/6/15
 */
@Getter
@Setter
public class AvengerHomeworkCommand implements Serializable {

    private static final long serialVersionUID = 1056008554445049105L;

    List<AvengerHomework> results;
}
