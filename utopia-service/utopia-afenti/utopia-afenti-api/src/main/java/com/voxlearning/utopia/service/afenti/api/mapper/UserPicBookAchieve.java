package com.voxlearning.utopia.service.afenti.api.mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 用户绘本成就
 */
@Getter
@Setter
@EqualsAndHashCode(of = {"userId"})
@UtopiaCacheRevision("20180301")
public class UserPicBookAchieve implements Serializable{

    private static final long serialVersionUID = 2869532982709018682L;

    @JsonIgnore private Long userId;

    private int averageScore;
    private long learnTime;                     // 学习时长
    private int readingNum;                     // 阅读本数
    private int newWordsNum;                    // 新掌握的单词
    private Map<String,Integer> scoreMap;       // 记录当前周完成绘本的分数
    private Set<String> readBookIds;            // 读过书的id列表
    private Set<String> newWords;               // 新词的列表

    public UserPicBookAchieve(){
        this.readBookIds = new HashSet<>();
        this.newWords = new HashSet<>();
    }
}
