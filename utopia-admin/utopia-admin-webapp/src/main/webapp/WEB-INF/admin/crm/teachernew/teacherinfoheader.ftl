<#macro teacherHead>
    <#setting datetime_format="yyyy-MM-dd"/>
<link href="${requestContext.webAppContextPath}/public/css/teachernew/teacherdetail.css" rel="stylesheet">
<div>
    <div>
        <fieldset>
            <font class="txt" size="5">
            ${teacherInfoHeaderMap["teacherName"]!''}(${teacherId!''})
                <#if teacherInfoHeaderMap["isCheat"]!false>
                    <span style="color: red;">作弊老师</span>
                </#if>
                <#if mainaccountflag && (accountMainSubSubjectMap??&&accountMainSubSubjectMap?size lt 3)>
                    <a href="javascript:void(0);" id="subAccountApply" class="btn btn-info"> 开通包班 </a>
                </#if>
                <#if mainaccountflag && (accountMainSubSubjectMap??&&accountMainSubSubjectMap?size gt 1)>
                    <a href="javascript:void(0);" id="cancelAccountApply" class="btn btn-info"> 取消包班 </a>
                </#if>
                <#if !mainaccountflag && (accountMainSubSubjectMap??&&accountMainSubSubjectMap?size gt 1)>
                    <a href="javascript:void(0);" id="changeToMainCount" class="btn btn-info"> 变更为主账号 </a>
                </#if>
            </font>
            <img class="modifyteachername" src="/public/img/w-icon-7.png"
                 style="height: 16px;width: 16px;margin-bottom: .2em;margin-left: .3em"/>
            <div style="float: right">
                <#if accountMainSubSubjectMap??&&accountMainSubSubjectMap?size gt 1>
                    <span class="txt">主副账号切换:</span>
                    <select name="pageselect" onchange="parent.location.href=options[selectedIndex].value">
                        <#list accountMainSubSubjectMap as account>
                            <option value="teacherdetail.vpage?teacherId=${account.key!''}"
                                    <#if "${teacherId!''}"=="${account.key}">selected="selected" </#if>>${account.value!''}</option>
                        </#list>
                    </select>
                </#if>
            </div>
        </fieldset>
    </div>
    <fieldset class="personalInfo">
            <span class="basic_info">老师状态：
                <#if teacherInfoHeaderMap["authenticationState"]?has_content && teacherInfoHeaderMap["authenticationState"] == 1>
                    <img title="${teacherInfoHeaderMap["authType"]!''}" style="margin-bottom: .2em;"
                         src="/public/img/w-icon-2.png">
                <#else >
                    <img style="margin-bottom: .2em;" src="/public/img/w-icon-4.png">
                </#if>
                <#if teacherInfoHeaderMap["isAmbassador"]?has_content && teacherInfoHeaderMap["isAmbassador"] ==true>
                    <img title="成为大使时间:${teacherInfoHeaderMap["ambassadorTime"]!''}" style="margin-bottom: .2em;"
                         src="/public/img/w-icon-6.png">
                <#else >
                    <img style="margin-bottom: .2em;" src="/public/img/w-icon-8.png">
                </#if>
                <#if teacherInfoHeaderMap["fakeTeacher"]?has_content && teacherInfoHeaderMap["fakeTeacher"] ==true>
                    <#if teacherInfoHeaderMap["fakeType"]?has_content && teacherInfoHeaderMap["fakeType"] == "MANUAL_VALIDATION">
                        <img title="${teacherInfoHeaderMap["fakeTypeDesc"]!''}" style="margin-bottom: .2em;"
                             src="/public/img/w-icon-1.png">
                    <#else>
                        <img title="${teacherInfoHeaderMap["fakeTypeDesc"]!''}" style="margin-bottom: .2em;"
                             src="/public/img/w-icon-01.png">
                    </#if>
                <#else >
                    <img style="margin-bottom: .2em;" src="/public/img/w-icon-3.png">
                </#if>
                <#if teacherInfoHeaderMap["accountStatus"]?has_content  && teacherInfoHeaderMap["accountStatus"] == 'NORMAL'>
                    <div style="display: inline-block; border: 1px solid #999; font-size: 18px;font-weight: 700;border-radius: 5px; padding: 3px 4px 4px;margin-bottom: .2em;background-color: #ddd;color: #fff;">封</div>
                    <input id="forbid"  type="button" value="封禁">
                <#else >
                    <div style="display: inline-block; border: 1px solid #999; font-size: 18px;font-weight: 700;border-radius: 5px; padding: 3px 4px 4px;margin-bottom: .2em;background-color: #ee5f5b;color: #fff;">封</div>
                    <input id="forbid" type="button" value="解封">
                </#if>
            </span>
        <span class="basic_info">老师手机：
            <span class="txt">
                <#if teacherId??>
                    <#assign subFlag = subaccountStateMap["canBindMobile"]?has_content && subaccountStateMap["canBindMobile"]=="true"/>
                    <#if teacherInfoHeaderMap["canBindMobile"] == true><img style="margin-bottom: .2em;"
                                                                            src="/public/img/w-icon-40.png"
                                                                            <#if subFlag>title="副账号不允许绑定手机"</#if>>
                        <#if !requestContext.getCurrentAdminUser().isCsosUser() && !subFlag><input type="button"
                                                                                                   id="btnBindMobile"
                                                                                                   value="绑定"/></#if>
                    <#else>
                        <button type="button" id="query_user_phone_${teacherId}" class="btn btn-info">查看</button><img
                            style="margin-bottom: .2em;" src="/public/img/w-icon-41.png">
                    </#if>
                </#if>
            </span>
        </span>
        <span class="basic_info">老师学科：
            <span class="txt">
                <#if teacherInfoHeaderMap["subject"]?has_content>${teacherInfoHeaderMap["subject"]!''}</#if></span>
                <img src="/public/img/w-icon-7.png"
                     style="height: 16px;width: 16px;margin-bottom: .2em;margin-left: .3em" onclick="changeSubject()"/>
        </span>
        <span class="basic_info">学校信息：
            <span class="txt"><#if teacherInfoHeaderMap["schoolId"]?has_content>
                <a href="/crm/school/schoolhomepage.vpage?schoolId=${teacherInfoHeaderMap["schoolId"]!}"
                   target="_blank">${teacherInfoHeaderMap["schoolName"]!''}</a>(${teacherInfoHeaderMap["schoolId"]!})
            </#if></span>
        <#-- changeschool表示老师带班转校,changeschoolwithoutclazz表示老师不带班转校-->
            <img class="cschooltype" src="/public/img/w-icon-7.png"
                 style="height: 16px;width: 16px;margin-bottom: .2em;margin-left: .3em"/>
        </span>
        <span class="basic_info">
            <#if (isDictSchool == true)!false>
                <b style="color: red;">重点校</b>
                <#if marketInfo?exists>
                    <b style="color: red;">市场:</b>
                    <#if marketInfo["userName"]??>
                        <b style="color: red;">${marketInfo["userName"]!''}</b>
                    </#if>
                    <#if marketInfo["mobile"]??>
                        <b style="color: red;">(${marketInfo["mobile"]!''})</b>
                    </#if>
                </#if>
            </#if>
        </span>
        <#if subjectLeaderMap?has_content >
            <br/>
            <span class="basic_info">学科组长:
            <span class="txt">
            <#if subjectLeaderMap.subjectLeaderFlag == true>
                <#if subjectLeaderMap.clazzLevelStrSet?has_content>
                    <#list subjectLeaderMap.clazzLevelStrSet as clazzLevelStrSet>
                    ${clazzLevelStrSet}
                    </#list>
                <#else>
                    暂无
                </#if>
            <#else>
                非学科组长
            </#if>
                <img class="changeKlxSubjectLeader" src="/public/img/w-icon-7.png"
                     style="height: 16px;width: 16px;margin-bottom: .2em;margin-left: .3em"/>
            </span>
        </span>
        </#if>

        <#if klxTeacherUserName??>
            <span class="basic_info">快乐学用户名: <span class="txt">${klxTeacherUserName}</span></span>
        </#if>

        <#if (seniorTeacher?? && seniorTeacher) || (juniorTeacher?? &&  juniorTeacher)>
            <span class="basic_info">班主任:
        <span class="txt">
            <#if classManagerFlag && managedClassList?has_content > ${managedClassList!} <#else> 非班主任 </#if>
            <img class="changeManagedClass" src="/public/img/w-icon-7.png"
                 style="height: 16px;width: 16px;margin-bottom: .2em;margin-left: .3em" onclick="setClassManager()"/>
        </span>
        </span>
            <span class="basic_info">&nbsp;&nbsp;年级主任:
        <span class="txt">
            <#if gradeManagerFlag && managedGradeList?has_content> ${managedGradeList!} <#else> 非年级主任 </#if>
            <img class="changeManagedGrade" src="/public/img/w-icon-7.png"
                 style="height: 16px;width: 16px;margin-bottom: .2em;margin-left: .3em" onclick="setGradeManager()"/>
        </span>
        </span>
            <span class="basic_info">&nbsp;&nbsp;中学校长:
        <span class="txt">
            <#if schoolMasterFlag!false> 校长 <#else> 非校长 </#if>
            <img class="changeManagedSchool" src="/public/img/w-icon-7.png"
                 style="height: 16px;width: 16px;margin-bottom: .2em;margin-left: .3em" onclick="setSchoolMaster()"/>
        </span>
        </span>
        </#if>
    </fieldset>
</div>
<div style="padding:5px 0 0;">
    <ul class="inline" style="padding:5px 0 0;border:1px #ccc solid; background-color:#e4e4e4;">
        <table style="width:90%;">
            <tr>
                <td>
                    <#if subaccountStateMap["loginIndexState"]?has_content  && subaccountStateMap["loginIndexState"]=="true">
                        <input id="login" type="button" value="登录首页" disabled>
                    <#else>
                        <input id="login" type="button" value="登录首页">
                    </#if>
                </td>
                <td>
                    <#if subaccountStateMap["passwdResetState"]?has_content  && subaccountStateMap["passwdResetState"]=="true">
                        <input id="pwd_reset" type="button" value="密码重置" disabled>
                    <#else>
                        <input id="pwd_reset" type="button" value="密码重置">
                    </#if>
                </td>
                <td class="middle-1">
                    <#if subaccountStateMap["decideForFalseState"]?has_content  && subaccountStateMap["decideForFalseState"]=="true">
                        <input id="faker" type="button" value="判定为假" disabled>
                    <#else>
                        <input id="faker" type="button" value="判定为假">
                    </#if>
                </td>
                <td class="middle-2">
                    <#if teacherInfoHeaderMap["pending"]?has_content  && teacherInfoHeaderMap["pending"] == 1>

                        <input id="pause" disabled type="button" data-id="0" value="恢复"
                               <#if subaccountStateMap["pendingTeacherState"]?has_content  && subaccountStateMap["pendingTeacherState"]=="true">disabled</#if>>
                    <#else >
                        <input id="pause" disabled type="button" data-id="1" value="暂停"
                               <#if subaccountStateMap["pendingTeacherState"]?has_content  && subaccountStateMap["pendingTeacherState"]=="true">disabled</#if>>
                    </#if>
                </td>
                <td>
                    <#if subaccountStateMap["newRecordState"]?has_content  && subaccountStateMap["newRecordState"]=="true">
                        <input id="new-record" type="button" value="新建记录" onclick="record.new(${teacherId!})" disabled>
                    <#else >
                        <input id="new-record" type="button" value="新建记录" onclick="record.new(${teacherId!})">
                    </#if>
                </td>
                <td>
                    <#if subaccountStateMap["newTaskState"]?has_content  && subaccountStateMap["newTaskState"]=="true">
                        <input id="new-task" type="button" value="新建任务" onclick="task.new(${teacherId!})" disabled>
                    <#else>
                        <input id="new-task" type="button" value="新建任务" onclick="task.new(${teacherId!})">
                    </#if>
                </td>
                <#if teacherInfoHeaderMap["authenticationState"] != 1>
                    <td>
                        <#if subaccountStateMap["bigDataAuthState"]?has_content  && subaccountStateMap["bigDataAuthState"]=="true">
                            <input id="big-data-auth" type="button" value="大数据认证" onclick="auth.get(${teacherId!})" disabled>
                        <#else>
                            <input id="big-data-auth" type="button" value="大数据认证" onclick="auth.get(${teacherId!})">
                        </#if>
                    </td>
                </#if>
            </tr>
            <tr>
                <td><input id="call_${teacherId!''}" type="button" value="呼叫老师"></td>
                <td>
                    <#if subaccountStateMap["specialPropertySetState"]?has_content  && subaccountStateMap["specialPropertySetState"]=="true">
                        <input type="button" onclick="viewBlackWhiteAttr()" value="特殊属性设置" disabled>
                    <#else >
                        <input type="button" onclick="viewBlackWhiteAttr()" value="特殊属性设置">
                    </#if>
                </td>
                <td class="middle-1">
                    <#if subaccountStateMap["relieveDecideFalseState"]?has_content  && subaccountStateMap["relieveDecideFalseState"]=="true">
                        <input id="undo_faker" type="button" value="解除判假" disabled>
                    <#else >
                        <input id="undo_faker" type="button" value="解除判假">
                    </#if>
                </td>
                <td class="middle-2">
                    <#if teacherInfoHeaderMap["authenticationState"]?has_content && teacherInfoHeaderMap["authenticationState"] != 1>
                        <#if subaccountStateMap["updateAuthenticationState"]?has_content  && subaccountStateMap["updateAuthenticationState"]=="true">
                            <input id="auth_update" type="button" value="更新认证状态" disabled>
                        <#else >
                            <input id="auth_update" type="button" value="更新认证状态">
                        </#if>
                    <#else>
                        <input id="auth_update" type="button" value="更新认证状态" disabled>
                    </#if>
                </td>
                <td>
                    <#if subaccountStateMap["gardenerBeanProvideSate"]?has_content  && subaccountStateMap["gardenerBeanProvideSate"]=="true">
                        <input id="add_integral" type="button" value="园丁豆发放" disabled>
                    <#else>
                        <input id="add_integral" type="button" value="园丁豆发放">
                    </#if>
                </td>
                <td>
                    <#if subaccountStateMap["telephoneChargeProvideState"]?has_content  && subaccountStateMap["telephoneChargeProvideState"]=="true">
                        <input id="add_fee" type="button" value="话费发放" disabled>
                    <#else>
                        <input id="add_fee" type="button" value="话费发放">
                    </#if>
                </td>

                <td>
                    <input id="add_fault_order" onclick="openFaultOrderDialog()" type="button" value="问题跟踪">
                </td>
                <td>
                    <input id="btnShowDialogDelTeacher" type="button" onclick="javascript:$('#delete_user_dialog').modal('show');"   value="注销账号">
                </td>
            </tr>
        </table>
    </ul>
</div>
<style>
    .class-manage-list {
        text-align: center;
        border: 1px solid #0f92a8;
        background: #51c1f0;
        color: #fff;
        border-radius: 8px;
        padding: 5px;
        margin-right: 5px;
        display: inline-block;
    }

    .js-delClassItemBtn {
        color: #fff;
        padding-left: 5px;
    }

    .grade-selector {
        width: 80px;
        margin-right: 3px;
    }

    .class-selector {
        width: 120px;
        margin-right: 3px;
    }

    .select-class-btn {
        border: 1px solid #FDA700;
        border-radius: 100%;
        cursor: pointer;
        background-color: #FDA700;
        color: #fff;
        display: inline-block;
        width: 20px;
        height: 20px;
        text-align: center;
    }
    .display-ib{
        display: inline-block;
    }
    .vertical-t{
        vertical-align: top;
    }
</style>
<div class="personalTab">
    <a target="teacherNew" class="active" href="/crm/teachernew/teacherinfo.vpage?teacherId=${teacherId!''}">关键信息</a>
    <a target="teacherNew" href="/crm/teachernew/teacherclazz.vpage?teacherId=${teacherId!''}">班级学生</a>
    <#if (teacherInfoHeaderMap["ktwelve"] == 'JUNIOR_SCHOOL')!false>
        <a target="teacherNew" href="${ms_crm_admin_url}/crm/teacher/homeworkhistory?teacherId=${teacherId!''}">使用记录</a>
    </#if>
    <#if (teacherInfoHeaderMap["ktwelve"] == 'PRIMARY_SCHOOL')!false>
        <a target="teacherNew"
           href="/crm/teachernew/teachernewhomeworkhistory.vpage?teacherId=${teacherId!''}">新作业使用记录</a>
    </#if>
    <#if (teacherInfoHeaderMap["ktwelve"] == 'INFANT')!false>
        <a target="teacherNew"
           href="/crm/teachernew/teachernewhomeworkhistory.vpage?teacherId=${teacherId!''}">新作业使用记录</a>
    </#if>
    <#if (teacherInfoHeaderMap["ktwelve"] == 'PRIMARY_SCHOOL')!false>
        <a target="teacherNew" href="/crm/teachernew/newexamclazz.vpage?teacherId=${teacherId!''}">统考查询</a>
    </#if>

    <#if (teacherInfoHeaderMap["ktwelve"] == 'JUNIOR_SCHOOL')!false>
        <a target="teacherNew" href="${ms_crm_admin_url}/crm/teacher/homeworkPlanList?teacherId=${teacherId!''}">假期作业查询</a>
    </#if>
    <#if (teacherInfoHeaderMap["ktwelve"] == 'PRIMARY_SCHOOL')!false>
        <a target="teacherNew" href="/crm/vacation/homework/report/list.vpage?teacherId=${teacherId!''}">寒假作业查询</a>
    </#if>

    <#if (teacherInfoHeaderMap["subjectName"] == 'CHINESE')!false>
        <a target="teacherNew" href="/crm/teachernew/outsidereadingclazz.vpage?teacherId=${teacherId!''}">语文阅读任务查询</a>
    </#if>
    <a target="teacherNew" href="/crm/task/user_record.vpage?userId=${teacherId!}">工作记录</a>
    <a target="teacherNew"
       href="/crm/task/user_task.vpage?userId=${teacherId!}&userType=TEACHER">任务记录(${taskCount!0})</a>
    <a target="teacherNew" href="/crm/teachernew/teacherinviteandmentor.vpage?teacherId=${teacherId!''}">相关老师</a>
    <a target="teacherNew" href="/crm/teachernew/teacherrewardhistory.vpage?teacherId=${teacherId!''}">任务及奖励</a>
    <a target="teacherNew" href="/crm/teachernew/teachernewrewardorder.vpage?teacherId=${teacherId!''}">奖品中心</a>
    <a target="teacherNew" href="/crm/toolkit/toolkit.vpage">工具箱</a>
    <a target="teacherNew" href="/crm/teachernew/aboutlottery.vpage?teacherId=${teacherId!''}">抽奖相关</a>
    <a target="teacherNew"
       href="/crm/teachernew/teacherrewardstudenthistory.vpage?teacherId=${teacherId!''}">智慧课堂奖励记录</a>
    <a target="teacherNew"
       href="/crm/teachernew/teacherintegralwhereaboutsrecord.vpage?teacherId=${teacherId!''}">园丁豆去向</a>
    <a target="teacherNew" href="/crm/teachernew/appteacherlog.vpage?teacherId=${teacherId!''}">APP日志查询</a>
    <a target="teacherNew"
       href="/crm/cs_productfeedback/teacher_feedback_list.vpage?teahcerId=${teacherId!''}">老师反馈记录详情</a>
    <a target="teacherNew" href="/crm/teachernew/funnyeventsearch.vpage?teacherId=${teacherId!''}">趣味活动查询</a>
</div>
    <#include "../specialschool.ftl" />
<#--各种各样的弹出框操作----各种各样的弹出框操作----各种各样的弹出框操作--各种各样的弹出框操作各种各样的弹出框操作-->
<#--更换学科-->
<div id="change_subject_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>更改老师学科</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>老师ID</dt>
                    <dd>${teacherId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>记录类型</dt>
                    <dd>老师操作</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>学科</dt>
                    <dd>
                        <select id="subject_type">
                        <#list validSubjects as subject>
                              <option value="${subject.name}">${subject.value}</option>
                        </#list>
                        </select>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>问题描述</dt>
                    <dd><textarea id="changeSubjectDesc" cols="35" rows="4"></textarea></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>所做操作</dt>
                    <dd>更改老师学科</dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="change_subject_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<#--设置班主任-->
<div id="set_classmanager_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>设置班主任</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>老师ID</dt>
                    <dd>${teacherId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>记录类型</dt>
                    <dd>老师操作</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>班级列表</dt>
                    <dd id="classList"></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>班级列表</dt>
                    <dd id="chooseClass"></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>问题描述</dt>
                    <dd><textarea id="setClassManageDesc" cols="35" rows="4" style="resize:none;"></textarea></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>所做操作</dt>
                    <dd>设置班主任</dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="set_classmanager_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<#--设置年级主任-->
<div id="set_grademanager_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>设置年级主任</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>老师ID</dt>
                    <dd>${teacherId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>记录类型</dt>
                    <dd>老师操作</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>设置年级主任</dt>
                    <dd id="gradeList">
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>问题描述</dt>
                    <dd><textarea id="setGradeManagerDesc" cols="35" rows="4" style="resize: none;"></textarea></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>所做操作</dt>
                    <dd>设置年级主任</dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="set_grademanager_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<#--设置中学校长-->
<div id="set_schoolmaster_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>设置中学校长</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>老师ID</dt>
                    <dd>${teacherId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>记录类型</dt>
                    <dd>老师操作</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>设置校长</dt>
                    <dd>
                        <label class="display-ib" for="isSchoolMaster" >是：<input class="vertical-t" type="radio" id="isSchoolMaster" name="schoolMasterRadio" value="on" <#if schoolMasterFlag!false>checked="true"</#if>></label>
                        <label class="display-ib" for="notSchoolMaster">否：<input class="vertical-t" type="radio" id="notSchoolMaster" name="schoolMasterRadio" value="off" <#if !(schoolMasterFlag!false)>checked="true"</#if>></label>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>问题描述</dt>
                    <dd><textarea id="setSchoolMasterDesc" cols="35" rows="4" style="resize: none;"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="set_schoolmanager_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<#--登录老师账号-->
<div id="teacherLogin_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>登录老师账号</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>老师ID</dt>
                    <dd>${teacherId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>记录类型</dt>
                    <dd>老师操作</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>问题描述</dt>
                    <dd><textarea id="teacherLoginDesc" name="teacherLoginDesc" cols="35" rows="4"></textarea></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>所做操作</dt>
                    <dd>管理员登录老师账号</dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="teacher_login_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<#--重置密码-->
<div id="password_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>重置密码</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>用户ID</dt>
                    <dd>${teacherId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>记录类型</dt>
                    <dd>老师操作</dd>
                </li>
            </ul>
            <ul class="inline" name="bindmobileInput" id="bindmobileInput">
                <li>
                    <dt>绑定手机</dt>
                    <dd><input type="text" name="bindmobile" id="bindmobile" value=""/></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>问题描述</dt>
                    <dd>
                        <div id="password_dialog_radio" class="btn-group" data-toggle="buttons-radio">
                            <button type="button" class="btn active">TQ在线</button>
                            <button type="button" class="btn">TQ电话</button>
                            <button type="button" class="btn">其他</button>
                        </div>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>附加描述</dt>
                    <dd><textarea id="passwordExtraDesc" cols="35" rows="2"></textarea></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>所做操作</dt>
                    <dd>绑定手机,重置用户密码。</dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="dialog_edit_teacher_password" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>
<#--暂停/恢复账号-->
<div id="pending_teacher_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>暂停/恢复老师设置</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>原因</dt>
                    <dd>
                        <textarea id="pending_teacher_desc" rows="5"></textarea>
                    </dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="pending_teacher_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
    <input type="hidden" id="pending_teacher_id" value="" data-id="">
</div>

<#--人工判定假老师-->
<div id="faker_teacher_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>判定假老师</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>原因</dt>
                    <dd>
                        <textarea id="faker_teacher_desc" rows="5">${teacherInfoHeaderMap["fakeDesc"]!''}</textarea>
                    </dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="faker_teacher_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
    <input type="hidden" id="faker_teacher_id" value="">
</div>

<#--解除假老师判定-->
<div id="undo_faker_teacher_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>取消假老师判定</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>原因</dt>
                    <dd>
                        <textarea id="undo_faker_teacher_desc" rows="5"></textarea>
                    </dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="undo_faker_teacher_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
    <input type="hidden" id="undo_faker_teacher_id" value="">
</div>

<#--更新认证状态-->
<div id="update_authentication_dialog" class="modal hide fade" style="width: 700px; margin-left:-350px; ">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>更新认证状态</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>用户ID</dt>
                    <dd>${teacherId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>记录类型</dt>
                    <dd>老师操作</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>认证状态</dt>
                    <dd>
                        <select id="dialog_authenticationState" class="multiple">
                            <#if teacherInfoHeaderMap["authenticationState"]?has_content && teacherInfoHeaderMap["authenticationState"] == 1>
                                <option value="0">等待认证</option>
                                <option value="3">取消认证</option>
                            <#elseif teacherInfoHeaderMap["authenticationState"]?has_content && teacherInfoHeaderMap["authenticationState"] == 0 >
                                <option value="3">取消认证</option>
                                <option value="1">认证通过</option>
                            <#else>
                                <option value="1">认证通过</option>
                            </#if>
                        </select>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>问题描述</dt>
                    <dd>
                        <div id="update_authentication_dialog_radio" class="btn-group" data-toggle="buttons-radio">
                            <button type="button" class="btn active">市场人员反馈</button>
                            <button type="button" class="btn">老师主动申请</button>
                            <button type="button" class="btn">客服电话外呼认证</button>
                            <button type="button" class="btn">其他</button>
                        </div>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>附加描述</dt>
                    <dd><textarea id="updateAuthenticationExtraDesc" cols="35" rows="2"></textarea></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>所做操作</dt>
                    <dd>更新用户认证状态</dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="dialog_update_teacher_authentication" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>
<#-- 封禁/解封用户-->
<div id="forbid_teacher_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <#if teacherInfoHeaderMap["accountStatus"]?has_content  && teacherInfoHeaderMap["accountStatus"] == 'NORMAL'>
        <h3>封禁老师</h3>
        <#else>
        <h3>解封老师</h3>
        </#if>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>原因</dt>
                    <dd>
                        <textarea id="forbid_teacher_desc" rows="5"></textarea>
                    </dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <#if teacherInfoHeaderMap["accountStatus"]?has_content && teacherInfoHeaderMap["accountStatus"] == 'NORMAL'>
        <button id="forbid_teacher_btn" class="btn btn-primary">确 定</button>
        <#else>
         <button id="unforbid_teacher_btn" class="btn btn-primary">确 定</button>
        </#if>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
    <input type="hidden" id="faker_teacher_id" value="">
</div>
<#--发放园丁豆-->
<div id="integral_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>发放园丁豆</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>用户 ID</dt>
                    <dd>${teacherId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>记录类型</dt>
                    <dd><#if teacherInfoHeaderMap["userType"]?has_content &&teacherInfoHeaderMap["userType"] == 1>
                        老师<#else>未知</#if>
                        操作
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>类型</dt>
                    <dd>
                        <select class="multiple" name="integralType" id="integralType">
                            <#list integralTypeList as integralType>
                                <option value='${integralType.type!''}'>${integralType.description!''}</option>
                            </#list>
                        </select>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>园丁豆</dt>
                    <dd><input type="text" name="integral" id="integral" placeholder="只能是数字"/></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>备注</dt>
                    <dd><textarea id="comment" name="comment" cols="35" rows="3" value=""></textarea></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>所做操作</dt>
                    <dd>增加用户园丁豆</dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button class="btn btn-primary"
                onclick="$('#integral_dialog').modal('hide');$('#dialog-confirm').modal('show')">确 定
        </button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="dialog-confirm" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>提 示</h3>
    </div>
    <div class="modal-body">
        <span>是否继续修改？</span>
    </div>
    <div class="modal-footer">
        <button id="integral_confirm" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<#--修改老师学校的选择方案-->
<div id="cschooltype_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>是否要将老师名下的班级一同转到新学校?</h3>
    </div>
    <div class="modal-footer">
        <button id="changeschool_withoutclazz_btn" class="btn btn-primary">不带班转校</button>
        <button id="changeschool_withclazz_btn" class="btn btn-primary">带班转校</button>
    </div>
</div>

<#--不带班级修改老师学校提醒-->
<div id="changeSchool_withoutclazz_warning_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>注意:执行该操作将为老师检查所有作业,并解除与所有班级及学生的关系</h3>
    </div>
    <div class="modal-footer">
        <button id="changeSchool_withoutclazz_warning_backbtn" class="btn btn-primary">返回</button>
        <button id="changeSchool_withoutclazz_warning_confirmbtn" class="btn btn-primary">确定</button>
    </div>
</div>

<#--不带班级修改老师学校-->
<div id="changeSchool_withoutclazz_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>老师不带班更改学校</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>老师ID</dt>
                    <dd>${teacherId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>记录类型</dt>
                    <dd>老师操作</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>学校ID</dt>
                    <dd><input id="schoolId_withoutclazz" type='text'/></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>问题描述</dt>
                    <dd><textarea id="changeSchoolDesc_withoutclazz" cols="35" rows="4"></textarea></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>所做操作</dt>
                    <dd>老师不带班更改学校</dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="changeSchool_withoutclazz_backbtn" class="btn btn-primary">返回</button>
        <button id="changeSchool_withoutclazz_changebtn" class="btn btn-primary">确定</button>
    </div>
</div>

<#--带班级修改老师学校-->
<div id="changeSchool_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>更改老师学校</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>老师ID</dt>
                    <dd>${teacherId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>记录类型</dt>
                    <dd>老师操作</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>学校ID</dt>
                    <dd><input id="schoolId" type='text'/></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>问题描述</dt>
                    <dd><textarea id="changeSchoolDesc" cols="35" rows="4"></textarea></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>所做操作</dt>
                    <dd>老师带班更改学校</dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="change_school_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="clazzTeacher_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>该老师名下存在其他老师的班级</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <table id="teacher_clazz_list">
            </table>
        </dl>
        <div class="modal-footer">
            <button id="clazzTeacher_btn" class="btn btn-primary">关 闭</button>
        </div>
    </div>
</div>

<#--特殊属性-->
<div id="black_white_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>特殊属性设置</h3>
    </div>
    <div class="modal-body" id="activityTypeList">
        <#if activityTypeList??>
            <ul class="unstyled">
                <#list activityTypeList as activity>
                    <li>
                        <label for="bw_${activity.key}">
                            <input id="bw_${activity.key}" type="checkbox"
                                   data-key="${activity.key}"/> ${activity.value}
                        </label>
                    </li>
                </#list>
            </ul>
        </#if>
    </div>
</div>

<!-- 绑定手机 -->
<div id="bind_mobile_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>绑定手机</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>老师ID</dt>
                    <dd>${teacherId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>手机</dt>
                    <dd><input type="text" id="txtMobile"/></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>备注</dt>
                    <dd><textarea id="txtBindMobileDesc" cols="35" rows="4"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="bind_mobile_submit" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>

    <#include "../task/common/task_new.ftl">
    <#include "../task/common/record_new.ftl">
</div>

<#--修改老师姓名-->
<div id="modifyteachername_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>更改用户姓名</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>用户ID</dt>
                    <dd>${teacherId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>记录类型</dt>
                    <dd>老师操作</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>新的姓名</dt>
                    <dd><input id="teacherNewName" placeholder="名字中只能使用汉字" type='text'/></dd>

                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>问题描述</dt>
                    <dd><textarea id="modifyTeacherNameDesc" cols="35" rows="4"></textarea></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>所做操作</dt>
                    <dd>更改用户名字</dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="modifyteachername_dialog_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>
<#--修改学科组长信息 -->
<div id="change_klxsubjectleader_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>更改学科组长信息</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>老师ID</dt>
                    <dd>${teacherId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>记录类型</dt>
                    <dd>老师操作</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>设置学科组长</dt>
                    <dd>
                        是:
                        <input type="radio" name="klxsubjectleader" value="setklxsubjectleader" checked="checked"
                               onclick="$('#subjectleaderclazzlevel').css('display','');"/>
                        否：
                        <input type="radio" name="klxsubjectleader" value="cancelklxsubjectleader"
                               onclick="$('#subjectleaderclazzlevel').css('display','none');"/>
                    </dd>
                </li>
            </ul>

            <ul id="subjectleaderclazzlevel" class="inline">
                <li>
                    <dt>年级列表</dt>
                    <dd>
                        <#if (seniorTeacher == true)!false>
                            <select id="subject_leader_clazz_level">
                                <option value="11">高一</option>
                                <option value="12">高二</option>
                                <option value="13">高三</option>
                            </select>
                        </#if>
                        <#if (juniorTeacher == true)!false>
                            <select id="subject_leader_clazz_level">
                                <option value="6">6年级</option>
                                <option value="7">7年级</option>
                                <option value="8">8年级</option>
                                <option value="9">9年级</option>
                            </select>
                        </#if>
                    </dd>
                </li>
                <li>
                    <dt>年级</dt>
                    <dd>
                        添加:
                        <input type="radio" name="clazzlevelchoice" value="true" checked="checked"/>
                        删除：
                        <input type="radio" name="clazzlevelchoice" value="false"/>
                    </dd>
                </li>
            </ul>

            <ul class="inline">
                <li>
                    <dt>问题描述</dt>
                    <dd><textarea id="changeKlxSubjectLeaderDesc" cols="35" rows="4"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="change_klxsubjectleader_dialog_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="fault_order_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>选择追踪项-老师</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>用户ID</dt>
                    <dd>${teacherId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dd><input type="checkbox" name="faultType" value="1">用户登录-PC</dd>
                    <dd><input type="checkbox" name="faultType" value="2">用户登录-app</dd>
                    <dd><input type="checkbox" name="faultType" value="3">绑定手机</dd>

                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>追踪备注</dt>
                    <dd><textarea id="fault_order_create_info" cols="35" rows="5"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="dialog_add_fault_order" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>
<#--老师开通包班-->
<div id="subAccountApply_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>开通包班（多学科）</h3>
    </div>
    <div class="modal-body">
        <dl id="successModal" class="dl-horizontal" style="display: none;">
            <ul class="inline">
                <li>
                    <dt>老师ID</dt>
                    <dd>${teacherId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>记录类型</dt>
                    <dd>老师操作</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>开通学科</dt>
                    <dd>
                        <select id="subjectSelect"
                                onchange="showClazzList(this.options[this.options.selectedIndex].value)"></select>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>开通班级</dt>
                    <dd id="CLAZZLIST"></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>所做操作</dt>
                    <dd>开通包班</dd>
                </li>
            </ul>
        </dl>
        <div class="alert alert-error" id="errorModal" style="display: none;">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong id="errorMsg"></strong>
        </div>
    </div>
    <div class="modal-footer">
        <button id="create_subaccount_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<#--取消包班-->
<div id="cancelAccountApply_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>取消包班（多学科）</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>老师ID</dt>
                    <dd>${teacherId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>记录类型</dt>
                    <dd>老师操作</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>取消学科</dt>
                    <dd>
                        <select id="cancelSubjectSelect"
                                onchange=""></select>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt></dt>
                    <dd>请确认：该学科账号下的所有班级已转出。<br>取消后：该学科账号将被置为未认证，且被移出学校</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>所做操作</dt>
                    <dd>取消包班</dd>
                </li>
            </ul>
        </dl>
        <div class="alert alert-error" id="errorModal" style="display: none;">
            <button type="button" class="close" data-dismiss="alert">×</button>
            <strong id="errorMsg"></strong>
        </div>
    </div>
    <div class="modal-footer">
        <button id="cancel_subaccount_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<#--变更主账号弹窗-->
<div id="changToMainCount_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>系统提示</h3>
    </div>
    <div class="modal-body">
        <p>变更后，主账号的手机号、园丁豆、登录密码都将转移至该账号，确认变更？</p>
    </div>
    <div class="modal-footer">
        <button id="sure_change_maincount_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<#--删除账号弹窗-->
<div id="delete_user_dialog" class="modal hide fade">
    <div class="modal-header">
       <h3>注销用户</h3>
    </div>
    <div class="modal-body">
        <p>注销账号后，该老师账号相关数据都会删除并不可恢复!!（如果老师下有班级，请先将名下班级转让），确认注销？</p>
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                <dt>注销原因</dt>
                <dd><textarea id="delete_user_reason" name="deleteReason" cols="35" rows="4"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="btn_delete_user" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>


<#--统一报错弹窗-->
<div id="error_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close after-close-reload" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>系统提示</h3>
    </div>
    <div class="modal-body">
        <p id="errorAlertMsg"></p>
    </div>
    <div class="modal-footer">
        <button class="btn btn-primary after-close-reload" data-dismiss="modal" aria-hidden="true">确 定</button>
    </div>
</div>


<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>
<script type="text/html" id="T:CLAZZ_LIST">
    <% if(clazzList.length > 0) { %>
    <div id="CLAZZ_<%= subject %>" <%if (hide) %> style="display: none;">
    <% for(var i = 0; i < clazzList.length; ++i ) { %>
    <% var item = clazzList[i]; %>
    <li><input data-cid="<%= item.clazzId %>" data-gid="<%= item.groupId %>" type="radio" name="ITEM_<%= subject %>">
        <%= item.clazzName %>
    </li>
    <% } %>
    </div>
    <% } %>
</script>
<script>

    var gradeMap = [];

    function changeSubject() {
        $("#changeSubjectDesc").val("");
        $("#change_subject_dialog").modal("show");
    }

    function setClassManager() {
        // 初始化
        $.get("getclassmanagelist.vpage?teacherId=${teacherId!''}", function (res) {
            if (!res.success) {
                alert("无法获取老师班主任列表");
            } else {
                var tcHtml = '', i = 0;
                for (i = 0; i < res.teacherClass.length; ++i) {
                    var tc = res.teacherClass[i];
                    tcHtml += '<label class="class-manage-list" data-cid="' + tc.classId + '">' + tc.fullName + '<a href="javascript:void(0);" class="js-delClassItemBtn">&times;</a></label>';
                }
                $('#classList').html(tcHtml);
                var chooseHtml = '<select id="grade-select" class="grade-selector" onchange="updateClassSelector()">';
                for (i = 0; i < res.gradeList.length; i++) {
                    var g = res.gradeList[i];
                    chooseHtml += '<option value="' + g.level + '" ';
                    var clist = {};
                    clist.key = g.grade;
                    clist.classList = g.classList;
                    gradeMap.push(clist);
                    if (i == 0) chooseHtml += ' selected ';
                    chooseHtml += '>' + g.grade + '</option>';
                }
                chooseHtml += '</select>';
                chooseHtml += '<select id="class-select" class="class-selector"></select>';
                chooseHtml += '<span class="select-class-btn">＋</span>';
                $('#chooseClass').html(chooseHtml);
                $("#set_classmanager_dialog").modal("show");
                updateClassSelector();
            }
        });
    }

    function setGradeManager() {
        // 初始化
        $.get("getgrademanagelist.vpage?teacherId=${teacherId!''}", function (res) {
            if (!res.success) {
                alert("无法获取老师年级");
            } else {
                var listHtml = '';
                for (var i = 0; i < res.gradeList.length; i++) {
                    var g = res.gradeList[i];
                    listHtml += '<input name="grade_manage" type="checkbox" class="grade_manage_node" value="' + g.value + '"';
                    if (g.selected) listHtml += ' checked ';
                    listHtml += '><span style="margin: 0 5px;">' + g.text + '</span>';
                }
                $('#gradeList').html(listHtml);
                $("#set_grademanager_dialog").modal("show");
            }
        });
    }

    // 设置中学校长
    function setSchoolMaster() {
        $("#set_schoolmaster_dialog").modal("show");
    }

    function openFaultOrderDialog() {
        $("#fault_order_create_info").val();
        $("#fault_order_dialog").modal("show");
    }

    $(function () {
        dater.render();

        var tab = ${selectTab};
        activeTab(tab);

        $("#dialog_add_fault_order").on("click", function () {
            var faultTypes = [];
            $("input[type=checkbox][name=faultType]:checked").each(function (index, domEle) {
                var ckBox = $(domEle);
                faultTypes.push(ckBox.val());
            });

            if (faultTypes.length < 1) {
                alert("请至少选择一个追踪项");
                return;
            }

            var createInfo = $('#fault_order_create_info').val();
            if (!createInfo) {
                alert("请填写追踪备注");
                return;
            }
            if (createInfo.replace(/(^s*)|(s*$)/g, "").length == 0) {
                alert("请填写追踪备注");
                return;
            }

            var queryUrl = "/crm/faultOrder/addRecord.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId: "${teacherId!''}",
                    userName: "${teacherInfoHeaderMap.teacherName!''}",
                    userType: "${teacherInfoHeaderMap.userType!''}",
                    createInfo: $('#fault_order_create_info').val(),
                    faultType: faultTypes.join(",")
                },
                success: function (data) {
                    if (data.success) {

                        $("#fault_order_dialog").modal("hide");
                    } else {
                        alert("添加跟踪记录失败。");
                    }
                }
            });
        });

        $("#change_subject_btn").on("click", function () {
            var subject = $("#subject_type").find("option:selected").val();
            var desc = $("#changeSubjectDesc").val();
            if (desc == '') {
                alert("请输入描述！");
                return false;
            }
            var queryUrl = "../teacher/changesubject.vpage";

            if (window.confirm("修改学科将自动检查老师已布置作业，确认修改？")) {
                if (!checkSpecialSchool()) {
                    return false;
                }
                $.ajax({
                    type: "post",
                    url: queryUrl,

                    data: {
                        teacherId: ${teacherId!''},
                        subject: subject,
                        desc: desc
                    },
                    success: function (data) {
                        if (data.success) {
                            if (data.hasPendingApplications) {
                                if(window.confirm("您的账号 ${teacherId!''} 存在接管班级或转让班级的申请, 继续修改学科将自动取消转让/接管班级\n 是否确认继续修改学科? ")) {
                                    $.ajax({
                                        type: "post",
                                        url: queryUrl,
                                        data: {
                                            teacherId: ${teacherId!''},
                                            subject: subject,
                                            desc: desc,
                                            confirm: true
                                        },
                                        success: function (data) {
                                            if (data.success) {
                                                alert("修改成功！");
                                                window.location.reload();
                                            } else {
                                                alert(data.info);
                                                $("#change_subject_dialog").modal("hide");
                                            }
                                        }
                                    });
                                }
                            } else {
                                alert("修改成功！");
                                window.location.reload();
                            }
                        } else {
                            alert(data.info);
                            $("#change_subject_dialog").modal("hide");
                        }
                    }
                });
            }
        });

        var changeSchool = function (precheck) {
            var postUrl = "changeschool.vpage";
            $.ajax({
                type: "post",
                url: postUrl,
                data: {
                    teacherId: ${teacherId!''},
                    schoolId: $('#schoolId').val(),
                    changeSchoolDesc: $("#changeSchoolDesc").val(),
                    precheck: precheck
                },
                success: function (data) {
                    if (precheck) {
                        if (data.success) {
                            if (window.confirm("确实要将老师(ID:${teacherId!''}及其所在班级转至它校吗?")) {
                                $('#schoolId').val('');
                                $('#changeSchoolDesc').val('');
                                $('#changeSchool_dialog').modal('show');
                            }
                        } else {
                            $("#teacher_clazz_list").html("");
                            var clazzMap = data.clazzMap;
                            for (var key in clazzMap) {
                                var clazzHtml = "<tr><td>" + "<a target='_blank' href='/crm/clazz/groupinfo.vpage?groupId=" + key + "'</a>" + key + "</td>" + "<td>" + clazzMap[key] + "</td></tr>";
                                $("#teacher_clazz_list").append(clazzHtml);
                            }
                            $('#clazzTeacher_dialog').modal('show');
                        }
                    } else {
                        if (data.success) {
                            $("#changeSchool_dialog").modal("hide");
                            window.location.href = 'teacherdetail.vpage?teacherId=${teacherId}'
                        } else {
                            alert(data.info);
                        }
                    }

                }
            });
        };

        var changeSchoolwithoutclazz = function () {
            var postUrl = "changeschoolwithoutclazz.vpage";
            $.ajax({
                type: "post",
                url: postUrl,
                data: {
                    teacherId: ${teacherId!''},
                    schoolIdwithoutclazz: $('#schoolId_withoutclazz').val(),
                    changeSchoolDescwithoutclazz: $("#changeSchoolDesc_withoutclazz").val(),
                },
                success: function (data) {
                    if (data.success) {
                        alert("修改成功!");
                        window.location.href = 'teacherdetail.vpage?teacherId=${teacherId}'
                    } else {
                        alert(data.info);
                    }
                }
            });
        };

        $("#clazzTeacher_btn").on("click", function () {
            $('#clazzTeacher_dialog').modal('hide');
        });

        $(document).on("click", ".cschooltype", function () {
            $('#cschooltype_dialog').modal('show');
        });

        $("#changeschool_withoutclazz_btn").on("click", function () {
            $('#cschooltype_dialog').modal('hide');
            $('#changeSchool_withoutclazz_warning_dialog').modal("show");
        });

        $("#changeSchool_withoutclazz_warning_confirmbtn").on("click", function () {
            $('#changeSchool_withoutclazz_warning_dialog').modal("hide");
            $('#changeSchool_withoutclazz_dialog').modal("show");
        });

        $("#changeSchool_withoutclazz_backbtn").on("click", function () {
            $('#changeSchool_withoutclazz_dialog').modal("hide");
            $('#changeSchool_withoutclazz_warning_dialog').modal("show");
        });

        $("#changeSchool_withoutclazz_warning_backbtn").on("click", function () {
            $('#changeSchool_withoutclazz_warning_dialog').modal("hide");
            $('#cschooltype_dialog').modal('show');
        });

        $("#changeSchool_withoutclazz_changebtn").on("click", function () {
            $('#changeSchool_withoutclazz_dialog').modal('hide');
            $.ajax({
                url: "/crm/school/checkDictSchoolForChangeSchool.vpage",
                type: "post",
                data: {
                    teacherId: ${teacherId!''},
                    targetSchoolId: $('#schoolId_withoutclazz').val()
                },
                success: function (data) {
                    if (data.success) {
                        if (!(data.sourceSchoolId == null && data.targetSchoolId == null)) {//存在重点学校时
                            //确定弹窗
                            var reminderInfo = "提示\n\n";
                            if (data.sourceSchoolId != null) {
                                reminderInfo = reminderInfo + "原学校:" + data.sourceSchoolName + "(" + data.sourceSchoolId + ")是重点学校\n";
                            }
                            if (data.targetSchoolId != null) {
                                reminderInfo = reminderInfo + "修改后的学校:" + data.targetSchoolName + "(" + data.targetSchoolId + ")是重点学校\n"
                            }
                            if (window.confirm(reminderInfo)) {
                                changeSchoolwithoutclazz();
                            } else {
                                $('#schoolId_withoutclazz').val('');
                                $('#changeSchoolDesc_withoutclazz').val('');
                            }
                        } else { //不存在重点学校时
                            changeSchoolwithoutclazz();
                        }
                    } else {
                        alert(data.info);
                    }
                }
            })
        });
        
        $("#changeschool_withclazz_btn").on("click",function () {
//            $('#cschooltype_dialog').modal('hide');
//            changeSchool(true);
            window.open('/crm/teachernew/changeschoolwithclass.vpage?schoolId=${teacherInfoHeaderMap["schoolId"]!}&teacherId=${teacherId!''}')
        });

        $(document).on("click", "#change_school_btn", function () {
            $.ajax({
                url: "/crm/school/checkDictSchoolForChangeSchool.vpage",
                type: "post",
                data: {
                    teacherId: ${teacherId!''},
                    targetSchoolId: $('#schoolId').val()
                },
                success: function (data) {
                    if (data.success) {
                        if (!(data.sourceSchoolId == null && data.targetSchoolId == null)) {//存在重点学校时
                            //确定弹窗
                            var reminderInfo = "提示\n\n";
                            if (data.sourceSchoolId != null) {
                                reminderInfo = reminderInfo + "原学校:" + data.sourceSchoolName + "(" + data.sourceSchoolId + ")是重点学校\n";
                            }
                            if (data.targetSchoolId != null) {
                                reminderInfo = reminderInfo + "修改后的学校:" + data.targetSchoolName + "(" + data.targetSchoolId + ")是重点学校\n"
                            }
                            if (window.confirm(reminderInfo)) {
                                changeSchool(false);
                            } else {
                                $('#schoolId').val('');
                                $('#changeSchoolDesc').val('');
                                $('#changeSchool_dialog').modal('hide');
                            }
                        } else { //不存在重点学校时
                            changeSchool(false);
                        }
                    } else {
                        alert(data.info);
                    }
                }
            })
        });

        $("#login").on("click", function () {
            $("#teacherLoginDesc").val("");
            $("#teacherLogin_dialog").modal("show");
        });

        //登录老师账号
        $("#teacher_login_btn").on("click", function () {
            if ($("#teacherLoginDesc").val() == undefined || $("#teacherLoginDesc").val() == "") {
                alert("备注信息不能为空。");
                return false;
            }
            var queryUrl = "../teacher/teacherlogin.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    teacherId: ${teacherId!''},
                    teacherLoginDesc: $("#teacherLoginDesc").val()
                },
                success: function (data) {
                    if (data.success) {
                        $("#teacherLogin_dialog").modal("hide");
                        var postUrl = data.postUrl;
                        window.open(postUrl);
                    } else {
                        alert("登录老师账号失败。");
                    }
                }
            });
        });

        //重置密码
        $("#pwd_reset").on("click", function () {
            // 用户一个月内修改过密码则出现提示
            $.get('../teacher/checkchangedpassword.vpage', {teacherId: '${(teacherId)!}'}, function (data) {
                if (!data.success || confirm('用户在一个月内已经改过密码，请确认是否继续？')) {
                    $.get('../teachernew/isbindmobile.vpage', {teacherId: '${(teacherId)!}'}, function (data) {
                        if (!data.success) {
                            $("#bindmobileInput").empty();
                        }
                    });
                    $('#passwordExtraDesc').val('');
                    $("#password_dialog_radio button").removeClass("active").eq(0).addClass("active");
                    $("#password_dialog").modal("show");
                }
            });
        });

        $("#activityTypeList ul li").each(function () {
            $(this).find("input").on("click", function () {
                var $this = $(this);
                var checkFlag;
                if ($this.prop("checked")) {
                    checkFlag = 'checked';
                } else {
                    checkFlag = 'unChecked';
                }
                $.ajax({
                    type: "post",
                    url: "../teacher/updateUserActivity.vpage",
                    data: {
                        userId: ${teacherId!!""},
                        key: $this.data("key"),
                        checkFlag: checkFlag
                    },
                    success: function (data) {
                        if (data.success) {

                        } else {
                            alert(data.info);
                        }
                    }
                });
            });
        });


        $("#dialog_edit_teacher_password").on("click", function () {
            var queryUrl = "../teachernew/resetpassword.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId:${teacherId!""},
                    bindMobile: $("#bindmobile").val(),
                    passwordDesc: $("#password_dialog_radio button[class='btn active']").html(),
                    passwordExtraDesc: $('#passwordExtraDesc').val()
                },
                success: function (data) {
                    if (data.success) {
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                }
            });
        });

        //暂停/恢复账号
        $("#pause").on('click', function () {
            var id = ${teacherId!""};
            var pending = $(this).attr("data-id");
            $('#pending_teacher_id').val(id);
            $('#pending_teacher_id').attr('data-id', pending);
            $('#pending_teacher_dialog').modal("show");
        });
		
        $("#pending_teacher_btn").on("click", function () {
            var id = parseInt($('#pending_teacher_id').val());
            var pending = parseInt($('#pending_teacher_id').attr("data-id"));
            var desc = $('#pending_teacher_desc').val();
            if (desc == '') {
                alert('请输入原因！');
                return false;
            }
            $.post("../teacher/pendingteacher.vpage", {
                teacherId: id,
                pending: pending,
                desc: desc
            }, function (data) {
                if (data.success) {
                    window.location.reload();
                } else {
                    alert(data.info);
                }

            });
        });
		// 封禁/解封
		$("#forbid").on('click', function () {
            $('#forbid_teacher_dialog').modal("show");
        });
		
		$("#forbid_teacher_btn").on("click", function () {
            var desc = $('#forbid_teacher_desc').val();
            if (desc == '') {
                alert('请输入原因！');
                return false;
            }
            $.post("/crm/teachernew/forbidteacher.vpage", {
                teacherId: ${teacherId!""},
                desc: desc
            }, function (data) {
                if (data.success) {
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });
		$("#unforbid_teacher_btn").on("click", function () {
            var desc = $('#forbid_teacher_desc').val();
            if (desc == '') {
                alert('请输入原因！');
                return false;
            }
            $.post("/crm/teachernew/unforbidteacher.vpage", {
                teacherId: ${teacherId!""},
                desc: desc
            }, function (data) {
                if (data.success) {
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
        });
        //判定假老师
        $("#faker").on('click', function () {
            var id = ${teacherId!""};
            $('#faker_teacher_id').val(id);
            $('#faker_teacher_dialog').modal("show");
        });

        //解除判定假老师
        $("#undo_faker").on('click', function () {
            var id = ${teacherId!""};
            $('#undo_faker_teacher_id').val(id);
            $('#undo_faker_teacher_dialog').modal("show");
        });

        $("#undo_faker_teacher_btn").on("click", function () {
            var id = parseInt($("#undo_faker_teacher_id").val());
            var desc = $("#undo_faker_teacher_desc").val();
            if (desc == "") {
                alert('请输入原因！');
                return false;
            }
            $.post("undofaketeacher.vpage", {
                teacherId: id,
                desc: desc
            }, function (data) {
                if (data.success) {
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });

        });

        $("#faker_teacher_btn").on("click", function () {
            if (!confirm("老师如存在未处理的换班申请，判假后将取消这些申请。\r\n确认判假？")) {
                return false;
            }
            var id = parseInt($('#faker_teacher_id').val());
            var desc = $('#faker_teacher_desc').val();
            if (desc == '') {
                alert('请输入原因！');
                return false;
            }
            $.post("faketeacher.vpage", {
                teacherId: id,
                desc: desc
            }, function (data) {
                if (data.success) {
                    window.location.reload();
                } else {
                    alert(data.info);
                }

            });
        });

        //更新认证状态
        $("#auth_update").on("click", function () {
            $('#updateAuthenticationExtraDesc').val('');
            $("#update_authentication_dialog_radio button").removeClass("active").eq(0).addClass("active");
            $("#update_authentication_dialog").modal("show");
        });

        $("#dialog_update_teacher_authentication").on("click", function () {

            if ($('#updateAuthenticationExtraDesc').val() == '') {
                alert('请填写附加描述');
                return;
            }
            var queryUrl = "../teacher/cancelteacherauthentication.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId: ${teacherId!""},
                    authenticationState: $("#dialog_authenticationState").val(),
                    authenticationDesc: $("#update_authentication_dialog_radio button[class='btn active']").html(),
                    authenticationExtraDesc: $('#updateAuthenticationExtraDesc').val()
                },
                success: function (data) {
                    if (data.success) {
                        alert("修改认证老师成功！");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                }
            });
        });

        //发放园丁豆
        $("#add_integral").on("click", function () {
            $("#integral").val('');
            $("#comment").val('');
            $("#integral_dialog").modal("show");
        });

        $("#integral_confirm").on("click", function () {
            var queryUrl = "../integral/addintegralhistory.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId: ${teacherId!""},
                    integral: $("#integral").val(),
                    integralType: $("#integralType").val(),
                    comment: $("#comment").val()
                },
                success: function (data) {
                    if (data.success) {
                        window.location.reload();
                    } else {
                        alert("增加园丁豆失败，请检查是否正确填写数量和备注。");
                    }
                    $("#dialog-confirm").modal('hide');
                }
            });
        });

        $('#btnBindMobile').on('click', function () {
            var $dialog = $('#bind_mobile_dialog');
            $dialog.modal('show');
        });
        //绑定手机
        $('#bind_mobile_submit').on('click', function () {
            $.post('bindmobile.vpage',
                    {
                        teacherId: ${teacherId!""},
                        mobile: $('#txtMobile').val(),
                        desc: $('#txtBindMobileDesc').val()
                    }, function (data) {
                        if (data.success) {
                            window.location.reload();
                        } else {
                            alert(data.info);
                        }

                    });
        });

        $(document).on('click', '.modifyteachername', function () {
            $('#modifyteachername_dialog').modal('show');
        });

        $("#modifyteachername_dialog_btn").on('click', function () {
            if (!checkSpecialSchool()) {
                return false;
            }
            var teacherId =  ${teacherId!''};
            var teacherNewName = $('#teacherNewName').val();
            var modifyTeacherNameDesc = $('#modifyTeacherNameDesc').val();
            if (teacherNewName == "") {
                alert("请填写新的姓名");
                return;
            }
            //teacherNewName校验 只能是汉字
            if (teacherNewName.match(/[^\u4e00-\u9fa5]/g)) {
                alert("姓名请使用汉字");
                return;
            }

            if (modifyTeacherNameDesc == "") {
                alert("请填写问题描述");
                return;
            }
            $.post("../user/updateusername.vpage", {
                userId: teacherId,
                userName: teacherNewName,
                nameDesc: modifyTeacherNameDesc
            }, function (data) {
                if (data.success) {
                    alert("老师姓名修改成功");
                    $('#modifyteachername_dialog').modal('hide');
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });

        });

        $(document).on('click', '.changeKlxSubjectLeader', function () {
            $('#change_klxsubjectleader_dialog').modal("show");
        });

        $("#change_klxsubjectleader_dialog_btn").on('click', function () {
            var teacherId = ${teacherId!''};
            var klxsubjectleaderflag = false;
            var obj = document.getElementsByName("klxsubjectleader");
            for (var i = 0; i < obj.length; i++) {
                if (obj[i].checked) {
                    if (obj[i].value == "setklxsubjectleader") {
                        klxsubjectleaderflag = true;
                        break;
                    }
                }
            }
            var desc = $('#changeKlxSubjectLeaderDesc').val();
            if (klxsubjectleaderflag) {
                var clazzLevel = $("#subject_leader_clazz_level").find("option:selected").val();
                var clazzLevelChoice = "true";
                var obj = document.getElementsByName("clazzlevelchoice");
                for (var i = 0; i < obj.length; i++) {
                    if (obj[i].checked) {
                        clazzLevelChoice = obj[i].value;
                    }
                }
                $.post("changeklxsubjectleader.vpage", {
                    teacherId: teacherId,
                    klxSubjectLeaderSetFlag: klxsubjectleaderflag,
                    clazzLevelChoice: clazzLevelChoice,
                    clazzLevel: clazzLevel,
                    desc: desc
                }, function (data) {
                    if (data.success) {
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            } else {
                $.post("changeklxsubjectleader.vpage", {
                    teacherId: teacherId,
                    klxSubjectLeaderSetFlag: klxsubjectleaderflag,
                    desc: desc
                }, function (data) {
                    if (data.success) {
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        $(document).on('click', '#subAccountApply', function () {
            var successModal = $('#successModal');
            var errorModal = $('#errorModal');
            var btn = $('#create_subaccount_btn');
            $.get('teacherclazzapply.vpage', {teacherId: ${teacherId!}}, function (res) {
                if (res.success) {
                    var subjectMap = res.subjectClazz;
                    var subjectHtml = '';
                    var clazzListHtml = '';
                    var flag = false;
                    var subjectName = {"MATH": "数学", "ENGLISH": "英语", "CHINESE": "语文"};
                    for (var subject in subjectMap) {
                        subjectHtml += "<option value='" + subject + "'>" + subjectName[subject] + "</option>";
                        clazzListHtml += template("T:CLAZZ_LIST", {
                            subject: subject,
                            clazzList: subjectMap[subject],
                            hide: flag
                        });
                        flag = true;
                    }
                    $('#subjectSelect').html(subjectHtml);
                    $('#CLAZZLIST').html(clazzListHtml);
                    successModal.show();
                    btn.show();

                    errorModal.hide();
                } else {
                    successModal.hide();
                    btn.hide();

                    $('#errorMsg').html(res.info);
                    errorModal.show();
                }
                $('#subAccountApply_dialog').modal("show");
            });
        });

        $(document).on('click', '#cancelAccountApply', function () {
            var sureModal = $('#cancelAccountApply_dialog');
            var errorModal = $('#error_dialog');
            var errorMsg = $('#errorAlertMsg');
            $.get('findsubteachersubject.vpage', {teacherId: ${teacherId!}}, function (res) {
                if (res.success) {
                    var subjectList = res.subjectList;
                    var subjectHtml = '';
                    var subjectName = {"MATH": "数学", "ENGLISH": "英语", "CHINESE": "语文"};
                    for (var i = 0; i < subjectList.length; i++) {
                        subjectHtml += "<option value='" + subjectList[i] + "'>" + subjectName[subjectList[i]] + "</option>";
                    }
                    $('#cancelSubjectSelect').html(subjectHtml);
                    sureModal.modal("show");
                } else {
                    errorModal.modal("show");
                    errorMsg.text(res.info);
                }
            });
        });

        $(document).on('click', '#changeToMainCount', function () {
            $('#changToMainCount_dialog').modal("show");
        });

        $('#btnShowDialogDelTeacher').on('click', function () {
            $('#del_teacher_dialog').modal("show");
        });

        // 开通包班弹窗确定
        $('#create_subaccount_btn').on('click', function () {
            var subject = $('#subjectSelect').val();
            var selectedClazz = $("input[name=ITEM_" + subject + "]:checked").data('cid');
            $.post('teacherclazzapply.vpage', {
                teacherId: ${teacherId!},
                subject: subject,
                clazzId: selectedClazz
            }, function (res) {
                alert(res.info);
                window.location.reload();
            });
        });

        // 取消包班弹窗确定
        $('#cancel_subaccount_btn').on('click', function () {
            var errorModal = $('#error_dialog');
            var errorMsg = $('#errorAlertMsg');
            $.post('cancelsubteacheraccount.vpage', {
                teacherId: ${teacherId!},
                subject: $('#cancelSubjectSelect').val()
            }, function (res) {
                $('#cancelAccountApply_dialog').modal("hide");
                if (res.success) {
                    errorModal.modal("show");
                    errorMsg.text('成功取消包班');
                    // 监听关闭后reload
                    $('.after-close-reload').on('click', function () {
                        window.location.reload();
                    });
                } else {
                    errorModal.modal("show");
                    errorMsg.text(res.info);
                }
            });
        });

        // 变更为主账号弹窗确定
        $('#sure_change_maincount_btn').on('click', function () {
            var errorModal = $('#error_dialog');
            var errorMsg = $('#errorAlertMsg');
            $.post('changemainteacheraccount.vpage', {teacherId: ${teacherId!}}, function (res) {
                $('#changToMainCount_dialog').modal("hide");
                if (res.success) {
                    errorModal.modal("show");
                    errorMsg.text('成功变更为主账号');
                    // 监听关闭后reload
                    $('.after-close-reload').on('click', function () {
                        window.location.reload();
                    });
                } else {
                    errorModal.modal("show");
                    errorMsg.text(res.info);
                }
            });
        });
        // 设置班主任
        $('#set_classmanager_btn').on('click', function () {
            var nodes = $('.class-manage-list');
            var ids = [];
            if (nodes.length !== 0) {
                $.each(nodes, function (i, item) {
                    ids.push($(item).data('cid'));
                })
            }
            var desc = $("#setClassManageDesc").val();
            if (desc == '') {
                alert("请输入描述！");
                return false;
            }
            if (nodes.length == 0) {
                if (!confirm("是否确认设置班主任")) {
                    return false;
                }
            }
            var data = {
                teacherId: ${teacherId!''},
                classIds: ids.join(","),
                desc: desc
            };

            $.post("setclassmanagelist.vpage", data, function (res) {
                if (res.success) {
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            });
        });

        // 设置年级主任
        $('#set_grademanager_btn').on('click', function () {
            var gradeList = [];
            var gradeNodes = $('.grade_manage_node');

            if (gradeNodes.length !== 0) {
                $.each(gradeNodes, function (i, item) {
                    if ($(item).is(":checked")) gradeList.push($(item).val());
                })
            }
            var desc = $("#setGradeManagerDesc").val();
            if (desc == '') {
                alert("请输入描述！");
                return false;
            }
            if (gradeList.length == 0) {
                if (!confirm("是否确认取消设置年级主任")) {
                    return false;
                }
            }
            var data = {
                teacherId: ${teacherId!''},
                grades: gradeList.join(","),
                desc: desc
            };
            $.post("setgrademanagelist.vpage", data, function (res) {
                if (res.success) {
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            });
        });

        // 设置中学校长
        $('#set_schoolmanager_btn').on('click', function () {
            var desc = $("#setSchoolMasterDesc").val();
            var isSetSchoolManager = $("input[name='schoolMasterRadio']:checked").val();
            if (desc == '') {
                alert("请输入描述！");
                return false;
            }
            var data = {
                schoolId: ${teacherInfoHeaderMap["schoolId"]!0},
                teacherId: ${teacherId!''},
                setSchoolMaster: isSetSchoolManager == 'on' ? true : false,
                desc: desc
            };
            $.post('addSchoolMaster.vpage', data, function (res) {
               if (res.success) {
                   window.location.reload();
               } else {
                   alert(res.info);
               }
            });
        });
        //删除用户
        $("#btn_delete_user").on("click", function () {
            var del_desc = $("#delete_user_reason").val().trim();
            if (del_desc == '') {
                alert("请输入注销原因！");
                return false;
            }
            $.ajax({
                type: "post",
                url: "/crm/user/delete.vpage",
                data: {
                    userId : ${teacherId!''},
                    desc: del_desc,
                },
                success: function (data) {
                    $("#delete_user_dialog").modal("hide");
                    if (data.success) {
                        alert("注销成功！");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                }
            });
        });
    });

    $(document).on('click', '.select-class-btn', function () {
        var nodes = $('.class-manage-list');
        var ids = [];
        if (nodes.length !== 0) {
            $.each(nodes, function (i, item) {
                ids.push($(item).data('cid'));
            })
        }
        var op = $('#class-select').find('option:selected');
        var name = op.data('fullname');
        var id = op.val();
        var exist = false;
        for (var i = 0; i < ids.length; i++) {
            if (ids[i] == id) {
                exist = true;
                break;
            }
        }
        if (exist) {
            return;
        }
        var html = '<label class="class-manage-list" data-cid="' + id + '">' + name + '<a href="javascript:void(0);" class="js-delClassItemBtn">&times;</a></label>';
        $('#classList').append(html);
    });

    $(document).on('click', '.js-delClassItemBtn', function () {
        $(this).parent().remove();
    });

    function updateClassSelector() {
        var op = $('#grade-select').find('option:selected');
        var key = op.html().trim();
        var list = [];
        var i;
        for (i = 0; i < gradeMap.length; ++i) {
            if (gradeMap[i].key == key) {
                list = gradeMap[i].classList;
                break;
            }
        }
        var html = '';
        for (i = 0; i < list.length; ++i) {
            html += '<option value="' + list[i].classId + '" data-fullname="' + list[i].fullName + '">' + list[i].className + '</option>'
        }
        $('#class-select').html(html);
    }

    function activeTab(tab) {
        if (tab == 1 || tab == 0) {
            $("#basic").trigger("click");
        } else if (tab == 2) {
            $("#clazz_student").trigger("click");
        } else if (tab == 3) {
            $("#use_record").trigger("click");
        } else if (tab == 4) {
            $("#word_record").trigger("click");
        } else if (tab == 5) {
            $("#task_record").trigger("click");
        } else if (tab == 6) {
            $("#ref_teacher").trigger("click");
        } else if (tab == 7) {
            $("#reward_record").trigger("click");
        } else if (tab == 8) {
            $("#reward_center").trigger("click");
        } else if (tab == 9) {
            $("#tool_kit").trigger("click");

        }
    }

    $(".button_label").on("click", function (tab) {
        var curTab = $(this).attr("id");
        var alltab = "basic,clazz_student,use_record,word_record,task_record,ref_teacher,reward_record,reward_center,tool_kit";
        var tabList = alltab.split(",");
        var clickObj;
        for (var tabIndex in tabList) {
            var obj = document.getElementById(tabList[tabIndex]);
            if (tabList[tabIndex] == curTab) {
                obj.setAttribute("class", "button_label_select");
                clickObj = obj;
            } else {
                obj.setAttribute("class", "button_label");
            }
        }
    });

    $('[id^="call_"]').on("click", function () {
        var teacherId = $(this).attr("id").substring("call_".length);
        $("#phoneCall").attr("src", "teacherphonecall.vpage?teacherId=" + teacherId);
        $("#phoneCall").attr("src", $("#phoneCall").attr("src"));
//        setTimeout(window.phoneCall.cmdDial_onclick(),5000);
    });

    $(document).on("click", ".personalTab a", function () {
        $(this).addClass("active").siblings().removeClass("active");
    });

    function viewBlackWhiteAttr() {
        $.ajax({
            type: "post",
            url: "../teacher/getUserActivity.vpage",
            data: {
                userId: ${teacherId!""}
            },
            success: function (data) {
                if (data.success) {
                    $(data.bwlist).each(function (index) {
                        $("#bw_" + data.bwlist[index].activityType).attr("checked", "checked");
                    });
                }
            }
        });
        $("#black_white_dialog").modal("show");
    }

    function showClazzList(subject) {
        var showList = $('#CLAZZ_' + subject);
        showList.show();
        showList.siblings().hide();
    }

</script>
</#macro>