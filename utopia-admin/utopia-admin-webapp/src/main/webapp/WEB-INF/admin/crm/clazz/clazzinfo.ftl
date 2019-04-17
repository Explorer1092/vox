<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div class="span9">
    <div>
        <fieldset>
            <#--<legend>班级详情 &nbsp;<#if !requestContext.getCurrentAdminUser().isCsosUser()><button id="change_clazz_level" class="btn">修改年级</button></#if></legend>-->
            <legend>班级详情</legend>
        </fieldset>
        <table class="table table-hover table-striped table-bordered">
            <tr>
                <th>班级编号</th>
                <th>年级班级</th>
                <th>班级容量</th>
                <th>学校 ID</th>
                <th>学校名称</th>
            </tr>
            <tr>
                <td>${(clazzInfo.clazzId)!?html}</td>
                <td>
                    <#if clazzInfo.classLevel?? && clazzInfo.classLevel == '99'>
                        小学毕业
                    <#else>
                        ${(clazzInfo.classLevel)!?html}年级
                    </#if>
                    ${(clazzInfo.className?html)!''}
                </td>
                <td>${(clazzInfo.classSize)!?html}</td>
                <td>${(clazzInfo.schoolId)!?html}</td>
                <td><a href="../school/schoolhomepage.vpage?schoolId=${(clazzInfo.schoolId?html)!''}">${(clazzInfo.schoolName?html)!''}</a></td>
            </tr>
        </table>
    </div>
    <br/>
    <br/>
    <legend>老师列表</legend>
    <#if !requestContext.getCurrentAdminUser().isCsosUser()>
    <ul class="inline">
        <li>
            <a class="btn" href="changeteacherhistory.vpage?clazzId=${(clazzInfo.clazzId)!?html}">任课历史</a>
        </li>
        <li>
            <#--<button class="btn" id="addTeacher">添加老师</button>-->
        </li>
    </ul>
    </#if>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th> 姓 名(ID)</th>
            <th> 学 科 </th>
            <th> 班级创建者 </th>
            <th> 教 材(ID) </th>
            <th> 操作 </th>
        </tr>
        <#if clazzInfo.teacherInfoList?has_content>
            <#list clazzInfo.teacherInfoList as teacherInfo>
                <tr>
                    <td><a href="../user/userhomepage.vpage?userId=${teacherInfo.teacherId!}"> ${(teacherInfo.teacherName?html)!}</a>(${teacherInfo.teacherId!})</td>
                    <td>${teacherInfo.subject!}</td>
                    <td>${teacherInfo.creator?string("是", "否")}</td>

                    <td>
                        <#if teacherInfo.clazzBookInfoList?has_content>
                            <#list teacherInfo.clazzBookInfoList as clazzBookInfo>
                                <ul class="inline">
                                    <li>
                                        ${clazzBookInfo.bookName!} (${clazzBookInfo.bookId!}，${clazzBookInfo.compulsoryTextbook?string("必修", "非必修")})
                                    </li>
                                </ul>
                            </#list>
                        </#if>
                    </td>
                    <td class="text-center">
                        <#if !requestContext.getCurrentAdminUser().isCsosUser()>
                        <#--<button teacherId="${teacherInfo.teacherId!}" class="btn btn-danger delClassTeacher">删除</button>-->
                        </#if>
                    </td>
                </tr>
            </#list>
        </#if>
    </table>
    <br>
    <br>
    <legend>学生列表 <#if !requestContext.getCurrentAdminUser().isCsosUser()><a href="deletedstudentlist.vpage?clazzId=${(clazzInfo.clazzId)!}">删除历史</a></#if></legend>
    <#if clazzInfo.studentList?has_content>
        <table id="students" class="table table-hover table-striped table-bordered">
            <tr>
                <th> 姓 名(ID)</th>
                <th> 操 作</th>
                <th> 姓 名(ID)</th>
                <th> 操 作</th>
                <th> 姓 名(ID)</th>
                <th> 操 作</th>
            </tr>
            <#list clazzInfo.studentList as student>
                <#if student_index % 3 == 0><tr></#if>
                    <td>
                        <#if !requestContext.getCurrentAdminUser().isCsosUser()>
                        <a href="../student/studenthomepage.vpage?studentId=${student.studentId!""}"> ${student.studentName!''}</a>(${student.studentId!})
                        <#else>
                        ${student.studentName!''}
                        </#if>
                    </td>
                    <td class="text-center">
                        <#if !requestContext.getCurrentAdminUser().isCsosUser()>
                        <button class="btn btn-danger" onclick="deleteUser(${student.studentId!""},${clazzInfo.clazzId!''},${clazzInfo.managerId!0});">删除</button>
                        </#if>
                    </td>
                <#if student_index % 3 == 2 || !student_has_next></tr></#if>
            </#list>
        </table>
    </#if>
    <div id="dialog-confirm" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>是否真的删除此学生</h3>
        </div>
        <div class="modal-body">
            <input id="studentId" style="display: none"/>
            <input id="clazzId" style="display: none"/>
            <input id="teacherId" style="display: none"/>
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>班级 ID</dt>
                        <dd>${clazzInfo.clazzId!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>备 注</dt>
                        <dd><textarea id="deleteDesc" name="deleteDesc" cols="35" rows="4" value=""></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="delete_student_dialog_btn_ok" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="jclass" class="modal fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">讲老师加入该班，请输入老师ID</h4>
                </div>
                <div class="modal-body" id="scid">
                    <ul class="inline">
                        <li>老师ID:</li>
                        <li>
                            <input type="text" size="20" id="joinTid"/>
                        </li>
                    </ul>
                </div>

                <div class="modal-footer" id="edb">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="btn_join_submit"  type="button" class="btn btn-primary">提交</button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->

    <div id="changecl" class="modal fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">请选择年级</h4>
                </div>
                <div class="modal-body" id="scid">
                    <ul class="inline">
                        <li>年级列表:</li>
                        <li>
                            <select id="clazz_level" name="clazz_level" class="input-small" style="width: 100px">
                                <#list clazzLevels as clazzLevel>
                                    <#if clazzLevel.key lte 6 || clazzLevel.key == 99>
                                        <option value="${clazzLevel.key!}" <#if clazzLevel.key?c == clazzInfo.classLevel > selected </#if>>${clazzLevel.value!}</option>
                                    </#if>
                                </#list>
                            </select>
                        </li>
                        <#--新班级体系先去掉学制的修改了---20150820-->
                        <#--<li>学制:</li>-->
                        <#--<li>-->
                            <#--<select id="eduSystem" name="eduSystem" class="input-small" style="width: 150px">-->
                                   <#--<option value="P5" <#if clazzInfo.eduSystem == 'P5' > selected </#if>>小学五年制</option>-->
                                   <#--<option value="P6" <#if clazzInfo.eduSystem == 'P6' > selected </#if>>小学六年制</option>-->
                            <#--</select>-->
                        <#--</li>-->
                    </ul>
                </div>

                <div class="modal-footer" id="edb">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="btn_change_clazz_submit"  type="button" class="btn btn-primary">提交</button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->

</div>
<script>

    function deleteUser(studentId,clazzId,teacherId){
        $("#studentId").val(studentId);
        $("#clazzId").val(clazzId);
        $("#teacherId").val(teacherId);
        $('#deleteDesc').val('');
        $("#dialog-confirm").modal("show");
    }

    $(function() {

        $("#delete_student_dialog_btn_ok").on("click", function(){
            var queryUrl = "deletestudent.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    studentId : $("#studentId").val(),
                    clazzId  :  $("#clazzId").val(),
                    teacherId : $("#teacherId").val(),
                    deleteDesc : $('#deleteDesc').val()
                },
                success:function(data){
                    if(data.success){
                        location.href = "clazzinfo.vpage?clazzId=" + $("#clazzId").val();
                    }else{
                        alert("删除班级学生失败,请检查是否填写备注。");
                    }
                    $("#dialog-confirm").modal("hide");
                }
            });
        });

        $(document).on("click", "#addTeacher", function() {
            $("#jclass").modal("show");
        });

        var classId = ${(clazzInfo.clazzId)!};
        $(document).on("click", "#btn_join_submit", function() {
            $.post('joinclass.vpage',{teacherId : $("#joinTid").val(),classId : classId},function(data){
                alert(data.info);
                window.location.href = "/crm/clazz/clazzinfo.vpage?clazzId="+classId;
            });
        });

        $(document).on("click", ".delClassTeacher", function() {
            var teacherId = $(this).attr("teacherid");
            if(window.confirm("将老师(ID:"+teacherId+")从该班删除")){
                $.post('/crm/clazz/quitclass.vpage',{teacherId : teacherId,classId : classId},function(data){
                    if(data.success){
                        alert("操作成功");
                    }else{
                        alert("操作失败.原因:\n"+data.info);
                    }
                    window.location.href = "/crm/clazz/clazzinfo.vpage?clazzId="+classId;
                });
            }
        });

        $("#change_clazz_level").on("click",function(){
            $("#changecl").modal("show");
        });

        $('#btn_change_clazz_submit').on("click",function(){

            $.post('changeclazzlevel.vpage',
                    {
                        clazzId : classId,
                        clazzLevel : $('#clazz_level').find('option:selected').val(),
                    },function(data){
                        alert(data.info);
                        //修改年级的跳转----入口已干掉-------现在又重新开放了。
                        window.location.href = "/crm/clazz/clazzinfo.vpage?clazzId="+classId;
                    });
        });

    });
</script>
</@layout_default.page>