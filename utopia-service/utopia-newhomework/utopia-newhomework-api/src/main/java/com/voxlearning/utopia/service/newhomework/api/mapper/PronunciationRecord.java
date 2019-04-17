package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.UnisoundScoreLevel;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkSyllable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 发音处理工具类
 */

public class PronunciationRecord implements Serializable {
    private static final long serialVersionUID = -4030294758620795160L;
    @Getter
    @Setter
    private String bookId;

    private Set<String> weakWords = new HashSet<>();

    @Getter
    @Setter
    private int count;//没有达标的单词数目
    @Getter
    @Setter
    private List<Map> unitAndSentenceList = new LinkedList<>();
    @Getter
    @Setter
    private List<Word> words = new LinkedList<>(); //单词信息
    @Getter
    @Setter
    private List<Line> lines = new LinkedList<>(); //句子信息

    //初始化数据,转化数据
    public PronunciationRecord(Collection<NewHomeworkSyllable> newHomeworkSyllableComparable) {
        //原始数据转化
        List<PronunciationRecord.LineData> lineDatas = newHomeworkSyllableComparable.stream()
                .filter(o -> o.getBookId() != null)
                .filter(o -> o.getUnitId() != null)
                .filter(o -> o.getSentenceId() != null)
                .filter(o -> o.getLines() != null)
                .filter(o -> o.getCreateAt() != null)
                .map(o -> new PronunciationRecord.LineData(o.getBookId(), o.getUnitId(), o.getSentenceId(), o.getLines(), o.getCreateAt()))
                .filter(o -> !o.isOk)
                .collect(Collectors.toList());
        //给出没有去重和标点符号问题的处理
        for (LineData lineData : lineDatas) {
            Map map = new HashMap<>();
            map.put("unit_id", lineData.unitId);
            map.put("sentence_id", lineData.sentenceId);
            unitAndSentenceList.add(map);
            if (bookId == null) {
                bookId = lineData.bookId;
            }
            if (lineData.wordFlag) {
                if (CollectionUtils.isEmpty(lineData.words)) {
                    continue;
                }
                Word word = lineData.words.get(0);
                word.createAt = lineData.createAt.getTime();
                words.add(word);
            } else {
                Line line = new Line();
                line.text = lineData.sample;
                for (Word w : lineData.words) {
                    line.words.add(w);
                }
                line.createAt = lineData.createAt.getTime();
                lines.add(line);
            }
        }
        //保障标点符号问题
        transmute();
        //去重和个数统计
        duplicateRemoval();
    }

    /**
     * 单词
     */
    @Getter
    @Setter
    public static class Word implements Serializable {
        private static final long serialVersionUID = 2454507667274940306L;
        private String text;//单词
        private long createAt;
        private boolean weak; //是否弱
        private List<SubWord> subWords = new LinkedList<>();
    }

    @Getter
    @Setter
    public static class SubWord implements Serializable {
        private static final long serialVersionUID = 1584179106263884701L;
        private String subtext;
        private boolean weak;
    }

    /**
     * 库内数据转化包含句子和单词工具类
     */
    private static class LineData implements Serializable {
        private static final long serialVersionUID = -8761359067060484158L;
        private String unitId;
        private String sentenceId;
        private String bookId;
        private boolean wordFlag;
        private String usertext;
        private String sample;
        private Date createAt;
        private List<Word> words = new LinkedList<>();
        private boolean isOk;

        LineData(String bookId, String unitId, String sentenceId, List<NewHomeworkSyllable.Sentence> sentences, Date createAt) {
            this.bookId = bookId;
            this.unitId = unitId;
            this.createAt = createAt;
            this.sentenceId = sentenceId;
            processLineData(sentences);//转义句子

        }

        /**
         * 将原始数据转化单词和句子，但是没有去重，也没有标点符号存在问题
         */
        private void processLineData(List<NewHomeworkSyllable.Sentence> sentences) {
            if (CollectionUtils.isNotEmpty(sentences)) {
                //this.isOk = true;
                NewHomeworkSyllable.Sentence sentence = sentences.get(0);
                if (sentence.getSample() != null && sentence.getWords() != null) {
                    sample = sentence.getSample();
                    usertext = sentence.getUsertext();
                    List<NewHomeworkSyllable.Word> words = sentence.getWords();
                    this.wordFlag = sentence.getWords().size() == 1;//是否是单词
                    for (NewHomeworkSyllable.Word o : words) {
                        if (o.getText() != null && o.getSubwords() != null) {
                            PronunciationRecord.Word word = new PronunciationRecord.Word();
                            word.text = SafeConverter.toString(o.getText());
                            if (this.wordFlag) {
                                for (Map<String, Object> subWordMap : o.getSubwords()) {
                                    if (subWordMap.containsKey("subtext") && subWordMap.containsKey("score")) {
                                        PronunciationRecord.SubWord subWord = new PronunciationRecord.SubWord();
                                        subWord.subtext = SafeConverter.toString(subWordMap.get("subtext"), "");
                                        double score = SafeConverter.toDouble(subWordMap.get("score"));
                                        if (score * 10 <= UnisoundScoreLevel.C.getMaxScore()) {
                                            subWord.weak = true;
                                            word.weak = true;
                                        }
                                        word.subWords.add(subWord);
                                    } else {
                                        this.isOk = false;
                                    }
                                }
                            } else {
                                if (CollectionUtils.isNotEmpty(o.getSubwords())) {//句子单词
                                    if (SafeConverter.toDouble(o.getScore()) * 10 <= UnisoundScoreLevel.C.getMaxScore()) {
                                        word.weak = true;
                                    }
                                }
                            }
                            this.words.add(word);
                        } else {
                            this.isOk = false;
                        }
                    }
                } else {
                    this.isOk = false;
                }
            }
        }
    }


    /**
     * 库内数据转化出来的句子
     */
    @Getter
    @Setter
    public static class Line implements Serializable {
        private static final long serialVersionUID = -5310140820650692932L;
        private String text;
        private long createAt;
        private List<Word> words = new LinkedList<>();
    }


    /**
     * 转化为前端需要的方式
     */
    private void transmute() {

        if (CollectionUtils.isNotEmpty(this.lines)) {

            //句子处理，防止传送数据没有空格符和标点符号、
            this.lines = this.lines.stream()
                    .filter(o -> o.words != null)
                    .map(line -> {
                        try {
                            char[] chars = line.text.toCharArray();
                            int index = 0;
                            List<Word> target = new LinkedList<>();
                            for (Word w : line.words) {
                                char[] chars1 = w.text.toCharArray();
                                while (chars[index] != chars1[0]) {
                                    Word word = new Word();
                                    word.text = "" + chars[index];
                                    word.weak = false;
                                    target.add(word);
                                    index++;
                                }
                                target.add(w);
                                index += chars1.length;
                            }
                            if (index < chars.length) {
                                for (int i = index; i < chars.length; i++) {
                                    Word word = new Word();
                                    word.text = "" + chars[index];
                                    word.weak = false;
                                    target.add(word);
                                }
                            }
                            line.words = target;
                            return line;
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    /**
     * 去重
     */
    private void duplicateRemoval() {
        if (CollectionUtils.isNotEmpty(this.words)) {
            Map<String, Word> wordMap = new HashMap<>();
            for (Word word : this.words) {
                if (wordMap.containsKey(SafeConverter.toString(word.text).toLowerCase())) {
                    //word duplicate Removal
                    Word w = wordMap.get(SafeConverter.toString(word.text).toLowerCase());
                    if (word.subWords.size() == w.subWords.size()) {
                        for (int i = 0; i < w.subWords.size(); i++) {
                            if (word.subWords.get(i).weak && (!w.subWords.get(i).weak)) {
                                w.subWords.get(i).weak = true;

                            }
                        }
                    }
                } else {
                    wordMap.put(SafeConverter.toString(word.text).toLowerCase(), word);
                }
            }
            this.words = new LinkedList<>(wordMap.values());
            this.words.sort((o1, o2) -> Long.compare(o1.getCreateAt(), o2.getCreateAt()));
            this.weakWords.addAll(wordMap.keySet());
        }
        if (CollectionUtils.isNotEmpty(this.lines)) {
            Map<String, Line> lineMap = new HashMap<>();
            for (Line line : this.lines) {
                if (lineMap.containsKey(line.text)) {
                    //line duplicate Removal
                    Line l = lineMap.get(line.text);
                    for (int i = 0; i < line.words.size(); i++) {
                        if (line.words.get(i).weak && !l.words.get(i).weak) {
                            l.words.get(i).weak = true;
                        }
                    }
                } else {
                    lineMap.put(line.text, line);
                }
                for (Word word : line.words) {
                    if (word.weak) {
                        this.weakWords.add(SafeConverter.toString(word.text).toLowerCase());
                    }
                }
            }
            this.lines = new LinkedList<>(lineMap.values());
            this.lines.sort((o1, o2) -> Long.compare(o1.getCreateAt(), o2.getCreateAt()));
        }

        this.count = this.weakWords.size();

    }
}
