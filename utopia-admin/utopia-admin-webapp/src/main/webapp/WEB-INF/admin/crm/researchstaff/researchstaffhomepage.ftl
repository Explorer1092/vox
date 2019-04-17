<#import "../../layout_default.ftl" as layout_default/>
<#--<#import "researchstaffquery.ftl" as researchStaffQuery/>-->
<#import "../chip/customerservicerecordchip.ftl" as recordChip/>
<@layout_default.page page_title="${researchStaffInfoMap.userName!}(${researchStaffInfoMap.userId!})" page_num=3>
    <div class="span9">
        <#--<@researchStaffQuery.queryPage/>-->
        <legend>教研员主页:${researchStaffInfoMap.userName!}(${researchStaffInfoMap.userId!})</legend>
        <ul class="inline">
            <li>
                <button class="btn btn-primary" id="reset_password_button">重置密码</button>
            </li>
            <li>
                <button class="btn btn-primary" id="change_name_button">修改姓名</button>
            </li>
            <#--<li>-->
                <#--<button class="btn btn-primary" id="change_region_button">修改区域</button>-->
            <#--</li>-->
            <#--<li>-->
                <#--<button class="btn btn-primary" id="change_disabled_button">有效性设置</button>-->
            <#--</li>-->
        </ul>
        <table class="table table-hover table-striped table-bordered">
            <tr>
                <th>ID</th>
                <th>姓名</th>
                <th>手机</th>
                <th>密码</th>
                <th>学科</th>
                <th>地区</th>
                <th>园丁豆</th>
                <th>是否可用</th>
            </tr>
            <#if researchStaffInfoMap?has_content>
                <tr>
                    <td>${researchStaffInfoMap.userId!}</td>
                    <td id="researchStaff_name">${researchStaffInfoMap.userName!}</td>
                    <td>
                        <#if researchStaffInfoMap.userId??>
                            <button type="button" id="query_research_staff_phone_${researchStaffInfoMap.userId!''}" class="btn btn-info">查看</button>
                        </#if>
                    </td>
                    <td>
                        <span id="real_code"></span>
                        <#if researchStaffInfoMap.userId??>
                            <button type="button" id="query_user_password_${researchStaffInfoMap.userId!''}" class="btn btn-info">临时密码</button>
                        </#if>
                    </td>
                    <td>${researchStaffInfoMap.subjectName!}</td>
                    <td>
                        <#if researchStaffInfoMap.affairTeacher!false>
                            <a href="/crm/school/schoolhomepage.vpage?schoolId=${researchStaffInfoMap.schoolId!}" target="_blank">${researchStaffInfoMap.schoolName!}</a> （${researchStaffInfoMap.schoolId!}）
                        <#else>
                            ${researchStaffInfoMap.regionName!}（${researchStaffInfoMap.regionCode!}）
                        </#if>
                    </td>
                    <td>
                        <a href="../integral/integraldetail.vpage?userId=${researchStaffInfoMap.userId!}">${researchStaffInfoMap.goldCoin!""}</a>
                    </td>
                    <td>${researchStaffInfoMap.disabled?string("否", "是")}</td>
                </tr>
            </#if>
        </table>
        <br/><br/>
        <@recordChip.recordList customerServiceRecordList=researchStaffInfoMap.customerServiceRecordList userId=researchStaffInfoMap.userId defaultType=6/>
        <#--邀请他人列表-->
        <legend>邀请他人列表</legend>
        <table class="table table-hover table-striped table-bordered">
            <tr>
                <th>姓名</th>
                <th>同意时间</th>
                <th>是否成功</th>
                <th>姓名</th>
                <th>同意时间</th>
                <th>是否成功</th>
            </tr>
            <#if researchStaffInfoMap.inviteeList?has_content>
                <#list researchStaffInfoMap.inviteeList as invitee>
                    <#if invitee_index % 2 = 0><tr></#if>
                        <td>
                            <#if invitee.inviteeId??>
                                <a href="../user/userhomepage.vpage?userId=${invitee.inviteeId}">${invitee.inviteeName!""}</a>(${invitee.inviteeId!})
                            </#if>
                        </td>
                        <td>${invitee.acceptTime!""} </td>
                        <td>${invitee.success?string("是", "否")}</td>
                    <#if invitee_index % 2 = 1 || !invitee_has_next></tr></#if>
                </#list>
            </#if>
        </table>
    </div>
    <!----------------------------dialog----------------------------------------------------------------------------------->
    <div id="username_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>更改用户姓名</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>用户ID</dt>
                        <dd>${researchStaffInfoMap.userId!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>记录类型</dt>
                        <dd>教研员操作</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>新的姓名</dt>
                        <dd><input type="text" name="name" id="name" placeholder="名字中只能使用汉字"/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>问题描述</dt>
                        <dd><textarea id="nameDesc" name="nameDesc" cols="35" rows="5"></textarea></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>所做操作</dt>
                        <dd>更改用户名字。</dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="dialog_edit_researchStaff_name" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="password_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>重置密码</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <ul class="inline">
                    <li>
                        <dt>用户ID</dt>
                        <dd>${researchStaffInfoMap.userId!''}</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>记录类型</dt>
                        <dd>教研员操作</dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>新的密码</dt>
                        <dd><input type="text" name="password" id="password" value="123456"/></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>问题描述</dt>
                        <dd>
                            <div class="btn-group" data-toggle="buttons-radio">
                                <button type="button" class="btn active">TQ在线</button>
                                <button type="button" class="btn">TQ电话</button>
                                <button type="button" class="btn">其他</button>
                            </div>
                        </dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>附加描述</dt>
                        <dd><textarea id="passwordExtraDesc" cols="35" rows="2"></textarea></dd>
                    </li>
                </ul>
                <ul class="inline">
                    <li>
                        <dt>所做操作</dt>
                        <dd>重置用户密码。</dd>
                    </li>
                </ul>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="dialog_edit_researchStaff_password" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>
    <script>
        $(function() {

            $('#reset_password_button').on('click', function() {
                $( "#password").val("123456");
                $('#passwordExtraDesc').val('');
                $("div[class='btn-group'] button").removeClass("active").eq(0).addClass("active");
                $("#password_dialog").modal("show");
            });

            $("#dialog_edit_researchStaff_password").on("click", function(){
                var queryUrl = "../user/resetpassword.vpage";
                $.ajax({
                    type: "post",
                    url: queryUrl,
                    data: {
                        userId : ${researchStaffInfoMap.userId!""},
                        password : $("#password").val(),
                        passwordDesc : $("div[class='btn-group'] button[class='btn active']").html(),
                        passwordExtraDesc : $('#passwordExtraDesc').val()
                    },
                    success: function (data){
                        if(data.success){
                            window.location.reload();
                        }else{
                            alert(data.info);
                        }
                    }
                });
            });

            $('#change_name_button').on('click', function() {
                $('#name').val('');
                $('#nameDesc').val('');
                $("#username_dialog").modal("show");
            });

            $("#dialog_edit_researchStaff_name").on("click", function(){
                var queryUrl = "../user/updateusername.vpage";
                $.ajax({
                    type: "post",
                    url: queryUrl,
                    data: {
                        userId : ${researchStaffInfoMap.userId!''},
                        userName : $("#name").val(),
                        nameDesc : $('#nameDesc').val()
                    },
                    success: function (data){
                        if(data.success){
                            appendNewRecord(data);
                            $("#researchStaff_name").html(data.userName);
                            $("#username_dialog").modal("hide");
                        }else{
                            alert("修改失败，请填写问题描述，并检查名字是否符合规范。");
                        }
                    }
                });
            });
        });

        $('[id^="query_user_password_"]').on('click', function(){
            var item = $(this);
            var id = parseInt(item.attr("id").substr("query_user_password_".length));
            $("#real_code").text("");
            $.get("../user/temppassword.vpage",{
                userId:id
            },function(data){
                $("#real_code").text(data.password);
            });
        });
    </script>

</@layout_default.page>