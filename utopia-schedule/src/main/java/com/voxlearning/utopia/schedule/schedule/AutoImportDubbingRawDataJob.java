package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.alps.spi.storage.StorageSystem;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.dubbing.api.DubbingRawService;
import com.voxlearning.utopia.service.dubbing.api.entity.DubbingRaw;
import lombok.Cleanup;

import javax.inject.Named;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: wei.jiang
 * @Date: Created on 2017/12/12
 */
@Named
@ScheduledJobDefinition(
        jobName = "配音原始数据抓取及导入",
        jobDescription = "手动执行(长期使用)",
        disabled = {Mode.UNIT_TEST, Mode.STAGING, Mode.DEVELOPMENT},
        ENABLED = false,
        cronExpression = "0 0 8 * * ? "
)
@ProgressTotalWork(100)
public class AutoImportDubbingRawDataJob extends ScheduledJobWithJournalSupport {
    @StorageClientLocation(storage = "news-video-content")
    private StorageClient storageClient_videoContent;

    @ImportService(interfaceClass = DubbingRawService.class)
    private DubbingRawService dubbingRawService;

    @StorageClientLocation(system = StorageSystem.GFS, storage = "fs-default")
    private StorageClient gfsClient;

    @Override
    @SuppressWarnings("unchecked")
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        List<Map<String, Object>> dubbing_data_list = (List<Map<String, Object>>) parameters.get("dubbing_data_list");
        boolean is_category_data = SafeConverter.toBoolean(parameters.get("is_category_data"));
        if (CollectionUtils.isEmpty(dubbing_data_list) || StringUtils.isBlank(SafeConverter.toString(parameters.get("is_category_data")))) {
            return;
        }
        ISimpleProgressMonitor iSimpleProgressMonitor = progressMonitor.subTask(100, dubbing_data_list.size());
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Map<String, Object> p : dubbing_data_list) {
            String parentId = SafeConverter.toString(parameters.get("parentId"));
            if (!is_category_data) {
                Map<String, Object> currentDubbingMap;
                currentDubbingMap = (Map<String, Object>) p.get("data");
                if (MapUtils.isEmpty(currentDubbingMap)) {
                    continue;
                }
                mapList.add(currentDubbingMap);
                if (dubbing_data_list.indexOf(p) + 1 != dubbing_data_list.size()) {
                    continue;
                }
            } else {
                mapList = (List<Map<String, Object>>) p.get("data");
            }
            if (StringUtils.isBlank(parentId)) {
                parentId = SafeConverter.toString(p.get("parentId"));
            }
//            progressMonitor.worked(10);
//        List<Dubbing> list = new ArrayList<>();
//            ISimpleProgressMonitor iSimpleProgressMonitor = progressMonitor.subTask(80, mapList.size());
            String finalParentId = parentId;
            List<Map<String, Object>> finalMapList = mapList;
            mapList.forEach(e -> {
                DubbingRaw dubbing = new DubbingRaw();
                if (StringUtils.isNotBlank(finalParentId)) {
                    dubbing.setCategoryId(finalParentId);
                }
                String videoSrt = SafeConverter.toString(e.get("video_srt"));
                if (StringUtils.isNotBlank(videoSrt)) {
                    String video_url = uploadVideoAndGenerateVideoUrl(videoSrt);
                    if (StringUtils.isNotBlank(video_url)) {
                        video_url = "https://v.17zuoye.cn/" + video_url;
                    } else {
                        logger.info("获取视频失败！名称：" + SafeConverter.toString(e.get("title")));
                        return;
                    }
                    dubbing.setVideoUrl(video_url);
                }
                dubbing.setRank(finalMapList.indexOf(e) + 1);
                dubbing.setDifficult(SafeConverter.toInt(e.get("dif_level")));
                dubbing.setVideoName(SafeConverter.toString(e.get("title")));
                dubbing.setVideoSummary(SafeConverter.toString(e.get("description")));
                String audio = SafeConverter.toString(e.get("audio"));
                if (StringUtils.isNotBlank(audio)) {
                    String audio_url = uploadAudioAndGenerateAudioUrl(audio);
                    if (StringUtils.isNotBlank(audio_url)) {
                        String url = ConfigManager.getInstance().getCommonConfig().getConfigs().get("image_url") + audio_url;
                        dubbing.setBackgroundMusic(url);
                    } else {
                        logger.info("获取音频失败！名称：" + SafeConverter.toString(e.get("title")));
                        return;
                    }
                }
                String picUrl = SafeConverter.toString(e.get("pic"));
                String img = uploadImg(picUrl);
                if (StringUtils.isNotBlank(img)) {
                    String url = ConfigManager.getInstance().getCommonConfig().getConfigs().get("image_url") + img;
                    dubbing.setCoverUrl(url);
                }
                String srtUrl = SafeConverter.toString(e.get("subtitle_en"));
                try {
                    List<DubbingRaw.DubbingSentence> dubbingSentences = generateDubbingSentences(srtUrl);
                    dubbing.setSentences(dubbingSentences);
                    dubbing.setSrtUrl(srtUrl);
                } catch (IOException e1) {
                    logger.warn("get srt error");
                }
//            String dubbing_json = JsonUtils.toJson(dubbing);
//            Document document = Document.parse(dubbing_json);
//            list.add(dubbing);
                if (CollectionUtils.isNotEmpty(dubbing.getSentences())) {
                    dubbing.setIsSync(Boolean.TRUE);
                } else {
                    dubbing.setIsSync(Boolean.FALSE);
                }
                dubbingRawService.upsertDubbingRaw(dubbing);
            });
            iSimpleProgressMonitor.worked(1);
        }

//        MongoClient mongoClient = MongoClientBuilder.getInstance().getMongoClient("mongo-plat");
//        MongoDatabase database = mongoClient.getClient().getDatabase("vox-question");
////        database.createCollection("online_dubbing");
//        MongoCollection<Document> dubbing = database.getCollection("online_dubbing");
//        progressMonitor.worked(10);
//        dubbing.withWriteConcern(WriteConcern.ACKNOWLEDGED).insertMany(list);
        progressMonitor.done();

    }

    private String uploadVideoAndGenerateVideoUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return "";
        }
        AlpsHttpResponse execute = HttpRequestExecutor.defaultInstance().get(url).execute();
        if (execute != null) {
            StorageMetadata storageMetadata = new StorageMetadata();
            if (execute.getOriginalResponse() == null) {
                logger.warn("not response");
                return "";
            }
            try {
                @Cleanup ByteArrayInputStream inStream = new ByteArrayInputStream(execute.getOriginalResponse());
                String path = "dubbing" + generateUploadPrefix("video") + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
                storageMetadata.setContentLength(SafeConverter.toLong(execute.getOriginalResponse().length));
//            String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date());
                String fileName = new Date().getTime() + RandomStringUtils.randomNumeric(3) + ".mp4";
                return storageClient_videoContent.upload(inStream, fileName, path, storageMetadata);
            } catch (Exception ex) {
                logger.warn("Upload video: failed writing into oss", ex.getMessage());
                throw new RuntimeException("上传文件失败");
            }
        }
        return "";
    }


    private String uploadAudioAndGenerateAudioUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return "";
        }
        AlpsHttpResponse execute = HttpRequestExecutor.defaultInstance().get(url).execute();
        if (execute != null) {
            if (execute.getOriginalResponse() == null) {
                for (int retryCount = 0; retryCount < 3; retryCount++) {
                    logger.warn("not response retry {}", retryCount);
                    execute = HttpRequestExecutor.defaultInstance().get(url).execute();
                    if (execute.getOriginalResponse() != null) {
                        break;
                    }
                }
                if (execute.getOriginalResponse() == null) {
                    logger.warn("not response");
                    return "";
                }
            }
            String gfsId = RandomUtils.nextObjectId();
            String filename = "dubbing-" + DateUtils.dateToString(new Date(), "yyyyMMdd") + "-" + gfsId + ".sy3";
            try {
                @Cleanup ByteArrayInputStream content = new ByteArrayInputStream(execute.getOriginalResponse());
                StorageMetadata metadata = new StorageMetadata();
                metadata.setContentType("audio/mp3");
                gfsClient.uploadWithId(content, gfsId, filename, null, metadata);
//                MongoClientBuilder mongoClientBuilder = MongoClientBuilder.getInstance();
//                MongoClient client = mongoClientBuilder.getMongoClient("mongo-gfs");
//                client.openGridFSBucket("GFSDatabase")
//                        .uploadFromStream(new ObjectId(gfsId), filename, "audio/mp3", content);
                return filename;
            } catch (Exception ex) {
                logger.warn("Upload audio: failed writing into mongo gfs", ex.getMessage());
                throw new RuntimeException("上传文件失败");
            }
        }
        return "";

    }


    private String uploadImg(String url) {
        if (StringUtils.isBlank(url)) {
            return "";
        }
        AlpsHttpResponse execute = HttpRequestExecutor.defaultInstance().get(url).execute();
        String gfsId = RandomUtils.nextObjectId();
        String filename = "dubbing-Img-" + DateUtils.dateToString(new Date(), "yyyyMMdd") + "-" + gfsId + ".jpg";

        try {
            @Cleanup ByteArrayInputStream content = new ByteArrayInputStream(execute.getOriginalResponse());
            StorageMetadata metadata = new StorageMetadata();
            metadata.setContentType("image/jpeg");
            gfsClient.uploadWithId(content, gfsId, filename, null, metadata);
//            MongoClientBuilder mongoClientBuilder = MongoClientBuilder.getInstance();
//            MongoClient client = mongoClientBuilder.getMongoClient("mongo-gfs");
//            client.openGridFSBucket("GFSDatabase")
//                    .uploadFromStream(new ObjectId(gfsId), filename, "image/jpeg", content);

            return filename;
        } catch (Exception ex) {
            logger.warn("Upload img: failed writing into mongo gfs", ex.getMessage());
            throw new RuntimeException("上传文件失败");
        }
    }

    private List<DubbingRaw.DubbingSentence> generateDubbingSentences(String srtUrl) throws IOException {
        if (StringUtils.isBlank(srtUrl)) {
            return Collections.emptyList();
        }
        AlpsHttpResponse execute = HttpRequestExecutor.defaultInstance().get(srtUrl).execute();
        if (execute.getOriginalResponse() == null) {
            for (int retryCount = 0; retryCount < 3; retryCount++) {
                logger.warn("not response retry {}", retryCount);
                execute = HttpRequestExecutor.defaultInstance().get(srtUrl).execute();
                if (execute.getOriginalResponse() != null) {
                    break;
                }
            }
            if (execute.getOriginalResponse() == null) {
                logger.warn("not response");
                return Collections.emptyList();
            }
        }
        @Cleanup InputStream inStream = new ByteArrayInputStream(execute.getOriginalResponse());
        BufferedReader inStreamBufferedReader = new BufferedReader(new InputStreamReader(inStream));
        String line;  //一行数据
        List<DubbingRaw.DubbingSentence> srtEntityList = new ArrayList<>();
        List<List<String>> sentenceArrays = new ArrayList<>();
        List<String> sentences = new ArrayList<>();
        while ((line = inStreamBufferedReader.readLine()) != null) {
            if (StringUtils.isBlank(line)) {
                sentenceArrays.add(sentences);
                sentences = new ArrayList<>();
            }
            sentences.add(String.valueOf(line));
        }
        if (CollectionUtils.isNotEmpty(sentenceArrays)) {
            for (List<String> ss : sentenceArrays) {
//            line = line.replace(" ", "");
                DubbingRaw.DubbingSentence srtEntity = new DubbingRaw.DubbingSentence();
                for (String sentence : ss) {
                    if (sentence.contains("\uFEFF")) {
                        sentence = sentence.replace("\uFEFF", "");
                    }
                    try {
                        Integer integer = Integer.valueOf(sentence);
                        srtEntity.setRank(integer);
                    } catch (NumberFormatException ex) {
                        String[] split = sentence.split("-->");
                        if (split.length > 1 && split[1] != null) {
                            String startTime = split[0].trim();
                            srtEntity.setVoiceStart(startTime);
                            String endTime = split[1].trim();
                            srtEntity.setVoiceEnd(endTime);
                        } else {
                            Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
                            Pattern englishPattern = Pattern.compile("[a-zA-Z]");
                            Matcher m = p.matcher(sentence);
                            Matcher em = englishPattern.matcher(sentence);
                            if (m.find() && !em.find()) {
                                String chineseText = sentence;
                                if (StringUtils.isBlank(srtEntity.getChineseText())) {
                                    srtEntity.setChineseText(chineseText);
                                    if (ss.indexOf(sentence) == ss.size() - 1) {
                                        srtEntityList.add(srtEntity);
                                    }
                                } else {
                                    srtEntity.setChineseText(srtEntity.getChineseText() + sentence);
                                    srtEntityList.add(srtEntity);
                                }
                            } else if (!"".equals(sentence) && em.find()) {
                                String englishText = sentence;
                                if (StringUtils.isBlank(srtEntity.getEnglishText())) {
                                    srtEntity.setEnglishText(englishText);
                                } else {
                                    srtEntity.setEnglishText(srtEntity.getEnglishText() + sentence);
                                }
                            }
                        }
                    }
                }

            }
        }
        return srtEntityList;
    }


    private String generateUploadPrefix(String prefix) {
        String env = prefix;
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = prefix + "/test/";
        }
        return env;
    }
}
