package com.voxlearning.utopia.service.newhomework.impl.service.queue;

import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkSyllable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2017/9/19
 */
@Getter
@Setter
public class SaveHomeworkSyllableCommand implements Serializable {

    private static final long serialVersionUID = 991619821103354365L;

    List<NewHomeworkSyllable> results;
}
