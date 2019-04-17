package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.api.constant.AfentiState;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ruib
 * @since 2016/7/21
 */
@Getter
@Setter
@NoArgsConstructor
public class ElfResultContext extends AbstractAfentiContext<ElfResultContext> {
    private static final long serialVersionUID = 9146531189358749955L;

    // in
    private StudentDetail student; // 学生
    private Subject subject; // 学科
    private String questionId; // 试题id
    private Boolean master; // 是否做对了
    private AfentiState afentiState; // 题目状态
    private String originalQuestionId; // 类题的原题

    // middle
    private WrongQuestionLibrary question;
    private String similarId;
    private Set<String> ids = new HashSet<>();
    private int integral;
    private Integer creditCount = 0;    // 自学积分数量[45497]

    // out
    private Map<String, Object> result = new HashMap<>();
}
