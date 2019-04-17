<#-- @ftlvariable name="userName" type="java.lang.String" -->
<#-- @ftlvariable name="vitalityLogList" type="java.util.List<com.voxlearning.utopia.admin.data.VitalityMapper>" -->
<#-- @ftlvariable name="userId" type="java.lang.Long" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<div id="main_container" class="span9">
    <div>
        <fieldset>
            <legend>学生<a href="../student/studenthomepage.vpage?studentId=${userId!}">${userName!}</a>活力值详情(最近7天)
            </legend>
        </fieldset>
        <ul class="inline">
            <li>
                <a class="btn" href="pkdetail.vpage?userId=${userId!}">活力详情</a>
            </li>
            <li>
                <button onclick="addVitality()" class="btn">添加活力</button>
            </li>
            <li>
                <button onclick="addExperience()" class="btn">添加经验</button>
            </li>
            <li>
                <button id="change_pk_gender" class="btn">改变性别</button>
            </li>
            <li>
                <button id="clear_role_bag_btn" class="btn">清空装备</button>
            </li>
            <li>
                <a class="btn" href="battlereportlist.vpage?studentId=${userId!}">查询PK记录</a>
            </li>
            <li>
                <a class="btn" href="pktransfercareerinfo.vpage?userId=${userId!}">PK转职记录</a>
            </li>
            <li>
                <button class="btn" id="student_pk_info_btn">PK战胜全班情况</button>
            </li>
            <li>
                <a class="btn" href="pkequipmentlog.vpage?userId=${userId!}">获得PK武器记录</a>
            </li>
            <li>
                <a class="btn" href="pkprizeexchangelog.vpage?userId=${userId!}">奖品兑换记录</a>
            </li>
            <li>
                <a class="btn" href="pkpetlog.vpage?userId=${userId!}">宠物日志</a>
            </li>
            <li>
                <a class="btn" href="exphistory.vpage?userId=${userId!}">经验记录</a>
            </li>
            <#if championflag>
                <li>
                    <label>上周周冠军</label>
                </li>
            </#if>
        </ul>

        <div>
            <table class="table table-hover table-striped table-bordered">
                <tr id="vitalitylog_title">
                    <td> I D</td>
                    <td> 用户 ID</td>
                    <td> 活力值数量</td>
                    <td> 活力值类型</td>
                    <td> 获得时间</td>
                </tr>
                <#if vitalityLogList?has_content>
                    <#list vitalityLogList as vitalityLog>
                        <tr id="vitalitylog_title_${vitalityLog.id!}">
                            <td>${vitalityLog.id!""}</td>
                            <td>
                                <a href="../user/userhomepage.vpage?userId=${vitalityLog.studentId!}"> ${vitalityLog.studentId!}</a>
                            </td>
                            <td>${vitalityLog.vitalityQty!}</td>
                            <td>${vitalityLog.type!}</td>
                            <td>
                                <#if vitalityLog.createTime??>
                                    ${vitalityLog.createTime?string("yyyy-MM-dd HH:mm:ss")}
                                </#if>
                            </td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </div>
    </div>

    <div id="vitality_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>增加活力值</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <dd>
                    <fieldset>
                        <label>学生：${userName!}，ID：${userId!""}</label>
                        <label>操作类型:学生操作</label>
                        <br/>
                        <label>积分的类型:<select name="type" id="type">
                            <option value="1">学生激活老师获得活力值</option>
                            <option value="2">学生激活学生获得活力值</option>
                            <option value="3">阿分题试用任务获得活力值</option>
                            <option value="4">阿分题正式版获得活力值</option>
                            <option value="5">阿分题基础版完成任务获得活力值</option>
                            <option value="6">阿分题试用区域同学完成作业获得活力值</option>
                        </select><br>
                        </label>
                        <label>活力值数量:<input type="text" name="vitalityQty" id="vitalityQty"
                                            class="text ui-widget-content ui-corner-all" value=""
                                            placeholder="只能是数字"/><br></label>
                        <label>活力值备注:<textarea id="vitalityDesc" name="vitalityDesc" cols="35" rows="3"></textarea><br/></label>
                        所做操作:增加用户活力值。
                    </fieldset>
                </dd>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="vitality_dialog_btn" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>


    <div id="experience_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>增加经验值</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <dd>
                    <fieldset>
                        <label>经验值数量:<input type="text" name="experience" id="experience"
                                            class="text ui-widget-content ui-corner-all" value=""
                                            placeholder="只能是数字"/><br></label>
                    </fieldset>
                </dd>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="experience_dialog_btn" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="change_pk_gender_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>改变用户PK角色性别</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <dd>
                    <fieldset>
                        <label>学生：${userName!}，ID：${userId!}</label>
                        <label>操作类型:学生操作</label>
                        <br/>
                        <label>备注:<textarea id="changePkGenderDesc" cols="35" rows="3"></textarea><br/></label>
                        所做操作:改变用户PK角色性别
                    </fieldset>
                </dd>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="change_pk_gender_dialog_btn" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>

    <div id="clear_role_bag_dialog" class="modal hide fade">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
            <h3>清空PK角色装备</h3>
        </div>
        <div class="modal-body">
            <dl class="dl-horizontal">
                <dd>
                    <fieldset>
                        <label>学生：${userName!}，ID：${userId!}</label>
                        <label>操作类型:学生操作</label>
                        <br/>
                        <label>备注:<textarea id="clear_role_bag_desc" cols="35" rows="3"></textarea><br/></label>
                        所做操作：清空用户角色装备
                    </fieldset>
                </dd>
            </dl>
        </div>
        <div class="modal-footer">
            <button id="clear_role_bag_dialog_btn" class="btn btn-primary">确 定</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
        </div>
    </div>
</div>
<script>

    function addVitality() {
        $("#vitalityQty").val("");
        $('#vitalityDesc').val('');
        $("#vitality_dialog").modal("show");
    }

    function addExperience() {
        $('#experience').val('');
        $("#experience_dialog").modal("show");
    }

    $(function () {

        $('#vitality_dialog_btn').on('click', function () {
            var queryUrl = "addvitality.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId: ${userId!""},
                    type: $("#type").val(),
                    vitalityQty: $("#vitalityQty").val(),
                    vitalityDesc: $('#vitalityDesc').val()
                },
                success: function (data) {
                    if (data.success) {
                        $("#vitality_dialog").modal("hide");
                        location.href = '?userId=${userId!}';
                    } else {
                        alert("增加活力失败，请检查是否正确填写数量和备注。");
                    }
                }
            });
        });

        $('#experience_dialog_btn').on('click', function () {
            var queryUrl = "addexperience.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId: ${userId!""},
                    experience: $('#experience').val()
                },
                success: function (data) {
                    if (data.success) {
                        $("#experience_dialog").modal("hide");
                        location.href = '?userId=${userId!}';
                    } else {
                        alert("增加精力失败，请检查是否正确填写数量。");
                    }
                }
            });
        });

        $('#change_pk_gender').click(function () {
            $('#changePkGenderDesc').val('');
            $('#change_pk_gender_dialog').modal('show');
        });

        $('#change_pk_gender_dialog_btn').click(function () {
            if (confirm('改变PK性别会删除所有背包中的物品，确认继续？')) {
                var postData = {
                    userId: '${userId!}',
                    changePkGenderDesc: $('#changePkGenderDesc').val()
                };
                $.post('changepkgender.vpage', postData, function (data) {
                    alert(data.info);
                    if (data.success) {
                        $('#change_pk_gender_dialog').modal('hide');
                    }
                });
            }
        });

        $('#clear_role_bag_btn').click(function () {
            $('#clear_role_bag_desc').val('');
            $('#clear_role_bag_dialog').modal('show');
        });

        $('#clear_role_bag_dialog_btn').click(function () {
            if (confirm('删除背包中所有物品，确认继续？')) {
                var postData = {
                    studentId: '${userId!}',
                    clearRoleBagDesc: $('#clear_role_bag_desc').val()
                };

                $.post('clearrolebag.vpage', postData, function (data) {
                    alert(data.info);
                    if (data.success) {
                        $('#clear_role_bag_dialog').modal('hide');
                    }
                });
            }
        });

        $('#student_pk_info_btn').click(function () {
            $.get('studentpkinfo.vpage', {studentId: '${userId!}'}, function (data) {
                alert(data.info);
            });
        });

        $('#student_pk_transfer_career_btn').click(function () {


        });

    });

</script>
</@layout_default.page>