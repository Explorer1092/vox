<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page phoneType="payPhone">
    <@sugar.capsule css=["project.afentiexam"] />
    <!--//start-->
    <div class="header">
        <div class="inner">
            <a href="/afenti/api/index.vpage" class="btnOpening btnTry" title="免费试用">免费试用</a>
            <a href="/apps/afenti/order/exam-cart.vpage?refer=go" class="btnOpening" title="立即开通">立即开通</a>
        </div>
    </div>
    <div class="main">
        <p class="picture_1"></p>
        <p class="picture_2"></p>
       <#-- <p class="picture_3"></p>
        <p class="picture_4"></p>
        <@ftlmacro.chargeinfo name="all" game="1" />
        -->
    </div>

    <div class="footer">
        <div class="inner">
            <a href="/afenti/api/index.vpage" class="btnOpening btnTry" title="免费试用">免费试用</a>
            <a href="/apps/afenti/order/exam-cart.vpage?refer=go" class="btnOpening" title="立即开通">立即开通</a>
        </div>
    </div>
    <!--end//-->
    <script type="text/javascript">
        $(function(){
            $(".btnTry").on("click", function(){
                $17.tongji("阿分题统计", "阿分题_介绍页_免费试用");
            });
            $(".btnOpening").on("click", function(){
                $17.tongji("阿分题统计", "阿分题_介绍页_立即开通");
            });
        });
    </script>
</@temp.page>