<#if (currentUser.userType) == 3>
    <#assign userType="STUDENT"/>
<#elseif (currentUser.userType) == 1>
    <#assign userType="TEACHER"/>
<#elseif (currentUser.userType) == 8>
    <#assign userType="RSTAFF"/>
</#if>
<#import "../../layout/layout.ftl" as temp />
<@temp.page index='ambassador' columnType="normal">
<div class="w-content">
    <div class="m-side-contentBox">
        <!--全部奖品分类-->
        <div class="w-slide">
            <#include "../leftmenu.ftl"/>
            <@temp.column.leftMessageInfo />
        </div>
        <div class="w-information">
        <#if userType == 'TEACHER'>
                <div class="w-public-info" style="margin-bottom: 0;"><a href="/ambassador/center.vpage" target="_blank" class="w-btn-info w-orange"><i class="w-gold-icon w-gold-icon-7"></i><span>${(ambassadorLevel.level.description)!'不是大使？去看看'}</span><i class="w-arrow w-miarrow"></i></a></div>
        </#if>
        <!--筛选信息-->
            <div class="t-classify-nav-box">
                <div class="cla-nav cla-small-label">
                    <ul id="smallLevelTagsBox">
                    </ul>
                </div>
                <div class="cla-check" id="canExchangeFlagBut">
                    <span class="w-orange">筛选：</span>
                    <#--<#if userType == 'TEACHER'>
                        <span class="ck" data-type="teacherLevelFlag"><i class="w-check"></i>当前等级可兑换</span>
                        <span class="ck" data-type="nextLevelFlag"><i class="w-check"></i>下一等级可兑换</span>
                    </#if>-->
                    <span class="ck" data-type="canExchangeFlag"><i class="w-check"></i>当前可兑换</span>
                </div>

                <div class="cla-nav">
                    <div id="twoLevelTagsBox" style="display: none;">
                        <a data-two_id="0" href="javascript:void(0);" class="all_sort active"><i class="J_sprites"></i><span>全部</span></a>
                    </div>
                    <ul id="threeLevelTagsBox">
                    <#--<li class="mun">排序</li>-->
                        <li class="mun  dis" style="cursor: default;">排序：</li>
                        <li class="mun sequence active" data-three_id="" >默认</li>
                        <li class="mun sequence " data-three_id="soldQuantity">销量<i class="w-arrow w-arrow-orange-down"></i></li>
                        <li class="mun sequence"  data-three_id="price">价格<i class="w-arrow w-arrow-orange-down"></i></li>
                        <li class="mun sequence" data-three_id="wishQuantity">新品<i class="w-arrow w-arrow-orange-down"></i></li>
            <#--<#if userType == 'TEACHER'><li class="mun" data-three_id="ambassadorLevel">等级<i class="w-arrow w-arrow-orange-down"></i></li></#if>-->
                        <li class="w-clear"></li>
                    </ul>
                </div>
            </div>
            <div class="w-clear"></div>
            <!--商品列表-->
            <div class="t-wishList-box t-wishListSmall-box">
                <ul id="product_detail_list_box"></ul>
            </div>
            <!--翻页-->
            <div class="products_page_box">
                <div class="message_page_list">
                <#--分页-->
                </div>
            </div>
        </div>
        <div class="w-clear"></div>
    </div>
</div>

    <#if temp.currentUserType == 'TEACHER'>
        <#if currentUser.fetchCertificationState() != "SUCCESS">
        <script type="text/javascript">
            $(function(){
                if(!$17.getCookieWithDefault("AUTHPP")){
                    $17.setCookieOneDay("AUTHPP", "1", 1);
                    $.prompt("<p style='font-size: 14px; padding: 20px 0;'>${(currentUser.profile.realname)!}老师，您的<@ftlmacro.garyBeansText/>数量：${(currentTeacherDetail.userIntegral.usable)!}。</p><h3>通过认证，可以使用园丁豆兑换奖品哦！</h3>", {
                        title : "系统提示",
                        focus : 0,
                        buttons : {"去认证" : true, "知道了" : false},
                        position :{},
                        submit : function(e, v){
                            if(v){
                                location.href = "${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage";
                            }
                        }
                    });
                }
            });
        </script>
        </#if>
    </#if>
<script id="t:商品详情" type="text/html">
    <%if(detailData !=undefined && detailData.length > 0){%>
        <%var productUrl%>
        <%for(var i = 0; i < detailData.length; i++){%>
            <#--库存为空时不显示-->
            <%  var num=0;
                for(var j = 0; j < detailData[i].skus.length; j++){
                num+=detailData[i].skus[j].inventorySellable;
                }
                if(num){
            %>
                <#--根据产品类型 JPZX_SHIWU("实物"),JPZX_TIYAN("体验") 进入相应的详情页面-->
                <%if(detailData[i].productType == 'JPZX_SHIWU'){%>
                    <%productUrl='/reward/product/detail.vpage?productId='+detailData[i].id+'&tagType=' + tagType%>
                <%}else{%>
                    <%productUrl='/reward/product/experience/detail.vpage?productId='+detailData[i].id+'&tagType=' + tagType%>
                <%}%>
                <li>
                    <a class="wl-list" href="<%=productUrl%>"  target="_blank">
                        <div class="ws-img">
                            <%if(detailData[i].image){%>
                                <%if(detailData[i].image.indexOf('oss-image.17zuoye.com') > -1){%>
                                    <img src="<%=detailData[i].image%>?x-oss-process=image/resize,w_200/quality,Q_90" />
                                    <%}else{%>
                                    <img src="<@app.avatar href="<%=detailData[i].image%>?x-oss-process=image/resize,w_200/quality,Q_90"/>" />
                                <%}%>
                            <%}else{%>
                                <img src="<@app.avatar href="<%=detailData[i].image%>"/>" />
                            <%}%>
                        </div>
                        <div class="ws-info">
                            <#if userType != "TEACHER"><span class="w-vip-orange">vip</span></#if>
                            <#if userType != "STUDENT">
                                <%if(detailData[i].ambassadorLevel > 0){%>
                                <span class="level"><%=(detailData[i].ambassadorLevelName ? detailData[i].ambassadorLevelName : '实习大使')%></span>
                                <%}else{%>
                                <#--<span class="level">LV <%=(detailData[i].teacherLevel ? detailData[i].teacherLevel : 0)%></span>-->
                                <%}%>
                            </#if>
                            <p><span class="gold"><%=detailData[i].discountPrice%></span><%=detailData[i].unit%></p>
                        </div>
                        <div class="ws-txt" title="<%=detailData[i].productName%>"><%=detailData[i].productName%></div>
                    </a>
                </li>
            <%}%>
        <%}%>
    <%}else{%>
    <div class="search_no_prize">
        <div class="no_prize_bg"></div>
        <#--在37个城市之中的学生，注意userType="STUDENT"可删除，否则老师账号登录时会报错-->
        <#if userType="STUDENT" && ((currentStudentWebGrayFunction.isAvailable("Reward", "OfflineShiWu",true))!false)>
            <p class="btn_box font_twenty J_deep_gray">电脑端奖品已下线</p>
            <p class="btn_box J_light_gray" style="margin-top: 10px;">更多奖品可以去手机APP查看~</p>
        <#else>
            <p class="btn_box font_twenty J_deep_gray">没有找到你想要的奖品</p>
            <p class="btn_box J_light_gray" style="margin-top: 10px;">适当放宽条件再试试吧</p>
        </#if>
    </div>
    <%}%>
</script>
</@temp.page>