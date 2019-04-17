<#import "../../layout_default.ftl" as layout_default>
<#import "../headsearch.ftl" as headsearch>
<#--<#import "studentquery.ftl" as studentQuery>-->
<@layout_default.page page_title="${(studentInfoAdminMapper.student.profile.realname)!}(${(studentInfoAdminMapper.student.id)!})" page_num=3>
<div class="span9">
    <@headsearch.headSearch/>
<#--<@studentQuery.queryPage/>-->
    <legend>学生主页:${(studentInfoAdminMapper.student.profile.realname)!}(${(studentInfoAdminMapper.student.id)!})
        <#if (stuAccountStatus == "NORMAL")!false><b style="color: green;">正常</b></#if>
        <#if (stuAccountStatus == "FORBIDDEN")!false><b style="color: red;">封禁</b></#if>
        <#if (stuAccountStatus == "FREEZING")!false><b style="color: red;">冻结</b></#if>

        <#if studentInfoAdminMapper.schoolLevel?? && (studentInfoAdminMapper.schoolLevel==2 || studentInfoAdminMapper.schoolLevel==4)>
            <a target="_blank" href="http://stat.log.17zuoye.net/nodestat?userId=${studentInfoAdminMapper.student.id!}&date=${.now?string('yyyyMMdd')}&type=junior">
                <span class="icon-book"></span>
            </a>&nbsp;
        <#else>
            <a target="_blank" href="http://stat.log.17zuoye.net/nodestat?userId=${studentInfoAdminMapper.student.id!}&date=${.now?string('yyyyMMdd')}">
                <span class="icon-book"></span>
            </a>&nbsp;
        </#if>
        <input type="button" class="btn btn-primary" value="新建记录" onclick="record.new(${(studentInfoAdminMapper.student.id)!})">&nbsp;
        <input type="button" class="btn btn-primary" value="新建任务" onclick="task.new(${(studentInfoAdminMapper.student.id)!})">&nbsp;
        <input type="button" class="btn btn-primary" value="工作记录历史" onclick="userRecords(${(studentInfoAdminMapper.student.id)!})">
    </legend>
    <div id="user-records" load-content="false" style="display: none">
        <legend>工作记录历史</legend>
        <table class="table table-bordered">
            <tr>
                <th>记录时间</th>
                <th>一级分类</th>
                <th>二级分类</th>
                <th>三级分类</th>
                <th>记录内容</th>
                <th>沟通渠道</th>
                <th>记录人</th>
            </tr>
            <tbody id="user-records-list"></tbody>
        </table>
        <form id="iform" onsubmit="return reloadUserRecords(${(studentInfoAdminMapper.student.id)!});"></form>
        <br>
    </div>
    <ul class="inline">
        <li>
            <button class="btn" onclick="openUserNameDialog()">修改个人信息</button>
        </li>
        <li>
            <button class="btn" onclick="resetPassword()">重置密码</button>
        </li>
        <li>
            <button id="resetPaymentPassword_btn" class="btn">重置支付密码</button>
        </li>
    <#--<li>-->
    <#--<button id="resetPasswordQuestion_btn" class="btn">重置密保问题</button>-->
    <#--</li>-->
        <li>
            <a class="btn" href="../integral/integraldetail.vpage?userId=${studentInfoAdminMapper.student.id!}">修改积分</a>
        </li>
        <li>
            <a class="btn" href="../finance/financedetail.vpage?userId=${studentInfoAdminMapper.student.id!}">增加作业币</a>
        </li>
        <li>
            <a class="btn" href="../afenti/learningplanhistory.vpage?userId=${studentInfoAdminMapper.student.id!}">阿分题相关</a>
        </li>
        <li>
            <a class="btn" href="../walkerelf/elflog.vpage?userId=${studentInfoAdminMapper.student.id!}">拯救精灵王相关</a>
        </li>
        <li>
            <a class="btn" href="studentrewardorder.vpage?studentId=${studentInfoAdminMapper.student.id!}" target="_blank">兑换历史</a>
        </li>
        <li>
            <a class="btn" href="studentnewrewardorder.vpage?studentId=${studentInfoAdminMapper.student.id!}" target="_blank">兑换历史（新）</a>
        </li>
        <li>
            <a class="btn" href="javascript:void(0)" onclick="kickOutOfApp(${studentInfoAdminMapper.student.id!})" target="_blank">App重新登录</a>
        </li>
        <li>
            <a class="btn" href="javascript:void(0)" onclick="changeStudentAccountStatus()" target="_blank">学生账号状态</a>
        </li>
        <li>
            <a class="btn" href="javascript:void(0)" onclick="closeAppGossip()" target="_blank">关闭移动端班级爆料</a>
        </li>
        <li>
            <a class="btn" href="javascript:void(0)" onclick="showRefundPhone()" target="_blank">付退款手机</a>
        </li>
    </ul>
    <ul class="inline">
        <li>
            <button class="btn" onclick="forbidDialog()">
                <#if (stuAccountStatus == "NORMAL")!false>封禁用户<#else>解封用户</#if>
            </button>
        </li>
        <li>
            <button class="btn" onclick="recoverStudentData()">
                恢复极算数据
            </button>
            <a class="btn" target="_blank" href="activity.vpage?userId=${(studentInfoAdminMapper.student.id)!}">趣味活动</a>
            <a class="btn btn-danger" onclick="javascript:$('#delete_user_dialog').modal('show');">注销账号</a>
        </li>
    </ul>
    <ul class="inline" style="line-height: 35px;">
        <li>
            <#if studentInfoAdminMapper.schoolLevel?? && studentInfoAdminMapper.schoolLevel == 2>
                <a class="btn" href="${ms_crm_admin_url}/crm/student/homeworkdetail?userId=${studentInfoAdminMapper.student.id!0}">查看学生作业情况</a>
            <#else>
                <a class="btn" href="studenthomeworkdetail.vpage?userId=${studentInfoAdminMapper.student.id!0}">查看学生作业情况</a>
            </#if>
        </li>
    <#--<li>-->
    <#--<a class="btn" href="/crm/student/useraudionewhomeworkresultdetail.vpage?sid=${studentInfoAdminMapper.student.id!0}">查看学生语音作业</a>-->
    <#--</li>-->

        <li>
            <a class="btn" href="studentbasichomeworkdetail.vpage?userId=${studentInfoAdminMapper.student.id!0}">学生基础必过详情</a>
        </li>
        <li>
            <a class="btn" href="/crm/student/studentfetchvacationhomework.vpage?userId=${studentInfoAdminMapper.student.id!0}">查看假期作业</a>
        </li>
        <li>
            <a class="btn" href="../user/userrecord.vpage?userId=${(studentInfoAdminMapper.student.id)!''}">查看登录历史</a>
        </li>
        <li>
            <a class="btn" href="userfeedback.vpage?userId=${(studentInfoAdminMapper.student.id)!''}">查看反馈历史</a>
        </li>
    <#--<li>-->
    <#--<a class="btn" href="studentinviteelist.vpage?studentId=${(studentInfoAdminMapper.student.id)!''}">查看邀请历史</a>-->
    <#--</li>-->
        <li>
            <input id="btnStudentLogin" type="button" class="btn" value="学生登录"/>
        </li>
        <li>
            <a class="btn" href="../user/wechatnoticelist.vpage?userId=${(studentInfoAdminMapper.student.id)!''}">最新微信消息历史</a>
        </li>
        <li>
            <a class="btn" href="../user/wechatnoticelist.vpage?userId=${(studentInfoAdminMapper.student.id)!''}&isHistory=true">七天内微信消息历史</a>
        </li>
        <li>
            <a class="btn btn-danger" href="http://stat.log.17zuoye.net/nodestat?userId=${studentInfoAdminMapper.student.id!}&date=${.now?string('yyyyMMdd')}">诊断学生</a>
        </li>
        <li>
            <a class="btn" onclick="openFaultOrderDialog()">问题追踪</a>
        </li>

        <#if (ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv() || ProductDevelopment.isProductionEnv())!false>
            <#if (ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv())!false>
                <#assign self_study_url="//ss-admin.test.17zuoye.net/afenti/wealthHistory?subject=ENGLISH&userId=${(studentInfoAdminMapper.student.id)!''}"/>
            </#if>
            <#if (ProductDevelopment.isProductionEnv())!false>
                <#assign self_study_url="//ss-admin2.17zuoye.net/afenti/wealthHistory?subject=ENGLISH&userId=${(studentInfoAdminMapper.student.id)!''}"/>
            </#if>
            <li>
                <a href="${self_study_url}" target="_blank" class="btn">自学应用</a>
            </li>
        </#if>

        <li>
            <a class="btn" href="studentgrindear.vpage?userId=${studentInfoAdminMapper.student.id!0}">查看学生磨耳朵完成情况</a>
        </li>
        <li>
            <a class="btn" href="/crm/userlevel/studentuserlevel.vpage?userId=${studentInfoAdminMapper.student.id!0}">学生等级详情</a>
        </li>
        <li>
            <a class="btn btn-warning" onclick="payFree()">支付限制解除</a>
        </li>
        <li>
            <a class="btn" href="parentreward.vpage?userId=${studentInfoAdminMapper.student.id!0}">家长奖励历史</a>
        </li>
        <li>
            <a class="btn" href="groupcircle.vpage?userId=${studentInfoAdminMapper.student.id!0}">家长通消息和通知列表</a>
        </li>
    </ul>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>ID</th>
            <th>创建时间/最近登录时间</th>
            <th>姓名</th>
            <th>密码</th>
            <th>省市区</th>
            <th>手机</th>
        <#--<th>邮箱</th>-->
            <th>QQ</th>
            <th>地区编码</th>
            <th>学豆总数</th>
            <th>作业币总数</th>
            <th>成长值</th>
            <th>成就数量</th>
            <th>学生端APP</th>
        </tr>
        <tr>
            <td>${studentInfoAdminMapper.student.id!""}</td>
            <td>${studentInfoAdminMapper.student.createTime!""} <br/> ${studentInfoAdminMapper.lastLoginTime!""}  </td>
            <td id="student_name">${studentInfoAdminMapper.student.profile.realname!""}</td>
            <td>
                <span id="real_code"></span>
                <#if studentInfoAdminMapper.student?? && studentInfoAdminMapper.student.id??>
                    <button type="button" id="query_user_password_${studentInfoAdminMapper.student.id!''}" class="btn btn-info">临时密码</button>
                </#if>
            </td>
        <#--<td id="real_code">${studentInfoAdminMapper.student.realCode!""}</td>-->
            <td>${studentInfoAdminMapper.regionName!""}</td>
            <td>
                <#if studentInfoAdminMapper.authentication??>
                    <button type="button" id="query_user_phone_${studentInfoAdminMapper.student.id!''}" class="btn btn-info">查看</button>
                    <button type="button" onclick="changeStudnetPhone()" class="btn btn-info">更改手机</button>
                </#if>
            </td>
        <#--<td>-->
        <#--<button type="button" id="query_user_email_${studentInfoAdminMapper.student.id!''}" class="btn btn-info">查看</button>-->
        <#--</td>-->
            <td>${studentInfoAdminMapper.student.profile.sensitiveQq!""}</td>
            <td>${studentInfoAdminMapper.regionCode!""}</td>
            <td>
                <#if studentInfoAdminMapper.student.id??>
                    <a href="../integral/integraldetail.vpage?userId=${studentInfoAdminMapper.student.id!0}">${studentInfoAdminMapper.integral!""}</a>
                <#else>
                ${studentInfoAdminMapper.integral!""}
                </#if>
            </td>
            <td>
                <#if studentInfoAdminMapper.student.id??>
                    <a href="../finance/financedetail.vpage?userId=${studentInfoAdminMapper.student.id!0}">${studentInfoAdminMapper.balance?string("0.##")}</a>
                <#else>
                ${studentInfoAdminMapper.balance?string("0.##")}
                </#if>
            </td>
            <td>
                <#if growth??>
                    <a href="../growth/detail.vpage?userId=${studentInfoAdminMapper.student.id!0}">${growth}(Lv.${growthLevel!1})</a>
                </#if>
            </td>
            <td>
                <#if achievements??>
                    <a href="../achievement/detail.vpage?userId=${studentInfoAdminMapper.student.id!0}">${achievements}</a>
                </#if>
            </td>
            <td>
                <#if studentInfoAdminMapper.vendorAppsUserRef??>
                    已使用
                    <br/>
                    开始使用时间：
                    <br/>
                ${studentInfoAdminMapper.vendorAppsUserRef.createDatetime!""}
                <#else>
                    未使用
                </#if>
                <p>
                    <button type="button" id="user_use_phone_model_btn" data-sid="${studentInfoAdminMapper.student.id!0}" class="btn btn-info">机型</button>
                </p>
            </td>
        </tr>
    </table>
    <br>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>认证状态</th>
            <th>用户黑名单状态</th>
            <th>支付白名单</th>
            <th>是否开放奖品中心</th>
            <th>是否允许兑换实物</th>
            <th>家长是否关闭奖品中心</th>
        </tr>
        <tr>
            <td>
                <#if studentInfoAdminMapper.studentAuthed>
                    <span style="color: green">已认证</span>
                <#else>
                    <span style="color: red">未认证</span>
                </#if>
            </td>
            <td>
            ${(studentInfoAdminMapper.blackStatus)!""}
            </td>
            <td>
            ${(studentInfoAdminMapper.whiteStatus)!""}
            </td>
            <td>
                ${closeSchool?string("不开放","开放")}
            </td>
            <td>
                ${offlineShiWu?string("不允许","允许")}
            </td>
            <td>
                ${closeIntegral?string("关闭","打开")}
            </td>
        </tr>
    </table>
    <br>
    <div>
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation" class="active">
                <a href="#teacher_clazz" id="teacher_clazz_tab" role="tab" data-toggle="tab" aria-controls="base" aria-expanded="true">老师班组</a>
            </li>
            <li role="presentation" >
                <a href="#parent" id="parent_tab" role="tab" data-toggle="tab" aria-controls="base" aria-expanded="true">家长</a>
            </li>
            <li role="presentation" >
                <a href="#klx" id="klx_tab" role="tab" data-toggle="tab" aria-controls="base" aria-expanded="true">快乐学</a>
            </li>
            <li role="presentation" >
                <a href="#score" id="score_tab" role="tab" data-toggle="tab" aria-controls="base" aria-expanded="true">学分</a>
            </li>
            <li role="presentation" >
                <a href="#order" id="order_tab" role="tab" data-toggle="tab" aria-controls="base" aria-expanded="true">订单</a>
            </li>
            <li role="presentation" >
                <a href="#remark" id="remark_tab" role="tab" data-toggle="tab" aria-controls="base" aria-expanded="true">用户备注</a>
            </li>
        </ul>
        <div class="tab-content" id="tabContent">
            <div class="tab-pane fade active in" role="tabpanel" id="teacher_clazz" aria-labelledby="teacher_clazz_tab">
                <legend>老师列表</legend>
                <table class="table table-hover table-striped table-bordered" style="width: 1000px;">
                    <tr>
                        <th>老师姓名(ID)</th>
                        <th>班级</th>
                        <th>学校</th>
                        <th>学校等级</th>
                        <th>是否仍执教班级</th>
                    </tr>
                    <#if studentInfoAdminMapper?has_content&&studentInfoAdminMapper.teacherInfoList?has_content>
                        <#list studentInfoAdminMapper.teacherInfoList as teacherInfo>
                            <tr>
                                <td>
                                    <ul class="inline">
                                        <li>
                                            <#if teacherInfo.teacherId??>
                                                <a href="../teachernew/teacherdetail.vpage?teacherId=${teacherInfo.teacherId!}" target="_blank">${teacherInfo.teacherName!""}</a>(${teacherInfo.teacherId!})
                                            <#else>
                                            ${teacherInfo.teacherName!""}()
                                            </#if>,
                                        </li>
                                        <li> ${teacherInfo.teacherSubject!},</li>
                                        <li> ${teacherInfo.creator?string("班级创建者", "任课老师")},</li>
                                        <li> <#if teacherInfo.teacherId??>tel:
                                            <button type="button" id="query_user_phone_${teacherInfo.teacherId!''}" class="btn btn-info">查 看</button> </#if></li>
                                    </ul>
                                </td>
                                <td>
                                    <#if teacherInfo.classId??>
                                        <a href="../clazz/groupinfo.vpage?teacherId=${teacherInfo.teacherId!}&clazzId=${teacherInfo.classId!}" target="_blank">
                                        ${teacherInfo.className!""}
                                        </a>(${teacherInfo.classId!""})
                                        (${(teacherInfo.eduSys)!'未知学制'})
                                    <#else>
                                    ${teacherInfo.className!""}()
                                    </#if>
                                </td>
                                <td><a href="../school/schoolhomepage.vpage?schoolId=${studentInfoAdminMapper.schoolId!""}" target="_blank">${studentInfoAdminMapper.schoolName!""}</a>(${studentInfoAdminMapper.schoolId!""})</td>
                                <td>
                                    <#if studentInfoAdminMapper.schoolLevel??>
                                        <#if studentInfoAdminMapper.schoolLevel==1>
                                            小学
                                        <#elseif studentInfoAdminMapper.schoolLevel==2>
                                            中学
                                        <#elseif studentInfoAdminMapper.schoolLevel==4>
                                            高中
                                        <#elseif studentInfoAdminMapper.schoolLevel==5>
                                            学前
                                        </#if>
                                    </#if>
                                </td>
                                <td><#if teacherInfo.isExit!true >否<#else>是</#if></td>
                            </tr>
                        </#list>
                    </#if>
                </table>

                <legend>班组记录</legend>
                <table class="table table-hover table-striped table-bordered">
                    <tr>
                        <th>班组ID</th>
                        <th>更新时间</th>
                        <th>是否有效</th>
                    </tr>
                    <tbody>
                        <#list exClass as record >
                        <tr>
                            <td>
                                <a target="_blank" href="../clazz/groupinfo.vpage?groupId=${record.GROUP_ID}"><#if record.FULL_NAME?has_content>${record.FULL_NAME}<#else>${record.CLASS_NAME}</#if></a>
                                (${record.GROUP_ID})
                            </td>
                            <td>${record.UPDATETIME!}</td>
                            <td>${record.DISABLED?string("无效", "有效")}</td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
            <div class="tab-pane fade" role="tabpanel" id="parent" aria-labelledby="parent_tab">
                <#if studentInfoAdminMapper.cClazzLevel??>C端用户信息年级：${studentInfoAdminMapper.cClazzLevel}</#if>
                <legend>家长列表</legend>
                <table class="table table-hover table-striped table-bordered" style="width: 1000px;">
                    <tr>
                        <th>家长姓名</th>
                        <th>称呼</th>
                        <th>手机绑定</th>
                        <th>关键家长</th>
                        <th>手机</th>
                        <th>解除关联</th>
                        <th>家长APP</th>
                        <th>上月签到</th>
                        <th>本月签到</th>
                    </tr>
                    <#list studentInfoAdminMapper.parentsInfo as parentInfo>
                        <tr>
                            <td>
                                <#if (parentInfo.id)??>
                                    <a href="../parent/parenthomepage.vpage?parentId=${parentInfo.id}">${(parentInfo.realName?html)!}</a>(${parentInfo.id})
                                    <a target="_blank" href="../parent/parenthomepage.vpage?parentId=${parentInfo.id}">
                                        <span class="icon-search"></span>
                                    </a>&nbsp;
                                <#else>
                                ${(parentInfo.realName?html)!}()
                                </#if>
                            </td>
                            <td>${parentInfo.callName!"无身份"}</td>
                            <td>${parentInfo.isAuthenticated?string('是', '否')}</td>
                            <td>${parentInfo.isKeyParent?string('是', '否')}</td>
                            <td style="margin: 0 0 0 0;"><#if (parentInfo.id)??>
                                <button type="button" id="query_user_phone_${parentInfo.id!''}" class="btn btn-info">查 看</button></#if></td>
                            <td>
                                <input type="button" class="btn btn_unbind_student_parent" sid="${studentInfoAdminMapper.student.id!}" pId="${parentInfo.id}" value="解除关联"/>
                            </td>
                            <td>
                                <#if (parentInfo.parentApp)??>
                                    已使用
                                <#else>
                                    未使用
                                </#if>
                            </td>
                            <td>${parentInfo.isLastMonthSigned?string('是', '否')}</td>
                            <td>${parentInfo.isCurrentMonthSigned?string('是', '否')}</td>
                        </tr>
                    </#list>
                </table>
                <br>
                <legend>微信绑定</legend>
                <table class="table table-hover table-striped table-bordered" style="width: 1000px;">
                    <tr>
                        <th>家长ID</th>
                        <th>OPEN_ID</th>
                        <th>绑定时间</th>
                        <th>更新时间</th>
                        <th>绑定方式</th>
                        <th>是否有效</th>
                    </tr>
                    <#list wechats as wechat>
                        <tr>
                            <td>
                            <#--<a href="../parent/parenthomepage.vpage?parentId=${wechat.parentId}" target="_blank">${wechat.parentName}</a>-->
                                    ${wechat.USER_ID}
                            </td>
                            <td>${wechat.OPEN_ID}</td>
                            <td>${wechat.CREATE_DATETIME}</td>
                            <td>${wechat.UPDATE_DATETIME}</td>
                            <td>${wechat.SOURCE}</td>
                            <td>
                                <#if wechat.DISABLED>
                                    无效
                                <#else >
                                    有效
                                </#if>
                            </td>
                        </tr>
                    </#list>
                </table>
            </div>

            <div class="tab-pane fade" role="tabpanel" id="klx" aria-labelledby="klx_tab">
                <legend>关联快乐学账号</legend>
                <table class="table table-hover table-striped table-bordered">

                    <tr>
                        <td>
                            <input type="text" name="linkedKlxId" id="linkedKlxId" placeholder="快乐学ID"/>
                        </td>
                        <td>
                            <input type="button" class="btn btn_link_klxstudent" value="关联17ID"/>
                        </td>
                    </tr>
                </table>

                <legend>关联的快乐学用户列表</legend>
                <table class="table table-hover table-striped table-bordered">
                    <tr>
                        <th>快乐学ID</th>
                        <th>学号</th>
                        <th>填涂号</th>
                        <th>解除关联</th>
                    </tr>
                    <tbody>
                        <#if klxStudents?has_content>
                            <#list klxStudents as klxStudent >
                            <tr>
                                <td>${klxStudent.id!""}</td>
                                <td>${klxStudent.studentNumber!""}</td>
                                <td>${klxStudent.scanNumber!""}</td>
                                <td>
                                    <input type="button" class="btn btn_unbind_student_klxstudent" a17Id="${klxStudent.a17id!}" klxId="${klxStudent.id!}" value="解除关联"/>
                                </td>
                            </tr>
                            </#list>
                        </#if>
                    </tbody>
                </table>
            </div>

            <div class="tab-pane fade" role="tabpanel" id="score" aria-labelledby="score_tab">
                <legend>学分列表
                    <button type="button" id="modifycredit" data-userId="${studentInfoAdminMapper.student.id!''}" class="btn btn-info">修改学分</button>
                </legend>
                <table class="table table-hover table-striped table-bordered">
                    <tr>
                        <th>学生id</th>
                        <th>总学分</th>
                        <th>可用学分</th>
                        <th>创建时间</th>
                        <th>更新时间</th>
                    </tr>
                    <tbody>
                        <#if credit?has_content >
                        <tr>
                            <td>${credit.userId!""}</td>
                            <td>${credit.totalCredit!""}</td>
                            <td>${credit.usableCredit!""}</td>
                            <td>${credit.createDatetime!""}</td>
                            <td>${credit.updateDatetime!""}</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>

                <legend>学分历史列表（默认显示最新20条）
                    <button type="button" id="credit_50num" data-userId="${studentInfoAdminMapper.student.id!''}" class="btn btn-info">50条</button>
                    <button type="button" id="credit_100num" data-userId="${studentInfoAdminMapper.student.id!''}" class="btn btn-info">100条</button>

                </legend>
                <div id = "credit_history">
                    <table class="table table-hover table-striped table-bordered">
                        <tr>
                            <th>学生id</th>
                            <th>增加学分</th>
                            <th>创建前总学分</th>
                            <th>创建可用学分</th>
                            <th>创建后总学分</th>
                            <th>创建后用学分</th>
                            <th>修改时间</th>
                            <th>备注</th>
                        </tr>
                        <tbody>
                            <#if creditHistories?has_content >
                                <#list creditHistories as creditHistory >
                                <tr>
                                    <td>${creditHistory.userId!""}</td>
                                    <td>${creditHistory.amount!""}</td>
                                    <td>${creditHistory.totalCreditBefore!""}</td>
                                    <td>${creditHistory.usableCreditBefore!""}</td>
                                    <td>${creditHistory.totalCreditAfter!""}</td>
                                    <td>${creditHistory.usableCreditAfter!""}</td>
                                    <td>${creditHistory.updateDatetime!""}</td>
                                    <td>${creditHistory.comment!""}</td>
                                </tr>
                                </#list>
                            </#if>
                        </tbody>
                    </table>
                </div>

            </div>

            <div class="tab-pane fade" role="tabpanel" id="order" aria-labelledby="order_tab">
                <legend>订单列表</legend>
                <a href="${requestContext.webAppContextPath}/legacy/afenti/main.vpage?userId=${studentInfoAdminMapper.student.id!}" class="btn">更多订单</a>
                <table class="table table-hover table-striped table-bordered">
                    <tr>
                        <th>订单号</th>
                        <th>创建时间</th>
                        <th>产品服务类型</th>
                        <th>产品名称</th>
                        <th>应付金额</th>
                    </tr>
                    <#if studentInfoAdminMapper.latestOrderList??>
                        <#list studentInfoAdminMapper.latestOrderList as order>
                            <tr>
                                <td>${order.id!''}</td>
                                <td>${order.createDatetime!''}</td>
                            <#--<td>${order.activateDatetime!''}</td>-->
                            <#--<td>${order.serviceEndDatetime!''}</td>-->
                                <td>${order.orderProductServiceType!''}</td>
                                <td>${order.productName!''}</td>
                            <#--<td>${order.payMethod!''}</td>-->
                                <td>${order.orderPrice!''}</td>
                            </tr>
                        </#list>
                    </#if>
                </table>

                <legend>优惠劵列表</legend>
                <table class="table table-hover table-striped table-bordered">
                    <tr>
                        <th>优惠劵ID</th>
                        <th>名称</th>
                        <th>类型</th>
                        <th>折扣力度</th>
                        <th>获取时间</th>
                        <th>有效期</th>
                        <th>状态</th>
                        <th>发放人</th>
                        <th>获取渠道</th>
                        <th>使用时间</th>
                        <th>使用人</th>
                    </tr>
                    <#if studentInfoAdminMapper.couponList??>
                        <#list studentInfoAdminMapper.couponList as coupon>
                            <tr>
                                <td>${coupon.couponId!''}</td>
                                <td>${coupon.couponName!''}</td>
                                <td>${coupon.couponType.getDesc()!''}</td>
                                <td>${coupon.typeValue!''}</td>
                                <td>${coupon.createDate!''}</td>
                                <td>${coupon.effectiveDateStr!''}</td>
                                <td>${coupon.couponUserStatus.getDesc()!''}</td>
                                <td>${coupon.sendUser!''}</td>
                                <td>${coupon.channel!''}</td>
                                <td>${coupon.usedTime!''}</td>
                                <td>${coupon.usedUser!''}</td>
                            </tr>
                        </#list>
                    </#if>
                </table>
            </div>

            <div class="tab-pane fade" role="tabpanel" id="remark" aria-labelledby="remark_tab">
                <ul class="inline">
                    <li>
                        <button class="btn" onclick="addCustomerServiceRecord()">新增备注</button>
                    </li>
                    <li>
                        <button id="hide_record_btn" class="btn" onclick="hideCustomerServiceRecord()">隐藏备注</button>
                    </li>
                </ul>
                <legend>用户备注列表（默认显示最新20条）
                    <button type="button" data-number = "50" data-userId="${studentInfoAdminMapper.student.id!''}" class="user_record_number btn btn-info">50条</button>
                    <button type="button" data-number = "100" data-userId="${studentInfoAdminMapper.student.id!''}" class="user_record_number btn btn-info">100条</button>

                </legend>
                <table id="customer_service_record" class="table table-hover table-striped table-bordered">
                    <tr id="comment_title">
                        <th>用户ID</th>
                        <th>添加人</th>
                        <th>创建时间</th>
                        <th>问题描述</th>
                        <th>所做操作</th>
                        <th>类型</th>
                    </tr>
                    <#list studentInfoAdminMapper.customerServiceRecordList as record >
                        <tr>
                            <td>${record.userId!""}</td>
                            <td>${record.operatorId!""}</td>
                            <td>${record.createTime!""}</td>
                            <td width="150">${record.operationContent!""}</td>
                            <td width="150">${record.comments!""}</td>
                            <td>${record.operationType!""}</td>
                        </tr>
                    </#list>
                </table>
            </div>
        </div>
    </div>


    <!----------------------------dialog----------------------------------------------------------------------------------->
    <div id="username_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>修改个人信息</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>用户ID</dt>
                        <dd>${studentInfoAdminMapper.student.id!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>记录类型</dt>
                        <dd>学生操作</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>新的姓名</dt>
                        <dd><input type="text" name="name" id="name" placeholder="名字中只能使用汉字"/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>性别</dt>
                        <dd>
                            <label style="display: inline-block"><input type="radio" name="gender" value="M" <#if (student.profile.gender) == 'M'>checked="true"</#if>>男</label>
                            <label style="display: inline-block"><input type="radio" name="gender" value="F" <#if (student.profile.gender) == 'F'>checked="true"</#if>>女</label>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>生日</dt>
                        <dd><input type="text" name="birth" id="birth" placeholder=${student.fetchBirthdayFormat("%s/%s/%s")!''}></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>问题描述</dt>
                        <dd><textarea id="nameDesc" name="nameDesc" cols="35" rows="5"></textarea></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>所做操作</dt>
                        <dd>更改用户名字。</dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="dialog_edit_student_name" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

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
                        <dd>${studentInfoAdminMapper.student.id!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>记录类型</dt>
                        <dd>学生操作</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>新的密码</dt>
                        <dd><input type="text" name="password" id="password" readonly="readonly"/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>问题描述</dt>
                        <dd>
                            <div class="btn-group password" data-toggle="buttons-radio">
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
                        <dd>重置用户密码。</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>验证手机号</dt>
                        <dd>
                            <input type="text" id="verifyMobile" name="verifyMobile"/>
                        </dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="dialog_edit_student_password" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

<#--充值用户支付密码 dialog-->
    <div id="resetPaymentPassword_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>重置支付密码</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal inline">
                <dt>用户ID</dt>
                <dd>${studentInfoAdminMapper.student.id!''}</dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>记录类型</dt>
                <dd>学生操作</dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>新的支付密码</dt>
                <dd><label><input type="text" id="paymentPassword_text" value="123456"/></label></dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>问题描述</dt>
                <dd>
                    <div class="btn-group paymentPassword" data-toggle="buttons-radio">
                        <button type="button" class="btn active">TQ在线</button>
                        <button type="button" class="btn">TQ电话</button>
                        <button type="button" class="btn">其他</button>
                    </div>
                </dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>附加描述</dt>
                <dd><label><textarea id="paymentPasswordExtraDesc_textarea" cols="35" rows="2"></textarea></label></dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>所做操作</dt>
                <dd>重置用户支付密码</dd>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="resetPaymentPassword_dialog_btn" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

<#--重置用户密保问题 dialog-->
    <div id="resetPasswordQuestion_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>重置密保问题</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal inline">
                <dt>用户ID</dt>
                <dd>${studentInfoAdminMapper.student.id!''}</dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>记录类型</dt>
                <dd>学生操作</dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>问题描述</dt>
                <dd>
                    <div class="btn-group passwordQuestion" data-toggle="buttons-radio">
                        <button type="button" class="btn active">TQ在线</button>
                        <button type="button" class="btn">TQ电话</button>
                        <button type="button" class="btn">其他</button>
                    </div>
                </dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>附加描述</dt>
                <dd><label><textarea id="passwordQuestionExtraDesc_textarea" cols="35" rows="2"></textarea></label></dd>
            </dl>
            <dl class="dl-horizontal inline">
                <dt>所做操作</dt>
                <dd>重置用户密保问题</dd>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="resetPasswordQuestion_dialog_btn" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="record_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>用户进线记录</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>用户ID</dt>
                        <dd>${studentInfoAdminMapper.student.id!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>问题描述</dt>
                        <dd><textarea id="questionDesc" name="questionDesc" cols="35" rows="3"></textarea></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>所做操作</dt>
                        <dd><textarea id="operation" name="operation" cols="35" rows="3"></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="dialog_edit_student_date" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>
    <!-- student login -->
    <div id="studentLogin_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>登录学生账号</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>学生ID</dt>
                        <dd>${studentInfoAdminMapper.student.id!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>记录类型</dt>
                        <dd>学生操作</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>问题描述</dt>
                        <dd><textarea id="studentLoginDesc" name="studentLoginDesc" cols="35" rows="4"></textarea></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>所做操作</dt>
                        <dd>管理员登录学生账号</dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="student_login_btn" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <#include "../task/common/task_new.ftl">
    <#include "../task/common/record_new.ftl">
</div>


<div id="student_accountstatus_change_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>修改学生账号状态</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>学生ID</dt>
                    <dd>${studentId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>账号状态</dt>
                    <dd>
                        <select id="student_account_type">
                            <option value="NORMAL" <#if (stuAccountStatus == "NORMAL")!false>selected="selected"</#if>>正常</option>
                            <option value="FORBIDDEN" <#if (stuAccountStatus == "FORBIDDEN")!false>selected="selected"</#if>>封禁</option>
                            <option value="FREEZING" <#if (stuAccountStatus == "FREEZING")!false>selected="selected"</#if>>冻结</option>
                        </select>
                    </dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="student_accountstatus_view_change_btn" class="btn btn-primary">修 改</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="student_app_gossip_change_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>班级爆料功能开关</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>学生ID</dt>
                    <dd>${studentId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>大爆料是否开启</dt>
                    <dd>
                        <select id="gossip_status_type">
                            <option value="OPEN" <#if (gossipStatus == "OPEN")!false>selected="selected"</#if>>开启</option>
                            <option value="CLOSE" <#if (gossipStatus == "CLOSE")!false>selected="selected"</#if>>关闭</option>
                        </select>
                    </dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="student_app_gossip_view_change_btn" class="btn btn-primary">修 改</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="show_refund_phone_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>显示退款手机号</h3>
    </div>
    <div class="modal-body">

    </div>
</div>

<div id="forbid_dialog" class="modal hide fade">
    <div class="modal-header">
        <#if (stuAccountStatus == "NORMAL")>
            <h3>封禁用户</h3>
        <#else>
            <h3>解封用户</h3>
        </#if>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                <#if (stuAccountStatus == "NORMAL")!false>
                    <dt>封禁原因</dt>
                <#else>
                    <dt>解封原因</dt>
                </#if>
                <dd><textarea id="forbidReason" name="forbidReason" cols="35" rows="4"></textarea></dd>
                </li>
            </ul>
        </dl>

    </div>

    <div class="modal-footer">
        <#if (stuAccountStatus == "NORMAL")!false>
            <button id="forbid_user_btn" class="btn btn-primary">确 定</button>
        <#else>
            <button id="unforbid_user_btn" class="btn btn-primary">确 定</button>
        </#if>

        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="recoverStudentData_dialog" class="modal hide fade">
    <div class="modal-header">
        <h3>恢复极算学生数据</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>学生ID</dt>
                    <dd>${studentId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>原极算手机号</dt>
                    <dd><input type="text" id="sszPhone" name="sszPhone"/></dd>
                </li>
            </ul>
        </dl>

    </div>

    <div class="modal-footer">
        <button id="recoverStudentData_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="delete_user_dialog" class="modal hide fade">
    <div class="modal-header">
       <h3>注销用户</h3>
    </div>
    <div class="modal-body">
        <p>注销账号后，该用户相关数据(班级信息、作业记录、学豆)都会删除且不可恢复，确认注销？</p>
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

<div id="changeStudentPhone_dialog" class="modal hide fade">
    <div class="modal-header">
        <h3>更改手机</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>学生ID</dt>
                    <dd>${studentId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>手机号</dt>
                    <dd><input type="text" id="changePhone" name="changePhone"/></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>备注</dt>
                    <dd><textarea id="changePhoneReason" name="changePhoneReason" cols="35" rows="4"></textarea></dd>
                </li>
            </ul>
        </dl>

    </div>

    <div class="modal-footer">
        <button id="changeStudentPhone_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="modifycredit_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>修改学生学分</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>学生ID</dt>
                    <dd>${studentId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>增加或减少的学分</dt>
                    <dd>
                        <input id="modifycreditnum" value=""/>
                    </dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>备注</dt>
                    <dd><textarea id="modifycreditdesc" name="modifycreditdesc" cols="200" rows="5"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="modifycredit_dialog_btn" data-userId="${studentInfoAdminMapper.student.id!''}" class="btn btn-primary">修 改</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="fault_order_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>选择追踪项-学生</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>用户ID</dt>
                    <dd>${studentInfoAdminMapper.student.id!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dd><input type="checkbox" name="faultType" value="1">用户登录-PC</dd>
                    <dd><input type="checkbox" name="faultType" value="2">用户登录-app</dd>
                    <dd><input type="checkbox" name="faultType" value="3">绑定手机</dd>
                    <dd><input type="checkbox" name="faultType" value="4">提交作业 <input type="text" id="fault_order_homework_id"/></dd>
                    <dd><input type="checkbox" name="faultType" value="5">作业录音</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>追踪备注</dt>
                    <dd><textarea id="fault_order_create_info"  cols="35" rows="5"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="dialog_add_fault_order" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>
<#--临时密码查看原因-->
<div id="checkPsd_reason_dialog" class="modal hide fade">
    <div class="modal-header">
        <h3>如需继续查看，请填写具体原因</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>学生ID</dt>
                    <dd>${studentId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>备注</dt>
                    <dd><textarea id="checkPsd_reason" name="checkPsd_reason" cols="35" rows="4" placeholder="内容至少为10个字符"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>

    <div class="modal-footer">
        <button id="checkPsd_reason_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>
<#--手机查看原因-->
<div id="checkPhone_reason_dialog" class="modal hide fade">
    <div class="modal-header">
        <h3>如需继续查看，请填写具体原因</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>学生ID</dt>
                    <dd>${studentId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>备注</dt>
                    <dd><textarea id="checkPhone_reason" name="checkPhone_reason" cols="35" rows="4" placeholder="内容至少为10个字符"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>

    <div class="modal-footer">
        <button id="checkPhone_reason_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>
<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>
<script>
    function openUserNameDialog() {
        $('#name').val('');
        $('#nameDesc').val('');
        $("#username_dialog").modal("show");
    }

    function openFaultOrderDialog() {
        $("#fault_order_homework_id").val();
        $("#fault_order_create_info").val();
        $("#fault_order_dialog").modal("show");
    }

    function resetPassword() {
        var studentId = '${(studentInfoAdminMapper.student.id)!}';
        // 用户已经绑定手机或密保问题则出现提示
        $.get('checkbindingphoneorsetsq.vpage', {studentId: studentId}, function (data) {
            if (!data.success || confirm(data.info)) {
                $("#verifyMobile").val($.trim(data.mobile));
                // 用户15天内修改过密码则出现提示
                $.get('checkchangedpassword.vpage', {studentId: studentId}, function (data) {
                    if (!data.success || confirm('用户在15天内已经改过密码，请确认是否继续？')) {
                        // 用户有付费产品则出现提示
                        $.get('haspaidproduct.vpage', {studentId: studentId}, function (data) {
                            if (!data.success || confirm('用户有付费产品，请确认是否继续？')) {
                                $("#password").val(randomPassword());
                                $('#passwordExtraDesc').val('');
                                $("div[class='btn-group password'] button").removeClass("active").eq(0).addClass("active");
                                $("#password_dialog").modal("show");
                            }
                        });
                    }
                });
            }
        });
    }

    function payFree() {
        var studentId = '${(studentInfoAdminMapper.student.id)!}';

        if(confirm("解除当天学生端支付将不受任何限制，是否确定？")){
            $.get('setstudentpayfree.vpage', {studentId: studentId}, function (data) {
                if(data.success){
                    alert("解除成功");
                }
            });
        }
    }

    function randomPassword() {
        var password = "";
        for (var i = 0; i < 6; i++) {
            password += Math.floor(Math.random() * 10);
        }
        return password;
    }

    function addCustomerServiceRecord() {
        $("#questionDesc").val('');
        $("#operation").val('');
        $("#record_dialog").modal("show");
    }

    function hideCustomerServiceRecord() {
        $("#customer_service_record").toggle(function () {
            var $target = $("#hide_record_btn");
            switch ($target.html()) {
                case "隐藏备注":
                    $target.html("显示备注");
                    break;
                case "显示备注":
                    $target.html("隐藏备注");
                    break;
            }
        });
    }

    function appendNewRecord(data) {
        var record = "<tr>" +
                "<td>" + data.customerServiceRecord.userId + "</td>" +
                "<td>" + data.customerServiceRecord.operatorId + "</td>" +
                "<td>" + data.createTime + "</td> " +
                "<td>" + data.customerServiceRecord.operationContent + "</td> " +
                "<td>" + data.customerServiceRecord.comments + "</td> " +
                "<td>" + data.customerServiceRecord.operationType + "</td>  " +
                "</tr> ";
        $("#comment_title").after(record);
    }

    $(function () {
        dater.render();

        $('[id^="query_user_password_"]').on('click', function () {
            var item = $(this);
            var id = parseInt(item.attr("id").substr("query_user_password_".length));
            $("#real_code").text("");
            $.get("../user/temppassword.vpage", {
                userId: id
            }, function (data) {
                // 查看临时密码时增加了次数限制
                if(data.success){
                    $("#real_code").text(data.password);
                }else if(data.popup){
                    $("#checkPsd_reason_dialog").modal("show");
                    $("#checkPsd_reason").val('');
                }else {
                    alert('访问次数过多');
                }
            });
        });

        $("#dialog_edit_student_name").on("click", function () {
            var queryUrl = "../user/updateusername.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId: ${studentInfoAdminMapper.student.id!""},
                    userName: $("#name").val(),
                    nameDesc: $('#nameDesc').val(),
                    gender: $("input[name='gender']:checked").val(),
                    birth: $("#birth").val()
                },
                success: function (data) {
                    if (data.success) {
                        appendNewRecord(data);
                        $("#student_name").html(data.userName);
                        $("#username_dialog").modal("hide");
                    } else {
                        alert("修改失败，请填写问题描述，并检查名字是否符合规范。");
                    }
                }
            });
        });

        $("#dialog_add_fault_order").on("click", function () {
            var faultTypes = [];
            $("input[type=checkbox][name=faultType]:checked").each(function (index, domEle) {
                var ckBox = $(domEle);
                faultTypes.push(ckBox.val());
            });

            if(faultTypes.length < 1){
                alert("请至少选择一个追踪项");
                return;
            }

            var createInfo  = $('#fault_order_create_info').val();
            if(!createInfo){
                alert("请填写追踪备注");
                return;
            }
            if(createInfo.replace(/(^s*)|(s*$)/g, "").length ==0){
                alert("请填写追踪备注");
                return;
            }

            var queryUrl = "/crm/faultOrder/addRecord.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId: "${studentInfoAdminMapper.student.id!''}",
                    userName: "${studentInfoAdminMapper.student.profile.realname!''}",
                    userType: "${studentInfoAdminMapper.student.userType!''}",
                    createInfo:$('#fault_order_create_info').val(),
                    homeworkId:$('#fault_order_homework_id').val(),
                    faultType:faultTypes.join(",")
                },
                success: function (data) {
                    if (data.success) {
                        $("#fault_order_homework_id").val();
                        $("#fault_order_create_info").val();
                        $("#fault_order_dialog").modal("hide");
                    } else {
                        alert("添加跟踪记录失败。");
                    }
                }
            });
        });

        var cellRegCheck = /^1\d{10}$/;
        $("#dialog_edit_student_password").on("click", function () {
            var mobile = $.trim($("#verifyMobile").val());
            if (mobile != "" && !cellRegCheck.test(mobile)) {
                alert("手机号输入错误，请输入11位手机号码。");
                return;
            }
            $.ajax({
                type: "post",
                url: "/crm/user/mobileusers.vpage",
                data: {mobile: mobile},
                success: function (data) {
                    var tip = "手机号【" + mobile + "】当前无用户绑定。\n";
                    if (jsonSize(data) > 0) {
                        tip = "手机号【" + mobile + "】当前被以下用户绑定：\n";
                        var student = data["STUDENT"];
                        if (student != null) {
                            tip += student.profile.realname + ", " + student.id + "【学生】\n";
                        }
                        var parent = data["PARENT"];
                        if (parent != null) {
                            tip += parent.profile.realname + ", " + parent.id + "【家长】\n";
                        }
                        var teacher = data["TEACHER"];
                        if (teacher != null) {
                            tip += teacher.profile.realname + ", " + teacher.id + "【老师】\n";
                        }
                    }
                    tip += "\n确定要继续吗？";
                    if (window.confirm(tip)) {
                        $.ajax({
                            type: "post",
                            url: "/crm/user/resetpassword.vpage",
                            data: {
                                userId: ${studentInfoAdminMapper.student.id!""},
                                password: $("#password").val(),
                                passwordDesc: $("div[class='btn-group password'] button[class='btn active']").html(),
                                passwordExtraDesc: $('#passwordExtraDesc').val(),
                                verifyMobile: $('#verifyMobile').val()
                            },
                            success: function (data) {
                                if (data.success) {
                                    window.location.reload();
                                } else {
                                    alert(data.info);
                                }
                            }
                        });
                    }
                }
            });
        });

        function jsonSize(json) {
            if (json == null) {
                return 0;
            }
            var size = 0;
            for (var key in json) {
                size++;
            }
            return size;
        }

        $("#dialog_edit_student_date").on("click", function () {
            var queryUrl = "../user/addcustomerrecord.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId: ${studentInfoAdminMapper.student.id!''},
                    questionDesc: $("#questionDesc").val(),
                    operation: $("#operation").val()
                },
                success: function (data) {
                    if (data.success) {
                        appendNewRecord(data);
                        $("#record_dialog").modal("hide");
                    } else {
                        alert("增加日志失败。");
                    }
                }
            });
        });

    <#--重置用户支付密码-->
        $('#resetPaymentPassword_btn').click(function () {
            $("#paymentPassword_text").val("123456");
            $('#paymentPasswordExtraDesc_textarea').val('');
            $("div[class='btn-group paymentPassword'] button").removeClass("active").eq(0).addClass("active");
            $("#resetPaymentPassword_dialog").modal("show");
        });
        $('#resetPaymentPassword_dialog_btn').click(function () {
            var queryUrl = "../user/resetpaymentpassword.vpage";
            var postData = {
                userId: ${studentInfoAdminMapper.student.id!},
                paymentPassword: $("#paymentPassword_text").val(),
                passwordDesc: $("div[class='btn-group paymentPassword'] button[class='btn active']").html(),
                passwordExtraDesc: $('#paymentPasswordExtraDesc_textarea').val()
            };
            $.post(queryUrl, postData, function (data) {
                alert(data.info);
                if (data.success) {
                    appendNewRecord(data);
                    $("#resetPaymentPassword_dialog").modal("hide");
                }
            });
        });

    <#--重置用户密保问题-->
    <#--$('#resetPasswordQuestion_btn').click(function () {-->
    <#--$('#passwordQuestionExtraDesc_textarea').val('');-->
    <#--$("div[class='btn-group passwordQuestion'] button").removeClass("active").eq(0).addClass("active");-->
    <#--$("#resetPasswordQuestion_dialog").modal("show");-->
    <#--});-->
    <#--$('#resetPasswordQuestion_dialog_btn').click(function () {-->
    <#--var queryUrl = "../user/resetpasswordquestion.vpage";-->
    <#--var postData = {-->
    <#--userId: ${studentInfoAdminMapper.student.id!},-->
    <#--passwordDesc: $("div[class='btn-group passwordQuestion'] button[class='btn active']").html(),-->
    <#--passwordExtraDesc: $('#passwordQuestionExtraDesc_textarea').val()-->
    <#--};-->
    <#--$.post(queryUrl, postData, function (data) {-->
    <#--alert(data.info);-->
    <#--if (data.success) {-->
    <#--appendNewRecord(data);-->
    <#--$("#resetPasswordQuestion_dialog").modal("hide");-->
    <#--}-->
    <#--});-->
    <#--});-->

    <#-- 学生账号状态-->
        $("#student_accountstatus_view_change_btn").on("click", function () {
            var status = $("#student_account_type").find("option:selected").val();
            $.ajax({
                type: "post",
                url: "updateStudentAccountStatus.vpage",
                data: {
                    studentId : ${studentId!""},
                    status : status
                },
                success: function (data) {
                    if (data.success) {
                        alert("修改成功！");
                        window.location.reload();
                    } else {
                        alert(data.info);
                        $("#student_accountstatus_change_dialog").modal("hide");
                    }
                }
            });
        });

        $("#forbid_user_btn").on("click", function () {
            $.ajax({
                type: "post",
                url: "forbidstudent.vpage",
                data: {
                    studentId : ${studentId!""},
                    forbidReason: $("#forbidReason").val(),
                },
                success: function (data) {
                    $("#forbid_dialog").modal("hide");
                    if (data.success) {
                        alert("封禁成功！");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                }
            });
        });

        $("#unforbid_user_btn").on("click", function () {
            $.ajax({
                type: "post",
                url: "unforbidstudent.vpage",
                data: {
                    studentId : ${studentId!""},
                    forbidReason: $("#forbidReason").val(),
                },
                success: function (data) {
                    $("#forbid_dialog").modal("hide");
                    if (data.success) {
                        alert("解封成功！");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                }
            });
        });
        $("#recoverStudentData_btn").on("click", function () {
            $.ajax({
                type: "post",
                url: "/crm/shensz/recoverstudentdata.vpage",
                data: {
                    userId : ${studentId!""},
                    phone: $("#sszPhone").val(),
                },
                success: function (data) {
                    $("#recoverStudentData_dialog").modal("hide");
                    if (data.success) {
                        alert("恢复成功");
                    } else {
                        alert(data.info);
                    }
                }
            });
        });

        $("#changeStudentPhone_btn").on("click", function () {
            $.ajax({
                type: "post",
                url: "/crm/account/changeStudentPhone.vpage",
                data: {
                    studentId : ${studentId!""},
                    changePhone: $("#changePhone").val(),
                    changePhoneReason: $("#changePhoneReason").val(),
                },
                success: function (data) {
                    if (data.success) {
                        $("#changeStudentPhone_dialog").modal("hide");
                        alert("修改成功");
                    } else {
                        alert(data.info);
                    }
                }
            });
        });
    <#-- 关闭移动端大爆料-->
        $("#student_app_gossip_view_change_btn").on("click", function () {
            var status = $("#gossip_status_type").find("option:selected").val();
            $.ajax({
                type: "post",
                url: "updateAppGossipStatus.vpage",
                data: {
                    studentId : ${studentId!""},
                    status : status
                },
                success: function (data) {
                    if (data.success) {
                        alert("修改成功！");
                        window.location.reload();
                    } else {
                        alert(data.info);
                        $("#student_app_gossip_change_dialog").modal("hide");
                    }
                }
            });
        });
        $("#checkPsd_reason_btn").on("click", function () {
            var desc = $("#checkPsd_reason").val();
            if (desc.length < 10) {
                alert('备注内容至少输入10个字符哦');
                return;
            }
            $.get("../user/temppassword.vpage", {
                userId: ${studentId!""},
                desc: desc
            }, function (data) {
                if (!data.success) {
                    alert('访问次数过多');
                    return;
                }
                $("#checkPsd_reason_dialog").modal("hide");
                $("#real_code").text(data.password);
            });
        });

        $("#modifycredit").on("click",function () {
            $("#modifycredit_dialog").modal("show");
        });

        $("#credit_50num").on("click",function () {
            var userId = $(this).attr("data-userId");
            $.ajax({
                type: "get",
                url: "/crm/student/studenthistorycredit.vpage",
                data: {
                    studentId:userId,
                    number:50
                },
                success: function (data) {
                    $("#credit_history").html(data)
                }
            });
        });

        $("#credit_100num").on("click",function () {
            var userId = $(this).attr("data-userId");
            $.ajax({
                type: "get",
                url: "/crm/student/studenthistorycredit.vpage",
                data: {
                    studentId:userId,
                    number:100
                },
                success: function (data) {
                    $("#credit_history").html(data)
                }
            });
        });

        $(".user_record_number").on("click",function () {
            var userId = $(this).attr("data-userId");
            var number = $(this).attr("data-number");
            $.ajax({
                type: "get",
                url: "/crm/student/studentrecords.vpage",
                data: {
                    studentId:userId,
                    number:number
                },
                success: function (data) {
                    $("#customer_service_record").html(data)
                }
            });
        });

        $("#modifycredit_dialog_btn").on("click",function () {
            var userId = $(this).attr("data-userId");
            var amount = $("#modifycreditnum").val();
            var comment = $("#modifycreditdesc").val();

            if(isBlank(comment)){
                alert("请填写备注");
                return false;
            }

            $.post('/crm/user/modifycredit.vpage',{userId:userId,amount:amount,comment:comment},function (data) {
                if(data.success){
                    window.location.reload();
                }else{
                    alert(data.info);
                }
            });
        });

        function isBlank(str){
            return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
        };

        //获取机型
        $("#user_use_phone_model_btn").on('click', function () {
            var self = $(this);
            if (self.hasClass('disabled')) {
                return false;
            }
            self.addClass("disabled");
            var userId = self.data('sid');
            $.post('http://stat.log.17zuoye.net/nodestat/crm/logDetail', {userId: userId}, function (data) {
                if (data.success) {
                    var text = "<span title='model (native_version/system_version)'>"+data.content.model + ' (' + data.content.native_version + '/' + data.content.system_version + ')'+"</span>";

                    if(!data.content.model){
                        text = '<span style="color: #fa7252">暂无机型数据</span>';
                    }

                    if(data.content.moreModel){
                        text += '<span class="icon-exclamation-sign" title="多机型登录"></span>';
                    }
                    self.closest('p').html(text);
                } else {
                    alert(data.info);
                }
                self.removeClass("disabled");
            }).fail(function () {
                self.removeClass("disabled");
            });
        });
    });



    //解绑学生与家长关联
    $('.btn_unbind_student_parent').on('click', function () {
        if (!confirm('确定要解除学生与家长的关联关系吗？')) {
            return false;
        }
        var studentId = $(this).attr('sId');
        var parentId = $(this).attr('pId');
        $.post('unbindparent.vpage', {
            studentId: studentId,
            parentId: parentId
        }, function (data) {
            if (!data.success) {
                alert(data.info);
            } else {
                window.location.reload();
            }
        });
    });

    // 关联17ID和klxID
    $('.btn_link_klxstudent').on('click', function () {
        $.post('linkklxstudent.vpage ', {
            linkedKlxId: $('#linkedKlxId').val(),
            studentId : ${studentInfoAdminMapper.student.id!}
        }, function (data) {
            if (!data.success) {
                alert(data.info);
            } else {
                window.location.reload();
            }
        })
    });

    //解绑17作业学生和快乐学学生关联
    $('.btn_unbind_student_klxstudent').on('click', function () {
        if (!confirm('确定给该学生解除合并？解除后17id和klxid将断开关联')) {
            return false;
        }
        var a17Id = $(this).attr('a17Id');
        var klxId = $(this).attr('klxId');
        $.post('unbindklxstudent.vpage', {
            a17Id: a17Id,
            klxId: klxId
        }, function (data) {
            if (!data.success) {
                alert(data.info);
            } else {
                window.location.reload();
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
                userId : ${studentId!""},
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

    $('#btnStudentLogin').on('click', function () {
        var $dialog = $('#studentLogin_dialog');
        $dialog.modal('show');
    });
    $("#student_login_btn").on("click", function () {
        $.ajax({
            type: "post",
            url: 'studentlogin.vpage',
            data: {
                studentId: ${studentInfoAdminMapper.student.id!},
                studentLoginDesc: $("#studentLoginDesc").val()
            },
            success: function (data) {
                if (data.success) {
                    $("#teacherLogin_dialog").modal("hide");
                    var postUrl = data.postUrl;
                    window.open(postUrl);
                } else {
                    alert("登录学生账号失败。");
                }
            }
        });
        var $dialog = $('#studentLogin_dialog');
        $dialog.modal('hide');
    });

    function reloadUserRecords(userId) {
        userRecords(userId);
        return false;
    }

    function kickOutOfApp(userId) {
        if (!confirm("确定踢出App重新登录?")){
            return;
        }

        $.ajax({
            url: "/crm/student/kickOutOfApp.vpage",
            type: "POST",
            async: false,
            data: {
                "userId": userId
            },
            success: function (data) {
                if(data.success)
                    alert("操作成功");
                else
                    alert(data.info);
            }
        });
    }

    function changeStudentAccountStatus() {

        /*
                var select = document.getElementById("student_account_type");
                for(var i=0 ;i<select.options.length;i++){
                    alert(select.options[i].value);
                }
        */

        $("#student_accountstatus_change_dialog").modal("show");
    }

    function closeAppGossip() {
        $("#student_app_gossip_change_dialog").modal("show");
    }

    function showRefundPhone(){
        $("#show_refund_phone_dialog").modal("show");

        $.ajax({
            url:"/crm/student/getRefundPhone.vpage",
            data:{"stuId":${studentInfoAdminMapper.student.id!0}},
            success:function(data){
                $("#show_refund_phone_dialog .modal-body").html("手机号为:" + data.phone);
            }
        })
    }

    function forbidDialog(){
        $("#forbid_dialog").modal("show");
    }

    function recoverStudentData(){
        $("#recoverStudentData_dialog").modal("show");
    }

    function changeStudnetPhone(){
        $("#changeStudentPhone_dialog").modal("show");
    }

    function userRecords(userId) {
        $.ajax({
            url: "/crm/task/user_all_record.vpage",
            type: "POST",
            async: false,
            data: {
                "userId": userId
            },
            success: function (data) {
                $("#user-records-list").empty();
                if (data) {
                    for (var i in data) {
                        var record = data[i];
                        var row = "<tr>";
                        row += "<td>" + record.niceCreateTime + "</td>";
                        row += "<td>" + record.firstCategory + "</td>";
                        row += "<td>" + record.secondCategory + "</td>";
                        row += "<td>" + record.thirdCategory + "</td>";
                        row += "<td>" + record.content + "</td>";
                        row += "<td>" + record.contactType + "</td>";
                        row += "<td>" + record.recorderName + "</td>";
                        row += "</tr>";
                        $("#user-records-list").append(row);
                    }
                }
            }
        });
        $("#user-records").show("fast");
    }
</script>
</@layout_default.page>