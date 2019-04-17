package com.voxlearning.washington.service.tts;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.business.api.entity.TtsListeningPaper;
import com.voxlearning.utopia.business.api.mapper.TtsListeningQuestion;
import com.voxlearning.utopia.business.api.mapper.TtsListeningSentence;
import com.voxlearning.utopia.business.api.mapper.TtsListeningSubQuestion;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Junjie Zhang
 * @since 2016-03-17
 */
public class TtsHtmlHelper {
    private static final float DEFAULT_PAUSE = 1f;                                             //默认停顿间隔 单位s

    private static void buildQuestionPlayList(TtsListeningPaper paper, TtsListeningQuestion question, Float questionPause, List<TtsListeningSentence> list) {

        for (TtsListeningSubQuestion subQuestion : question.getSubQuestions()) {
            if (subQuestion.getSentences() == null || subQuestion.getSentences().size() == 0) {
                continue;
            }
            for (int i = 0; i < question.getPlayTimes(); i++) {
                //间隔音
                if (StringUtils.isNotEmpty(paper.getIntervalVoice())) {
                    list.add(new TtsListeningSentence(paper.getIntervalVoice(), DEFAULT_PAUSE));
                }
                int length = subQuestion.getSentences().size();
                //小题句子
                for (int j = 0; j < length; j++) {
                    TtsListeningSentence sentence = subQuestion.getSentences().get(j);
                    if (sentence != null && StringUtils.isNotEmpty(sentence.getVoice())) {
                        float pause = sentence.getPause() == null ? DEFAULT_PAUSE : sentence.getPause();
                        //最后一句话加上大题默认间隔
                        if (j == length - 1) {
                            if (questionPause == null)
                                questionPause = DEFAULT_PAUSE;
                            pause += questionPause;
                        }
                        list.add(new TtsListeningSentence(sentence.getVoice(), pause));

                    }
                }
            }
        }

    }

    public static List<TtsListeningSentence> getPlayList(TtsListeningPaper paper) {
        List<TtsListeningSentence> list = new ArrayList<>();
        //html格式
        //考前提示音
        if (StringUtils.isNotEmpty(paper.getBeginningVoice())) {
            list.add(new TtsListeningSentence(paper.getBeginningVoice(), DEFAULT_PAUSE));
        } else if (paper.getBeginningSentence() != null && StringUtils.isNotEmpty(paper.getBeginningSentence().getVoice())) {
            list.add(new TtsListeningSentence(paper.getBeginningSentence().getVoice(), DEFAULT_PAUSE));
        }

        if (paper.getQuestions() != null) {
            for (TtsListeningQuestion question : paper.getQuestions()) {
                if (question == null)
                    continue;
                //大题提示
                if (StringUtils.isNotEmpty(question.getTip())) {
                    list.add(new TtsListeningSentence(question.getTip(), DEFAULT_PAUSE));
                } else if (question.getTipSentence() != null && StringUtils.isNotEmpty(question.getTipSentence().getVoice())) {
                    list.add(new TtsListeningSentence(question.getTipSentence().getVoice(), DEFAULT_PAUSE));
                }

                //播放小题
                if (question.getSubQuestions() != null) {
                    buildQuestionPlayList(paper, question, question.getInterval(), list);
                }
            }
        }
        //考后提示音
        if (StringUtils.isNotEmpty(paper.getEndingVoice())) {
            list.add(new TtsListeningSentence(paper.getEndingVoice(), DEFAULT_PAUSE));
        } else if (paper.getEndingSentence() != null && StringUtils.isNotEmpty(paper.getEndingSentence().getVoice())) {
            list.add(new TtsListeningSentence(paper.getEndingSentence().getVoice(), DEFAULT_PAUSE));
        }
        return list;
    }

    /**
     * 计算大题时长
     *
     * @param paper
     * @param question
     * @param questionPause
     * @return
     */
    private static float calcQuestionDuration(TtsListeningPaper paper, TtsListeningQuestion question, Float questionPause) {
        float total = 0f;
        for (TtsListeningSubQuestion subQuestion : question.getSubQuestions()) {
            if (subQuestion.getSentences() == null || subQuestion.getSentences().size() == 0) {
                continue;
            }
            for (int i = 0; i < question.getPlayTimes(); i++) {
                //间隔音
                if (StringUtils.isNotEmpty(paper.getIntervalVoice())) {
                    total += 0.1 + DEFAULT_PAUSE;
                }
                int length = subQuestion.getSentences().size();
                //小题句子
                for (int j = 0; j < length; j++) {
                    TtsListeningSentence sentence = subQuestion.getSentences().get(j);
                    if (sentence != null && StringUtils.isNotEmpty(sentence.getVoice())) {
                        float pause = sentence.getPause() == null ? DEFAULT_PAUSE : sentence.getPause();
                        //最后一句话加上大题默认间隔
                        if (j == length - 1) {
                            if (questionPause == null)
                                questionPause = DEFAULT_PAUSE;
                            pause += questionPause;
                        }
                        if (sentence.getDuration() != null)
                            total += sentence.getDuration() + pause;
                        else
                            total += 1 + pause;

                    }
                }
            }
        }
        return total;
    }

    public static float calcPaperDuration(TtsListeningPaper paper) {
        float total = 0f;
        //考前提示音
        if (StringUtils.isNotEmpty(paper.getBeginningVoice())) {
            total += 2 + DEFAULT_PAUSE;
        } else if (paper.getBeginningSentence() != null && paper.getBeginningSentence().getDuration() != null) {
            total += paper.getBeginningSentence().getDuration() + DEFAULT_PAUSE;
        } else {
            total += DEFAULT_PAUSE * 2;
        }

        if (paper.getQuestions() != null) {
            for (TtsListeningQuestion question : paper.getQuestions()) {
                if (question == null)
                    continue;
                //大题提示
                if (StringUtils.isNotEmpty(question.getTip())) {
                    total += 1 + DEFAULT_PAUSE;
                } else if (question.getTipSentence() != null && question.getTipSentence().getDuration() != null) {
                    total += question.getTipSentence().getDuration() + DEFAULT_PAUSE;
                } else {
                    total += DEFAULT_PAUSE * 2;
                }

                //播放小题
                if (question.getSubQuestions() != null) {
                    total += calcQuestionDuration(paper, question, question.getInterval());
                }
            }
        }
        //考后提示音
        if (StringUtils.isNotEmpty(paper.getEndingVoice())) {
            total += 2 + DEFAULT_PAUSE;
        } else if (paper.getEndingSentence() != null && paper.getEndingSentence().getDuration() != null) {
            total += paper.getEndingSentence().getDuration() + DEFAULT_PAUSE;
        } else {
            total += DEFAULT_PAUSE * 2;
        }
        return total;
    }
}
