package com.voxlearning.utopia.service.workflow.impl;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.mapper.xml.XmlUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowEvent;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowStatus;
import lombok.Cleanup;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2016/11/8
 */
public class WorkFlowConfigParser {
    private static Logger logger = LoggerFactory.getLogger(WorkFlowConfigParser.class);
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // 5分钟更新一次比重启方便一点吧
    private static final long delay = TimeUnit.MINUTES.toMillis(5);
    private static final long period = TimeUnit.MINUTES.toMillis(5);


    // FIXME 这里最好能取到当前的环境
    // FIXME 如若不然，只好先都加载进来吧
    private static Map<String, Map<String, WorkFlowStatus>> workFlowConfig_test = new HashMap<>();
    private static Map<String, Map<String, WorkFlowStatus>> workFlowConfig_prod = new HashMap<>();
    private static String currentVersion;  // 标志当前版本 FIXME 这个版本实际上是不起作用的。。。只能在本地做HotSwap。。手动捂脸

    static {
        lock.writeLock().lock();
        try {
            reload();
        } catch (Exception ignored) {
        } finally {
            lock.writeLock().unlock();
        }

        new Timer("ReloadWorkFlowConfigTimer", true).scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                lock.writeLock().lock();
                try {
                    reload();
                } catch (Exception ignored) {
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }, delay, period);
    }

    private static void reload() throws IOException {
        //从config/wfconfig.properties中解析工作流xml
        @Cleanup InputStream inputStream = WorkFlowConfigParser.class.getResourceAsStream("/config/workflowconfig.properties");
        if (inputStream != null) {
            Properties properties = new Properties();
            properties.load(inputStream);
            String version = (String) properties.remove("config_version");
            logger.debug("WorkFlowConfig: currentVersion:" + currentVersion + " fileVersion:" + version);
            // 检查版本，确认是否需要重新加载
            if (currentVersion != null && currentVersion.equals(version)) {
                return;
            }
            logger.info("WorkFlowConfig Reload, version {} -> {} ", currentVersion, version);
            currentVersion = version;
            Enumeration enums = properties.propertyNames();
            while (enums.hasMoreElements()) {

                String workFlowName = (String) enums.nextElement();
                String workFlowConfigXml = properties.getProperty(workFlowName);
                if (StringUtils.isAnyBlank(workFlowName, workFlowConfigXml)) {
                    logger.error("WorkFlowXml /config/workflowconfig.properties config wrong");
                    continue;
                }
                try {
                    initialTestConfig(workFlowName, workFlowConfigXml);
                    initialProdConfig(workFlowName, workFlowConfigXml);
                } catch (SAXException ex) {
                    logger.error("WorkFlowConfig: Failed load " + workFlowConfigXml + " of " + workFlowName);
                }
            }
        } else {
            logger.error("WorkFlow /config/workflowconfig.properties not exists");
        }
    }

    private static void initialTestConfig(String workFlowName, String configXml) throws IOException, SAXException {
        workFlowConfig_test.put(workFlowName, new LinkedHashMap<>());

        @Cleanup InputStream inputStream = WorkFlowConfigParser.class.getResourceAsStream("/config/test/" + configXml);
        if (inputStream == null) {
            logger.error("WorkFlow-Test {} not exists", configXml);
            return;
        }
        Document document = XmlUtils.parseDocument(inputStream);
        Element rootElement = document.getDocumentElement();
        List<Element> statusElementList = XmlUtils.getChildElements(rootElement, "status");

        for (Element statusElement : statusElementList) {
            //实例化status
            String beforeStatus = statusElement.getAttribute("name").trim();
            String processor = statusElement.getAttribute("processor").trim();
            Boolean afterTreatment = SafeConverter.toBoolean(statusElement.getAttribute("aftertreatment").trim());
            if (StringUtils.isBlank(beforeStatus)) {
                logger.error("WorkFlowXml {} config wrong", configXml);
                continue;
            }
            Map<String, WorkFlowStatus> statusMap = workFlowConfig_test.get(workFlowName);
            WorkFlowStatus workFlowStatus = new WorkFlowStatus();
            workFlowStatus.setName(beforeStatus);
            workFlowStatus.setAftertreatment(afterTreatment);
            workFlowStatus.setEventMap(new HashMap<>());
            if (StringUtils.isNotBlank(processor)) {
                workFlowStatus.setProcessor(processor.split(","));
                List<WorkFlowProcessUser> processUserList = Arrays.stream(processor.split(","))
                        .filter(p -> StringUtils.isNotBlank(StringUtils.trim(p)))
                        .map(p -> {
                            String jsonStr = p.replaceAll("@", ",");
                            return JsonUtils.fromJson(jsonStr, WorkFlowProcessUser.class);
                        }).filter(Objects::nonNull).collect(Collectors.toList());
                workFlowStatus.setProcessUserList(processUserList);
            }

            statusMap.put(beforeStatus, workFlowStatus);

            List<Element> eventElementList = XmlUtils.getChildElements(statusElement, "event");
            for (Element eventElement : eventElementList) {
                String event = eventElement.getAttribute("name").trim();
                String afterStatus = eventElement.getAttribute("status").trim();
                String mqmsg = eventElement.getAttribute("mqmsg").trim();
                //校验字段是否完全
                if (StringUtils.isAnyBlank(event, afterStatus)) {
                    logger.error("WorkFlowXml {} config wrong", configXml);
                    continue;
                }
                Map<String, WorkFlowEvent> eventMap = workFlowStatus.getEventMap();
                if (eventMap == null) {
                    eventMap = new HashMap<>();
                    workFlowStatus.setEventMap(eventMap);
                }
                eventMap.put(event, new WorkFlowEvent(event, afterStatus, mqmsg));
            }
        }
    }

    private static void initialProdConfig(String workFlowName, String configXml) throws IOException, SAXException {
        workFlowConfig_prod.put(workFlowName, new LinkedHashMap<>());

        @Cleanup InputStream tempInputStream = WorkFlowConfigParser.class.getResourceAsStream("/config/prod/" + configXml);
        if (tempInputStream == null) {
            logger.error("WorkFlow-Prod {} not exists", configXml);
            return;
        }
        Document document = XmlUtils.parseDocument(tempInputStream);
        Element rootElement = document.getDocumentElement();
        List<Element> statusElementList = XmlUtils.getChildElements(rootElement, "status");
        for (Element statusElement : statusElementList) {
            //实例化status
            String beforeStatus = statusElement.getAttribute("name").trim();
            String processor = statusElement.getAttribute("processor").trim();
            Boolean afterTreatment = SafeConverter.toBoolean(statusElement.getAttribute("aftertreatment").trim());
            if (StringUtils.isBlank(beforeStatus)) {
                logger.error("WorkFlowXml-Test {} config wrong", configXml);
                continue;
            }
            Map<String, WorkFlowStatus> statusMap = workFlowConfig_prod.get(workFlowName);
            WorkFlowStatus workFlowStatus = new WorkFlowStatus();
            workFlowStatus.setName(beforeStatus);
            workFlowStatus.setAftertreatment(afterTreatment);
            workFlowStatus.setEventMap(new HashMap<>());
            if (StringUtils.isNotBlank(processor)) {
                workFlowStatus.setProcessor(processor.split(","));
                List<WorkFlowProcessUser> processUserList = Arrays.stream(processor.split(","))
                        .filter(p -> StringUtils.isNotBlank(StringUtils.trim(p)))
                        .map(p -> {
                            String jsonStr = p.replaceAll("@", ",");
                            return JsonUtils.fromJson(jsonStr, WorkFlowProcessUser.class);
                        }).filter(Objects::nonNull).collect(Collectors.toList());
                workFlowStatus.setProcessUserList(processUserList);
            }

            statusMap.put(beforeStatus, workFlowStatus);

            List<Element> eventElementList = XmlUtils.getChildElements(statusElement, "event");
            for (Element eventElement : eventElementList) {
                String event = eventElement.getAttribute("name").trim();
                String afterStatus = eventElement.getAttribute("status").trim();
                String mqmsg = eventElement.getAttribute("mqmsg").trim();
                //校验字段是否完全
                if (StringUtils.isAnyBlank(event, afterStatus)) {
                    logger.error("WorkFlowXml-Prod {} config wrong", configXml);
                    continue;
                }
                Map<String, WorkFlowEvent> eventMap = workFlowStatus.getEventMap();
                if (eventMap == null) {
                    eventMap = new HashMap<>();
                    workFlowStatus.setEventMap(eventMap);
                }
                eventMap.put(event, new WorkFlowEvent(event, afterStatus, mqmsg));
            }
        }
    }

    public static Map<String, Map<String, WorkFlowStatus>> getWorkFlowConfig(Mode runtime) {
        Objects.requireNonNull(runtime);
        lock.readLock().lock();
        try {
            if (runtime.ge(Mode.STAGING)) return workFlowConfig_prod;
            return workFlowConfig_test;
        } finally {
            lock.readLock().unlock();
        }
    }

    public static String currentVersion() {
        return currentVersion;
    }

    public static void main(String[] args) {
        System.out.println(JsonUtils.toJsonPretty(workFlowConfig_test.get("agent_data_report_apply")));
//        System.out.println(JsonUtils.toJsonPretty(workFlowConfig_prod));
    }

}
