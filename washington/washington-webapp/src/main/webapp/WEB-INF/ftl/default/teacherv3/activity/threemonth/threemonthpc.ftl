<#import "../../../layout/project.module.ftl" as temp />
<@temp.page title="3月作业好礼" header="hide">
<@app.css href="public/skin/project/threemonthgift/teacher/threemonth.css" />
<div class="t-ycMarchSpree-box">
    <div class="t-ycMarchSpree-inner">
        <div class="header">
            <div class="logo"></div>
            <div class="return"><a href="/teacher/index.vpage">返回首页</a></div>
        </div>
        <div class="t-ycMarchSpree-content">
            <div class="yc-title">活动时间：3月23日－3月31日</div>
            <div class="yc-column">
                <div class="yc-left">
                    <div class="yc-tag">活动1.</div>
                    <div class="yc-info">
                        <p class="yc-tips1">3月每班检查3次本月的作业，领10次免费抽奖机会。</p>
                        <#if dataMap??>
                            <p class="yc-tips2">我有<span>${(dataMap.clazzCount)!'0'}</span>个班级，${(dataMap.enoughCount)!'0'}个班级已检查3次 <a href="javascript:void(0);" class="view_btn js-showClazzDetail">查看详情</a></p>
                        </#if>
                    </div>
                </div>
                <div class="yc-right">
                    <a href="javascript:void(0);" class="js-getRewardChange info_btn <#if ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv() || ProductDevelopment.isStagingEnv() || ((.now>="2016-04-01 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')) && (.now<"2016-04-11 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')))><#else>disabled</#if>">4月1日领10次免费抽奖机会</a>
                </div>
            </div>
            <div class="yc-column">
                <div class="yc-left">
                    <div class="yc-tag">活动2.</div>
                    <div class="yc-info">
                        <p class="yc-tips1">3月学生完成我的3次作业，按学生数领园丁豆。</p>
                        <#if dataMap??>
                            <p class="yc-tips2">我已有<span class="js-countBeanNumber">${(dataMap.studentCount)!'0'}</span>名学生完成我的3次作业</p>
                        </#if>
                        <p class="yc-tips3">(只有绑定手机的学生才会计入以上人数)</p>
                    </div>
                </div>
                <div class="yc-right">
                    <a href="javascript:void(0);" class="js-getTeacherBean info_btn <#if ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv() || ProductDevelopment.isStagingEnv() || ((.now>="2016-04-01 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')) && (.now<"2016-04-11 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')))><#else>disabled</#if>">4月1日领取<#if dataMap??>${(dataMap.studentCount)!'0'}</#if>园丁豆</a>
                </div>
            </div>
            <div class="yc-column bgNone">
                <div class="yc-left">
                    <div class="yc-tag">活动3.</div>
                    <div class="yc-info">
                        <p class="yc-tips1">3月小组成员完成3次作业即可获得额外10个学豆奖励。（小组功能目前仅对英语、数学老师开放）</p>
                    </div>
                </div>
                <div class="yc-right">
                    <a href="/teacher/systemclazz/clazzindex.vpage" class="info_btn info_btn_1">去管理我的小组</a>
                </div>
            </div>
            <div class="yc-triangle">
                <p>奖励请在4月10日前领取，过期后活动下线将无法领取。（所有数据次日更新）</p>
            </div>
        </div>

    </div>
</div>
<#--T:班级详情-->
<script type="text/html" id="T:threeMonthClazzDetail">
    <div class="ycMarchSpree-popup2">
        <div class="hp-title">我的班级学生完成作业情况</div>
        <div class="hp-btn">
            <a href="javascript:$.prompt.close();" class="sure_btn">确定</a>
        </div>
        <div class="hp-close">
            <a class="close" href="javascript:$.prompt.close();"></a>
        </div>
        <div class="hp-list">
            <ul>
                <li class="l-head"><span class="l-left">我的班级</span><span>3次作业是否达成</span></li>
                <%for (var i=0;i<classList.length;i++) {%>
                <li> <span class="l-left"><%=classList[i].clazzName%></span>
                    <span><%if (classList[i].hasEnough){%>已<%}%><%if (!classList[i].hasEnough){%>未<%}%>达成</span>
                </li>
                <%}%>
                <%if (classList.length ==0) {%>
                <li>
                    <span>暂时无法查看班级详情</span>
                </li>
                <%}%>
            </ul>
        </div>
    </div>
</script>
<#--T:弹窗提示详情-->
<script type="text/html" id="T:successDialog">
    <div class="ycMarchSpree-popup">
        <%if (type == 'teacherBean') {%>
            <div class="hp-content">您已成功领取<%=beanNumber%>园丁豆</div>
        <%}%>
        <%if (type == 'awardChance') {%>
            <div class="hp-content">您已领取10次免费抽奖机会</div>
            <div class="hp-info">抽奖机会仅今天有效，过期无法使用，快去抽奖吧！</div>
        <%}%>
        <div class="hp-btn">
            <a href="/campaign/teacherlottery.vpage" class="reward_btn">去抽奖</a>
        </div>
        <div class="hp-close">
            <a class="close" href="javascript:$.prompt.close();"></a>
        </div>
    </div>
</script>
<script>
var threemonthClazzDetail = [];
<#if dataMap??>
threemonthClazzDetail = ${json_encode(dataMap.clazzList)};
</#if>
/*查看详情*/
$(document).on("click",".js-showClazzDetail",function(){
    $.prompt(template("T:threeMonthClazzDetail", {classList:threemonthClazzDetail}),{
        prefix : "null-popup",
        buttons : { },
        classes : {
            fade: 'jqifade',
            close: 'w-hide'
        }
    });
});

/*领取抽奖机会*/
$(document).on("click",".js-getRewardChange",function(){
    if($(this).hasClass("disabled")){
        return false;
    }else{
        $.post('/teacher/activity/receivethreemonthlotteryreward.vpage',{},function(result){
            if(result.success){
                $.prompt(template("T:successDialog", {type:'awardChance'}),{
                    prefix : "null-popup",
                    buttons : { },
                    classes : {
                        fade: 'jqifade',
                        close: 'w-hide'
                    }
                });
            }else{
                $17.alert(result.info);
            }
        });
    }
});

/*领取园丁豆*/
$(document).on("click",".js-getTeacherBean",function(){
    if($(this).hasClass("disabled")){
        return false;
    }else {
        $.post('/teacher/activity/receivethreemonthintegralreward.vpage', {}, function (result) {
            if (result.success) {
                $.prompt(template("T:successDialog", {type:'teacherBean',beanNumber:$(".js-countBeanNumber").html()}),{
                    prefix : "null-popup",
                    buttons : { },
                    classes : {
                        fade: 'jqifade',
                        close: 'w-hide'
                    }
                });
            } else {
                $17.alert(result.info);
            }
        });
    }
});

</script>
</@temp.page>
