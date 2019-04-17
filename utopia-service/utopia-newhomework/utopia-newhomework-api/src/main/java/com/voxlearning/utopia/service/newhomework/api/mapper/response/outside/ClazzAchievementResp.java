package com.voxlearning.utopia.service.newhomework.api.mapper.response.outside;

import com.voxlearning.utopia.service.newhomework.api.mapper.response.base.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/11/16
 */
@Getter
@Setter
public class ClazzAchievementResp extends BaseResp {
    private static final long serialVersionUID = 6093121736711650701L;

    private Double avgReadingCount;        // 人均阅读成就字数
    private Double avgGoldenWordsCount;    // 人均收藏好词好句数量
    private List<StudentAchievement> studentAchievements = new LinkedList<>();

    @Getter
    @Setter
    @AllArgsConstructor
    public static class StudentAchievement implements Serializable {
        private static final long serialVersionUID = -7014875571170202108L;

        private Long studentId;
        private String studentName;
        private double totalReadingCount;     // 阅读成就字数
        private int goldenWordsCount;      // 收藏好词好句数量
    }
}
