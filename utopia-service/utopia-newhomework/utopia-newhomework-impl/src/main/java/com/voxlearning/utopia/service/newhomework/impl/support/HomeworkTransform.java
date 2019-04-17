package com.voxlearning.utopia.service.newhomework.impl.support;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.shard.ShardHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.shard.ShardHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.entity.shard.ShardHomeworkPractice;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.*;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkProcessResultHBase;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkResultAnswerHBase;
import com.voxlearning.utopia.service.newhomework.api.hbase.HomeworkResultHBase;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2017/10/10
 */
public class HomeworkTransform {

    public static SubHomework NewHomeworkToSub(NewHomework newHomework) {
        if (newHomework == null) {
            return null;
        }
        SubHomework subHomework = new SubHomework();
        HomeworkCopyUtils.copyProperties(subHomework, newHomework);
        return subHomework;
    }

    public static ShardHomework NewHomeworkToShard(NewHomework newHomework) {
        if (newHomework == null) {
            return null;
        }
        ShardHomework shardHomework = new ShardHomework();
        HomeworkCopyUtils.copyProperties(shardHomework, newHomework);
        return shardHomework;
    }

    public static SubHomework.Location NewHomeworkLocationToSub(NewHomework.Location newHomeworkLocation) {
        if (newHomeworkLocation == null) {
            return null;
        }
        SubHomework.Location location = new SubHomework.Location();
        HomeworkCopyUtils.copyProperties(location, newHomeworkLocation);
        return location;
    }

    public static ShardHomework.Location NewHomeworkLocationToShard(NewHomework.Location newHomeworkLocation) {
        if (newHomeworkLocation == null) {
            return null;
        }
        ShardHomework.Location location = new ShardHomework.Location();
        HomeworkCopyUtils.copyProperties(location, newHomeworkLocation);
        return location;
    }

    public static SubHomeworkBook NewHomeworkBookToSub(NewHomeworkBook newHomeworkBook) {
        if (newHomeworkBook == null) {
            return null;
        }
        SubHomeworkBook subHomeworkBook = new SubHomeworkBook();
        HomeworkCopyUtils.copyProperties(subHomeworkBook, newHomeworkBook);
        return subHomeworkBook;
    }

    public static ShardHomeworkBook NewHomeworkBookToShard(NewHomeworkBook newHomeworkBook) {
        if (newHomeworkBook == null) {
            return null;
        }
        ShardHomeworkBook shardHomeworkBook = new ShardHomeworkBook();
        HomeworkCopyUtils.copyProperties(shardHomeworkBook, newHomeworkBook);
        return shardHomeworkBook;
    }

    public static SubHomeworkPractice NewHomeworkToSubHomeworkPractice(NewHomework newHomework) {
        if (newHomework == null || CollectionUtils.isEmpty(newHomework.getPractices())) {
            return null;
        }
        SubHomeworkPractice subHomeworkPractice = new SubHomeworkPractice();
        HomeworkCopyUtils.copyProperties(subHomeworkPractice, newHomework);
        return subHomeworkPractice;
    }

    public static ShardHomeworkPractice NewHomeworkToShardHomeworkPractice(NewHomework newHomework) {
        if (newHomework == null || CollectionUtils.isEmpty(newHomework.getPractices())) {
            return null;
        }
        ShardHomeworkPractice shardHomeworkPractice = new ShardHomeworkPractice();
        HomeworkCopyUtils.copyProperties(shardHomeworkPractice, newHomework);
        return shardHomeworkPractice;
    }

    public static SubHomeworkResult NewHomeworkResultToSub(NewHomeworkResult newHomeworkResult) {
        if (newHomeworkResult == null) {
            return null;
        }
        SubHomeworkResult subHomeworkResult = new SubHomeworkResult();
        HomeworkCopyUtils.copyProperties(subHomeworkResult, newHomeworkResult);
        return subHomeworkResult;
    }

    public static SubHomeworkResult HomeworkResultHBaseToSub(HomeworkResultHBase homeworkResultHBase) {
        if (homeworkResultHBase == null) {
            return null;
        }
        SubHomeworkResult subHomeworkResult = new SubHomeworkResult();
        HomeworkCopyUtils.copyProperties(subHomeworkResult, homeworkResultHBase);
        return subHomeworkResult;
    }

    public static SubHomeworkResultAnswer HomeworkResultAnswerHBaseToSub(HomeworkResultAnswerHBase homeworkResultAnswerHBase) {
        if (homeworkResultAnswerHBase == null) {
            return null;
        }
        SubHomeworkResultAnswer subHomeworkResultAnswer = new SubHomeworkResultAnswer();
        HomeworkCopyUtils.copyProperties(subHomeworkResultAnswer, homeworkResultAnswerHBase);
        return subHomeworkResultAnswer;
    }

    public static SubHomeworkProcessResult NewHomeworkProcessResultToSub(NewHomeworkProcessResult newHomeworkProcessResult) {
        if (newHomeworkProcessResult == null) {
            return null;
        }
        SubHomeworkProcessResult subHomeworkProcessResult = new SubHomeworkProcessResult();
        HomeworkCopyUtils.copyProperties(subHomeworkProcessResult, newHomeworkProcessResult);
        return subHomeworkProcessResult;
    }

    public static SubHomeworkProcessResult HomeworkProcessResultHBaseToSub(HomeworkProcessResultHBase homeworkProcessResultHBase) {
        if (homeworkProcessResultHBase == null) {
            return null;
        }
        SubHomeworkProcessResult subHomeworkProcessResult = new SubHomeworkProcessResult();
        HomeworkCopyUtils.copyProperties(subHomeworkProcessResult, homeworkProcessResultHBase);
        return subHomeworkProcessResult;
    }

    public static BaseHomeworkResultAppAnswer NewHomeworkResultAppAnswerToBase(NewHomeworkResultAppAnswer newHomeworkResultAppAnswer) {
        if (newHomeworkResultAppAnswer == null) {
            return null;
        }
        BaseHomeworkResultAppAnswer baseHomeworkResultAppAnswer = new BaseHomeworkResultAppAnswer();
        HomeworkCopyUtils.copyProperties(baseHomeworkResultAppAnswer, newHomeworkResultAppAnswer);
        return baseHomeworkResultAppAnswer;
    }

    public static NewHomeworkResultAnswer BaseHomeworkResultAnswerToNewHomeworkResultAnswer(BaseHomeworkResultAnswer baseHomeworkResultAnswer) {
        if (baseHomeworkResultAnswer == null) {
            return null;
        }
        NewHomeworkResultAnswer newHomeworkResultAnswer = new NewHomeworkResultAnswer();
        HomeworkCopyUtils.copyProperties(newHomeworkResultAnswer, baseHomeworkResultAnswer);

        LinkedHashMap<String, BaseHomeworkResultAppAnswer> subAppAnswerMap = baseHomeworkResultAnswer.getAppAnswers();
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswerMap = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(subAppAnswerMap)) {
            for (Map.Entry<String, BaseHomeworkResultAppAnswer> entry : subAppAnswerMap.entrySet()) {
                String key = entry.getKey();
                NewHomeworkResultAppAnswer value = BaseHomeworkResultAppAnswerToNew(entry.getValue());
                if (value != null) {
                    appAnswerMap.put(key, value);
                }
            }
            if (MapUtils.isNotEmpty(appAnswerMap)) {
                newHomeworkResultAnswer.setAppAnswers(appAnswerMap);
            }
        }
        return newHomeworkResultAnswer;
    }


    /**
     * =========================================
     * To NewHomeworkResult
     * =========================================
     */
    public static NewHomeworkResult SubHomeworkResultToNew(SubHomeworkResult subHomeworkResult, Collection<SubHomeworkResultAnswer> answers) {
        if (subHomeworkResult == null) {
            return null;
        }

        NewHomeworkResult newHomeworkResult = new NewHomeworkResult();
        HomeworkCopyUtils.copyProperties(newHomeworkResult, subHomeworkResult);

        LinkedHashMap<ObjectiveConfigType, BaseHomeworkResultAnswer> subPracticeMap = subHomeworkResult.getPractices();
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practiceMap = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(subPracticeMap)) {
            for (Map.Entry<ObjectiveConfigType, BaseHomeworkResultAnswer> entry : subPracticeMap.entrySet()) {
                ObjectiveConfigType key = entry.getKey();
                NewHomeworkResultAnswer value = BaseHomeworkResultAnswerToNewHomeworkResultAnswer(entry.getValue());
                if (value != null) {
                    practiceMap.put(key, value);
                }
            }
            if (MapUtils.isNotEmpty(practiceMap)) {
                newHomeworkResult.setPractices(practiceMap);
            }
        }

        for (SubHomeworkResultAnswer answer : answers) {
            // 根据 id的位数进行分组
            SubHomeworkResultAnswer.ID ID = answer.parseID();
            String[] segments = StringUtils.split(answer.getId(), "|");

            // 基础训练等appAnswers类型
            if (segments.length == 6) {
                ObjectiveConfigType type = ID.getType();
                String key = StringUtils.join(ID.getJoinKeys(), "-");
                String questionId = ID.getQuestionId();
                String processId = answer.getProcessId();
                boolean isOral = answer.getIsOral() == null ? false : answer.getIsOral();
                boolean isImageTextRhyme = answer.getIsImageTextRhyme() == null ? false : answer.getIsImageTextRhyme();
                boolean isChineseCourse = answer.getIsChineseCourse() == null ? false : answer.getIsChineseCourse();
                newHomeworkResult = processAppResultAnswer(newHomeworkResult, type, key, questionId, processId, isOral, isImageTextRhyme, isChineseCourse);
            }
            // 应试类型
            if (segments.length == 5) {
                ObjectiveConfigType type = ID.getType();
                String questionId = ID.getQuestionId();
                String processId = answer.getProcessId();
                newHomeworkResult = processNotAppResultAnswer(newHomeworkResult, type, questionId, processId);
            }
        }
        return newHomeworkResult;
    }

    private static NewHomeworkResult processNotAppResultAnswer(NewHomeworkResult newHomeworkResult, ObjectiveConfigType type, String questionId, String processId) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = newHomeworkResult.getPractices();
        if (practices == null) {
            practices = new LinkedHashMap<>();
        }
        NewHomeworkResultAnswer resultAnswer = practices.getOrDefault(type, new NewHomeworkResultAnswer());
        LinkedHashMap<String, String> answerMap = resultAnswer.getAnswers();
        if (answerMap == null) {
            answerMap = new LinkedHashMap<>();
        }
        answerMap.put(questionId, processId);
        resultAnswer.setAnswers(answerMap);
        practices.put(type, resultAnswer);
        newHomeworkResult.setPractices(practices);
        return newHomeworkResult;
    }

    private static NewHomeworkResult processAppResultAnswer(NewHomeworkResult newHomeworkResult, ObjectiveConfigType type, String key, String questionId,
                                                            String processId, boolean isOral, boolean isImageTextRhyme, boolean isChineseCourse) {
        LinkedHashMap<ObjectiveConfigType, NewHomeworkResultAnswer> practices = newHomeworkResult.getPractices();
        if (practices == null) {
            practices = new LinkedHashMap<>();
        }
        NewHomeworkResultAnswer answer = practices.getOrDefault(type, new NewHomeworkResultAnswer());
        LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswerMap = answer.getAppAnswers();
        if (appAnswerMap == null) {
            appAnswerMap = new LinkedHashMap<>();
        }
        NewHomeworkResultAppAnswer appAnswer = appAnswerMap.getOrDefault(key, new NewHomeworkResultAppAnswer());

        // 公共部分处理
        LinkedHashMap<String, String> answerMap = appAnswer.getAnswers();
        if (answerMap == null) {
            answerMap = new LinkedHashMap<>();
        }
        answerMap.put(questionId, processId);
        appAnswer.setAnswers(answerMap);
        appAnswerMap.put(key, appAnswer);

        // 绘本跟读的特殊处理
        if ((ObjectiveConfigType.READING == type || ObjectiveConfigType.LEVEL_READINGS == type) && isOral) {
            LinkedHashMap<String, String> oralAnswerMap = appAnswer.getOralAnswers();
            if (oralAnswerMap == null) {
                oralAnswerMap = new LinkedHashMap<>();
            }
            oralAnswerMap.put(questionId, processId);
            //answerMap已经包含口语题所以需要把口语题赋值给oralAnswerMap的同时从answerMap中去掉
            if (answerMap.get(questionId) != null) {
                answerMap.remove(questionId);
            }
            appAnswer.setOralAnswers(oralAnswerMap);
            appAnswerMap.put(key, appAnswer);
        }
        // 字词讲练 图文入韵&汉字文化部分特殊处理
        if ((ObjectiveConfigType.WORD_TEACH_AND_PRACTICE == type)) {
            if (isImageTextRhyme) {
                LinkedHashMap<String, String> imagetextrhymeAnswers = appAnswer.getImageTextRhymeAnswers();
                if (imagetextrhymeAnswers == null) {
                    imagetextrhymeAnswers = new LinkedHashMap<>();
                }
                imagetextrhymeAnswers.put(questionId, processId);
                //answerMap已经包含图文入韵题所以需要把图文入韵题赋值给answerMap的同时从answerMap中去掉
                if (answerMap.get(questionId) != null) {
                    answerMap.remove(questionId);
                }
                appAnswer.setImageTextRhymeAnswers(imagetextrhymeAnswers);
            }
            if (isChineseCourse) {
                LinkedHashMap<String, String> chineseCourses = appAnswer.getChineseCourses();
                if (chineseCourses == null) {
                    chineseCourses = new LinkedHashMap<>();
                }
                chineseCourses.put(questionId, processId);
                //answerMap已经包含汉字文化所以需要把图文入韵题赋值给answerMap的同时从answerMap中去掉
                if (answerMap.get(questionId) != null) {
                    answerMap.remove(questionId);
                }
                appAnswer.setChineseCourses(chineseCourses);
            }
            appAnswerMap.put(key, appAnswer);
        }
        answer.setAppAnswers(appAnswerMap);
        practices.put(type, answer);
        newHomeworkResult.setPractices(practices);
        return newHomeworkResult;
    }

    public static NewHomeworkResultAppAnswer BaseHomeworkResultAppAnswerToNew(BaseHomeworkResultAppAnswer baseHomeworkResultAppAnswer) {
        if (baseHomeworkResultAppAnswer == null) {
            return null;
        }
        NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = new NewHomeworkResultAppAnswer();
        HomeworkCopyUtils.copyProperties(newHomeworkResultAppAnswer, baseHomeworkResultAppAnswer);
        return newHomeworkResultAppAnswer;
    }

    /**
     * =========================================
     * To NewHomework
     * =========================================
     */

    public static NewHomework SubHomeworkToNew(SubHomework subHomework, SubHomeworkPractice subHomeworkPractice) {
        if (subHomework == null) {
            return null;
        }
        NewHomework newHomework = new NewHomework();
        HomeworkCopyUtils.copyProperties(newHomework, subHomework);
        if (subHomeworkPractice != null) {
            HomeworkCopyUtils.copyProperties(newHomework, subHomeworkPractice);
            newHomework.setCreateAt(subHomework.getCreateAt());
            newHomework.setUpdateAt(subHomework.getUpdateAt());
        }
        return newHomework;
    }

    public static NewHomework ShardHomeworkToNew(ShardHomework shardHomework, ShardHomeworkPractice shardHomeworkPractice) {
        if (shardHomework == null) {
            return null;
        }
        NewHomework newHomework = new NewHomework();
        HomeworkCopyUtils.copyProperties(newHomework, shardHomework);
        if (shardHomeworkPractice != null) {
            HomeworkCopyUtils.copyProperties(newHomework, shardHomeworkPractice);
            newHomework.setCreateAt(shardHomework.getCreateAt());
            newHomework.setUpdateAt(shardHomework.getUpdateAt());
        }
        return newHomework;
    }

    public static NewHomework.Location SubHomeworkLocationToNew(SubHomework.Location subLocation) {
        if (subLocation == null) {
            return null;
        }
        NewHomework.Location location = new NewHomework.Location();
        HomeworkCopyUtils.copyProperties(location, subLocation);
        return location;
    }

    public static NewHomework.Location ShardHomeworkLocationToNew(ShardHomework.Location shardLocation) {
        if (shardLocation == null) {
            return null;
        }
        NewHomework.Location location = new NewHomework.Location();
        HomeworkCopyUtils.copyProperties(location, shardLocation);
        return location;
    }

}
