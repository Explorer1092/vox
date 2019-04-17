package com.voxlearning.utopia.service.newhomework.impl.support;

import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachUniSound7SentenceScoreLevel;

import javax.inject.Named;

/**
 * @Description: 图文入韵星级计算
 * @author: Mr_VanGogh
 * @date: 2018/12/24 下午8:19
 */
@Named
public class ImageTextRhymeStarCalculator {

    public int calculateImageTextRhymeStar(int score) {
        int star = 0;
        if (score >= WordTeachUniSound7SentenceScoreLevel.A.getMinScore()) {
            star = 3;
        } else if (score >= WordTeachUniSound7SentenceScoreLevel.B.getMinScore()) {
            star = 2;
        } else if (score >= WordTeachUniSound7SentenceScoreLevel.C.getMinScore()) {
            star = 1;
        }
        return star;
    }

    public int calculateImageTextRhymeScore(int star) {
        int score;
        if (star == 3) {
            score = 100;
        } else if (star == 2) {
            score = 90;
        } else if (star == 1) {
            score = 75;
        } else {
            score = 60;
        }
        return score;
    }
}
