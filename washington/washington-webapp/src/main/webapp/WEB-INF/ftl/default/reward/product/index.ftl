<#-- @ftlvariable name="twoLevelTags" type="com.voxlearning.utopia.service.reward.entity.RewardTag" -->
<#-- @ftlvariable name="categories" type="com.voxlearning.utopia.service.reward.entity.RewardCategory" -->
<#--根据角色获取相应积分-->
<#if (currentUser.userType) == 3>
    <#assign userType="STUDENT"/>
<#elseif (currentUser.userType) == 1>
    <#assign userType="TEACHER"/>
<#elseif (currentUser.userType) == 8>
    <#assign userType="RSTAFF"/>
</#if>
<#import "../layout/layout.ftl" as temp />
<@temp.page index='${tagType}' columnType="normal">
    <#--37个城市灰度的学生导航栏只保留"幸运抽大奖"，由于其他导航的链接较多，故此处使用简单方法，如果是跳转到其他导航链接的全部重定向到"幸运抽大奖"-->
    <#if (currentUser.userType) == 3 && ((currentStudentWebGrayFunction.isAvailable("Reward", "OfflineShiWu", true))!false)>
    <script>
        window.location.replace('/project/common/emptytip.vpage?type=1'); // 抽奖页面下线后，重定向到空页面提示

        // var localHref = window.location.href;
        // // 如果是导航：一起作业专属、限量精品、爱心捐赠等
        // if (localHref.indexOf('/reward/product/exclusive/index.vpage') > -1 ||
        //     localHref.indexOf('/reward/product/boutique/index.vpage') > -1 ||
        //     localHref.indexOf('/reward/product/present/index.vpage')) {
        //     window.location.replace('/campaign/studentlottery.vpage');
        // }
    </script>
    </#if>

    <#--老师初始化操作-->
    <#if (userType == "TEACHER")>
    <script>
        if (location.href.indexOf("exclusive/index.vpage") > -1){
            var tipShowFlag = +"${tipShowFlag!0}";
            if (tipShowFlag === 1 || tipShowFlag === 3) {
                showInitAlert1();
            } else if (tipShowFlag === 2) {
                showInitAlert2();
            }
        }
        function showInitAlert1() {
            var insufficientAmount = '500园丁豆';
            var spendAmount = '200园丁豆';
                <#if (currentTeacherDetail.isJuniorTeacher())!false>
                    insufficientAmount = '5000学豆';
                    spendAmount = '2000学豆';
                </#if>
            var alertHtml = "<div style='padding: 10px 20px; text-align: left;'>亲爱的老师，即日起奖品中心将实行阶梯包邮制度：如当月（寒暑假期除外）累计兑换实物奖品不足" + insufficientAmount + "，需额外使用" + spendAmount + "兑换包邮服务一次（下月发货时自动扣除，余额不足" + spendAmount + "，则全部扣除）；如累计实物奖品超过" + insufficientAmount + "，则自动包邮。</div>"
            $.prompt(alertHtml, {
                title : "系统提示",
                focus : 0,
                buttons : {"知道了" : true},
                position :{width: 600},
                submit: function (e, v) {
                    if (v) {
                        e.preventDefault();
                        $.prompt.close();

                        if (tipShowFlag === 3) { // 对于3，展示第二个弹窗
                            setTimeout(function () {
                                showInitAlert2();
                            }, 500);
                        }
                    }
                }
            });
        }
        function showInitAlert2() {
            var alertHtml = "<div style='padding: 10px 20px; text-align: left;'>亲爱的老师，系统检测到您所带的班级中有毕业班学生，为了学生们毕业前能兑换到心仪奖品，辛苦您通知毕业班同学在5月31日前进行本学期最后一次实物兑换，毕业生虚拟兑换不受影响，教师奖品也不受此影响哦~~感谢您的辛勤付出~~</div>"
            $.prompt(alertHtml, {
                title : "系统提示",
                focus : 0,
                buttons : {"知道了" : true},
                position :{width: 600}
            });
        }
    </script>
    </#if>

    <#--学生初始化操作-->
    <#if (userType == "STUDENT")>
    <script>
        if (location.href.indexOf("exclusive/index.vpage") > -1){
            var tipShowFlag = +"${tipShowFlag!0}";
            if (tipShowFlag === 2) {
                var alertHtml = "<div style='padding: 10px 20px; text-align: left;'>亲爱的同学，祝贺你即将毕业，为避免毕业离校无法收到奖品，建议你在5月31日前进行本学期最后一次实物兑换，6月起毕业生仍能兑换虚拟奖品哦~~</div>"
                $.prompt(alertHtml, {
                    title: "系统提示",
                    focus: 0,
                    buttons: {"知道了": true},
                    position: {width: 600}
                });
            }
        }
    </script>
    </#if>

    <div class="w-content">
        <div class="m-side-contentBox">
            <!--全部奖品分类-->
            <div class="w-slide">
                <#include "leftmenu.ftl"/>
                <@temp.column.leftMessageInfo />
            </div>
            <div class="w-information">
                <#if (userType == "TEACHER" && taskInfo?? && currentTeacherDetail.isPrimarySchool())!false>
                <#--新手任务-->
                    <div class="new_test">
                        <p class="new_test_p">新手任务  免费领大礼</p>
                        <div class="new_con_l">
                            <#assign headFlag = (taskInfo.addressFlag && taskInfo.wechatFlag && taskInfo.bbsFlag)!false/>
                            <div class="price-icon-0"></div>
                            <a class="price_task1 <#if !(headFlag)>price_task2</#if> v-receiveRewardBtn" style="display: block;" href="javascript:void(0)">领取新手奖励</a>
                            <script type="text/javascript">
                                $(function(){
                                    $(document).on("click", ".v-receiveRewardBtn", function(){
                                        var $this = $(this);

                                        if( $this.hasClass("price_task2") ){
                                            return false;
                                        }
                                        $this.addClass("price_task2");
                                        $.post("/reward/getreward.vpage", {
                                            taskType : "NEW_HAND_TASK",
                                            rewardName : "GOLD"
                                        }, function(data){
                                            if(data.success){
                                                $17.alert("领取成功");
                                                $(".v-receiveRewardBtn").addClass("w-but-disabled");
                                            }else{
                                                $17.alert(data.info);
                                            }
                                            $this.removeClass("price_task2");
                                        });
                                    });

                                    $(document).on("click", ".click-binding-weixin", function(){
                                        $17.getQRCodeImgUrl({
                                            role : "teacher"
                                        }, function(url){
                                            $.prompt('<div style="text-align: center;"><div style="color: #f00;">请扫描下方二维码绑定微信！可得双倍话费奖励！</div><img style="width: 200px; height: 200px;" src='+url+' alt="二维码"></div>',{
                                                title : "绑定微信",
                                                buttons : {"完成": true}
                                            });
                                        });
                                    });
                                });
                            </script>
                        </div>
                        <div class="price_content1_right">
                            <ul>
                                <li>
                                    <h5>第1步 填写资料</h5>
                                    <div class="price-icon-1"></div>
                                    <#if (taskInfo.addressFlag)!false>
                                        <a href="javascript:void(0)" class="price_task2">已完成</a>
                                    <#else>
                                        <a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myprofile.vpage" class="price_task1">去做任务</a>
                                    </#if>
                                    <em></em>
                                </li>
                                <li>
                                    <h5>第2步 绑定微信</h5>
                                    <div class="price-icon-2"></div>
                                    <#if (taskInfo.wechatFlag)!false>
                                        <a href="javascript:void(0)" class="price_task2">已完成</a>
                                    <#else>
                                        <a href="javascript:void(0);" class="click-binding-weixin price_task1">去做任务</a>
                                    </#if>
                                    <em></em>
                                </li>
                                <li>
                                    <h5>第3步 论坛报道</h5>
                                    <div class="price-icon-3"></div>
                                    <#if (taskInfo.bbsFlag)!false>
                                        <a href="javascript:void(0)" class="price_task2">已完成</a>
                                    <#else>
                                        <a href="http://www.17huayuan.com/forum.php?mod=viewthread&tid=29879" target="_blank" class="price_task1">去做任务</a>
                                    </#if>
                                </li>
                            </ul>
                        </div>
                    </div>
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
                        <div id="twoLevelTagsBox" style="display: none">
                            <a data-two_id="0" href="javascript:void(0);" class="all_sort active"><i class="J_sprites"></i><span>全部</span></a>
                        </div>
                        <ul id="threeLevelTagsBox">
                        <#--<li class="mun">排序</li>-->
                            <li class="mun sequence dis" readonly style="cursor: default;">排序：</li>
                            <li class="mun sequence active" data-three_id="" >默认</li>
                            <li class="mun sequence js-desc" data-three_id="soldQuantity">销量<i class="w-arrow w-arrow-orange-down"></i></li>
                            <li class="mun sequence"  data-three_id="price">价格<i class="w-arrow w-arrow-orange-down"></i></li>
                            <li class="mun sequence" data-three_id="wishQuantity">新品<i class="w-arrow w-arrow-orange-down"></i></li>
                            <#--<#if userType == 'TEACHER'><li class="mun" data-three_id="teacherLevel">等级<i class="w-arrow w-arrow-orange-down"></i></li></#if>-->
                            <li class="w-clear"></li>
                        </ul>
                    </div>
                    <div class="warm-prompt" id="warmPromtText">
                        <p></p>
                    </div>
                </div>
                <script type="text/javascript">

                </script>
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

        <#if userType == 'TEACHER'>
            <#if currentUser.fetchCertificationState() != "SUCCESS">
            <script type="text/javascript">
                $(function(){
                    if(!$17.getCookieWithDefault("AUTHPP")){
                        $17.setCookieOneDay("AUTHPP", "1", 1);
                        $.prompt("<p style='font-size: 14px; padding: 20px 0;'>${(currentUser.profile.realname)!}老师，您的<@ftlmacro.garyBeansText/>数量：${(currentTeacherDetail.userIntegral.usable)!}。</p><h3>通过认证，可以使用<@ftlmacro.garyBeansText/>兑换奖品哦！</h3>", {
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
            <#--<%  var num=0;
                for(var j = 0; j < detailData[i].skus.length; j++){
                num+=detailData[i].skus[j].inventorySellable;
                }
                if(num){
            %>-->
                <#--根据产品类型 JPZX_SHIWU("实物"),JPZX_TIYAN("体验") 进入相应的详情页面-->
                <%if(detailData[i].oneLevelCategoryType == 1){%>
                    <%productUrl='/reward/product/detail.vpage?productId='+detailData[i].id%>
                <%}else{%>
                    <%productUrl='/reward/product/experience/detail.vpage?productId='+detailData[i].id%>
                <%}%>

                <li onclick="prize_click('<%=detailData[i].id%>','<%=detailData[i].productType%>')">

                    <#if userType == "TEACHER" >
                        <%if(detailData[i].tags!='' && detailData[i].tags !='公益'){%>
                            <div class="coupon-tag-gaoj"><%=detailData[i].tags%></div>
                        <%}%>
                    </#if>
                    <#if userType == "STUDENT" >
                        <%if(detailData[i].tags!='' && detailData[i].tags =='公益'){%>
                        <div class="coupon-tag-gaoj">公益</div>
                        <%}%>
                    </#if>
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
                            <#--<#if userType != "TEACHER"><span class="w-vip-orange">vip</span></#if>-->
                            <#if userType != "STUDENT">
                                <%if(detailData[i].ambassadorLevel > 0){%>
                                <span class="level"><%=(detailData[i].ambassadorLevelName ? detailData[i].ambassadorLevelName : '实习大使')%></span>
                                <%}else{%>
                                <#--<span class="level">LV <%=(detailData[i].teacherLevel ? detailData[i].teacherLevel : 0)%></span>-->
                                <%}%>
                            </#if>
                            <p><span class="gold"><%=detailData[i].discountPrice%></span><i class="w-gold-icon w-gold-icon-8"></i></p>
                        </div>
                        <div class="ws-txt" title="<%=detailData[i].productName%>"><%=detailData[i].productName%></div>
                    </a>
                </li>
                <#--<%}%>-->
            <%}%>
        <%}else{%>
        <div class="search_no_prize">
            <div class="no_prize_bg"></div>
            <#--在37个城市之中的是学生，注意userType="STUDENT"可删除，否则老师账号登录时会报错-->
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
<script type="text/javascript">
    var roleType = "teacher";
    var module_log = 'm_2ekTvaNe';
    var periodType = '';
    <#if userType == 'STUDENT'>
        roleType = "student";
        module_log = 'm_wVdGfet6';
    </#if>

    <#if userType == 'STUDENT'>
    periodType = '小学学生';
    <#else>
        <#if (currentTeacherDetail.isJuniorTeacher())!false>
            periodType = '中学老师';
        <#else>
            periodType = '小学老师';
        </#if>
    </#if>
    $17.voxLog({
        module: module_log,
        op: "prize_sy_load",
        s0: periodType
    },roleType);
    function prize_click(id,productType) {
        $17.voxLog({
            module: module_log,
            op:"anyone_prize_click",
            s0:id,
            s1:productType
        },roleType);
    }
</script>
<#if (.now lt "2016-02-21 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss") && userType == "TEACHER")!false>
    <script type="text/html" id="T:imageDesignReward-popup">
        <style>
            .imageDesignReward-box{ background: url(<@app.link href="public/skin/reward/imagesV1/lottery/reward-popup.png?1.0.1"/>) no-repeat 5000px 5000px;}
            .imageDesignReward-box{ width: 493px; height: 403px; position: relative; background-position: 0 0;}
            .imageDesignReward-box .hp-btn{ position: absolute; bottom: 34px; right: 154px; }
            .imageDesignReward-box .hp-btn a{ width: 205px; height: 42px; display: inline-block; background-position: -134px -325px;}
            .imageDesignReward-box .hp-btn a:hover{ background-position: -134px -403px;}
            .imageDesignReward-box .hp-btn a:active{ background-position: -134px -403px;}
            .imageDesignReward-box .hp-close{ position: absolute; top: 0; right: 0;}
            .imageDesignReward-box .hp-close a{ width: 50px; height: 50px; display: inline-block;}
        </style>
        <div class="imageDesignReward-box">
            <div class="hp-btn">
                <a class="imageDesignReward-box v-designRewardBtn" href="https://www.wenjuan.com/s/aUnQJv/" target="_blank"></a>
            </div>
            <div class="hp-close">
                <a href="javascript:$.prompt.close();"></a>
            </div>
        </div>
    </script>
    <script type="text/javascript">
        (function(){
            if(!$17.getCookieWithDefault("idrpp")){
                $17.setCookieOneDay("idrpp", "1", 1);
                $.prompt(template("T:imageDesignReward-popup", {}),{
                    prefix : "null-popup",
                    buttons : { },
                    loaded : function(){
                        $(".v-designRewardBtn").on("click", function(){
                            $17.setCookieOneDay("idrpp", "25", 25);
                        });
                    },
                    classes : {
                        fade: 'jqifade',
                        close: 'w-hide'
                    }
                });
                return false;
            }
        })();
    </script>
</#if>
</@temp.page>