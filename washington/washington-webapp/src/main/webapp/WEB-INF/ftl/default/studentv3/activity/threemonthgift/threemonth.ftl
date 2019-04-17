<#import "../../../layout/project.module.student.ftl" as temp />
<@temp.page title="3月作业好礼" header="hide">
<@app.css href="public/skin/project/threemonthgift/student/threemonth.css" />
<div class="t-ycMarchSpree-box">
    <div class="t-ycMarchSpree-inner">
        <div class="header">
            <div class="logo"></div>
            <div class="return"><a href="/student/index.vpage">返回首页</a></div>
        </div>
        <div class="t-ycMarchSpree-content">
            <div class="yc-title">活动时间：3月23日－3月31日</div>
            <div class="yc-column yc-differ">
                <div class="yc-left">
                    <div class="yc-tag">活动1.</div>
                    <div class="yc-info">
                        <p class="yc-tips1">魔法城堡全天开放，唤醒沉睡的魔法师！</p>

                    </div>
                </div>
                <div class="yc-right">
                    <a href="/student/magic/castle.vpage" class="info_btn info_btn_1">去魔法城堡</a>
                </div>
            </div>
            <div class="yc-column bgNone yc-differ">
                <div class="yc-left">
                    <div class="yc-tag">活动2.</div>
                    <div class="yc-info">
                        <p class="yc-tips1">3月小组成员完成3次作业即可获得额外10学豆奖励。（班上有英语或数学老师的时候才能参与这个活动哟）</p>
                    </div>
                </div>
                <div class="yc-right">
                    <#if ftlmacro.devTestStagingSwitch>
                        <#if ((.now>="2016-03-22 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')) && (.now<"2016-04-11 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')))>
                            <a href="javascript:void(0);" class="info_btn info_btn_1 js-getStudentRward">领取10学豆</a>
                        <#elseif (.now>="2016-04-11 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')) >
                            <a href="javascript:void(0);" class="info_btn info_btn_1 disabled">活动已结束</a>
                        </#if>
                    <#else>
                        <#if ((.now>="2016-03-23 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')) && (.now<"2016-04-01 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')))>
                            <a href="/student/clazz/index.vpage" class="info_btn info_btn_1">去查看我的小组</a>
                        <#elseif ((.now>="2016-04-01 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')) && (.now<"2016-04-11 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')))>
                            <a href="javascript:void(0);" class="info_btn info_btn_1 js-getStudentRward">领取10学豆</a>
                        <#elseif (.now>="2016-04-11 00:00:00"?datetime('yyyy-MM-dd HH:mm:ss')) >
                            <a href="javascript:void(0);" class="info_btn info_btn_1 disabled">活动已结束</a>
                        </#if>
                    </#if>
                </div>
            </div>
            <div class="yc-triangle">
                <p>奖励请在4月10日前领取，过期后活动下线将无法领取。（所有数据次日更新）</p>
            </div>
        </div>
    </div>

</div>
<#--T:弹窗提示详情-->
<script type="text/html" id="T:successDialog">
    <div class="ycMarchSpree-popup">
        <div class="hp-content">您已成功领取10学豆</div>
        <div class="hp-btn">
            <a href="javascript:$.prompt.close();" class="reward_btn">确定</a>
        </div>
        <div class="hp-close">
            <a class="close" href="javascript:$.prompt.close();"></a>
        </div>
    </div>
</script>
<script>
    $(document).on("click",".js-getStudentRward",function(){
        $.post("receivethreemonthreward.vpage",{},function(result){
            if(result.success){
                $.prompt(template("T:successDialog", {}),{
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
    });
</script>
</@temp.page>