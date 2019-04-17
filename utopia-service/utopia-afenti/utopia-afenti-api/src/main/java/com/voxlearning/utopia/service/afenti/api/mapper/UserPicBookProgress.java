package com.voxlearning.utopia.service.afenti.api.mapper;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.voxlearning.alps.lang.convert.SafeConverter.toDouble;
import static com.voxlearning.utopia.service.afenti.api.mapper.UserPicBookProgress.Attr.*;

/**
 * 用户绘本的阅读进度
 */
@Getter
@Setter
@EqualsAndHashCode(of = {"userId", "bookId"})
public class UserPicBookProgress implements Serializable {

    private static final long serialVersionUID = 6877735472128780184L;

    private Long userId;
    private String bookId;
    private List<Map<String, Object>> moduleDetail;      // 模块进度(做到了第几页、第几题)
    private Long readTime;                              // 最近一次的阅读时间,包括做题
    private Map<String, Object> scoreMap;                // 存储题目的分数
    private Set<String> readPages;                      // 阅读过的页面

    public UserPicBookProgress() {
        this.moduleDetail = new ArrayList<>();
    }

    public boolean isModuleFinished(int module) {
        Map<String, Object> _moduleDetail = getModule(module);
        return _moduleDetail != null && SafeConverter.toBoolean(_moduleDetail.get(FINISH.name));
    }

    public Map<String, Object> getModule(int module) {
        if (moduleDetail == null) {
            this.moduleDetail = new ArrayList<>();
        }

        Map<String, Object> _moduleDetail = null;
        for (Map<String, Object> progressRecord : moduleDetail) {
            Integer _module = MapUtils.getInteger(progressRecord, MODULE.name);
            if (Objects.equals(_module, module)) {
                _moduleDetail = progressRecord;
                break;
            }
        }

        if (_moduleDetail == null) {
            _moduleDetail = MapUtils.m(MODULE.name, module);
            this.moduleDetail.add(_moduleDetail);
        }

        return _moduleDetail;

        /*return Optional.ofNullable(moduleDetail)
                .orElse(moduleDetail = new ArrayList<>())
                .stream()
                .filter(md -> Objects.equals(md.get("module"),module))
                .findFirst()
                .orElseGet(() -> {
                    Map<String,Object> _m = MapUtils.m("module",module);
                    moduleDetail.add(_m);
                    return _m;
                });*/
    }

    public void recordProgress(Integer module,
                               String pageId,
                               String questionId,
                               Boolean finish,
                               Integer subScore,
                               Double totalScore,
                               String audioUrl) {
        if (module == null)
            return;

        Map<String, Object> _moduleDetail = getModule(module);
        // 只有第3模块需要历史
        List<Map<String, Object>> history = (List<Map<String, Object>>) _moduleDetail.get("history");
        if (module == 3 && history == null) {
            history = new ArrayList<>();
            _moduleDetail.put(HISTORY.name, history);
        }

        if (!StringUtils.isEmpty(pageId) && module == 1) {
            _moduleDetail.put(PAGE_ID.name, pageId);
        }

        if (!StringUtils.isEmpty(questionId)) {
            _moduleDetail.put(QUESTION_ID.name, questionId);
            if (module == 3) {
                Map<String, Object> entry = MapUtils.m(
                        QUESTION_ID.name, questionId,
                        SCORE.name, subScore,
                        AUDIO_URL.name, audioUrl);

                int existIndex = -1;
                for (int index = 0; index < history.size(); index++) {
                    Map<String, Object> _record = history.get(index);
                    if (Objects.equals(questionId, _record.getOrDefault(QUESTION_ID.name, 0L))) {
                        existIndex = index;
                        break;
                    }
                }

                // 如果存在则覆盖老的
                if (existIndex >= 0)
                    history.set(existIndex, entry);
                else
                    history.add(entry);
            }
        }

        if (totalScore != null && totalScore > 0) {
            _moduleDetail.put(SCORE.name, totalScore);
        }

        if (finish != null) {
            boolean orgFinish = SafeConverter.toBoolean(_moduleDetail.get(FINISH.name));
            // 如果原来已经是完成状态的，就不再更新了
            // 只更新非完成状态的
            if (!orgFinish) {
                _moduleDetail.put(FINISH.name, finish);
            }

            // 记录完成时间
            if (finish)
                _moduleDetail.put(FINISH_TIME.name, new Date().getTime());
        }
    }

    public void fill(int moduleNum) {
        if (moduleDetail == null) {
            this.moduleDetail = new ArrayList<>();
        }

        Map<Integer, Integer> moduleMap = this.moduleDetail.stream().collect(Collectors.toMap(md -> MapUtils.getInteger(md, MODULE.name), md -> 1));
        for (int i = 1; i <= moduleNum; i++) {
            if (!moduleMap.containsKey(i)) {
                this.moduleDetail.add(MapUtils.m(MODULE.name, i, FINISH.name, false));
            }
        }
    }

    public void recordScore(String questionId, double score) {
        if (scoreMap == null)
            scoreMap = new HashMap<>();

        scoreMap.put(questionId, score);
    }

    public Double getQuestionScore(String questionId) {
        if (scoreMap == null || !scoreMap.containsKey(questionId))
            return 0d;

        return SafeConverter.toDouble(scoreMap.get(questionId));
    }

    public int calTotalScore(int moduleNum) {
        if (moduleDetail == null)
            return 0;

        AtomicInteger finishCount = new AtomicInteger(0);
        int score = this.moduleDetail.stream()
                .filter(md -> SafeConverter.toBoolean(md.get(FINISH.name))) // 只看完成的
                .peek(c -> finishCount.incrementAndGet())
                .map(md -> BigDecimal.valueOf(toDouble(md.get(SCORE.name))))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(0, RoundingMode.HALF_UP)
                .min(BigDecimal.valueOf(100))
                .intValue();

        // 只有所有模块都完成了，才算分数
        if (finishCount.get() >= moduleNum)
            return score;
        else
            return 0;
    }

    /**
     * 描述模块进度的属性
     */
    @Getter
    public enum Attr {

        MODULE("module"),
        FINISH("finish"),
        FINISH_TIME("finishTime"),
        SCORE("score"),
        HISTORY("history"),
        PAGE_ID("pageId"),
        QUESTION_ID("questionId"),
        AUDIO_URL("audioUrl");

        private String name;

        Attr(String name) {
            this.name = name;
        }
    }

}
