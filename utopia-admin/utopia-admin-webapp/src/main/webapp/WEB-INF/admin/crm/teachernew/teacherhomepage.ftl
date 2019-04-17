<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <link href="${requestContext.webAppContextPath}/public/css/bootstrap.css" rel="stylesheet">
    <link href="${requestContext.webAppContextPath}/public/css/admin.css" rel="stylesheet">
    <link href="${requestContext.webAppContextPath}/public/css/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
    <script src="${requestContext.webAppContextPath}/public/js/jquery-1.9.1.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/template.js"></script>
    <style>
        html,body,input,button { font-family: "Microsoft Yahei"; }
        .table_soll {
            overflow-y: hidden;
            overflow-x: auto;
        }

        .table_soll table td, .table_soll table th {
            white-space: nowrap;
        }

        .basic_info {
            margin-right: 2em;
        }

        .txt {
            font-weight: 800
        }

        .button_label {
            with: 7em;
            height: 3em;
            margin-top: 1em
        }

        .info_td {
            width: 7em;
        }

        .info_td_txt {
            width: 13em;
            font-weight: 600
        }

        .personalInfo { margin: 0.6em 0 0; padding: 0.3em 0; border-top: 2px #000 solid; }
        .info_td_txt_blue { color: #08c; }
        .info_td_txt_green { color: #00a30c; }
        .personalTab { margin: 2em 0 0; overflow: auto; border-top: 2px #949494 solid; }
        .personalTab a { float: left; padding: 0 1em; color: #333; font-size: 1em; line-height: 2.4em; }
        .personalTab a:hover,.personalTab .active,.personalTab a:visited { color: #fff; background-color: #949494; text-decoration: none; }
        ul.inline > li,ol.inline > li { padding-left: 0; }
        ul.inline { margin-top: 10px; }
        ul.inline table { width: 100%; margin-bottom: 10px; }
        ul.inline table td { padding: 0.5em 0; text-align: center; }
        ul.inline table td.middle-1 { border-left: 4px #ccc solid; }
        ul.inline table td.middle-2 { border-right: 4px #ccc solid; }
        ul.inline table td div,ul.inline table td input { display: inline-block; }
        ul.inline table td input { margin: 0 auto; width: 7em; }
        ul.inline table .info_td { text-align: left; width: 25%; }
        iframe { border: 1px #000 solid; }
        legend { margin-bottom: 0; border-color: #ccc; }
        iframe { border: 0 none; height: auto; }
        .noBgColor { background: none; }
    </style>
<#include "../../common/query_sensitive.ftl" />
</head>
<body class="noBgColor">
<div>
<#if teacherInfoMap?has_content>
    <ul class="inline" style="margin-top: 2em">
        <legend style="width:20%; font-weight: 700;">基础信息</legend>
        <table>
            <tr>
                <td class="info_td">注册方式：<span class="info_td_txt"><#if teacherInfoMap.regType??>${teacherInfoMap.regType!''}</#if></span></td>
                <td class="info_td">注册时间：<span class="info_td_txt"><#if teacherInfoMap.registerTime??>${teacherInfoMap.registerTime!""}</#if></span></td>
                <td class="info_td">注册邀请人：
                    <span class="info_td_txt">
                        <#if teacherInfoMap.inviterId??>
                            <a target="_blank" href="teacherdetail.vpage?teacherId=${teacherInfoMap.inviterId!''}">
                            ${teacherInfoMap.inviterName!''}
                            </a>(${teacherInfoMap.inviterId!''})
                        </#if>
                    </span>
                </td>

                <td class="info_td">是否绑定微信：
                    <span class="info_td_txt info_td_txt_blue">
                        <#if teacherInfoMap.wechatBinded??>
                            <#if teacherInfoMap.wechatBinded ==true>
                                <a href="/crm/teachernew/teacherwechathistory.vpage?teacherId=${teacherInfoMap.id!}" target="_blank">是</a>
                            <#else>
                                否
                            </#if>
                        </#if>
                    </span>
                </td>
            </tr>
            <tr>
                <td class="info_td">是否使用APP：
                    <span class="info_td_txt">
                        <#if teacherInfoMap.appUsed??>
                            <#if teacherInfoMap.appUsed ==true>
                                是
                            </#if>
                        <#else>
                            否
                        </#if>
                    </span>
                </td>
                <#if teacherInfoMap.appUsed??><#if teacherInfoMap.appUsed ==true>
                    <td class="info_td">注册APP时间：
                        <span class="info_td_txt">${teacherInfoMap.appCreateTime?string("yyyy-MM-dd")!}</span>
                    </td>
                </#if></#if>
                <td class="info_td">最近登录时间：
                    <span class="info_td_txt"> ${teacherInfoMap.latestLoginTime!''}</span>
                </td>
            </tr>
        </table>
        <legend style="width:20%; font-weight: 700;">使用信息</legend>
        <table>
            <tr>
                <td class="info_td">最近使用时间：<span class="info_td_txt"><#if teacherInfoMap.latestUseTime??>${teacherInfoMap.latestUseTime?number_to_date}</#if></span></td>
                <td class="info_td">名下班级数：<span class="info_td_txt"><#if teacherInfoMap.clazzCount??>${teacherInfoMap.clazzCount!"0"}个</#if></span></td>
                <td class="info_td">使用次数：<span class="info_td_txt"><#if teacherInfoMap.totalHomeworkCount??>${teacherInfoMap.totalHomeworkCount!"0"}次</#if></span></td>
                <td class="info_td">园丁豆：<span class="info_td_txt">${teacherInfoMap.integral!''}</span></td>
            </tr>
        </table>
        <legend style="width:20%; font-weight: 700;">认证信息</legend>
        <table>
            <tr>
                <td class="info_td">认证状态：
                    <div>
                        <#if teacherInfoMap.isAuth?? &&teacherInfoMap.isAuth == true>
                            <span class="info_td_txt info_td_txt_green">已认证</span>
                        </#if>
                        <#if teacherInfoMap.authCond1Reached?? && teacherInfoMap.authCond1Reached == true>
                            <img title="认证一:8个同学完成3次作业" src="/public/img/a-1.png" style="height: 30px;width: 30px;margin-bottom: .2em;">
                        <#else>
                            <img title="认证一:8个同学完成3次作业" src="/public/img/w-a-1.png" style="height: 30px;width: 30px;margin-bottom: .2em;">
                        </#if>
                        <#if teacherInfoMap.authCond2Reached?? &&teacherInfoMap.authCond2Reached == true>
                            <img title="认证二:设置姓名并绑定手机" src="/public/img/a-2.png" style="height: 30px;width: 30px;margin-bottom: .2em;">
                        <#else>
                            <img title="认证二:设置姓名并绑定手机" src="/public/img/w-a-2.png" style="height: 30px;width: 30px;margin-bottom: .2em;">
                        </#if>
                        <#if teacherInfoMap.authCond3Reached?? &&teacherInfoMap.authCond3Reached == true>
                            <img title="认证三:验证学生，至少3名学生，每人绑定了家长手机或自己手机" src="/public/img/a-3.png" style="height: 30px;width: 30px;margin-bottom: .2em;">
                        <#else>
                            <img title="认证三:验证学生，至少3名学生，每人绑定了家长手机或自己手机" src="/public/img/w-a-3.png" style="height: 30px;width: 30px;margin-bottom: .2em;">
                        </#if>
                    </div>
                </td>
                <td class="info_td">认证时间：<span class="info_td_txt"><#if teacherInfoMap.authTime??>${teacherInfoMap.authTime!""}</#if></span></td>
                <td class="info_td">认证方式：<span class="info_td_txt"><#if teacherInfoMap.authType??>${teacherInfoMap.authType!''}</#if></span></td>
                <td class="info_td">认证学生数：<span class="info_td_txt"><#if teacherInfoMap.authStudentCount??>${teacherInfoMap.authStudentCount!'0'}个</#if></span></td>
            </tr>
        </table>
    </ul>
</#if>
    <legend style="font-weight: 700;">用户备注</legend>
<#if !requestContext.getCurrentAdminUser().isCsosUser()>
    <ul class="inline">
        <li>
            <button class="btn btn-primary" onclick="addCustomerServiceRecord()">新增备注</button>
        </li>
        <li>
            <button id="hide_record_btn" class="btn btn-primary" onclick="hideCustomerServiceRecord()">隐藏备注</button>
        </li>
    </ul>
</#if>
<#if teacherInfoMap?? && teacherInfoMap.customerServiceRecordList??>
    <table id="customer_service_record" class="table table-hover table-striped table-bordered">
        <tr id="comment_title">
            <th style="width: 6em;">用户ID</th>
            <th style="width: 6em;">添加人</th>
            <th style="width: 6em;">添加人姓名</th>
            <th style="width: 10em;">创建时间</th>
            <th style="width: 10em;">问题描述</th>
            <th style="width: 20em;">所做操作</th>
            <th style="width: 6em;">类型</th>
        </tr>
        <#list teacherInfoMap.customerServiceRecordList as record >
            <tr>
                <td style="width: 6em;">${record.userId!""}</td>
                <td style="width: 6em;">${record.operatorId!""}</td>
                <td style="width: 6em;">${record.operatorName!""}</td>
                <td style="width: 10em;">${record.createTime!""}</td>
                <td style="width: 10em;">${record.operationContent!""}</td>
                <td style="width: 20em;">${record.comments!""}</td>
                <td>${record.operationType!""}</td>
            </tr>
        </#list>
    </table>
<#else >暂无记录
</#if>
</div>

<div id="record_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>添加用户备注</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal">
            <ul class="inline">
                <li>
                    <dt>用户ID</dt>
                    <dd>${teacherInfoMap.id!''}</dd>
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

<div id="info_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>信息</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal" style="text-align: center;">
            <ul class="inline">
                <li style="text-align: left;"></li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">确 定</button>
    </div>
</div>
<div id="fakeDes_dialog" class="modal hide fade">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>判假描述</h3>
    </div>
    <div class="modal-body">
        <dl class="dl-horizontal" style="text-align: center;">
            <ul class="inline">
                <li style="text-align: left;"><#if teacherInfoMap?? && teacherInfoMap.fakeDesc??>${teacherInfoMap.fakeDesc!''}</#if></li>
            </ul>
        </dl>
    </div>
    <div class="modal-footer">
        <button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">确 定</button>
    </div>
</div>
</body>

<script>
    $(function () {

        $("#showFakeDes").on("click", function () {
            $("#fakeDes_dialog").modal('show');
        });

        $("#dialog_edit_teacher_date").on("click", function () {
            var queryUrl = "../user/addcustomerrecord.vpage";
            $.ajax({
                type: "post",
                url: queryUrl,
                data: {
                    userId: ${teacherInfoMap.id!''},
                    questionDesc: $("#questionDesc").val(),
                    operation: $("#operation").val()
                },
                success: function (data) {
                    $("#record_success").val(data.success);
                    if (data.success) {
                        appendNewRecord(data);
                    } else {
                        alert("增加备注失败。");
                    }
                    $("#record_dialog").modal("hide");
                }
            });
        });

    });
    function hideCustomerServiceRecord() {
        $("#customer_service_record").toggle(function () {
            var $target = $("#hide_record_btn");
            switch ($target.html()) {
                case "隐藏备注":
                    $target.html("显示备注");
                    break;
                case "显示备注":
                    $target.html("隐藏备注");
                    break;
            }
        });
    }
    function addCustomerServiceRecord() {
        $("#questionDesc").val('');
        $("#operation").val('');
        $("#record_dialog").modal("show");
    }
    function appendNewRecord(data) {
        var record = "<tr>" +
                "<td>" + data.customerServiceRecord.userId + "</td>" +
                "<td>" + data.customerServiceRecord.operatorId + "</td>" +
                "<td>" + data.createTime + "</td> " +
                "<td>" + data.customerServiceRecord.operationContent + "</td> " +
                "<td>" + data.customerServiceRecord.comments + "</td> " +
                "<td>" + data.customerServiceRecord.operationType + "</td>  " +
                "</tr> ";
        $("#comment_title").after(record);
    }
</script>
</html>
