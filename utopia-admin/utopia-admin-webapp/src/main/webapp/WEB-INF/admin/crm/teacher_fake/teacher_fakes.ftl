<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="判假老师审核" page_num=3>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>

<div class="span11">
    <legend>
        <a href="/crm/school_clue/clue_list.vpage">学校信息审核</a>&nbsp;&nbsp;
        判假老师审核&nbsp;&nbsp;
        <a href="/crm/teacher_appeal/index.vpage">老师申诉审核</a>&nbsp;&nbsp;
        <a href="/crm/teachertransfer/teacherTransferSchool.vpage">转校审核</a>
    </legend>

    <form id="iform" action="/crm/teacher_fake/teacher_fakes.vpage" method="post">
        <ul class="inline">
            <li>
                <label for="reviewStatus">
                    审核状态：
                    <select id="reviewStatus" name="reviewStatus">
                        <#if reviewStatuses?has_content>
                            <#list reviewStatuses as status>
                                <#if reviewStatus?? && reviewStatus.name() == status.name()>
                                    <option value="${status.name()}" selected="selected">${status.value!}</option>
                                <#else>
                                    <option value="${status.name()}">${status.value!}</option>
                                </#if>
                            </#list>
                        </#if>
                    </select>
                </label>
            </li>

            <li>
                <label for="schoolName">
                    老师ID：
                    <input name="teacherId" id="teacherId" value="${teacherId!}" type="text"/>
                </label>
            </li>
        </ul>

        <ul class="inline">
            <li>
                <button type="submit">查询</button>
            </li>
            <li>
                <input type="button" value="重置" onclick="formReset()"/>
            </li>
        </ul>

        <input id="PAGE" name="PAGE" type="hidden"/>
        <input id="SIZE" name="SIZE" value="25" type="hidden"/>
        <input id="ORDER" name="ORDER" value="DESC" type="hidden"/>
        <input id="SORT" name="SORT" value="createTime" type="hidden"/>
    </form>

    <#setting datetime_format="yyyy-MM-dd HH:mm"/>
    <div>
        <table class="table table-bordered">
            <tr>
                <th>判假时间</th>
                <th>老师</th>
                <th>学校名称</th>
                <th>判假原因</th>
                <th>申请人</th>
                <th>联系方式</th>
                <th>审核人</th>
                <th>审核时间</th>
                <th>审核意见</th>
                <th>操作</th>
            </tr>
            <tbody>
                <#if teacherFakes?has_content>
                    <#list teacherFakes.content as teacherFake>
                    <tr>
                        <td>${teacherFake.createTime!}</td>
                        <td><a href="/crm/teachernew/teacherdetail.vpage?teacherId=${teacherFake.teacherId!}" target="_blank">${teacherFake.teacherName!}</a> (${teacherFake.teacherId!"无用户信息"})</td>
                        <td>${teacherFake.schoolName!}</td>
                        <td>${teacherFake.fakeNote!}</td>
                        <td>${teacherFake.fakerName!}</td>
                        <td>${teacherFake.fakerPhone!}</td>
                        <td>${teacherFake.reviewerName!}</td>
                        <td>${teacherFake.reviewTime!}</td>
                        <td>${teacherFake.reviewNote!}</td>
                        <td>
                            <#if teacherFake.reviewStatus?? && teacherFake.reviewStatus.name() == "WAIT">
                                <input type="button" value="通过" onclick="reviewPass('${teacherFake.id!}')">
                                <input type="button" value="驳回" onclick="rejectNote('${teacherFake.id!}')">
                            </#if>
                        </td>
                    </tr>
                    </#list>
                </#if>
            </tbody>
        </table>

        <#assign pager = teacherFakes!>
        <#include "../pager_foot.ftl">
    </div>
</div>

<div id="reject-note" title="驳回原因" style="font-size: small; display: none" fake-id="">
    <table width="100%">
        <tr>
            <td><textarea id="review-note" style="height: 120px; width: 400px" placeholder="在此写明为什么驳回..."></textarea></td>
        </tr>
        <tr>
            <td style="text-align: right">
                <input type="button" value="提交" onclick="reviewReject()"/>
                <input type="button" value="取消" onclick="closeDialog('reject-note')"/>
            </td>
        </tr>
    </table>
</div>

<script type="text/javascript">
    function formReset() {
        $("#reviewStatus").val("WAIT");
        $("#teacherId").val("");
    }

    function reviewPass(fakeId) {
        reviewFake(fakeId, "PASS");
    }

    function rejectNote(fakeId) {
        $("#reject-note").attr("fake-id", fakeId);
        $("#reject-note").dialog({
            height: "auto",
            width: "450",
            autoOpen: true
        });
    }

    function reviewReject() {
        var note = $("#review-note").val();
        if (blankString(note)) {
            alert("请填写驳回原因！");
            return false;
        }
        var fakeId = $("#reject-note").attr("fake-id");
        reviewFake(fakeId, "REJECT", note);
    }

    function reviewFake(fakeId, status, note) {
        if (blankString(fakeId)) {
            alert("无效的记录ID！");
            return false;
        }
        $.ajax({
            url: "/crm/teacher_fake/review_fake.vpage",
            type: "POST",
            data: {
                "id": fakeId,
                "reviewStatus": status,
                "reviewNote": note
            },
            success: function (data) {
                if (!data) {
                    alert("操作失败！");
                } else {
                    if (confirm("操作成功，是否刷新页面查看最新记录状态？")) {
                        $("#iform").submit();
                    }
                }
            }
        });
    }
</script>
</@layout_default.page>