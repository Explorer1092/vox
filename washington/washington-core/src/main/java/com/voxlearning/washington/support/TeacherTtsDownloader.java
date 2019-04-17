/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.support;

import com.mongodb.MongoGridFSException;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucket;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucketNamespace;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.lang.util.ZipUtils;
import com.voxlearning.alps.storage.gridfs.factory.GridFSBucketFactory;
import com.voxlearning.utopia.business.api.entity.TtsListeningPaper;
import com.voxlearning.utopia.business.api.mapper.TtsListeningQuestion;
import com.voxlearning.utopia.business.api.mapper.TtsListeningSentence;
import com.voxlearning.utopia.business.api.mapper.TtsListeningSubQuestion;
import freemarker.template.Template;
import lombok.Cleanup;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.inject.Named;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * @author Junjie Zhang
 * @since 2014-08-13
 */
@Named
public class TeacherTtsDownloader extends SpringContainerSupport {

    private boolean addZipEntryByResource(ZipArchiveOutputStream zos, String resourceFile, String fileName) {
        try {
            @Cleanup InputStream is = getClass().getResourceAsStream(resourceFile);
            ZipUtils.addZipEntry(zos, fileName, is);
            return true;
        } catch (Exception e) {
            logger.error("TTS add zip entry error", e);
            return false;
        }
    }

    private void getVoiceFilesBySubQuestion(TtsListeningQuestion question, Set<String> filesSet) {
        //小题
        if (question.getSubQuestions() != null) {
            for (TtsListeningSubQuestion subQuestion : question.getSubQuestions()) {
                if (subQuestion == null)
                    continue;
                //句子
                if (subQuestion.getSentences() != null) {
                    for (TtsListeningSentence sentence : subQuestion.getSentences()) {
                        if (sentence == null)
                            continue;
                        if (StringUtils.isNotEmpty(sentence.getVoice()))
                            filesSet.add(sentence.getVoice());
                    }
                }
            }
        }
    }

    private Set<String> getVoiceFiles(TtsListeningPaper paper) {
        Set<String> filesSet = new HashSet<>();
        //考前提示音
        if (StringUtils.isNotEmpty(paper.getBeginningVoice()))
            filesSet.add(paper.getBeginningVoice());
        else if (paper.getBeginningSentence() != null && StringUtils.isNotEmpty(paper.getBeginningSentence().getVoice()))
            filesSet.add(paper.getBeginningSentence().getVoice());
        //间隔音
        if (StringUtils.isNotEmpty(paper.getIntervalVoice()))
            filesSet.add(paper.getIntervalVoice());
        //大题
        if (paper.getQuestions() != null) {

            for (TtsListeningQuestion question : paper.getQuestions()) {
                if (question == null)
                    continue;
                // 大题提示音
                if (StringUtils.isNotEmpty(question.getTip()))
                    filesSet.add(question.getTip());
                else if (question.getTipSentence() != null && StringUtils.isNotEmpty(question.getTipSentence().getVoice()))
                    filesSet.add(question.getTipSentence().getVoice());
                // 小题
                getVoiceFilesBySubQuestion(question, filesSet);
            }
        }

        //考后提示音
        if (StringUtils.isNotEmpty(paper.getEndingVoice()))
            filesSet.add(paper.getEndingVoice());
        else if (paper.getEndingSentence() != null && StringUtils.isNotEmpty(paper.getEndingSentence().getVoice()))
            filesSet.add(paper.getEndingSentence().getVoice());
        return filesSet;
    }

    /**
     * 生成下载资源
     * 注意：这里没有关闭流！controller关闭流
     */
    public void downloadTtsResource(TtsListeningPaper paper, List<TtsListeningSentence> playList, ZipArchiveOutputStream zos) {
        if (paper == null) {
            return;
        }
        try {
            // FIXME: =========================================================
            // FIXME: Use StorageClient instead
            // FIXME: =========================================================
            GridFSBucketNamespace namespace = new GridFSBucketNamespace("vox-tts");
            GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-tts", namespace);

            //收集听力试卷中的所有音频，使用Set去重
            Set<String> filesSet = getVoiceFiles(paper);
            filesSet.stream()
                    .filter(ObjectId::isValid)
                    .forEach(n -> {
                        try {
                            @Cleanup GridFSDownloadStream downloadStream = bucket.openDownloadStream(new ObjectId(n));
                            ZipUtils.addZipEntry(zos, "js/" + n + ".mp3", downloadStream);
                        } catch (MongoGridFSException ignored) {
                        }
                    });

            addZipEntryByResource(zos, "/resource/tts/jquery-1.7.1.min.js", "js/jquery-1.7.1.min.js");
            addZipEntryByResource(zos, "/resource/tts/jquery.jplayer.min.js", "js/jquery.jplayer.min.js");
            addZipEntryByResource(zos, "/resource/tts/Jplayer.swf", "js/Jplayer.swf");
            addZipEntryByResource(zos, "/resource/tts/zuoye_logo.png", "js/zuoye_logo.png");

            addZipEntryByResource(zos, "/resource/tts/m.png", "js/m.png");
            addZipEntryByResource(zos, "/resource/tts/f.png", "js/f.png");
            addZipEntryByResource(zos, "/resource/tts/c.png", "js/c.png");
            addZipEntryByResource(zos, "/resource/tts/bf.png", "js/bf.png");
            addZipEntryByResource(zos, "/resource/tts/bm.png", "js/bm.png");

            addZipEntryByResource(zos, "/resource/tts/YQZuoyeTtsClient.exe", "双击播放.exe");

            Map<String, Object> map = new HashMap<>();
            map.put("paper", paper);
            map.put("playList", JsonUtils.toJson(playList));
            Template headerTemplate = FreemarkerTemplateParser.parse("/resource/tts/view.ftl");
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(headerTemplate, map);
            ZipUtils.addZipEntry(zos, "PAPER.html", content);

        } catch (Exception ex) {
            logger.warn("Error occurs when downloading tts resource '{}'", paper, ex);
        }
    }

    /**
     * 生成下载MP3
     * 注意：这里没有关闭流！controller关闭流
     */
    public void downloadTtsMp3(String id, OutputStream outputStream) {
        if (!ObjectId.isValid(id)) {
            return;
        }

        GridFSDownloadStream downloadStream = null;
        try {
            // FIXME: =========================================================
            // FIXME: Use StorageClient instead
            // FIXME: =========================================================
            GridFSBucketNamespace namespace = new GridFSBucketNamespace("vox-tts");
            GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-tts", namespace);

            try {
                downloadStream = bucket.openDownloadStream(new ObjectId(id));
            } catch (MongoGridFSException ex) {
                downloadStream = null;
            }
            if (downloadStream == null) {
                logger.warn("TTS mp3 not found :" + id);
                return;
            }
            byte[] content = IOUtils.toByteArray(downloadStream);
            outputStream.write(content);
            outputStream.flush();
        } catch (Exception ex) {
            logger.warn("Error occurs when downloading tts MP3 '{}'", id, ex);
        } finally {
            IOUtils.closeQuietly(downloadStream);
        }
    }

    public void downloadTtsOfflinePapers(Collection<String> papers, ZipArchiveOutputStream zos) {
        if (CollectionUtils.isEmpty(papers)) {
            return;
        }
        try {
            // FIXME: =========================================================
            // FIXME: Use StorageClient instead
            // FIXME: =========================================================
            GridFSBucketNamespace namespace = new GridFSBucketNamespace("vox-exam-storage");
            GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);

            papers.stream()
                    .filter(ObjectId::isValid)
                    .forEach(n -> {
                        try {
                            @Cleanup GridFSDownloadStream downloadStream = bucket.openDownloadStream(new ObjectId(n));
                            ZipUtils.addZipEntry(zos, n + ".jpg", downloadStream);
                        } catch (MongoGridFSException ignored) {
                        }
                    });
        } catch (Exception ex) {
            logger.warn("Error occurs when downloading tts resource ", ex);
        }
    }

    public void downloadOfflineTtsImg(String id, OutputStream outputStream) {
        if (!ObjectId.isValid(id)) {
            return;
        }
        GridFSDownloadStream downloadStream = null;
        try {
            // FIXME: =========================================================
            // FIXME: Use StorageClient instead
            // FIXME: =========================================================
            GridFSBucketNamespace namespace = new GridFSBucketNamespace("vox-exam-storage");
            GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);

            try {
                downloadStream = bucket.openDownloadStream(new ObjectId(id));
            } catch (Exception ex) {
                downloadStream = null;
            }
            if (downloadStream == null) {
                logger.warn("TTS mp3 not found :" + id);
                return;
            }

            byte[] content = IOUtils.toByteArray(downloadStream);
            outputStream.write(content);
            outputStream.flush();
        } catch (Exception ex) {
            logger.warn("Error occurs when downloading tts MP3 '{}'", id, ex);
        } finally {
            IOUtils.closeQuietly(downloadStream);
        }
    }
}
