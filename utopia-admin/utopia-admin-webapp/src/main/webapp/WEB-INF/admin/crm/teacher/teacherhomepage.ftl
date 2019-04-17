<#import "../../layout_default.ftl" as layout_default/>
<#import "../headsearch.ftl" as headsearch>
<#--<#import "teacherquery.ftl" as queryPage/>-->
<#import "teacherclazzlistchip.ftl" as clazzListChipPage/>
<#macro checkAuth systemName relativeUrl method='get'>
<#--fixme-->
<!--for example: systemName="crm" relativeUrl="student/studenthomepage[.vpage]" method="post"-->
    <#if method='get'>
        <#assign roleName='getAccessor'/>
    <#else>
        <#assign roleName='postAccessor'/>
    </#if>
    <#if requestContext.getCurrentAdminUser().checkAuthByRoleNames(systemName, relativeUrl, [roleName])>
        <#nested/>
    </#if>
</#macro>
<@layout_default.page page_title="${teacherInfoAdminMapper.teacher.profile.realname!}(${teacherInfoAdminMapper.teacher.id!})" page_num=3>
<style>
    ul.inline > li{
        margin-top:5px;
    }
</style>
<div class="span9">
    <@headsearch.headSearch/>
    <#--<@queryPage.queryConditons/>-->
    <legend>老师主页:${teacherInfoAdminMapper.teacher.profile.realname!}(${teacherInfoAdminMapper.teacher.id!}) <#if !requestContext.getCurrentAdminUser().isCsosUser()>
        <#if teacherInfoAdminMapper.schoolLevel?? && (teacherInfoAdminMapper.schoolLevel==2 || teacherInfoAdminMapper.schoolLevel==4)>
            <a target="_blank" href="http://stat.log.17zuoye.net/nodestat?userId=${teacherInfoAdminMapper.teacher.id!}&date=${.now?string('yyyyMMdd')}&colname=web_teacher_logs&type=junior"><span class="icon-book"></span></a>
        <#else>
        <a target="_blank" href="http://stat.log.17zuoye.net/nodestat?userId=${teacherInfoAdminMapper.teacher.id!}&date=${.now?string('yyyyMMdd')}&colname=web_teacher_logs"><span class="icon-book"></span></a>
        </#if>
    </#if>
        <#if teacherInfoAdminMapper.isCheat!false>
            <span style="color: red;">作弊老师</span>
        </#if>
    </legend>
    <#--button-->
    <ul class="inline">
        <#if !requestContext.getCurrentAdminUser().isCsosUser()>
        <li>
            <button class="btn" onclick="resetPassword()">重置密码</button>
        </li>
        <li>
            <button class="btn cschool" >修改学校</button>
        </li>
        </#if>
        <li>
            <button class="btn" id="checkTeacherAuthentication">查看认证条件</button>
        </li>
        <li>
            <#--<button class="btn" onclick="quizs()">查看旧测验历史</button>-->

            <a href="/crm/teachernew/newexamclazz.vpage?teacherId=${(teacherInfoAdminMapper.teacher.id)!}" class="btn">统考查询</a>

<#if teacherInfoAdminMapper.schoolLevel?? && (teacherInfoAdminMapper.schoolLevel==2)>
    <a href="${ms_crm_admin_url}/crm/teacher/homeworkPlanList?teacherId=${(teacherInfoAdminMapper.teacher.id)!}" class="btn">寒假作业查询</a>
<#else>
    <a href="/crm/vacation/homework/report/list.vpage?teacherId=${(teacherInfoAdminMapper.teacher.id)!}" class="btn">寒假作业查询</a>
</#if>

        </li>
        <#if !requestContext.getCurrentAdminUser().isCsosUser()>
        <#if teacherInfoAdminMapper.verifyState != '已认证'>
            <@checkAuth systemName='crm' relativeUrl='teacher/addteacherauthentication'>
                <li>
                    <button class="btn" onclick="updateAuthentication()">认证老师</button>
                </li>
                <li>
                    <button class="btn" onclick="cancelAuthentication()">取消认证老师</button>
                </li>
            </@checkAuth>
        </#if>
        <li>
            <a href="teacherchangeclazzhistory.vpage?teacherId=${(teacherInfoAdminMapper.teacher.id)!}" class="btn">查询换班历史</a>
        </li>
        <li>
            <button class="btn" onclick="teacherLogin()">登录老师账号</button>
        </li>
        <li>
            <button class="btn" onclick="changeSubject()">修改学科</button>
        </li>
        <li>
            <a class="btn" href="../user/wechatnoticelist.vpage?userId=${teacherInfoAdminMapper.teacher.id!''}">微信消息历史</a>
        </li>
        </#if>
    </ul>
    <#if !requestContext.getCurrentAdminUser().isCsosUser()>
    <ul class="inline">
        <li>
            <a href="studentandparentbind.vpage?teacherId=${teacherInfoAdminMapper.teacher.id!}" class="btn">查看学生和家长手机绑定</a>
        </li>
        <li>
            <button class="btn" onclick="viewBlackWhiteAttr()">特殊属性</button>
        </li>
        <li>
            <a href="../user/userrecord.vpage?userId=${teacherInfoAdminMapper.teacher.id!}" class="btn">查看登录历史</a>
        </li>
        <li>
            <a class="btn" href="teacherrewardorder.vpage?teacherId=${teacherInfoAdminMapper.teacher.id!}">兑换历史</a>
        </li>
        <li>
            <a class="btn" href="teachernewrewardorder.vpage?teacherId=${teacherInfoAdminMapper.teacher.id!}" target="_blank">兑换历史（新）</a>
        </li>
        <li>
            <a class="btn" href="teacheractivehistory.vpage?teacherId=${teacherInfoAdminMapper.teacher.id!}" target="_blank">唤醒历史</a>
        </li>
        <li>
            <a class="btn" href="../student/userfeedback.vpage?userId=${teacherInfoAdminMapper.teacher.id!}">反馈历史</a>
        </li>
        <li>
            <a class="btn" href="javascript:void(0)" onclick="kickOutOfApp(${teacherInfoAdminMapper.teacher.id!})">App重新登录</a>
        </li>
        <li>
            <a class="btn" href="javascript:void(0)" onclick="openFaultOrderDialog()">问题追踪</a>
        </li>
    </ul>
    </#if>




    <#--用户信息-->
    <div>
        <table class="table table-hover table-striped table-bordered">
            <tr>
                <th>ID</th>
                <th>姓名</th>
                <th>密码</th>
                <th>园丁豆</th>
                <th>手机</th>
                <#--<th>邮箱</th>-->
                <th>QQ</th>
                <th>创建时间</th>
                <th>活跃等级</th>
                <th>操作</th>
            </tr>
            <#if teacherInfoAdminMapper??>
                <tr>
                    <td>${teacherInfoAdminMapper.teacher.id!}</td>
                    <td>${teacherInfoAdminMapper.teacher.profile.realname!}</td>
                    <#--<td>请从“登录老师账号”进入</td>-->
                    <td>
                        <span id="real_code"></span>
                        <#if teacherInfoAdminMapper.teacher?? && teacherInfoAdminMapper.teacher.id??>
                            <button type="button" id="query_user_password_${teacherInfoAdminMapper.teacher.id!''}" class="btn btn-info">临时密码</button>
                        </#if>
                    </td>
                    <td><a href="../integral/integraldetail.vpage?userId=${teacherInfoAdminMapper.teacher.id}">${teacherInfoAdminMapper.integral!0}</a></td>
                    <td><#if teacherInfoAdminMapper.teacher?? && teacherInfoAdminMapper.teacher.id??><button type="button" id="query_user_phone_${teacherInfoAdminMapper.teacher.id!''}" class="btn btn-info">查 看</button></#if>
                        <#if !requestContext.getCurrentAdminUser().isCsosUser()>
                        <#if teacherInfoAdminMapper.canBindMobile>
                            <input type="button" id="btnBindMobile" value="绑定" />
                        </#if>
                        </#if>
                    </td>
                    <#--<td><#if teacherInfoAdminMapper.teacher?? && teacherInfoAdminMapper.teacher.id??><button type="button" id="query_user_email_${teacherInfoAdminMapper.teacher.id!''}" class="btn btn-info">查 看</button></#if></td>-->
                    <td>${teacherInfoAdminMapper.qq!''}</td>
                    <td>${teacherInfoAdminMapper.teacher.getCreateTime()?datetime}</td>
                    <td>${teacherInfoAdminMapper.teacher.level!}(${teacherInfoAdminMapper.teacher.levelValue!})</td>
                    <td>
                        <#if !requestContext.getCurrentAdminUser().isCsosUser()>
                        <#if teacherInfoAdminMapper.teacher.pending?? && teacherInfoAdminMapper.teacher.pending == 1>
                            <button type="button" disabled id="btn_pending_teacher_${teacherInfoAdminMapper.teacher.id!''}" data-id="0" class="btn btn-success">恢 复</button>
                        <#else >
                            <button type="button" disabled id="btn_pending_teacher_${teacherInfoAdminMapper.teacher.id!''}" data-id="1" class="btn btn-warning">暂 停</button>
                        </#if>
                        </#if>
                    </td>
                </tr>
            </#if>
        </table>
        <table class="table table-hover table-striped table-bordered">
            <tr>
                <th>学科</th>
                <th>学校</th>
                <th>学校级别</th>
                <th>学校类型</th>
                <th>省市区(地区编码)</th>
                <th>认证状态</th>
                <th>认证时间</th>
                <th>原始邀请人</th>
                <th>是否成功</th>
                <th>唤醒人</th>
                <th>是否成功</th>
                <th>唤醒类型</th>
            </tr>
            <#if teacherInfoAdminMapper??>
                <tr>
                    <td>${(teacherInfoAdminMapper.teacher.subject.value)!""}</td>
                    <td><a href="../school/schoolhomepage.vpage?schoolId=${teacherInfoAdminMapper.schoolId!}">${teacherInfoAdminMapper.schoolName!""}</a>(${teacherInfoAdminMapper.schoolId!})</td>
                    <td>
                        <#if teacherInfoAdminMapper.schoolLevel??>
                            <#if teacherInfoAdminMapper.schoolLevel== 1>
                                小学
                            <#elseif teacherInfoAdminMapper.schoolLevel== 2>
                                中学
                            <#elseif teacherInfoAdminMapper.schoolLevel== 4>
                                高中
                            </#if>
                        </#if>
                    </td>
                    <td>${teacherInfoAdminMapper.schoolType!""}</td>
                    <td>${teacherInfoAdminMapper.regionName!""}(${teacherInfoAdminMapper.regionCode!""})</td>
                    <td id="verify_state">${teacherInfoAdminMapper.verifyState!""}</td>
                    <td id="verify_time">${teacherInfoAdminMapper.verifyTime!""}</td>
                    <td><a href="../user/userhomepage.vpage?userId=${teacherInfoAdminMapper.inviterId!""}">${teacherInfoAdminMapper.inviterName!''}</a>(${teacherInfoAdminMapper.inviterId!""}) </td>
                    <td><#if teacherInfoAdminMapper.inviteSuccess??>${teacherInfoAdminMapper.inviteSuccess?string('是', '否')}</#if></td>
                    <td>
                        <#if teacherInfoAdminMapper.activateHistory?? && teacherInfoAdminMapper.activateHistory.inviter??>
                            <a href="../user/userhomepage.vpage?userId=${teacherInfoAdminMapper.activateHistory.inviter.id!""}">${teacherInfoAdminMapper.activateHistory.inviter.profile.realname!}</a>(${teacherInfoAdminMapper.activateHistory.inviter.id!""})
                        </#if>
                    </td>
                    <td>
                        <#if teacherInfoAdminMapper.activateHistory?? && teacherInfoAdminMapper.activateHistory.history??>
                            ${teacherInfoAdminMapper.activateHistory.history.over?string('是','否')!}
                        </#if>
                    </td>
                    <td>
                        <#if teacherInfoAdminMapper.activateHistory?? && teacherInfoAdminMapper.activateHistory.history??>
                            ${teacherInfoAdminMapper.activateHistory.history.activationType!}
                        </#if>
                    </td>
                </tr>
            </#if>
        </table>
    </div>
    <br/>
    <br/>

    <legend>用户备注</legend>
    <#if !requestContext.getCurrentAdminUser().isCsosUser()>
    <ul class="inline">
        <li>
            <button class="btn btn-primary" onclick="addCustomerServiceRecord()">新增备注</button>
        </li>
        <li>
            <button id="hide_record_btn" class="btn btn-primary" onclick="hideCustomerServiceRecord()">隐藏备注</button>
        </li>
    </ul>
    </#if>
    <table id="customer_service_record" class="table table-hover table-striped table-bordered">
            <tr id="comment_title" >
            <th>用户ID</th>
            <th>添加人</th>
            <th>创建时间</th>
            <th>操作内容</th>
            <th>备注</th>
            <th>类型</th>
        </tr>
        <#list teacherInfoAdminMapper.customerServiceRecordList as record >
            <tr>
                <td style="width: 6em;">${record.userId!""}</td>
                <td style="width: 6em;">${record.operatorId!""}</td>
                <td style="width: 10em;">${record.createTime!""}</td>
                <td style="width: 10em;">${record.operationContent!""}</td>
                <td style="width: 20em;">${record.comments!""}</td>
                <td style="width: 6em;">${record.operationType!""}</td>
            </tr>
        </#list>
    </table>
    <br/>
    <br/>
    <#--班级列表-->
    <@clazzListChipPage.clazzListChip/>
    <#if !requestContext.getCurrentAdminUser().isCsosUser()>
    <#--<legend><a href="javascript:void(0);" id="create_clazz">新建班级</a></legend>-->
    </#if>

    <#if !requestContext.getCurrentAdminUser().isCsosUser()>
    <legend>微信绑定</legend>
    <table class="table table-hover table-striped table-bordered" style="width: 1000px;">
        <tr>
            <th>教师ID</th>
            <th>OPEN_ID</th>
            <th>绑定时间</th>
            <th>更新时间</th>
            <th>绑定方式</th>
            <th>是否有效</th>
            <th>操作</th>
        </tr>
        <#if wechats?has_content>
            <#list wechats as wechat>
                <tr>
                    <td>
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
                    <td>
                        <#if !wechat.DISABLED>
                            <a href="javascript:void(0);" class="btn btn-info sendwxnotice" data-id="${wechat.USER_ID}" style="width: 90px">发送微信消息</a>
                        </#if>
                    </td>
                </tr>
            </#list>
        </#if>
    </table>
    </#if>


    <!----------------------------dialog----------------------------------------------------------------------------------->

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
                        <dd>${teacherInfoAdminMapper.teacher.id!''}</dd>
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
                        <dd>${teacherInfoAdminMapper.teacher.id!''}</dd>
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
                        <dt>新的密码</dt>
                        <dd><input type="text" name="password" id="password" value="123456"/></dd>
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
                        <dd>重置用户密码。</dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="dialog_edit_teacher_password" class="btn btn-primary">确 定</button>
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
                        <dd>${teacherInfoAdminMapper.teacher.id!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>记录类型</dt>
                        <dd>
                            <select name="recordType" id="recordType" class="multiple">
                                <#if recordTypeList?has_content>
                                    <#list recordTypeList as recordType>
                                        <option value='${recordType.key}'>${recordType.value}</option>
                                    </#list>
                                </#if>
                            </select>
                        </dd>
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
            <button id="dialog_edit_teacher_date" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="authentication_dialog" class="modal hide fade" style="width: 700px; margin-left:-350px; ">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>认证老师</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>用户ID</dt>
                        <dd>${teacherInfoAdminMapper.teacher.id!''}</dd>
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
                        <dd>
                            <div id="authentication_dialog_radio" class="btn-group" data-toggle="buttons-radio">
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
                        <dd><textarea id="authenticationExtraDesc" cols="35" rows="2"></textarea></dd>
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
            <button id="dialog_edit_teacher_authentication" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="cancel_authentication_dialog" class="modal hide fade" style="width: 700px; margin-left:-350px; ">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>更新认证状态</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>用户ID</dt>
                        <dd>${teacherInfoAdminMapper.teacher.id!''}</dd>
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
                                <option value="0">等待认证</option>
                            <#--<option value="2">认证资料不全</option>-->
                                <option value="3">取消认证</option>
                            </select>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>问题描述</dt>
                        <dd>
                            <div id="cancel_authentication_dialog_radio" class="btn-group" data-toggle="buttons-radio">
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
                        <dd><textarea id="cancelAuthenticationExtraDesc" cols="35" rows="2"></textarea></dd>
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
            <button id="dialog_cancel_teacher_authentication" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

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
                        <dd>${teacherInfoAdminMapper.teacher.id!''}</dd>
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
                        <dd>${teacherInfoAdminMapper.teacher.id!''}</dd>
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
                        <dd>更改老师学校</dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="change_school_btn" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="info_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>信息</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal" style="text-align: center;">
                <ul class="inline">
                    <li style="text-align: left;"></li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">确 定</button>
        </div>
    </div>

    <div id="delete_teacher_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>删除老师账号</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>老师ID</dt>
                        <dd>${teacherInfoAdminMapper.teacher.id!''}</dd>
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
                        <dd><textarea id="deleteTeacherDesc" cols="35" rows="4"></textarea></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>所做操作</dt>
                        <dd>管理员删除老师账号</dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="delete_teacher_btn" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
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
                        <dd>${teacherInfoAdminMapper.teacher.id!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>手机</dt>
                        <dd><input type="text" id="txtMobile" /></dd>
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
    </div>

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
                            <input id="bw_${activity.key}" type="checkbox" data-key="${activity.key}"/> ${activity.value}
                        </label>
                    </li>
                    </#list>
                </ul>
            </#if>
        </div>
    </div>
    <div id="create_clazz_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>新建班级</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>老师ID</dt>
                        <dd>${teacherInfoAdminMapper.teacher.id!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>学制</dt>
                        <dd>
                            <input type="radio" name="edu_system" value="P5" checked>五年制&nbsp;&nbsp;
                            <input type="radio" name="edu_system" value="P6">六年制
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>年级</dt>
                        <dd>
                            <select id="classLevel">
                                <option value="1">一年级</option>
                                <option value="2">二年级</option>
                                <option value="3">三年级</option>
                                <option value="4">四年级</option>
                                <option value="5">五年级</option>
                                <option value="6">六年级</option>
                            </select>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>班级名称</dt>
                        <dd>
                            <input id="create_clazz_name" type="input" value="">
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>学生数量</dt>
                        <dd><input id="create_clazz_stu_num" type="input" value=""></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="create_clazz_btn" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

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
                        <dd>${teacherInfoAdminMapper.teacher.id!''}</dd>
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
                                <#if subjectTypeList??>
                                    <#list subjectTypeList as subject>
                                        <option value="${subject}">${subject.value!}</option>
                                    </#list>
                                </#if>
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
    <div id="sendWxNoticeDialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>发送微信消息</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>老师ID</dt>
                        <dd>${teacherInfoAdminMapper.teacher.id!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>内容</dt>
                        <dd><textarea id="wxNoticeContent" cols="35" rows="4"></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="send_wxnotice_submit" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>
</div>

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
<script>
    function appendNewRecord(data){
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

    function hideCustomerServiceRecord(){
        $("#customer_service_record").toggle(function(){
            var $target = $("#hide_record_btn");
            switch($target.html()){
                case "隐藏备注":
                    $target.html("显示备注");
                    break;
                case "显示备注":
                    $target.html("隐藏备注");
                    break;
            }
        });
    }

    function checkAuthentication(){
    <#if (teacherInfoAdminMapper.teacher.isJuniorTeacher())!false>
        window.open("${ms_crm_admin_url}/crm/teacher/homeworkdetail?userId=${teacherInfoAdminMapper.teacher.id!''}&day=30");
    <#else>
        window.location.href = "teacherhomeworkdetail.vpage?userId=${teacherInfoAdminMapper.teacher.id!''}&day=30";
    </#if>
    }

    function checkNewAuthentication(){
        <#if (teacherInfoAdminMapper.teacher.isJuniorTeacher())!false>
            window.open("${ms_crm_admin_url}/crm/teacher/homeworkdetail?userId=${teacherInfoAdminMapper.teacher.id!''}&day=30");
        <#else>
            window.location.href = "/crm/teachernew/teachernewhomeworkhistory.vpage?teacherId=${teacherInfoAdminMapper.teacher.id!''}&day=30";
        </#if>
    }

    function quizs(){
        window.location.href = "teacherquizdetail.vpage?userId=${teacherInfoAdminMapper.teacher.id!''}";
    }

    function resetPassword(){
        // 用户一个月内修改过密码则出现提示
        $.get('checkchangedpassword.vpage', {teacherId : '${(teacherInfoAdminMapper.teacher.id)!}'}, function(data) {
            if (!data.success || confirm('用户在一个月内已经改过密码，请确认是否继续？')) {
                $("#password").val("123456");
                $('#passwordExtraDesc').val('');
                $("#password_dialog_radio button").removeClass("active").eq(0).addClass("active");
                $("#password_dialog").modal("show");
            }
        });
    }

    function addCustomerServiceRecord(){
        $("#questionDesc").val('');
        $("#operation").val('');
        $('#recordType').val('1');
        $("#record_dialog").modal("show");
    }

    function updateAuthentication(){
        $('#authenticationExtraDesc').val('');
        $("#authentication_dialog_radio button").removeClass("active").eq(0).addClass("active");
        $("#authentication_dialog").modal("show");
    }

    function cancelAuthentication(){
        $('#cancelAuthenticationExtraDesc').val('');
        $("#cancel_authentication_dialog_radio button").removeClass("active").eq(0).addClass("active");
        $("#cancel_authentication_dialog").modal("show");
    }

    function viewBlackWhiteAttr(){
        $.ajax({
            type: "post",
            url: "getUserActivity.vpage",
            data: {
                userId : ${teacherInfoAdminMapper.teacher.id!""}
            },
            success: function (data){
                if(data.success){
                    $(data.bwlist).each(function(index){
                        $("#bw_"+data.bwlist[index].activityType).attr("checked", "checked");
                    });
                }
            }
        });
        $("#black_white_dialog").modal("show");
    }

    function teacherLogin(){
        $("#teacherLoginDesc").val("");
        $("#teacherLogin_dialog").modal("show");
    }

    function changeSubject(){
        $("#changeSubjectDesc").val("");
        $("#change_subject_dialog").modal("show");
    }

    function kickOutOfApp(userId){
        if (!confirm("确定踢出App重新登录?")){
            return;
        }
        $.ajax({
            url: "/crm/teacher/kickOutOfApp.vpage",
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

    function openFaultOrderDialog() {
        $("#fault_order_create_info").val();
        $("#fault_order_dialog").modal("show");
    }

    $(function () {

        $('[id^="query_user_password_"]').on('click', function(){
            var item = $(this);
            var id = parseInt(item.attr("id").substr("query_user_password_".length));
            $("#real_code").text("");
            $.get("../user/temppassword.vpage",{
                userId:id
            },function(data){
                $("#real_code").text(data.password);
            });
        });

        $('[id^="btn_pending_teacher_"]').on('click', function(){
            var id = $(this).attr("id").substr("btn_pending_teacher_".length);
            var pending = $(this).attr("data-id");
            $('#pending_teacher_id').val(id);
            $('#pending_teacher_id').attr('data-id',pending);
            $('#pending_teacher_dialog').modal("show");
        });

        $("#pending_teacher_btn").on("click", function(){
            var id = parseInt($('#pending_teacher_id').val());
            var pending = parseInt($('#pending_teacher_id').attr("data-id"));
            var desc = $('#pending_teacher_desc').val();
            if(desc == '') {
                alert('请输入原因！');
                return false;
            }
            $.post("pendingteacher.vpage",{
                teacherId:id,
                pending:pending,
                desc:desc
            },function(data){
                if(data.success) {
                    window.location.reload();
                }else {
                    alert(data.info);
                }

            });
        });



        $("#activityTypeList ul li").each(function(){
            $(this).find("input").on("click", function(){
                var $this = $(this);
                var checkFlag;
                if($this.prop("checked")){
                    checkFlag = 'checked';
                }else{
                    checkFlag = 'unChecked';
                }
                $.ajax({
                    type: "post",
                    url: "updateUserActivity.vpage",
                    data: {
                        userId : ${teacherInfoAdminMapper.teacher.id!""},
                        key : $this.data("key"),
                        checkFlag : checkFlag
                    },
                    success: function (data){
                        if(data.success){

                        }else{
                            alert(data.info);
                        }
                    }
                });
            });
        });


        $("#dialog_edit_teacher_password").on("click", function(){
            var queryUrl = "../user/resetpassword.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId :${teacherInfoAdminMapper.teacher.id!""},
                    password : $("#password").val(),
                    passwordDesc : $("#password_dialog_radio button[class='btn active']").html(),
                    passwordExtraDesc : $('#passwordExtraDesc').val()
                },
                success: function (data){
                    if(data.success){
                        window.location.reload();
                    }else{
                        alert(data.info);
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
                    userId: "${teacherInfoAdminMapper.teacher.id!''}",
                    userName: "${teacherInfoAdminMapper.teacher.profile.realname!''}",
                    userType: "${teacherInfoAdminMapper.teacher.userType!''}",
                    createInfo:$('#fault_order_create_info').val(),
                    faultType:faultTypes.join(",")
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

        $("#dialog_edit_teacher_date").on("click", function(){
            var queryUrl = "../user/addcustomerrecord.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId : ${teacherInfoAdminMapper.teacher.id!''},
                    recordType : $("#recordType").val(),
                    questionDesc : $("#questionDesc").val(),
                    operation : $("#operation").val()
                },
                success: function (data){
                    $("#record_success").val(data.success);

                    if(data.success){
                        appendNewRecord(data);
                    }else{
                        alert("增加日志失败。");
                    }
                    $("#record_dialog").modal("hide");
                }
            });
        });

        $("#dialog_edit_teacher_authentication").on("click", function(){

            if($('#authenticationExtraDesc').val() == '') {
                alert('请填写附加描述');
                return;
            }

            var queryUrl = "addteacherauthentication.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId : ${teacherInfoAdminMapper.teacher.id!""},
                    authenticationDesc : $("#authentication_dialog_radio button[class='btn active']").html(),
                    authenticationExtraDesc : $('#authenticationExtraDesc').val()
                },
                success: function (data){
                    if(data.success){
                        alert("认证老师成功！");
                        window.location.reload();
                    }else{
                        alert(data.info);
                    }
                }
            });
        });

        $("#dialog_cancel_teacher_authentication").on("click", function(){

            if($('#cancelAuthenticationExtraDesc').val() == '') {
                alert('请填写附加描述');
                return;
            }
            var queryUrl = "cancelteacherauthentication.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId : ${teacherInfoAdminMapper.teacher.id!""},
                    authenticationState : $("#dialog_authenticationState").val(),
                    authenticationDesc : $("#cancel_authentication_dialog_radio button[class='btn active']").html(),
                    authenticationExtraDesc : $('#cancelAuthenticationExtraDesc').val()
                },
                success: function (data){
                    if(data.success){
                        alert("取消认证老师成功！");
//                        appendNewRecord(data);
//                        $("#verify_state").text(data.authenticationState);
//                        $("#verify_time").text(data.verifyTime);
//                        $("#cancel_authentication_dialog").modal("hide");
                        window.location.reload();
                    }else{
                        alert("修改老师认证状态失败，请更改老师认证状态，并填写问题描述。");
                    }
                }
            });
        });

        $("#teacher_login_btn").on("click", function(){
            var queryUrl = "teacherlogin.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    teacherId : ${teacherInfoAdminMapper.teacher.id!''},
                    teacherLoginDesc : $("#teacherLoginDesc").val()
                },
                success: function (data){
                    if(data.success){
                        $("#teacherLogin_dialog").modal("hide");
                        var postUrl = data.postUrl;
                        window.open(postUrl);
                    }else{
                        alert("登录老师账号失败。");
                    }
                }
            });
        });

        $("#change_subject_btn").on("click", function(){

            var subject = $("#subject_type").find("option:selected").val();
            var desc = $("#changeSubjectDesc").val();
            if(desc == '') {
                alert("请输入描述！");
                return false;
            }
            var queryUrl = "changesubject.vpage";

            if(window.confirm("修改学科将自动检查老师已布置作业，确认修改？")) {
                $.ajax({
                    type: "post",
                    url: queryUrl,
                    data: {
                        teacherId: ${teacherInfoAdminMapper.teacher.id!''},
                        subject: subject,
                        desc: desc
                    },
                    success: function (data) {
                        if (data.success) {
                            if (data.hasPendingApplications) {
                                if(window.confirm("您的账号 ${teacherInfoAdminMapper.teacher.id!''} 存在接管班级或转让班级的申请, 继续修改学科将自动取消转让/接管班级\n 是否确认继续修改学科? ")) {
                                    $.ajax({
                                        type: "post",
                                        url: queryUrl,
                                        data: {
                                            teacherId: ${teacherInfoAdminMapper.teacher.id!''},
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

        var changeSchool = function(precheck){
            var postUrl = "changeschool.vpage";
            $.ajax({
                type: "post",
                url: postUrl,
                data: {
                    teacherId : ${teacherInfoAdminMapper.teacher.id!''},
                    schoolId : $('#schoolId').val(),
                    changeSchoolDesc : $("#changeSchoolDesc").val(),
                    precheck : precheck
                },
                success: function (data){
                    if(precheck){
                        if(data.success){
                            if(window.confirm("确实要将老师(ID:${teacherInfoAdminMapper.teacher.id!''}及其所在班级转至它校吗?")){
                                $('#schoolId').val('');
                                $('#changeSchoolDesc').val('');
                                $('#changeSchool_dialog').modal('show');
                            }
                        }else{
                            alert(data.info);
                        }
                    }else{
                        if(data.success){
                            $("#changeSchool_dialog").modal("hide");
                            alert(data.info);
                            window.location.href='teacherhomepage.vpage?teacherId=${teacherInfoAdminMapper.teacher.id}'
                        }else{
                            alert(data.info);
                        }
                    }

                }
            });
        }
        $(document).on("click", ".cschool", function() {
            changeSchool(true);
        });

        $(document).on("click", "#change_school_btn", function() {
            changeSchool(false);
        });

        $("#checkTeacherAuthentication").on("click", function(){
            $.get("checkteacherauthentication.vpage?teacherId=${teacherInfoAdminMapper.teacher.id}",function(data){
                var $dialog = $('#info_dialog');
                $dialog.find('li').empty();
                $dialog.find('li').append(data.info);
                $dialog.modal('show');
            });
        });

        $('#deleteTeacher').on('click', function() {
            $('#deleteTeacherDesc').text('');
            $('#delete_teacher_dialog').modal('show');
        });

        $('#delete_teacher_btn').on('click', function() {
            if (confirm('确认要删除老师${(teacherInfoAdminMapper.teacher.profile.realname)!}')) {
                var postData = {
                    teacherId           :   '${(teacherInfoAdminMapper.teacher.id)!}',
                    deleteTeacherDesc   :   $('#deleteTeacherDesc').val()
                };
                $.post('deleteteacher.vpage', postData, function(data) {
                    alert(data.info);
                    if (data.success) {
                        location.href = 'teacherlist.vpage';
                    }
                });
            }
        });

        $('#btnBindMobile').on('click',function(){
            var $dialog = $('#bind_mobile_dialog');
            $dialog.modal('show');
        });
        //绑定手机
        $('#bind_mobile_submit').on('click',function(){
            $.post('bindmobile.vpage',
                    {
                        teacherId : ${teacherInfoAdminMapper.teacher.id!''},
                        mobile : $('#txtMobile').val(),
                        desc : $('#txtBindMobileDesc').val()
                    },function(data){
                        if(data.success){
                            window.location.reload();
                        }else{
                            alert(data.info);
                        }

            });
        });

        $("#create_clazz").on('click',function(){
            $("#create_clazz_dialog").modal('show');
        });

        $("#create_clazz_btn").on('click',function(){
            var eduSystem = $('input:radio[name="edu_system"]:checked').val();
            var classLevel = $('#classLevel').find('option:selected').val();
            if(eduSystem == "P5" && classLevel == "6") {
                alert("学制与年级匹配不符!");
                return false;
            }
            var clazzName = $('#create_clazz_name').val().trim();
            if(clazzName == '') {
                alert("请输入班级名称!");
                return false;
            }
            var classSize = $('#create_clazz_stu_num').val();
            if(!$.isNumeric(classSize)) {
                alert("请输入正确的学生数量!");
                return false;
            }
            $.post('createclazz.vpage',
            {
                teacherId : ${teacherInfoAdminMapper.teacher.id!''},
                addStudentType : "common",
                classLevel : classLevel,
                classSize : classSize,
                clazzName : clazzName,
                eduSystem : eduSystem,
                names : [],
                schoolId : ${teacherInfoAdminMapper.schoolId!0}
            },function(data){
                if(data.success){
                    alert("创建成功!");
                    window.location.reload();
                }else{
                    alert(data.info);
                }
            });
        });

        $(".sendwxnotice").on('click',function(){
            $("#sendWxNoticeDialog").modal('show');
        });

        $("#send_wxnotice_submit").on('click',function(){
            var content = $('#wxNoticeContent').val().trim();
            if(content == '') {
                alert("请输入内容!");
                return false;
            }
            $.post('sendwxnotice.vpage',
                    {
                        teacherId : ${teacherInfoAdminMapper.teacher.id!''},
                        content : content
                    },function(data){
                        if(data.success){
                            alert("发送成功!");
                            window.location.reload();
                        }else{
                            alert(data.info);
                        }
                    });
        });

    });
</script>
</@layout_default.page>
