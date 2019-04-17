<#import "../schoolmaster.ftl" as com>
<@com.page menuIndex=12 >
    <@sugar.capsule js=["echarts"]/>
<ul class="breadcrumb_vox">
    <li><a href="/schoolmaster/report/knowledgeabilityanalysis.vpage">大数据报告</a> <span class="divider">/</span></li>
    <li>学校概况</li>
    <li>班级学情分布</li>
    <li class="active">知识能力分析</li>
</ul>

<div class="r-titleResearch-box">
    <p>
        <#if title?has_content>
             ${title}
        </#if>
    </p>
    <div style="text-align: center">
        <select id="select-year">
            <#list historyYears as yearUnit>
                <option value="${yearUnit}"
                        <#if year?has_content && year == yearUnit>selected="selected"</#if>>${yearUnit}</option>
            </#list>
        </select>
        学年
        <select id="select-term">
            <option value="1" <#if term?has_content && term == "1">selected="selected"</#if>>上学期</option>
            <option value="2" <#if term?has_content && term == "2">selected="selected"</#if>>下学期</option>
        </select>
        <a id="btn-query" href="javascript:void(0);" class="btn_vox btn_vox_small">
            查询
        </a>
    </div>
</div>
<div class="r-mapResearch-box">
    <div class="y-nav-tip">
        <ul id="tabUL">
            <#foreach grade in gradeData>
                <li <#if grade.isActive?has_content && grade.isActive>class="active"</#if>>
                    <a href="javascript:void(0);" data-tab-ref="${grade.value}">${grade.name}</a>
                </li>
            </#foreach>
        </ul>
    </div>
</div>
<div id="totalEmbedded" class="tabDiv r-table">
</div>
<script id="movieTemplate" type="text/html">
    <table class="table table-hover table-striped table-bordered">
        <tbody>
        <tr>
            <td>时间/能力</td>
            <td>听</td>
            <td>说</td>
            <td>读</td>
            <td>写</td>
        </tr>
        <%for(var i = 0; i < stResultDetailList.length; i++){%>
        <tr>
            <td><%=stResultDetailList[i].time%></td>
            <td><%=stResultDetailList[i].listenStr%></td>
            <td><%=stResultDetailList[i].speakStr%></td>
            <td><%=stResultDetailList[i].readStr%></td>
            <td><%=stResultDetailList[i].writeStr%></td>
        </tr>
        <%}%>
        </tbody>
    </table>
</script>

<script id="movieTemplate2" type="text/html">
    <table class="table table-hover table-striped table-bordered">
        <tbody>
        <tr>
            <td>时间/能力</td>
            <td>听力</td>
            <td>口语</td>
            <td>词汇</td>
            <td>阅读</td>
            <td>语法</td>
        </tr>
        <%for(var i = 0; i < stResultDetailList.length; i++){%>
        <tr>
            <td><%=stResultDetailList[i].time%></td>
            <td><%=stResultDetailList[i].listenStr%></td>
            <td><%=stResultDetailList[i].oralStr%></td>
            <td><%=stResultDetailList[i].wordStr%></td>
            <td><%=stResultDetailList[i].readStr%></td>
            <td><%=stResultDetailList[i].grammarStr%></td>
        </tr>
        <%}%>
        </tbody>
    </table>
</script>


<script type="text/javascript">
    $(function () {
        var stResultDetailList =${resultList};
        var firstGrade = ${firstGrade};
        var isJuniorSchool =  ${isJuniorSchool?c};
        function rendering(grade) {

            if (firstGrade == "0") {
                $("#totalEmbedded").html("暂无数据");
                return;
            }

            var data = stResultDetailList != undefined && stResultDetailList[grade] != undefined ? stResultDetailList[grade] : [];
            if ( !isJuniorSchool ) {
                for (var i = 0; i < data.length; i++) {
                    if (data[i]["listen"] == undefined || data[i]["listen"] == 0) {
                        data[i]["listenStr"] = "【暂无】";
                    } else {
                        data[i]["listenStr"] = Math.ceil(100 * data[i]["listen"]) + "%";
                    }
                    if (data[i]["speak"] == undefined || data[i]["speak"] == 0) {
                        data[i]["speakStr"] = "【暂无】";
                    } else {
                        data[i]["speakStr"] = Math.ceil(100 * data[i]["speak"]) + "%";
                    }
                    if (data[i]["read"] == undefined || data[i]["read"] == 0) {
                        data[i]["readStr"] = "【暂无】";
                    } else {
                        data[i]["readStr"] = Math.ceil(100 * data[i]["read"]) + "%";
                    }
                    if (data[i]["write"] == undefined || data[i]["write"] == 0) {
                        data[i]["writeStr"] = "【暂无】";
                    } else {
                        data[i]["writeStr"] = Math.ceil(100 * data[i]["write"]) + "%";
                    }
                }
                $("#totalEmbedded").html(template("movieTemplate", {
                    stResultDetailList: data
                }));
            } else {

                for (var i = 0; i < data.length; i++) {
                    if (data[i]["listen"] == undefined || data[i]["listen"] == 0) {
                        data[i]["listenStr"] = "【暂无】";
                    } else {
                        data[i]["listenStr"] = Math.ceil(100 * data[i]["listen"]) + "%";
                    }

                    if (data[i]["oral"] == undefined || data[i]["oral"] == 0) {
                        data[i]["oralStr"] = "【暂无】";
                    } else {
                        data[i]["oralStr"] = Math.ceil(100 * data[i]["oral"]) + "%";
                    }

                    if (data[i]["word"] == undefined || data[i]["word"] == 0) {
                        data[i]["wordStr"] = "【暂无】";
                    } else {
                        data[i]["wordStr"] = Math.ceil(100 * data[i]["word"]) + "%";
                    }

                    if (data[i]["read"] == undefined || data[i]["read"] == 0) {
                        data[i]["readStr"] = "【暂无】";
                    } else {
                        data[i]["readStr"] = Math.ceil(100 * data[i]["read"]) + "%";
                    }

                    if (data[i]["grammar"] == undefined || data[i]["grammar"] == 0) {
                        data[i]["grammarStr"] = "【暂无】";
                    } else {
                        data[i]["grammarStr"] = Math.ceil(100 * data[i]["grammar"]) + "%";
                    }
                }
                $("#totalEmbedded").html(template("movieTemplate2", {
                    stResultDetailList: data
                }));
            }
        }

        //点击的选择班级的
        $("#tabUL").on("click", "a", function () {
            var $this = $(this);
            var $li = $(this).closest("li");
            if (!$li.hasClass("active")) {
                $li.addClass("active").siblings("li").removeClass("active");
                var grade = $this.attr("data-tab-ref");
                rendering(grade);
            }
            return false;
        });
        //点击查询
        $("#btn-query").on("click", function () {
            var year = $("#select-year").val();
            var term = $("#select-term").val();
            //请求后端接口，返回数据，然后渲染版面
            window.location = '/schoolmaster/report/knowledgeabilityanalysis.vpage?year=' + year + '&term=' + term;
        });
        rendering(firstGrade);
    });
</script>
</@com.page>