<#assign wechatBindedTemp = (data.wechatBinded)?? && data.wechatBinded/><#--是否绑定微信-->
<#assign appBindedTemp = (data.appBinded)?? && data.appBinded/><#--是否绑定微app-->
<#assign inviteCFlag = ((data.phase lt 2)!false) && ((data.countDown30 > 0)!false) && ((data.tcrshow == "SHOW_OLD")!false) /><#--首页奖励new  data.tcrshow包含合肥过虑-->
<#assign studentSendAcNum = (data.downloadClazzs)?? && data.downloadClazzs?size gt 0/><#--给学生发练习-->
<#assign showTermBeginCard = (data.showTermBeginCard || data.assignFlag)!false/><#--开学大礼包-->
<#assign ambBindWechatSign = ((data.ambassadorWechatBinded)?? && !data.ambassadorWechatBinded && (currentTeacherDetail.schoolAmbassador)?? && currentTeacherDetail.schoolAmbassador)!false/><#--一起教育科技校园大使微信号 双号老师-->
<#assign absFlag = (currentTeacherDetail.subject != "CHINESE" && !((data.schoolAmbassadorName)??))!false><#--校园大使-->
<#assign schoolSectionFlag = (([440303,440304,440305,510104,510105,510106,510107,510108,320402,320404,320411,320412,320482,310101,310104,310105,310106,310107,310108,310109,310110,310112,310115,410102,410103,410104,410105,410302,410303,410305,410311,320506,320507,320508,320509,320581,320582,320583,320585,370102,370103,370104,370105,370112,130102,130104,130105,130108,120101,120102,120103,120104,120105,120106,120114]?seq_index_of(currentTeacherDetail.regionCode) gt -1)!false)><#--中小学邀请灰度-->

<#--//假期logic start-->
<#assign notYuYangFlag = (!currentTeacherWebGrayFunction.isAvailable("PHONEFEE", "NEVERSHOW"))/>
<#assign latelyAuthenticated = true/>
<#if ['1', '2', '7', '8']?seq_contains(.now?string("M"))>
    <#assign latelyAuthenticated = false/>
</#if>
<#--假期logic end//-->

<#--初始化卡片页面-->
<div class="w-sets">
    <div class="w-sets-title">
        <h1>今日任务</h1>
        <div class="w-sets-title-side">
        <#--null-->
        </div>
    </div>
    <#--w-opt-back-content-->
    <div class="w-sets-container" id="step-showTips">
        <div class="cl-step-showTips-startText PNG_24"></div>
        <div class="cl-step-showTips-start"><a href="javascript:void(0);" data-title="知道了" class="btn PNG_24"></a></div>
        <ul class="t-teacher-home-task" id="homeworkItemContainer"></ul>
    </div>
</div>
<script type="text/javascript">
    $(function() {
        //打点
        $(document).on("click",".v-goToSetClazz",function(){
            YQ.voxLogs({
                database : "web_teacher_logs",
                module: 'm_rQjVWe1G',
                op : "o_1576PMPn"
            });
        });
        $(document).on("click",".v-goToHomework",function(){
            YQ.voxLogs({
                database : "web_teacher_logs",
                module: 'm_rQjVWe1G',
                op : "o_SbgPMsJJ"
            });
        });

        var homeworkItemContainer = $("#homeworkItemContainer");
        //练习

        var assignHomeworkList = ${data.assignHomeworkList!'[]'};
        var checkHomeworkList = ${data.checkHomeworkList!'[]'};
        var ongingHomeworkList = ${data.ongingHomeworkList!'[]'};



        var _tempAuthFlag = ${((currentUser.fetchCertificationState() == "SUCCESS")!false)?string};//认证老师为true : false

        //卡片显示方式
        var cUserId = ${(currentUser.id)!0};
        var cardLogicItem = {
            homeworkList : {
                title : "基础练习",
                sTipsFlag : ($17.getQuery("step") == "showtip" && $17.getQuery("index") == 1),
                showFlag : true
            },
            teachingStudentsUse : {
                title : "教学生使用",
                showFlag : ${studentSendAcNum?string},
                abTest : (/^\d*[0]$/.test(cUserId) || /^\d*[9]$/.test(cUserId))
            },
            <#if (.now gt "2017-04-13 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss") && .now lt "2017-05-31 23:59:59"?datetime("yyyy-MM-dd HH:mm:ss"))!false>
            scholarship:{
                title: "17奖学金",
                showFlag: ${((data.showScholarship)!false)?string}
            },
            </#if>
            inviteReward : {
                title : "首页奖励",
                showFlag : _tempAuthFlag && ${(inviteCFlag!false)?string}
            }
        };

        <#if (.now gt "2017-01-22 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss") && .now lt "2017-03-10 23:59:59"?datetime("yyyy-MM-dd HH:mm:ss"))!false>
            cardLogicItem["termbeginActivity"] = {title: "开学大礼包", showFlag: (${(showTermBeginCard!false)?string}) };
        </#if>

        <#--首页卡片显示时间1月9 00:00--2月28 23:59 -->
        <#if ((.now gt "2017-01-09 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss") && .now lt "2017-02-28 23:59:59"?datetime("yyyy-MM-dd HH:mm:ss")) || ftlmacro.devTestSwitch)!false>
            cardLogicItem["vacationCard"] = {title : "寒假练习", showFlag : ${((currentTeacherDetail.subject != "CHINESE")!false)?string}};
        </#if>

        //认证 ：true - false
        if(_tempAuthFlag){
            cardLogicItem["authPrivilege"] = {title : "认证特权", showFlag : (${((data.privilege)!false)?string}) };
            cardLogicItem["inviteTeacherUpdate"] = {title: "邀请老师升级啦", showFlag: false };//邀请老师升级
            <#--cardLogicItem["inviteTeacher"] = {title : "邀请老师", showFlag : ${((currentTeacherDetail.subject != "CHINESE" && notYuYangFlag)!false)?string} };//邀请送话费-->
            cardLogicItem["leaderCard"] = {title : "任命小组长", showFlag : false};//下线
            cardLogicItem["ambassadorWelfare"] = {title : "大使福利", showFlag : false };//下线
            cardLogicItem["campusAmbassador"] = {title : "首页校园大使", showFlag : false };//下线
            cardLogicItem["teacherAwaken"] = {title : "老师唤醒", showFlag : (${((data.showActivateCard)!false)?string})};
            cardLogicItem["experienceDivision"] = {title : "首页体验师", showFlag : true};
        }else{
            cardLogicItem["inviteReward"] = {title : "首页奖励", showFlag : false };
            cardLogicItem["bindingWeChat"] = {title: "绑定微信", showFlag: !(${wechatBindedTemp?string})};
            cardLogicItem["inviteTeacherUpdate"] = {title: "邀请老师升级啦", showFlag: false };//邀请老师升级
            cardLogicItem["authenticate"] = {title : "认证身份", showFlag : false};
            <#--cardLogicItem["inviteTeacher"] = {title : "邀请老师", showFlag : ${((notYuYangFlag)!false)?string} };//邀请送话费-->
            cardLogicItem["leaderCard"] = {title : "任命小组长", showFlag : false};//下线
            cardLogicItem["noContent"] = {title : "没有更多任务了", showFlag : true};
        }

        /*卡片广告位*/
        YQ.voxSpread({
            keyId : 110101
        }, function(result){
            console.log(result);
            if(result.success && result.data.length > 0){
                var popupItems = result.data;
                if(popupItems.length>0){
                    result.showFlag = true;
                    cardLogicItem["crmSetCard"] = result;
                }
            }
        });

        setTimeout(function(){
            //加载作业卡片
            homeworkItemContainer.html(template("T:首页所有作业卡片", {
                assignHomeworkList: assignHomeworkList,
                checkHomeworkList: checkHomeworkList,
                ongingHomeworkList: ongingHomeworkList,
                cardLogicItem : cardLogicItem,
                authFlag : _tempAuthFlag,
                subject  : $uper.subject.key
            }));
        }, 50);


        //帮助微信
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

        //click-ambBindingWeiXin
        $(document).on("click", ".click-ambBindingWeiXin", function(){
            $.prompt("<p style='font-size: 18px; margin-bottom: 15px; text-align: center;'>扫描二维码绑定校园大使微信号</p><p style='border: 1px solid #ddd; width: 144px; height: 144px; margin: 0 auto;'><img src='//cdn.17zuoye.com/static/project/app/publiccode_teacherAcademy.jpg'/><p>", {
                title: "系统提示",
                buttons: { "知道了": true },
                position: {width: 500}
            });
        });

        //不再显示任命小组长卡片
        $(document).on("click", ".v-noShowTinyGroupCard", function(){
            $.post("/teacher/forbid30d.vpage", {}, function(data){
                if(data.success){
                    location.reload();
                }else{
                    $17.alert(data.info)
                }
            });
        });

        //切换学号或手机号
        $(document).on("click", ".v-clickSwitchAccountOrMobile", function(){
            var $thisType = $(this).attr("data-account");

            function switchAccount(type){
                $.post("/teacher/mobileoraccount.vpage", {
                    method : type
                }, function(data){
                    if(data.success){
                        //成功
                        location.reload();
                    }else{
                        $17.alert(data.info);
                    }
                });
            }

            if($thisType == "MOBILE"){
                switchAccount($thisType);
                return false;
            }

            var _html = "<div style=' line-height: 150%; margin: 0 40px;'><p style='text-align: center; color: #fa7252; margin-bottom: 10px;'>您的一起老师ID为：${(currentUser.id)!}</p>"
                    + "<p>如果您不方便把手机号公布给学生，您可以把您的一起老师ID告诉学生，学生可以通过您的ID加入班级完成练习。</p></div>";

            $.prompt(_html, {
                focus : 1,
                title: "系统提示",
                buttons: {"取消" : false, "切换ID": true },
                position: {width: 500},
                submit : function(e, v){
                    if(v){
                        switchAccount($thisType);
                    }
                }
            });
        });

        $(document).on("click", ".v-goToSetClazz", function(){
            $.get("/activity/recordadjust.vpage", {}, function(data){
                $17.tongji("新学生调整班级", "去调整");
                location.href = "${(ProductConfig.getUcenterUrl())!''}/teacher/systemclazz/clazzindex.vpage?ref=editClazz&refType=home";
            });
        });
        $(document).on("click", ".v-goToHomework", function(){
            <#if data.adjustFlag!false>
                location.href = "/teacher/homework/batchassignhomework.vpage?ref=termbeginCard";
            <#else>
                $.prompt("<div class='w-ag-center' style='font-size: 20px;color: #4e5656;font-weight:bold;'>春季开学任教班级是否有变动？<div style='color: #4e5656; padding: 15px;'>请先<span style='color:#f86638;font-size:24px;'>确认任教班级！</span></div></div>", {
                    focus : 1,
                    title: "系统通知",
                    buttons: { "教的班级不变": false , "有变动，去调整": true },
                    position: {width: 500},
                    close : function(){
                        $.get("/activity/recordadjust.vpage", {}, function(data){
                            $.prompt.close();
                            $17.tongji("新学生调整班级", "保持不变");
                            hasAdjust = false;
                        });
                    },
                    classes : {
                        close: 'w-hide'
                    },
                    submit : function(e, v){
                        if(v){
                            YQ.voxLogs({
                                database : "web_teacher_logs",
                                module: 'm_rQjVWe1G',
                                op : "o_IeEkPhy7"
                            });
                            $.get("/activity/recordadjust.vpage", {}, function(data){
                                $.prompt.close();
                                $17.tongji("新学生调整班级", "去调整");
                                location.href = "${(ProductConfig.getUcenterUrl())!''}/teacher/systemclazz/clazzindex.vpage?ref=editClazz&refType=project";
                            });
                        }else{
                            YQ.voxLogs({
                                database : "web_teacher_logs",
                                module: 'm_rQjVWe1G',
                                op : "o_kscVhYUz"
                            });
                            location.href = "/teacher/homework/batchassignhomework.vpage?ref=termbeginCard";
                        }
                    }
                });
            </#if>
        });

        $(document).on("mouseover",".J_loadLinkApp",function(){
            $(this).find(".card-loadCode").show();
            $17.voxLog({
                module : "arrangesummerhomework",
                op : "download_a"
            });
        }).on("mouseout",".J_loadLinkApp",function(){
            $(this).find(".card-loadCode").hide();
        });

        <#if !wechatBindedTemp>
        //获取二维码
        setTimeout(function(){
            if( $(".teacherSendBillCampaignType").length > 0 ){
                var qrCodeUrl = "http://cdn.17zuoye.com/static/project/app/publiccode_teacher.jpg";
                $.get("/teacher/qrcode.vpage?campaignId=23", function(data){
                    if(data.success){
                        qrCodeUrl = data.qrcode_url;
                    }
                    $(".teacherSendBillCampaignType").html("<img src='"+ qrCodeUrl +"' width='130' height='130'/>");
                });
            }
        }, 200);
        </#if>

        //卡片曝光率log发送
        homeworkItemContainer.children("[data-for-log]").each(function(){
            $17.voxLog({
                module : "teacherIndexCardShow",
                op : $(this).attr("data-for-log")+"-show"
            });
        });
        //卡片点击率log发送
        homeworkItemContainer.on("click",".w-btn",function(){
            $17.voxLog({
                module : "teacherIndexCardShow",
                op : $(this).parents("li[data-for-log]").attr("data-for-log")+"-click"
            });
        });

        $(document).on("click",".J_TermEndDetail",function(){
            $17.voxLog({
                module : "m_QpgwghIg",
                op     : "card_final_revision_click"
            });
            setTimeout(function(){
                location.href = "/teacher/termend.vpage";
            },200);
        });
    });
</script>

<script type="text/html" id="T:首页所有作业卡片">
    <%var cardItemRecord = 0%>
    <%for(var i in cardLogicItem){%>
        <%if(cardLogicItem[i].showFlag && cardItemRecord < 3 ){%>
        <%cardItemRecord++%>
        <li class="t-teacher-home-item <%if(cardItemRecord == 1){%>t-teacher-home-item-first<%}%> home-card-<%=i%>"  data-for-log="<%=i%>">
        <#--start-->
            <%if(i == "inviteTeacher"){%>
            <#assign prKey = ((wechatBindedTemp || appBindedTemp)?string('10', '5'))/>
            <div data-title="邀请老师" class="t-treeType-box">
                <h2 class="t-treeType-title">邀请老师</h2>
                <div class="t-treeType-fire" style="background: url(<@app.link href='public/skin/teacherv3/images/publicbanner/invite-teacher-card.png'/>) repeat-x center 0;"></div>
                <div class="t-treeType-item" style="padding: 15px 0 0 15px;">
                    <p style="font-size: 14px; margin-bottom: 10px;">
                        已邀请 <strong class="w-orange">${(data.inviteCount)!0}</strong> 人，最高可得 <strong class="w-orange">${(data.inviteCount * 30)!0}</strong> 元话费
                    </p>
                    <p style="font-size: 12px;">
                        全国已有 <strong class="w-orange">${(data.totalInviteCount)!0}</strong> 名老师参与活动<br/>
                        累计已获得 <strong class="w-orange">${(data.totalFee)!0}</strong> 元话费
                    </p>
                </div>
                <div class="t-treeType-btn">
                    <a href="/teacher/invite/index.vpage?ref=card" class="w-btn w-btn-red">立即邀请</a>
                </div>
            </div>
            <%}%>

            <#--升级认证后认证并且before41使用count3-->
            <%if(i == "inviteReward"){%>
            <#if wechatBindedTemp || appBindedTemp>
                <#assign prKey = 10/>
            <#else>
                <#assign prKey = 5/>
            </#if>
            <div class="cl-step-showTips-start"><a href="javascript:void(0);" class="btn" style="left: -200px;"></a></div>
            <div data-title="首页奖励" class="t-treeType-box">
                <h2 class="t-treeType-title">老师认证话费补贴</h2>
                <div class="sendBillActivity-block">
                <#if wechatBindedTemp || appBindedTemp>
                    <p class="head-title">更多学生认证，更多话费奖励</p>
                <#else>
                    <p class="head-title" style="color: #333;">点此<a href="javascript:void(0);" class="w-blue click-binding-weixin">绑定微信</a>，<span class="w-red">双倍话费</span>奖励！</p>
                </#if>
                    <ul>
                        <li class="<#if (data.phase)?? && data.phase gte 1><#--完成-->complete</#if><#if (data.phase)?? && data.phase == 0><#--当前-->active</#if>">
                            <div class="count">30学生</div>
                            <div class="bill">${prKey}元</div>
                            <div class="info"><#if (data.phase)?? && data.phase gte 1>已达成<#else><#if (data.phase)?? && data.phase == 0>${data.count6!0}个学生<#else>话费奖励</#if></#if></div>
                        </li>
                        <li class="<#if (data.phase gte 2)!false><#--完成-->complete</#if> <#if (data.phase == 1)!false><#--当前-->active</#if>">
                            <div class="count">90学生</div>
                            <div class="bill">${prKey*2}元</div>
                            <div class="info"><#if (data.phase gte 2)!false>已达成<#else><#if (data.phase == 1)!false>${(data.count6)!0}个学生<#else>话费奖励</#if></#if></div>
                        </li>
                    </ul>
                </div>
                <div style="text-align: center; padding-top: 20px">
                    <a href="http://help.17zuoye.com/?page_id=1439" target="_blank" class="w-orange">关于"认证话费补贴"活动下线的说明</a>
                </div>
                <div class="t-treeType-btn" style="font-size: 18px; line-height: 70px;">
                    <p style="font-size:14px;margin-bottom:-20px;">话费在满足条件后72小时内到账</p>
                    <#if (currentUser.fetchCertificationState() == "SUCCESS")!false>
                    倒计时 <strong class="w-orange">${(data.countDown30)!0}</strong> 天后奖励消失
                    <#else>
                        <a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage?ref=card" class="w-blue">认证后才能获得话费</a>
                    </#if>
                </div>
            </div>
            <%}%>

            <%if(i == "teachingStudentsUse"){%>
            <div class="cl-step-showTips-text"></div>
            <div data-title="教学生使用" class="t-treeType-box">
                <h2 class="t-treeType-title" style="background: url(<@app.link href='public/skin/teacherv3/images/step-card-back.png'/>) repeat-x 0 0;"><%if(!authFlag){%>第2步：<%}%>教学生使用</h2>
            <#assign _recordId = (currentUser.id)!0/>
            <#if (data.mobileoraccount == "MOBILE" && data.mobile?has_content)!false>
                <div class="t-treeType-fire" style="background: url(<@app.link href='public/skin/teacherv3/images/step-card-board.png'/>) no-repeat center center; height: 90px;">
                    <p style="color: #fff; font-size: 16px; text-align: center; line-height: 150%; padding-top: 19px;">你的手机号<br/><strong style="font-size: 18px;">${(data.mobile)!}</strong></p>
                </div>
                <div class="t-treeType-nullCtn" style="margin-top: 0; line-height: 140%;">
                    <div style="margin-bottom: 10px;"><span style="display: inline-block; overflow: hidden; color: #5fc8ce; width: 15px; margin:0 0 -4px -15px;">●</span>第一步：把你的手机号写在黑板上，让学生记下</div>
                    <div><span style="display: inline-block; overflow: hidden; color: #5fc8ce; width: 15px; margin:0 0 -4px -15px;">●</span>第二步：学生打开一起小学后填写你的手机号即可加入你的班级完成作业</div>
                    <p style="text-align: center;">
                        <a href="javascript:void(0);" class="w-blue v-clickSwitchAccountOrMobile" data-account="ACCOUNT" style="text-decoration: underline;">不方便公布手机号？</a>
                    </p>
                </div>
                <#assign _recordId = (data.mobile)!0/>
            <#else>
                <div class="t-treeType-fire" style="background: url(<@app.link href='public/skin/teacherv3/images/step-card-board.png'/>) no-repeat center center; height: 90px;">
                    <p style="color: #fff; font-size: 16px; text-align: center; line-height: 150%; padding-top: 19px;">你的ID<br/><strong style="font-size: 18px;">${(currentUser.id)!}</strong></p>
                </div>
                <div class="t-treeType-nullCtn" style="margin-top: 0; line-height: 140%;">
                    <div style="margin-bottom: 10px;"><span style="display: inline-block; overflow: hidden; color: #5fc8ce; width: 15px; margin:0 0 -4px -15px;">●</span>第一步：把你的ID写在黑板上，让学生记下</div>
                    <div><span style="display: inline-block; overflow: hidden; color: #5fc8ce; width: 15px; margin:0 0 -4px -15px;">●</span>第二步：学生打开一起小学
                        后填写你的ID号即可加入你的班级完成作业</div>
                    <#if (data.mobile?has_content)!false>
                        <p style="text-align: center;">
                            <a href="javascript:void(0);" class="w-blue v-clickSwitchAccountOrMobile" data-account="MOBILE" style="text-decoration: underline;">让学生使用手机号加入班级</a>
                        </p>
                    </#if>
                </div>
            </#if>
                <div class="t-treeType-btn" style="background: url(<@app.link href='public/skin/teacherv3/images/step-card-back.png'/>) repeat-x 0 bottom;">
                    <%if(cardLogicItem[i].abTest){%>
                    <a href="javascript:void(0);" class="w-btn w-btn-cyan w-circular-5 w-border-cyan" id="clickProfessorStudent">教学生如何使用</a>
                    <%}else{%>
                    <a target="_blank" href="/project/professorstudent/index.vpage?id=${((data.mobileoraccount == "MOBILE")?string(data.mobile, currentUser.id))!''}&name=${(currentUser.profile.realname)!}&subject=${(currentTeacherDetail.subject)!}&ref=card" class="w-btn w-btn-cyan w-circular-5 w-border-cyan">教学生如何使用</a>
                    <%}%>
                </div>
            </div>
            <%}%>

            <%if(i == "homeworkList"){%>
            <div class="cl-step-showTips-text"></div>
            <div class="t-treeType-box">
                <h2 class="t-treeType-title"><%if(!authFlag){%>第1步：<%}%>作业</h2>
                <%var homeworkList = [], homeworkFlag = 'assign', circleCount = 0%>
                <%if(assignHomeworkList.length < 1 && checkHomeworkList.length < 1){%>
                    <%homeworkList = ongingHomeworkList; homeworkFlag = 'onging'%>
                <%}else{%>
                    <%if(checkHomeworkList.length > 0){%>
                        <%homeworkList = checkHomeworkList; homeworkFlag = 'check'%>
                    <%}else{%>
                        <%homeworkList = assignHomeworkList; homeworkFlag = 'assign'%>
                    <%}%>
                <%}%>
                <%if(homeworkList.length < 1){%>
                <div class="t-treeType-nullCtn" style="margin-top: 20px; text-align: center;">
                    <p>
                        暂时没有班级可以布置/检查作业
                    </p>
                </div>
                <%}else{%>
                    <div class="t-treeType-item">
                        <ul>
                            <%for(var z = 0; z < homeworkList.length; z++){%>
                            <li>
                                <%if(z%5 == 0){%>
                                <% circleCount = 0%>
                                <%}%>
                                <%circleCount++%>
                                <p class="circle circle-<%=circleCount%>"><span class="point"></span></p>
                                <p class="name"><%=homeworkList[z].clazzName%><%=homeworkList[z].groupName%></p>
                                <p class="count">
                                    <%if(homeworkFlag == 'assign'){%>
                                    <a href="/teacher/new/homework/batchassignhomework.vpage?log=cardList&subject=<%=subject%>"><span class="crr">待布置</span></a>
                                    <%}else{%>
                                    <span class="crr"><%=homeworkList[z].finishCount%></span>/<%=(homeworkList[z].studentCount)%>
                                    <%}%>
                                </p>
                            </li>
                            <%}%>
                        </ul>
                    </div>
                <%}%>
                <div class="t-treeType-btn">
                    <%if(cardLogicItem[i].sTipsFlag){%>
                        <a href="/teacher/new/homework/batchassignhomework.vpage?log=cardList&subject=<%=subject%>" class="w-btn w-btn-red w-circular-5 w-border-red">布置新作业</a>
                    <%}else{%>
                        <%if(homeworkFlag == 'onging'){%>
                            <a href="/teacher/new/homework/report/list.vpage?log=cardList&subject=<%=subject%>" class="w-btn w-btn-red w-circular-5 w-border-red">管理作业</a>
                        <%}%>
                        <%if(homeworkFlag == 'check'){%>
                            <a href="/teacher/new/homework/report/list.vpage?log=cardList&subject=<%=subject%>" class="w-btn w-btn-red w-circular-5 w-border-red">检查作业</a>
                        <%}%>
                        <%if(homeworkFlag == 'assign'){%>
                            <a href="/teacher/new/homework/batchassignhomework.vpage?log=cardList&subject=<%=subject%>" class="w-btn w-btn-red w-circular-5 w-border-red">布置新作业</a>
                        <%}%>
                    <%}%>
                </div>
            </div>
            <%}%>

            <#--寒假练习 英-->
            <%if(i == 'vacationCard'){%>
                <#if data.assignVacationHomeworked?has_content && data.assignVacationHomeworked>
                    <div data-title="寒假作业" class="t-treeType-box">
                        <h2 class="t-treeType-title">寒假作业</h2>
                        <div class="t-treeType-fire" style="background: url(<@app.link href='public/skin/teacherv3/images/publicbanner/winter-vacation-bg-2.png'/>) #73accd;"></div>
                        <div class="t-treeType-nullCtn" style="margin-top: 15px; text-align: center;">
                            <p style="padding: 0; font-size: 20px;">
                                学生需要您不断的<br/>评价和鼓励
                            </p>
                        </div>
                        <div class="t-treeType-btn">
                            <a href="/teacher/vacation/report/list.vpage?subject=<%=subject%>" class="w-btn w-btn-red">查看进度</a>
                        </div>
                    </div>
                <#else>
                    <#if ftlmacro.devTestSwitch || (.now lt "2017-02-12 23:59:59"?datetime("yyyy-MM-dd HH:mm:ss"))>
                        <div class="t-treeType-box">
                            <h2 class="t-treeType-title">寒假作业</h2>
                            <div class="t-treeType-fire" style="background: url(<@app.link href='public/skin/teacherv3/images/vacation/vacation-card.png'/>) repeat-x center 0;">
                            </div>
                            <div class="final-content">
                                <#if currentTeacherDetail.subject?has_content>
                                    <#if currentTeacherDetail.subject =="ENGLISH" ><#--ENGLISH-->
                                        <p>• 个性化薄弱巩固 <span class="final-item">• 每周一个主题绘本</span></p>
                                        <p>• 每周一次口语专练 • 下学期新单词预习</p>
                                    <#else><#--MATH-->
                                        <p>• 个性化薄弱巩固 <span class="final-item">• 数学趣味绘本</span></p>
                                        <p>• 每周一组口算 </p>
                                    </#if>
                                </#if>
                            </div>
                            <div class="t-treeType-btn">
                                <a href="/teacher/vacation/index.vpage?subject=<%=subject%>" class="w-btn w-btn-green">布置个性化寒假作业</a>
                            </div>
                        </div>
                    </#if>
                </#if>
            <%}%>

            <#--//认证身份-->
            <%if(i == "authenticate"){%>
            <div class="cl-step-showTips-text"></div>
            <div data-title="认证身份" class="t-treeType-box">
                <h2 class="t-treeType-title">认证身份</h2>
                <div class="t-treeType-fire" style="background: url(<@app.link href='public/skin/teacherv3/images/publicbanner/auth-card-1.png'/>) #aae0e2;"></div>
                <div class="t-treeType-item" style="padding: 15px 0 0 15px;">
                    <p>
                        <span class="te-point te-point-5"></span>
                        <span class="w-icon-md">设置姓名并绑定手机${( (data.tnut.name?has_content && data.tnut.mobile?has_content)!false )?string('<span class="w-green">[已达成]</span>', '<span class="w-red">[暂未完成]</span>')}</span>
                    </p>
                    <p>
                        <span class="te-point te-point-5"></span>
                        <span class="w-icon-md"><span class="w-red">8</span>名学生完成<span class="w-red">3</span>次作业${( (data.tnut.finishThreePlusCount gte 8)!false )?string('<span class="w-green">[已达成]</span>', '<span class="w-red">[${(data.tnut.finishThreePlusCount)!0}名已完成]</span>')}</span>
                    </p>
                    <p>
                        <span class="te-point te-point-5"></span>
                        <span class="w-icon-md"><span class="w-red">3</span>名学生绑定手机 ${( (data.tnut.bindMobileCount gte 3)!false )?string('<span class="w-green">[已达成]</span>', '<span class="w-red">[${(data.tnut.bindMobileCount)!0}名已绑定]</span>')}</span>
                    </p>
                </div>
                <div class="t-treeType-btn">
                <#if ((data.tnut.name?has_content && data.tnut.mobile?has_content)!false) && ((data.tnut.finishThreePlusCount gte 8)!false) && ((data.tnut.bindMobileCount gte 3)!false)>
                    <a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage?red=card1" class="w-btn w-btn-red" onclick="$17.tongji('老师新首页-认证身份-立即认证');">立即认证</a>
                <#else>
                    <a href="${(ProductConfig.getUcenterUrl())!''}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage?red=card1" class="w-btn w-btn-orange" onclick="$17.tongji('老师新首页-认证身份-认证进度');">认证进度</a>
                </#if>
                </div>
            </div>
            <%}%>

            <#--任命小组长-->
            <%if(i == "leaderCard"){%>
            <#--<div data-title="任命小组长" class="t-treeType-box">-->
                <#--<h2 class="t-treeType-title">任命小组长</h2>-->
                <#--<div class="t-treeType-fire" style="background: url(<@app.link href='public/skin/teacherv3/images/publicbanner/tinyGroupMem-banner.png'/>) #4acce3;"></div>-->
                <#--<div class="t-treeType-nullCtn" style=" margin: 10px 20px 0; line-height: 22px; ">-->
                    <#--亲爱的老师：<br/>-->
                    <#--一起教育科技推出“小组长”功能，可以让小组长帮您督促学生完成练习！快来尝试下吧~-->
                <#--</div>-->
                <#--<div class="t-treeType-btn">-->
                    <#--<a href="${(ProductConfig.getUcenterUrl())!''}/teacher/systemclazz/clazzindex.vpage?ref=leaderCard" class="w-btn w-btn-cyan w-circular-5 w-border-cyan">去任命小组长</a>-->
                    <#--<br/><a href="javascript:void(0);" class="v-noShowTinyGroupCard" style="margin-top: 4px; display: inline-block">不再显示</a>-->
                <#--</div>-->
            <#--</div>-->
            <%}%>

            <%if(i == "bindingWeChat"){%>
            <div class="t-treeType-box">
                <div data-title="下载老师端">
                    <h2 class="t-treeType-title">下载老师端</h2>
                    <div class="t-treeType-fire" style="background: url(<@app.link href='public/skin/teacherv3/images/publicbanner/auth-card-2.png'/>) #f9ecc6;"></div>
                    <div class="t-treeType-item" style="padding: 15px 0 0 15px;">
                        <p>
                            <span class="te-point te-point-5"></span>
                            <span class="w-icon-md"><span class="w-red">随时随地</span>布置作业</span>
                        </p>
                        <p>
                            <span class="te-point te-point-5"></span>
                            <span class="w-icon-md">每天免费<span class="w-red">5次抽奖</span>，平板等你拿</span>
                        </p>
                        <p>
                            <span class="te-point te-point-5"></span>
                            <span class="w-icon-md">练习/活动信息<span class="w-red">及时</span>接收</span>
                        </p>
                    </div>
                    <div class="t-treeType-btn">
                        <a href="/help/downloadApp.vpage?refrerer=pc&count=0" target="_blank" class="w-btn">立即下载</a>
                    </div>
                </div>
            </div>
            <%}%>

            <#--邀请老师升级啦!-->
            <%if(i == "inviteTeacherUpdate"){%>
                <div data-title="邀请老师升级啦!" class="t-treeType-box">
                    <h2 class="t-treeType-title">邀请老师升级啦!</h2>
                    <div class="t-treeType-fire" style="background: url(<@app.link href='public/skin/teacherv3/images/publicbanner/invitationupgrade-card.png'/>) repeat-x center 0;">
                        <div style="color: #fff;text-align: center;font-size: 18px;">
                            <#if schoolSectionFlag>
                                <p style="padding-top:30px;">中学老师能邀请<br>话费更多更轻松</p>
                            <#else>
                                <p style="padding-top:41px;">邀请同班老师也能得话费</p>
                            </#if>
                        </div>
                    </div>
                    <div class="t-treeType-item" style="padding: 15px 0 0 15px;">
                        <#--<p style="font-size: 14px;text-align: center;">
                            全国已有 <strong class="w-orange">${(data.totalInviteCount)!0}</strong> 名老师参与活动<br/>
                            累计已获得 <strong class="w-orange">${(data.totalFee)!0}</strong> 元话费
                        </p>-->
                        <p style="font-size: 14px;text-align: center;">
                           <a href="http://help.17zuoye.com/?page_id=1427" target="_blank" class="w-orange">关于邀请活动11月9号下线的说明</a>
                        </p>
                        <p style="text-align: center;margin-top: 20px;">
                            <a href="http://help.17zuoye.com/?p=<#if schoolSectionFlag>1343<#else>1339</#if>" target="_blank" class="w-blue" style="text-decoration: underline;">查看活动规则</a>
                        </p>
                    </div>
                    <div class="t-treeType-btn">
                        <a href="/teacher/invite/index.vpage?ref=card" class="w-btn w-btn-green">去邀请</a>
                    </div>
                </div>
            <%}%>

            <#--没有更多任务了-->
            <%if(i == "noContent"){%>
            <div class="t-treeType-box">
                <div data-title="没有更多任务了">
                    <h2 class="t-treeType-title">没有更多任务了</h2>
                    <div class="t-treeType-fire" style="background-color: #2298eb; position: relative; text-align: center; height: 305px;">
                        <div style="position: absolute; font-size: 14px; top: 99px; width: 100%; z-index: 5;">
                            <a style="display: block; height: 40px; width: 100%; background: url(<@app.link href="public/skin/teacherv3/images/w-point.png"/>) no-repeat -100px -100px; " href="/teacher/clazz/clazzlist.vpage" data-title="查看班级情况"></a>
                            <a style="display: block; height: 40px; width: 100%; background: url(<@app.link href="public/skin/teacherv3/images/w-point.png"/>) no-repeat -100px -100px;" target="_blank" href="/ucenter/partner.vpage?url=${ProductConfig.getBbsSiteBaseUrl()}/open.php?mod=register&teacherType=2" data-title="去教师论坛看看"></a>
                            <a style="display: block; height: 40px; width: 100%; background: url(<@app.link href="public/skin/teacherv3/images/w-point.png"/>) no-repeat -100px -100px;" target="_blank" href="/reward/index.vpage" data-title="去奖品中心看看"></a>
                        </div>
                        <img src="<@app.link href='public/skin/teacherv3/images/publicbanner/teacher-card-back.png'/>">
                    </div>
                </div>
            </div>
            <%}%>

            <%if(i == "authPrivilege"){%>
            <div class="cl-step-showTips-text"></div>
            <div data-title="认证特权" class="t-treeType-box">
                <h2 class="t-treeType-title">认证特权</h2>
                <div class="t-treeType-fire" style="background: url(<@app.link href='public/skin/teacherv3/images/publicbanner/auth-card-1.png'/>) #aae0e2;"></div>
                <div class="t-treeType-item" style="padding: 15px 0 0 15px; line-height: 24px;">
                    <div>
                        <span class="te-point te-point-5"></span>
                        <span class="w-icon-md">认证成功，专属奖励   <a href="/teacher/updatetpc.vpage?type=awardCenter" class="w-blue">奖品中心</a></span>
                    </div>
                    <div>
                        <span class="te-point te-point-5"></span>
                        <span class="w-icon-md">全部绘本，随心所用   <a href="/teacher/updatetpc.vpage?type=reading" class="w-blue">立即体验</a></span>
                    </div>
                    <div>
                        <span class="te-point te-point-5"></span>
                        <span class="w-icon-md">布置作业，免费抽奖   <a href="/teacher/updatetpc.vpage?type=lottery" class="w-blue">大抽奖</a></span>
                    </div>
                    <div>
                        <span class="te-point te-point-5"></span>
                        <span class="w-icon-md">课堂气氛，活跃神器   <a href="/teacher/updatetpc.vpage?type=smartClazz" class="w-blue">智慧课堂</a></span>
                    </div>
                </div>
                <div class="t-treeType-btn" style="font-size: 18px; line-height: 70px;">
                    <a href="/teacher/updatetpc.vpage?type=doNotShowAgain" style="color: #999; font-size: 14px; text-decoration: underline;">我知道了  不再显示</a>
                </div>
            </div>
            <%}%>

            <%if(i == "ambassadorWelfare"){%>
            <div data-title="大使福利" class="t-treeType-box">
                <h2 class="t-treeType-title">大使福利</h2>
                <div class="t-treeType-fire" style="background: url(<@app.link href='public/skin/teacherv3/images/publicbanner/ambassador-card-1.png'/>) #aae0e2;"></div>
                <div class="t-treeType-nullCtn" style="text-align: center; margin: 5px 0 0;">
                    <p>绑定校园大使微信公众号</p>
                    <p>送红米手机、现金红包、海量园丁豆</p>
                    <p class="w-red">每天只限8点到24点能抽奖！</p>
                </div>
                <div class="t-treeType-btn">
                    <a href="javascript:void(0);" class="w-btn w-btn-cyan w-circular-5 w-border-cyan click-ambBindingWeiXin">绑定微信</a>
                </div>
            </div>
            <%}%>

            <#--首页校园大使 暂时关闭 “校园大使” 入口 11-16-->
            <%if(i == "campusAmbassador"){%>
            <div data-title="首页校园大使" class="t-treeType-box">
                <h2 class="t-treeType-title">校园大使</h2>
                <div class="t-treeType-fire" style="background: url(<@app.link href='public/skin/teacherv3/images/publicbanner/ambassador-card.png'/>) #aae0e1;"></div>
                <div class="t-treeType-nullCtn" style="text-align: center;">
                    <p>每所学校一个名额</p>
                    <p>享奖品兑换专区</p>
                </div>
                <div class="t-treeType-btn">
                    <a href="/ambassador/schoolambassador.vpage?ref=card" target="_blank" class="w-btn w-btn-cyan w-circular-5 w-border-cyan" onclick="$17.tongji('老师新首页-运营卡片-申请校园大使');">申请校园大使</a>
                </div>
            </div>
            <%}%>

            <#--老师唤醒-->
            <%if(i == "teacherAwaken"){%>
            <div data-title="老师唤醒" class="t-treeType-box">
                <h2 class="t-treeType-title">老师唤醒</h2>
                <div class="t-treeType-fire" style="background: url(<@app.link href='public/skin/teacherv3/images/publicbanner/teacherawaken.jpg'/>) repeat-x center 0;">
                </div>
                <div class="t-treeType-nullCtn" style="text-align: center;">
                    <p>一次至少获得50园丁豆哦</p>
                </div>
                <div class="t-treeType-btn">
                    <a href="/teacher/invite/activateteacher.vpage" class="w-btn w-btn-green">去唤醒</a>
                </div>
            </div>
            <%}%>

            <#--首页体验师-->
            <%if(i == "experienceDivision"){%>
            <div data-title="首页体验师" class="t-treeType-box">
                <h2 class="t-treeType-title">首页体验师</h2>
                <div class="t-treeType-nullCtn" style="position: relative;">
                    <div class="index-experience-icon"></div>
                    <h4>亲爱的老师：</h4>
                    <p>您好！您被选中优先体验新版首页，新版不影响已有功能，请放心。<a href="http://help.17zuoye.com/?page_id=794" target="_blank" class="w-blue">点击了解变化</a></p>
                </div>
                <div class="t-treeType-btn">
                    <a href="javascript:void(0);" class="w-btn w-btn-cyan w-circular-5 w-border-cyan message_right_sidebar" data-title="新首页反馈" onclick="$17.tongji('老师新首页-运营卡片-反馈问题');">反馈问题</a>
                </div>
            </div>
            <%}%>

            <#--假期练习-->
            <%if(i == "holidayCard"){%>
                <#if (.now gt "2016-07-31 23:59:59"?datetime("yyyy-MM-dd HH:mm:ss"))!false>
                    <div data-title="假期作业" class="t-treeType-box">
                        <h2 class="t-treeType-title">假期作业</h2>
                        <div class="t-treeType-fire" style="background: url(<@app.link href='public/skin/teacherv3/images/publicbanner/summerJob-card02.png'/>) #73accd;"></div>
                        <div class="t-treeType-nullCtn" style="margin-top: 15px; text-align: center;">
                            <p>学生需要您不断的评价和鼓励</p>
                            <p style="font-size:13px;">老师app开学最高再领<span style="color:#f86638">150园丁豆</span></p>
                            <!--二维码朝上-->
                            <div class="card-download">
                                <a href="javascript:void(0)" class="J_loadLinkApp loadLink">现在下载
                                    <div class="card-loadCode card-loadCode-top">
                                        <img class="code" src="<@app.link href='public/skin/teacherv3/images/card-loadCode.png'/>">
                                        <p class="text">扫二维码下载老师app</p>
                                        <span class="arrow"></span>
                                    </div>
                                </a>
                            </div>
                        </div>
                        <div class="t-treeType-btn">
                            <a href="/teacher/holiday/vhindex.vpage?ref=card" class="w-btn w-btn-red">查看进度</a>
                        </div>
                    </div>
                <#else>
                    <div data-title="假期作业" class="t-treeType-box">
                        <h2 class="t-treeType-title">假期作业</h2>
                        <div class="t-treeType-fire" style="background: url(<@app.link href='public/skin/teacherv3/images/publicbanner/summerJob-card01.png'/>) #ec3237;"></div>
                        <div class="t-treeType-nullCtn" style="margin-top: 15px; text-align: center;">
                            <p style="line-height:22px">布置立得100园丁豆</p>
                            <p style="line-height:22px">开学可抽取高清电视、智能手机</p>
                            <p style="line-height:22px;font-size:13px;">老师app开学最高再领<span style="color:#f86638">150园丁豆</span></p>
                            <!--二维码朝上-->
                            <div class="card-download">
                                <a href="javascript:void(0)" class="J_loadLinkApp loadLink">现在下载
                                    <div class="card-loadCode card-loadCode-top">
                                        <img class="code" src="<@app.link href='public/skin/teacherv3/images/card-loadCode.png'/>">
                                        <p class="text">扫二维码下载老师app</p>
                                        <span class="arrow"></span>
                                    </div>
                                </a>
                            </div>
                        </div>
                        <div class="t-treeType-btn">
                            <a href="/teacher/holiday/bavhstac.vpage?ref=homecard" class="w-btn w-btn-red">立即布置</a>
                        </div>
                    </div>
                </#if>
            <%}%>

            <#--开学大礼包-->
            <%if(i == "termbeginActivity"){%>
                <div data-title="开学大礼包" class="t-treeType-box">
                    <h2 class="t-treeType-title">
                        开学大礼包
                    </h2>
                    <div class="t-treeType-fire" style="background: url(<@app.link href='public/skin/teacherv3/images/publicbanner/termbegin-card-banner2.png'/>) #ffa897; height: 140px;"></div>
                    <div class="t-treeType-nullCtn" style="line-height: 140%; margin-top: 20px;">
                        <div style="margin-bottom: 10px;">
                            <span style="display: inline-block; overflow: hidden; color: #383a4c; width: 15px; margin:0 0 -4px -15px;">●</span>开学班级有变动？
                            <span style="display: inline-block; margin-left: 45px;">
                            <#if (data.adjustFlag)!false>
                                已调整
                            <#else>
                                <a href="javascript:void(0);" class="w-blue v-goToSetClazz">去调整</a>
                            </#if>
                            </span>
                        </div>
                        <div style="margin-bottom: 10px;">
                            <span style="display: inline-block; overflow: hidden; color: #383a4c; width: 15px; margin:0 0 -4px -15px;">●</span>新学期第一份作业
                            <span style="display: inline-block; margin-left: 45px;">
                            <#if (data.homeworkFlag)!false>
                                已布置
                            <#else>
                                <a href="javascript:void(0);" class="w-blue v-goToHomework">去布置</a>
                            </#if>
                            </span>
                        </div>
                    </div>
                    <div class="t-treeType-btn">
                        <a href="/activity/termbegin.vpage?ref=schoolCard&s0=home_card" class="w-btn js-check" target="_blank">开学大礼包</a>
                    </div>
                </div>
            <%}%>

            <#--17奖学金-->
            <%if(i == "scholarship"){%>
                <div data-title="17奖学金" class="t-treeType-box">
                    <h2 class="t-treeType-title">
                        17奖学金
                    </h2>
                    <div class="t-treeType-fire" style="background: url(<@app.link href='public/skin/teacherv3/images/publicbanner/teacherlotterynew-card-banner02.jpg'/>) #ffa897; height: 120px;"></div>
                    <div class="final-content">
                        <p>• 春风十里不如，赢取17奖学金</p>
                        <p>• 奖池全面升级，只为优秀的你</p>
                        <p>• 30万大赏奖励，尽在一起教育科技</p>
                    </div>
                    <div class="t-treeType-btn">
                        <a href="/activity/teacherlotterynew.vpage" class="w-btn js-check" target="_blank">17奖学金</a>
                    </div>
                </div>
            <%}%>

            <#--卡片广告位-->
            <%if(i == "crmSetCard"){%>
                <%var popupItems = cardLogicItem[i].data[0];%>
                <div data-title="crmSetCard" class="t-treeType-box">
                    <%if(popupItems.description){%>
                    <h2 class="t-treeType-title"><%=popupItems.description%></h2>
                    <%}%>
                    <%if(popupItems.img){%>
                    <div class="t-treeType-fire" style="background: url(<%=cardLogicItem[i].imgDoMain%>/gridfs/<%=popupItems.img%>) #ec3237;"></div>
                    <%}%>
                    <%if(popupItems.content){%>
                    <div class="t-treeType-nullCtn" style="margin-top: 15px; text-align: center;">
                        <p style="line-height:22px"><%=popupItems.content%></p>
                    </div>
                    <%}%>
                    <%if(popupItems.btnContent){%>
                    <div class="t-treeType-btn">
                        <a href="<%=cardLogicItem[i].goLink%>?aid=<%=popupItems.id%>&index=<%=index%>" class="w-btn w-btn-blue"><%=popupItems.btnContent%></a>
                    </div>
                    <%}%>
                </div>
            <%}%>
        <#--end//-->
        </li>
        <%}%>
    <%}%>
<#--<#if (.now lt "2016-04-11 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss") || ftlmacro.devTestSwitch)!false>
    <div style="position: relative; clear: both;">
        <div style="position: absolute; left: 70px; top: 5px;">
            <a href="/teacher/activity/threemonth.vpage" target="_blank"><img src="<@app.link href="public/skin/project/threemonthgift/teacher/images/ycMarch-icon.png"/>"/></a>
        </div>
    </div>
</#if>-->
</script>

<@sugar.capsule js=["ZeroClipboard"] css=[] />
<#if studentSendAcNum!false>
<script type="text/html" id="T:教学生如何使用Popup">
    <style>
        /*reg-program-copy*/
        .clickProfessorStudent-jqi .jqi{ background: #f3f2f0;}
        .reg-program-copy{margin: -30px auto 0;}
        .reg-program-copy .title{padding:10px;}
        .reg-program-copy .title h5{font-size:14px;font-weight: normal;line-height: 22px;}
        .reg-program-copy .title h5 span{color:#fa7252;}
        .reg-program-copy .paragraph{border:1px solid #dfdfdf;/*box-shadow:2px 2px 2px #d6d6d6 inset;*/border-radius: 4px;padding:17px;position: relative;background-color: #ffffff;}
        .reg-program-copy .paragraph .par-space-arrow{position: absolute; background: url(<@app.link href="public/skin/project/professorstudent/images/arrow.png"/>) no-repeat;width:13px;height:11px;top:-11px;left:25px;}
        .reg-program-copy .paragraph p{font-size:14px;line-height: 24px;}
        .reg-program-copy .paragraph p a{color:#189cfb;}
        .reg-program-copy .btn{text-align: center;margin:20px 0 20px 0;}
        .reg-program-copy .btn .copy-btn{background-color: #189cfb;border:1px solid #0979ca;display: inline-block;color: #ffffff;font-size: 14px;border-radius: 3px;}
    </style>
    <div class="reg-program-copy">
        <div class="title">
            <h5>推荐老师：通过<span>校讯通、飞信、微信群、QQ群</span>等，通知家长帮孩子注册账号！</h5>
        </div>
        <div class="paragraph">
            <i class="par-space-arrow"></i>
            <p>
                <#if currentUser.id%10 gte 5><#--5_9-->
                    重要：通知各位家长尽快帮孩子注册账号，完成班级在线作业。我将在教育部课题平台——一起老师上布置少量配合教材的在线练习。学生可以在线学习<#if (currentTeacherDetail.subject == "ENGLISH")!false>口语、听力</#if>，这个平台内容、资源丰富且完全免费，练习寓教于乐、可大大提高学习兴趣。请及时帮孩子注册，并注意控制孩子使用时长。
                <#else>
                    家长好！我发现了一个教材同步、资源丰富且完全免费的学习网站：一起教育科技官网。学生可以在线学习<#if (currentTeacherDetail.subject == "ENGLISH")!false>口语、听力</#if>，寓教于乐、大大提高学习兴趣。我会在网站布置辅助课堂的在线作业，请各位家长帮孩子注册、领取孩子账号，注册时输入老师号码：
                    <span class="teacherIdBox" style="color:#189cfb;">${((data.mobileoraccount == "MOBILE")?string(data.mobile, currentUser.id))!''}</span>
                </#if>
            </p>
            <p>网站地址：<a href="javascript:void(0);">http://www.17zuoye.com</a>（有手机的可下载手机端做练习）</p>
            <p>注册时输入我的号：<span class="teacherIdBox" style="color:#189cfb;">${((data.mobileoraccount == "MOBILE")?string(data.mobile, currentUser.id))!''}</span></p>
            <p>不会注册或需下载手机端，请点击链接查看说明：<span class="professorCopyUrl"></span></p>
            <p class="sub">（<#if (currentTeacherDetail.subject == "ENGLISH")!false>英语</#if><#if (currentTeacherDetail.subject == "MATH")!false>数学</#if><#if (currentTeacherDetail.subject == "CHINESE")!false>语文</#if>老师：<span class="teacherNameBox">${(currentUser.profile.realname)!}</span>）</p>
        </div>
        <div class="btn">
            <a class="copy-btn"  href="javascript:void(0);" id="clip_container1" style="position: relative; width: 150px;"><span id="clip_button1" style="display: block; line-height: 45px; width: 100%;">复制上面内容</span></a>
        </div>
    </div>
    <textarea disabled="disabled" readonly="readonly" id="copy_info_url" style="display: none;"></textarea>
</script>
<script type="text/javascript">
    $(function(){
        //打点
        <#if currentTeacherDetail.isJuniorTeacher()>
            $(document).on("click",".js-check",function(){
                YQ.voxLogs({
                    database : "web_teacher_logs",
                    module: 'm_rQjVWe1G',
                    op : "o_NXl0M3Qx"
                });
            });
            $(document).on("click",".js-get",function(){
                YQ.voxLogs({
                    database : "web_teacher_logs",
                    module: 'm_rQjVWe1G',
                    op : "o_gP2WE60S"
                });
            });
        </#if>
        //教学生使用
        $(document).on("click", "#clickProfessorStudent", function(){
            var OrSharpLink = "http://"+location.host+"/project/professorstudent/index.vpage?id=${((data.mobileoraccount == "MOBILE")?string(data.mobile, currentUser.id))!''}&name="+encodeURIComponent("${(currentUser.profile.realname)!}")+"&subject=${(currentTeacherDetail.subject)!}&ref=card";
            var sharpLink = OrSharpLink;

            $17.getShortUrl(sharpLink, function(u){
                sharpLink = u;

                $.prompt(template("T:教学生如何使用Popup", {}), {
                    classes : {
                        box: 'clickProfessorStudent-jqi'
                    },
                    title: "如何添加学生？",
                    buttons: {},
                    position: {width: 760},
                    loaded : function(){
                        var subjectText = "";
                        switch ($17.getQuery("subject")){
                            case "ENGLISH" :
                                subjectText = "英语";
                                break;
                            case "MATH" :
                                subjectText = "数学";
                                break;
                            case "CHINESE" :
                                subjectText = "语文";
                                break;
                        }

                        var content = "家长好！我发现了一个教材同步、资源丰富且完全免费的学习网站：一起教育科技官网。学生可以在线学习<#if (currentTeacherDetail.subject == "ENGLISH")!false>口语、听力</#if>，寓教于乐、大大提高学习兴趣。我会在网站布置辅助课堂的在线练习，请各位家长帮孩子注册、领取孩子账号，注册时输入老师号码：${((data.mobileoraccount == "MOBILE")?string(data.mobile, currentUser.id))!''}";
                        <#if currentUser.id%10 gte 5><#--5_9-->
                            content = "重要：通知各位家长尽快帮孩子注册账号，完成班级在线练习。我将在教育部课题平台——一起老师端布置少量配合教材的在线练习。学生可以在线学习<#if (currentTeacherDetail.subject == "ENGLISH")!false>口语、听力</#if>，这个平台内容、资源丰富且完全免费，练习寓教于乐、可大大提高学习兴趣。请及时帮孩子注册，并注意控制孩子使用时长。"
                        </#if>
                        var textAreaVal = content
                                +"网站地址：http://www.17zuoye.com（有手机的可下载手机端做练习）"
                                +"注册时输入我的号：${((data.mobileoraccount == "MOBILE")?string(data.mobile, currentUser.id))!''}"
                                +"不会注册或需下载手机端，请点击链接查看说明：" + sharpLink
                                + "（"+subjectText+"老师：${(currentUser.profile.realname)!}）";

                        $(".professorCopyUrl").html("<a target='_black' href='"+OrSharpLink+"'>"+sharpLink+"</a>");
                        $("#copy_info_url").val(textAreaVal);

                        $17.copyToClipboard($("#copy_info_url"), $("#clip_button1"), "clip_button1", "clip_container1", function(){
                            $17.voxLog({
                                app : "shares",
                                module : "teacherSharesRegCourse",
                                op : "pc-homeCard-copyLink"
                            });
                        });
                    }
                });
            });
        });
    });
    function nextHomeWork(){
        $.prompt.close();
    }
</script>
</#if>