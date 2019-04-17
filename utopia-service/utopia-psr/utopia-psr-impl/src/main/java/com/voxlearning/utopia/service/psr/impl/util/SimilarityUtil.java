package com.voxlearning.utopia.service.psr.impl.util;

import com.voxlearning.alps.core.util.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/7/29.
 */
public class SimilarityUtil {

    private static final Pattern htmlPattern = Pattern.compile("<[^>]*>");
    private static final Pattern wordPattern = Pattern.compile("([a-zA-Z|'])+");
    private static final Double threshold = 0.3;

    public static  Set<String> getWords(String content){
        Set<String> set = new HashSet<>();
        if (StringUtils.isBlank(content))
            return set;

        Matcher matcher = htmlPattern.matcher(content);
        // 去掉html标签
        String contentNew = matcher.replaceAll("");
        matcher = wordPattern.matcher(contentNew);

        while(matcher.find()){
            String word = matcher.group();
            word = word.toLowerCase();
            set.add(word);
        }
        return set;
    }

    // Jaccard similarity
    public  static boolean isSimilar(Set<String> words1, Set<String> words2){
        Set<String> innerWords = new HashSet<>();
        innerWords.clear();
        innerWords.addAll(words1);
        innerWords.retainAll(words2);

        Double innerSize = (double)(innerWords.size());
        if (innerSize == 0)
            return false;

        Set<String> unionWords = new HashSet<>();
        unionWords.clear();
        unionWords.addAll(words1);
        unionWords.addAll(words2);

        Double unionSize = (double)(unionWords.size());

        if ((innerSize/unionSize) > threshold){
            return true;
        } else {
            return false;
        }
    }

}
