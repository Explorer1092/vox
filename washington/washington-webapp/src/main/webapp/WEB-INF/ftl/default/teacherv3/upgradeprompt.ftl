<#import "../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
<style type="text/css">
    .t-featureUpgrades-box{text-align: center;color: #5b6565;background-color: #fff;}
    .t-featureUpgrades-box .fu-title{font-size:24px;line-height: 70px;padding:40px 0 0 0;}
    .t-featureUpgrades-box .fu-banner{background: url('<@app.link href="public/skin/teacherv3/images/featureUpgrades-banner.png"/>') no-repeat;width:135px;height:136px;margin:0 auto;}
    .t-featureUpgrades-box .fu-info{font-size:16px;line-height: 45px;padding:35px 0 250px 0;}
</style>
<!--//start-->
<div class="t-featureUpgrades-box">
    <div class="fu-title">作业功能升级中...</div>
    <div class="fu-banner"></div>
    <div class="fu-info">为了更好的作业体验，作业功能于2月17号晚至2月22号开始升级，<br/>
        升级期间作业功能暂不可用并且系统在本周内会自动检查作业和发放园丁豆，敬请期待全新的版本
    </div>
</div>
<!--end//-->
<script type="text/javascript">
    $(function(){
        LeftMenu.focus("main");
    });
</script>
</@shell.page>