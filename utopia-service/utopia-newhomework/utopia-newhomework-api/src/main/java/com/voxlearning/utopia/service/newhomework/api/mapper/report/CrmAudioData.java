package com.voxlearning.utopia.service.newhomework.api.mapper.report;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class CrmAudioData implements Serializable {
    private static final long serialVersionUID = -1224524829648483285L;

    private String version;

    private List<LineData> lines = new LinkedList<>();

    @Getter
    @Setter
    public static class LineData implements Serializable {
        private String sample;
        private String usertext;
        private Double begin;
        private Double end;
        private Double score;
        private Double standardScore;
        private String businessLevel;
        private Double fluency;
        private Double integrity;
        private Double pronunciation;
        private List<WordData> words = new LinkedList<>();
    }

    @Getter
    @Setter
    public static class WordData implements Serializable {
        private static final long serialVersionUID = -9027114353401580580L;
        private String text;
        private Double type;
        private Double begin;
        private Double end;
        private Double volume;
        private Double score;
        private List<SubwordData> subwords = new LinkedList<>();

    }

    @Getter
    @Setter
    public static class SubwordData implements Serializable {

        private static final long serialVersionUID = -206816929524076515L;
        private String subtext;
        private Double volume;
        private Double begin;
        private Double end;
        private Double score;
    }

    @Getter
    @Setter
    public static class Summary implements Serializable {
        private static final long serialVersionUID = 8048854444220552219L;
        private String content = "";

        private String quota = "";

        private String detailed = "";

        private String week = "";

        private boolean flag;

    }


    public Summary refineInfo() {
        Summary summary = new Summary();
        if (CollectionUtils.isNotEmpty(lines)) {
            LineData lineData = lines.get(0);
            if (CollectionUtils.isNotEmpty(lineData.getWords())) {

                summary.setContent(SafeConverter.toString(lineData.getUsertext()));

                if (lineData.getWords().size() > 1) {
                    StringBuilder quotaStr = new StringBuilder();
                    quotaStr.append("总分:")
                            .append(SafeConverter.toDouble(lineData.getScore()))
                            .append(",")
                            .append("流利度:")
                            .append(SafeConverter.toDouble(lineData.getFluency()))
                            .append(",")
                            .append("发音完整度:")
                            .append(SafeConverter.toDouble(lineData.getIntegrity()))
                            .append(",")
                            .append("发音准度:")
                            .append(SafeConverter.toDouble(lineData.getPronunciation()));
                    summary.setQuota(quotaStr.toString());

                    StringBuilder detailedStr = new StringBuilder();


                    StringBuilder weekStr = new StringBuilder();

                    for (WordData wordData : lineData.getWords()) {
                        if (CollectionUtils.isNotEmpty(wordData.getSubwords())) {

                            if (SafeConverter.toDouble(wordData.getScore()) <= 5) {

                                weekStr.append(wordData.getText())
                                        .append(":")
                                        .append(SafeConverter.toDouble(wordData.getScore()))
                                        .append(",");
                            }

                            detailedStr.append("/")
                                    .append(wordData.getText())
                                    .append("/")
                                    .append(":")
                                    .append(SafeConverter.toDouble(wordData.getScore()))
                                    .append(",");
                        }
                    }
                    if (detailedStr.length() > 1) {
                        summary.setDetailed(detailedStr.deleteCharAt(detailedStr.length() - 1).toString());
                    }

                    if (weekStr.length() > 1) {
                        summary.setWeek(weekStr.deleteCharAt(weekStr.length() - 1).toString());
                    }
                    summary.setFlag(true);

                } else if (lineData.getWords().size() == 1) {
                    StringBuilder quotaStr = new StringBuilder();
                    quotaStr.append("总分:")
                            .append(SafeConverter.toDouble(lineData.getScore()))
                            .append(",")
                            .append("流利度:")
                            .append(SafeConverter.toDouble(lineData.getFluency()))
                            .append(",")
                            .append("发音完整度:")
                            .append(SafeConverter.toDouble(lineData.getIntegrity()))
                            .append(",")
                            .append("发音准度:")
                            .append(SafeConverter.toDouble(lineData.getPronunciation()));
                    summary.setQuota(quotaStr.toString());

                    WordData wordData = lineData.getWords().get(0);

                    if (CollectionUtils.isNotEmpty(wordData.getSubwords())) {

                        StringBuilder detailedStr = new StringBuilder();

                        StringBuilder weekStr = new StringBuilder();

                        for (SubwordData subwordData : wordData.getSubwords()) {

                            if (SafeConverter.toDouble(subwordData.getScore()) <= 5) {

                                weekStr.append(subwordData.getSubtext())
                                        .append(":")
                                        .append(SafeConverter.toDouble(subwordData.getScore()))
                                        .append(",");
                            }

                            detailedStr.append("/")
                                    .append(subwordData.getSubtext())
                                    .append("/")
                                    .append(":")
                                    .append(SafeConverter.toDouble(subwordData.getScore()))
                                    .append(",");

                        }

                        if (detailedStr.length() > 1) {
                            summary.setDetailed(detailedStr.deleteCharAt(detailedStr.length() - 1).toString());
                        }

                        if (weekStr.length() > 1) {
                            summary.setWeek(weekStr.deleteCharAt(weekStr.length() - 1).toString());
                        }
                        summary.setFlag(true);
                    }
                }
            }

        }
        return summary;
    }


}
