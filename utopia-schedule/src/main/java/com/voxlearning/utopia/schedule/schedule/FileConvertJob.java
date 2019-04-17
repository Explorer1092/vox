package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.*;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.schedule.util.ZipUtils;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;
import com.voxlearning.utopia.service.campaign.client.TeacherCoursewareContestServiceClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import de.innosystec.unrar.Archive;
import de.innosystec.unrar.NativeStorage;
import de.innosystec.unrar.rarfile.FileHeader;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author peng.zhang
 * @since 2018/10/09
 **/
@Named
@ScheduledJobDefinition(
        jobName = "课件大赛文件转换任务",
        jobDescription = "课件大赛文件修改文件存储路径",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.DEVELOPMENT, Mode.TEST, Mode.STAGING},
        ENABLED = false,
        cronExpression = "0 0/30 * * * ? ")
@ProgressTotalWork(100)
public class FileConvertJob extends ScheduledJobWithJournalSupport {

    @StorageClientLocation(storage = "plat-doc-content")
    private StorageClient fileStorageClient;

    @Inject
    private TeacherCoursewareContestServiceClient teacherCoursewareContestServiceClient;
    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    private static final String AtomicCacheKey = "TeacherCourseware_FileConvertJob";

    private StringBuilder emailContent = new StringBuilder(); // 有全局锁, 先不考虑安全性,手动调用的先不管

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        String cids = SafeConverter.toString(parameters.get("cids"));
        if (StringUtils.isNoneBlank(cids)) {
            String[] idList = cids.split(",");
            for (String cid : idList) {
                TeacherCourseware courseware = teacherCoursewareContestServiceClient.fetchCoursewareDetailById(cid);
                processCourseware(courseware);
            }
        } else {
            AtomicLockManager instance = AtomicLockManager.instance();
            try {
                instance.acquireLock(AtomicCacheKey, 2 * 60 * 60); // 2 小时自动解
                executeJob();
            } catch (CannotAcquireLockException e) {
                logger.error("FileConvertJob DUPLICATED OPERATION");
            } finally {
                instance.releaseLock(AtomicCacheKey);
            }
        }

    }

    private MapMessage executeJob() {
        cleanEmailContent();

        // 按照审核状态处理
        List<TeacherCourseware> dataList = teacherCoursewareContestServiceClient.getTeacherCoursewareContestService().fetchCourseWareListByExamStatus(TeacherCourseware.ExamineStatus.PASSED.name());
        Collections.sort(dataList, (o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime()));

        for (TeacherCourseware courseware : dataList) {
            processCourseware(courseware);
        }

        sendEmail();

        return MapMessage.successMessage();
    }

    private void processCourseware(TeacherCourseware courseware) {
        fillinTeacherName(courseware);
        fillinSerieName(courseware);
        reUpload(courseware);
        unZip(courseware);
        unRar(courseware);
        zip(courseware);
    }

    private void fillinTeacherName(TeacherCourseware courseware) {
        if (StringUtils.isNoneBlank(courseware.getTeacherName())) {
            return;
        }

        User teacher = userLoaderClient.loadUser(courseware.getTeacherId());
        if (teacher != null && !teacher.isDisabledTrue()) {
            courseware.setTeacherName(teacher.fetchRealname());
            teacherCoursewareContestServiceClient.upsertCourseware(courseware);
        }
    }

    private void fillinSerieName(TeacherCourseware courseware) {
        if (StringUtils.isNoneBlank(courseware.getSerieName())) {
            return;
        }

        if (StringUtils.isBlank(courseware.getSerieId()) || courseware.getSubject() == null) {
            return;
        }

        List<NewBookCatalog> newBookCatalogList = newContentLoaderClient.loadShowSeriesBySubject(courseware.getSubject());
        NewBookCatalog catalog = newBookCatalogList.stream()
                .filter(p -> Objects.equals(p.getId(), courseware.getSerieId()))
                .findFirst()
                .orElse(null);

        if (catalog != null) {
            courseware.setSerieName(catalog.getName());
            teacherCoursewareContestServiceClient.upsertCourseware(courseware);
        }
    }

    private void unRar(TeacherCourseware courseware) {
        courseware = teacherCoursewareContestServiceClient.fetchCoursewareDetailById(courseware.getId());
        String coursewareId = courseware.getId();
        String rarFileUrl = courseware.getCoursewareFile();
        if (StringUtils.isEmpty(rarFileUrl)) {
            return;
        }
        if (!(rarFileUrl.toLowerCase().endsWith(".rar"))) {
            return;
        }
        // 解压过的不再重复解压
        if (StringUtils.isNotEmpty(courseware.getPptCoursewareFile())) {
            return;
        }

        String rarFilePath = "";
        Archive archive = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        ByteArrayInputStream pptInputStream = null;
        try {
            byte[] rarByte = HttpRequestExecutor.defaultInstance().get(rarFileUrl)
                    .socketTimeout(30000)
                    .connectionTimeout(5000).execute().getOriginalResponse();

            String uniq = System.currentTimeMillis() + RandomUtils.randomNumeric(5);
            String rarFileName = uniq + ".rar";
            rarFilePath = "/tmp/teachercourseware/" + rarFileName;

            File writeFile = new File(rarFilePath);
            FileUtils.writeByteArrayToFile(writeFile, rarByte);

            archive = new Archive(new NativeStorage(writeFile));
            List<FileHeader> list = archive.getFileHeaders();

            String pptName = null;
            String pptExtName = "";

            for (FileHeader header : list) {
                if (header.isDirectory()) continue;
                String originFileName = "";
                if (header.isUnicode()) {
                    originFileName = header.getFileNameW().trim();
                } else {
                    originFileName = header.getFileNameString().trim();
                }
                originFileName = originFileName.substring(originFileName.lastIndexOf("\\") + 1);
                if (originFileName.startsWith(".") || originFileName.startsWith("~")) continue;

                if (originFileName.endsWith(".ppt") || originFileName.endsWith(".pptx")) {
                    pptName = originFileName;
                    pptExtName = pptName.substring(pptName.lastIndexOf("."));
                    byteArrayOutputStream = new ByteArrayOutputStream();
                    archive.extractFile(header, byteArrayOutputStream);
                    break;
                }
            }

            if (pptName == null) {
                logger.warn("TeacherCourseware unrar not fount ppt, courseware id:{}", coursewareId);
                writeLog("无法解压 rar 文件或识别不到 ppt 文件, courseware id:" + coursewareId + ", teacher id:" + courseware.getTeacherId());
                return;
            }

            pptInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            String cdnUrl = fileStorageClient.upload(pptInputStream, uniq + pptExtName, "courseware");
            if (StringUtils.isEmpty(cdnUrl)) {
                logger.error("TeacherCourseware unrar error, courseware id:{}", coursewareId);
                return;
            }
            cdnUrl = "https://v.17xueba.com/" + cdnUrl;
            teacherCoursewareContestServiceClient.updatePptCoursewareFile(coursewareId, cdnUrl, pptName);
            logger.info("TeacherCourseware file unrar finished. old url:{}, new url:{}", rarFileUrl, cdnUrl);
        } catch (Exception e) {
            logger.error("TeacherCourseware file unrar error. courseware id:" + coursewareId);
            writeLog("解压 rar 文件异常,id:" + coursewareId + ", teacher id:" + courseware.getTeacherId());
        } finally {
            try {
                if (archive != null) archive.close();
                IOUtils.closeQuietly(byteArrayOutputStream);
                IOUtils.closeQuietly(pptInputStream);
                if (StringUtils.isNotEmpty(rarFilePath)) {
                    File file = new File(rarFilePath);
                    FileUtils.forceDelete(file);
                }
            } catch (Exception ignore) {
            }
        }
    }

    private void zip(TeacherCourseware courseware) {
        courseware = teacherCoursewareContestServiceClient.fetchCoursewareDetailById(courseware.getId());
        // 是否需要重新打包, 有重传ppt的情况
        boolean needPackage = SafeConverter.toBoolean(courseware.getNeedPackage(), true);
        if (!needPackage) {
            return;
        }

        String coursewareId = courseware.getId();
        String basePath = "/tmp/teachercourseware/";
        String uniq = System.currentTimeMillis() + RandomUtils.randomNumeric(5);
        String baseDir = basePath + uniq + "/";

        String resultFileFullName = baseDir + "result.zip";
        String zipDirPath = baseDir + "zip/";
        String pptDir = zipDirPath + "ppt/";
        String imgDir = zipDirPath + "img/";
        String wordDir = zipDirPath + "doc/";
        String zipDir = zipDirPath + "zip/";

        try {
            // 下载解压后的 PPT
            String pptCoursewareFile = courseware.getPptCoursewareFile();
            String pptCoursewareFileName = courseware.getPptCoursewareFileName();
            if (StringUtils.isNotEmpty(pptCoursewareFile)) {
                File file = new File(pptDir + pptCoursewareFileName);
                downloadFile(pptCoursewareFile, file);
            } else {
                // 下载用户上传的 PPT 或者 ZIP
                String coursewareFile = courseware.getCoursewareFile();
                String coursewareFileName = courseware.getCoursewareFileName();
                if (StringUtils.isNotEmpty(coursewareFile)) {
                    File file = null;
                    if (coursewareFile.endsWith(".zip") || coursewareFile.endsWith(".ZIP")
                            || coursewareFile.endsWith(".rar") || coursewareFile.endsWith(".RAR")) {
                        file = new File(zipDir + coursewareFileName);
                    } else {
                        file = new File(pptDir + coursewareFileName);
                    }
                    downloadFile(coursewareFile, file);
                }
            }

            // 下载 word
            String wordUrl = courseware.getWordUrl();
            String wordName = courseware.getWordName();
            if (StringUtils.isNotEmpty(wordUrl)) {
                File file = new File(wordDir + wordName);
                downloadFile(wordUrl, file);
            }

            // 下载 图片
            List<Map<String, String>> picturePreview = courseware.getPicturePreview();
            if (CollectionUtils.isNotEmpty(picturePreview)) {
                for (Map<String, String> map : picturePreview) {
                    String imgUrl = MapUtils.getString(map, "url");
                    String imgName = MapUtils.getString(map, "name");
                    File file = new File(imgDir + imgName);
                    downloadFile(imgUrl, file);
                }
            }

            ZipUtils.zipFile(zipDirPath, resultFileFullName);

            String cdnUrl = fileStorageClient.upload(FileUtils.openInputStream(new File(resultFileFullName)), uniq + ".zip", "courseware");
            if (StringUtils.isEmpty(cdnUrl)) {
                logger.error("TeacherCourseware zip error, courseware id:{}", coursewareId);
                return;
            }
            cdnUrl = "https://v.17xueba.com/" + cdnUrl;

            teacherCoursewareContestServiceClient.updateZipFile(coursewareId, cdnUrl);
            logger.info("TeacherCourseware zip finished. new url:{}", cdnUrl);
        } catch (Exception e) {
            logger.error("TeacherCourseware zip error. courseware id:" + coursewareId + ",teacher id:" + courseware.getTeacherId());
            writeLog("zip 打包异常,id:" + coursewareId + ", teacher id:" + courseware.getTeacherId());
        } finally {
            File file = new File(baseDir);
            if (file.exists() && file.isDirectory()) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void unZip(TeacherCourseware courseware) {
        courseware = teacherCoursewareContestServiceClient.fetchCoursewareDetailById(courseware.getId());
        String coursewareId = courseware.getId();
        String zipFileUrl = courseware.getCoursewareFile();
        if (StringUtils.isEmpty(zipFileUrl)) {
            return;
        }
        if (!(zipFileUrl.toLowerCase().endsWith(".zip"))) {
            return;
        }
        // 解压过的不再重复解压
        if (StringUtils.isNotEmpty(courseware.getPptCoursewareFile())) {
            return;
        }

        String zipFilePath = "";
        try {
            byte[] zipByte = HttpRequestExecutor.defaultInstance().get(zipFileUrl)
                    .socketTimeout(30000)
                    .connectionTimeout(5000).execute().getOriginalResponse();

            String uniq = System.currentTimeMillis() + RandomUtils.randomNumeric(5);
            String zipFileName = uniq + ".zip";
            zipFilePath = "/tmp/teachercourseware/" + zipFileName;

            File writeFile = new File(zipFilePath);
            FileUtils.writeByteArrayToFile(writeFile, zipByte);

            ZipFile zipfile = new ZipFile(zipFilePath, Charset.forName("GBK")); // 包含中文名的压缩包需要用 GBK

            String pptName = null;
            InputStream pptInputStream = null;
            String pptExtName = "";

            Enumeration<? extends ZipEntry> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.isDirectory()) continue;
                String itemName = zipEntry.getName();
                if (itemName.endsWith(".ppt") || itemName.endsWith(".pptx")) {
                    pptName = itemName.substring(itemName.lastIndexOf("/") + 1); // 去除一级的文件夹名
                    if (pptName.startsWith(".") || pptName.startsWith("~")) continue;
                    pptExtName = pptName.substring(pptName.lastIndexOf("."));
                    pptInputStream = zipfile.getInputStream(zipEntry);
                    break;
                }
            }
            if (pptName == null) {
                logger.warn("TeacherCourseware unzip not fount ppt, courseware id:{}", coursewareId);
                writeLog("无法解压 zip 文件或识别不到 ppt 文件, courseware id:" + coursewareId + ",teacher id:" + courseware.getTeacherId());
                return;
            }

            String cdnUrl = fileStorageClient.upload(pptInputStream, uniq + pptExtName, "courseware");
            if (StringUtils.isEmpty(cdnUrl)) {
                logger.error("TeacherCourseware unzip error, courseware id:{}", coursewareId);
                return;
            }
            zipfile.close();
            cdnUrl = "https://v.17xueba.com/" + cdnUrl;
            teacherCoursewareContestServiceClient.updatePptCoursewareFile(coursewareId, cdnUrl, pptName);
            logger.info("TeacherCourseware file unzip finished. old url:{}, new url:{}", zipFileUrl, cdnUrl);
        } catch (Exception e) {
            logger.error("TeacherCourseware file unzip error. courseware id:" + coursewareId);
            writeLog("解压 zip 文件异常,id:" + coursewareId + ",teacher id:" + courseware.getTeacherId());
        } finally {
            if (StringUtils.isNotEmpty(zipFilePath)) {
                try {
                    File file = new File(zipFilePath);
                    FileUtils.forceDelete(file);
                } catch (IOException ignore) {
                }
            }
        }
    }

    private void reUpload(TeacherCourseware courseware) {
        courseware = teacherCoursewareContestServiceClient.fetchCoursewareDetailById(courseware.getId());
        String wordUrl = courseware.getWordUrl();
        if (StringUtils.isBlank(wordUrl)) {
            return;
        }
        if (wordUrl.contains("v.17xueba.com")) {
            return;
        }
        String coursewareId = courseware.getId();
        try {
            URL url = new URL(wordUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(30000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            int code = urlConnection.getResponseCode();
            String newFilePath = "";
            if (code != 200) {
                logger.error("TeacherCourseware convert error, download, courseware id:{}", coursewareId);
                return;
            }

            if (wordUrl.endsWith("docx") || wordUrl.endsWith("doc")) {
                InputStream inputStream = urlConnection.getInputStream();
                ByteArrayInputStream newStream = new ByteArrayInputStream(IOUtils.toByteArray(inputStream));

                String fileType = wordUrl.substring(wordUrl.lastIndexOf("."));
                String fileName = System.currentTimeMillis() + RandomUtils.randomNumeric(5) + fileType;
                newFilePath = fileStorageClient.upload(newStream, fileName, "courseware");
                if (StringUtils.isBlank(newFilePath)) {
                    logger.error("TeacherCourseware convert error, upload, courseware id:{}", coursewareId);
                    return;
                }

                String fullPath = "https://v.17xueba.com/" + newFilePath;
                teacherCoursewareContestServiceClient.updateNewWordFile(coursewareId, fullPath);
                logger.info("TeacherCourseware file convert finished. old url:{}, new url:{}", wordUrl, fullPath);
            }
        } catch (Exception e) {
            logger.error("file convert error, id:{}, tid:{}, wordUrl:{}", courseware.getId(), courseware.getTeacherId(), courseware.getWordUrl(), e);
            writeLog("课件大赛 convert 异常,id:" + coursewareId + ",teacher id:" + courseware.getTeacherId());
        }
    }

    private static void downloadFile(String url, File file) throws IOException {
        byte[] fileByte = HttpRequestExecutor.defaultInstance().get(url)
                .socketTimeout(30000)
                .connectionTimeout(5000).execute().getOriginalResponse();
        FileUtils.writeByteArrayToFile(file, fileByte);
    }

    private void writeLog(String string){
        if (StringUtils.isNotEmpty(string)) {
            emailContent.append(string + "\n");
        }
    }

    private void cleanEmailContent() {
        emailContent.setLength(0);
    }

    private void sendEmail() {
        if(!emailContent.toString().isEmpty()) {
            if (RuntimeMode.isProduction()) {
                emailServiceClient.createPlainEmail()
                        .to("zhilong.hu@17zuoye.com")
                        .cc("junbao.zhang@17zuoye.com;xu.yan@17zuoye.com;sha.zeng@17zuoye.com;kaiyue.jing@17zuoye.com")
                        .subject("课件大赛异常")
                        .body(emailContent.toString())
                        .send();
            } else {
                emailServiceClient.createPlainEmail()
                        .to("junbao.zhang@17zuoye.com")
                        .subject(RuntimeMode.getCurrentStage() + "-课件大赛异常")
                        .body(emailContent.toString())
                        .send();
            }
        }
    }

}
