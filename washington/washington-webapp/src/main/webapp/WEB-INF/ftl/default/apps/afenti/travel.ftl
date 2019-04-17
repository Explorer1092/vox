<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page>
    <@app.css href="public/skin/project/afenti/travel/skin.css" />
    <!--//start-->
    <div class="header">
        <div class="inner">
            <#if ftlmacro.devTestStagingSwitch>
                <a href="/apps/afenti/order/travel-cart.vpage" class="btnOpening travelkaitong" title="立即开通">立即开通</a>
                <a href="/student/afenti/travel/index.vpage" class="btnOpening btnOpeningShiy travelkaitong" title="马上试用">马上试用</a>
            </#if>
        </div>
    </div>
    <div class="main">
        <p class="picture_1"></p>
        <p class="picture_2"></p>
    </div>
    <div class="picture_3">
        <div class="inner"></div>
    </div>
    <@ftlmacro.chargeinfo name="all" game="3" />
    <!--end//-->
    <script type="text/javascript">
        $(function(){
            $(".travelkaitong").on("click", function(){
                $17.tongji("阿分题统计", "阿分题_介绍页_立即开通");
            });
        });
    </script>
</@temp.page>