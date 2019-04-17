<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='CRM-区域信息' page_num=3>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-eye-open"></i> 地区用户信息</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a class="btn btn-round" href="javascript:window.history.back();">
                    <i class="icon-chevron-left"></i>
                </a>&nbsp;
            </div>
        </div>

        <div class="box-content">
            <div id="DataTables_Table_0_wrapper" class="dataTables_wrapper" role="grid">
                <table class="table table-striped table-bordered" id="DataTables_Table_0"
                       aria-describedby="DataTables_Table_0_info">
                    <thead>
                        <tr>
                            <th class="sorting" style="width: 250px;">地区名称</th>
                            <th class="sorting" style="width: 120px;" title="该地区现在注册老师数量，不包括转班，退班，学校转区等异动老师">老师注册数量<i class="icon-question-sign"></i></th>
                            <th class="sorting" style="width: 120px;" title="该地区现在认证老师数量，不包括转班，退班，学校转区等异动老师">老师认证数量<i class="icon-question-sign"></i></th>
                            <#if showTeacherName??>
                            <th class="sorting" style="width: 120px;">老师姓名</th>
                            </#if>
                            <th class="sorting" style="width: 120px;" title="该地区现在注册学生数量，不包括转班，退班，升学，学校转区等异动学生">学生注册数量<i class="icon-question-sign"></i></th>
                            <th class="sorting" style="width: 120px;" title="该地区现在认证学生数量，不包括转班，退班，升学，学校转区等异动学生">学生认证数量<i class="icon-question-sign"></i></th>
                        </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                        <#if dataList?has_content>
                            <#list dataList as detail>
                            <tr class="odd">
                                <td class="center  sorting_1">
                                    <#if detail["clazz_name_show"]?has_content>
                                        ${(detail["clazz_name_show"])!""}
                                    <#elseif detail["schoolName"]?has_content>
                                        <a href="areainfo.vpage?schoolId=${detail["schoolId"]}">${detail["schoolName"]}</a>
                                    <#elseif detail["regionName"]?has_content>
                                        <a href="areainfo.vpage?regionCode=${detail["regionCode"]}">${detail["regionName"]}</a>
                                    </#if>
                                </td>
                                <td class="center  sorting_1">
                                    <#if detail["teacher_register"]?has_content && detail["teacher_register"] gt 0>
                                            ${detail["teacher_register"]}
                                    </#if>
                                </td>
                                <td class="center  sorting_1">
                                    <#if detail["teacher_auth"]?has_content && detail["teacher_auth"] gt 0>
                                            ${detail["teacher_auth"]}
                                        </#if>
                                </td>
                                <#if showTeacherName??>
                                <td class="center  sorting_1">
                                    <#if detail["teachers"]?has_content>
                                        <#list detail["teachers"] as teacher>
                                            <#if teacher.subject?has_content && teacher.subject == 'ENGLISH'>
                                                <span class="label label-important">英</span>
                                            </#if>
                                            <#if teacher.subject?has_content && teacher.subject == 'MATH'>
                                                <span class="label label-success">数</span>
                                            </#if>
                                            <#if teacher.subject?has_content && teacher.subject == 'CHINESE'>
                                                <span class="label label-info">语</span>
                                            </#if>
                                            <a id="teachers_${detail["clazz_id"]}_${teacher.teacher_id!}" href="javascript:void(0);">${teacher.name!}</a>
                                            &nbsp;&nbsp;
                                            <input id="t_subject_${detail["clazz_id"]}_${teacher.teacher_id!}" value="${teacher.subject?has_content?string(teacher.subject!, '无')}" type="hidden">
                                            <input id="t_email_${detail["clazz_id"]}_${teacher.teacher_id!}" value="${teacher.email?has_content?string(teacher.email!, '无')}" type="hidden">
                                            <input id="t_mobile_${detail["clazz_id"]}_${teacher.teacher_id!}" value="${teacher.mobile?has_content?string(teacher.mobile!, '无')}" type="hidden">
                                            <input id="t_regtime_${detail["clazz_id"]}_${teacher.teacher_id!}" value="${teacher.regtime?has_content?string(teacher.regtime!, '无')}" type="hidden">
                                            <input id="t_auth_${detail["clazz_id"]}_${teacher.teacher_id!}" value="${teacher.auth?string('已认证', '未认证')}" type="hidden">
                                            <input id="t_note_${detail["clazz_id"]}_${teacher.teacher_id!}" value="${teacher.notes!}" type="hidden">
                                        </#list>
                                    </#if>
                                </td>
                                </#if>
                                <td class="center  sorting_1">
                                    <#if detail["student_register"]?has_content && detail["student_register"] gt 0>
                                        ${detail["student_register"]}
                                    </#if>
                                </td>

                                <td class="center  sorting_1">
                                    <#if  detail["student_auth"]?has_content && detail["student_auth"] gt 0>
                                            ${detail["student_auth"]}
                                        </#if>
                                </td>
                            </tr>
                            </#list>
                        <#else>
                            <tr class="odd">
                                <td class="center  sorting_1" colspan="5">暂无数据</td>
                            </tr>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<div id="show_teacher_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">老师信息/填写评价</h4>
            </div>
            <div class="form-horizontal">
                <div class="modal-body" style="height: auto; overflow: visible; width: auto">
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">姓名</label>
                        <div class="controls">
                            <label class="control-label" for="focusedInput" id="teacher_name" style="text-align: left"></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">学科</label>
                        <div class="controls">
                            <label class="control-label" for="focusedInput" id="teacher_subject" style="text-align: left"></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">Email</label>
                        <div class="controls">
                            <label class="control-label" for="focusedInput" id="teacher_email" style="text-align: left"></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">手机号</label>
                        <div class="controls">
                            <label class="control-label" for="focusedInput" id="teacher_mobile" style="text-align: left"></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">认证</label>
                        <div class="controls">
                            <label class="control-label" for="focusedInput" id="teacher_auth" style="text-align: left"></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">注册时间</label>
                        <div class="controls">
                            <label class="control-label" for="focusedInput" id="teacher_reg_time" style="text-align: left"></label>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">备注</label>
                        <div class="controls">
                            <textarea id="teacher_note" class="input-xlarge focused" type="text" rows="5"></textarea>
                        </div>
                    </div>
                    <input type="hidden" id="teacher_id" value="">
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="save_teacher_note_btn" type="button" class="btn btn-primary">保存</button>
                </div>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $("a[id^='teachers_']").live('click',function(){
        var id = $(this).attr("id").substring("teachers_".length);
        var idArray = id.split("_");
        var clazzId = idArray[0];
        var teacherId = idArray[1];
        $('#teacher_name').html($(this).html().trim());
        var subject = $('#t_subject_'+clazzId+'_'+teacherId).val().trim();
        if (subject == 'MATH') {
            subject = '数学';
        } else if (subject == 'ENGLISH') {
            subject = '英语';
        } else {
            subject = '';
        }
        $('#teacher_subject').html(subject);
        $('#teacher_mobile').html($('#t_mobile_'+clazzId+'_'+teacherId).val().trim());
        $('#teacher_reg_time').html($('#t_regtime_'+clazzId+'_'+teacherId).val().trim());
        $('#teacher_email').html($('#t_email_'+clazzId+'_'+teacherId).val().trim());
        $('#teacher_auth').html($('#t_auth_'+clazzId+'_'+teacherId).val().trim());
        $('#teacher_note').val($('#t_note_'+clazzId+'_'+teacherId).val().trim());
        $('#teacher_id').val(teacherId);
        $('#show_teacher_dialog').modal('show');
    });
    $("#save_teacher_note_btn").live('click',function(){
        var notes = $('#teacher_note').val();
        var id = $('#teacher_id').val();
        if(notes != ''){
            $.post('addteachernote.vpage',{
                teacherId:id,
                notes:notes
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.reload();
                }
            });
        }else{
            $('#show_teacher_dialog').modal('hide');
        }
    });

</script>
</@layout_default.page>
