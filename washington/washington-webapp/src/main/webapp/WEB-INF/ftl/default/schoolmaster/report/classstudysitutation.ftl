<#import "../schoolmaster.ftl" as com>
<@com.page menuIndex=11 >
    <@sugar.capsule js=["echarts"]/>
<ul class="breadcrumb_vox">
    <li><a href="/schoolmaster/report/classstudysitutation.vpage">大数据报告</a> <span class="divider">/</span></li>
    <li>学校概况</li>
    <li class="active">班级学情分析</li>
    <li>知识能力分析</li>
</ul>


<div class="r-titleResearch-box">
    <p>
        <#if title?has_content>
             ${title}
        </#if>
    </p>
    <div style="text-align: center">
        时间:
        <select id="select-time">
            <option value="01" <#if month==1 >selected="selected" </#if>>1月</option>
            <option value="02" <#if month==2 >selected="selected" </#if>>2月</option>
            <option value="03" <#if month==3 >selected="selected" </#if>>3月</option>
            <option value="04" <#if month==4 >selected="selected" </#if>>4月</option>
            <option value="05" <#if month==5 >selected="selected" </#if>>5月</option>
            <option value="06" <#if month==6 >selected="selected" </#if>>6月</option>
            <option value="07" <#if month==7 >selected="selected" </#if>>7月</option>
            <option value="08" <#if month==8 >selected="selected" </#if>>8月</option>
            <option value="09" <#if month==9 >selected="selected" </#if>>9月</option>
            <option value="10" <#if month==10 >selected="selected" </#if>>10月</option>
            <option value="11" <#if month==11 >selected="selected" </#if>>11月</option>
            <option value="12" <#if month==12 >selected="selected" </#if>>12月</option>
        </select>
        学科
        <select id="select-subject">
            <#foreach subject in subjectData>
                <option value="${subject.value}"
                        <#if subject.isSelected >selected="selected" </#if>>${subject.name}</option>
            </#foreach>
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
            <td>班级</td>
            <td>老师</td>
            <td>当月布置作业</td>
            <td>按时完成学生</td>
            <td>平均完成率</td>
            <td>平均分</td>
            <td>85-100</td>
            <td>60-84</td>
            <td>&lt60</td>
        </tr>
        <%for(var i = 0; i < stResultDetailList.length; i++){%>
        <tr>
            <td><%=stResultDetailList[i].className%></td>
            <td><%=stResultDetailList[i].teacherName%></td>
            <td><%=stResultDetailList[i].teacher_assgin_numStr%></td>
            <td><%=stResultDetailList[i].accomplish_stu_numStr%></td>
            <td><%=stResultDetailList[i].accomplish_hw_rateStr%></td>
            <td><%=stResultDetailList[i].group_accomplish_hw_avgscoreInt%></td>
            <td><%=stResultDetailList[i].stu_accomplish_hw_avgscore_85_up_stu_numStr%></td>
            <td><%=stResultDetailList[i].stu_accomplish_hw_avgscore_60_up_stu_numStr%></td>
            <td><%=stResultDetailList[i].stu_accomplish_hw_avgscore_60_down_stu_numStr%></td>
        </tr>
        <%}%>
        </tbody>
    </table>
</script>
<script type="text/javascript">
    $(function () {
        var stResultDetailList =${resultList};
        var firstGrade = ${firstGrade};

        function rendering(grade) {
            if (firstGrade == "0") {
                $("#totalEmbedded").html("暂无数据");
                return;
            }
            var data = stResultDetailList != undefined && stResultDetailList[grade] != undefined ? stResultDetailList[grade] : [];
            for (var i = 0; i < data.length; i++) {
                if (data[i]["accomplish_hw_rate"] == undefined || data[i]["accomplish_hw_rate"] == "") {
                    data[i]["accomplish_hw_rateStr"] = "【暂无】";
                } else {
                    data[i]["accomplish_hw_rateStr"] = Math.ceil(data[i]["accomplish_hw_rate"] * 100) + "%";
                }
                if (data[i]["group_accomplish_hw_avgscore"] == undefined || data[i]["group_accomplish_hw_avgscore"] == "") {
                    data[i]["group_accomplish_hw_avgscoreInt"] = "【暂无】";
                } else {
                    data[i]["group_accomplish_hw_avgscoreInt"] = Math.ceil(data[i]["group_accomplish_hw_avgscore"]) + "分";
                }
                if (data[i]["accomplish_stu_num"] == undefined || data[i]["accomplish_stu_num"] == "") {
                    data[i]["accomplish_stu_numStr"] = "【暂无】";
                } else {
                    data[i]["accomplish_stu_numStr"] = data[i]["accomplish_stu_num"] + "人";
                }
                if (data[i]["teacher_assgin_num"] == undefined || data[i]["teacher_assgin_num"] == "") {
                    data[i]["teacher_assgin_numStr"] = "【暂无】";
                } else {
                    data[i]["teacher_assgin_numStr"] = data[i]["teacher_assgin_num"] + "次";
                }
                if (data[i]["stu_accomplish_hw_avgscore_85_up_stu_num"] == undefined || data[i]["stu_accomplish_hw_avgscore_85_up_stu_num"] == "") {
                    data[i]["stu_accomplish_hw_avgscore_85_up_stu_numStr"] = "【暂无】";
                } else {
                    data[i]["stu_accomplish_hw_avgscore_85_up_stu_numStr"] = data[i]["stu_accomplish_hw_avgscore_85_up_stu_num"] + "人";
                }
                if (data[i]["stu_accomplish_hw_avgscore_60_up_stu_num"] == undefined || data[i]["stu_accomplish_hw_avgscore_60_up_stu_num"] == "") {
                    data[i]["stu_accomplish_hw_avgscore_60_up_stu_numStr"] = "【暂无】";
                } else {
                    data[i]["stu_accomplish_hw_avgscore_60_up_stu_numStr"] = data[i]["stu_accomplish_hw_avgscore_60_up_stu_num"] + "人";
                }
                if (data[i]["stu_accomplish_hw_avgscore_60_down_stu_num"] == undefined || data[i]["stu_accomplish_hw_avgscore_60_down_stu_num"] == "") {
                    data[i]["stu_accomplish_hw_avgscore_60_down_stu_numStr"] = "【暂无】";
                } else {
                    data[i]["stu_accomplish_hw_avgscore_60_down_stu_numStr"] = data[i]["stu_accomplish_hw_avgscore_60_down_stu_num"] + "人";
                }
            }
            $("#totalEmbedded").html(template("movieTemplate", {
                stResultDetailList: data
            }));
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
            var time = $("#select-time").val();
            var subject = $("#select-subject").val();
            //请求后端接口，返回数据，然后渲染版面
            window.location = '/schoolmaster/report/classstudysitutation.vpage?month=' + time + '&subject=' + subject;
        });
        rendering(firstGrade);
    });
</script>
</@com.page>