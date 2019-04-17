<!doctype html>
<html>
<head>
<#include "../../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <style type="text/css">
        body { background-color: #eff2f6; }
        .testPaper-wrapper { margin: 0 auto; padding: 18px 14px 0 0; width: 986px; background: url(/public/skin/teacherv3/images/examview/testPaper-wrapper-bg.png) no-repeat 0 0; _background: none; _filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='../images/testPaper-wrapper-bg.png'); }
        .testPaper-wrapper .testPaper-box { padding: 60px 100px; border: 1px #d3d8df solid; border-radius: 7px; background: url(/public/skin/teacherv3/images/examview/testPaper-box-bg.png) no-repeat -1px 100% #fff; _background: none; _filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='../images/testPaper-box-bg.png'); _background-color: #fff; }
        /* head & title & top & item */
        .testPaper-wrapper .tw-head,.testPaper-wrapper .tw-title,.testPaper-wrapper .tw-top { text-align: center; }
        .testPaper-wrapper .tw-head { font-size: 20px; line-height: 48px; font-weight:bold; }
        .testPaper-wrapper .tw-title { padding: 13px 0 0; font-size: 18px; line-height: 26px; }
        .testPaper-wrapper .tw-top { margin: 38px 66px 0; border-top: 1px #4e5656 solid; height: 11px; }
        .testPaper-wrapper .tw-top div { position: relative; margin: -13px 0 0; font-size: 16px; height: 24px; }
        .testPaper-wrapper .tw-top span { display: inline-block; padding: 0 20px; background-color: #fff; }

        .testPaper-wrapper .tw-box .tw-item { border-top: 1px #dfdfdf solid; }
        .testPaper-wrapper .tw-box .first { border: 0 none; }
        .testPaper-wrapper .tw-box img { display: block; }
        .voxExam-pc-text-main{  border-bottom: 1px #dfdfdf solid; }  /*处理每道题直接添加分割线*/
    </style>

<@sugar.capsule js=["jquery", "ko", "core","examCore", "template","alert"] css=["plugin.alert", "new_teacher.widget"]/>
<@sugar.site_traffic_analyzer_begin />
</head>

<body>
<div class="testPaper-wrapper">
    <div class="testPaper-box">
    <#if examPaperInfo?has_content>
        <div class="tw-head">${examPaperInfo["examPaperName"]}</div>
        <div class="tw-title">出卷人：${examPaperInfo["providerName"]}</div>
        <div class="tw-top">
            <div>
                <span>
                    ${examPaperInfo["totalExamNum"]}道题  预计完成时间：${examPaperInfo["totalTime"]!} 分钟
                </span>
            </div>
        </div>
        <div class="tw-box">
            <div style="text-align: right;">
                <a href="javascript:void(0);" class="w-btn w-btn-mini feedback" style="width: 70px;">纠错</a>
            </div>
        </div>
        <div class="tw-box">
            <div class="tw-item first">
                <div id="examListBox">
                    <div style="height: 200px; background-color: white; width: 98%;"><img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="margin-top: 25px; margin-left: 40%;" /></div>
                </div>
            </div>
        </div>
    <#else>
        <div class="tw-head">暂无相关内容</div>
    </#if>
    </div>
</div>

<script type="text/javascript">
    $(function(){
    <#if examPaperInfo?has_content>
        var examIds = '${ids!0}';
        try{
            vox.exam.create(function(data){
                if(data.success){
                    var node = document.getElementById('examListBox');
                    vox.exam.render(node, 'teacher_preview', {
                        ids: examIds.split(','),
                        imgDomain : '${imgDomain!''}',
                        domain : '${requestContext.webAppBaseUrl}/',
                        env : <@ftlmacro.getCurrentProductDevelopment />
                    });
                }else{
                    $('#examListBox').html('<div style="padding: 100px 0; text-align: center; font-size: 16px;">数据没有加载出来!<a href="javascript:void (0);" onclick="window.location.reload();">点击重试</a>或者联系客服</div>');
                    $17.voxLog({
                        module: 'vox_exam_create',
                        op:'create_error'
                    });
                    $17.tongji('voxExamCreate','create_error',location.pathname);
                }
            });
        }catch(exception){
            $17.voxLog({
                module: 'vox_exam_create',
                op: 'examCoreJs_error',
                errMsg: exception.message
            });
            $17.tongji('voxExamCreate','examCoreJs_error',exception.message);
        }

        //统计
        $17.tongji('预览','${(examPaperInfo["subject"])!}');
    </#if>


        $(".feedback").on("click", function(){
            $.prompt(template("t:错题反馈",{examIds : examIds.split(',')}),{
                title: "错题反馈",
                focus: 1,
                buttons: { "取消": false, "提交": true },
                submit: function(e, v){
                    if(v){
                        var feedbackContent = $("#feedbackContent");
                        if($17.isBlank(feedbackContent.val())){
                            feedbackContent.siblings(".init").html("错题反馈不能为空。");
                            feedbackContent.focus();
                            return false;
                        }

                        var examId = $("#examIdSelect").val();
                        if($17.isBlank(examId) || examId == "0"){
                            feedbackContent.siblings(".init").html("请选择题目");
                            return false;
                        }

                        $.post("/project/examfeedback.vpage", {
                            feedbackType    : 5,
                            examId          : examId,
                            content         : feedbackContent.val()
                        }, function(data){
                            if(data.success){
                                $17.alert("提交成功，感谢您的支持！");
                            }
                        });
                    }
                }
            });
        });


    });
</script>
<script id="t:错题反馈" type="text/html">
    <div>
        <span class='text_blue'>如果您发现题目出错了，请及时反馈给我们，感谢您的支持！</span>
        <p style="padding: 5px 0;">
            请选择题目序号：
            <select id="examIdSelect" name="examIdSelect">
                <option value="0">请选择</option>
                <%for(var i = 0,iLen = examIds.length; i < iLen; i++){%>
                <option value="<%=examIds[i]%>"><%=(i+1)%></option>
                <%}%>
            </select>
        </p>

        <textarea id='feedbackContent' cols='91' rows='8' style='width: 94%' class='int_vox'></textarea>
        <p class='init text_red'></p>
    </div>
</script>
    <@sugar.site_traffic_analyzer_end />
</body>
</html>


