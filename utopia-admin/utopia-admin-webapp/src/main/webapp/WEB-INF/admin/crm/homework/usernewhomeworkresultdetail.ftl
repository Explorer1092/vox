<#-- @ftlvariable name="stResultDetailList" type="java.util.Map<String, Object>" -->
<#-- @ftlvariable name="homeworkId" type="java.lang.Long" -->
<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title="CRM" page_num=3>
<style>
    blockquote {
        margin: 0;
    }

    detail {

    }

</style>
<div id="main_container" class="span9">
    <fieldset>
        <legend>
            <a href="${requestContext.webAppContextPath}/crm/student/studenthomepage.vpage?studentId=${studentId!}">${realName}</a>的作业
            <a href="${requestContext.webAppContextPath}/crm/homework/newhomeworkhomepage.vpage?homeworkId=${homeworkId!}">${homeworkId!}</a>答题详情
            <select id="select">
                <#list selectItemKey as detail>
                    <option value=${detail}>${selectItemValue[detail_index]}</option>
                </#list>
            </select>
            <button id="button">字段解析</button>
        </legend>
    </fieldset>

    <div id="content">

    </div>
    <div id="alertDiv" title="答题详情">

    </div>
    <div id="alertDiv2" title="字段解析">

    </div>
    <div id="alertDiv3" title="音频解析">

    </div>

</div>

<script id="alertTemple" type="text/html">
        <%for(var i = 0; i < alertContent.length; i++){%>
        <p><%=alertContent[i]%></p>
        <%}%>
</script>


<script id="voiceTemple" type="text/html">
    <ul>
        <li>题目朗读内容:<%=summary.content%></li>
        <li><%=summary.quota%></li>
        <li>详细分数（10分满分）<%=summary.detailed%></li>
        <li>其中打分低于5分的的单词或音标<%=summary.week%></li>
    </ul>
</script>


<script id="movieTemplate" type="text/html">

    <!--单独处理纸质口算题型-->
        <#if isContainOralMentalPractise?? >
        <table class="table table-hover table-striped table-bordered">
            <#if stResultDetailList?? >
                <tr>
                    <th>作业类型</th>
                    <th>学生上传的违规图片</th>
                    <th>中间结果表保存图片</th>
                    <th>创建时间</th>
                    <th>明细操作</th>
                    <th>替换违规图片</th>
                </tr>
                <#list stResultDetailList as stu>
                    <tr>
                        <#if stu.objectiveConfigType == 'OCR_MENTAL_ARITHMETIC' || stu.objectiveConfigType == 'OCR_DICTATION'>
                            <td>${stu.itemName}</td>
                            <td>
                                <#if stu.originImageUrl != "">
                                    <image src=${stu.originImageUrl} style="width:90px;height:90px;"></image>
                                </#if>
                            </td>
                            <td>
                                <image src=${stu.imageUrl} style="width:90px;height:90px;"></image>
                            </td>
                            <td>${stu.createTime}</td>
                            <td><a class="pDetail" data-pId=${stu.pId}>查看全部</a></td>
                            <td style="text-align: center;">
                                    <input type="hidden" value=${stu.objectiveConfigType} />
                                    <input type="hidden" value=${stu.pId} />
                                    <input type="button" class="btn btn-primary replaceImage" value="替换违规图片">
                                    <div id="confirDia" title="确认替换图片">
                                    </div>
                            </td>

                        </#if>
                    </tr>
                </#list>
            </#if>
        </table>
        </#if>
    <!--含有 qid的作业类型-->
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th>作业类型</th>
            <th>题ID</th>
            <th>单元名称</th>
            <th>课程名称</th>
            <th>标准分</th>
            <th>实际得分</th>
            <th>完成时长</th>
            <th>创建时间</th>
            <th>是否掌握</th>
            <th>标准答案</th>
            <th>用户答案</th>
            <th>区域掌握</th>
            <th>主观作业</th>
            <th>跟读作业</th>
            <th width="100">语音分析</th>
            <th>明细操作</th>
            <th>客户端名称</th>
            <th>客户端类型</th>
            <th>应用跟读口语分数等级</th>
            <th>订正信息</th>
        </tr>
        <%for(var i = 0; i < stResultDetailList.length; i++){%>
        <tr>
            <%if(stResultDetailList[i].objectiveConfigType != 'OCR_MENTAL_ARITHMETIC' && stResultDetailList[i].objectiveConfigType != 'OCR_DICTATION'){%>
            <td><%=stResultDetailList[i].itemName%></td>
            <td><%=stResultDetailList[i].qId%></td>
            <td><%=stResultDetailList[i].unitName%></td>
            <td><%=stResultDetailList[i].lessonName%></td>
            <td><%=stResultDetailList[i].standardScore%></td>
            <td><%=stResultDetailList[i].score%></td>
            <td><%=stResultDetailList[i].duration%></td>
            <td><%=stResultDetailList[i].createTime%></td>
            <td><%=stResultDetailList[i].grasp%></td>
            <td><%=stResultDetailList[i].sourceAnswer%></td>
            <td><%=stResultDetailList[i].userAnswers%></td>
            <td><%=stResultDetailList[i].subGrasp%></td>
            <td>
                <% if(stResultDetailList[i].fileNames){%>
                    <%for(var j = 0; j < stResultDetailList[i].fileNames.length; j++){%>
                    <a target="_blank" href="<%=stResultDetailList[i].relativeUrls[j]%>"><%=stResultDetailList[i].fileNames[j]%></a>
                    <br>
                    <%}%>
                <%}%>
            </td>
            <td>
                <% if(stResultDetailList[i].audioUrls){%>
                    <%for(var j = 0; j < stResultDetailList[i].audioUrls.length; j++){%>
                    <a target="_blank" href="<%=stResultDetailList[i].audioUrls[j]%>"><%=stResultDetailList[i].audioInfo[j]%></a>
                    <br>
                    <%}%>
                <%}%>
            </td>
            <td>
                <% if(stResultDetailList[i].audioUrls){%>
                    <%for(var j = 0; j < stResultDetailList[i].audioUrls.length; j++){%>
                    <a class="voiceAnalysis" data-voice="<%=stResultDetailList[i].audioUrls[j]%>">语音分析</a>
                    <br>
                    <%}%>
                <%}%>
            </td>
            <td><a class="detail" data-qId="<%=stResultDetailList[i].qId%>">查看全部</a></td>
            <td><%=stResultDetailList[i].clientName%></td>
            <td><%=stResultDetailList[i].clientType%></td>
            <td><%=stResultDetailList[i].appOralScoreLevel%></td>
            <td><%if(stResultDetailList[i].isHaveCorrectNewHomeworkProcessInfo){%>
                <a class="correctNewHomeworkProcessDetail"
                   data-correctNewHomeworkProcessInfo="<%=stResultDetailList[i].correctNewHomeworkProcessInfo%>">查看订正信息</a>
                <%}%>
            </td>
            <%}%>
        </tr>
        <%}%>
    </table>
</script>

<script>
    $(function () {

        var stResultDetailList = ${resultList};
        var homeworkId = "${homeworkId}";
        var studentId = ${studentId};
        debugger
        $("#content").html(template("movieTemplate", {
            stResultDetailList: stResultDetailList
        }));
        $('#select').change(function () {
            var selectValue = $(this).children('option:selected').val();
            var filterValue = [];
            for (var i = 0; i < stResultDetailList.length; i++) {
                if (stResultDetailList[i].selectItemKey == selectValue || selectValue == "0.0") {
                    filterValue.push(stResultDetailList[i]);
                }
            }
            $("#content").html(template("movieTemplate", {
                stResultDetailList: filterValue
            }));
            $("a[class='detail'").click(detailClick)

        });

        function correctNewHomeworkProcessDetailClick() {
            var correctNewHomeworkProcessInfo = $(this).attr("data-correctNewHomeworkProcessInfo");
            if (correctNewHomeworkProcessInfo == "") {
                correctNewHomeworkProcessInfo = "该题没有订正答题信息";
            }
            $("#alertDiv").html(template("alertTemple", {
                alertContent: [correctNewHomeworkProcessInfo]
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

        }

        function detailClick() {
            var qId = $(this).attr("data-qId");
            var target;
            for (var i = 0; i < stResultDetailList.length; i++) {
                if (stResultDetailList[i].qId == qId) {
                    target = stResultDetailList[i];
                    break;
                }
            }
            $("#alertDiv").html(template("alertTemple", {
                alertContent: [target.content]
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

        }

        function detailPidClick() {
            var pId = $(this).attr("data-pId");
            var target;
            for (var i = 0; i < stResultDetailList.length; i++) {
                if (stResultDetailList[i].pId == pId) {
                    target = stResultDetailList[i];
                    break;
                }
            }
            $("#alertDiv").html(template("alertTemple", {
                alertContent: [target.content]
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

        }

        $("#button").click(function () {
            $("#alertDiv2").html(template("alertTemple", {
                alertContent: ["标准分：这道题目的满分", "实际得分：学生答案的得分", "是否掌握：这道题目学生回答是否全对", "区域掌握：当用户答案为多项时，表示每项是否正确", "跟读作业：引擎分/流利程度/完整度/发音准确度"]
            }));
            $("#alertDiv2").dialog({
                resizable: false,
                height: "auto",
                width: 400,
                modal: true,
                buttons: {
                    "关闭": function () {
                        $(this).dialog("close");
                    }
                }
            });
        })
        $("a[class='correctNewHomeworkProcessDetail'").click(correctNewHomeworkProcessDetailClick)
        $("a[class='detail'").click(detailClick);
        $("a[class='pDetail'").click(detailPidClick);

        $(".voiceAnalysis").click(function () {
            var voiceUrl = $(this).attr("data-voice");
            $.ajax({
                url: "/crm/homework/voiceanalysis.vpage",
                type: "GET",
                data: {voiceUrl: voiceUrl},
                dataType: "json",
                success: function (data) {
                    if (data && data.success && data.summary) {
                        $("#alertDiv3").html(template("voiceTemple", {
                            summary: data.summary
                        }));
                        $("#alertDiv3").dialog({
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

                    }
                }

            });
        });

        /**
         * 替换纸质口算/纸质听写学生上传图片
         */
        $(".replaceImage").click(function(){
            var processId=$(this).prev().val();
            var objType=$(this).prev().prev().val();
            $("#confirDia").html(template("alertTemple", {
                alertContent: ["确认是否替换纸质口算图片？"]
            }));
            $("#confirDia").dialog({
                resizable: false,
                height: "auto",
                width: 600,
                modal: true,
                buttons: {
                    "取消": function () {
                        $(this).dialog("close");
                    },
                    "确定":function(){
                        var ocrMentalUrl="/crm/homework/replaceimage.vpage";
                        var ocrDictationUrl="/crm/homework/replaceOcrDictationImage.vpage";
                        var url = "";
                        if (objType == "OCR_MENTAL_ARITHMETIC") {
                            url = ocrMentalUrl;
                        }
                        if (objType == "OCR_DICTATION") {
                            url = ocrDictationUrl;
                        }
                        if (url == "") {
                            alert("无法对此题型进行图片替换!");
                            window.location.reload();
                        }
                        $.ajax({
                            url: url,
                            type: "GET",
                            data: {processId: processId, homeworkId: homeworkId, userId: studentId},
                            dataType: "json",
                            success: function (data) {
                                if (data && data.success) {
                                    alert("替换成功");
                                } else {
                                    alert("替换失败!");
                                }
                                window.location.reload();
                            },
                            error: function (e) {
                                console.log(e);
                                window.location.reload();
                            }
                        });
                        $(this).dialog("close");
                    }
                }
            });

        });

    });
</script>


</@layout_default.page>