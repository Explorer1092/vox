package com.voxlearning.utopia.enanalyze.service.support;

import org.springframework.stereotype.Service;

/**
 * 学情报告处理类
 *
 * @author xiaolei.li
 * @version 2018/7/19
 */
@Service
public class ArticleReportor {

    /**
     * 获取标签
     *
     * @param score 作文评分
     * @return 标签
     */
    public String getTag(float score) {
        String tag;
        if (95 <= score && 100 >= score) {
            tag = "X星人";
        } else if (90 <= score) {
            tag = "星际外交官";
        } else if (87 <= score) {
            tag = "燃爆超新星";
        } else if (84 <= score) {
            tag = "神仙C位";
        } else if (80 <= score) {
            tag = "SpaceX火箭";
        } else if (77 <= score) {
            tag = "唤星骑士";
        } else if (74 <= score) {
            tag = "星际漫游者";
        } else if (70 <= score) {
            tag = "追光达人";
        } else if (65 <= score) {
            tag = "静心大师";
        } else if (55 <= score) {
            tag = "黎明之光";
        } else if (40 <= score) {
            tag = "逆天暗物质";
        } else if (30 <= score) {
            tag = "潜能者";
        } else if (20 <= score) {
            tag = "未知能力者";
        } else {
            tag = "求生欲超强";
        }
        return tag;
    }
}
