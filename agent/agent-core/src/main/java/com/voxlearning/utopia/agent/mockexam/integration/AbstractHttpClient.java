package com.voxlearning.utopia.agent.mockexam.integration;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.http.client.execute.GET;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.core.HttpClientType;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 基于http实现的客户端
 *
 * @author xiaolei.li
 * @version 2018/8/6
 */
public abstract class AbstractHttpClient {

    public static final String PAPER_DEV_HOST = "http://dev.tiku.17zuoye.net";
//    public static final String PAPER_DEV_HOST = "http://10.200.8.132:5000";
    public static final String PAPER_PRODUCT_HOST = "http://tiku.17zuoye.net";

    public static final String REPORT_PRODUCT_HOST = "http://yqc.17zuoye.net";
    public static final String REPORT_DEV_HOST = "http://10.7.4.240:8116";

    public static final String MIDDLE_SCHOOL_PAPER_DEV_HOST = "http://zytiku.test.17zuoye.net";
    public static final String MIDDLE_SCHOOL_PAPER_PRODUCT_HOST = "http://zytiku.17zuoye.com";

    /**
     * 服务注册表
     */
    public static final Table<Mode, Service, String> SERVICE_REGISTRY = HashBasedTable.create();


    static {
        // 创建试卷
        SERVICE_REGISTRY.put(Mode.DEVELOPMENT, Service.PAPER_CREATE, PAPER_DEV_HOST + "/service/evaluationTask");
        SERVICE_REGISTRY.put(Mode.TEST, Service.PAPER_CREATE, PAPER_DEV_HOST + "/service/evaluationTask");
        SERVICE_REGISTRY.put(Mode.STAGING, Service.PAPER_CREATE, PAPER_PRODUCT_HOST + "/service/evaluationTask");
        SERVICE_REGISTRY.put(Mode.PRODUCTION, Service.PAPER_CREATE, PAPER_PRODUCT_HOST + "/service/evaluationTask");
        // 检查试卷
        SERVICE_REGISTRY.put(Mode.DEVELOPMENT, Service.PAPER_CHECK, PAPER_DEV_HOST + "/service/paper_check");
        SERVICE_REGISTRY.put(Mode.TEST, Service.PAPER_CHECK, PAPER_DEV_HOST + "/service/paper_check");
        SERVICE_REGISTRY.put(Mode.STAGING, Service.PAPER_CHECK, PAPER_PRODUCT_HOST + "/service/paper_check");
        SERVICE_REGISTRY.put(Mode.PRODUCTION, Service.PAPER_CHECK, PAPER_PRODUCT_HOST + "/service/paper_check");
        // 查询试卷
        SERVICE_REGISTRY.put(Mode.DEVELOPMENT, Service.PAPER_QUERY, PAPER_DEV_HOST + "/service/paper_list");
        SERVICE_REGISTRY.put(Mode.TEST, Service.PAPER_QUERY, PAPER_DEV_HOST + "/service/paper_list");
        SERVICE_REGISTRY.put(Mode.STAGING, Service.PAPER_QUERY, PAPER_PRODUCT_HOST + "/service/paper_list");
        SERVICE_REGISTRY.put(Mode.PRODUCTION, Service.PAPER_QUERY, PAPER_PRODUCT_HOST + "/service/paper_list");
        // 申请考试
        SERVICE_REGISTRY.put(Mode.DEVELOPMENT, Service.EXAM_APPLY, PAPER_DEV_HOST + "/service/applyExam");
        SERVICE_REGISTRY.put(Mode.TEST, Service.EXAM_APPLY, PAPER_DEV_HOST + "/service/applyExam");
        SERVICE_REGISTRY.put(Mode.STAGING, Service.EXAM_APPLY, PAPER_PRODUCT_HOST + "/service/applyExam");
        SERVICE_REGISTRY.put(Mode.PRODUCTION, Service.EXAM_APPLY, PAPER_PRODUCT_HOST + "/service/applyExam");
        // 上线
        SERVICE_REGISTRY.put(Mode.DEVELOPMENT, Service.EXAM_ONLINE, PAPER_DEV_HOST + "/service/examOnOffLine");
        SERVICE_REGISTRY.put(Mode.TEST, Service.EXAM_ONLINE, PAPER_DEV_HOST + "/service/examOnOffLine");
        SERVICE_REGISTRY.put(Mode.STAGING, Service.EXAM_ONLINE, PAPER_PRODUCT_HOST + "/service/examOnOffLine");
        SERVICE_REGISTRY.put(Mode.PRODUCTION, Service.EXAM_ONLINE, PAPER_PRODUCT_HOST + "/service/examOnOffLine");
        // 下线
        SERVICE_REGISTRY.put(Mode.DEVELOPMENT, Service.EXAM_OFFLINE,  PAPER_DEV_HOST + "/service/examOnOffLine");
        SERVICE_REGISTRY.put(Mode.TEST, Service.EXAM_OFFLINE, PAPER_DEV_HOST + "/service/examOnOffLine");
        SERVICE_REGISTRY.put(Mode.STAGING, Service.EXAM_OFFLINE, PAPER_PRODUCT_HOST + "/service/examOnOffLine");
        SERVICE_REGISTRY.put(Mode.PRODUCTION, Service.EXAM_OFFLINE, PAPER_PRODUCT_HOST + "/service/examOnOffLine");

        // 撤销
        SERVICE_REGISTRY.put(Mode.DEVELOPMENT, Service.EXAM_WITHDRAW,  PAPER_DEV_HOST + "/service/evaluationWithdraw");
        SERVICE_REGISTRY.put(Mode.TEST, Service.EXAM_WITHDRAW, PAPER_DEV_HOST + "/service/evaluationWithdraw");
        SERVICE_REGISTRY.put(Mode.STAGING, Service.EXAM_WITHDRAW, PAPER_PRODUCT_HOST + "/service/evaluationWithdraw");
        SERVICE_REGISTRY.put(Mode.PRODUCTION, Service.EXAM_WITHDRAW, PAPER_PRODUCT_HOST + "/service/evaluationWithdraw");

        // 查询是否有报告
        SERVICE_REGISTRY.put(Mode.DEVELOPMENT, Service.EXAM_REPORT, REPORT_DEV_HOST + "/api/v1/checkReportData");
        SERVICE_REGISTRY.put(Mode.TEST, Service.EXAM_REPORT, REPORT_DEV_HOST + "/api/v1/checkReportData");
        SERVICE_REGISTRY.put(Mode.STAGING, Service.EXAM_REPORT, REPORT_PRODUCT_HOST + "/api/v1/checkReportData");
        SERVICE_REGISTRY.put(Mode.PRODUCTION, Service.EXAM_REPORT, REPORT_PRODUCT_HOST + "/api/v1/checkReportData");

        //中学
        //试卷查询
        SERVICE_REGISTRY.put(Mode.DEVELOPMENT, Service.MIDDLE_SCHOOL_EXAM_PAPER, MIDDLE_SCHOOL_PAPER_DEV_HOST + "/service/search-exam-papers");
        SERVICE_REGISTRY.put(Mode.TEST, Service.MIDDLE_SCHOOL_EXAM_PAPER, MIDDLE_SCHOOL_PAPER_DEV_HOST + "/service/search-exam-papers");
        SERVICE_REGISTRY.put(Mode.STAGING, Service.MIDDLE_SCHOOL_EXAM_PAPER, MIDDLE_SCHOOL_PAPER_PRODUCT_HOST + "/service/search-exam-papers");
        SERVICE_REGISTRY.put(Mode.PRODUCTION, Service.MIDDLE_SCHOOL_EXAM_PAPER, MIDDLE_SCHOOL_PAPER_PRODUCT_HOST + "/service/search-exam-papers");

        //试卷搜索选项查询
        SERVICE_REGISTRY.put(Mode.DEVELOPMENT, Service.MIDDLE_SCHOOL_PAPER_SEARCH_ITEM, MIDDLE_SCHOOL_PAPER_DEV_HOST + "/service/get-paper-configs");
        SERVICE_REGISTRY.put(Mode.TEST, Service.MIDDLE_SCHOOL_PAPER_SEARCH_ITEM, MIDDLE_SCHOOL_PAPER_DEV_HOST + "/service/get-paper-configs");
        SERVICE_REGISTRY.put(Mode.STAGING, Service.MIDDLE_SCHOOL_PAPER_SEARCH_ITEM, MIDDLE_SCHOOL_PAPER_PRODUCT_HOST + "/service/get-paper-configs");
        SERVICE_REGISTRY.put(Mode.PRODUCTION, Service.MIDDLE_SCHOOL_PAPER_SEARCH_ITEM, MIDDLE_SCHOOL_PAPER_PRODUCT_HOST + "/service/get-paper-configs");
    }

    @AllArgsConstructor
    public enum Service {

        PAPER_CREATE("创建试卷", RequestMethod.POST, 10 * 1000, 30 * 1000) {
            @Override
            public String getUrl(Mode mode) {
                return SERVICE_REGISTRY.get(mode, this);
            }
        },

        PAPER_CHECK("检查试卷", RequestMethod.POST, 10 * 1000, 30 * 1000) {
            @Override
            public String getUrl(Mode mode) {
                return SERVICE_REGISTRY.get(mode, this);
            }
        },

        PAPER_QUERY("查询试卷", RequestMethod.POST, 10 * 1000, 30 * 1000) {
            @Override
            public String getUrl(Mode mode) {
                return SERVICE_REGISTRY.get(mode, this);
            }
        },

        EXAM_APPLY("申请考试", RequestMethod.POST, 10 * 1000, 30 * 1000) {
            @Override
            public String getUrl(Mode mode) {
                return SERVICE_REGISTRY.get(mode, this);
            }
        },

        EXAM_ONLINE("考试上线", RequestMethod.POST, 10 * 1000, 30 * 1000) {
            @Override
            public String getUrl(Mode mode) {
                return SERVICE_REGISTRY.get(mode, this);
            }
        },

        EXAM_OFFLINE("考试下线", RequestMethod.POST, 10 * 1000, 30 * 1000) {
            @Override
            public String getUrl(Mode mode) {
                return SERVICE_REGISTRY.get(mode, this);
            }
        },
        EXAM_WITHDRAW("考试撤销", RequestMethod.POST, 10 * 1000, 30 * 1000) {
            @Override
            public String getUrl(Mode mode) {
                return SERVICE_REGISTRY.get(mode, this);
            }
        },
        EXAM_REPORT("查询报告", RequestMethod.GET, 10 * 1000, 30 * 1000) {
            @Override
            public String getUrl(Mode mode) {
                return SERVICE_REGISTRY.get(mode, this);
            }
        },

        //中学
        MIDDLE_SCHOOL_EXAM_PAPER("中学查询试卷", RequestMethod.POST, 10 * 1000, 30 * 1000) {
            @Override
            public String getUrl(Mode mode) {
                return SERVICE_REGISTRY.get(mode, this);
            }
        },
        MIDDLE_SCHOOL_PAPER_SEARCH_ITEM("中学试卷搜索选项查询", RequestMethod.POST, 10 * 1000, 30 * 1000) {
            @Override
            public String getUrl(Mode mode) {
                return SERVICE_REGISTRY.get(mode, this);
            }
        },
        ;

        public final String desc;
        public final RequestMethod method;
        public final int connect_timeout;
        public final int socket_timeout;

        public abstract String getUrl(Mode mode);
    }

    /**
     * 根据服务枚举构建一个可用的POST链接
     *
     * @param service 服务枚举
     * @return post链接
     */
    public static POST build(Service service) {
        HttpRequestExecutor executor = HttpRequestExecutor.instance(HttpClientType.POOLING);
        POST post = executor.post(service.getUrl(RuntimeMode.current()));
        post.connectionTimeout(service.connect_timeout);
        post.socketTimeout(service.socket_timeout);
        return post;
    }

    /**
     * 根据服务枚举构建一个可用的GET链接
     * @param service 服务枚举
     * @return get链接
     */
    public static GET buildGet(Service service){
        GET get = HttpRequestExecutor.defaultInstance().get(service.getUrl(RuntimeMode.current()));
        get.connectionTimeout(service.connect_timeout);
        get.socketTimeout(service.socket_timeout);
        return get;
    }
}