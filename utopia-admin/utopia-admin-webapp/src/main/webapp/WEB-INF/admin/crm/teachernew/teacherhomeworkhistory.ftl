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
</head>
<body style="background: none;">
<div style="margin-left: 2em" >
    <div style="margin-top: 2em">
        <table >
            <tr>
                <td class="info_td">使用总次数：</td>
                <td class="info_td_txt"><#if teacherSummary??&&teacherSummary.totalHomeworkCount?has_content>${teacherSummary.totalHomeworkCount}次</#if></td>
                <td class="info_td">最近7天使用次数：</td>
                <td class="info_td_txt"><#if teacherSummary??&&teacherSummary.day7HomeworkCount?has_content>${teacherSummary.day7HomeworkCount}次</#if></td>
                <td class="info_td">最近30天使用次数：</td>
                <td class="info_td_txt"><#if teacherSummary??&&teacherSummary.day30HomeworkCount?has_content>${teacherSummary.day30HomeworkCount}次</#if></td>
            </tr>
            <tr>
                <td class="info_td">首次使用时间：</td>
                <td class="info_td_txt"><#if teacherSummary??&&teacherSummary.firstAssignHomeworkTime?has_content>${teacherSummary.firstAssignHomeworkTime?number_to_datetime}</#if></td>
                <td class="info_td">最近一次使用时间：</td>
                <td class="info_td_txt"><#if teacherSummary??&&teacherSummary.latestAssignHomeworkTime?has_content>${teacherSummary.latestAssignHomeworkTime?number_to_datetime}</#if></td>
                <td class="info_td">距今日未使用天数：</td>
                <td class="info_td_txt"><#if unuseToToday?has_content>${unuseToToday}天</#if></td>
            </tr>
        </table>
    </div>
    <div style="margin-top: 2em">
        <ul class="inline">
            <li><span style="font-weight:600">布置作业历史</span></li>
            <#if teacherSummary??>
            <li><form method="post" action="teacherhomeworkhistory.vpage?homeworkDay=30&teacherId=${teacherSummary.teacherId!''}">
                <button  type="submit" <#if homeworkDay == 30 ||homeworkDay == 0>style="background-color: #C5C5C5"</#if>>查看30天内记录</button>
            </form></li>
            <li><form method="post" action="teacherhomeworkhistory.vpage?homeworkDay=90&teacherId=${teacherSummary.teacherId!''}">
                <button  type="submit" <#if homeworkDay == 90>style="background-color: #C5C5C5"</#if>>查看3个月内记录</button>
            </form></li>
            </#if>
        </ul>
        <table  class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 布置时间</th>
                <th> 检查时间</th>
                <th> 作业ID</th>
                <th> 班级名称</th>
                <th> 学生总人数</th>
                <th> 参与人数</th>
                <th> 完成人数</th>
                <th> IP数量</th>
                <th> 布置老师</th>
                <th> 操作</th>
                <th> 来源</th>
                <th> 作业类型</th>
            </tr>
            <#if homeworkHistoryList?? && homeworkHistoryList?has_content>
                <#list homeworkHistoryList as homeworkHistory>
                    <tr <#if (homeworkHistory["disabled"])>class="warning"</#if>>
                        <td>${homeworkHistory["arrangeTime"]!''}</td>
                        <td>${homeworkHistory["checkTime"]!''}</td>
                        <td><a target="_blank"
                               href="../homework/homeworkhomepage.vpage?homeworkId=${homeworkHistory["homeworkId"]!''}&homeworkSubject=${homeworkHistory["homeworkSubject"]!''}">${homeworkHistory["homeworkId"]!''}</a>
                        </td>
                        <td><a target="_blank"
                               href="../clazz/groupinfo.vpage?teacherId=${teacherSummary.teacherId!}&clazzId=${homeworkHistory["clazzId"]!''}">${homeworkHistory["clazzName"]!''}</a>
                        </td>
                        <td>${homeworkHistory["studentCount"]!''}</td>
                        <td>${homeworkHistory["joinCount"]!''}</td>
                        <td>${homeworkHistory["completeCount"]!''}</td>
                        <td>${homeworkHistory["ipcount"]!''}</td>
                        <td><#if (homeworkHistory["homeworkTeacherId"])??>
                            <a target="_blank"
                               href="../teachernew/teacherdetail.vpage?teacherId=${homeworkHistory['homeworkTeacherId']}">
                            ${homeworkHistory["homeworkTeacherName"]!''}(${homeworkHistory["homeworkTeacherId"]!''})
                            </a>
                        </#if></td>
                        <td>
                            <#if (homeworkHistory["disabled"])>
                                <label style="color: red;">该作业已删除</label>
                            <#else>
                                <a name="homework_delete" role="button"
                                   data-content-id="${homeworkHistory["homeworkId"]!}" class="btn">删除</a>
                                <a name="homework_add_Integral" role="button"
                                   data-content-id="${homeworkHistory["homeworkId"]!}" class="btn">补发园丁豆</a>
                                <a name="integral_detail" role="button"
                                   data-content-id="${homeworkHistory["homeworkId"]!}" data-type="homework" class="btn">园丁豆详情</a>
                            </#if>
                        </td>
                        <td>${homeworkHistory["source"]!''}</td>
                        <td><#if homeworkHistory["isTermEnd"]?? && homeworkHistory["isTermEnd"]>期末复习<#else>普通作业</#if></td>
                    </tr>
                </#list>
            <#else ><td colspan="9">暂无历史信息</td>
            </#if>
        </table>
    </div>

    <div style="margin-top: 2em">
        <ul class="inline">
            <#if teacherSummary??>
            <li><span style="font-weight:600">布置测验历史</span></li>
            <li><form method="post" action="teacherhomeworkhistory.vpage?quizDay=30&teacherId=${teacherSummary.teacherId!''}">
                <button  type="submit"<#if quizDay == 30 ||quizDay == 0>style="background-color: #C5C5C5"</#if>>查看30天内记录</button>
            </form></li>
            <li><form method="post" action="teacherhomeworkhistory.vpage?quizDay=90&teacherId=${teacherSummary.teacherId!''}">
                <button  type="submit"<#if quizDay == 90>style="background-color: #C5C5C5"</#if>>查看3个月内记录</button>
            </form></li>
            </#if>
        </ul>
        <table  class="table table-hover table-striped table-bordered">
            <tr id="title">
                <th> 布置时间</th>
                <th> 检查时间</th>
                <th> 测验ID</th>
                <th> 班级名称</th>
                <th> 学生总人数</th>
                <th> 参与人数</th>
                <th> 完成人数</th>
                <th> IP数量</th>
                <th> 布置老师</th>
                <th> 操作</th>
                <th> 来源</th>
            </tr>
            <#if quizList?? && quizList?has_content>
                <#list quizList as quiz>
                    <tr>
                        <td>${quiz["arrangeTime"]!''}</td>
                        <td>${quiz["checkTime"]!''}</td>
                        <td>${quiz["quizId"]!''}</td>
                        <td><a target="_blank" href="../clazz/groupinfo.vpage?teacherId=${teacherSummary.teacherId!}&clazzId=${quiz["clazzId"]!''}">${quiz["clazzName"]!''}</a></td>
                        <td>${quiz["studentCount"]!''}</td>
                        <td>${quiz["joinCount"]!''}</td>
                        <td>${quiz["completeCount"]!''}</td>
                        <td>${quiz["ipcount"]!''}</td>
                        <td><#if (quiz["quizTeacherId"])??>
                            <a target="_blank" href="../teachernew/teacherdetail.vpage?teacherId=${quiz['quizTeacherId']}">
                                ${quiz["quizTeacherName"]!''}(${quiz["quizTeacherId"]!''})
                            </a>
                        </#if></td>
                        <td>
                            <#--<a>删除</a>-->
                            <#--<a>补发园丁豆</a>-->
                                <a name="integral_detail" role="button" data-content-id="${quiz["quizId"]!}" data-type="quiz" class="btn">园丁豆详情</a>
                        </td>
                        <td>${quiz["source"]!''}</td>
                    </tr>
                </#list>
            <#else ><td colspan="9">暂无历史信息</td>
            </#if>
        </table>
    </div>
</div>
<#-- 园丁豆计算公式提示框 -->
<div  id="integralTipBox"  class="modal hide fade">
    <dl style="position: relative;overflow-y: auto;min-height: 450px;padding: 15px;">
        <dl class="dl-horizontal">
            <div class="dropDownBox_tip">
                <div style="width: 550px; padding: 10px 30px 30px 30px;" >
                    <h4 class="h-title">上学期平均完成作业人数：<span class="studentNum">0</span></h4>
                    <div class="Calculation-detailClazz-box">
                        <ul style="border: medium none; display: block;" class="goldDetailSuccess">
                            <li>
                                <h5 class="w-orange"><span style="float: right;">x</span>作业完成比例</h5>
                                <p class="w-orange"><span class="currentCount finishedRatio">0</span><span class="text_well">%</span></p>
                            </li>
                            <li>
                                <h5 class="w-blue"><span style="float: right;">x</span>按时完成</h5>
                                <p class="w-blue"><span class="currentCount finishNum">0</span><span class="text_well">人</span></p>
                            </li>
                            <li>
                                <h5 class="w-blue"><span style="float: right;">=</span>减负系数</h5>
                                <p class="factor w-blue coefficient">0</p>
                            </li>
                            <li>
                                <h5 class="w-red">获得园丁豆</h5>
                                <p class="gold w-red integralNum">0</p>
                            </li>
                        </ul>
                    </div>
                    <div class="Calculation-foot">
                        <p id="factor-text">影响园丁豆数量的因素：</p>
                        <p>按时完成作业人数：在作业到期或老师检查作业之前完成作业的学生数</p>
                        <p>作业完成比例：按时完成作业的人数占上学期平均完成作业人数的比例</p>
                        <p>减负系数：根据每周布置作业次数的不同而奖励不同比例的园丁豆（提高教师等级可以提高该系数）</p>
                    </div>
                </div>
            </div>
        </dl>
    </div>
</div>
</body>
<script>
    $(function(){

        $("a[name='homework_delete']").on("click", function () {
            var homeworkId = $(this).attr("data-content-id");
            if(confirm("确定删除ID为" + homeworkId + "的作业吗？")){
                $.ajax({
                    type: "post",
                    url: "../teacher/disablehomework.vpage",
                    data: {
                        homeworkId: homeworkId,
                        teacherId: ${teacherSummary.teacherId!''}
                    },
                    success: function (data) {
                        if (data.success) {
                            window.location.href = "teacherhomeworkhistory.vpage?homeworkDay=30&teacherId=${teacherSummary.teacherId!''}";
                        } else {
                            alert(data.info);
                        }
                    }
                });
            }
        });

        $("a[name='homework_add_Integral']").on("click", function () {
            var homeworkId = $(this).attr("data-content-id");
            if(confirm("确定要补发园丁豆么？教师将按照完成学生数量的1.5倍发放，每个完成的学生将获得11学豆")){
                $.ajax({
                    type: "post",
                    url: "../teacher/addhomeworkintegral.vpage",
                    data: {
                        homeworkId: homeworkId,
                        teacherId: ${teacherSummary.teacherId!''}
                    },
                    success: function (data) {
                        if (data.success) {
                            window.location.href = "teacherhomeworkhistory.vpage?homeworkDay=30&teacherId=${teacherSummary.teacherId!''}";
                        } else {
                            alert(data.info);
                        }
                    }
                });
            }
        });

        $("a[name='integral_detail']").on("click",function(){
            var homeworkId = $(this).attr("data-content-id");
            var type = $(this).attr("data-type");
            if(homeworkId != undefined &&  homeworkId !=""){
                $.ajax({
                    type:"post",
                    url:"homeworkgolddetail.vpage",
                    data:{
                        homeworkId:homeworkId,
                        type:type,
                        teacherId:${teacherSummary.teacherId!''}
                    },
                    success:function(data){
                        if(data.success){
                            if(!data.isCheating){
                                var homeworkIntegral = data.homeworkIntegral;
                                if(homeworkIntegral != undefined && homeworkIntegral !=""){
                                    $(".studentNum").text(homeworkIntegral.totalCount);
                                    $(".finishedRatio").text(homeworkIntegral.ratio);
                                    $(".finishNum").text(homeworkIntegral.currentCount);
                                    $(".coefficient").text(homeworkIntegral.factor);
                                    $(".integralNum").text(homeworkIntegral.gold);
                                    $("#integralTipBox").modal("show");
                                }else{
                                    alert("该作业没有园丁豆奖励记录");
                                }
                            }else{
                                alert("该作业是疑似作弊作业，没有奖励园丁豆");
                            }
                        }else{
                            alert(data.info);
                        }
                    }
                });
            }else{
                alert("作业id错误，请刷新页面重试");
            }
        });
    });
</script>
</html>