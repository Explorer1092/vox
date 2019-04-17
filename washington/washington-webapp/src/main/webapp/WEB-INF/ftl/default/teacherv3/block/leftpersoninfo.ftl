<div class="m-person-infoTwo">
    <#if (showLevelUpTip)!false>
        <div class="lv-keyInfo" style="position: relative; z-index: 2;">
            <div class="k-inner v-clickUpgradeInfo PNG_24"></div>
            <script type="text/javascript">
                $(function(){
                    $(document).on("click", ".v-clickUpgradeInfo", function(){
                        $.get("/teacher/recordlevelup.vpage", {}, function(){location.href = "/reward/index.vpage";});
                    });
                });
            </script>
        </div>
    </#if>
    <dl>
        <dt class="parson-avatar">
            <a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage?ref=newIndex" target="_blank"><img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>" width="80" height="80"/></a>
        </dt>
        <dd class="parson-info">
            <div class="name" style="padding-top: 10px;">
                <#if (currentUser.profile.realname)?? && currentUser.profile.realname != "">
                    ${(currentUser.profile.realname)!}
                <#else>
                    <a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myprofile.vpage?ref=newIndex" class="w-red">设置姓名</a>
                </#if>
            </div>
            <div style="height: 3px; overflow: hidden;"></div>
            <div class="privilege">
                <#if currentUser.fetchCertificationState() == "SUCCESS" >
                    <a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage?ref=newIndex" target="_blank" class="auth"><span class="w-icon-public w-icon-new-authVip" title="已认证">已认证</span></a>
                <#else>
                    <a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage?ref=newIndex" class="auth" title="未认证"><span class="w-icon-public w-icon-new-authVip-dis">未认证</span></a>
                </#if>
                <#if ((currentTeacherDetail.subject == "CHINESE" && currentTeacherDetail.schoolAmbassador) || currentTeacherDetail.subject != "CHINESE")!false>
                <span class="w-icon-public <#if (currentTeacherDetail.schoolAmbassador)!false>w-icon-new-authAmb<#else>w-icon-new-authAmb-dis</#if>" title="校园大使">校园大使</span>
                </#if>
                <a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/mygold.vpage?ref=newIndex" style="display: inline-block;"><span class="w-icon-public w-icon-gold PNG_24"></span>${(currentTeacherDetail.userIntegral.usable)!0}
                    <i class="w-orange-tips PNG_24 w-ft-well w-ag-center">园丁豆</i>
                </a>
            </div>
        </dd>
    </dl>
</div>
<!--主菜单-->
<#include "leftmenu.ftl" />
<!--m-synthesize-->
<div class="m-synthesize">

    <#--// 限制广州市-->
    <div id="TeacherAgentLeftBox"></div>
    <script type="text/html" id="T:TeacherAgentLeftBox">
        <div class="m-side-news">
            <dl style="padding: 10px; color: #fff;">
                <dd style="text-align: center; padding-bottom: 10px;">一起教育科技为您服务</dd>
                <%if(item.avatar){%>
                <dt class="parson-avatar" style="text-align: center;  ">
                    <img src="<%=item.avatar%>" width="80" height="80" style="border-radius: 100%;"/>
                </dt>
                <%}%>
                <dd class="parson-info" style="text-align: center;">
                    <div style="padding: 3px 0;"><%=item.userName%></div>
                    <div style="padding: 3px 0;"><%=item.mobile%></div>
                </dd>
            </dl>
        </div>
    </script>
    <script type="text/javascript">
        $(function(){
            $.get('/teacher/teacheragent.vpage', function(data){
                if(data.success && data.agentList){
                    if(data.agentList.mobile){
                        $("#TeacherAgentLeftBox").html( template("T:TeacherAgentLeftBox", {item: data.agentList}) );
                    }
                }
            });
        });
    </script>

    <#--广告-->
    <div class="m-side-width m-switch-banner">
    <#include "switchbanner.ftl" />
    </div>

    <#--客服-->
    <div class="sever-info m-side-width">
        <div class="w-btn w-btn-gray w-btn-block">
            <p class="tel">
                <span><@ftlmacro.hotline phoneType="teacher"/></span>
                <span class="text">客服时间：8:00-22:00</span>
            </p>
            <div class="s-help">
                <a href="javascript:void(0);" class="w-btn w-btn-small on-line w-btn-green message_right_sidebar">
                    <span class="w-icon w-icon-white w-icon-25"></span>
                    <span class="w-icon-md">反馈建议</span>
                </a>
                <a href="/help/kf/index.vpage?menu=teacher" class="w-btn w-btn-small on-line" target="_blank">
                    <span class="w-icon w-icon-white w-icon-15"></span>
                    <span class="w-icon-md">帮助中心</span>
                </a>
            </div>
        <#--<a id="tqchat1" href="javascript:void(0);" class="w-btn w-btn-small on-line v-hotline">
            <span class="w-icon w-icon-white w-icon-15"></span>
            <span class="w-icon-md">在线咨询</span>
        </a>
        <script language="javascript" src=http://float2006.tq.cn/floatcard?adminid=9558911&sort=1 ></script>
        <script language="javascript">
            TQ_RQF = 6;
            TQ_RQC = 2;
            if("undefined" != typeof TQKF && "undefined" != typeof TQKF.Binding){
                TQKF.Binding("tqchat1","acd","","1");
            }
        </script>-->
        </div>
    </div>
</div>
<#-- 2017-04-27 校园大使下线前提示 -->
<#--<#if (currentTeacherDetail.subject != "CHINESE" )!false>-->
<#--<div class="m-side-amb">-->
    <#--<#if (currentUser.fetchCertificationState())?? && currentUser.fetchCertificationState() == "SUCCESS">&lt;#&ndash;非认证老师不在看到校园大使&ndash;&gt;-->
        <#--<#if (data.schoolAmbassadorExist)?has_content>-->
            <#--<#if (currentTeacherDetail.schoolAmbassador)?? && currentTeacherDetail.schoolAmbassador && false>-->
                <#--<a href="javascript:void(0);" id="serviceCallMe" title="大使特权，如遇问题可获得1对1客服回电">专属客服：点击为您回电</a>-->
                <#--<script type="text/javascript">-->
                    <#--$(function(){-->
                        <#--var serviceCallMeCount = 0;-->
                        <#--var serviceCallMe = $("#serviceCallMe");-->

                        <#--serviceCallMe.on("click", function(){-->
                            <#--$.prompt(template("t:校园大使一对一客服", {}), {-->
                                <#--title: "30分钟内为您回电，请保持手机畅通",-->
                                <#--buttons : { "取消" :  false, "确定" : true},-->
                                <#--focus: 1,-->
                                <#--submit : function(e, v){-->
                                    <#--if(v){-->
                                        <#--$.post("/teacher/callambassador.vpage", {content: $("#v-content-me").val()} ,function(data){-->
                                            <#--if(data.success){-->
                                                <#--$17.alert("&nbsp;&nbsp;&nbsp;&nbsp;您的申请已成功提交，我们的专线客服将在20分钟内与您联系，请保持手机畅通。如遇紧急问题可直接拨打校园大使客服专线4009989696转8000。客服工作时间：<span class='text_red'>早9：00--晚21：00</span>");-->
                                                <#--serviceCallMeCount++;-->
                                            <#--}else{-->
                                                <#--$17.alert(data.info);-->
                                            <#--}-->
                                        <#--});-->
                                    <#--}-->
                                <#--}-->
                            <#--});-->
                        <#--});-->
                    <#--});-->
                <#--</script>-->
            <#--<#else>-->
                <#--<#if (data.schoolAmbassadorName)??>-->
                    <#--&lt;#&ndash;<a href="javascript:void(0);" title="本校校园大使是 ${(data.schoolAmbassadorName)!}老师" style="cursor: default;">本校校园大使是 ${(data.schoolAmbassadorName)!}老师</a>&ndash;&gt;-->
                <#--<#else>-->
                    <#--&lt;#&ndash;fix #35543&ndash;&gt;-->
                    <#--&lt;#&ndash;<a href="/ambassador/schoolambassador.vpage?ref=newIndex" target="_blank" class="q-blue">申请校园大使</a>&ndash;&gt;-->
                <#--</#if>-->
            <#--</#if>-->
        <#--</#if>-->
    <#--</#if>-->
<#--</div>-->
<#--</#if>-->

<script type="text/html" id="t:校园大使一对一客服">
    <div class="w-form-table" style="padding: 0;">
        <dl>
            <dt style="width:90px;">问题描述：</dt>
            <dd style="margin-left:90px;">
                <textarea style="width: 300px; height: 100px;" class="w-int" id="v-content-me"></textarea>
            </dd>
        </dl>
    </div>
</script>