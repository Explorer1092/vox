package com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.personal;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class BasicReviewHomeworkPersonalEnglishReport implements Serializable {
    private static final long serialVersionUID = -1334373646927577785L;
    private String stageName;
    private Subject subject;
    private WordPart wordPart;
    private SentencePart sentencePart;

    @Getter
    @Setter
    public static class WordPart implements Serializable {
        private int totalWord;
        private int personalWrongNum;
        private Set<String> words = new LinkedHashSet<>();
        private int bestWrongNum;
    }

    @Getter
    @Setter
    public static class SentencePart implements Serializable {
        private static final long serialVersionUID = 9176453414620831400L;
        private int totalSentenceNum;
        private int personalWrongNum;
        private Set<String> sentences = new LinkedHashSet<>();
        private int bestWrongNum;
    }

    @Getter
    @Setter
    public static class StudentDetail implements Serializable {
        private static final long serialVersionUID = 2838768509005036437L;
        private Set<String> wrongWords = new LinkedHashSet<>();
        private Set<String> wrongSentences = new LinkedHashSet<>();
    }

}
