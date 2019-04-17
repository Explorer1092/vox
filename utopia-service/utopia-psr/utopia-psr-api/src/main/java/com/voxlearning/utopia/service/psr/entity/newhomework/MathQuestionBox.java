package com.voxlearning.utopia.service.psr.entity.newhomework;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/7/27
 * Time: 11:09
 * To change this template use File | Settings | File Templates.
 */
@Getter
@Setter
public class MathQuestionBox implements Serializable {

    private static final long serialVersionUID = -2687588615380010367L;

    private String name;
    private int type;
    private String id;
    private int difficulty;
    private List<String> questionIds;
    private String usageType;
    private boolean  alternative;
}
