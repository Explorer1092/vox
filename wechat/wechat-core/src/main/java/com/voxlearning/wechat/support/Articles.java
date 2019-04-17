package com.voxlearning.wechat.support;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Xin Xin
 * @since 10/19/15
 */
@Getter
@Setter
public class Articles {
    private List<Article> articles;

    public Articles() {
        articles = new ArrayList<>();
    }
}
