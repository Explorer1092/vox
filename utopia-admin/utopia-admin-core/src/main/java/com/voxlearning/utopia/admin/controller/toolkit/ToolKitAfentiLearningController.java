/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.toolkit;

import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.utopia.service.content.api.entity.Book;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: tanguohong
 * Date: 13-10-12
 * Time: 上午11:32
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/toolkit/afenti/learningplan")
@NoArgsConstructor
public class ToolKitAfentiLearningController extends ToolKitAbstractController {

    private List<Book> loadAfentiBooks(final Term term) {
        if (term == null) {
            return Collections.emptyList();
        }
        List<Book> bookList = regionContentLoaderClient.loadAfentiEnglishBooks("EXAM");
        bookList = bookList.stream().filter(source -> source.fetchtTermType() == term).collect(Collectors.toList());

        return bookList;
    }
}
