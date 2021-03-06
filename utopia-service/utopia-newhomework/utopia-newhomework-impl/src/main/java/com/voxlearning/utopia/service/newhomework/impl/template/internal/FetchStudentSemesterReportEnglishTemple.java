package com.voxlearning.utopia.service.newhomework.impl.template.internal;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;

import javax.inject.Named;

@Named
public class FetchStudentSemesterReportEnglishTemple extends FetchStudentSemesterReportBaseTemple{
    @Override
    public Subject getSubject() {
        return Subject.ENGLISH;
    }

    @Override
    protected String getBookName(NewBookCatalog newBookCatalog) {
        return newBookCatalog.getName();
    }
}
