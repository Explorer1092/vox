<#import "../../../layout/project.module.ftl" as temp />
<@temp.page title="3月作业好礼" header="hide">
<@app.css href="public/skin/project/threemonthgift/teacher/threemonthwx.css" />
<div class="t-package-box">
    <div class="p-bg">
        <div class="p-title">活动时间：3月23日－3月31日</div>
        <div class="p-activity">
            <div class="p-tag">活动1</div>
            <div class="p-content">
                <p>3月每个班检查3次本月布置的作业，4月1日领10次免费抽奖机会。</p>
                <#if dataMap??>
                <p>我有${(dataMap.clazzCount)!'0'}个班级，${(dataMap.enoughCount)!'0'}个班级已检查3次。<a href="javascript:void(0);" class="view_btn js-showClazzDetail">查看详情</a></p>
                </#if>
                </div>
            <div class="p-btn">
                <a href="javascript:void(0);" class="js-getRewardChange info_btn <#if ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv() || ProductDevelopment.isStagingEnv() || ((.now>="2016-04-01 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')) && (.now<"2016-04-11 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')))><#else>disabled</#if>">4月1日领10次免费抽奖机会</a>
            </div>
        </div>
        <div class="p-activity">
            <div class="p-tag">活动2</div>
            <div class="p-content">
                <p>3月学生完成我的3次作业，4月1日按学生数领园丁豆。</p>
                <#if dataMap??>
                <p>我已有<span class="js-countBeanNumber">${(dataMap.studentCount)!'0'}</span>名学生完成我的3次作业(只有绑定手机的学生才会计入以上人数)</p>
                </#if>
                </div>
            <div class="p-btn">
                <a href="javascript:void(0);" class="js-getTeacherBean info_btn <#if ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv() || ProductDevelopment.isStagingEnv() || ((.now>="2016-04-01 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')) && (.now<"2016-04-11 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')))><#else>disabled</#if>">4月1日领取${(dataMap.studentCount)!'0'}个园丁豆</a>
            </div>
        </div>
        <div class="p-activity">
            <div class="p-tag">活动3</div>
            <div class="p-content"><p>3月小组成员完成3次作业即可获得额外10个学豆奖励。（小组功能目前仅对英语、数学老师开放）</p></div>
            <div class="p-btn">
                <a href="javascript:void(0);" class="info_btn" id="manageclazz">去管理我的小组</a>
            </div>
        </div>
        <div class="p-info">
            <div class="p-arrow"><p>免费抽奖机会和园丁豆请在4月10日前领取，过期后活动下线将无法领取。（所有数据次日更新）</p></div>
        </div>
    </div>
</div>
<@app.script href="public/skin/project/threemonthgift/teacher/js/deviceFontSize.js" />
<#--T:弹窗提示详情-->
<script type="text/html" id="T:successDialog">
    <div class="t-package-pop">
        <div class="p-inner">
            <div class="p-close"><a href="javascript:$.prompt.close();" class="close">×</a></div>
            <%if (infoType) {%>
                <%if (type == 'teacherBean') {%>
                <div class="p-title">您已成功领取<%=beanNo%>园丁豆</div>

                <%}%>
                <%if (type == 'awardChance') {%>
                <div class="p-title">您已领取10次免费抽奖机会</div>
                <div class="p-info"><p>抽奖机会仅今天</p><p>有效，过期无法使用，快去抽奖吧！</p></div>
                <%}%>
                <div class="p-btn"><a href="javascript:void(0);" class="btn_reward" id="golottery">去抽奖</a></div>

            <%}%>
            <%if (!infoType) {%>
            <div class="p-title"><%=info%></div>
            <div class="p-btn"><a href="javascript:$.prompt.close();" class="btn_reward">确定</a></div>
            <%}%>
        </div>
    </div>
</script>
<#--T:班级详情-->
<script type="text/html" id="T:threeMonthClazzDetail">
    <div class="t-package-pop">
        <div class="p-inner">
            <div class="p-title">我的班级学生完成作业情况</div>
            <div class="p-list">
                <ul>
                    <li class="p-head"> <span class="p-left">我的班级</span><span>3次作业是否达成</span></li>
                    <%for (var i=0;i<classList.length;i++) {%>
                    <li> <span class="p-left"><%=classList[i].clazzName%></span>
                        <%if (classList[i].hasEnough){%>
                        <span class="completed">已完成</span>
                        <%}%><%if (!classList[i].hasEnough){%>
                        <span class="noCompleted">未完成</span>
                        <%}%>
                    </li>
                    <%}%>
                    <%if (classList.length ==0) {%>
                    <li>
                        <span>暂时无法查看班级详情</span>
                    </li>
                    <%}%>
                </ul>
            </div>
            <div class="p-close"><a href="javascript:$.prompt.close();" class="close">×</a></div>
            <div class="p-btn"><a href="javascript:$.prompt.close();" class="btn_reward">确定</a></div>
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
            buttons : {},
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
                postFunCallback(result,'awardChance');
            });
        }
    });

    /*领取园丁豆*/
    $(document).on("click",".js-getTeacherBean",function(){
        if($(this).hasClass("disabled")){
            return false;
        }else {
            $.post('/teacher/activity/receivethreemonthintegralreward.vpage', {}, function (result) {
                postFunCallback(result,'teacherBean');
            });
        }
    });

    /*去微信管理班级*/
    $(document).on("click","#manageclazz",function(){
        if($(this).hasClass("disabled")){
            return false;
        }else {
            // 跳转到微信的页面去，因为域名不一样，所以如下
            location.href="http://wx."+location.host.replace("www.","")+"/teacher/clazzmanage/list.vpage";
        }
    });

    /*去微信抽奖*/
    $(document).on("click","#golottery",function(){
        if($(this).hasClass("disabled")){
            return false;
        }else {
            // 跳转到微信的页面去，因为域名不一样，所以如下
            location.href="http://wx."+location.host.replace("www.","")+"/teacher/ucenter/lottery/index.vpage";
        }
    });

    var postFunCallback = function(result,type){
        var data = {type:type,infoType:result.success,info:result.info,beanNo:$(".js-countBeanNumber").html()};
        $.prompt(template("T:successDialog", data),{
            prefix : "null-popup",
            buttons : { },
            classes : {
                fade: 'jqifade',
                close: 'w-hide'
            }
        });
    }
</script>
</@temp.page>
