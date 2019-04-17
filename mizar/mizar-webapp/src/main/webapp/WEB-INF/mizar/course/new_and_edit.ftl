<#import "../module.ftl" as module>
<@module.page
title="微课堂CRM"
pageJsFile={"micocourse" : "public/script/course/new_and_edit"}
pageJs=["micocourse"]
leftMenu="课程管理"
>
<#include "bootstrapTemp.ftl">
<h3 class="title-h1">
    课程管理
</h3>
<div class="op-wrapper orders-wrapper clearfix">
    <a class="btn btn-success" style="float:left; margin-right: 30px;" href="javascript:window.history.go(-1);"><i class="glyphicon glyphicon-chevron-left"></i> 返回列表</a>
    <#if !(mode?has_content && mode == 'view')>
        <a class="btn btn-primary" id="createBtn" style="float:left;" href="javascript:void(0)"><i class="glyphicon glyphicon-edit"></i> 保存课程</a>
    </#if>
</div>
<div id="courseInfo" class="clearfix">
    <div id="infoLeft" style="float: left; width: 48%">
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon"><span class="red-mark">*</span>课程名称</div>
                <input value="<#if course?has_content && course.courseName?has_content>${course.courseName!''}</#if>" name="name" class="v-select form-control js-create" id="course" style="width: 250px;">
            </div>
        </div>
        <div class="checkbox" style="margin-bottom: 15px; margin-top: 0; height:34px;">
            <label>
                <input type="checkbox" value="" id="buyByCourse" <#if course?has_content && (course.payAll)>checked</#if>>
                支持按课程购买
            </label>
            <input class="js-create" type="hidden" value="<#if course?has_content && (course.payAll)>true<#else>false</#if>" id="payAll" name="payAll">
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon"><span class="red-mark">*</span>&nbsp;&nbsp;&nbsp;按钮前</div>
                <input type="text" class="form-control js-buy_course_input js-create" name="btnContent" maxlength="40" placeholder="请输入按钮前文字" value="<#if course?has_content && course.btnContent?has_content>${course.btnContent!''}</#if>" <#if !course?has_content || !(course.payAll)>disabled</#if> style="width: 250px;">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">&nbsp;&nbsp;&nbsp;加群提示</div>
                <input type="text" class="form-control js-buy_course_input js-create" name="qqTip" maxlength="40" placeholder="请输入加群提示文字" value="<#if course?has_content && course.qqTip?has_content>${course.qqTip!''}</#if>" style="width: 250px;">
            </div>
        </div>
    </div>
    <div id="infoRight" style="float: right; width: 48%">
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon"><span class="red-mark">*</span>课程分类</div>
                <input value="<#if course?has_content && course.category?has_content>${course.category!''}</#if>" name="category" class="v-select form-control js-create" id="category" style="width: 250px;">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon"><span class="red-mark">*</span>课程价格</div>
                <input type="text" class="form-control js-buy_course_input js-create" name="price" maxlength="7" placeholder="请输入课程价格" value="<#if course?has_content && course.price?has_content>${course.price!}</#if>" <#if !course?has_content || !(course.payAll)>disabled</#if> style="width: 250px;">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon"><span class="red-mark">*</span>备注提示</div>
                <input type="text" class="form-control js-buy_course_input js-create" name="tip" maxlength="50" placeholder="请输入备注提示文字" value="<#if course?has_content && course.tip?has_content>${course.tip!''}</#if>" <#if !course?has_content || !(course.payAll)>disabled</#if> style="width: 250px;">
            </div>
        </div>
        <div class="form-group">
            <div class="input-group">
                <div class="input-group-addon">&nbsp;&nbsp;&nbsp;加群链接</div>
                <input type="text" class="form-control js-buy_course_input js-create" name="qqUrl" maxlength="120" placeholder="请输入加群链接" value="<#if course?has_content && course.qqUrl?has_content>${course.qqUrl!''}</#if>" style="width: 250px;">
            </div>
        </div>
    </div>
</div>

<div id="contentDiv" style="margin-top:20px;display: <#if course?has_content && course.courseId?has_content>block<#else>none</#if>;">
    <p>添加教师和助教</p>
    <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
        <div class="panel panel-default">
            <div class="panel-heading" role="tab" id="headingOne">
                <h5 class="panel-title">
                        <strong>主讲：</strong><#if lecturers?has_content && lecturers?size gt 0><#list lecturers as t><#if t.selected!false> ${t.userName!''}</#if></#list></#if>
                </h5>
            </div>
            <#if mode?has_content && mode == 'view'><#else>
                <div id="collapseOne" class="panel-collapse collapse in" role="tabpanel" aria-labelledby="headingOne">
                    <div class="panel-body">
                        <div class="js-LecturerList">
                            <#if lecturers?has_content && lecturers?size gt 0>
                                <#list lecturers as t>
                                    <span class="btn btn-default js-teacher_single js-role <#if t.selected!false>active</#if>" style="margin:5px 0;white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" data-sid="${t.userId!0}">${t.userName!''}</span>
                                </#list>
                            </#if>
                        </div>
                        <div>
                            <div class="btn btn-primary pull-right js-sureBtn" style="float: none;" data-type="Lecturer">
                                <i class="glyphicon glyphicon-check"></i>确  定
                            </div>
                        </div>
                    </div>
                </div>
            </#if>
        </div>
        <div class="panel panel-default">
            <div class="panel-heading" role="tab" id="headingOne">
                <h5 class="panel-title">
                    <strong>助教：</strong><#if assistants?has_content && assistants?size gt 0><#list assistants as t><#if t.selected!false> ${t.userName!''}</#if></#list></#if>
                </h5>
            </div>
            <#if mode?has_content && mode == 'view'><#else>
                <div id="collapseOne" class="panel-collapse collapse in" role="tabpanel" aria-labelledby="headingOne">
                    <div class="panel-body">
                        <div class="js-AssistantList">
                            <#if assistants?has_content && assistants?size gt 0>
                                <#list assistants as t>
                                    <span class="btn btn-default js-teacher js-role <#if t.selected!false>active</#if>" style="margin:5px 0;white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" data-sid="${t.userId!0}">${t.userName!''}</span>
                                </#list>
                            </#if>
                        </div>
                        <div>
                            <div class="btn btn-primary pull-right js-sureBtn" style="float: none;" data-type="Assistant">
                                <i class="glyphicon glyphicon-check"></i> 确  定
                            </div>
                        </div>
                    </div>
                </div>
            </#if>
        </div>
    </div>
</div>
<div>
    <#if course?has_content && course.courseId?has_content>
        <#if mode?has_content && mode == 'view'><#else>
        <div class="pull-right" style="margin-bottom: 10px;">
            <a class="btn btn-success" id="addClassTimeBtn" href="periodinfo.vpage?courseId=${course.courseId!}"><i class="glyphicon glyphicon-plus"></i> 添加课时</a>
        </div>
        </#if>
        <div class="will">
            <table class="table table-bordered table-striped" style="background-color: #FFF;">
                <thead>
                <tr>
                    <th style="width: 120px;">课时ID</th>
                    <th>课时名称</th>
                    <th style="width: 100px;">开始时间</th>
                    <th style="width: 100px;">结束时间</th>
                    <th style="width: 60px;">价格</th>
                    <th style="width:160px;">操作</th>
                </tr>
                </thead>
                <tbody>
                    <#if periods?has_content && periods?size gt 0>
                        <#list periods as t>
                        <tr>
                            <td>${t.id!''}</td>
                            <td>${t.theme!''}</td>
                            <td>${t.startTime?string("yyyy-MM-dd HH:mm")!''}</td>
                            <td class="valar-morghulis" data-pid="${t.id!''}" data-st="<#if t.tk?? && t.tk.courseStatus?has_content>${t.tk.courseStatus}</#if>">${t.endTime?string("yyyy-MM-dd HH:mm")!''}</td>
                            <td>${t.price!0}</td>
                            <td data-pid="${t.id!''}">
                            <#if mode?has_content && mode == 'view'>
                                <a class="btn btn-success btn-xs" href="/course/manage/periodinfo.vpage?periodId=${t.id!}&courseId=<#if course?has_content && course.courseId?has_content>${course.courseId!}</#if>&mode=view" title="查看"><span><i class="glyphicon glyphicon-eye-open"></i></span></a>
                            <#else>
                                <a class="btn btn-primary btn-xs" href="/course/manage/periodinfo.vpage?periodId=${t.id!}&courseId=<#if course?has_content && course.courseId?has_content>${course.courseId!}</#if>" title="编辑"><span class="js-editBtn"><i class="glyphicon glyphicon-edit"></i></span></a>
                                <span class="js-delBtn btn btn-danger btn-xs" title="删除"> <i class="glyphicon glyphicon-trash"></i></span>
                            </#if>
                                <a class="btn btn-warning btn-xs" href="/course/manage/periodorder.vpage?period=${t.id!}" title="查看数据"><span><i class="glyphicon glyphicon-list-alt"></i></span></a>
                                <a class="btn <#if t.tk?? && t.tk.courseId?has_content>btn-success<#else>btn-danger</#if> btn-xs js-regBtn" href="javascript:void(0);" title="<#if t.tk?? && t.tk.courseId?has_content>更新欢拓课程:${t.tk.courseId!}<#else>注册欢拓课程</#if>"><span><i class="glyphicon glyphicon-tree-deciduous"></i></span></a>
                                <a class="btn btn-info btn-xs js-liveBtn" href="javascript:void(0);" title="进入直播"><span><i class="glyphicon glyphicon-play"></i></span></a>
                            </td>
                        </tr>
                        </#list>
                    <#else>
                    <tr>
                        <td colspan="6" style="text-align: center;">暂无课时数据~</td>
                    </tr>
                    </#if>
                </tbody>
            </table>
        </div>
    </#if>
</div>
<script>
    var cursourceId = 0;
    <#if course?has_content && course.courseId?has_content>
        cursourceId = '${course.courseId!}';
    </#if>
</script>
</@module.page>