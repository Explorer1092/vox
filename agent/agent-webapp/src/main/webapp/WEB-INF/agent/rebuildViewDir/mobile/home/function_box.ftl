<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="百宝箱" pageJs="" footerIndex=1>
<@sugar.capsule css=['new_home','swiper3']/>
<div class="res-top fixed-head">
    <div class="return"><a href="/mobile/performance/index.vpage"><i class="return-icon"></i>返回</a></div>
    <span class="return-line"></span>
    <span class="res-title">百宝箱</span>
</div>
<div class="home-box">
<div class="h-content">
<div class="h-main">
    <div class="h-column j-flex">
        <#--进校效果-->
        <#if requestContext.getCurrentUser().isBusinessDeveloper()>
            <a onclick='openSecond("/mobile/analysis/school_analysis.vpage")' style="width:31%">
        <#else>
            <a onclick='openSecond("choose_agent.vpage?breakUrl=school_analysis&needCityManage=1")' style="width:31%">
        </#if>
                <div>
                    <p class="img5"></p>
                    <p>学校回流情况</p>
                </div>
            </a>
        <#--学校分析-->
        <#if requestContext.getCurrentUser().isBusinessDeveloper()>
            <a href="visit_school_result_page.vpage" style="width:31%">
        <#else>
            <a href="choose_agent.vpage?breakUrl=visit_school_result&needCityManage=1" style="width:31%">
        </#if>
                <div>
                    <p class="img3"></p>
                    <p>进校效果</p>
                </div>
            </a>
            <#--回流分析-->
            <a class="teacherAuth" style="width:31%">
                <div>
                    <#if teacherAuthCount?? && teacherAuthCount != 0><div style="border-radius:100%;color: white;width:1rem;height:1rem;display: inline-block;font-size:.6rem;line-height:1rem;background:red;position:absolute;top: -.3rem;right: 1.75rem;text-align: center;">${teacherAuthCount!0}</div></#if>
                    <p class="img7"></p>
                    <p>新老师认证进度</p>
                </div>
            </a>
    </div>
    <div class="h-column j-flex">
        <#--TOP校榜-->
            <#if requestContext.getCurrentUser().isBusinessDeveloper()>
                <a href="/mobile/analysis/top_school_rankings.vpage" style="width:31%">
            <#else>
                <a href="choose_agent.vpage?breakUrl=top_school_rankings&needCityManage=1" style="width:31%">
            </#if>
                    <div>
                        <p class="img5"></p>
                        <p>TOP校榜</p>
                    </div>
                </a>
            <a href="/mobile/apply/application.vpage" style="width:31%">
                <div>
                    <p class="applyProcess"></p>
                    <p>申请与审核</p>
                </div>
            </a>
    </div>
</div>
    </div>
    </div>
<script src="https://cdn-cnc.17zuoye.cn/public/script/voxLogs.js?v=2016-06-02"></script>
<script>
    $(document).on("click",'.teacherAuth',function(){
        YQ.voxLogs({
            database : "marketing", //不设置database  , 默认库web_student_logs
            module : "m_VjelQG49", //打点流程模块名
            op : "o_Avfhcr60" ,//打点事件名
            userId:${currentUser.userId!0}
        });
        <#if requestContext.getCurrentUser().isBusinessDeveloper()>
            setTimeout('window.location.href = "/mobile/resource/teacherauth/index.vpage"',500);
        <#else>
            setTimeout("window.location.href = 'choose_agent.vpage?breakUrl=teacherauth&needCityManage=1'",500);
        </#if>
    });
    $(document).on("click",".js-tianquan",function(){
        AT.alert("请到天权-我的申请模块发起申请");
    });
</script>
</@layout.page>
