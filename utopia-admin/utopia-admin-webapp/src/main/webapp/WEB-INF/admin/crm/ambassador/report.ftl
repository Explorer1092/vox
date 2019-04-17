<#-- @ftlvariable name="totalPageNum" type="java.lang.Integer" -->
<#-- @ftlvariable name="conditionMap" type="java.util.Map" -->
<#-- @ftlvariable name="ambassadorInfoList" type="java.util.List<java.util.Map>" -->
<#-- @ftlvariable name="authStateMap" type="java.util.Map" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="校园大使举报信息" page_num=3>
<div class="span9">
    <div>
        <fieldset>
            <legend>校园大使举报信息</legend>
        </fieldset>
        <form id="s_form" action="?" method="post" class="form-horizontal">
            <ul class="inline">
            <#--<li>-->
            <#--要移动到的学校ID：<input id="schoolId" name="schoolId" class="input-medium" type="text"-->
            <#--placeholder="请输入学校ID">-->
            <#--</li>-->
                <li>
                    举报类型：<select name="type" id="type">
                    <option <#if type == 2>selected="selected" </#if> value="2">申请取消老师认证身份</option>
                    <option <#if type == 3>selected="selected" </#if> value="3">申请暂停老师认证身份</option>
                    <option <#if type == 1>selected="selected" </#if> value="1">举报非认证老师</option>
                </select>
                </li>
                <li>
                    <button type="submit" class="btn btn-primary">查询</button>
                <#--<button id="batchSchool" type="button" class="btn btn-primary">批量移动学校</button>-->
                </li>
            </ul>
        </form>
    </div>
    <div id="data_table_journal">
        <#if infoList??>
            <table class="table table-hover table-striped table-bordered so_checkboxs" so_checkboxs_values="">
                <tr>
                    <#--<td><input type="checkbox" class="so_checkbox_all"></td>-->
                    <th>老师ID</th>
                    <th>老师名字</th>
                    <th>举报原因</th>
                    <th>举报人</th>
                    <th>举报时间</th>
                    <th>举报类型</th>
                    <th>状态</th>
                    <th>处理时间</th>
                    <th>原因</th>
                </tr>
                <#list infoList as info>
                    <tr>
                        <#--<td><input name="teacherId" type="checkbox" class="so_checkbox" value="${info.teacherId!}"></td>-->
                        <td><a href="../user/userhomepage.vpage?userId=${info.teacherId}">${info.teacherId}</a></td>
                        <td>${info.teacherName}</td>
                        <td>${info.reason}</td>
                        <td>${info.reportId}</td>
                        <td>${(info.createDatetime?string('yyyy-MM-dd HH:mm:ss'))!}</td>
                        <td><#if info.type == 1>举报非认证老师</#if>
                            <#if info.type == 2>申请取消老师认证身份</#if>
                            <#if info.type == 3>申请暂停老师认证身份</#if>
                        </td>
                        <td>${(info.status.description?string)!''}</td>
                        <td>${(info.updateDatetime?string('yyyy-MM-dd HH:mm:ss'))!}</td>
                        <td>${(info.comment)!}</td>
                    </tr>
                </#list>
            </table>
        </#if>
    </div>
</div>

<script>
    $(function () {
        $("a[name='deleteIndex']").on("click", function () {
            var infoId = $(this).attr("data-content-id");
            $.ajax({
                type: "post",
                url: "deletereport.vpage",
                data: {
                    infoId: infoId
                },
                success: function (data) {
                    if (data.success) {
                        alert(data.info);
                    } else {
                        alert(data.info);
                    }
                }
            });
        });

//        $('#batchSchool').on('click', function () {
//            /*
//            35204 北京市朝阳区银座十号小学
//
//            353246 银座九号小学
//
//            393213 银座九号小学
//
//            377137 北京市朝阳区阳明广场小学
//
//            2000 一起作业体验学校
//             */
//            var fakeSchoolIds = ["35204","353246","393213","377137","2000"];
//            var schoolId = $("#schoolId").val();
//            var teacherIds = $("table.so_checkboxs").attr("so_checkboxs_values").split(",");
//            if (teacherIds.length == 0 || (teacherIds.length == 1 && teacherIds[0] == "")) {
//                alert("请至少选择一条数据");
//                return;
//            }
//            if (schoolId == "") {
//                alert("请输入要移动到的学校ID");
//                return;
//            }else if($.inArray(schoolId,fakeSchoolIds)>=0){
//                alert("禁止将老师移动到此学校，如果老师确实为假老师，请事先做老师的判假处理！");
//                return;
//            }
//            var postData = {
//                teacherIds: teacherIds.join(","),
//                schoolId: schoolId
//            };
//            if (confirm("确定将这些老师移动到新的学校吗？")) {
//                $.ajax({
//                    type: 'post',
//                    url: 'batchmoveschool.vpage',
//                    data: postData,
//                    success: function (data) {
//                        alert(data.info);
//                    }
//                });
//            }
//        });


        $("input.so_checkbox_all").on("click", function () {
            if (!$("input.so_checkbox_all").is(':checked')) {
                $("input.so_checkbox_all").prop("checked", false);
                $("input.so_checkbox").prop("checked", false);
                $("table.so_checkboxs").attr("so_checkboxs_values", "");
            } else {
                $("input.so_checkbox").prop("checked", true);
                var so_checkboxs_values = [];
                $("input.so_checkbox:checked").each(function () {
                    so_checkboxs_values.push($(this).val());
                });
                $("table.so_checkboxs").attr("so_checkboxs_values", so_checkboxs_values.toString());
            }
        });

        $("input.so_checkbox").on("click", function () {
            if ($("input.so_checkbox").size() == $("input.so_checkbox:checked").size()) {
                $("input.so_checkbox_all").prop("checked", true);
            } else {
                $("input.so_checkbox_all").prop("checked", false);
            }
            var so_checkboxs_values = [];
            $("input.so_checkbox:checked").each(function () {
                so_checkboxs_values.push($(this).val());
            });
            $("table.so_checkboxs").attr("so_checkboxs_values", so_checkboxs_values.toString());
        });

    });
</script>
</@layout_default.page>