<#-- @ftlvariable name="s" type="com.voxlearning.utopia.service.reward.entity.RewardCategory" -->
<#-- @ftlvariable name="t" type="com.voxlearning.utopia.service.reward.entity.RewardCategory" -->
<#macro page index='home' columnType="normal" phoneType="servicePhone">
    <#import "../layout/column.ftl" as column>
    <!DOCTYPE HTML>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>
-->
    <html>
        <head>
            <#include "../../nuwa/meta.ftl" />
            <title>一起教育科技，教学用品中心 - 一起教育科技 www.17zuoye.com</title>
            <@sugar.capsule js=["jquery", "core", "alert", "template", "voxLogs","ko"] css=["plugin.alert", "column","rewardBase"] />
            <@sugar.site_traffic_analyzer_begin/>
        </head>

        <body>
            <#--当前登录用户类型-->
            <#assign currentUserType><#switch (currentUser.userType)!'3'><#case 1>TEACHER<#break><#case 3>STUDENT<#break><#case 8>RSTAFF<#break></#switch></#assign>
            <#--积分类型-->
            <#assign integarlType><#switch (currentUser.userType)!'3'><#case 1><@ftlmacro.garyBeansText/><#break><#case 8><@ftlmacro.garyBeansText/><#break><#case 3>学豆<#break></#switch></#assign>
            <!--头部信息-->
            <div class="w-head">
                <div class="m-nav-bar">
                    <div class="w-inner">
                        <#if currentUserType != 'STUDENT'>
                            <a href="${(currentUserType == 'TEACHER')?string('${(ProductConfig.getUcenterUrl())!""}/teacher/center/index.vpage#/teacher/center/myprofile.vpage','${(ProductConfig.getUcenterUrl())!""}/rstaff/center/edit.vpage')}" target="_blank">收货地址</a>
                            <span>|</span>
                        </#if>

                        <a href="/reward/order/myorder.vpage">我的奖品(<strong class="my_order_count">0</strong>)</a>
                    <#--<a href="/ucenter/partner.vpage?url=${ProductConfig.getRewardSiteBaseUrl()}/order" target="_blank">查看旧订单</a>-->
                        <span>|</span>
                        <a href="/${currentUserType?lower_case}/index.vpage">返回首页</a>
                        <i class="w-leaf-left"></i>
                        <i class="w-leaf-right"></i>
                    </div>
                </div>

                <!--banner-->
                <#if (currentUser.userType == 1 || currentUser.userType == 3)!false>
                    <div class="switchBox belist" id="rewardSwitchBanner" style="width: 1000px; margin: 0 auto;">
                        <ul>
                            <#--<#if (currentUser.userType == 1)!false>-->
                                <#--<#if (currentTeacherDetail.isJuniorTeacher())!false>-->
                                    <#--<li>-->
                                        <#--<a href="/campaign/middleteacherlottery.vpage" target="_blank"><img src="<@app.link href="public/skin/reward/imagesV1/lottery/reward-banner-arrange.png"/>" width="1001" height="181"></a>-->
                                    <#--</li>-->
                                <#--</#if>-->
                                <#--<li>
                                    <a href="javascript:void(0)" target="_blank"><img src="<@app.link href="public/skin/reward/imagesV1/lottery/reward-banner-tea.png"/>"></a>
                                </li>-->
                                <#--下线：活动已过期（2016/5/23）-->
                                <#--<li>
                                    <a href="http://www.17huayuan.com/forum.php?mod=viewthread&tid=40924&extra=" target="_blank"><img src="<@app.link href="public/skin/reward/imagesV1/lottery/reward-banner-involvement.png"/>" width="1001" height="181"></a>
                                </li>-->
                            <#--</#if>-->

                            <#--老师banner-->
                            <#if (currentUser.userType == 1)!false>
                                <#if (currentTeacherDetail.isPrimarySchool())!false>
                                <li style="display:none;">
                                    <img style="width: 1000px; height: 180px;" src="<@app.link href="public/skin/reward/imagesV1/lottery/reward-banner-teacher-primary.png"/>">
                                </li>
                                </#if>
                                <#if (currentTeacherDetail.isJuniorTeacher())!false>
                                <li style="display:none;">
                                    <img style="width: 1000px; height: 180px;" src="<@app.link href="public/skin/reward/imagesV1/lottery/reward-banner-teacher-junior.png"/>">
                                </li>
                                </#if>
                            </#if>
                            <#--学生banner-->
                            <#--<#if (currentUser.userType == 3)!false>
                                <li style="display:none;">
                                    <img style="width: 1000px; height: 180px;" src="<@app.link href="public/skin/reward/imagesV1/lottery/reward-banner-stunew.jpg"/>">
                                </li>
                            </#if>-->
                        </ul>
                        <div class="tab" style="position: absolute; bottom: 0; width: 100%;"></div>
                    </div>
                    <@ftlmacro.allswitchbox target="#rewardSwitchBanner" second=3000/>
                </#if>
                <div class="m-slide-bar">
                    <#if currentUserType == 'TEACHER'>
                        <h1 class="logo logo-1"><a href="/reward/index.vpage"></a></h1>
                    </#if>
                    <#if currentUserType == 'STUDENT'>
                        <h1 class="logo logo-2"><a href="/reward/index.vpage"></a></h1>
                        <#--<div class="luck-draw-join" <#if index='lottery'>class="active"</#if>><a href="/campaign/studentlottery.vpage">幸运抽大奖</a></div>-->
                    </#if>
                    <div class="ms-box">
                        <ul>
                            <#--此处由于经常灰度控制显示，且老师学生等公用，故将三者拆开来控制-->
                            <#--学生-->
                            <#--<#if currentUserType == 'STUDENT'>-->
                                <#--37个城市灰度下的学生不显示下面两个导航-->
                                <#--<#if !(currentStudentWebGrayFunction.isAvailable("Reward", "OfflineShiWu"))!false>-->
                                    <#--<li <#if index='exclusive'>class="active"</#if>><a href="/reward/product/exclusive/index.vpage">一起专属</a></li>-->
                                    <#--<li <#if index='boutique'>class="active"</#if>><a href="/reward/product/boutique/index.vpage">限量精品</a></li>-->
                                <#--</#if>-->
                                <#--<li <#if index='home'>class="active"</#if>><a href="/reward/index.vpage">首页推荐</a></li>-->
                                <#--<li><a href="/reward/product/experience/index.vpage" <#if index='experience'>class="active"</#if>>体验中心</a></li>-->
                                <#--<#if (currentStudentWebGrayFunction.isAvailable("Reward", "Donation"))!false>
                                   //<#--<li <#if index='present'>class="active"</#if>><a href="/reward/product/present/index.vpage">爱心捐赠</a></li>-->
                                <#--</#if>-->
                                <#--<#if (![0, 9]?seq_contains(currentStudentDetail.clazz.schoolId%10))!false>-->
                                    <#--<li class="luck-draw-join" <#if index='lottery'>class="active"</#if>><a href="/campaign/studentlottery.vpage">幸运抽大奖</a></li>-->
                                <#--</#if>-->
                            <#--</#if>-->

                            <#--老师-->
                            <#if currentUserType == ''>
                                <li <#if index='exclusive'>class="active"</#if>><a href="/reward/product/exclusive/index.vpage">一起专属</a></li>
                                <li <#if index='boutique'>class="active"</#if>><a href="/reward/product/boutique/index.vpage">限量精品</a></li>
                                <#--<li <#if index='home'>class="active"</#if>><a href="/reward/index.vpage">首页推荐</a></li>-->
                                <#--<li><a href="/reward/product/experience/index.vpage" <#if index='experience'>class="active"</#if>>体验中心</a></li>-->

                                <#--小学老师-->
                                <#if currentTeacherDetail.isPrimarySchool()!false>
                                    <#--<li <#if index='lottery'>class="active"</#if>><a href="/campaign/teacherlottery.vpage">抽奖中心</a></li>-->
                                    <#if (currentTeacherDetail.schoolAmbassador)?? && currentTeacherDetail.schoolAmbassador>
                                        <li <#if index='ambassador'>class="active"</#if>><a href="/reward/product/ambassador/index.vpage">大使专区</a></li>
                                    </#if>
                                </#if>
                                <#--初中老师-->
                                <#if currentTeacherDetail.isJuniorTeacher()!false>
                                    <#--<li <#if index='lottery'>class="active"</#if>><a href="/campaign/middleteacherlottery.vpage">抽奖中心</a></li>-->
                                </#if>
                            </#if>

                            <#--"未知"类型-->
                            <#if currentUserType == ''>
                                <li <#if index='ambassador'>class="active"</#if>><a href="/reward/product/ambassador/index.vpage">大使专区</a></li>
                            </#if>

                            <li style="float: right;">
                                <@column.userInfo />
                            </li>
                        </ul>
                    </div>
                    <div class="w-clear"></div>
                </div>
            </div>
            <!--内容主题-->
            <div class="w-content">
                <#nested />
            </div>

            <#--<div class="main_bg">
                <#switch columnType>
                    <#case "normal">
                        <div class="main clearfix">
                            <div class="main_left_box">
                                &lt;#&ndash;content start&ndash;&gt;
                                <div id="rewardContentBox"><#nested /></div>
                                &lt;#&ndash;content end&ndash;&gt;
                            </div>
                            <div class="main_right_box">
                                <div class="top_box clearfix">
                                    <@column.userInfo userType=currentUserType />
                                    &lt;#&ndash;基本信息&ndash;&gt;
                                </div>
                            </div>
                        </div>

                        <#break />
                    <#case "empty">
                        <div class="main">
                            <div class="main_product_box">
                            &lt;#&ndash;content start&ndash;&gt;
                                <div id="rewardContentBox_empty"><#nested /></div>
                            &lt;#&ndash;content end&ndash;&gt;
                            </div>
                        </div>
                        <#break />
                </#switch>
            </div>-->

            <!--底部信息-->
            <div class="footer">
                <div class="nav">
                    <ul class="clearfix">
                        <li><a href="/help/aboutus.vpage" target="_blank">关于我们</a></li>
                        <li><a href="/help/uservoice.vpage" target="_blank">用户声音</a></li>
                        <li><a href="/help/jobs.vpage" target="_blank">诚聘英才</a></li>
                        <li><a href="/help/privacyprotection.vpage" target="_blank">隐私保护</a></li>
                        <#if currentUserType == "STUDENT">
                            <li><a href="/help/parentsguidelines.vpage" target="_blank">家长须知</a></li>
                            <li><a href="/help/childrenhealthonline.vpage" target="_blank">儿童健康上网</a></li>
                            <li><a href="/help/kf/index.vpage?menu=student" target="_blank">帮助中心</a></li>
                            <li><a href="/help/serviceagreement.vpage?agreement=0" target="_blank">用户协议</a></li>
                        <#else>
                            <li><a href="/help/news/index.vpage" target="_blank">新闻中心</a></li>
                            <li><a href="/project/educationsubject/index.vpage" target="_blank">教育部课题</a></li>
                            <li><a href="/help/serviceagreement.vpage?agreement=0" target="_blank">用户协议</a></li>
                            <#--<li><a href="/help/kf/index.vpage?menu=teacher" target="_blank">帮助中心</a></li>-->
                        </#if>
                        <li><a href="javascript:;" class="js-commentsButton">我要评论</a></li>
                        <li><a href="javascript:;" class="js-reportButton">我要举报</a></li>
                    </ul>
                </div>
                <div class="copyright">
                    ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
                </div>
            </div>
            <#include "../../common/to_comments_report.ftl" >
            <script type="text/javascript">
                var roleTypes;
                    <#if currentUserType == 'STUDENT'>
                    roleTypes = "web_student_logs";
                    <#elseif currentUserType == 'TEACHER'>
                    roleTypes = "web_teacher_logs";
                    </#if>
                //更新我的奖品数
                function updateMyRewardCount(updateType){
                    var cBox = $(".my_order_count");
                    if(updateType == 'plus'){
                        cBox.text(cBox.text()*1 + 1);
                    }else{
                        cBox.text(cBox.text()*1 - 1);
                    }
                }

                $(function(){
                    if(${(stuforbidden!false)?string}){
                        $.prompt("<div style='text-align: center; padding: 30px 0;'>账号异常，暂时无法使用</div>", {
                            title : "账号异常",
                            buttons : {'退出登录': true},
                            classes : {
                                close: 'w-hide'
                            },
                            submit: function(){
                                YQ.voxLogs({
                                    database:roleTypes,
                                    module : "studentForbidden",
                                    op : "popup-logout"
                                }, 'student');
                                location.href = "/ucenter/logout.vpage";
                            },
                            loaded : function(){
                                YQ.voxLogs({
                                    database:roleTypes,
                                    module : "studentForbidden",
                                    op : "popup-load"
                                }, 'student');
                            }
                        });
                        return false;
                    }

                    //获取我的奖品数
                    $.post('/reward/getordercount.vpage',{},function(data){
                        $(".my_order_count").html(data.orderCount);
                    });
                    var tagTypeModel = "${tagType!'normal'}";
                    var tagTypeOP = "o_tMUs4LwO";

                    if(tagTypeModel == 'exclusive'){tagTypeOP = "o_uvTlUZ8o";}

                    if(tagTypeModel != 'normal'){
                        YQ.voxLogs({ database:roleTypes,module : "m_2ekTvaNe", op : tagTypeOP, s0: tagTypeModel, s1: "${(currentUser.userType)!0}"});
                    }
                });
            </script>
            <@sugar.site_traffic_analyzer_end />
        </body>
    </html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>
</#macro>