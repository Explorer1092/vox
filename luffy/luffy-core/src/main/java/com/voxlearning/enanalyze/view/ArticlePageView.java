package com.voxlearning.enanalyze.view;

import com.voxlearning.utopia.enanalyze.model.ArticleGeneralInfo;
import lombok.Data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 批改记录分页视图
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class ArticlePageView implements Serializable {
    private int page;
    private int size;
    private List<Record> data;

    @Data
    public static class Record implements Serializable {
        /**
         * id
         */
        private String id;

        /**
         * 原文
         */
        private String text;

        /**
         * 创建时间
         */
        private Date createDate;

        /**
         * 创建时间
         */
        private String _createDate;

        public static class Builder {
            public static Record build(ArticleGeneralInfo info) {
                Record record = new Record();
                record.setId(info.getId());
                record.setText(info.getText());
                Date createDate = info.getCreateDate();
                record.setCreateDate(createDate);
                SimpleDateFormat _sdf = new SimpleDateFormat("MM月dd日 HH:mm:ss");
                record.set_createDate(_sdf.format(createDate));
                return record;
            }
        }
    }
}
