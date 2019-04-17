<#--动态上方广告入口-->
<div style="position: relative;">
    <div style="position: absolute; top: 0; left: 70px;" >
    <#if (.now lt "2016-06-25 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss") && [330100,610100,120100,410100,130100]?seq_contains(currentTeacherDetail.cityCode))!false>
    <#--杭州APP推广动态入口-->
        <a href="/project/extension/index.vpage" target="_blank" style="display: block; margin-top: -7px; background: url(<@app.link href="public/skin/teacherv3/images/publicbanner/extension_gardenerbean.png"/>) no-repeat; width: 368px; height: 43px;" title="50园丁豆轻松得" class="js-click"></a>
    <#elseif (.now lt "2016-09-15 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss"))!false>
    <#--教师节推广动态入口1-->
        <a href="/teacher/activity/teachersday2016.vpage?_from=news_feed" target="_blank" style="display: block; margin-left: -10px; background: url(<@app.link href="public/skin/teacherv3/images/publicbanner/teaDay-in01.png"/>) no-repeat; width: 144px; height: 34px;" title="教师节送祝福"></a>
    <#else>
        <div class="js-ugcClickBtnPopup" style=" display: none;">
            <a href="javascript:void(0);"  style="display: block; margin-top: -19px; background: url(<@app.link href="public/skin/teacherv3/images/publicbanner/teacher-ugc-enter-icon.png"/>) no-repeat; width: 312px; height: 57px;" title="学校信息大收集"></a>
        </div>
    </#if>
    </div>
    <#if (.now lt "2016-09-15 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss"))!false>
    <#--教师节推广动态入口2-->
        <div style="position: fixed; bottom: 30px; right: 20px; z-index: 1">
            <a href="/teacher/activity/teachersday2016.vpage?_from=news_gift" target="_blank" style="display: block; background: url(<@app.link href="public/skin/teacherv3/images/publicbanner/teaDay-in02.png"/>) no-repeat; width: 82px; height: 76px;" title="教师节送祝福"></a>
        </div>
    </#if>
</div>
<script type="text/javascript">
    $(function(){
        $(document).on("click",".js-click",function(){
            $17.voxLog({
                module : "project-extension",
                op : "entrance"
            });
        });
    });
</script>

<div class="w-sets">
    <div class="w-sets-title">
        <h1>动态</h1>
        <div class="clazzAdjustment">
            班级调整小助手
        </div>
        <div class="w-sets-title-side">
            <#--园丁豆礼物数-->
            <div id="teacherWeekContainer"></div>
        </div>
    </div>
    <div class="w-sets-container w-sets-container-back">
        <#--临时活动动态 Temporary dynamic activity-->
        <div id="temporaryDynamicActivity"></div>
        <#--实时动态 Real-time dynamic-->
        <div id="realTimeDynamic"></div>
    </div>
</div>
<div class="t-dynamic-btn" id="dynamicMoreClickBtn" style="display: none;">
    <a class="more" href="javascript:void (0);">展开更多</a>
</div>

<script type="text/html" id="T:首页园丁豆和礼物数">
    本周获得：
    <a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/mygold.vpage?ref=newIndex" style="margin-left: 15px;"><span class="t-dynamic-sIcon t-dynamic-sIcon-1"></span> 园丁豆 <%if(data.gold > 0){%><span class="w-orange">＋<%=data.gold%></span><%}%></a>
    <a href="/teacher/gift/receive/index.vpage?ref=newIndex" style="margin-left: 15px;"><span class="t-dynamic-sIcon t-dynamic-sIcon-2"></span> 礼物 <%if(data.gift > 0){%><span class="w-orange">＋<%=data.gift%></span><%}%></a>
    <#if (!currentTeacherWebGrayFunction.isAvailable("Flower", "Close"))!true>
        <#if (currentTeacherDetail.subject != "CHINESE" && currentUser.fetchCertificationState() == "SUCCESS")!false>
        <a href="/teacher/flower/exchange.vpage?ref=flower" style="margin-left: 15px;"><span class="t-dynamic-sIcon t-dynamic-sIcon-4"></span> 家长点赞 <%if(data.flower > 0){%><span class="w-orange">＋<%=data.flower%></span><%}%></a>
        </#if>
    </#if>
    <#--<a href=""><span class="t-dynamic-sIcon t-dynamic-sIcon-3"></span> 赞<span class="w-orange">＋5</span></a>-->
</script>

<script type="text/html" id="T:班级调整">
    <style>div.jqi .jqiclose{border:none;}</style>
   <div class="clazzAdjustmentBox">
       <p>新学期到了，您在的班级调整上遇到了问题？<br/>选择您的问题，让小助手帮您一起操作吧！</p>
       <div class="clazzAdjustmentBtn">
            <a class="btn1" target="_blank" href="${(ProductConfig.getUcenterUrl())!''}/teacher/systemclazz/clazzindex.vpage?join=true">我要创建新班级</a>
            <a class="btn2" target="_blank" href="${(ProductConfig.getUcenterUrl())!''}/teacher/systemclazz/clazzindex.vpage?adjustment=true">我要转让班级</a>
       </div>
   </div>
</script>
<#--Template 加载-->
<script type="text/javascript">
    $(document).on("click",".js-get-award",function(){
        YQ.voxLogs({
            module: 'm_rQjVWe1G',
            op : "o_HNaTMdcF"
        });
    });

    //模拟a标签
    $(document).on("click","[data-href]",function(){
        location.href=$(this).data().href;
    }).on("click",".clazzAdjustment",function () {
        $.prompt(template("T:班级调整", {}), {
            buttons: {},
            position: {width: 480}
        });
    });

    $(function() {
        var temporaryDynamicActivity = $("#temporaryDynamicActivity");

        //获取园丁豆和礼物数
        $.post("/teacher/teacherweekggc.vpage", {}, function (data) {
            $("#teacherWeekContainer").html(template("T:首页园丁豆和礼物数", {data: data}));
        });

        //临时活动动态
        //动态广告位
        YQ.voxSpread({
            keyId : 110104  <#--动态广告位-->
        }, function(result){
            if(result.success && result.data.length > 0){
                var popupItems = result.data;
                if(popupItems.length>0){
                    temporaryDynamicActivity.append(template("T:temporaryDynamicActivity", {result : result}) );
                }
                return false;
            }else{
                temporaryDynamicActivity.append(template("T:temporaryDynamicActivity", {}) );
            }
        });

        setTimeout(function(){
            //一起作业学生手机版上线啦 - 复制
            if($("#recommendedAppContentCode").length > 0) {
                $17.copyToClipboard($("#recommendedAppContentCode"), $("#clip_button2"), "clip_button2", "clip_container2", function () {
                    $17.voxLog({
                        app: "shares",
                        module: "teacherSharesRegCourse",
                        op: "pc-dym-copyLink"
                    });
                });
            }
        }, 200);

        /*屏幕有奖互助动态模块 2018/8/21*/
        //有奖互助动态
        // $.get("/teacher/getmentorinfo.vpage", {}, function(data){
        //     temporaryDynamicActivity.append( template("T:有奖互助", {item: (data.mentorLatestInfo ? data.mentorLatestInfo : false),subject:$uper.subject.key }) );
        //
        //     temporaryDynamicActivity.on("mouseenter", ".m-invite-dynamic-box li", function(){
        //         $(this).find(".iv-btn").show();
        //     }).on("mouseleave", ".m-invite-dynamic-box li", function(){
        //         $(this).find(".iv-btn").hide();
        //     });
        // });

    });
</script>

<#--临时活动动态-->
<#include "dynamic-public.ftl"/>

<#--实时动态-->
<#include "dynamic-items.ftl"/>