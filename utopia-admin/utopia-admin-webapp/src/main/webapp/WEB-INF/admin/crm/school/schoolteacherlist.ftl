<#import "../../layout_default.ftl" as layout_default/>
<@layout_default.page page_title="CRM" page_num=3>
    <legend>
        老师列表&nbsp;<a href="/crm/school/schoolhomepage.vpage?schoolId=${schoolId}">返回学校主页</a>
    </legend>
    <ul class="inline">
        <li>
            <span>暂停批量删除</span>
            <#--<input type="button" id="btn_batch_del" class="btn" value="批量删除" />-->
        </li>
        <li>
            <span>暂停批量修改学校</span>
            <#--<input type="button" id="btn_batch_move" class="btn" value="批量修改学校" />-->
        </li>
    </ul>
    <div id="main_container" class="span9">
        <table id="schools" class="table table-hover table-striped table-bordered">
            <tr>
                <td></td>
                <th>ID</th>
                <th>老师名称</th>
                <th>学科</th>
                <th>是否认证</th>
                <th>未认证原因</th>
                <th>邮箱</th>
                <th>手机</th>
                <th>所在班级数</th>
                <th>园丁豆</th>
                <th>累计邀请人数</th>
                <th>活跃等级</th>
                <th>活跃时间</th>
                <th>创建时间</th>
                <#if ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv()>
                    <th>更改认证状态</th>
                </#if>
                <th>新增备注</th>
            </tr>
            <#if teachers?has_content>
                <#list teachers as teacherMap>
                    <tr>
                        <td><input type="checkbox" id="${teacherMap.teacher.id!}" name="chkteacher" /></td>
                        <td>${teacherMap.teacher.id!}</td>
                        <td><a target="_blank" href="/crm/teacher/teacherhomepage.vpage?teacherId=${teacherMap.teacher.id!}">${teacherMap.teacher.profile.realname!}</a></td>
                        <td><#if teacherMap.teacher.subject??>${teacherMap.teacher.subject.value!''}</#if> </td>
                        <td id="teacher_certification_state_${teacherMap.teacher.id!}">
                            ${teacherMap.certificationState!}
                        </td>
                        <td>
                            <#if !teacherMap.certificationCondition.enoughStudentsFinishedHomework>没有足够的学生完成作业<br></#if>
                            <#if !teacherMap.certificationCondition.enoughStudentsBindParentMobile>不足3人绑定手机<br></#if>
                            <#if !teacherMap.certificationCondition.mobileAuthenticated>老师手机未认证</#if>
                        </td>
                        <td>${teacherMap.teacher.profile.sensitiveEmail!}</td>
                        <td>${teacherMap.teacher.profile.sensitiveMobile!}</td>
                        <#if teacherClassNum[teacherMap.teacher.id?string]?has_content>
                            <td class="classnum">${teacherClassNum[teacherMap.teacher.id?string]}
                            <#if otherClassNum[teacherMap.teacher.id?string]?has_content && teacherClassNum[teacherMap.teacher.id?string]!="0">
                                (<span class="scnum">${otherClassNum[teacherMap.teacher.id?string]}</span>个班中有其他老师)</td>
                            <#elseif teacherClassNum[teacherMap.teacher.id?string]="0">
                            <#else>
                            (没有同班老师)
                            </#if>
                            </td>
                        <#else>
                            <td class="classnum">0</td>
                        </#if>
                        <td><a href="../integral/integraldetail.vpage?userId=${teacherMap.teacher.id}">${teacherMap.integral!}</a></td>
                        <td>${teacherMap.inviteCount!}</td>
                        <td>${teacherMap.teacher.level!}</td>
                        <td>${teacherMap.activityTime!}</td>
                        <td>${teacherMap.teacher.createTime!}</td>
                        <#if ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv()>
                            <td><input type="button" onclick="updateAuthentication(${teacherMap.teacher.id!});" class="btn btn-info" value="更改" /></td>
                        </#if>
                        <td><input type="button" onclick="addCustomerServiceRecord(${teacherMap.teacher.id!});" class="btn btn-primary" value="新增备注" /></td>
                    </tr>
                </#list>
            <#else>
                <tr>
                    <td colspan="6">未查询到数据</td>
                </tr>
            </#if>
        </table>
    </div>
    <div id="del_dialog" class="modal fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">删除确认</h4>
                </div>
                <div class="modal-body">
                    <ul class="inline">
                        <li>删除说明:</li>
                        <li>
                            <textarea id="delDesc"></textarea>
                        </li>
                    </ul>
                </div>
                <div class="modal-footer">
                    <input type="hidden" id="delids" />
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="btn_del_submit"  type="button" class="btn btn-primary">提交</button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->

    <div id="change_school" class="modal fade">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">批量修改学校，请输入学校ID</h4>
                </div>
                <div class="modal-body" id="scid">
                    <ul class="inline">
                        <li>学校ID:</li>
                        <li>
                            <input type="text" size="20" id="changeSchoolId"/>
                        </li>
                    </ul>
                </div>
                <div class="modal-body" id="proc">
                    <ul class="inline">
                        <li>处理中......</li>
                    </ul>
                    <ul class="inline">
                        <li>共操作<span></span>个老师，正在操作第<span></span>个......</li>
                    </ul>
                </div>
                <div class="modal-footer" id="edb">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    <button id="btn_change_submit"  type="button" class="btn btn-primary">提交</button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->
    <div id="authentication_dialog" class="modal hide fade" style="width: 700px; margin-left:-350px; ">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>更新认证状态</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>用户ID</dt>
                        <dd id="auth_teacher_id"></dd>
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
                        <dt>认证状态</dt>
                        <dd>
                            <select id="dialog_authenticationState" class="multiple">
                                <option value="0">等待认证</option>
                                <option value="1">认证成功</option>
                                <option value="3">取消认证</option>
                            </select>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>问题描述</dt>
                        <dd>
                            <div id="authentication_dialog_radio" class="btn-group" data-toggle="buttons-radio">
                                <button type="button" class="btn active">市场人员反馈</button>
                                <button type="button" class="btn">老师主动申请</button>
                                <button type="button" class="btn">客服电话外呼认证</button>
                                <button type="button" class="btn">其他</button>
                            </div>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>附加描述</dt>
                        <dd><textarea id="authenticationExtraDesc" cols="35" rows="2"></textarea></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>所做操作</dt>
                        <dd>更新用户认证状态</dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="dialog_edit_teacher_authentication" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>
    <div id="record_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>用户进线记录</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>用户ID</dt>
                        <dd id="record_teacher_id"></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>记录类型</dt>
                        <dd>
                            <select name="recordType" id="recordType" class="multiple">
                                <#if recordTypeList?has_content>
                                    <#list recordTypeList as recordType>
                                        <option value='${recordType.key}'>${recordType.value}</option>
                                    </#list>
                                </#if>
                            </select>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>问题描述</dt>
                        <dd><textarea id="questionDesc" name="questionDesc" cols="35" rows="3"></textarea></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>所做操作</dt>
                        <dd><textarea id="operation" name="operation" cols="35" rows="3"></textarea></dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="dialog_edit_teacher_date" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>
<script type="text/javascript">
    $(function(){

        $('#btn_batch_del').on('click',function(){
            var ids = getSelectedTeachers();
            if($.trim(ids)==""){
                alert("请先选取老师");
                return;
            }
            $('#delids').val(ids);

            var $dialog = $('#del_dialog');
            $dialog.modal('show');
        });

        //批量修改学校
        $(document).on("click", "#btn_batch_move", function() {
            var ids = "";
            var unchangableNum = 0;
            $('input[name="chkteacher"]').each(function(data){
                if($(this).is(':checked')){
                    var otherClassNum = $(this).parent().siblings(".classnum").find("span");
                    if($(otherClassNum).length>0){
                        if(parseInt($(otherClassNum).text())>0){
                            unchangableNum++;
                            return;
                        }
                    }
                    ids+=$(this).attr('id')+",";
                }
            });
            if(unchangableNum > 0){
                alert("所选中的老师还有同班老师，不可修改学校");
                return;
            }
            if($.trim(ids)==""){
                alert("请先选取老师");
                return;
            }

            var $dialog = $('#change_school');
            $dialog.modal('show');
            $("#scid").show();
            $("#proc").hide();
            $("#edb").show();
            ids = ids.split(",");
            $( document ).on("click", "#btn_change_submit", function() {
                $("#scid").hide();
                $("#proc").show();
                $("#edb").hide();
                queueUpdateTeacherSchool(ids,0,0,'',0,'');
            });
        });

        $('#btn_del_submit').on('click',function(){
            var ids = $('#delids').val();
            var desc = $('#delDesc').val();
            $.post('batchdelteacher.vpage',{
                teacherIds:ids,
                delDesc:desc
            },function(data){
                if(data.success){
                    alert(data.info);
                    window.location.reload();
                }else{
                    alert(data.info);
                }
            });
        });

        function getSelectedTeachers(){
            var ids='';
            $('input[name="chkteacher"]').each(function(data){
                if($(this).is(':checked')){
                    if(ids.length > 0){
                        ids+=',';
                    }
                    ids+=$(this).attr('id');
                }
            });
            return ids;
        }

        var queueUpdateTeacherSchool = function(idArray,index,successCount,successStr,failCount,failStr){
            $("#proc").find(".inline:last").find("span:first").text(idArray.length-1)
            $("#proc").find(".inline:last").find("span:last").text(index+1)
            var teacherId = idArray[index];
            try{
                teacherId = parseInt(teacherId);
            }catch(e){
                return;
            }

            if(!teacherId>0){
                return;
            }
            $.post('/crm/teacher/changeschool.vpage',{
                teacherId:teacherId,
                schoolId:$("#changeSchoolId").val(),
                changeSchoolDesc:"批量修改学校"
            },function(data){
                if(!data.success){
                    failCount++;
                    failStr+=teacherId+" 原因:"+data.info+"\n";
                }else{
                    successCount++;
                    successStr+=teacherId+"\n";

                }
                if((index+2) < idArray.length){
                    queueUpdateTeacherSchool(idArray,index+1,successCount,successStr,failCount,failStr);
                }
                else{
                    if(failCount==0){
                        alert("批量操作"+successCount+"个老师全部成功。ID列表：\n"+successStr);
                    }else{
                        alert("操作结果：\n成功"+successCount+"个:\n"+successStr+"\n失败"+failCount+"个:\n"+failStr);
                    }
                    $("#proc").find(".inline:first").hide();
                    $("#proc").find(".inline:last").text("刷新页面...")
                    window.location.reload();
                }
            });
        }

        $("#dialog_edit_teacher_authentication").on("click", function(){
            var queryUrl = "../teacher/teacherauthentication.vpage";
            var userId = $("#auth_teacher_id").html().trim();
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId : userId,
                    authenticationState : $("#dialog_authenticationState").val(),
                    authenticationDesc : $("#authentication_dialog_radio button[class='btn active']").html(),
                    authenticationExtraDesc : $('#authenticationExtraDesc').val()
                },
                success: function (data){
                    if(data.success){
                        alert("修改老师认证状态成功！");
                        $("#authentication_dialog").modal("hide");
                        $("#teacher_certification_state_"+userId).html(data.authenticationState);
                    }else{
                        alert("修改老师认证状态失败，请更改老师认证状态，并填写问题描述。");
                    }
                }
            });
        });

        $("#dialog_edit_teacher_date").on("click", function(){
            var queryUrl = "../user/addcustomerrecord.vpage";
            var userId = $("#record_teacher_id").html().trim();
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId : userId,
                    recordType : $("#recordType").val(),
                    questionDesc : $("#questionDesc").val(),
                    operation : $("#operation").val()
                },
                success: function (data){
                    $("#record_success").val(data.success);

                    if(data.success){
                        alert("增加日志成功。");
                    }else{
                        alert("增加日志失败。");
                    }
                    $("#record_dialog").modal("hide");
                }
            });
        });

    });
    function updateAuthentication(teacherId){
        $('#authenticationExtraDesc').val('');
        $('#auth_teacher_id').html(teacherId);
        $("#authentication_dialog_radio button").removeClass("active").eq(0).addClass("active");
        $("#authentication_dialog").modal("show");
    }
    function addCustomerServiceRecord(teacherId){
        $("#questionDesc").val('');
        $("#record_teacher_id").html(teacherId);
        $("#operation").val('');
        $('#recordType').val('1');
        $("#record_dialog").modal("show");
    }
</script>
</@layout_default.page>