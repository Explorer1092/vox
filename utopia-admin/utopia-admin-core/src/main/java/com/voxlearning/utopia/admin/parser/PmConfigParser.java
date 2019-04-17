package com.voxlearning.utopia.admin.parser;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.xml.XmlUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import lombok.Cleanup;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by yaguang.wang
 * on 2017/9/4.
 */
public class PmConfigParser {
    private static Logger logger = LoggerFactory.getLogger(PmConfigParser.class);
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private static List<WorkFlowProcessUser> PM_ACCOUNT_LIST = new ArrayList<>();

    private static String PM_CONFIG = "/config/pm_config.xml";
    private static String PM_CONFIG_TEST = "/config/pm_config_test.xml";
    private static String currentVersion;

    static {
        atomicReload();
    }

    private static void atomicReload() {
        lock.writeLock().lock();
        try {
            reload();
        } catch (Exception ignored) {
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static void reload() throws IOException, SAXException {
        String resourcePath;
        if (RuntimeMode.ge(Mode.STAGING)) {
            resourcePath = PM_CONFIG;
        } else {
            resourcePath = PM_CONFIG_TEST;
        }
        @Cleanup InputStream inputStream = PmConfigParser.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            logger.error("PM config is not exists");
            return;
        }
        Document document = XmlUtils.parseDocument(inputStream);
        Element rootElement = document.getDocumentElement();
        List<Element> statusElementList = XmlUtils.getChildElements(rootElement, "user");
        //Element version = XmlUtils.getSingleChildElement(rootElement, "version");
        /*if (version == null || StringUtils.isBlank(version.getAttribute("value"))) {
            logger.error("version is not exists");
            return;
        }*/
       /* if (currentVersion != null && currentVersion.equals(version.getAttribute("value"))) {
            return;
        }*/
        //logger.info("Pm config reload, version {} -> {} ", currentVersion, version.getAttribute("value"));
        //currentVersion = version.getAttribute("value");
        PM_ACCOUNT_LIST = new ArrayList<>();
        statusElementList.forEach(p -> {
            if (cStr(p.getAttribute("userplatform")) || cStr(p.getAttribute("account")) || cStr(p.getAttribute("accountname"))) {
                return;
            }
            WorkFlowProcessUser user = new WorkFlowProcessUser();
            user.setUserPlatform(p.getAttribute("userplatform"));
            user.setAccount(p.getAttribute("account"));
            user.setAccountName(p.getAttribute("accountname"));
            PM_ACCOUNT_LIST.add(user);
        });
        Collections.sort(PM_ACCOUNT_LIST, (o1, o2) -> {
            String xing1 = o1.getAccount().split("\\.")[1];
            String ming1 = o1.getAccount().split("\\.")[0];

            String xing2 = o2.getAccount().split("\\.")[1];
            String ming2 = o2.getAccount().split("\\.")[0];

            if(xing1.compareTo(xing2) != 0){
                return xing1.compareTo(xing2);
            }else {
                return ming1.compareTo(ming2);
            }
        });
    }

    private static boolean cStr(String str) {
        return StringUtils.isBlank(str);
    }

    public static List<WorkFlowProcessUser> getPmConfig() {
        return PM_ACCOUNT_LIST;
    }

    public static void main(String[] args) {
        for (WorkFlowProcessUser user : getPmConfig())
            System.out.println(user.getAccountName());
    }
}
