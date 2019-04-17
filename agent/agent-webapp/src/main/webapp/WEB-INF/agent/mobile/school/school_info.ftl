<#import "../layout_new.ftl" as layout>
<@layout.page group="搜索" title="学校详情">
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back()" class="headerBack">&lt;&nbsp;返回</a>
            <div class="headerText">学校信息</div>
        </div>
    </div>
</div>
    <#if schoolSummary??>
    <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
        <a href="/mobile/school_clue/new_school_detail.vpage?schoolId=${schoolId!''}" class="message message-link">
            <div class="build"></div>
            <div class="name" style="font-size: 0.9rem;">${schoolSummary.schoolName!}</div>
            <div class="id" style="font-size: 0.8rem;">
                <span>ID:${schoolSummary.schoolId!}</span>
            </div>
            <div style="font-size: 0.8rem;">
                <#if schoolSummary.authStatus?? && schoolSummary.authStatus == 1>
                    <span style="color: green;"><b>鉴定通过</b></span>
                <#elseif schoolSummary.authStatus?? && schoolSummary.authStatus == 3>
                    <span style="color: red;"><b>假学校</b></span>
                <#else>
                    <span style="color: red;"><b>待鉴定</b></span>
                </#if>
            </div>
        </a>
    </div>

    <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt" id="teacher_statis">

        <div class="list">
            <div>
                <div class="hd red"><#if schoolSize?? && schoolSize gt 0 >${schoolSize}<#else>-</#if></div>
                <div class="ft">学校规模</div>
            </div>
            <div>
                <a href="/mobile/school/statistics_school_clazz.vpage?schoolId=${schoolSummary.schoolId!}&type=1">
                    <div class="hd red"><#if statisticsData??>${statisticsData.totalCount!0}<#else>0</#if></div>
                    <div class="ft" style="color:#a2a2a2;">注册学生</div>
                </a>
            </div>
            <div>
                <a href="/mobile/school/teacher_register_info_list.vpage?schoolId=${schoolSummary.schoolId!}">
                    <div class="hd red"><#if schoolSummary.teacherTotalList??>${schoolSummary.teacherTotalList?size}<#else>
                        0</#if></div>
                    <div class="ft" style="color:#a2a2a2;">注册老师</div>
                </a>
            </div>
        </div>

    </div>


    <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
        <a href="/mobile/school/statistics_school_clazz.vpage?schoolId=${schoolSummary.schoolId!}&type=2"
           class="hdLink">认证学生<span class="side-fr side-nums"><span
                style="font-size: 0.8rem;">累计</span>&nbsp;&nbsp;<#if statisticsData??>${statisticsData.authedCount!0}<#else>
            0</#if></span></a>
        <ul class="mobileCRM-V2-list">
            <#if authedList?? && authedList?size gt 0>
                <#list authedList as item>
                    <#if item.teacherData??>
                        <li>
                            <a href="/mobile/teacher/v2/teacher_info.vpage?teacherId=${item.teacherData.teacherId!}"
                               class="link">
                                <div class="side-fl " style="color:#a2a2a2;">
                                ${item.teacherData.realName!''}&nbsp;&nbsp;
                                <span class="mobileCRM-V2-globalTag mobileCRM-V2-globalTag-green">
                                    <#if item.teacherData.subject?? && item.teacherData.subject.value?? && item.teacherData.subject.value?length gt 0>${item.teacherData.subject.value[0..0]}</#if>
                                </span>
                                <span class="mobileCRM-V2-globalTag mobileCRM-V2-globalTag-orange">
                                    <#if item.teacherData.authStatus?? && item.teacherData.authStatus.state?? && item.teacherData.authStatus.state == 1>
                                        证</#if>
                                </span>
                                </div>

                                <#if item.statisticsData??>
                                    <div class="side-fr side-width side-orange"
                                         style="width: 5%;">${(item.statisticsData.totalCount!0) - (item.statisticsData.authedCount!0)}</div>
                                    <div class="side-fr side-width side-small" style="width: 15%;">剩余</div>
                                    <div class="side-fr side-width"
                                         style="width: 5%;">${item.statisticsData.authedCount!0}</div>
                                    <div class="side-fr side-width side-small" style="width: 10%;">认证</div>
                                </#if>
                            </a>
                        </li>
                    </#if>
                </#list>
            <#else>
                <li>
                    <div class="side-fl side-small" style="width: 50%; line-height: 1.5rem;">
                        暂无推荐
                    </div>
                </li>
            </#if>
        </ul>
    </div>


    <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
        <a href="/mobile/school/statistics_school_clazz.vpage?schoolId=${schoolSummary.schoolId!}&type=3"
           class="hdLink">本月高质量学生<span class="side-fr side-nums"><span
                style="font-size: 0.8rem;">累计</span>&nbsp;&nbsp;<#if statisticsData??>${statisticsData.hcaActiveCount!0}<#else>
            0</#if></span></a>
        <ul class="mobileCRM-V2-list">
            <#if hcaActiveList?? && hcaActiveList?size gt 0>
                <#list hcaActiveList as item>
                    <#if item.teacherData??>
                        <li>
                            <a href="/mobile/teacher/v2/teacher_info.vpage?teacherId=${item.teacherData.teacherId!}"
                               class="link">
                                <div class="side-fl " style="color:#a2a2a2;">
                                ${item.teacherData.realName!''}&nbsp;&nbsp;
                                    <span class="mobileCRM-V2-globalTag mobileCRM-V2-globalTag-green">
                                        <#if item.teacherData.subject?? && item.teacherData.subject.value?? && item.teacherData.subject.value?length gt 0>${item.teacherData.subject.value[0..0]}</#if>
                                    </span>
                                    <span class="mobileCRM-V2-globalTag mobileCRM-V2-globalTag-orange">
                                        <#if item.teacherData.authStatus?? && item.teacherData.authStatus.state?? && item.teacherData.authStatus.state == 1>
                                            证</#if>
                                    </span>
                                </div>

                                <#if item.statisticsData??>
                                    <div class="side-fr side-width side-orange"
                                         style="width: 5%;">${(item.statisticsData.authedCount!0) - (item.statisticsData.hcaActiveCount!0)}</div>
                                    <div class="side-fr side-width side-small" style="width: 15%;">剩余</div>
                                    <div class="side-fr side-width"
                                         style="width: 5%;">${item.statisticsData.hcaActiveCount!0}</div>
                                    <div class="side-fr side-width side-small" style="width: 10%;">本月</div>
                                </#if>
                            </a>
                        </li>
                    </#if>
                </#list>
            <#else>
                <li>
                    <div class="side-fl side-small" style="width: 50%; line-height: 1.5rem;">
                        暂无推荐
                    </div>
                </li>
            </#if>
        </ul>
    </div>


    <div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
        <a href="/mobile/school/statistics_school_clazz.vpage?schoolId=${schoolSummary.schoolId!}&type=4"
           class="hdLink">双科认证学生<span class="side-fr side-nums"><span
                style="font-size: 0.8rem;">累计</span>&nbsp;&nbsp;<#if statisticsData??>${statisticsData.doubleSubjectCount!0}<#else>
            0</#if></span></a>
        <ul class="mobileCRM-V2-list">
            <#if doubleAuthList?? && doubleAuthList?size gt 0>
                <#list doubleAuthList as item>
                    <#if item.teacherData??>
                        <li>
                            <a href="/mobile/teacher/v2/teacher_info.vpage?teacherId=${item.teacherData.teacherId!}"
                               class="link">
                                <div class="side-fl " style="color:#a2a2a2;">
                                ${item.teacherData.realName!''}&nbsp;&nbsp;
                                            <span class="mobileCRM-V2-globalTag mobileCRM-V2-globalTag-green">
                                                <#if item.teacherData.subject?? && item.teacherData.subject.value?? && item.teacherData.subject.value?length gt 0>${item.teacherData.subject.value[0..0]}</#if>
                                            </span>
                                            <span class="mobileCRM-V2-globalTag mobileCRM-V2-globalTag-orange">
                                                <#if item.teacherData.authStatus?? && item.teacherData.authStatus.state?? && item.teacherData.authStatus.state == 1>
                                                    证</#if>
                                            </span>
                                </div>

                                <#if item.statisticsData??>
                                    <div class="side-fr side-width side-orange"
                                         style="width: 5%;">${item.statisticsData.noDoubleSubject!0}</div>
                                    <div class="side-fr side-width side-small" style="width: 15%;">剩余</div>
                                    <div class="side-fr side-width"
                                         style="width: 5%;">${item.statisticsData.doubleSubjectCount!0}</div>
                                    <div class="side-fr side-width side-small" style="width: 10%;">双科</div>
                                </#if>
                            </a>
                        </li>
                    </#if>
                </#list>
            <#else>
                <li>
                    <div class="side-fl side-small" style="width: 50%; line-height: 1.5rem;">
                        暂无推荐
                    </div>
                </li>
            </#if>
        </ul>
    </div>
    <#else>
    <ul class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
        <li>
            <div class="list">
                <div class="side-fl side-small">暂无学校数据</div>
            </div>
        </li>
    </ul>
    </#if>

<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <ul class="mobileCRM-V2-list">
        <li>
            <a href="/mobile/work_record/schoolRecordListPage.vpage?schoolId=${schoolId!''}" class="link link-ico">
                <div class="side-fl">进校记录</div>
            </a>
        </li>
    </ul>
</div>

<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <div class="hdText">校园大使</div>
    <ul class="mobileCRM-V2-list">
        <#if ambassadorList?? && ambassadorList?size gt 0>
            <#list ambassadorList as ambassador>
                <#if ambassador??>
                    <li>
                        <a href="/mobile/teacher/v2/teacher_info.vpage?teacherId=${ambassador.teacherId!}"
                           class="link link-ico">
                            <div class="side-fl side-mode side-info">${ambassador.realName!''}</div>
                            &nbsp;&nbsp;
                            <span class="mobileCRM-V2-globalTag mobileCRM-V2-globalTag-green">
                                <#if ambassador.subjectValueWithoutDefault?? && ambassador.subjectValueWithoutDefault?length gt 0>${ambassador.subjectValueWithoutDefault[0..0]}</#if>
                            </span>
                            <span class="mobileCRM-V2-globalTag mobileCRM-V2-globalTag-orange">
                                <#if ambassador.authState?? && ambassador.authState == 1>证</#if>
                            </span>
                        </a>
                    </li>
                </#if>
            </#list>
        <#else >
            <li>
                <div class="box">
                    <div class="side-fl " style="color:#a2a2a2;">暂无校园大使</div>
                </div>
            </li>
        </#if>
    </ul>
</div>

</@layout.page>