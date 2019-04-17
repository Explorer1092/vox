<#import "../layout.ftl" as temp >
<@temp.page title="学豆奖励">
    <@app.css href="public/skin/mobile/pc/css/integralReward.css" />

    <#assign integral = integralPrize!0 >

    <div class="beanAward">
        <div class="header">
            <#if integral gt 0>
                <div class="bean-num"><i class="icon"></i><span class="num">${integral}</span></div>
            </#if>
        </div>
        <div class="main">
            <div class="copywriter">登录家长通查看每次作业的详细诊断，有机会获得额外的学豆奖励哦</div>
            <a href="javascript:;" class="btn-default btn-blue doClickOpenParent" data-module="m_ERIgMdYP" data-op="extra_studies">立即查看</a>
        </div>
        <div class="footer"></div>
    </div>
    <script type="text/javascript">
        document.title = "学豆奖励";
    </script>
</@temp.page>
