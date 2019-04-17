
<div class="schoolParticular-box">
    <div class="particular-title"><i class="icon"></i>关键KP：</div>
    <div class="particular-info">
    <#if (schoolKpInfo["ambassadorMap"])?? && schoolKpInfo["ambassadorMap"]?size gt 0>
        <p>校园大使：
            <#list schoolKpInfo["ambassadorMap"]?keys as key>
                <#assign ambassadorMap = schoolKpInfo["ambassadorMap"][key]>
                <a class="orange-color" href="/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=${key!0}">${ambassadorMap!'--'}</a>
            </#list>
        </p>
    </#if>
    <#if schoolBasicInfo?has_content && schoolBasicInfo.schoolLevel?? && (schoolBasicInfo.schoolLevel == "MIDDLE" || schoolBasicInfo.schoolLevel == "HIGH")>
        <#if schoolKpInfo["schoolAffairTeachers"]?? && schoolKpInfo["schoolAffairTeachers"]?size gt 0>
            <p>教务老师：<span class="txtOrange">
                <#list schoolKpInfo["schoolAffairTeachers"] as key>
                    <a
                        <#if key.userType?? && key.userType == 1>
                                href="/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=${key.id!'0'}" class="orange-color"
                        </#if>
                    >${key.fetchRealname()}</a><#if key_has_next> 、</#if >
                </#list>
                                    </span>
            </p>
        </#if>
        <#if schoolKpInfo["schoolQuizBankAdministratorTeachers"]?? && schoolKpInfo["schoolQuizBankAdministratorTeachers"]?size gt 0>
            <p>校本题库管理员： <span class="txtOrange">
                <#list schoolKpInfo["schoolQuizBankAdministratorTeachers"] as key>
                    <a href="/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=${key.id!'0'}" class="orange-color">${key.fetchRealname()}</a> <#if key_has_next> 、</#if >
                </#list>
                                </span>
            </p>
        </#if>
        <#if schoolKpInfo["schoolKlxSubjectLeader"]?? && schoolKpInfo["schoolKlxSubjectLeader"]?size gt 0>
            <p>快乐学学科组长：<span class="txtOrange">
                <#list schoolKpInfo["schoolKlxSubjectLeader"] as key>
                    <a href="/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=${key.id!'0'}" class="orange-color">${key.fetchRealname()}</a><#if key_has_next> 、</#if >
                </#list>
                                    </span>
            </p>
        </#if>
        <#if schoolKpInfo["gradeManagerTeachers"]?? && schoolKpInfo["gradeManagerTeachers"]?size gt 0>
            <p>快乐学年级主任：<span class="txtOrange">
                <#list schoolKpInfo["gradeManagerTeachers"] as key>
                    <a href="/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=${key.id!'0'}" class="orange-color">${key.fetchRealname()}</a><#if key_has_next> 、</#if >
                </#list>
                                </span>
            </p>
        </#if>
        <#if schoolKpInfo["classManagerTeachers"]?? && schoolKpInfo["classManagerTeachers"]?size gt 0>
            <p>快乐学班主任：<span class="txtOrange">
                <#list schoolKpInfo["classManagerTeachers"] as key>
                    <a href="/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=${key.id!'0'}" class="orange-color">${key.fetchRealname()}</a><#if key_has_next> 、</#if >
                </#list>
                                </span>
            </p>
        </#if>
    </#if>
    <#if schoolKpInfo["presidentTeachers"]?? && schoolKpInfo["presidentTeachers"]?size gt 0>
        <p>校长：
            <#list schoolKpInfo["presidentTeachers"] as p>
                <a class="orange-color" href="/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=${p.id!'0'}">${p.profile.realname!'--'}</a><#if p_has_next> 、</#if >
            </#list>
        </p>
    </#if>
    <#if schoolKpInfo["directorTeachers"]?? && schoolKpInfo["directorTeachers"]?size gt 0>
        <p>主任：
            <#list schoolKpInfo["directorTeachers"] as d>
                <a class="orange-color" href="/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=${d.id!'0'}">${d.profile.realname!'--'}</a>
            </#list>
        </p>
    </#if>
    <#if schoolKpInfo["leaderTeachers"]?? && schoolKpInfo["leaderTeachers"]?size gt 0>
        <p>组长：
            <#list schoolKpInfo["leaderTeachers"] as l>
                <a class="orange-color" href="/view/mobile/crm/teacher/teacher_card_new.vpage?teacherId=${l.id!'0'}">${l.profile.realname!'--'}</a>
            </#list>
        </p>
    </#if>
    </div>
</div>
