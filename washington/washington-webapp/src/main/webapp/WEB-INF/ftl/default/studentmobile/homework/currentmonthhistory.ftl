
<#import "../layout.ftl" as temp >
<@temp.page>
<style>
    .tab li { width: 32%;}
    .tab li:nth-child(2){ margin-right: 2%;}
</style>
<div class="wr">
    <ul id="subject_list_box" class="tab clearfix">
        <li class="" data-subject="ENGLISH">英语</li>
        <li class="" data-subject="MATH">数学</li>
        <li class="" data-subject="CHINESE">语文</li>
    </ul>
    <div id="content_box"></div>
</div>

<script type="text/html" id="content_box_tem">
    <%if(data.homeworkHistory.length > 0){%>
        <%var charColor = ''%>
        <%if(subject == 'ENGLISH'){%>
            <% charColor = 'result-chart-yellow'%>
        <%}else if(subject == 'MATH'){%>
            <% charColor = 'result-chart-green'%>
        <%}else if(subject == 'CHINESE'){%>
            <% charColor = 'result-chart-blue'%>
        <%}%>

        <div class="result-chart <%=charColor%>">
            <div class="box">
                <div class="title">本月成绩：</div>
                <%if(data.avgScore >= 90){%>
                    <div class="md-img md-img-0"></div>
                    <div class="md-text">作业平均成绩达 90 分以上</div>
                <%}else if(data.avgScore >= 80 && data.avgScore < 90){%>
                    <div class="md-img md-img-1"></div>
                    <div class="md-text">作业平均成绩 80 分以上</div>
                <%}else if(data.avgScore >= 60 && data.avgScore < 80){%>
                    <div class="md-img md-img-2"></div>
                    <div class="md-text">作业平均成绩未达到 80 分</div>
                <%}else if(data.avgScore < 60){%>
                    <div class="md-img md-img-3"></div>
                    <div class="md-text">作业平均成绩未达到 60 分</div>
                <%}%>
            </div>
        </div>

        <ul class="result-list">
            <%for(var i = 0,score = data.homeworkHistory; i < score.length; i++){%>
                <a onclick="clickReport();" href="/studentMobile/homework/app/currentmonth/history/detail.vpage?homeworkId=<%=score[i].homeworkId%>&subject=<%=subject%>">
                    <li>
                        <div class="head"><%=score[i].unitNames%></div>
                        <div class="time">开始时间 : <%=score[i].startDate%></div>
                        <div class="score">
                            <%if(score[i].finished) { %>
                                <%if(score[i].homeworkScore == null) { %>
                                    已完成
                                <% } else { %>
                                    <%=score[i].homeworkScore%>分
                                <% } %>
                            <% } else { %>
                                未完成
                            <% } %>
                        </div>
                    </li>
                </a>
            <%}%>
        </ul>
    <%}else{%>
        <div class="no-record">
            <#if (currentStudentDetail.clazz.classLevel)??>
                还没有作业报告哦～
            <#else>
                加入班级后才可查看哦~
            </#if>
        </div>
    <%}%>
</script>

<script type="text/javascript">
    //设置title
    document.title = '成绩单';

    function clickReport(){
        //log
        var logObj = {};
        logObj.app = "17homework_my";
        logObj.type = "log_normal";
        logObj.module = "user";
        logObj.operation = "page_homework_report_click";
        $M.appLog('reward',logObj);
    }

    $(function(){
        var subject = '${subject!'ENGLISH'}'.toUpperCase();
        var subject_list_box = $('#subject_list_box');

        //选择学科
        subject_list_box.find('li').on('click',function(){
            var $this = $(this);
            var subject = $this.data('subject');
            if(subject == 'ENGLISH'){
                $this.addClass('yellow').siblings().removeClass('green blue');
            }else if(subject == 'MATH'){
                $this.addClass('green').siblings().removeClass('yellow blue');
            }else if(subject == 'CHINESE'){
                $this.addClass('blue').siblings().removeClass('green yellow');
            }
            $M.showLoading();
            $.ajax({
                url: '/studentMobile/homework/app/currentmonth/history.vpage',
                async: false,
                type: "POST",
                data: {subject: subject},
                success: function (data) {
                    if(data.success){
                        $('#content_box').empty().html(template('content_box_tem',{data : data,subject : subject}));
                    }else{
                        loginInvalid(data);
                    }
                    $M.hideLoading();
                },
                error: function(){
                    $M.hideLoading();
                }
            });
        });

        <#if subject?? && subject?has_content>
            subject_list_box.find('li[data-subject='+subject+']').click();
        <#else>
            subject_list_box.find('li[data-subject="ENGLISH"]').click();
        </#if>

        //log
        $M.appLog('homework',{
            app: "17homework_my",
            type: "log_normal",
            module: "user",
            operation: "page_homework_report"
        });

    });
</script>
</@temp.page>