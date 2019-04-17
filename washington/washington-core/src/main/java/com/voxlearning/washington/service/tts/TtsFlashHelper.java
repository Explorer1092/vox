package com.voxlearning.washington.service.tts;

import com.voxlearning.utopia.business.api.entity.TtsListeningPaper;
import com.voxlearning.utopia.business.api.mapper.TtsListeningSentence;
import com.voxlearning.utopia.business.api.mapper.TtsListeningTag;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Junjie Zhang
 * @since 2016-03-17
 */
public class TtsFlashHelper {
    private static final float DEFAULT_PAUSE = 1f;    //默认停顿间隔 单位s
    private static final float DEFAULT_SENTENCE_PAUSE = 0.3f;    //默认停顿间隔 单位s

    public static List<TtsListeningSentence> getPlayList(TtsListeningPaper paper) {
        if (paper.getTagList() != null) {
            return buildPlayList(paper.getTagList(), 1);
        }
        return new ArrayList<>();
    }

    private static List<TtsListeningSentence> buildPlayList(List<TtsListeningTag> tags, Integer loopTimes) {
        List<TtsListeningSentence> list = new ArrayList<>();
        List<TtsListeningTag> blocks = null;
        if (loopTimes == null || loopTimes < 1)
            loopTimes = 1;
        for (int i = 0; i < loopTimes; i++) {
            for (TtsListeningTag tag : tags) {
                if (tag == null)
                    continue;
                switch (tag.getTagType()) {
                    case LOOP:
                        if (blocks == null) {
                            blocks = new ArrayList<>();
                        } else {
                            list.addAll(buildPlayList(blocks, tag.getLoopTimes()));
                            blocks = null;
                        }
                        break;
                    case PAUSE:
                    case SENTENCE:
                        //如果循环块内
                        if (blocks != null) {
                            blocks.add(tag);
                            continue;
                        }
                        list.add(tag.getSentence());
                        break;
                    default:
                        break;
                }
            }
        }
        return list;
    }

    private static float calcTagList(List<TtsListeningTag> tags, Integer loopTimes) {
        List<TtsListeningTag> blocks = null;
        if (loopTimes == null || loopTimes < 1)
            loopTimes = 1;
        float total = 0;
        for (TtsListeningTag tag : tags) {
            if (tag == null)
                continue;

            switch (tag.getTagType()) {
                case LOOP:
                    if (blocks == null) {
                        blocks = new ArrayList<>();
                    } else {
                        total += calcTagList(blocks, tag.getLoopTimes());
                        blocks = null;
                    }
                    break;
                case PAUSE:
                    //如果循环块内
                    if (blocks != null) {
                        blocks.add(tag);
                        continue;
                    }
                    if (tag.getSentence() == null || tag.getSentence().getPause() == null)
                        continue;
                    total += tag.getSentence().getPause();
                    break;
                case SENTENCE:
                    //如果循环块内
                    if (blocks != null) {
                        blocks.add(tag);
                        continue;
                    }
                    if (tag.getSentence() == null)
                        continue;
                    if (tag.getSentence().getDuration() != null)
                        total += tag.getSentence().getDuration();
                    else
                        total += DEFAULT_PAUSE;
                    if (tag.getSentence().getPause() != null)
                        total += tag.getSentence().getPause();
                    else
                        total += DEFAULT_SENTENCE_PAUSE;
                    break;
                default:
                    break;
            }

        }
        return total * loopTimes;
    }

    public static float calcPaperDuration(TtsListeningPaper paper) {
        if (paper.getTagList() != null) {
            return calcTagList(paper.getTagList(), 1);
        }
        return 0f;
    }
}
