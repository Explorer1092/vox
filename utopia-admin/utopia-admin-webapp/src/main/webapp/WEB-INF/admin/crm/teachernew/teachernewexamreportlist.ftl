<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <link  href="${requestContext.webAppContextPath}/public/css/bootstrap.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/admin.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
    <script src="${requestContext.webAppContextPath}/public/js/jquery-1.9.1.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/template.js"></script>
    <style>
        .table_soll{ overflow-y:hidden; overflow-x: auto;}
        .table_soll table td,.table_soll table th{white-space: nowrap;}
        .basic_info {margin-left: 2em;}
        .txt{margin-left: .5em;font-weight:800}
        .button_label{with:7em;height: 3em;margin-top: 1em}
        .info_td{width: 10em;}
        .info_td_txt{width: 13em;font-weight:600}
        .dropDownBox_tip{ position:absolute; z-index: 9; }
        .dropDownBox_tip span.arrow{ position: absolute; top:-9px; _top:-8px; left:20px; font: 18px/100% Arial, Helvetica, sans-serif; color: #ffe296;}
        .dropDownBox_tip span.arrow span.inArrow{ color: #feef94; position:absolute; left:0; top:1px;}
        .dropDownBox_tip span.arrowLeft{ left:-9px; top:15px;}
        .dropDownBox_tip span.arrowLeft span.inArrow{ left:1px; top:0px;}
        .dropDownBox_tip span.arrowRight{ left: auto; right:-9px; top:15px;}
        .dropDownBox_tip span.arrowRight span.inArrow{left:-1px; top:0px;}
        .dropDownBox_tip span.arrowBot{ top:auto; bottom:-9px; _bottom:-11px; left:20px;}
        .dropDownBox_tip span.arrowBot span.inArrow{ left:0; top:-1px}
        .dropDownBox_tip .tip_content{ border:1px solid #ffe296; background-color:#feef94; overflow:hidden; width:160px; color:#d77a00; padding:15px; border-radius: 3px;}
        .dropDownBox_tip h4.h-title{ font-size: 16px; padding: 5px 0 15px 0;}
        .dropDownBox_tip span.close{ cursor: pointer; position: absolute; right: 10px;top: 10px; color: #cca313; font-size: 22px;}
        .Calculation-detailClazz-box ul{ overflow: hidden; *zoom: 1;  padding: 15px 0; margin: 0!important;}
        .Calculation-detailClazz-box ul li{ float: left; font: bold 14px/1.125 "微软雅黑", "Microsoft YaHei", Arial; color: #666; width: 24%; text-align: center;}
        .Calculation-detailClazz-box ul li h5{ font-size: 14px; padding: 0 0 12px;}
        .Calculation-detailClazz-box ul li p{ font-size: 18px; font-weight: normal; padding: 0;}
        .Calculation-foot{ font-size: 12px; padding: 20px 0;}
        .Calculation-foot p{ padding: 5px 0;}
        .dropDownBox_tip .swap-box{ text-align: center; padding-top: 20px;}
        .dropDownBox_tip .swap-box span{ width: 110px; height: 110px; display: inline-block; background-color: #fff;}
        .w-blue{ color: #189cfb;}
        .w-red{ color: #e00;}
        .w-orange{ color: #fa7252;}
    </style>
    <style>
        detail {

        }
    </style>
</head>
<body style="background: none;">
<div style="margin-left: 2em" >
    <div style="margin-top: 2em">
<#if (newExamReportForClazz["success"])>
<ul class="inline">
    <li><span style="font-weight:600">${newExamReportForClazz["examId"]!''}模考历史</span></li>
    <li><span
            style="font-weight:600">参加学生${newExamReportForClazz["joinExamNum"]!''}/${newExamReportForClazz["totalStudentNum"]!''}</span>
    </li>
    <li><span
            style="font-weight:600">完成学生${newExamReportForClazz["finishedExamNum"]!''}/${newExamReportForClazz["totalStudentNum"]!''}</span>
    </li>
    <li><span
            style="font-weight:600">试卷ID${newExamReportForClazz["paperId"]!''}  总题数目${newExamReportForClazz["totalNum"]!''}</span>
    </li>
    <br>
    <br>
    <br>
    口语题修改:千万注意，不传学生ID的时候是全部学生处理，支持多个学生处理，逗号隔开
    <form>
        题ID：
        <input type="text" id="oralQuestionId">
        <br>
        学生的账号([31414,314141]):
        <input type="text" id="oralStudentIds">
        <br>
        试卷DocID:
        <input type="text" id="oralPaperId">
        <br>
        <button type="button" id = "oralBtn">改变模考答题记录</button>
    </form>

    <br>
    <br>
    <br>

    客观题修改
    <form>
        题ID：
        <input type="text" id="questionDocId">
        <br>
        修改后答案([[1]]):
        <input type="text" id="answer">
        <br>
        现在题答案([[2]]):
        <input type="text" id="errorAnswer">
        <br>
        学生的账号([31414]):
        <input type="text" id="studentIds">
        <br>
        试卷ID:
        <input type="text" id="paperId">
        <br>
        是否全部学生(true/false)
        <input type="text" id="allUsers">
        <br>

        <button type="button" id = "myBtn">改变模考答题记录</button>
    </form>

    <form>
        学生ID：
        <input type="text" id="studentId">
        <button type="button" id = "newExamBtn">补考或重考(只支持单个操作)</button>
    </form>
    <form>
        学生ID：
        <input type="text" id="submitExamStudentId">
        <button type="button" id = "submitExamBtn">交卷功能(只支持单个操作)</button>
    </form>


</ul>
<table class="table table-hover table-striped table-bordered">
    <tr id="title">
        <th> 学生模考记录ID</th>
        <th> 试卷ID</th>
        <th> 学生ID</th>
        <th> 学生名字</th>
        <th> 开始时间</th>
        <th> 完成时间</th>
        <th> 交卷时间</th>
        <th> 实际分数</th>
        <th> 批改分数</th>
        <th> 批改时间</th>
        <th> 完成时长(单位:秒)</th>
        <th> 客户端类型</th>
        <th> 客户端名称</th>
        <th> 明细</th>
        <th> 操作</th>
    </tr>
    <#if newExamReportForClazz["crmStudentNewExamReports"]?has_content>
        <#list newExamReportForClazz["crmStudentNewExamReports"] as crmStudentNewExamReport>
            <tr>
                <td>${crmStudentNewExamReport["newExamResultId"]!''}</td>
                <td>${crmStudentNewExamReport["paperId"]!''}</td>
                <td>${crmStudentNewExamReport["userId"]!''}</td>
                <td>${crmStudentNewExamReport["userName"]!''}</td>
                <td>${crmStudentNewExamReport["createAt"]!''}</td>
                <td>${crmStudentNewExamReport["finishAt"]!''}</td>
                <td>${crmStudentNewExamReport["submitAt"]!''}</td>
                <td>${crmStudentNewExamReport["score"]!''}</td>
                <td>${crmStudentNewExamReport["correctScore"]!''}</td>
                <td>${crmStudentNewExamReport["correctAt"]!''}</td>
                <td>${crmStudentNewExamReport["durationSeconds"]!''}</td>
                <td>${crmStudentNewExamReport["clientType"]!''}</td>
                <td>${crmStudentNewExamReport["clientName"]!''}</td>
                <td><a class="detail" data-qId=${crmStudentNewExamReport["detail"]!''}>查看全部</a></td>
                <td><a target="_blank"
                       href="/crm/teachernew/teachernewexamreportforstudent.vpage?newExamResultId=${crmStudentNewExamReport['newExamResultId']}">查看详情
                </a></td>
            </tr>
        </#list>
    <div id="alertDiv" title="答题详情">
    <#else >
        <td colspan="9">暂无历史信息</td>
    </#if>
</table>
<#else>
<li><span style="font-weight:600">${newExamReportForClazz["description"]!''}</span></li>
</#if>
</div>
</div>
</body>
<script id="alertTemple" type="text/html">
    <p><%=alertContent%></p>
</script>
<script>
    $(function () {
        var newExamId = "${newExamReportForClazz["examId"]!''}";
        $("#myBtn").click(function(){
            var questionDocId = $("#questionDocId").val();
            var answer = $("#answer").val();
            var paperId = $("#paperId").val();
            var errorAnswer = $("#errorAnswer").val();
            var studentIds = $("#studentIds").val();
            var allUsers = $("#allUsers").val();
//            if(questionDocId == ""||answer==""||errorAnswer==""||studentIds==""){
//                alert("参数错误");
//            }else{
                var data = {
                    allUser:allUsers == "true",
                    newExamId:newExamId,
                    paperId:paperId,
                    questionDocId:questionDocId,
                    errorAnswerStr:errorAnswer,
                    studentIdsStr:studentIds,
                    answerStr:answer
                };

                var param = JSON.stringify(data);
            $.ajax({ url: "/crm/teachernew/newresetscore.vpage",data:{"param":param}, success: function(result){
                var str = result.success ? "操作成功":"操作失败";
                alert(str);
            }});




//            }




        });

        $("#oralBtn").click(function(){
            var questionId = $("#oralQuestionId").val();
            var paperDocId = $("#oralPaperId").val();
            var studentIds = $("#oralStudentIds").val();
            var data = {
                newExamId:newExamId,
                paperDocId:paperDocId,
                questionId:questionId,
                studentIds:studentIds
            };

            $.ajax({ url: "/crm/teachernew/jobCorrect.vpage",data:data, success: function(result){
                var str = result.success ? "操作成功":"操作失败";
                alert(str);
            }});
        });
        $("#submitExamBtn").click(function(){
            var studentId = $("#submitExamStudentId").val();
            $.ajax({ url: "/crm/teachernew/submitexam.vpage",
                data:{ "studentId":studentId,
                    "newExamId":newExamId},
                success: function(result){
                    var str = result.success ? "操作成功":"操作失败";
                    alert(str);
                }});
        });

  $("#newExamBtn").click(function(){
            var studentId = $("#studentId").val();
            $.ajax({ url: "/crm/teachernew/newstudentexam.vpage",
            data:{ "studentId":studentId,
                   "newExamId":newExamId},
            success: function(result){
                var str = result.success ? "操作成功":"操作失败";
                alert(str);
            }});
            });


        function detailClick() {
            var alertContent = $(this).attr("data-qId");
            $("#alertDiv").html(template("alertTemple", {
                alertContent: alertContent
            }));
            $("#alertDiv").dialog({
                resizable: false,
                height: "auto",
                width: 800,
                modal: true,
                buttons: {
                    "关闭": function () {
                        $(this).dialog("close");
                    }
                }
            });
        };
        $("a[class='detail'").click(detailClick);
    });
</script>
</html>