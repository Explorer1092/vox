<#import "../../layout_default.ftl" as layout_default>
<#import "../headsearch.ftl" as headsearch>
<#setting datetime_format="yyyy-MM-dd HH:mm"/>
<@layout_default.page page_title="${schoolInfoAdminMapper.schoolShortName!''}(${schoolInfoAdminMapper.schoolId!''})" page_num=3>
<script type="text/javascript" src="https://webapi.amap.com/maps?v=1.4.12&key=3e70b92d4cfb39707ac12aad08059f63&&plugin=AMap.Scale,AMap.OverView,AMap.ToolBar,AMap.Autocomplete,AMap.PlaceSearch"></script>
<style>
    #alert_div tr td:nth-child(1){  width:100px  }
    #alert_div tr td:nth-child(2){  width:100px  }
    #alert_div tr td{  vertical-align: middle;  }
    .title_btn {  margin-top:2px;}
    .text-left {  text-align:left !important;}
    .schoolMapBox{width: 770px;height: 380px;position: relative;}
    .schoolMapBox #schoolMap{width: 770px;height: 380px;}
    .schoolMapSearch{padding: 10px 15px;border-radius: 5px;position: absolute;top: 5px;left: 5px;background-color: white;width: auto;border-width: 0;box-shadow: 0 2px 6px 0 rgba(114, 124, 245, .5);}
    .schoolMapSearch .input-item{position: relative;height: 28px;}
    .schoolMapSearch .input-item .input-item-text{display: inline-block;height: 28px;line-height: 28px;border:1px solid #ccc;border-right: none;border-radius: 5px 0 0 5px;vertical-align: middle;padding: 0 10px;background: #e9ecef;font-size: 14px;float: left;}
    .schoolMapSearch .input-item #tipinput{display: inline-block;width: 150px;vertical-align: middle;border-radius: 0 5px 5px 0;-webkit-appearance: none;appearance: none;outline: none;}
    .amap-sug-result{z-index: 99999999;}
</style>
<div id="main_container" class="span9">
    <@headsearch.headSearch/>
    <div class="form-horizontal">
        <legend>
            学校主页:${schoolInfoAdminMapper.schoolName!''}(${schoolInfoAdminMapper.schoolId!''})
            <#if (isDictSchool == true)!false>
                <b style="color: red;">重点校</b>
            </#if>
            <#if (daiteSchool == true)!false>
                <b>&nbsp;&nbsp;合作校</b>
            </#if>
        </legend>
        <#if !requestContext.getCurrentAdminUser().isCsosUser()>
            <ul class="inline">
                <li>
                    <button id="update_school_btn" class="btn btn-primary title_btn" onclick="updateSchool()">修改学校</button>
                </li>
                <li>
                    <button class="btn btn-primary title_btn" id='merge_school'>合并学校</button>
                </li>
                <li>
                    <button id="update_school_btn title_btn" class="btn btn-primary" onclick="updateAuthState()">修改鉴定状态</button>
                </li>
                <li>
                    <a href="downloadschoolteachers.vpage?schoolId=${schoolInfoAdminMapper.schoolId!}" class="btn btn-primary title_btn">下载学校老师名单</a>
                </li>
                <li>
                    <a href="schoolteacherlist.vpage?schoolId=${schoolInfoAdminMapper.schoolId!}" class="btn btn-primary title_btn">老师管理</a>
                </li>
                <li>
                    <a id="delete" href="#delete_school_dialog" role="button" class="btn btn-danger title_btn" data-toggle="modal">删除学校</a>
                </li>
                <li>
                    <a id="setCount" href="#set_clazz_count" role="button" class="btn btn-danger title_btn" data-toggle="modal">修改班级人数上限</a>
                </li>
                <#if  schoolInfoAdminMapper.locked?? && schoolInfoAdminMapper.locked >
                    <li>
                        <button class="btn btn-danger title_btn" id='unLocked' onclick="unLockedSchool()">解锁学校</button>
                    </li>
                </#if>
                <li>
                    <button class="btn btn-primary title_btn" id='school_evaluate'>学校评分</button>
                </li>
                <li>
                    <button class="btn btn-primary title_btn" id='evaluate_history'>学校评分历史</button>
                </li>
                <li>
                    <a class="btn btn-primary title_btn" href="breakchangeclass.vpage?schoolId=${schoolInfoAdminMapper.schoolId!''}" target="_blank">年级打散换班</a>
                </li>
                <li>
                    <a class="btn btn-primary title_btn" href="linkclasses.vpage?schoolId=${schoolInfoAdminMapper.schoolId!''}" target="_blank">关联走课学生</a>
                </li>
                <li>
                    <button class="btn btn-primary title_btn" id='modifyEduSystem'>修改学校学制</button>
                </li>
                <li>
                    <button class="btn btn-primary title_btn" id='modifyDaite'>合作校</button>
                </li>
                <#if schoolInfoAdminMapper?has_content && schoolInfoAdminMapper.schoolLevelValue?has_content && (schoolInfoAdminMapper.schoolLevelValue == 2 || schoolInfoAdminMapper.schoolLevelValue == 4)>
                <li>
                    <button class="btn btn-primary title_btn" id='modifyAdjustClazz'>自主调整班级开关(${(adjustClazz)?string('开', '关')})</button>
                </li>
                </#if>
                <li>
                    <button class="btn btn-primary title_btn" id='trackSchoolMap'>标记学校坐标</button>
                </li>
            </ul>
        </#if>
        <table id="schools" class="table table-hover table-striped table-bordered">
            <tr>
                <th>学校ID</th>
                <th>学校名称</th>
                <th>所在地区</th>
                <th>地区编号</th>
                <th>鉴定状态</th>
                <th>学校级别</th>
                <th>vip等级</th>
                <th>学校类型</th>
                <th>开放付费</th>
                <th>创建时间</th>
                <th>年制</th>
                <th>地理位置</th>
            </tr>
            <#if schoolInfoAdminMapper?has_content>
                <tr>
                    <td>${schoolInfoAdminMapper.schoolId!''}</td>
                    <td>${schoolInfoAdminMapper.schoolName!''}</td>
                    <td>${schoolInfoAdminMapper.regionName!''}</td>
                    <td>${schoolInfoAdminMapper.regionCode!''}</td>
                    <td>${schoolInfoAdminMapper.authenticationState!''}</td>
                    <td>${schoolInfoAdminMapper.schoolLevel!''}</td>
                    <td>${schoolInfoAdminMapper.vipLevel!''}</td>
                    <td>${schoolInfoAdminMapper.schoolType!''}</td>
                    <td>${schoolInfoAdminMapper.payOpen?string('开放','未开放')}</td>
                    <td>${schoolInfoAdminMapper.createTime!''}</td>
                    <td>${schoolInfoAdminMapper.edusystem!''}</td>
                    <td>${(schoolExtInfo.address)!''}</td>
                </tr>
            </#if>
        </table>
    </div>
    <br/>

    <div>
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation" class="active">
                <a href="#clazz" id="clazz-tab" role="tab" data-toggle="tab" aria-controls="base" aria-expanded="true">班级列表</a>
            </li>
            <li role="presentation" >
                <a href="#ambassador" id="ambassador-tab" role="tab" data-toggle="tab" aria-controls="base" aria-expanded="true">校园大使</a>
            </li>
            <#if schoolInfoAdminMapper.affairTeacher!false>
                <li role="presentation" >
                    <a href="#edu-teacher" id="edu-teacher-tab" role="tab" data-toggle="tab" aria-controls="base" aria-expanded="true">教务老师</a>
                </li>
            </#if>
            <#if schoolInfoAdminMapper.schoolLevel?has_content && (schoolInfoAdminMapper.schoolLevel == "中学" || schoolInfoAdminMapper.schoolLevel == "高中")>
                <li role="presentation" >
                    <a href="#klx" id="klx-tab" role="tab" data-toggle="tab" aria-controls="base" aria-expanded="true">快乐学信息列表</a>
                </li>
            </#if>
        <#-- <#if schoolInfoAdminMapper.schoolLevel?has_content && (schoolInfoAdminMapper.schoolLevel == "中学" || schoolInfoAdminMapper.schoolLevel == "高中")>
         <li role="presentation" >
             <a href="#xb-admin" id="xb-admin-tab" role="tab" data-toggle="tab" aria-controls="base" aria-expanded="true">校本题库管理员</a>
         </li>
             <li role="presentation" >
                 <a href="#exam-manager" id="exam-manager-tab" role="tab" data-toggle="tab" aria-controls="base" aria-expanded="true">考试管理员</a>
             </li>
         </#if>-->
            <li role="presentation" >
                <a href="#without-clazz" id="without-clazz-tab" role="tab" data-toggle="tab" aria-controls="base" aria-expanded="true">无班级的老师列表</a>
            </li>
            <#if schoolInfoAdminMapper.schoolLevel == "中学" || schoolInfoAdminMapper.schoolLevel == "高中">
                <li role="presentation" >
                    <a href="#custom-clazz" id="custom-clazz-tab" role="tab" data-toggle="tab" aria-controls="base" aria-expanded="true">定制教学班级列表</a>
                </li>
            </#if>
            <li role="presentation" >
                <a href="#without-teacher" id="without-teacher-tab" role="tab" data-toggle="tab" aria-controls="base" aria-expanded="true">无老师班级列表</a>
            </li>
            <li role="presentation" >
                <a href="#record" id="record-tab" role="tab" data-toggle="tab" aria-controls="base" aria-expanded="true">学校备注</a>
            </li>
            <#if schoolInfoAdminMapper.schoolLevel?has_content && (schoolInfoAdminMapper.schoolLevel == "中学" || schoolInfoAdminMapper.schoolLevel == "高中")>
                <li role="presentation" >
                    <a href="#teacher-roles" id="teacher-roles-tab" role="tab" data-toggle="tab" aria-controls="base" aria-expanded="true">老师角色列表</a>
                </li>
            </#if>
        </ul>
        <div class="tab-content" id="tabContent">
            <div class="tab-pane fade" role="tabpanel" id="ambassador" aria-labelledby="ambassador-tab">
                <ul class="inline">
                    <li>
                        <a id="update_ambassador" href="#update_ambassador_dialog" role="button" class="btn btn-primary" data-toggle="modal">添加大使</a>
                    </li>
                </ul>
                <table class="table table-hover table-striped table-bordered">
                    <tr>
                        <th>ID</th>
                        <th>姓名</th>
                        <th>学科</th>
                        <th>创建时间</th>
                        <th>操作</th>
                    </tr>
                    <#if schoolInfoAdminMapper?has_content && schoolInfoAdminMapper.ambassadorList?has_content>
                        <#list schoolInfoAdminMapper.ambassadorList as ambassador>
                            <tr>
                                <td><a href="../teacher/teacherhomepage.vpage?teacherId=${ambassador.ambassadorId!""}">${ambassador.ambassadorId!''}</a></td>
                                <td>${ambassador.userName!''}</td>
                                <td>${ambassador.subject!''}</td>
                                <td>${ambassador.ambassadorDate!''}</td>
                                <td><a name="disable-ambassador" data-ambassador-id="${ambassador.ambassadorId!''}" role="button" class="btn btn-success" style="font-size: 12px;">取消</a></td>
                            </tr>
                        </#list>
                    </#if>
                </table>
            </div>
            <div class="tab-pane fade" role="tabpanel" id="edu-teacher" aria-labelledby="edu-teacher-tab">
                <ul class="inline">
                    <li>
                        <a class="btn btn-primary title_btn" id='openEduAdminAccount' href="javascript:;">开通教务账号</a>
                    </li>
                </ul>
                <table class="table table-hover table-striped table-bordered">
                    <tr>
                        <th>ID</th>
                        <th>姓名</th>
                        <th>手机号</th>
                        <th>创建时间</th>
                        <th>操作</th>
                    </tr>
                    <#if schoolInfoAdminMapper?has_content && schoolInfoAdminMapper.affairTeacherList?has_content>
                        <#list schoolInfoAdminMapper.affairTeacherList as affairTeacher>
                            <tr>
                                <td>${affairTeacher.id!''}</a></td>
                                <td>${affairTeacher.fetchRealname()!''}</td>
                                <td><a href="javascript:;" id="query_user_phone_${affairTeacher.id!''}" class="btn btn-success">查看手机号</td>
                                <td>${affairTeacher.createTime!?string('yyyy-MM-dd HH:mm:ss')}</td>
                                <td>
                                    <a href="javascript:;" data-tid="${affairTeacher.id!''}" class="btn btn-danger affair-mobile">变更手机号</a>
                                    <a href="javascript:;" data-tid="${affairTeacher.id!''}" class="btn btn-success affair-home-page">登录首页</a>
                                    <a href="javascript:;" data-tid="${affairTeacher.id!''}" class="btn affair-reset-pwd">重置密码</a>
                                    <a href="javascript:;" data-tid="${affairTeacher.id!''}" class="btn affair-delete">删除</a>
                                    <#if schoolInfoAdminMapper.lagerExamUserIds?has_content && schoolInfoAdminMapper.lagerExamUserIds?seq_contains(affairTeacher.id)>
                                        <input userid="${affairTeacher.id!''}" schoolid="${schoolInfoAdminMapper.schoolId!''}" name="bigTestPermission" type="checkbox" checked class="pop_condition bigTestPermission" style="margin-top: 3px;"/>大考班级权限
                                    <#else>
                                        <input userid="${affairTeacher.id!''}" schoolid="${schoolInfoAdminMapper.schoolId!''}" name="bigTestPermission" type="checkbox"  class="pop_condition bigTestPermission" style="margin-top: 3px;"/>大考班级权限
                                    </#if>
                                </td>
                            </tr>
                        </#list>
                    </#if>
                </table>
            </div>
            <div class="tab-pane fade" role="tabpanel" id="klx" aria-labelledby="klx-tab">
                <ul class="inline">
                    <li>
                        <a id="allow_klx_privilege" href="#allow_klx_privilege_dialog" role="button" class="btn btn-primary" data-toggle="modal">开通权限</a>
                        <a id="query_klx_scannumber" href="#query_klx_scannumber_dialog" role="button" class="btn btn-primary" data-toggle="modal">查询填涂号</a>
                    </li>
                </ul>
                <table class="table table-hover table-striped table-bordered" style="margin-bottom: 5">
                    <tr>
                        <th>填涂号位数</th>
                        <th>阅卷机权限</th>
                        <th>题卡合一答题卡权限</th>
                        <th>条形码答题卡权限</th>
                        <th>校本题库权限</th>
                        <th>A3答题卡权限</th>
                        <th>手写答题卡权限</th>
                    </tr>
                    <tr>
                        <td><#if (schoolKlxPrivilegeInfo.scanNumberDigit)??> ${(schoolKlxPrivilegeInfo.scanNumberDigit)!0} 位数填涂号 <#else> 未设定 </#if><a href="javascript:void(0);" class="btn btn-success" id="set_scan_number_digit">设置</a></td>
                        <td>${(schoolKlxPrivilegeInfo.scanMachineFlag)?string('开通', '未开通')}</td>
                        <td>${(schoolKlxPrivilegeInfo.questionCardFlag)?string('开通', '未开通')}</td>
                        <td>${(schoolKlxPrivilegeInfo.barcodeAnswerQuestionFlag)?string('开通', '未开通')}</td>
                        <td>${(schoolKlxPrivilegeInfo.questionBankFlag)?string('开通', '未开通')}</td>
                        <td>${(schoolKlxPrivilegeInfo.a3AnswerQuestionFlag)?string('开通', '未开通')}</td>
                        <td>${(schoolKlxPrivilegeInfo.manualAnswerQuestionFlag)?string('开通', '未开通')}</td>
                    </tr>
                </table>
                <#if schoolKlxPrivilegeInfo?has_content && (schoolKlxPrivilegeInfo.subjects)?has_content>
                    <table class="table table-hover table-striped table-bordered">
                        <tr>
                            <th rowspan="2" style="text-align: center;vertical-align: middle;">学科权限</th>
                            <#list (schoolKlxPrivilegeInfo.subjects) as sub><th style="text-align: center;vertical-align: middle;">${sub.name}</th></#list>
                        </tr>
                        <tr>
                            <#list (schoolKlxPrivilegeInfo.subjects) as sub><td style="text-align: center;vertical-align: middle;">${sub.checked?string('已开通', '--')}</td></#list>
                        </tr>
                    </table>
                </#if>
            </div>
        <#--<div class="tab-pane fade" role="tabpanel" id="xb-admin" aria-labelledby="xb-admin-tab">
            <ul class="inline">
                <li>
                    <a id="add_schoolQuizBankAdministrator" href="#add_schoolQuizBankAdministrator_dialog" role="button" class="btn btn-primary" data-toggle="modal">添加校本题库管理员</a>
                </li>
            </ul>
            <table class="table table-hover table-striped table-bordered">
                <tr>
                    <th>ID</th>
                    <th>姓名</th>
                    <th>学科</th>
                    <th>创建时间</th>
                    <th>操作</th>
                </tr>
                <#if schoolQuizBankAdministratorList?has_content>
                    <#list schoolQuizBankAdministratorList as schoolQuizBankAdministratorInfo>
                        <tr>
                            <td><a href="/crm/teachernew/teacherdetail.vpage?teacherId=${schoolQuizBankAdministratorInfo.teacherId!''}">${schoolQuizBankAdministratorInfo.teacherId!''}</a></td>
                            <td>${schoolQuizBankAdministratorInfo.userName!''}</td>
                            <td>${schoolQuizBankAdministratorInfo.subject!''}</td>
                            <td>${schoolQuizBankAdministratorInfo.createTime!''}</td>
                            <td><a name="disable-schoolQuizBankAdministrator" data-teacher-id="${schoolQuizBankAdministratorInfo.teacherId!''}" role="button" class="btn btn-success" style="font-size: 12px;">取消</a></td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </div>-->
        <#--<div class="tab-pane fade" role="tabpanel" id="exam-manager" aria-labelledby="exam-manager-tab">
            <ul class="inline">
                <li>
                    <a id="add_examManager" href="#add_examManager_dialog" role="button" class="btn btn-primary" data-toggle="modal">添加考试管理员</a>
                </li>
            </ul>
            <table class="table table-hover table-striped table-bordered" id="exam-teacher-tbody">L
                <tr>
                    <th>ID</th>
                    <th>姓名</th>
                    <th>操作</th>
                </tr>
                <#if examManagerList?has_content>
                    <#list examManagerList as examManagers>
                        <tr class="exam-teacher-info">
                            <td class="exam-teacherid">${examManagers.teacherId!''}</td>
                            <td>${examManagers.teacherName!''}</td>
                            <td><a name="disable-examManager" data-teacher-id="${examManagers.teacherId!''}" role="button" class="btn btn-success" style="font-size: 12px;">取消</a></td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </div>-->
            <div class="tab-pane fade" role="tabpanel" id="without-clazz" aria-labelledby="without-clazz-tab">
                <table class="table table-hover table-striped table-bordered">
                    <tr>
                        <th>姓名</th>
                        <th>学科</th>
                        <th>姓名</th>
                        <th>学科</th>
                        <th>姓名</th>
                        <th>学科</th>
                    </tr>
                    <#if schoolInfoAdminMapper?has_content && schoolInfoAdminMapper.teacherInfoWithoutClazzList?has_content>
                        <#list schoolInfoAdminMapper.teacherInfoWithoutClazzList as teacherInfoWithoutClazz>
                            <#if teacherInfoWithoutClazz_index % 3 = 0>
                            <tr></#if>
                            <td>
                                <a href="/crm/teachernew/teacherdetail.vpage?teacherId=${teacherInfoWithoutClazz.teacherId!''}"
                                   target="_blank">${teacherInfoWithoutClazz.teacherName!''}</a>
                                (ID:${teacherInfoWithoutClazz.teacherId!})
                            <#--是否认证的ico-->
                                <#if teacherInfoWithoutClazz.authenticationState?has_content && teacherInfoWithoutClazz.authenticationState == 1>
                                    <img style="margin-bottom: .2em;" src="/public/img/w-icon-2.png">
                                <#else >
                                    <img style="margin-bottom: .2em;" src="/public/img/w-icon-4.png">
                                </#if>
                            <#--修正假和疑标签 By Wyc 2016-07-04-->
                                <#if teacherInfoWithoutClazz.fakeTeacher?has_content && teacherInfoWithoutClazz.fakeTeacher>
                                    <#if !teacherInfoWithoutClazz.fakeType>
                                        <img style="margin-bottom: .2em;" src="/public/img/w-icon-1.png">
                                    <#else>
                                        <img style="margin-bottom: .2em;" src="/public/img/w-icon-01.png">
                                    </#if>
                                <#else >
                                    <img style="margin-bottom: .2em;" src="/public/img/w-icon-3.png">
                                </#if>
                            </td>
                            <td>${teacherInfoWithoutClazz.subject!}</td>
                            <#if teacherInfoWithoutClazz_index % 3 == 2 || !teacherInfoWithoutClazz_has_next></tr></#if>
                        </#list>
                    </#if>
                </table>
            </div>
            <div class="tab-pane fade active in" role="tabpanel" id="clazz" aria-labelledby="clazz-tab">
                <button id="hide_clazz_btn" class="btn btn-primary" onclick="hideClazz.call(this)">隐藏班级</button>
                <button id="addClazz" class="btn btn-primary">新建班级</button>
                <button id="delClazz" class="btn btn-primary">删除班级</button>

                <table id="clazz_table" class="table table-hover table-striped table-bordered" style="margin-top: 3px;">
                    <tr>
                        <th>班级</th>
                        <th>老师</th>
                        <th>班级</th>
                        <th>老师</th>
                        <th>班级</th>
                        <th>老师</th>
                    </tr>
                    <#if schoolInfoAdminMapper?has_content && schoolInfoAdminMapper.clazzLevelList?has_content>
                        <#list schoolInfoAdminMapper.clazzLevelList as clazzLevelChildList>
                            <#list clazzLevelChildList as clazzInfo>
                                <#if clazzInfo_index % 3 = 0>
                                <tr></#if>
                                <td>
                                    <a href="../clazz/groupinfo.vpage?clazzId=${clazzInfo.id!}" target="_blank">
                                    ${clazzInfo.className!}
                                    </a>(${clazzInfo.id!})
                                </td>
                                <td>
                                    <#if clazzInfo.teacherInfoList?has_content>
                                        <#list clazzInfo.teacherInfoList as teacherInfo>
                                            <ul class="inline">
                                                <li>
                                                    <a href="/crm/teachernew/teacherdetail.vpage?teacherId=${teacherInfo.teacherId!''}" target="_blank">${teacherInfo.teacherName!''}</a> (ID:${teacherInfo.teacherId!}, ${teacherInfo.subject!})
                                                <#--是否认证的ico-->
                                                    <#if teacherInfo.authenticationState?has_content && teacherInfo.authenticationState == 1>
                                                        <img style="margin-bottom: .2em;" src="/public/img/w-icon-2.png">
                                                    <#else >
                                                        <img style="margin-bottom: .2em;" src="/public/img/w-icon-4.png">
                                                    </#if>
                                                <#--修正假和疑标签 By Wyc 2016-07-04-->
                                                    <#if (teacherInfo.fakeTeacher)!false>
                                                        <#if (teacherInfo.fakeType)!false>
                                                            <img style="margin-bottom: .2em;" src="/public/img/w-icon-1.png">
                                                        <#else>
                                                            <img style="margin-bottom: .2em;" src="/public/img/w-icon-01.png">
                                                        </#if>
                                                    <#else >
                                                        <img style="margin-bottom: .2em;" src="/public/img/w-icon-3.png">
                                                    </#if>
                                                </li>
                                            </ul>
                                        </#list>
                                    </#if>
                                </td>
                                <#if clazzInfo_index % 3 = 2 || !clazzInfo_has_next></tr></#if>
                            </#list>
                        </#list>
                    </#if>
                </table>
            </div>
            <div class="tab-pane fade" role="tabpanel" id="custom-clazz" aria-labelledby="custom-clazz-tab">
                <button id="addWalkingClazz" class="btn btn-primary">新建教学班级</button>

                <table id="clazz_table" class="table table-hover table-striped table-bordered">
                    <tr>
                        <th>班级</th>
                        <th>班级</th>
                        <th>班级</th>
                    </tr>
                    <#if schoolInfoAdminMapper.walkingClazzList?has_content>
                        <#list schoolInfoAdminMapper.walkingClazzList as walkingClazzName>
                            <#if walkingClazzName_index % 3 = 0>
                            <tr>
                            </#if>
                            <td>
                                <span>${walkingClazzName}</span>
                                <button class="btn btn-danger v-removeWalkingClazzName">删除</button>
                            </td>
                            <#if walkingClazzName_index % 3 = 2 || !walkingClazzName_has_next>
                            </tr>
                            </#if>
                        </#list>
                    </#if>
                </table>
            </div>
            <div class="tab-pane fade" role="tabpanel" id="without-teacher" aria-labelledby="without-teacher-tab">
                <button id="hide_clazz_btn" class="btn btn-primary" onclick="hideClazz.call(this)">隐藏班级</button>
                <table id="clazz_table" class="table table-hover table-striped table-bordered">
                    <tr>
                        <th>班级</th>
                        <th>班级</th>
                        <th>班级</th>
                    </tr>
                    <#if schoolInfoAdminMapper?has_content && schoolInfoAdminMapper.noGroupsClazzLevelList?has_content>
                        <#list schoolInfoAdminMapper.noGroupsClazzLevelList as clazzLevelChildList>
                            <#list clazzLevelChildList as clazzInfo>
                                <#if clazzInfo_index % 3 = 0>
                                <tr></#if>
                                <td><a href="../clazz/groupinfo.vpage?clazzId=${clazzInfo.id!}">
                                ${clazzInfo.className!}</a>(${clazzInfo.id!})
                                </td>
                                <#if clazzInfo_index % 3 = 2 || !clazzInfo_has_next></tr></#if>
                            </#list>
                        </#list>
                    </#if>
                </table>
            </div>
            <div class="tab-pane fade" role="tabpanel" id="record" aria-labelledby="record-tab">
                <table class="table table-hover table-striped table-bordered">
                    <tr>
                        <th style="width: 65px;">添加人</th>
                        <th style="width: 130px;">创建时间</th>
                        <th>操作</th>
                        <th style="width: 65px;">类型</th>
                    </tr>
                    <#if records??>
                        <#list records as record>
                            <tr>
                                <td>${record.operatorId!''}</td>
                                <td>${record.createTime!''}</td>
                                <td>${(record.operationContent!'')?replace('\n','<br>')}</td>
                                <td><#if record.schoolOperationType??>${record.schoolOperationType.desc!''}</#if></td>
                            </tr>
                        </#list>
                    </#if>
                </table>
            </div>
            <div class="tab-pane fade" role="tabpanel" id="teacher-roles" aria-labelledby="teacher-roles-tab">
                <div style="margin-bottom: 20px">
                    <span>
                        <span>角色:</span>
                        <select id="teacher_roles_level" name="teacher_roles_level" class="input-small" style="width: 120px;margin-bottom: 0">
                            <option selected="selected" value="0">请选择</option>
                            <option value="SCHOOL_MASTER">中学校长</option>
                            <option value="GRADE_MANAGER">年级主任</option>
                            <option value="SUBJECT_LEADER">学科组长</option>
                            <option value="CLASS_MANAGER">班主任</option>
                            <option value="SCHOOL_BANK_MANAGER">校本题库管理员</option>
                            <option value="EXAM_MANAGER">考试管理员</option>
                        </select>
                    </span>
                    <button id="teacher_roles_btn" class="btn btn-primary">查询</button>
                    <a id="add_examManager" href="#add_examManager_dialog" role="button" class="btn btn-primary" data-toggle="modal" style="display: none;">添加考试管理员</a>
                    <a id="add_schoolQuizBankAdministrator" href="#add_schoolQuizBankAdministrator_dialog" role="button" class="btn btn-primary" data-toggle="modal" style="display: none;">添加校本题库管理员</a>
                </div>

                <table id="teacher-roles-table" class="table table-hover table-striped table-bordered">
                    <tr>
                        <th>序号</th>
                        <th>角色名称</th>
                        <th>ID</th>
                        <th>姓名</th>
                        <th>说明</th>
                        <th>操作</th>
                    </tr>

                <#--<#if schoolInfoAdminMapper?has_content && schoolInfoAdminMapper.noGroupsClazzLevelList?has_content>-->
                <#--<#list schoolInfoAdminMapper.noGroupsClazzLevelList as clazzLevelChildList>-->
                <#--<#list clazzLevelChildList as clazzInfo>-->
                <#--<#if clazzInfo_index % 3 = 0>-->
                <#--<tr></#if>-->
                <#--<td><a href="../clazz/groupinfo.vpage?clazzId=${clazzInfo.id!}">-->
                <#--${clazzInfo.className!}</a>(${clazzInfo.id!})-->
                <#--</td>-->
                <#--<#if clazzInfo_index % 3 = 2 || !clazzInfo_has_next></tr></#if>-->
                <#--</#list>-->
                <#--</#list>-->
                <#--</#if>-->
                </table>




            </div>


        </div>
    </div>

    <!----------------------------dialog----------------------------------------------------------------------------------->

    <div style="width:1000px;margin-left:-500px" id="view_evaluate_history" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>学校评分历史</h3>
        </div>
        <div class="modal-body">
            <div id="evaluate_table"></div>
        </div>
        <div class="modal-footer">
            <button class="btn" data-dismiss="modal" aria-hidden="true">关 闭</button>
        </div>
    </div>

    <div style="width:500px;" id="view_change_class" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>班级打散换班</h3>
        </div>
        <div class="modal-body">
            <h4>1.Excel中记录不要超过2000条！！！</h4>
            <form id="change_class_form" action="changeclass.vpage" method="post" enctype="multipart/form-data">
                <input id="excelFile" name="excelFile" type="file"/>
                <input type="hidden" name="schoolId" value="${schoolInfoAdminMapper.schoolId!''}">
                <input type="hidden" id="excelName" name="excelName" value="">
                <button id="change_class_file_submit" type="button" class="btn btn-primary">提交</button>
            </form>
        </div>
        <div class="modal-footer">
            <button class="btn" data-dismiss="modal" aria-hidden="true">关 闭</button>
        </div>
    </div>

    <div id ="add_school_evaluate" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>学校评分</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>地理位置</dt>
                        <dd><input id="placeScore" type="number" placeholder ="输入1-5之间的数字"/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>教学质量</dt>
                        <dd><input id="teachScore" type="number" placeholder ="输入1-5之间的数字"/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>生源水平</dt>
                        <dd><input id="studentScore"  type="number" placeholder ="输入1-5之间的数字"/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>商业潜力</dt>
                        <dd><input id="commercializeScore"  type="number" placeholder ="输入1-5之间的数字"/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>评分备注</dt>
                        <dd><textarea id="evaluate_remark"  cols="200" rows="5"></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="add_school_evaluate_btn_ok" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="update_school" class="modal hide fade" style="width: 920px; margin-left:-460px; ">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>修改学校</h3>
        </div>
        <div class="modal-body">
            <ul class="inline">
                <li>
                    <label for="provinces_school">
                        所在省：
                        <select id="provinces_school" name="provinces_school" class="multiple district_select" next_level="citys_school">
                            <option value="-1">全国</option>
                            <#if provinces??>
                                <#list provinces as p>
                                    <option value="${p.key}">${p.value}</option>
                                </#list>
                            </#if>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="citys_school">
                        所在市：
                        <select id="citys_school" data-init='false' name="citys_school" class="multiple district_select" next_level="countys_school">
                            <option value="-1">全部</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="countys_school">
                        所在区：
                        <select id="countys_school" data-init='false' name="countys_school" class="multiple district_select">
                            <option value="-1">全部</option>
                        </select>
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>
                        主干名称：
                        <input id="cmainname" name="cmainname" type="text"/>
                    </label>
                </li>
                <li>
                    <label>
                        校区名称：
                        <input id="schooldistrict" name="schooldistrict" type="text"/>
                    </label>
                </li>
                <li>
                    <label>
                        学校简称：
                        <input type="text" id="shortname" name="shortname"/>
                    </label>
                </li>
                <li>
                    <label>
                        学校名称：
                        <input id="cname" name="cname" type="text" readonly/>
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label>
                        vip等级：
                        <select id="vip" name="vip" style="width: 210px;">
                            <option value="1">重点学校</option>
                            <option value="2" selected>非重点学校</option>
                        </select>
                    </label>
                </li>
            <#--<li>
                <label for="authenticationState">
                    认证状态
                    <select id="authenticationState" name="authenticationState" class="multiple">
                        <option value="0" selected>等待认证</option>
                        &lt;#&ndash;<option value="1" selected>已认证</option>&ndash;&gt;
                    </select>
                </label>
            </li>-->
                <li>
                    <label for="level">
                        学校级别：
                        <select id="level" name="level" class="multiple" style="width: 210px;">
                            <option value="5">学前</option>
                            <option value="1" selected="">小学</option>
                            <option value="2">中学</option>
                            <option value="4">高中</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="type">
                        学校类型：
                        <select id="type" name="type" class="multiple" style="width: 210px;">
                            <option value="1" selected>公立制学校</option>
                            <option value="2">自定义学校</option>
                            <option value="3">私立学校</option>
                            <option value="5">培训学校</option>
                            <option value="4">虚拟学校</option>
                        </select>
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <label for="schoolDesc">
                        问题描述：
                        <textarea id="schoolDesc" name="schoolDesc" cols="200" rows="5"></textarea>
                    </label>
                </li>
            </ul>
        </div>
        <div class="modal-footer">
            <button id="modifyschoolsubmit" class="btn btn-primary">提 交</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="delete_school_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>删除学校</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>学校 ID</dt>
                        <dd>${schoolInfoAdminMapper.schoolId!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>学校名字</dt>
                        <dd>${schoolInfoAdminMapper.schoolName!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>问题描述</dt>
                        <dd><textarea id="deleteDesc" cols="35" rows="4"></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="delete_school_dialog_btn_ok" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="update_ambassador_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>添加校园大使</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>学校 ID</dt>
                        <dd>${schoolInfoAdminMapper.schoolId!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>学校名字</dt>
                        <dd>${schoolInfoAdminMapper.schoolName!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>校园大使</dt>
                        <dd><input id="school_ambassador" value=""/></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="update_school_ambassador_btn_ok" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="merge_school_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>合并学校</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>学校 ID</dt>
                        <dd>${schoolInfoAdminMapper.schoolId!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>学校名字</dt>
                        <dd>${schoolInfoAdminMapper.schoolName!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>被合并的学校ID</dt>
                        <dd><input id="source_school"/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>问题描述</dt>
                        <dd><textarea id="mergeDesc" cols="35" rows="3"></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="merge_school_dialog_btn_ok" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="addNewClazz" class="modal hide fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h3 class="modal-title">新建班级</h3>
                </div>
                <div class="modal-body">
                    <ul class="inline">
                        <li>批量添加班级,请去"网站管理-批量功能"</li>
                    </ul>
                </div>
                <div class="modal-body" id="scid">
                    <ul class="inline">
                        <li>年级列表:</li>
                        <li>
                            <select id="clazz_level" name="clazz_level" class="input-small" style="width: 100px">
                                <#if schoolInfoAdminMapper.schoolLevel == "小学" >
                                    <#list clazzLevels as clazzLevel>
                                        <#if clazzLevel.key lte 6>
                                            <option value="${clazzLevel.key!}">${clazzLevel.value!}</option>
                                        </#if>
                                    </#list>
                                <#elseif schoolInfoAdminMapper.schoolLevel == "中学">
                                    <#list clazzLevels as clazzLevel>
                                        <#if clazzLevel.key lte 9 && clazzLevel.key gte 6 >
                                            <option value="${clazzLevel.key!}">${clazzLevel.value!}</option>
                                        </#if>
                                    </#list>
                                <#elseif schoolInfoAdminMapper.schoolLevel == "高中">
                                    <#list clazzLevels as clazzLevel>
                                        <#if clazzLevel.key lte 13 && clazzLevel.key gte 11 >
                                            <option value="${clazzLevel.key!}">${clazzLevel.value!}</option>
                                        </#if>
                                    </#list>
                                <#elseif schoolInfoAdminMapper.schoolLevel == "学前">
                                    <#list clazzLevels as clazzLevel>
                                        <#if clazzLevel.key lte 54 && clazzLevel.key gte 51 >
                                            <option value="${clazzLevel.key!}">${clazzLevel.value!}</option>
                                        </#if>
                                    </#list>
                                </#if>
                            </select>
                            <input type="text" id="newClazzName" name="newClazzName" style="width: 100px;">&nbsp;班
                        </li>
                    </ul>
                </div>

                <div class="modal-footer" id="edb">
                    <input type="hidden" id="addClazzSchoolId" name="addClazzSchoolId" value="${schoolInfoAdminMapper.schoolId!''}">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="btn_add_new_clazz_submit" type="button" class="btn btn-primary">提交</button>
                </div>
            </div>
        </div>
    </div>

    <div id="delDupClazz" class="modal hide fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h3 class="modal-title">删除班级</h3>
                </div>

                <div class="modal-body">
                    <ul class="inline">
                        <li>请输入要删除的班级ID:</li>
                        <li><input type="text" id="delDupClazzId"></li>
                    </ul>
                </div>


                <div class="modal-footer" id="edb">
                    <input type="hidden" id="delClazzSchoolId" name="delClazzSchoolId" value="${schoolInfoAdminMapper.schoolId!''}">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="btn_del_dup_clazz_submit" type="button" class="btn btn-primary">提交</button>
                </div>
            </div>
        </div>
    </div>

    <div id="addNewWalkingClazz" class="modal hide fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h3 class="modal-title">新建教学班级</h3>
                </div>
                <div class="modal-body" id="scid">
                    <ul class="inline">
                        <li>年级列表:</li>
                        <li>

                            <#if schoolInfoAdminMapper.schoolLevel == "高中">
                                <select id="walkingclazz_level" name="clazz_level" class="input-small"
                                        style="width: 100px">
                                    <#list clazzLevels as clazzLevel>
                                        <#if clazzLevel.key lte 13 && clazzLevel.key gte 11 >
                                            <option value="${clazzLevel.key!}">${clazzLevel.value!}</option>
                                        </#if>
                                    </#list>
                                </select>
                                <select id="walkingclazz_subject" name="clazz_level" class="input-small"
                                        style="width: 100px">
                                    <option value="MATH">数学</option>
                                    <option value="PHYSICS">物理</option>
                                    <option value="CHEMISTRY">化学</option>
                                    <option value="BIOLOGY">生物</option>
                                </select>
                            <#else >
                                <select id="walkingclazz_level" name="clazz_level" class="input-small" style="width: 100px">
                                    <#list clazzLevels as clazzLevel>
                                        <#if clazzLevel.key lte 9 && clazzLevel.key gte 6 >
                                            <option value="${clazzLevel.key!}">${clazzLevel.value!}</option>
                                        </#if>
                                    </#list>
                                </select>
                                <select id="walkingclazz_subject" name="clazz_level" class="input-small" style="width: 100px">
                                    <option value="ENGLISH">英语</option>
                                    <option value="MATH">数学</option>
                                    <option value="CHINESE">语文</option>
                                    <option value="MATH">数学</option>
                                    <option value="PHYSICS">物理</option>
                                    <option value="CHEMISTRY">化学</option>
                                    <option value="BIOLOGY">生物</option>
                                </select>
                            </#if>
                            <input type="text" id="newWalkingClazzName" name="newWalkingClazzName" style="width: 100px;">&nbsp;班
                        </li>
                    </ul>
                </div>

                <div class="modal-footer" id="edb">
                    <input type="hidden" id="addClazzSchoolId" name="addClazzSchoolId" value="${schoolInfoAdminMapper.schoolId!''}">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="btn_add_new_walking_clazz_submit" type="button" class="btn btn-primary">提交</button>
                </div>
            </div>
        </div>
    </div>

    <div id="set_clazz_count" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>修改班级人数上限</h3>
        </div>
        <div class="modal-body">
            <form id="clazz_count" name="clazz_count">
                <dl class="dl-horizontal">
                    <ul class="inline">
                        <li>
                            <dt>学校 ID</dt>
                            <dd>${schoolInfoAdminMapper.schoolId!''}</dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>学校名字</dt>
                            <dd>${schoolInfoAdminMapper.schoolName!''}</dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>班级人数</dt>
                            <dd><input id="studentCount" name="studentCount" value="" placeholder="班级人数必须在100-200之间"></dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>备注</dt>
                            <dd><textarea id="comment" name="comment" value="" placeholder="必填信息，请记录下是哪一位市场人员提出的修改需求" cols="35" rows="3"></textarea></dd>
                        </li>
                    </ul>
                </dl>
            </form>
        </div>
        <div class="modal-footer">
            <button id="set_clazz_count_btn_ok" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="modify_edusystem_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>修改学校学制</h3>
        </div>
        <div class="modal-body dl-horizontal">
            <dl class="inline">
                <dt>学校学制</dt>
                <dd>
                    <#assign schoolLevel = (schoolInfoAdminMapper.schoolLevelValue)/>
                    <select id="schoolEduSystem">
                        <#switch schoolLevel>
                            <#case 1>
                                <option value="P6" <#if (schoolInfoAdminMapper.schoolEduSystem) == 'P6'> selected </#if>>小学六年制</option>
                                <option value="P5" <#if (schoolInfoAdminMapper.schoolEduSystem) == 'P5'> selected </#if>>小学五年制</option>
                                <#break/>
                            <#case 2>
                                <option value="J3" <#if (schoolInfoAdminMapper.schoolEduSystem) == 'J3'> selected </#if>>初中三年制</option>
                                <option value="J4" <#if (schoolInfoAdminMapper.schoolEduSystem) == 'J4'> selected </#if>>初中四年制</option>
                                <#break/>
                            <#case 4>
                                <option value="S3" <#if (schoolInfoAdminMapper.schoolEduSystem) == 'S3'> selected </#if>>高中三年制</option>
                                <option value="S4" <#if (schoolInfoAdminMapper.schoolEduSystem) == 'S4'> selected </#if>>高中四年制</option>
                                <#break/>
                        </#switch>
                    </select>
                </dd>
            </dl>
            <dl class="inline">
                <dt>备注</dt>
                <dd><textarea id="eduSysDesc" name="eduSysDesc"  placeholder="必填信息，请交代修改的动机" cols="35" rows="3"></textarea></dd>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="modify_edusystem_btn_ok" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="modify_daite_school_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>修改戴特合作校</h3>
        </div>
        <div class="modal-body dl-horizontal">
            <dl class="inline">
                <dt>戴特合作校</dt>
                <dd>
                    <select id="daiteSchoolFlag">
                        <option value="1">是</option>
                        <option value="0">否</option>
                    </select>
                </dd>
            </dl>
            <dl class="inline">
                <dt>备注</dt>
                <dd><textarea id="daiteSchoolDesc" name="daiteSchoolDesc"  placeholder="必填信息，请交代修改的动机" cols="35" rows="3"></textarea></dd>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="modify_daite_school_btn_ok" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="modify_adjust_clazz_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>自主调整班级开关</h3>
        </div>
        <div class="modal-body dl-horizontal">
            <dl class="inline">
                <dt>状态</dt>
                <dd id = "adjustClazzFlag">
                    <label class="radio-inline">
                        <input type="radio" name="adjustClazzFlag" value="true" <#if adjustClazz>checked</#if>> 开
                        &nbsp; &nbsp; &nbsp;
                        <input type="radio" name="adjustClazzFlag"  value="false" <#if !adjustClazz>checked</#if>> 关
                    </label>
                </dd>
            </dl>
            <dl class="inline">
                <dt>备注</dt>
                <dd><textarea id="adjustClazzDesc" name="adjustClazzDesc"  placeholder="必填信息，请交代修改的动机" cols="35" rows="3"></textarea></dd>
            </dl>

            <dl class="inline">
                <dd style="color: red">关闭时校内老师可以自主建班，开启时老师不可自主建班</dd>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="modify_adjust_clazz_btn_ok" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="allow_klx_privilege_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>开通快乐学学校权限</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt class="text-left">学校 ID</dt>
                        <dd>${schoolInfoAdminMapper.schoolId!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt class="text-left">学校名字</dt>
                        <dd>${schoolInfoAdminMapper.schoolName!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li style="display:block; margin-bottom: 7px;">
                        <dt class="text-left">阅卷机权限</dt>
                        <dd><input type="checkbox" name="school_klx_privilege" value="scanMachineFlag" <#if (schoolKlxPrivilegeInfo?has_content && schoolKlxPrivilegeInfo.scanMachineFlag?has_content && schoolKlxPrivilegeInfo.scanMachineFlag == true)!false>checked="checked"</#if>/></dd>
                    </li>

                    <li style="display:block; margin-bottom: 7px;">
                        <dt class="text-left">题卡合一答题卡权限</dt>
                        <dd><input type="checkbox" name="school_klx_privilege" value="questionCardFlag" <#if (schoolKlxPrivilegeInfo?has_content && schoolKlxPrivilegeInfo.questionCardFlag?has_content && schoolKlxPrivilegeInfo.questionCardFlag == true)!false>checked="checked"</#if> /></dd>
                    </li>
                    <li style="display:block; margin-bottom: 7px;">
                        <dt class="text-left">条形码答题卡权限</dt>
                        <dd><input type="checkbox" name="school_klx_privilege" value="barcodeAnswerQuestionFlag" <#if (schoolKlxPrivilegeInfo?has_content && schoolKlxPrivilegeInfo.barcodeAnswerQuestionFlag?has_content && schoolKlxPrivilegeInfo.barcodeAnswerQuestionFlag == true)!false>checked="checked"</#if> /></dd>
                    </li>
                    <li style="display:block; margin-bottom: 7px;">
                        <dt class="text-left">校本题库权限</dt>
                        <dd><input type="checkbox" name="school_klx_privilege" value="questionBankFlag" <#if (schoolKlxPrivilegeInfo?has_content && schoolKlxPrivilegeInfo.questionBankFlag?has_content && schoolKlxPrivilegeInfo.questionBankFlag == true)!false>checked="checked"</#if> /></dd>
                    </li>
                    <li style="display:block; margin-bottom: 7px;">
                        <dt class="text-left">A3答题卡权限</dt>
                        <dd><input type="checkbox" name="school_klx_privilege" value="a3AnswerQuestionFlag" <#if (schoolKlxPrivilegeInfo?has_content && schoolKlxPrivilegeInfo.a3AnswerQuestionFlag?has_content && schoolKlxPrivilegeInfo.a3AnswerQuestionFlag == true)!false>checked="checked"</#if> /></dd>
                    </li>
                    <li style="display:block; margin-bottom: 7px;">
                        <dt class="text-left">手写答题卡权限</dt>
                        <dd><input type="checkbox" name="school_klx_privilege" value="manualAnswerQuestionFlag" <#if (schoolKlxPrivilegeInfo?has_content && schoolKlxPrivilegeInfo.manualAnswerQuestionFlag?has_content && schoolKlxPrivilegeInfo.manualAnswerQuestionFlag == true)!false>checked="checked"</#if> /></dd>
                    </li>
                    <#if schoolKlxPrivilegeInfo?has_content && (schoolKlxPrivilegeInfo.subjects)?has_content>
                        <li style="margin-bottom: 7px;">
                            <dt class="text-left">学科权限</dt>
                            <dd>
                                <#assign subSize = schoolKlxPrivilegeInfo.subjects?size/>
                                <#list schoolKlxPrivilegeInfo.subjects as subject>
                                    <input style="margin-right: 2px;" type="checkbox" name="klx_subject" class="klx_valid_subject" value="${subject.value!}" <#if subject.checked> checked </#if> <#if subject.disabled> disabled</#if>/><span style="margin: 0 8px;">${subject.name!}</span>
                                    <#if subject_index+1 == subSize / 2> <br/></#if>
                                </#list>
                            </dd>
                        </li>
                    </#if>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="allow_klx_privilege_dialog_button" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="query_klx_scannumber_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>填涂号占用查询</h3>
        </div>
        <div class="modal-body">
            <form id="clazz_count" name="clazz_count">
                <dl class="dl-horizontal">
                    <ul class="inline">
                        <li>
                            <dt>学校 ID</dt>
                            <dd>${schoolInfoAdminMapper.schoolId!''}</dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>学校名字</dt>
                            <dd>${schoolInfoAdminMapper.schoolName!''}</dd>
                        </li>
                    </ul>
                    <ul class="inline">
                        <li>
                            <dt>填涂号</dt>
                            <dd><input id="query_scanmber" name="query_scanmber" value="" placeholder="填涂号5-11位数字(例如123344)" minlength="5" maxlength="11" type="text"></dd>
                        </li>
                    </ul>

                    <ul class="inline">
                        <li>
                            <dd><div id="query_scanmber_result"></div></dd>
                        </li>
                    </ul>
                </dl>
            </form>
        </div>
        <div class="modal-footer">
            <button id="query_klx_scannumber_dialog_ok" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

</div>

<div id="add_schoolQuizBankAdministrator_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>添加校本题库管理员</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>学校 ID</dt>
                    <dd>${schoolInfoAdminMapper.schoolId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>学校名字</dt>
                    <dd>${schoolInfoAdminMapper.schoolName!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>校本题库管理员</dt>
                    <dd><input id="school_QuizBankAdministrator" value=""/></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="add_schoolQuizBankAdministrator_dialog_button" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="add_examManager_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>管理员</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>学校 ID</dt>
                    <dd>${schoolInfoAdminMapper.schoolId!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>学校名字</dt>
                    <dd>${schoolInfoAdminMapper.schoolName!''}</dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>老师ID</dt>
                    <dd><input id="exam_manager_id" value=""/></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="add_examManager_dialog_button" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="update-auth-state" class="modal hide fade" style="width: 500px; margin-left:-460px; ">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>修改鉴定状态</h3>
    </div>
    <div class="modal-body">
        <ul class="inline">
            <li>
                <label for="iAuthenticationState">
                    鉴定状态
                    <select id="iAuthenticationState" name="authenticationState" class="multiple">
                        <option value="0" selected>待鉴定</option>
                        <option value="1" selected>鉴定通过</option>
                        <option value="3" selected>未通过</option>
                    </select>
                </label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <label for="iSchoolDesc">
                    备注：
                    <textarea id="iSchoolDesc" name="schoolDesc" cols="200" rows="5"></textarea>
                </label>
            </li>
        </ul>
    </div>
    <div class="modal-footer">
        <button class="btn btn-primary" onclick="updateAuthStateSubmit()">提 交</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="show-reject-receive-dialog" class="modal hide fade" >
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>拒收奖品名单</h3>
    </div>
    <div class="modal-body">

    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>
    </div>
</div>

<div id="similar-school-confirm" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>确认相似学校</h3>
    </div>
    <div class="modal-body">
        <div class="box-content">
            <div id="similarschoolinfo"></div>
        </div>

        <div class="modal-footer">
            <button id="modify_similar_school_also" type="submit" class="btn btn-primary">仍然修改</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>

    </div>
    <script type="text/html" id="T:SIMILAR-SCHOOLINFO">
        <table>
            <tr>
                <td>学校ID</td>
                <td>学校名称</td>
            </tr>
            <%for(var i = 0; i < existSchoolList.length; i++){%>
            <tr>
                <td>
                    <a target="_blank" href="schoolhomepage.vpage?schoolId=<%=existSchoolList[i].schoolId%>"><%=existSchoolList[i].schoolId%></a>
                </td>
                <td><%=existSchoolList[i].schoolName%></td>
            </tr>
            <%}%>

        <#--<#if existSchoolMap??&& existSchoolMap?has_content>-->
        <#--<#list existSchoolMap?keys as schoolId>-->
        <#--<tr>-->
        <#--<td>${schoolId}</td>-->
        <#--<td><a target="_blank"-->
        <#--href="schoolhomepage.vpage?schoolId=${schoolId!}">${existSchoolMap[schoolId]!''}</a>-->
        <#--</td>-->
        <#--</tr>-->
        <#--</#list>-->
        <#--</#if>-->
        </table>
    </script>
</div>

<div id="openEduAdminAccount_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>开通教务账号</h3>
    </div>
    <div class="modal-body">
        <div class="form-horizontal">
            <div class="control-group">
                <label class="control-label" for="">老师姓名</label>
                <div class="controls">
                    <input type="text" id="eduAdminTeacherName" placeholder="请输入6位以内汉字" maxlength="6">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="">手机号</label>
                <div class="controls">
                    <input type="text" id="eduAdminTeacherPhone" placeholder="请输入11位数手机号" maxlength="11">
                </div>
            </div>
            <div class="control-group">
                <p id="eduErrorInfo" style="text-align: center;font-size: 13px;color: #c32b2b;"></p>
            </div>
        </div>
    </div>
    <div class="modal-footer">
        <button id="eduAdminSureBtn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="openEduAdminMobile_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>变更教务老师手机号</h3>
    </div>
    <div class="modal-body">
        <div class="form-horizontal">
            <div class="control-group">
                <label class="control-label" for="">手机号</label>
                <div class="controls">
                    <input type="text" id="affairTeacherPhone" placeholder="请输入11位数手机号" maxlength="11">
                </div>
                <input type="hidden" id="affairTeacherId">
            </div>
            <div class="control-group">
                <p id="affairErrorInfo" style="text-align: center;font-size: 13px;color: #c32b2b;"></p>
            </div>
        </div>
    </div>
    <div class="modal-footer">
        <button id="affairAdminSureBtn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>

<div id="setScanNumber_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>设置学校填涂号位数</h3>
    </div>
    <div class="modal-body">
        <div class="form-horizontal">
            <div class="control-group">
                <label class="control-label" for="">填涂号位数</label>
                <div class="controls">
                    <select id="scanNumberDigit">
                        <option value="5">五位填涂号 </option>
                        <option value="6">六位填涂号 </option>
                        <option value="7">七位填涂号 </option>
                        <option value="8">八位填涂号 </option>
                        <option value="9">九位填涂号 </option>
                        <option value="10">十位填涂号 </option>
                        <option value="11">十一位填涂号 </option>
                    </select>
                </div>
            </div>
        </div>
    </div>
    <div class="modal-footer">
        <button id="setScanNumberBtn" class="btn btn-primary">确 定</button>
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
                    <dd><span id="affairid_login"></span></dd>
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
                    <dd><span id="affairId"></span></dd>
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

<#-- 高德地图标记学校坐标 -->
<div id="schoolmap_dialog" class="modal hide fade" style="width:800px;margin-left:-400px">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>标记学校坐标</h3>
    </div>
    <div class="modal-body">
        <div class="schoolMapBox">
            <div id="schoolMap"></div>
            <div class="schoolMapSearch">
                <div class="input-item">
                    <span class="input-item-text">请输入关键词</span>
                    <input id="tipinput" type="text">
                </div>
            </div>
        </div>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        <button id="update_school_map" class="btn btn-primary">确 定</button>
    </div>
</div>


<script type="text/html" id="school_evaluate_info">
    <table id="alert_div" class="table table-hover table-striped table-bordered">
        <tr>
            <td>评价时间</td>
            <td>评价人</td>
            <td>地理位置</td>
            <td>教学质量</td>
            <td>生源质量</td>
            <td>商业潜力</td>
            <td>评分备注</td>
        </tr>
        <%for(var i = 0, len = list.length; i < len; i++){%>
        <tr>
            <td><%=list[i].date%></td>
            <td><%=list[i].accountName%></td>
            <td><%=list[i].placeScore%></td>
            <td><%=list[i].teachScore%></td>
            <td><%=list[i].studentScore%></td>
            <td><%=list[i].commercializeScore%></td>
            <td title="<%=list[i].remark%>"><span style="width:100px; height:20px; display:block;overflow: hidden;"><%=list[i].remark%></span></td>
        </tr>
        <%}%>
    </table>
</script>
    <#include "../specialschool.ftl" />
<script>
    function unLockedSchool() {
        var schoolId = ${schoolInfoAdminMapper.schoolId!''};
        if (confirm("是否解锁这个学校")) {
            $.post("un_locked_school.vpage", {schoolId: schoolId}, function (res) {
                if (res.success) {
                    alert("解锁成功");
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            })
        }
    }

    function hideClazz() {
        var $btn = $(this);
        var $clazzTable = $btn.siblings("#clazz_table");
        console.log($clazzTable);
        $clazzTable.toggle(function () {
            switch ($btn.html()) {
                case "显示班级":
                    $btn.html("隐藏班级");
                    break;
                case "隐藏班级":
                    $btn.html("显示班级");
                    break;
            }
        });
    }

    function clearNextLevel(obj) {
        if (obj.attr("next_level")) {
            clearNextLevel($("#" + obj.attr("next_level")).html('<option value=""></option>'));
        }
    }

    function updateSchool() {
        $("#provinces_school").val(${schoolInfoAdminMapper.provinces!"-1"});
        $('#citys_school').data('init', false);
        $('#countys_school').data('init', false);
        $('#provinces_school').trigger('change');
        $('#cmainname').val("${schoolInfoAdminMapper.cmainName!''}");
        $('#schooldistrict').val("${schoolInfoAdminMapper.schoolDistrict!''}");
        $('#cname').val("${schoolInfoAdminMapper.schoolName!''}");
        $('#shortname').val("${schoolInfoAdminMapper.schoolShortName!''}");
        $('#vip').val("${schoolInfoAdminMapper.vipLevel!''}");
        $('#authenticationState').val(${schoolInfoAdminMapper.authenticationStateValue!''});
        $('#level').val(${schoolInfoAdminMapper.schoolLevelValue!''});
        $('#type').val(${schoolInfoAdminMapper.schoolTypeValue!''});
        $('#schoolDesc').val('');

        $("#update_school").modal("show");
    }

    function updateSchoolSubmit() {
        $.ajax({
            type: 'post',
            url: 'updateschool.vpage',
            data: {
                schoolId: ${schoolInfoAdminMapper.schoolId!''},
                countys: $('#countys_school').val(),
                cmainname : $('#cmainname').val(),
                schooldistrict : $('#schooldistrict').val(),
                cname: $('#cname').val(),
                shortname: $('#shortname').val(),
                vip: $('#vip').val(),
                authenticationState: $('#authenticationState').val(),
                level: $('#level').val(),
                type: $('#type').val(),
                schoolDesc: $('#schoolDesc').val()
            },
            success: function (data) {
                if (data.success)
                    window.location.href = 'schoolhomepage.vpage?schoolId=' + ${schoolInfoAdminMapper.schoolId!''};
                else
                    alert('修改失败，请正确设置各参数，并记录日志。');
            }
        });
    }

    function updateAuthState() {
        $('#iAuthenticationState').val(${schoolInfoAdminMapper.authenticationStateValue!''});
        $('#iSchoolDesc').val('');
        $("#update-auth-state").modal("show");
    }

    function updateAuthStateSubmit() {
        $.ajax({
            type: 'post',
            url: 'updateauthstate.vpage',
            data: {
                schoolId: ${schoolInfoAdminMapper.schoolId!''},
                authenticationState: $('#iAuthenticationState').val(),
                schoolDesc: $('#iSchoolDesc').val()
            },
            success: function (data) {
                if (data.success) {
                    window.location.href = 'schoolhomepage.vpage?schoolId=' + ${schoolInfoAdminMapper.schoolId!''};
                } else {
                    alert('修改失败，请正确设置各参数，并记录日志。');
                }
            }
        });
    }

    function checkSchoolScanNumber(schoolId,scanNumber) {
        $.post("checkschoolscannumber.vpage", {schoolId: schoolId,scanNumber:scanNumber}, function (data) {
            if (data.success) {
                if(data.containFlag){
                    alert("学校"+schoolId+"有填涂号:"+scanNumber);
                }else{
                    alert("学校"+schoolId+"没有填涂号:"+scanNumber);
                }
            } else {
                alert(data.info);
            }
        });
    }

    function removeScanNumberFromSchool(schoolId,scanNumber) {
        $.post("removescannumberfromschool.vpage", {schoolId: schoolId,scanNumber:scanNumber}, function (data) {
            if (data.success) {
                alert("清除成功!")
            } else {
                alert(data.info);
            }
        });
    }

    $(function () {
        $("#delete_school_dialog_btn_ok").on("click", function () {
            $.ajax({
                type: 'post',
                url: 'deleteschool.vpage',
                data: {
                    schoolId: ${schoolInfoAdminMapper.schoolId!""},
                    deleteDesc: $('#deleteDesc').val()
                },
                success: function (data) {
                    if (data.success) {
                        window.location.href = 'schoollist.vpage'
                    } else {
                        alert(data.info);
//                        alert("删除失败，请填写问题描述，并检查学校中是否还存在未合并班级。");
                    }
                }
            });
        });

        $("#update_school_ambassador_btn_ok").on("click", function () {
            if (!/^[0-9]*$/.test($('#school_ambassador').val())) {
                alert("请输入正确的用户ID!");
                return;
            }
            $.ajax({
                type: 'post',
                url: 'addschoolambassador.vpage',
                data: {
                    schoolId: ${schoolInfoAdminMapper.schoolId!""},
                    ambassadorUserId: $('#school_ambassador').val()
                },
                success: function (data) {
                    if (data.success) {
                        window.location.href = 'schoolhomepage.vpage?schoolId=' + ${schoolInfoAdminMapper.schoolId!''};
                    } else {
                        alert(data.info);
                    }
                }
            });
        });

        $("a[name='disable-ambassador']").on('click', function () {
            if (confirm("确定要取消该校园大使吗？")) {
                var ambassadorId = $(this).attr("data-ambassador-id");
                $.post("disabledambassador.vpage", {ambassadorId: ambassadorId, schoolId:${schoolInfoAdminMapper.schoolId!""}}, function (data) {
                    if (data.success) {
                        window.location.href = 'schoolhomepage.vpage?schoolId=' + ${schoolInfoAdminMapper.schoolId!''};
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        $('#merge_school_dialog_btn_ok').on('click', function () {
            var sourceSchoolId = $('#source_school').val();
            /*
           35204 北京市朝阳区银座十号小学

           353246 银座九号小学

           393213 银座九号小学

           377137 北京市朝阳区阳明广场小学

           2000 一起作业体验学校
            */
            var fakeSchoolIds = ["35204", "353246", "393213", "377137", "2000"];
            if ($.inArray(sourceSchoolId, fakeSchoolIds) >= 0) {
                alert("禁止将老师移动到此学校，如果老师确实为假老师，请事先做老师的判假处理！");
                return;
            }

            $.post("mergeschoolprecheck.vpage", {targetSchoolId: ${schoolInfoAdminMapper.schoolId!''}, sourceSchoolId: sourceSchoolId, mergeDesc: $('#mergeDesc').val()}, function (data) {
                if (data.success) {
                    var result = window.confirm("合并主校名:"+data.targetSchoolName+"\n合并副校名:"+data.sourceSchoolName+"\n\n提示: 合并后,副校完善的信息将合并至主校");
                    if (result) {
                        $.ajax({
                            type: 'post',
                            url: 'mergeschool.vpage',
                            data: {
                                targetSchoolId: ${schoolInfoAdminMapper.schoolId!''},
                                sourceSchoolId: sourceSchoolId,
                                mergeDesc: $('#mergeDesc').val()
                            },
                            success: function (data) {
                                if (data.success) {
                                    window.location.href = 'schoolhomepage.vpage?schoolId=' + ${schoolInfoAdminMapper.schoolId!''};
                                } else {
                                    alert(data.info);
                                }
                            }
                        });
                    } else {
                        window.location.href = 'schoolhomepage.vpage?schoolId=' + ${schoolInfoAdminMapper.schoolId!''};
                    }
                } else {
                    alert(data.info);
                }
            });
        });

        $(".district_select").on("change", function () {
            var html = null;
            var $this = $(this);
            var next_level = $this.attr("next_level");
            var regionCode = $this.val();
            if (next_level) {
                var codeType = next_level;
                next_level = $("#" + next_level);
                clearNextLevel($this);
                $.ajax({
                    type: "post",
                    url: "regionlist.vpage",
                    data: {
                        regionCode: regionCode
                    },
                    success: function (data) {
                        html = '';
                        var regionList = data.regionList;
                        for (var i in regionList) {
                            html += '<option value="' + regionList[i]["code"] + '">' + regionList[i]["name"] + '</option>';
                        }
                        next_level.html(html);
                        if (codeType == 'citys_school' && !next_level.data('init')) {
                            next_level.val(${schoolInfoAdminMapper.citys!'-1'});
                            next_level.data('init', true);
                        } else if (codeType == 'countys_school' && !next_level.data('init')) {
                            next_level.val(${schoolInfoAdminMapper.countys!'-1'});
                            next_level.data('init', true);
                        }
                        next_level.trigger('change');
                    }
                });
            }
        });

        $("#evaluate_history").on("click",function(){
            var schoolId = ${schoolInfoAdminMapper.schoolId!''};
            $.get("find_school_evaluate.vpage?schoolId="+schoolId,function(res){
                if(res.success){
                    $("#evaluate_table").html(template("school_evaluate_info",{list:res.data}));
                }else{
                    alert(res.info);
                }
            });
            $("#view_evaluate_history").modal('show');
        });

        $("#change_class").on("click",function(){
            $("#view_change_class").modal('show');
        });

        //选择文件
        $(document).on('click','#change_class_file_submit',function () {
            var that = this,
                    files = $("#excelFile")[0].files,
                    _index = $(that).attr("index");
            if(files.length >0){
                var file = files[0];
                var fileName = file.name;
                if(fileName.indexOf('.xls') == -1 || fileName.indexOf('.xlsx') == -1){
                    alert("请选择Excel文件");
                    return false;
                }else{
                    fileName = fileName.split(".")[0];
                    $("#excelName").val(fileName);
                    $("#change_class_form").submit();
                }
            }
        });

        $("#school_evaluate").on("click",function(){
            $("#add_school_evaluate").modal('show');
        });

        $("#merge_school").on("click", function () {
            $('#source_school').val('');
            $('#mergeDesc').val('');

            $('#merge_school_dialog').modal('show');
        });

        $("#modifyEduSystem").on("click", function () {
            $('#eduSysDesc').val('');
            $('#modify_edusystem_dialog').modal('show');
        });

        $("#modify_edusystem_btn_ok").on('click', function() {
            var desc = $('#eduSysDesc').val();
            var eduSys = $('#schoolEduSystem').val();

            if (eduSys == '' || desc == '') {
                alert("请选择和填写相应内容");
                return false;
            }

            var data = {
                schoolId : ${schoolInfoAdminMapper.schoolId!''},
                eduSys: eduSys,
                desc: desc
            };

            $.post('modifyedusystem.vpage', data, function(res) {
                if (!res.success) {
                    if (res.errorCode != '004') {
                        alert("学制修改失败：" + res.info);
                        return false;
                    }
                    if (!confirm(res.info)) {
                        return false;
                    }
                    data.confirmCode = "confirm";
                    $.post('modifyedusystem.vpage', data, function(res) {
                        if (res.success) {
                            alert("学制修改成功");
                            window.location.reload();
                        } else {
                            alert("学制修改失败：" + res.info);
                        }
                    });
                }  else if (confirm("是否确认修改学校学制？")) {
                    data.confirmCode = "confirm";
                    $.post('modifyedusystem.vpage', data, function(res) {
                        if (res.success) {
                            alert("学制修改成功");
                            window.location.reload();
                        } else {
                            alert("学制修改失败：" + res.info);
                        }
                    });
                }
            });

        });

        $("#modifyDaite").on("click", function () {
            $('#daiteSchoolDesc').val('');
            $('#modify_daite_school_dialog').modal('show');
        });

        $("#modifyAdjustClazz").on("click", function () {
            $('#adjustClazzDesc').val('');
            $('#modify_adjust_clazz_dialog').modal('show');
        });

        $("#modify_daite_school_btn_ok").on('click', function() {
            var daiteDesc = $('#daiteSchoolDesc').val();
            var daiteFlag = $('#daiteSchoolFlag').val();

            if (daiteDesc == '' || daiteFlag == '') {
                alert("请选择和填写相应内容");
                return false;
            }

            var data = {
                schoolId : ${schoolInfoAdminMapper.schoolId!''},
                daiteFlag: daiteFlag,
                daiteDesc: daiteDesc
            };

            $.post('modifydaiteschool.vpage', data, function(res) {
                if (res.success) {
                    alert("合作校修改成功");
                    window.location.reload();
                } else {
                    alert("合作校修改成功：" + res.info);
                }
            });

        });

        $("#modify_adjust_clazz_btn_ok").on('click', function() {
            var adjustClazzDesc = $('#adjustClazzDesc').val();
            var adjustClazzFlag = $('#adjustClazzFlag input[name="adjustClazzFlag"]:checked ').val();
            if (!adjustClazzDesc) {
                alert("请选择和填写相应内容");
                return false;
            }

            var data = {
                schoolId : ${schoolInfoAdminMapper.schoolId!''},
                adjustClazzFlag: adjustClazzFlag,
                adjustClazzDesc: adjustClazzDesc
            };

            $.post('modifyadjustclazz.vpage', data, function(res) {
                if (res.success) {
                    alert("修改成功");
                    $("#modify_adjust_clazz_dialog").hide();
                    window.location.reload();
                } else {
                    alert("修改成功：" + res.info);
                }
            });
        });

        $("#addClazz").on("click", function () {
            $("#newClazzName").val();
            $("#addNewClazz").modal("show");
        });

        $("#delClazz").on("click", function () {
//            $("#newClazzName").val();
            $("#delDupClazz").modal("show");
        });

        $("#addWalkingClazz").on("click", function () {
            $("#newWalkingClazzName").val();
            $("#addNewWalkingClazz").modal("show");
        });

        $("#btn_add_new_clazz_submit").on("click", function () {
            var thisSchoolId = $("#addClazzSchoolId").val();
            var clazzName = $("#newClazzName").val() + "班";
            var clazzLevel = $('#clazz_level').find('option:selected').val();
            if (!checkSpecialSchool()) {
                return false;
            }
            $.ajax({
                type: "post",
                url: "addsysclazz.vpage",
                data: {
                    schoolId: thisSchoolId,
                    clazzName: clazzName,
                    clazzLevel: clazzLevel
                },
                success: function (data) {
                    if (data.success) {
                        alert(data.info);
                        window.location.href = "/crm/school/schoolhomepage.vpage?schoolId=" + thisSchoolId;
                    } else {
                        alert(data.info);
                    }

                }
            });
        });

        $('#btn_del_dup_clazz_submit').on("click",function(){
            var delClazzSchoolId = $("#delClazzSchoolId").val();
            var delDupClazzId = $('#delDupClazzId').val();
//            var postData = {
//                clazzId : $('#delDupClazzId').val()
//            };
            if (!checkSpecialSchool()) {
                return false;
            }
            $.ajax({
                url: 'delclazz.vpage',
                type: 'POST',
                data: {
                    schoolId : delClazzSchoolId,
                    clazzId : delDupClazzId
                },
                success: function (res) {
                    if (res.success) {
                        alert(res.info);
                        window.location.href = "/crm/school/schoolhomepage.vpage?schoolId=" + delClazzSchoolId;
                    }else {
                        alert(res.info);
                    }
                }
            });
        });

        $("#add_school_evaluate_btn_ok").on("click",function(){
            var placeScore = $("#placeScore").val();
            var teachScore = $("#teachScore").val();
            var studentScore = $("#studentScore").val();
            var commercializeScore = $("#commercializeScore").val();
            var remark = $("#evaluate_remark").val();
            var schoolId = ${schoolInfoAdminMapper.schoolId!''};
            if(!checkScore(placeScore)){
                alert("地理位置评分填写错误");
                return;
            }
            if(!checkScore(teachScore)){
                alert("教学质量评分填写错误");
                return;
            }
            if(!checkScore(studentScore)){
                alert("生源水平评分填写错误");
                return;
            }
            if(!checkScore(commercializeScore)){
                alert("商业化潜力评分填写错误");
                return;
            }
            var data = {
                placeScore:placeScore,
                teachScore:teachScore,
                studentScore:studentScore,
                commercializeScore:commercializeScore,
                remark:remark,
                schoolId:schoolId
            };
            $.post("add_school_evaluate.vpage",data,function(res){
                if(res.success){
                    alert("学校评分成功");
                    window.location.href = "/crm/school/schoolhomepage.vpage?schoolId=" + schoolId;
                }else{
                    alert(res.info);
                }
            })
        });

        function checkScore(score){
            return score%1 == 0 && score > 0 && score < 6;
        }

        $("#btn_add_new_walking_clazz_submit").on("click", function () {
            var thisSchoolId = $("#addClazzSchoolId").val();
            var clazzName = $("#newWalkingClazzName").val() + "班";
            var clazzLevel = $('#walkingclazz_level').find('option:selected').val();
            var clazzSubject = $('#walkingclazz_subject').find('option:selected').val();
            if (!checkSpecialSchool()) {
                return false;
            }
            $.ajax({
                type: "post",
                url: "addwalkingclazzname.vpage",
                data: {
                    schoolId: thisSchoolId,
                    clazzName: clazzName,
                    clazzSubject: clazzSubject,
                    clazzLevel: clazzLevel
                },
                success: function (data) {
                    if (data.success) {
                        alert("增加教学班级成功!");
                        window.location.href = "/crm/school/schoolhomepage.vpage?schoolId=" + thisSchoolId;
                    } else {
                        alert(data.info);
                    }

                }
            });

        });

        $(".v-removeWalkingClazzName").on("click", function() {
            var $this = $(this);
            var clazzFullName = $this.siblings("span").text();
            var thisSchoolId = ${schoolInfoAdminMapper.schoolId!''}
            if (!checkSpecialSchool()) {
                return false;
            }
            $.ajax({
                type: "post",
                url: "removewalkingclazzname.vpage",
                data: {
                    schoolId: thisSchoolId,
                    clazzFullName: clazzFullName
                },
                success: function (data) {
                    if (data.success) {
                        alert("删除教学班级成功!");
                        window.location.href = "/crm/school/schoolhomepage.vpage?schoolId=" + thisSchoolId;
                    } else {
                        alert(data.info);
                    }

                }
            });
        })

        $("#show-reject-gift-btn").click(function(){
            var thisSchoolId = ${schoolInfoAdminMapper.schoolId!''}
                    $.get("getrejectreceivegiftlist.vpage",{schoolId:thisSchoolId},function(data){
                        if(data.success){
                            var modalBody = $("#show-reject-receive-dialog")
                                    .modal("show")
                                    .find(".modal-body");
                            $(modalBody).html('');

                            $.each(data.list,function(index,name){
                                $(modalBody).append("<span class='label label-default' style='margin-left:5px;'>"+name+"</span>")
                            });
                        }
                    });
        });

        $("#allow_klx_privilege_dialog_button").click(function () {
            var schoolId = ${schoolInfoAdminMapper.schoolId!''};
            var scanMachineFlag = false;
            var questionCardFlag = false;
            var barcodeAnswerQuestionFlag = false;
            var questionBankFlag = false;
            var a3AnswerQuestionFlag = false;
            var manualAnswerQuestionFlag = false;
            var items = document.getElementsByName("school_klx_privilege");
            for (i = 0; i < items.length; i++) {
                if (items[i].checked) {
                    if(items[i].value=="scanMachineFlag"){
                        scanMachineFlag = true;
                    }else if(items[i].value=="questionCardFlag"){
                        questionCardFlag = true;
                    }else if(items[i].value=="barcodeAnswerQuestionFlag"){
                        barcodeAnswerQuestionFlag = true;
                    }else if(items[i].value=="questionBankFlag"){
                        questionBankFlag = true;
                    } else if (items[i].value == "a3AnswerQuestionFlag") {
                        a3AnswerQuestionFlag = true;
                    } else if (items[i].value == "manualAnswerQuestionFlag") {
                        manualAnswerQuestionFlag = true;
                    }
                }
            }

            // 拼接学科
            var subjectNodes = $(".klx_valid_subject");
            var subjects = [];
            if(subjectNodes.length !== 0){
                $.each(subjectNodes,function(i,item){
                    if ($(item).is(":checked")) subjects.push($(item).val());
                })
            }

            var klxPrivilege = {
                schoolId: schoolId,
                scanMachineFlag: scanMachineFlag,
                questionCardFlag: questionCardFlag,
                barcodeAnswerQuestionFlag: barcodeAnswerQuestionFlag,
                questionBankFlag: questionBankFlag,
                a3AnswerQuestionFlag: a3AnswerQuestionFlag,
                manualAnswerQuestionFlag: manualAnswerQuestionFlag,
                subjects: subjects.join(",")
            };

            $.post("updateschoolklxprivilege.vpage", klxPrivilege, function (data) {
                if (data.success) {
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            })
        });

        $('#add_schoolQuizBankAdministrator_dialog_button').on('click', function () {
            if (!/^[0-9]*$/.test($('#school_QuizBankAdministrator').val())) {
                alert("请输入正确的用户ID!");
                return;
            }
            var schoolId = ${schoolInfoAdminMapper.schoolId!''};
            var teacherId = $('#school_QuizBankAdministrator').val();
            $.post("addschoolquizbankadministrator.vpage", {schoolId:schoolId,teacherId:teacherId}, function (data) {
                if (data.success) {
                    $("#add_schoolQuizBankAdministrator_dialog").modal('hide');
                    $("#teacher_roles_btn").click();
                } else {
                    alert(data.info);
                }
            })
        });

        $('#add_examManager_dialog_button').on('click', function () {
            if (!/^[0-9]*$/.test($('#exam_manager_id').val())) {
                alert("请输入正确的用户ID!");
                return;
            }
            var schoolId = ${schoolInfoAdminMapper.schoolId!''};
            var teacherId = $('#exam_manager_id').val();
            $.post("addExamManager.vpage", {schoolId:schoolId,teacherId:teacherId}, function (data) {
                if (data.success) {
                    $('#add_examManager_dialog').modal('hide');
                    $("#teacher_roles_btn").click();
                } else {
                    alert(data.info);
                    $('#exam_manager_id').val('');
                }
            })
        });

        $(document).on('click', "a[name=\'disable-schoolQuizBankAdministrator\']", function () {
            var teacherId = $(this).attr("data-teacher-id");
            var schoolId = ${schoolInfoAdminMapper.schoolId!""};
            if (confirm("确定要取消学校"+schoolId+"的校本题库管理员"+teacherId+"吗？")) {
                $.post("cancelschoolquizbankadministrator.vpage", {schoolId:schoolId,teacherId:teacherId}, function (data) {
                    if (data.success) {
                    <#--window.location.href = 'schoolhomepage.vpage?schoolId=' + ${schoolInfoAdminMapper.schoolId!''};-->
                        $("#teacher_roles_btn").click();
                    } else {
                        alert(data.info);
                    }
                });
            }
        });

        $(document).on('click', "a[name='disable-examManager']", function () {
            var teacherId = $(this).attr("data-teacher-id");
            var schoolId = ${schoolInfoAdminMapper.schoolId!""};
            if (confirm("确定要取消学校"+schoolId+"的考试管理员"+teacherId+"吗？")) {
                $.post("cancelExamManager.vpage", {schoolId:schoolId,teacherId: teacherId}, function (data) {
                    if (data.success) {
//                        var examTrList = $('.exam-teacher-info');
//                        var examTeacherIdList = $('.exam-teacherid');
//                        var deleteIndex = -1;
//                        for (var i = 0, len = examTeacherIdList.length; i < len; i++) {
//                            if (parseInt(examTeacherIdList.eq(i).text()) == teacherId) {
//                                deleteIndex = i;
//                            }
//                        }
//                        examTrList.eq(deleteIndex).remove();
                        $("#teacher_roles_btn").click();
                    } else {
                        alert(data.info);
                    }
                });
            }
        });
    });

    $(function () {
        $('#download_county_school_button').on('click', function () {

            var countyCode = $('#countys').val();

            if (countyCode < 0) {
                alert('请选择要导出的学校所在区');
            } else {
                location.href = 'downloadcountyschool.vpage?countyCode=' + countyCode;
            }

        });
        $("#set_clazz_count_btn_ok").on("click", function () {
            var schoolId = ${schoolInfoAdminMapper.schoolId!''};
            var studentCount = $("#studentCount").val();
            var comment = $("#comment").val();
            if (schoolId == undefined || schoolId == "" || studentCount == undefined || studentCount < 100 || studentCount > 200 || comment == undefined || comment == "") {
                alert("参数错误，请仔细检查输入内容");
                return false;
            } else {
                $.ajax({
                    type: "post",
                    url: "/crm/school/setclazzstudentcount.vpage",
                    data: {
                        schoolId: schoolId,
                        studentCount: studentCount,
                        comment: comment,
                    },
                    success: function (data) {
                        alert(data.info);
                        $("#studentCount").val("");
                        $("#comment").val("");
                        $("#set_clazz_count").modal("hide");
                    }
                });
            }
        });
        $("#modifyschoolsubmit").on("click", function () {
            $.ajax({
                type: 'post',
                url: 'updateschool.vpage',
                data: {
                    schoolId: ${schoolInfoAdminMapper.schoolId!''},
                    countys: $('#countys_school').val(),
                    cmainname: $('#cmainname').val(),
                    schooldistrict: $('#schooldistrict').val(),
                    cname: $('#cname').val(),
                    shortname: $('#shortname').val(),
                    vip: $('#vip').val(),
                    authenticationState: $('#authenticationState').val(),
                    level: $('#level').val(),
                    type: $('#type').val(),
                    schoolDesc: $('#schoolDesc').val(),
                    check: true
                },
                success: function (data) {
                    if (data.success) {
                        if (data.existSchoolList != null && data.existSchoolList.length > 0) {
                            $("#similar-school-confirm").modal("show");
                            $("#similarschoolinfo").html(template("T:SIMILAR-SCHOOLINFO", {
                                existSchoolList: data.existSchoolList
                            }));
                        }else{
                            updateSchoolSubmit();
                        }
                    } else {
                        alert('修改失败，请正确设置各参数，并记录日志。');
                    }
                }
            });
        });
        $("#modify_similar_school_also").on("click", function () {
            updateSchoolSubmit();
        });

        $("#query_klx_scannumber_dialog_ok").on("click", function () {
            var schoolId = ${schoolInfoAdminMapper.schoolId!''};
            var scanNumber = $("#query_scanmber").val();
            $.post("/crm/school/queryscannumber.vpage",{schoolId:schoolId,scanNumber:scanNumber},function (data) {
                if(data.success){
                    if(data.groupIds){
                        var info = "填涂号所在分组:<br/>";
                        for(var i=0;i<data.groupInfos.length;i++){
                            info = info + '<a target="_blank" href="/crm/clazz/groupinfo.vpage?groupId='+data.groupInfos[i].groupId+'">分组'+ data.groupInfos[i].groupId;
                            if (data.groupInfos[i].disabled) info = info + '(已失效)';
                            info = info + '</a><br/>';
                        }
                        $("#query_scanmber_result").html(info);
                    }else if(data.clear){
                        if (!confirm(data.warnInfo + "\n是否确认释放此填涂号？")) {
                            return false;
                        }
                        removeScanNumberFromSchool(schoolId, scanNumber);
                    }else{
                        alert(data.warnInfo);
                    }
                }else{
                    alert(data.info);
                }
            });
        });

        //开通教务账号
        $('#openEduAdminAccount').on("click",function () {
            $('#openEduAdminAccount_dialog').find('input').val('');
            $('#eduErrorInfo').html('');
            $('#openEduAdminAccount_dialog').modal();
        });

        //变更手机号
        $('.affair-mobile').on("click",function () {
            $('#openEduAdminMobile_dialog').find('input').val('');
            $('#affairTeacherId').val($(this).data("tid"));
            $('#affairErrorInfo').html('');
            $('#openEduAdminMobile_dialog').modal();
        });

        //登陆首页
        $('.affair-home-page').on("click", function () {
            $('#affairid_login').text($(this).data("tid"));
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
                    teacherId: $('#affairid_login').text(),
                    teacherLoginDesc: $("#teacherLoginDesc").val(),
                    affair:true
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
        $('.affair-reset-pwd').on("click", function () {
            $('#affairId').text($(this).data("tid"));
            $('#passwordExtraDesc').val('');
            $("#password_dialog_radio button").removeClass("active").eq(0).addClass("active");
            $("#password_dialog").modal("show");
        });

        $("#dialog_edit_teacher_password").on("click", function () {
            var queryUrl = "../teachernew/resetaffairpassword.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId:$('#affairId').text(),
                    passwordDesc: $("#password_dialog_radio button[class='btn active']").html(),
                    passwordExtraDesc: $('#passwordExtraDesc').val(),
                },
                success: function (data) {
                    if (data.success) {
                        alert("操作成功");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                }
            });
        });


        // 删除教务老师
        $(".affair-delete").on("click", function() {
            $('#affairId').text($(this).data("tid"));
            $.ajax({
                type: "post",
                url: "removeResearchStaff.vpage",
                data: {
                    userId:$('#affairId').text()
                },
                success: function (data) {
                    if (data.success) {
                        alert("删除教务老师成功!");
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                }
            });
        });



        //验证是否手机号
        function isMobile(value) {
            value = value + "";
            //严格判定
            var _reg = /^0{0,1}(13[4-9]|15[7-9]|15[0-2]|18[7-8])[0-9]{8}$/;
            //简单判定
            var reg = /^1[0-9]{10}$/;
            if (!value || value.length != 11 || !reg.test(value)) {
                return false;
            }
            return true;
        }
        //验证六位数汉字
        function isChineseName(value) {
            var reg = /^[\u4E00-\u9FA5]{1,6}$/;
            if(!value || !reg.test(value+"")){
                return false
            }
            return true;
        }

        $('#eduAdminSureBtn').on("click",function () {
            var postData = {
                teacherName:$("#eduAdminTeacherName").val(),
                phone:$("#eduAdminTeacherPhone").val(),
                schoolId : ${schoolInfoAdminMapper.schoolId!''}
            };

            if(!isChineseName(postData.teacherName)){
                $('#eduErrorInfo').html('请输入最多6位汉字的老师姓名');
                return false
            }

            if(!isMobile(postData.phone)){
                $('#eduErrorInfo').html('请输入最多11位数手机号码');
                return false
            }

            $('#eduErrorInfo').html('');

            $.post('createaffairteacher.vpage', postData, function (res) {
                if (res.success) {
                    alert("开通成功！账号密码短信已经发送至老师手机号码");
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            });

        });

        $('#affairAdminSureBtn').on("click",function () {
            var postData = {
                teacherId: $('#affairTeacherId').val(),
                mobile:$("#affairTeacherPhone").val(),
                schoolId : ${schoolInfoAdminMapper.schoolId!''}
            };

            if(!isMobile(postData.mobile)){
                $('#affairErrorInfo').html('请输入最多11位数手机号码');
                return false
            }

            $('#affairErrorInfo').html('');

            $.post('modifyaffairteacher.vpage', postData, function (res) {
                if (res.success) {
                    alert("变更成功！");
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            });

        });

        $('#set_scan_number_digit').on('click', function() {
            $('#setScanNumber_dialog').modal('show');
        });

        $('#setScanNumberBtn').on('click',function() {
            var digit = $('#scanNumberDigit').val();
            if (!confirm("阅卷机填涂号位数设定后若再修改，会影响到老师正常使用\n确定将填涂号设定成"+digit+"位么")) {
                return false;
            }
            $.post('setscannumber.vpage', {schoolId: ${schoolInfoAdminMapper.schoolId!''}, digit: digit}, function(res) {
                if (res.success) {
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            });
        });

        $("input.bigTestPermission").change(function(){
            var userId = $(this).attr("userid");
            var schoolId = $(this).attr("schoolid");
            var input = $(this);
            if (typeof($(this).attr("checked")) == "undefined") {
                var postData = {
                    userId: userId,
                    schoolId: schoolId,
                    permission: "true"
                };
                $.post('/crm/school/modifyTeacherLagerExam.vpage', postData, function (res) {
                    if (res.success) {
                        alert("大考班权限设置成功！");
                        input.attr("checked","checked");
                    } else {
                        alert(res.info);
                    }
                });
            } else {
                var postData = {
                    userId: userId,
                    schoolId: schoolId,
                    permission: "false"
                };
                $.post('/crm/school/modifyTeacherLagerExam.vpage', postData, function (res) {
                    if (res.success) {
                        alert("大考班权限取消成功！");
                        input.removeAttr("checked");
                    } else {
                        alert(res.info);
                    }
                });
            }
        });

        // 老师角色下拉
//        $('#teacher_roles_level').on('change', function () {
//        });

        // 老师角色列表
        $("#teacher_roles_btn").on("click", function () {
            $('#add_schoolQuizBankAdministrator').hide(); // 隐藏添加校本题库管理员btn
            $('#add_examManager').hide(); // 隐藏添加考试管理员btn
            var schoolId = ${schoolInfoAdminMapper.schoolId!''};
            var teacherRoles = $('#teacher_roles_level').val();
            if (teacherRoles === '0') return ;
            $.ajax({
                type: "POST",
                url: "/crm/school/teacherroleslist.vpage",
                data: {
                    schoolId : schoolId,
                    teacherRoles: teacherRoles,
                },
                success: function (data) {
                    if (data.success) {
                        var data = data.data;
                        $('#teacher-roles-table').find('tr:nth-child(n+2)').remove();
//                    <a name="disable-examManager" data-teacher-id="12978422" role="button" class="btn btn-success" style="font-size: 12px;">取消</a>
                        if (teacherRoles === 'SCHOOL_BANK_MANAGER')  { // 校本题库管理员
                            $('#add_schoolQuizBankAdministrator').show(); // 显示添加校本题库管理员btn
                            for (var i = 0, len = data.length; i < len; i++) {
                                $('#teacher-roles-table').append("<tr>" +
                                        "<td>" + i + "</td>" +
                                        "<td>" + data[i].rolesName + "</td>" +
                                        "<td>" + data[i].teacherId + "</td>" +
                                        "<td>" + data[i].teacherName + "</td>" +
                                        "<td>" + data[i].comment + "</td>" +
                                        "<td><a name=\"disable-schoolQuizBankAdministrator\" data-teacher-id=" + data[i].teacherId + " role=\"button\" class=\"btn btn-success\">取消</a></td>" +
                                        "</tr>");
                            }
                        } else if (teacherRoles === 'EXAM_MANAGER') { // 考试管理员
                            $('#add_examManager').show(); // 显示添加考试管理员btn
                            for (var i = 0, len = data.length; i < len; i++) {
                                $('#teacher-roles-table').append("<tr>" +
                                        "<td>" + i + "</td>" +
                                        "<td>" + data[i].rolesName + "</td>" +
                                        "<td>" + data[i].teacherId + "</td>" +
                                        "<td>" + data[i].teacherName + "</td>" +
                                        "<td>" + data[i].comment + "</td>" +
                                        "<td><a name=\"disable-examManager\" data-teacher-id=" + data[i].teacherId + " role=\"button\" class=\"btn btn-success\">取消</a></td>" +
                                        "</tr>");
                            }
                        } else {
                            for (var i = 0, len = data.length; i < len; i++) {
                                $('#teacher-roles-table').append("<tr>" +
                                        "<td>" + i + "</td>" +
                                        "<td>" + data[i].rolesName + "</td>" +
                                        "<td>" + data[i].teacherId + "</td>" +
                                        "<td>" + data[i].teacherName + "</td>" +
                                        "<td>" + data[i].comment + "</td>" +
                                        "<td>" + "-" + "</td>" +
                                        "</tr>");
                            }
                        }
                    } else {
                        alert(data.info || '请求出错，稍后重试！');
                    }
                }
            });
        });

        /******************************标记学校地图坐标start********************************/
        // 标记学校坐标
        $("#trackSchoolMap").on("click", function () {
            $('#schoolmap_dialog').modal('show');
            setTimeout(function () {
                initSchoolMap(); // 延迟渲染地图，否则marker不会在中心点（会在左上角）
            }, 500);
        });
        // 更新学校坐标
        $('#update_school_map').on("click", function () {
            updateSchoolMap();
            $('#schoolmap_dialog').modal('hide');
        });
        // 初始化地图
        function initSchoolMap() {
            var initSchoolLongitude = +"${(schoolExtInfo.longitude)!''}", // 117.066725
                initSchoolLatitude = +"${(schoolExtInfo.latitude)!''}", // 39.248488
                schoolMap = null; // 高德地图对象

            // 已经存在学校经纬度，将中心点设置在该经纬度上，不存在则已所在市区为地图中心点
            if (initSchoolLongitude && initSchoolLatitude) {
                schoolMap = new AMap.Map('schoolMap', {
                    resizeEnable: true, // 是否监控地图容器尺寸变化
                    zoom: 12, // 初始化地图层级
                    center: [initSchoolLongitude, initSchoolLatitude]
                });
                addMarker(schoolMap, initSchoolLongitude, initSchoolLatitude); // 添加初始标注
            } else {
                schoolMap = new AMap.Map('schoolMap', {
                    resizeEnable: true, //是否监控地图容器尺寸变化
                    zoom: 12 // 初始化地图层级
                });
                schoolMap.setCity("${schoolInfoAdminMapper.regionName!'北京'}");
            }

            // 添加控件
            var scale = new AMap.Scale({
                    visible: true,
                }), // 比例尺
                toolBar = new AMap.ToolBar({
                    visible: true,
                    locate: true, // 定位按钮
                    position: 'RT',
                    offset: new AMap.Pixel(22, 10)
                }), // 工具条
                overView = new AMap.OverView({
                    visible: true
                }), // 鹰眼预览
                auto = new AMap.Autocomplete({
                    input: "tipinput"
                }),  //输入提示
                placeSearch = new AMap.PlaceSearch({
                    map: schoolMap
                });  //构造地点查询类
            schoolMap.addControl(scale);
            schoolMap.addControl(toolBar);
            schoolMap.addControl(overView);
            schoolMap.on('click', function(e){
                bindMarkerPosi(e.lnglat.R, e.lnglat.Q); // lng、lat保留6位，R、Q保留11位
                clearMarker(schoolMap);
                addMarker(schoolMap, e.lnglat.R, e.lnglat.Q);
            });
            // 注册监听，当选中某条记录时会触发
            AMap.event.addListener(auto, "select", function(e) {
                placeSearch.setCity(e.poi.adcode);
                placeSearch.search(e.poi.name);  //关键字查询查询
            });
        }
        // 添加新标记(map对象，经度，纬度)
        function addMarker(schoolMap, longitude, latitude) {
            var marker = new AMap.Marker({
                icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
                position: [longitude, latitude],
                draggable: true, // 标注可拖拽
                cursor: 'move' // 拖拽手势
            });
            schoolMap.add(marker);
            marker.on('dragend', function (e) { // 记录拖拽结束之后的点
                bindMarkerPosi(e.lnglat.R, e.lnglat.Q); // lng、lat保留6位，R、Q保留11位
            });
        }
        // 清除现有标记
        function clearMarker(schoolMap) {
            schoolMap.clearMap(); // 清楚所有覆盖物，清楚单个覆盖物为remove(marker)
        }
        // 绑定标记坐标到map对象上，防止全局变量污染
        function bindMarkerPosi(longitude, latitude) {
            $('#schoolMap').attr({
                'date-longitude': longitude,
                'date-latitude': latitude
            });
        }
        // 存储学校新坐标
        function updateSchoolMap() {
            $.ajax({
                url: '/crm/school/loadschooladdress.vpage',
                type: 'POST',
                data: {
                    schoolId: "${schoolInfoAdminMapper.schoolId!''}",
                    longitude: $('#schoolMap').attr('date-longitude'),
                    latitude: $('#schoolMap').attr('date-latitude')
                },
                success: function (res) {
                    if (!res.success) {
                        alert(res.info);
                        return;
                    }
                    alert('您已成功标记学校坐标');
                    window.location.reload();
                }
            });
        }
        /******************************标记学校地图坐标end********************************/
    });

</script>
</@layout_default.page>