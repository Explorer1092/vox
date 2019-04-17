<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <link  href="${requestContext.webAppContextPath}/public/css/bootstrap.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/admin.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
    <script src="${requestContext.webAppContextPath}/public/js/jquery-1.9.1.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/template.js"></script>
    <style>
        .table_soll{ overflow-y:hidden; overflow-x: auto;}
        .table_soll table td,.table_soll table th{white-space: nowrap;}
        .basic_info {margin-left: 2em;}
        .txt{margin-left: .5em;font-weight:800}
        .button_label{width:7em;height: 3em;margin-top: 1em}
        .info_td{width: 7em;}
        .info_td_txt{width: 13em;font-weight:600}
    </style>
</head>
<body style="margin-left: 2em; background: none;">
    <div style=";margin-top: 2em">
        <input id="tid" name="tid" type="hidden" value="<#if teacherSummary??>${teacherSummary.teacherId!}</#if>">
            <fieldset style="margin-top: 2em;">班级信息<span style="margin-left: 2em"><#if teacherSummary??><a target="_blank" href="../teacher/teacherchangeclazzhistory.vpage?teacherId=${teacherSummary.teacherId!}">查看换班记录</a></#if></span>
                <#if teacherClazzGroupInfo?? && teacherClazzGroupInfo.clazzLevelList?has_content>
                <ul class="inline" style="margin-top: 1em;">正常班级</ul>
                    <table class="table table-hover table-striped table-bordered">
                        <#list teacherClazzGroupInfo.clazzLevelList as clazzLevelChildList>
                            <#list clazzLevelChildList as clazzInfo>
                                <#if clazzInfo_index % 3 = 0><tr></#if>
                                <td class="classEdit" id="clazz${clazzInfo.id!}">
                                    <a target="_blank" href="../clazz/groupinfo.vpage?groupId=${clazzInfo.groupId!}" >
                                        ${clazzInfo.className!}</a>
                                    (班级Id:${clazzInfo.id!}&nbsp;组Id:${clazzInfo.groupId!})
                                    (${(clazzInfo.eduSys)!'未知学制'})
                                    <#if !requestContext.getCurrentAdminUser().isCsosUser()>
                                        [<a href="javascript:void(0)" teacherId="${teacherSummary.teacherId!}" groupId="${clazzInfo.groupId!}" clazzId="${clazzInfo.id!}" class="delClassTeacher">删除</a>]
                                        [<a href="javascript:void(0)" onclick="openDelegateClazzDialog(${clazzInfo.groupId!},${clazzInfo.id!})">转让</a>]
                                    </#if>
                                </td>
                                <#if clazzInfo_index % 3 = 2 || !clazzInfo_has_next></tr></#if>
                            </#list>
                        </#list>
                    </table>
                </#if>
                <#if teacherClazzGroupInfo?? && teacherClazzGroupInfo.groupNotInClazzLevelList?has_content>
                    <ul class="inline" style="margin-top: 1em;">已退出但还有学生资源的班级</ul>
                    <table class="table table-hover table-striped table-bordered">
                        <#list teacherClazzGroupInfo.groupNotInClazzLevelList as clazzLevelChildList>
                            <#list clazzLevelChildList as clazzInfo>
                                <#if clazzInfo_index % 3 = 0><tr></#if>
                                <td class="classEdit" id="clazz${clazzInfo.id!}">
                                    <a target="_blank" href="../clazz/groupinfo.vpage?groupId=${clazzInfo.groupId!}" >${clazzInfo.classLevel!}年级${clazzInfo.className!}</a>
                                    (班级Id:${clazzInfo.id!}&nbsp;组Id:${clazzInfo.groupId!})
                                    <#if !requestContext.getCurrentAdminUser().isCsosUser()>
                                        [<a href="javascript:void(0)" teacherId="${teacherSummary.teacherId!}" groupId="${clazzInfo.groupId!}" clazzId="${clazzInfo.id!}" class="delClassTeacher">删除</a>]
                                        [<a href="javascript:void(0)" onclick="openDelegateClazzDialog(${clazzInfo.groupId!},${clazzInfo.id!})">转让</a>]
                                    </#if>
                                </td>
                                <#if clazzInfo_index % 3 = 2 || !clazzInfo_has_next></tr></#if>
                            </#list>
                        </#list>
                    </table>
                </#if>
            </fieldset>

            <fieldset style="margin-top: 2em;">学生信息
                <#if groupStudentList??>
                <#list groupStudentList as groupStudent>
                    <div class="table_soll" style="margin-top: 2em;"><a target="_blank" href="../clazz/groupinfo.vpage?groupId=${groupStudent.groupId}">${groupStudent.clazzName!""}</a>(${groupStudent.clazzId!""})</div>
                    <table id="students" data-mode="columntoggle" class="table table-hover table-striped table-bordered">
                        <#if (groupStudent.studentList)?? && (groupStudent.studentList)?has_content>
                            <#list groupStudent.studentList as student>
                                <#if student_index % 6 == 0><tr></#if>
                                <td>
                                    <#if student??><a target="_blank" href="../student/studenthomepage.vpage?studentId=${student.id!''}">${student.name!''}</a>(${student.id!''})&nbsp;&nbsp;</#if>
                                </td>
                                <#if student_index % 6 == 5 || !student_has_next></tr></#if>
                            </#list>
                            <tr><td colspan="6"></td></tr>
                        </#if>
                        <#if (groupStudent.klxStudents)?? && (groupStudent.klxStudents)?has_content>
                            <#list groupStudent.klxStudents as klxStudent>
                                <#if klxStudent_index % 6 == 0><tr></#if>
                                <td>
                                    <#if klxStudent??><#if klxStudent.isRealStudent()><a target="_blank" href="../student/studenthomepage.vpage?studentId=${klxStudent.a17id!''}"></#if>${klxStudent.name!''}</a>(${klxStudent.scanNumber!'--'} / ${klxStudent.a17id!'--'})&nbsp;&nbsp;</#if>
                                </td>
                                <#if klxStudent_index % 6 == 5 || !klxStudent_has_next></tr></#if>
                            </#list>
                            <tr><td colspan="6">合计：共 ${(groupStudent.klxStudents)?size + (groupStudent.studentList)?size} 人</td></tr>
                        </#if>
                    </table>
                </#list>
                </#if>
            </fieldset>
    </div>
    <#--转让班级-->
    <div id="delegateClazz_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>老师转让班级</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal inline">
                <ul class="inline">
                    <li>
                        <dt>老师ID</dt>
                        <dd><#if teacherSummary?has_content>${teacherSummary.teacherId!''}</#if></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>班级ID</dt>
                        <dd id="delegateClazzClazzId"></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>组ID</dt>
                        <dd id="groupIdTransferGroup"></dd>
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
                        <dt>接受班级的老师ID</dt>
                        <dd><input type="text" id="recipientId"/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>问题描述</dt>
                        <dd><textarea id="delegateClazzDesc" cols="35" rows="4"></textarea></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>所做操作</dt>
                        <dd>管理员转让老师班级</dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="delegateClazz_dialog_btn" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>
    <#include "../specialschool.ftl">
    <script type="text/javascript">
        function openDelegateClazzDialog(groupId,clazzId) {
            if(confirm("换班前请务必确认该老师已经拒绝所有换班申请")) {
                $("#delegateClazzClazzId").text(clazzId);
                $("#groupIdTransferGroup").text(groupId);
                $('#recipientId').val('');
                $("#delegateClazzDesc").val('');
                $("#delegateClazz_dialog").modal("show");
            }
        }

        $(function(){
            //转让班级
            $('#delegateClazz_dialog_btn').on('click', function(){
                var clazzId = $('#delegateClazzClazzId').text();
                var teacherId = <#if teacherSummary?has_content>${teacherSummary.teacherId!''}<#else>""</#if>;
                var data = {
                    teacherId           :   teacherId,
                    clazzId             :   clazzId,
                    newTeacherId         :   $('#recipientId').val(),
                    delegateClazzDesc   :   $("#delegateClazzDesc").val()
                };
                if (!checkSpecialSchool()) {
                    return false;
                }
                $.post("/crm/clazz/transfergroup.vpage", data, function(data) {
                    $("#delegateClazz_dialog").modal("hide");
                    if(data.success) {
                        $("#clazz"+clazzId).remove();
                        // 强行踢出，使用户重新登录
                        $.ajax({
                            url: "/crm/teacher/kickOutOfApp.vpage",
                            type: "POST",
                            async: false,
                            data: {
                                "userId": teacherId
                            },
                            success: function (data) {
                            }
                        });
                        alert(data.info);
                    } else {
                        alert(data.info);
                    }
                });
            });
            //删除班级
            $(document).on("click", ".delClassTeacher", function() {
                var thisTeacherId = $(this).attr("teacherid");
                var thisGroupId = $(this).attr("groupId");
                var thisClazzId=$(this).attr("clazzId");
                if(window.confirm("将老师(ID:"+thisTeacherId+")从该班删除")){
                    if (!checkSpecialSchool()) {
                        return false;
                    }
                    $.post('/crm/clazz/deletegroup.vpage',{teacherId : thisTeacherId,groupId : thisGroupId},function(data){
                        if(data.success){
                            alert("操作成功");
                            $("#clazz" + thisClazzId).remove();
                        }else{
                            alert("操作失败.原因:\n"+data.info);
                        }
                    });
                }
            });
        });
    </script>
</body>
</html>