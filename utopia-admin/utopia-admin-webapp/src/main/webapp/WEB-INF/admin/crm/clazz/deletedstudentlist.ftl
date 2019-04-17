<#-- @ftlvariable name="deletedStudentList" type="java.util.List<java.util.Map>" -->
<#-- @ftlvariable name="clazzId" type="java.lang.Long" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="${clazzName!'班'}组(${groupId!})删除历史" page_num=3>
<div class="span9">
    <fieldset>
        <legend>${clazzName!'班级'}(${clazzId!})组(${groupId!})学生删除历史</legend>
    </fieldset>
    <ul class="inline">
        <li>
            <#--<span>暂停恢复学生</span>-->
            <button id="recover_deleted_student" class="btn">恢复学生</button>
        </li>
    </ul>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th style="width: 45px;"><label><input type="checkbox" id="all_checkbox"/>全选</label></th>
            <th>学生信息</th>
            <th style="width: 160px;">更新时间</th>
            <th style="width: 65px;">操作</th>
        </tr>
        <#if deletedStudentList?has_content>
            <#list deletedStudentList as deletedStudent>
                <tr id="tr_${deletedStudent.id!}" data-id="${deletedStudent.id!}">
                    <td><label><input type="checkbox" class="select_student_checkbox"/></label></td>
                    <td><a href="../student/studenthomepage.vpage?studentId=${deletedStudent.studentId!}">${deletedStudent.studentName!}</a>(${deletedStudent.studentId!})</td>
                    <td>${deletedStudent.updateDatetime!?string('yyyy-MM-dd HH:mm:ss')}</td>
                    <#--<td>暂停恢复学生</td>-->
                    <td><a href="javascript:void(0)" data-id="${deletedStudent.id!}" data-student_name="${deletedStudent.studentName!}" data-student_id="${deletedStudent.studentId!}"
                           class="recoverStudent">恢复</a></td>
                </tr>
            </#list>
        </#if>
    </table>

    <#if deletedKlxStudentList?? && deletedKlxStudentList?has_content>
        <br/>
        <br/>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th style="width: 45px;"><label><input type="checkbox" id="all_klx_checkbox"/>全选</label></th>
            <th>学生信息</th>
            <th style="width: 160px;">更新时间</th>
            <th style="width: 65px;">操作</th>
        </tr>
        <#list deletedKlxStudentList as deletedKlxStudent>
            <tr id="tr_${deletedKlxStudent.refId!}" data-id="${deletedKlxStudent.refId!}">
                <td><label><input type="checkbox" class="select_klx_student_checkbox"/></label></td>
                <td>
                ${deletedKlxStudent.klxStudentName}( ${deletedKlxStudent.klxStudentId!}<#if (deletedKlxStudent.studentId)?has_content> / ${deletedKlxStudent.studentId!}</#if> )
                </td>
                <td>${deletedKlxStudent.updateTime!?string('yyyy-MM-dd HH:mm:ss')}</td>
                <td><a href="javascript:void(0)" data-id="${deletedKlxStudent.refId!}" data-student_name="${deletedKlxStudent.klxStudentName!}" data-student_id="${deletedKlxStudent.klxStudentId!}"
                       class="recoverKlxStudent">恢复</a></td>
            </tr>
        </#list>
    </table>
    </#if>

</div>
<div id="recover_student_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>恢复学生</h3>
    </div>
    <div class="modal-body dl-horizontal">
        <dl class="inline">
            <dt>班级ID</dt>
            <dd>${clazzId!}</dd>
        </dl>
        <dl class="inline">
            <dt>组ID</dt>
            <dd>${groupId!}</dd>
        </dl>
        <dl class="inline">
            <dt>学生信息</dt>
            <dd id="dialog_user_name"></dd>
        </dl>
        <dl>
            <dt>备注</dt>
            <dd><textarea id="recover_student_desc" cols="35" rows="3"  style="resize: none;"></textarea></dd>
        </dl>
        <dl class="inline">
            <dt>所做操作</dt>
            <dd>恢复学生用户</dd>
        </dl>
        <dl>
            <div id="dialog_ref_id"></div>
            <div id="dialog_klx_ref_id"></div>
        </dl>
    </div>
    <div class="modal-footer">
        <button class="btn btn-primary" id="recover_student_dialog_btn">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>
</div>
<script>
    $(function() {
        $('.recoverStudent').click(function(){
            var $this = $(this);
            $('#dialog_user_name').html('姓名：' + $this.data('student_name') + '<br/> ID：' + $this.data('student_id'));
            $('#recover_student_desc').val('');
            $('#dialog_ref_id').data('ids', [$this.data('id')]);
            $('#recover_student_dialog').modal('show');
        });

        $('#recover_student_dialog_btn').click(function() {
            var clazzId = ${clazzId!};
            var groupId = ${groupId!};

            var ids = $('#dialog_ref_id').data('ids');
            var klxIds = $('#dialog_klx_ref_id').data('ids');
            var postData = {
                clazzId:${clazzId!},
                groupId:${groupId!},
                groupStudentRefIds       :   ids,
                groupKlxStudentRefIds    :   klxIds,
                recoverStudentDesc       :   $('#recover_student_desc').val()
            };
            $.post('recoverdeletedstudent.vpage', postData, function(data) {
                if (data.success) {
                    if (ids) {
                        for (var i = 0; i < ids.length; i++) {
                            $('#tr_' + ids[i]).remove();
                            $('#recover_student_dialog').modal('hide');
                        }
                    }
                    if (klxIds) {
                        for (var i = 0; i < klxIds.length; i++) {
                            $('#tr_' + klxIds[i]).remove();
                            $('#recover_student_dialog').modal('hide');
                        }
                    }
                }
                alert(data.info);
            });
        });

        $('#all_checkbox').click(function() {
            $('.select_student_checkbox').prop('checked', $(this).prop('checked'));
        });

        $("#all_klx_checkbox").click(function () {
            $('.select_klx_student_checkbox').prop('checked',$(this).prop('checked'));
        });
        $(".recoverKlxStudent").click(function () {
            var $this = $(this);
            $('#dialog_user_name').html('姓名：' + $this.data('student_name') + '<br/> ID：' + $this.data('student_id'));
            $('#recover_student_desc').val('');
            $('#dialog_klx_ref_id').data('ids', [$this.data('id')]);
            $('#recover_student_dialog').modal('show');
        });

        $('#recover_deleted_student').click(function() {
            var ids = [];
            var $checkeds = $('.select_student_checkbox:checked');
            $checkeds.closest('tr').each(function() {
                ids.push($(this).data('id'));
            });

            var klxIds = [];
            var $klxChecked  = $('.select_klx_student_checkbox:checked');
            $klxChecked.closest('tr').each(function() {
                klxIds.push($(this).data('id'));
            });

            $('#dialog_user_name').text('恢复选中的学生');
            $('#recover_student_desc').val('');
            $('#dialog_ref_id').data('ids', ids);
            $('#dialog_klx_ref_id').data('ids', klxIds);
            $('#recover_student_dialog').modal('show');
        });
    });
</script>
</@layout_default.page>