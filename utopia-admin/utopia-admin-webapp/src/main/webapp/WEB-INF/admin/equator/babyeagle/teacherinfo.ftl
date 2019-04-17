<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="小鹰学堂老师管理" page_num=24>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<span class="span9">
    <div id="legend" class="">
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation"><a href="studentlearninfo.vpage?studentId=${studentId!''}">学生课程管理</a></li>
            <li role="presentation"><a href="courseinfoindex.vpage">课程内容管理</a></li>
            <li role="presentation"><a href="coursekindindex.vpage">课程种类管理</a></li>
            <li role="presentation" class="active"><a href="teacherindex.vpage">教师管理</a></li>
            <li role="presentation"><a href="classhourmodel.vpage">单日课时模板</a></li>
        </ul>
    </div>

    <ul class="inline">
        <li>
            <button id="add_babyeagle_teacher_btn" type="button" class="btn btn-primary">新增老师</button>
        </li>
    </ul>

     <div class="table_soll">
            <table class="table table-bordered">
                <tr>
                    <th>主播老师id</th>
                    <th>姓名</th>
                    <th>类别</th>
                    <th>邮箱</th>
                    <th>介绍</th>
                    <th>操作</th>
                </tr>
                <tbody id="tbody">
                    <#if teachers ?? >
                        <#list teachers as teacher>
                        <tr>
                            <td data-teacherId="${teacher.id?default("")}">${teacher.bid?default("")}</td>
                            <td>${teacher.name?default("")}</td>
                            <td>
                                <#if teacher.type??&&(teacher.type == "BaseSchool")>
                                    <span class="label label-info">小鹰学堂</span>
                                </#if>
                                <#if teacher.type??&&(teacher.type == "ChinaCulture")>
                                    <span class="label label-warning">国学堂</span>
                                </#if>
                            </td>
                            <td>${teacher.email?default("")}</td>
                            <td>${teacher.intro?default("")}</td>
                            <td>
                                <button type="button" class="delete_babyeagle_teacher_btn btn btn-danger" data-name="${(teacher.name)!}" data-teacherId="${(teacher.id)!}">删除</button>
                                <button type="button" class="update_babyeagle_teacher_btn btn btn-default" data-teacherId="${(teacher.id)!}" data-teacherEmail="${(teacher.email)!}" data-teacherName="${(teacher.name)!}" data-teacherIntro="${(teacher.intro)!}">编辑</button>
                            </td>
                        </tr>
                        </#list>
                    </#if>
                </tbody>
            </table>
        </div>
</span>


<div id="add_babyeagle_teacher_btn_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>新增老师(<strong style="color:red">5分钟后生效</strong>)</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>姓名:</dt>
                    <dd><input id="teacherName" type='text' placeholder="不能为空,最多12个字符"/></dd>
                </li>
                <li>
                    <dt>类别:</dt>
                    <dd><div class="controls">
                        <select id="teacherType" name="teacherType">
                            <option value="BaseSchool">小鹰学堂</option>
                            <option value="ChinaCulture">国学堂</option>
                        </select>
                        <span class="controls-desc"></span>
                    </div>
                    </dd>
                </li>
                <li>
                    <dt>密码:</dt>
                    <dd><input id="teacherPasswd1" type='password' placeholder="不能为空"/></dd>
                </li>
                <li>
                    <dt>密码:</dt>
                    <dd><input id="teacherPasswd2" type='password' placeholder="不能为空"/></dd>
                </li>
                <li>
                    <dt>邮箱:</dt>
                    <dd><input id="teacherEmail" type='text' placeholder="请输入正确的格式"/></dd>
                </li>
                <li>
                    <dt>简介:</dt>
                    <dd><textarea id="teacherIntro" cols="35" rows="3" placeholder="可以不填写,最多40个字符"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="add_babyeagle_teacher_btn_dialog_confirm_btn" class="btn btn-primary">确定</button>
        <button id="add_babyeagle_teacher_btn_dialog_cancel_btn" class="btn btn-primary">取消</button>
    </div>
</div>

<div id="update_babyeagle_teacher_btn_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>更新老师(<strong style="color:red">5分钟后生效</strong>)</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <input id="updateTeacherId" type='hidden'/>
                <li>
                    <dt>姓名:</dt>
                    <dd><input id="updateTeacherName" type='text' placeholder="不能为空,最多12个字符"/></dd>
                </li>
                <li>
                    <dt>密码:</dt>
                    <dd><input id="updateTeacherPasswd1" type='password' placeholder="如果不准备修改,请不要填写"/></dd>
                </li>
                <li>
                    <dt>密码:</dt>
                    <dd><input id="updateTeacherPasswd2" type='password' placeholder="如果不准备修改,请不要填写"/></dd>
                </li>
                <li>
                    <dt>邮箱:</dt>
                    <dd><input id="updateTeacherEmail" type='text' placeholder="请输入正确的格式"/></dd>
                </li>
                <li>
                    <dt>简介:</dt>
                    <dd><textarea id="updateTeacherIntro" cols="35" rows="3"  placeholder="如果不想更新简介,可以不填写,最多40个字符"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="update_babyeagle_teacher_btn_dialog_confirm_btn" class="btn btn-primary">确定</button>
        <button id="update_babyeagle_teacher_btn_dialog_cancel_btn" class="btn btn-primary">取消</button>
    </div>
</div>


<div id="delete_babyeagle_teacher_btn_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>删除老师</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>教师id</dt>
                    <dd><input type="text" id="deleteTeacherId" readonly/></dd></li>
                <li>
                    <dt>教师姓名</dt>
                    <dd><input type="text" id="deleteTeacherName" readonly/></dd></li>
                <li>
                    <dt>删除原因:</dt>
                    <dd><textarea id="deleteTeacherDesc" cols="35" rows="3"  placeholder="删除原因不能为空"></textarea></dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="delete_babyeagle_teacher_btn_dialog_confirm_btn" class="btn btn-primary">确定</button>
        <button id="delete_babyeagle_teacher_btn_dialog_cancel_btn" class="btn btn-primary">取消</button>
    </div>
</div>

<script>
    $(function () {
        $('#add_babyeagle_teacher_btn').click(function () {
            $('#add_babyeagle_teacher_btn_dialog').modal("show");
        });
        $('#add_babyeagle_teacher_btn_dialog_cancel_btn').click(function () {
            $('#add_babyeagle_teacher_btn_dialog').modal("hide");
        });
        $('#add_babyeagle_teacher_btn_dialog_confirm_btn').click(function () {
            var teacherName = $('#teacherName').val();
            var teacherType = $('#teacherType').val();
            var teacherEmail = $('#teacherEmail').val();
            var teacherPasswd1 = $('#teacherPasswd1').val();
            var teacherPasswd2 = $('#teacherPasswd2').val();
            var teacherIntro = $('#teacherIntro').val();
            if (isBlank(teacherName)) {
                alert("老师姓名不能为空");
                return;
            }
            if (isBlank(teacherPasswd1) || isBlank(teacherPasswd2)) {
                alert("密码不能为空");
                return;
            }
            if (teacherPasswd1 != teacherPasswd2) {
                alert("两次输入的密码必须相同");
                return;
            }

            $.post("addteacher.vpage", {teacherName: teacherName, teacherType: teacherType, teacherEmail: teacherEmail, teacherPasswd1: teacherPasswd1, teacherPasswd2: teacherPasswd2, teacherIntro: teacherIntro}, function (data) {
                if (data.success) {
                    alert("新增老师成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
            $('#add_babyeagle_teacher_btn_dialog').modal("hide");
        });


        $('.update_babyeagle_teacher_btn').click(function () {
            var id= $(this).attr("data-teacherId");
            var teacherName=$(this).attr("data-teacherName");
            var teacherEmail=$(this).attr("data-teacherEmail");
            var teacherIntro=$(this).attr("data-teacherIntro");

            $('#updateTeacherId').val(id);
            $('#updateTeacherName').val(teacherName);
            $('#updateTeacherEmail').val(teacherEmail);
            $('#updateTeacherIntro').val(teacherIntro);
            $('#updateTeacherPasswd1').val();
            $('#updateTeacherPasswd2').val();
            $('#update_babyeagle_teacher_btn_dialog').modal("show");
        });

        $('#update_babyeagle_teacher_btn_dialog_cancel_btn').click(function () {
            $('#updateTeacherId').val();
            $('#updateTeacherName').val();
            $('#updateTeacherEmail').val();
            $('#updateTeacherIntro').val();
            $('#updateTeacherPasswd1').val();
            $('#updateTeacherPasswd2').val();
            $('#update_babyeagle_teacher_btn_dialog').modal("hide");
        });
        $('#update_babyeagle_teacher_btn_dialog_confirm_btn').click(function () {
            var id = $('#updateTeacherId').val();
            var name = $('#updateTeacherName').val();
            var email = $('#updateTeacherEmail').val();
            var intro = $('#updateTeacherIntro').val();
            var passwd1 = $('#updateTeacherPasswd1').val();
            var passwd2 = $('#updateTeacherPasswd2').val();
            $.post("updateteacher.vpage", {id: id, name: name, passwd1: passwd1, passwd2: passwd2, email: email, intro: intro}, function (data) {
                if (data.success) {
                    alert("更新老师成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
            $('#update_babyeagle_teacher_btn_dialog').modal("hide");
        });


        $('.delete_babyeagle_teacher_btn').click(function () {
            var id= $(this).attr("data-teacherId");
            var name= $(this).attr("data-name");
            $('#deleteTeacherId').val(id);
            $('#deleteTeacherName').val(name);
            $('#delete_babyeagle_teacher_btn_dialog').modal("show");
        });

        $('#delete_babyeagle_teacher_btn_dialog_confirm_btn').click(function () {
            var id= $("#deleteTeacherId").val();
            var desc= $("#deleteTeacherDesc").val();
            var name= $("#deleteTeacherName").val();
            if(isBlank(id)){
                alert("参数id为不能为空");
                return;
            }
            if(isBlank(desc)){
                alert("删除原因不能为空");
                return;
            }

            $.post('/equator/babyeagle/deleteteacher.vpage', {teacherId: id,desc:desc,name:name}, function (data) {
                if (data.success) {
                    alert("删除成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            });
            $('#delete_babyeagle_teacher_btn_dialog').modal("hide");
        });

        $('#delete_babyeagle_teacher_btn_dialog_cancel_btn').click(function () {
            $('#deleteTeacherId').val();
            $('#deleteTeacherDesc').val();
            $('#delete_babyeagle_teacher_btn_dialog').modal("hide");
        });
    });

    function isBlank(str) {
        return typeof str == 'undefined' || String(str) == 'null' || $.trim(str) == '';
    }

</script>
</@layout_default.page>