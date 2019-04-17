package com.voxlearning.utopia.schedule.schedule.studytogether;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.schedule.util.ScheduleOssManageUtils;
import com.voxlearning.utopia.service.parent.api.StudyTogetherActiveCardService;
import com.voxlearning.utopia.service.parent.api.constants.CardTerm;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Named;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * @author jiangpeng
 * @since 2018-08-31 下午9:38
 **/
@Named
@ScheduledJobDefinition(
        jobName = "批量生成一起学课程激活卡密",
        jobDescription = "批量生成一起学课程激活卡密",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 0 12 ? * TUE-SAT",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoStudyTogetherActiveCardCodeJob extends ScheduledJobWithJournalSupport {


    @ImportService(interfaceClass = StudyTogetherActiveCardService.class)
    private StudyTogetherActiveCardService studyTogetherActiveCardService;



    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        String url = SafeConverter.toString(parameters.get("url"));
        String termStr = SafeConverter.toString(parameters.get("term"));
        CardTerm cardTerm = null;
        try {
            cardTerm = CardTerm.valueOf(termStr);
        }catch (Exception e) {
            return;
        }

        if (StringUtils.isNotBlank(url)){
            String[] codes = getCodesFromUrl(url);
            if (codes.length == 0){
                return;
            }
            ISimpleProgressMonitor monitor = progressMonitor.subTask(100, codes.length);
            for (String code : codes) {
                try {
                    String code1 = studyTogetherActiveCardService.createCode(StringUtils.trim(code), cardTerm);
                    if (StringUtils.isBlank(code1)){
                        logger.error("创建激活码失败！code = {}", code);
                    }
                }catch (Exception e){
                    logger.error("创建激活码异常！code = {}", code);
                }finally {
                    monitor.worked(1);
                }
            }
            monitor.done();
            return;
        }
        String codes = SafeConverter.toString(parameters.get("codes"));
        if (StringUtils.isNotBlank(codes)){
            String[] codeArray = codes.split(",");
            for (String code : codeArray) {
                studyTogetherActiveCardService.createCode(StringUtils.trim(code), cardTerm);
            }
            return;
        }

        int generateCount = SafeConverter.toInt(parameters.get("generateCount"));
        if (generateCount <= 0){
            return;
        }
        String existedCodesUrl = SafeConverter.toString(parameters.get("existedCodesUrl"));
        Set<String> existedCodeSet;
        if (StringUtils.isNotBlank(existedCodesUrl)){
            String[] codes1 = getCodesFromUrl(existedCodesUrl);
            existedCodeSet = new HashSet<>(Arrays.asList(codes1));
        }else {
            existedCodeSet = new HashSet<>();
        }
        String codesStr = generateNewCodes(existedCodeSet, generateCount);
        MultipartFile csvFile = generateCsvFile(codesStr);
        String fileUrl = ScheduleOssManageUtils.upload(csvFile, "17xue");
        logger.info("生成文件 url = " + fileUrl);
    }


    private String generateNewCodes(Set<String> existedCodeSet, Integer codeCount){
        int count = 0 ;
        StringBuffer stringBuffer = new StringBuffer();
        while (count < codeCount) {
            String code = generateRandomCode();
            int num = SafeConverter.toInt(code, -1);
            if (num != -1){
                continue;
            }
            boolean add = existedCodeSet.add(code);
            if (add) {
                count ++;
                stringBuffer.append(code).append("\n");
            }
        }
        return stringBuffer.toString();
    }

    private String[] getCodesFromUrl(String url) throws IOException {
        URL u = new URL(url);
        URLConnection uc = u.openConnection();
        int contentLength = uc.getContentLength();
        String content = "";
        try (InputStream raw = uc.getInputStream()) {
            InputStream in = new BufferedInputStream(raw);
            byte[] data = new byte[contentLength];
            int offset = 0;
            while (offset < contentLength) {
                int bytesRead = in.read(data, offset, data.length - offset);
                if (bytesRead == -1) {
                    break;
                }
                offset += bytesRead;
            }

            if (offset != contentLength) {
                throw new IOException("Only read " + offset
                        + " bytes; Expected " + contentLength + " bytes");
            }
            content = new String(data);
        }
        return content.split("\n");
    }

    private static String[] letterArray = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M",
            "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    private static String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        for (int i =0 ; i<6 ;i++) {
            int letterIndex = RandomUtils.nextInt(0, 33);
            String letter = letterArray[letterIndex];
            code.append(letter);
        }
        return code.toString();
    }

    private static MultipartFile generateCsvFile(String content) {// 对字节数组字符串进行Base64解码并生成图片
        if (StringUtils.isBlank(content)) // 图像数据为空
            return null;
        final byte[] bytes = content.getBytes();
        try {
            return new MultipartFile() {
                @Override
                public String getName() {
                    return "";
                }

                @Override
                public String getOriginalFilename() {
                    return "1.csv";
                }

                @Override
                public String getContentType() {
                    return "image/jpeg";
                }

                @Override
                public boolean isEmpty() {
                    return false;
                }

                @Override
                public long getSize() {
                    return bytes.length;
                }

                @Override
                public byte[] getBytes() throws IOException {
                    return bytes;
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return new ByteArrayInputStream(bytes);
                }

                @Override
                public void transferTo(File dest) throws IOException, IllegalStateException {
                    new FileOutputStream(dest).write(bytes);
                }
            };
        } catch (Exception e)  {
            return null;
        }

    }
}
