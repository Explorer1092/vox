package com.voxlearning.utopia.mizar.entity.yiqijt;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
public class YiqiJTConfCourseMapper {
    private Long id;
    private String grade;
    private String subject;
    private String title;
    private String lecturerUserName;
    private String lecturerIntroduction;
    private Long price;
    private String textContent;
    private String titlePictureUrl;
    private String videoPictureUrl;
    private String appPictureUrl;
    private String url;
//    private Date openTime;
//    private Date updateDatetime;
//    private boolean isOpen;
//    private Integer activeTime;
//    private Integer status;
    private Integer topNum;

    private Boolean featuring; // 首页推荐
    private String label; // 标签
    private List<YiqiJTConfCourseCatalogMapper> cataloglist;
    private List<YiqiJTConfChoiceNoteMapper> choiceNoteList;
    private List<YiqiJTConfCourseOuterchainMapper> outerchainList;

    private Integer source;  // 归属来源 0 教学助手 1 江西教学助手
    private String category; // 资源分类

    public enum Price{
        FREE(0, "免费"),
        INTEGRAL_1000(1000, "100");

        private int integralNum;
        private String integralName;
        Price(int integralNum, String integralName) {
            this.integralNum = integralNum;
            this.integralName = integralName;
        }
        public int getNum() {
            return integralNum;
        }

        public String getName() {
            return integralName;
        }

        public static String getNameByNum(int key) {
            Price[] enums = Price.values();
            for (int i = 0; i < enums.length; i++) {
                if (enums[i].getNum() == key) {
                    return enums[i].getName();
                }
            }
            return "";
        }

    }
}
