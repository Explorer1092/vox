<#--/mobile/apply/application.vpage-->
<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="申请与审批" pageJs="" navBar="hidden">
    <@sugar.capsule css=['audit']/>
<div class="crmList-box resources-box">
    <div class="apply_box">
        <ul class="list_two">
            <li><a onclick="openSecond('/mobile/apply/index.vpage')"><div class="myApply"></div><div class="info">我发起的申请</div></a></li>
            <li><a onclick="openSecond('/mobile/audit/todo_list.vpage')"><div class="pending"></div><div class="info">待我审批</div></a></li>
        </ul>
    </div>
    <div class="apply_box">
        <div class="title">创建新的申请</div>
        <ul class="list_three">
            <li><a onclick="openSecond('/mobile/school_clue/appraisalSchool.vpage')"><div class="authenticate"></div><div class="info">鉴定学校</div></a></li>
            <li class="js-tianquan"><a href="javascript:;"><div class="purchase"></div><div class="info">购买物料<br/>（天权）</div></a></li>
            <li class="js-tianquan"><a href="javascript:;"><div class="applyTest"></div><div class="info">统考测评<br/>（天权）</div></a></li>
        </ul>
    </div>
</div>
<script>
    var AT = new agentTool();
    $(document).on("click",".js-tianquan",function(){
        AT.alert("请到天权-我的申请模块发起申请");
    });
</script>
</@layout.page>