<#macro clazzListChip>
<legend>班级列表</legend>
<button id="btn-addClazz" class="btn btn-primary">加入班级</button>
<#if teacherInfoAdminMapper.clazzLevelList?has_content>
    <table class="table table-hover table-striped table-bordered">
        <#list teacherInfoAdminMapper.clazzLevelList as clazzLevelChildList>
            <#list clazzLevelChildList as clazzInfo>
                <#if clazzInfo_index % 3 = 0><tr></#if>
                    <td class="classEdit" id="clazz${clazzInfo.id!}">
                        <a href="../clazz/groupinfo.vpage?groupId=${clazzInfo.groupId!}" >
                            ${clazzInfo.className!}</a>
                        (班级Id:${clazzInfo.id!}&nbsp;组Id:${clazzInfo.groupId!})
                        <#if !requestContext.getCurrentAdminUser().isCsosUser()>
                        [<a href="javascript:void(0)" teacherId="${(teacherInfoAdminMapper.teacher.id)!}" groupId="${clazzInfo.groupId!}" class="delClassTeacher">删除</a>]
                        [<a href="javascript:void(0)" onclick="openDelegateClazzDialog(${clazzInfo.groupId!},${clazzInfo.id!})">转让</a>]
                        </#if>
                    </td>
                <#if clazzInfo_index % 3 = 2 || !clazzInfo_has_next></tr></#if>
           </#list>
       </#list>
    </table>
</#if>
<legend>已退出但还有学生资源的班级</legend>
    <#if teacherInfoAdminMapper.groupNotInClazzLevelList?has_content>
    <table class="table table-hover table-striped table-bordered">
        <#list teacherInfoAdminMapper.groupNotInClazzLevelList as clazzLevelChildList>
            <#list clazzLevelChildList as clazzInfo>
                <#if clazzInfo_index % 3 = 0><tr></#if>
                <td class="classEdit" id="clazz${clazzInfo.id!}">
                    <a href="../clazz/groupinfo.vpage?groupId=${clazzInfo.groupId!}" >${clazzInfo.classLevel!}年级${clazzInfo.className!}</a>
                    (班级Id:${clazzInfo.id!}&nbsp;组Id:${clazzInfo.groupId!})
                    <#if !requestContext.getCurrentAdminUser().isCsosUser()>
                        [<a href="javascript:void(0)" teacherId="${(teacherInfoAdminMapper.teacher.id)!}" groupId="${clazzInfo.groupId!}" class="delClassTeacher">删除</a>]
                        [<a href="javascript:void(0)" onclick="openDelegateClazzDialog(${clazzInfo.groupId!},${clazzInfo.id!})">转让</a>]
                    </#if>
                </td>
                <#if clazzInfo_index % 3 = 2 || !clazzInfo_has_next></tr></#if>
            </#list>
        </#list>
    </table>
    </#if>
<br/>
<br/>
<!----------------------------dialog----------------------------------------------------------------------------------->

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
                    <dd>${teacherInfoAdminMapper.teacher.id!''}</dd>
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

<div id="modal-addClazz" class="modal fade">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h3 class="modal-title">加入班级</h3>
            </div>
            <div class="modal-body" id="scid">
                <ul class="inline">
                    <li>年级列表:</li>
                    <li>
                        <select id="clazz_level" name="clazz_level" class="input-small" style="width: 100px">
                            <#if ((teacherInfoAdminMapper.schoolLevel)!1) == 1>
                                <option value="1">一年级</option>
                                <option value="2">二年级</option>
                                <option value="3">三年级</option>
                                <option value="4">四年级</option>
                                <option value="5">五年级</option>
                                <option value="6">六年级</option>
                            <#elseif ((teacherInfoAdminMapper.schoolLevel)!1) == 2>
                                <option value="6">六年级</option>
                                <option value="7">七年级</option>
                                <option value="8">八年级</option>
                                <option value="9">九年级</option>
                            <#elseif ((teacherInfoAdminMapper.schoolLevel)!1) == 5>
                                <option value="51">小班</option>
                                <option value="52">中班</option>
                                <option value="53">大班</option>
                                <option value="54">学前班</option>
                            <#else >
                                <option value="11">高一</option>
                                <option value="12">高二</option>
                                <option value="13">高三</option>
                            </#if>
                        </select>
                        <input type="text" id="clazzName" name="clazzName" style="width: 100px;">&nbsp;班
                    </li>
                </ul>
            </div>
            <div class="modal-footer" id="edb">
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button id="btn-teacherJoinClazz"  type="button" class="btn btn-primary">提交</button>
            </div>
        </div>
    </div>
</div>

<script>

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

        $('#delegateClazz_dialog_btn').on('click', function(){
            var data = {
                teacherId           :   '${teacherInfoAdminMapper.teacher.id!}',
                clazzId             :   $('#delegateClazzClazzId').text(),
                newTeacherId         :   $('#recipientId').val(),
                delegateClazzDesc   :   $("#delegateClazzDesc").val()
            };
            $.post("/crm/clazz/transfergroup.vpage", data, function(data) {
                if(data.success) {
                    // 强行踢出，使用户重新登录
                    $.ajax({
                        url: "/crm/teacher/kickOutOfApp.vpage",
                        type: "POST",
                        async: false,
                        data: {
                            "userId": '${teacherInfoAdminMapper.teacher.id!}'
                        },
                        success: function (data) {
                        }
                    });
                    alert(data.info);
                    location.href = "teacherhomepage.vpage?teacherId=${teacherInfoAdminMapper.teacher.id!}";
                } else {
                    alert(data.info);
                }
            });
        });

        $(document).on("click", ".delClassTeacher", function() {
            var thisTeacherId = $(this).attr("teacherid");
            var thisGroupId = $(this).attr("groupId");
            if(window.confirm("将老师(ID:"+thisTeacherId+")从该班删除")){
                $.post('/crm/clazz/deletegroup.vpage',{teacherId : thisTeacherId,groupId : thisGroupId},function(data){
                    if(data.success){
                        alert("操作成功");
                    }else{
                        alert("操作失败.原因:\n"+data.info);
                    }
                    location.href = "teacherhomepage.vpage?teacherId=${teacherInfoAdminMapper.teacher.id!}";
                });
            }
        });

        $(document).on("click", "#btn-addClazz", function(){
            $("#modal-addClazz").modal("show");
        });

        $(document).on("click", "#btn-teacherJoinClazz", function(){
            var teacherId = ${(teacherInfoAdminMapper.teacher.id)!''};
            var clazzName = $("#clazzName").val() + "班";
            var clazzLevel = $('#clazz_level').find('option:selected').val();
            if(window.confirm("将老师("+teacherId+")加入到"+clazzLevel+"年级"+clazzName)){
                $.post('/crm/teacher/teacherjoinclazz.vpage',{teacherId : teacherId, clazzLevel:clazzLevel, clazzName: clazzName},function(data){
                    if(data.success){
                        alert("操作成功");
                    }else{
                        alert("操作失败.原因:\n"+data.info);
                    }
                    location.href = "teacherhomepage.vpage?teacherId=${teacherInfoAdminMapper.teacher.id!}";
                });
            }
        });
    });

</script>
</#macro>