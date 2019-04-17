<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="转校审核" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>
<div class="span11">
    <legend>
        <a href="/crm/school_clue/clue_list.vpage">学校信息审核</a>&nbsp;&nbsp;
        <a href="/crm/teacher_fake/teacher_fakes.vpage">判假老师审核</a>&nbsp;&nbsp;
        <a href="/crm/teacher_appeal/index.vpage">老师申诉审核</a>&nbsp;&nbsp;
        转校审核
    </legend>
    <form id="iform" action="/crm/teachertransfer/index.vpage" method="post">
        <ul class="inline">
            <li>
                <label for="transferDate">
                    转校日期：
                    <input name="transferDate" id="transferDate" value="${transferDate!}" type="text" class="date"/>
                </label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <button class="btn btn-primary" type="submit">查询</button>
                &nbsp;&nbsp;
                <button class="btn btn-primary" type="button" onclick="formReset()">重置</button>
            </li>
        </ul>
        <input id="PAGE" name="PAGE" type="hidden"/>
        <input id="SIZE" name="SIZE" value="25" type="hidden"/>
    </form>
    <div>
        <table class="table table-bordered">
            <tr>
                <th>转校类别</th>
                <th>转校时间</th>
                <th>转校老师姓名（ID）</th>
                <th>老师手机</th>
                <th>认证状态</th>
                <th>转出学校名（ID）</th>
                <th>转出学校是否重点</th>
                <th>当前所带班级</th>
                <th>转入学校名（ID）</th>
                <th>转入学校是否重点</th>
                <th>申请人（电话）</th>
                <th>申请任务内容</th>
                <th>操作人</th>
                <th>其他联络人</th>
                <th>转校是否正确</th>
                <th>班级是否正确</th>
                <th>转校原因</th>
                <th>备注</th>
                <th>操作</th>
            </tr>
            <tbody>
                <#if dataResult??>
                    <#list dataResult as data>
                    <tr>
                        <td>${data.transferType!''}</td>
                        <td>${data.transferDate?string('yyyy-MM-dd HH:mm:ss')}</td>
                        <td><a href="/crm/teacher/teacherhomepage.vpage?teacherId=${data.teacherId!''}">${data.teacherName!''}(${data.teacherId!''})</a></td>
                        <td>${data.teacherMobile!''}</td>
                        <td>${data.authenticationState!''}</td>
                        <td>${data.transferOutSchoolName!''}(${data.transferOutSchoolId!''})</td>
                        <td><#if data.isEmphasisOutSchool>是<#else >否</#if></td>
                        <td><#if data.broughtClass??>
                                <#assign classNames=''>
                                <#list data.broughtClass as className>
                                    <#assign classNames= classNames + className +"," >
                                </#list>
                                ${classNames!''}
                            </#if>
                        </td>
                        <td>${data.transferInSchoolName!''}(${data.transferInSchoolId!''})</td>
                        <td><#if data.isEmphasisInSchool>是<#else >否</#if></td>
                        <td>${data.applicantName!''}(${data.applicantMobile!''})</td>
                        <td>${data.taskContent!''}</td>
                        <td>${data.executorName!''}</td>
                        <td>${data.otherLinkMan!''}</td>
                        <td><#if data.affirmTransferSchool??>
                                <#if data.affirmTransferSchool>
                                    正确
                                <#else>
                                    不正确
                                </#if>
                            </#if>
                        </td>
                        <td><#if data.affirmTransferClass??>
                                <#if data.affirmTransferClass>
                                    正确
                                <#else>
                                    不正确
                                </#if>
                            </#if>
                        </td>
                        <td>${data.transferSchoolReason!''}</td>
                        <td>${data.remark!''}</td>
                        <td><#if !data.isProof>
                                <input type="button" value="审核" onclick="review('${data.id!}')"/>
                            </#if>
                        </td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>
    </div>
</div>
<#include "teacherTransferReview.ftl">
<script type="application/javascript">
    $(function () {
        dater.render();
    });
    function formReset() {
        $("#transferDate").val("");
    }

    function review (id){
        if (blankString(id)) {
            alert("无效的任务ID！");
            return false;
        }
        $("#review-detail").attr("task-id",id);
        $("#review-detail").dialog({
            height: "auto",
            width: "600",
            autoOpen: true
        });
    }
</script>
</@layout_default.page>