<#import "../../layout_default.ftl" as layout_default>
<#import "teachercondition.ftl" as teacherConditionList>
<#import "../headsearch.ftl" as headsearch>
<@layout_default.page page_title="老师查询" page_num=3>
    <#setting datetime_format="yyyy-MM-dd"/>
<style>
    .table_soll table td, .table_soll table th {
        white-space: nowrap;
    }

    .my-dialog .ui-dialog-titlebar-close {
        display: none;
    }
</style>

<div class="span9">
    <@headsearch.headSearch/>
    <@teacherConditionList.teacherCondition/>
    <#if teacherMapList?has_content>
        <div class="table_soll">
            <ul class="inline">
                <li><input id="ck-all" type="checkbox"></li>
                <li>选中当前页的老师</li>
                <li><input id="new-task" type="button" value="新建任务"/></li>
                <li><input id="new-clue" type="button" value="新建线索"/></li>
            </ul>
            <table id="teachers" data-mode="columntoggle" class="table table-hover table-striped table-bordered">
                <tr>
                    <th></th>
                    <th>功能操作</th>
                    <th>老师姓名</th>
                    <th>认证状态</th>
                    <th>认证条件</th>
                    <th>判假状态</th>
                    <th><a id="array_latestConnectedTime" href="javascript:;">最近接通时间</a></th>
                    <th><a id="array_latestOutCallTime" href="javascript:;">最近拨打时间</a></th>
                    <th><a id="array_outCallCount" href="javascript:;">当日呼叫次数</a></th>
                    <th>最近拨打人</th>
                    <th>最近使用时间</th>
                    <th>电话</th>
                    <th>学科</th>
                    <th>学校</th>
                </tr>
                <#if teacherMapList?has_content>
                    <#list teacherMapList as teacher>
                        <#if teacher?has_content>
                            <#assign teacherCCInfo = ccRecordInfo[(teacher.teacherId!'')?string]>
                            <tr>
                                <td>
                                    <input type="checkbox" user-id="${teacher["teacherId"]!}" name="ck-user"/>
                                </td>
                                <td>
                                    <button id="login_${teacher["teacherId"]!}" name="login">登录</button>
                                    <button onclick="record.new(${teacher["teacherId"]!})">填写记录</button>
                                    <a target="_blank" href="/crm/teachernew/teacherdetail.vpage?teacherId=${teacher["teacherId"]!}&selectTab=5">
                                        <button id="task" name="task">任务(${teacher["taskCount"]!0})</button>
                                    </a>
                                </td>
                                <td>
                                    <a target="_blank" href="teacherdetail.vpage?teacherId=${teacher["teacherId"]!}">${teacher["teacherName"]!''}</a>${teacher["teacherId"]!}
                                </td>
                                <td <#if teacher["authStatus"].name() == "SUCCESS">style="color: green"</#if>>${(teacher["authStatus"].description)!}</td>
                                <td>
                                    <#if teacher.authCond1Reached?? && teacher.authCond1Reached?string == "true"><img src="/public/img/w-icon-11.png"><#else ><img src="/public/img/w-icon-10.png"></#if>
                                    <#if teacher.authCond2Reached?? && teacher.authCond2Reached?string == "true"><img src="/public/img/w-icon-21.png"><#else ><img src="/public/img/w-icon-20.png"></#if>
                                    <#if teacher.authCond3Reached?? && teacher.authCond3Reached?string == "true"><img src="/public/img/w-icon-31.png"><#else ><img src="/public/img/w-icon-30.png"></#if>
                                </td>
                                <td><#if teacher["fakeTeacher"]?has_content><#if teacher["fakeTeacher"] ==true >假老师(${teacher["fakeDesc"]!""})<#else >--</#if></#if></td>
                                <td><#if teacherCCInfo?has_content>${teacherCCInfo["latestConnectedTime"]!''}</#if></td>
                                <td><#if teacherCCInfo?has_content>${teacherCCInfo["latestOutCallTime"]!''}</#if></td>
                                <td><#if teacherCCInfo?has_content>${teacherCCInfo["outCallCount"]!''}</#if></td>
                                <td><#if teacherCCInfo?has_content>${teacherCCInfo["latestOutCallUser"]!''}</#if></td>
                                <td><#if teacher["latestAssignHomeworkTime"]?has_content>${teacher["latestAssignHomeworkTime"]!''}</#if></td>
                                <td><#if teacher["mobile"]?has_content>
                                    <button type="button" id="query_user_phone_${teacher["teacherId"]!}">查 看
                                    </button></#if>
                                </td>
                                <td>${teacher["subject"]!""}</td>
                                <td>${teacher["schoolName"]!""}</td>
                            </tr>

                        </#if>
                    </#list>
                </#if>
            </table>
            <ul class="inline" style="clear: both; padding-top: 40px;">
                <li>
                    <a id='first_page' href="javascript:void(0)">首页</a>
                </li>
                <li>
                    <a id='pre_page' href="javascript:void(0)">上一页</a>
                </li>
                <li>
                    <a>${currentPage!''}/${totalPage!"1"}</a>
                </li>
                <li>
                    <a id="next_page" href="javascript:void(0)">下一页</a>
                </li>
                <li>
                    <a id='last_page' href="javascript:void(0)">末页</a>
                </li>
                <li>
                    <input id="goto_page_num" value="" style="width: 30px;">
                    <a id='goto_page' href="javascript:void(0)">跳转到</a>
                </li>
                <li>
                    <div id='total' href="javascript:void(0)">总计${totalCount}条记录</div>
                </li>

            </ul>
        </div>
    <#else >
        未查询到任何满足条件的老师，请检查或更换查询条件
    </#if>
</div>

<#--登录老师账号-->
<div id="teacherLogin_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>登录老师账号</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>老师ID</dt>
                    <dd id="login_teacherId"></dd>
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
                    <dt>问题描述</dt>
                    <dd><textarea id="teacherLoginDesc" name="teacherLoginDesc" cols="35" rows="4"></textarea></dd>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <dt>所做操作</dt>
                    <dd>管理员登录老师账号</dd>
                </li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button id="teacher_login_btn" class="btn btn-primary">确 定</button>
        <button class="btn" data-dismiss="modal" aria-hidden="true">取 消</button>
    </div>

    <#include "../task/common/task_new.ftl">
    <#include "../task/common/record_new.ftl">
    <#include "../task/common/clue_new.ftl">
    <#include "../task/common/alert_box.ftl">
</div>

<script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>
<script type="text/javascript">
    $(function () {
        dater.render();

        $("#ck-all").click(function () {
            var checkAll = document.getElementById("ck-all");
            var checked = checkAll.checked == true;
            var checkUser = document.getElementsByName("ck-user");
            for (var i = 0; i < checkUser.length; i++) {
                checkUser[i].checked = checked;
            }
        });

        $("#new-task").click(function () {
            var userIds = [];
            var checkUser = document.getElementsByName("ck-user");
            for (var i = 0; i < checkUser.length; i++) {
                if (checkUser[i].checked == true) {
                    try {
                        var userId = checkUser[i].getAttribute("user-id");
                        if (validNumber(userId)) {
                            userIds.push(userId);
                        }
                    } catch (e) {
                    }
                }
            }
            if (emptyArray(userIds)) {
                alert("请选择老师！");
            } else {
                task.batchNew(userIds);
            }
        });

        $("#new-clue").click(function () {
            var userIds = [];
            var checkUser = document.getElementsByName("ck-user");
            for (var i = 0; i < checkUser.length; i++) {
                if (checkUser[i].checked == true) {
                    try {
                        var userId = checkUser[i].getAttribute("user-id");
                        if (validNumber(userId)) {
                            userIds.push(userId);
                        }
                    } catch (e) {
                    }
                }
            }
            if (emptyArray(userIds)) {
                alert("请选择老师！");
            } else {
                clue.batchNew(userIds, "teacher");
            }
        });
    });

    $('[id^="login_"]').on("click", function () {
        var tid = $(this).attr("id").substring("login_".length);
        $("#login_teacherId").html(tid);
        $("#teacherLoginDesc").val("");
        $("#teacherLogin_dialog").modal("show");
    });

    //登录老师账号
    $("#teacher_login_btn").on("click", function () {
        var queryUrl = "../teacher/teacherlogin.vpage";
        $.ajax({
            type: "post",
            url: queryUrl,
            data: {
                teacherId: $("#login_teacherId").html(),
                teacherLoginDesc: $("#teacherLoginDesc").val()
            },
            success: function (data) {
                if (data.success) {
                    $("#teacherLogin_dialog").modal("hide");
                    var postUrl = data.postUrl;
                    window.open(postUrl);
                } else {
                    alert("登录老师账号失败。");
                }
            }
        });
    });


    function FixTable(TableID, FixColumnNumber, width, height) {
        if ($("#" + TableID + "_tableLayout").length != 0) {
            $("#" + TableID + "_tableLayout").before($("#" + TableID));
            $("#" + TableID + "_tableLayout").empty();
        }
        else {
            $("#" + TableID).after("<div id='" + TableID + "_tableLayout' style='overflow:hidden;height:" + height + "; width:" + width + ";'></div>");
        }
        $('<div id="' + TableID + '_tableFix"></div>'
        + '<div id="' + TableID + '_tableHead"></div>'
        + '<div id="' + TableID + '_tableColumn"></div>'
        + '<div id="' + TableID + '_tableData"></div>').appendTo("#" + TableID + "_tableLayout");
        var oldtable = $("#" + TableID);
        var tableFixClone = oldtable.clone(true);
        tableFixClone.attr("id", TableID + "_tableFixClone");
        $("#" + TableID + "_tableFix").append(tableFixClone);
        var tableHeadClone = oldtable.clone(true);
        tableHeadClone.attr("id", TableID + "_tableHeadClone");
        $("#" + TableID + "_tableHead").append(tableHeadClone);
        var tableColumnClone = oldtable.clone(true);
        tableColumnClone.attr("id", TableID + "_tableColumnClone");
        $("#" + TableID + "_tableColumn").append(tableColumnClone);
        $("#" + TableID + "_tableData").append(oldtable);
        $("#" + TableID + "_tableLayout table").each(function () {
            $(this).css("margin", "0");
        });
        var HeadHeight = $("#" + TableID + "_tableHead thead").height();
        HeadHeight += 2;
        $("#" + TableID + "_tableHead").css("height", HeadHeight);
        $("#" + TableID + "_tableFix").css("height", HeadHeight);
        var ColumnsWidth = 0;
        var ColumnsNumber = 0;
        $("#" + TableID + "_tableColumn tr:last td:lt(" + FixColumnNumber + ")").each(function () {
            ColumnsWidth += $(this).outerWidth(true);
            ColumnsNumber++;
        });
        ColumnsWidth += 2;
        /*if ($.browser.msie) {
            switch ($.browser.version) {
                case "7.0":
                    if (ColumnsNumber >= 3) ColumnsWidth--;
                    break;
                case "8.0":
                    if (ColumnsNumber >= 2) ColumnsWidth--;
                    break;
            }
        }*/
        $("#" + TableID + "_tableColumn").css("width", ColumnsWidth);
        $("#" + TableID + "_tableFix").css("width", ColumnsWidth);
        $("#" + TableID + "_tableData").scroll(function () {
            $("#" + TableID + "_tableHead").scrollLeft($("#" + TableID + "_tableData").scrollLeft());
            $("#" + TableID + "_tableColumn").scrollTop($("#" + TableID + "_tableData").scrollTop());
        });
        $("#" + TableID + "_tableFix").css({"overflow": "hidden", "position": "relative", "z-index": "50", "background-color": "Silver"});
        $("#" + TableID + "_tableHead").css({"overflow": "hidden", "width": width - 17, "position": "relative", "z-index": "45", "background-color": "#fff"});
        $("#" + TableID + "_tableColumn").css({"overflow": "hidden", "height": height - 17, "position": "relative", "z-index": "40", "background-color": "#fff"});
        $("#" + TableID + "_tableData").css({"overflow": "auto", "width": width, "height": height, "position": "absolute", "z-index": "35"});
        if ($("#" + TableID + "_tableHead").width() > $("#" + TableID + "_tableFix table").width()) {
            $("#" + TableID + "_tableHead").css("width", $("#" + TableID + "_tableFix table").width());
            $("#" + TableID + "_tableData").css("width", $("#" + TableID + "_tableFix table").width() + 17);
        }
        if ($("#" + TableID + "_tableColumn").height() > $("#" + TableID + "_tableColumn table").height()) {
            $("#" + TableID + "_tableColumn").css("height", $("#" + TableID + "_tableColumn table").height());
            $("#" + TableID + "_tableData").css("height", $("#" + TableID + "_tableColumn table").height() + 17);
        }
        $("#" + TableID + "_tableFix").offset($("#" + TableID + "_tableLayout").offset());
        $("#" + TableID + "_tableHead").offset($("#" + TableID + "_tableLayout").offset());
        $("#" + TableID + "_tableColumn").offset($("#" + TableID + "_tableLayout").offset());
        $("#" + TableID + "_tableData").offset($("#" + TableID + "_tableLayout").offset());
    }

    $(document).ready(function () {
        FixTable("teachers", 1, "98%", "auto");
    });
</script>
</@layout_default.page>