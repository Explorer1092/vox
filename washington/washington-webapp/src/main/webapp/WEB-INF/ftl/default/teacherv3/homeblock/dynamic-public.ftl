<script type="text/html" id="T:temporaryDynamicActivity">
<#if (data.showSapl)!false>
<#--一起练习学生手机版上线啦-->
    <style>
        /*inform-main*/
        .inform-main{ position: relative; margin-left: 25px; width: 563px; height: 219px; font-size: 12px; background: #f2f2f2; padding: 12px 25px; border: 1px solid #ccc; border-radius: 10px;}
        .inform-main .triangle{ display: block; width: 24px; height: 20px; position: absolute; left: -24px; top: 17px; background: url(<@app.link href="public/skin/project/recommendedapp/images/triangle.jpg"/>) no-repeat;}
        .inform-main .imain-left{ float: left; width: 435px; display: inline; text-align: center;}
        .inform-main .tips{ color: #4e5658; text-align: left; line-height: 22px;}
        .inform-main .tips span{ color: #269ef8;}
        .inform-main .copy-text{ width: 396px; height: 110px; padding: 10px 12px; margin-bottom: 8px; background: #fff; border: 1px solid #979b9b; box-shadow: -1px 1px 2px #ccc inset;}
        .inform-main .copy-text textarea{ width: 396px; height: 106px; border: 0; outline-style: none; line-height: 18px; font-family: "微软雅黑"; color: #4e5656; font-size: 13px; overflow: hidden; resize: none;}
        .inform-main .copy-btn{ display: inline-block; cursor: pointer; color: #fff !important; background: #189cfb; border-radius: 3px; border: 1px solid #0979ca;}
        .inform-main .copy-btn span{display: inline-block; padding: 5px 20px;}
        .inform-main .imain-right{ float: right; }
        .inform-main .imain-right img{ width: 116px; height: 114px;}
    </style>
    <dl class="t-dynamic-module">
        <dt class="t-dynamic-avatar">
            <span class="avatar-icon avatar-icon-4"><img src="<@app.link href="public/skin/teacherv3/images/publicbanner/dy-avatar-icon.png"/>"></span>
        </dt>
        <dd class="t-dynamic-infoBox">
            <div class="dynamic-style">
                <div class="inform-main">
                    <span class="triangle"></span>
                    <div class="imain-left" style="width: auto;">
                        <p class="tips">Hi ${(currentUser.profile.realname)!}老师，一起小学学生手机版上线啦！<span>学生家里没电脑，也可以用一起小学学生端app了！</span></p>
                        <p class="tips">推荐您通过<span>校讯通、飞信、QQ群</span>等，告知家长下载使用：</p>
                    </div>
                    <div class="imain-left">
                        <div class="copy-text">
                            <textarea readonly="readonly" id="recommendedAppContentCode">家长你好！我在“一起小学”布置了作业，请家长帮孩子注册，保证孩子按时完成练习。可以用手机直接下载app并注册，在手机上完成练习。有电脑账号的在手机上输入账号密码即可登录。注册时请填写我的号码：${(currentUser.id)!}。
下载地址：www.17zyw.cn/ZRnAb2
（<#if (currentTeacherDetail.subject == "ENGLISH")!false>英语</#if><#if (currentTeacherDetail.subject == "MATH")!false>数学</#if><#if (currentTeacherDetail.subject == "CHINESE")!false>语文</#if>老师：${(currentUser.profile.realname)!}）</textarea>
                        </div>
                        <a class="copy-btn btn"  href="javascript:void(0);" id="clip_container2" style="position: relative; "><span id="clip_button2">复制上面内容</span></a>
                    </div>
                    <div class="imain-right">
                        <a href="/teacher/activity/recommendedapp.vpage" target="_blank"><img src="<@app.link href="public/skin/project/recommendedapp/images/app-code.jpg"/>">
                            <span style="display: block; text-align: center; font-size: 14px; line-height: 120%; text-decoration: underline; color: #39f;">手机扫描二维码<br/>下载体验学生端</span>
                        </a>
                    </div>
                </div>
            </div>
        </dd>
    </dl>
</#if>

<#--奖励发放-->
<#if inviteCFlag!false>
    <dl class="t-dynamic-module">
        <dt class="t-dynamic-avatar">
            <span class="avatar-icon avatar-icon-5"></span>
        </dt>
        <dd class="t-dynamic-infoBox">
            <div class="dynamic-style">
                <div class="dynamic-style-title">奖励发放
                    <a href="http://help.17zuoye.com/?page_id=1439" target="_blank" class="w-orange" style="font-size: 14px">（关于“老师认证话费补贴”活动下线的说明）</a>
                </div>
            <#if (data.wechatBinded)!false>
            <#else>
                <div style="background: url(<@app.link href='public/skin/teacherv3/images/notauthindex/dy-card_3.png'/>) no-repeat 0 center; height: 138px;"><span class="teacherSendBillCampaignType" style="float: left; padding: 4px 0 0 524px"><#--二维码--></span></div>
            </#if>
                <div class="rank-list rank-0 rank-show">
                    <div class="dynamic-style-content">
                    <#--//content-->
                        <div class="choose-teacherUsed-box" style="width: 658px;">
                            <style>
                                .t-awardInvitation-box .ai-title{border-bottom: 1px solid #d3d8df;}
                                .t-awardInvitation-box h4{font-size: 16px;font-weight: normal;line-height: 44px;padding:0 0 0 3px;}
                                .t-awardInvitation-box h5 .ai-time,.invite-box h5 .ai-time{color: #f87358;}
                                .t-awardInvitation-box h5, .ai-content h5, .invite-box h5{font-size: 16px;font-weight: normal;line-height: 40px;padding:0 0 0 3px;clear: both;}
                                .invite-box .ai-content{border:1px solid #e9ecf1;background-color: #f6f9fe;}
                                .invite-box .ai-content h5{border-bottom:1px solid #e9ecf1;}
                                .invite-box .ai-right{padding:10px 15px 0 0;}
                                .ai-content {overflow: hidden;width:100%;position: relative;}
                                .ai-content .ai-left{float: left;padding: 10px 0 0 14px;width:450px;}
                                .ai-right{padding:50px 16px 0 0;float: right;}
                                .ai-right .view_btn{color: #279ff9;font-size: 14px;}
                                .ai-content .ai-progress{overflow: hidden; *overflow: hidden;*zoom:1;}
                                .ai-content .ai-progress-info{padding:12px 0;clear: both;}
                                .ai-content .ai-progress-info .p-left{float: left;}
                                .ai-content .ai-progress-info .p-right{text-align: right;}
                                .ai-content .ai-progress .bar{border-radius: 50%;background-color: #eff2f6;width:30px;height:30px;line-height: 30px;text-align: center;color: #4e5656;font-size: 14px;float: left;}
                                .ai-content .ai-progress .inner{width:390px;height:10px;background-color: #eff2f6;float: left;margin:10px 0;overflow: hidden;}
                                .ai-content .ai-progress .inner .current{height:10px;background-color: #3da8f1;}
                                .ai-content .ai-progress .active{background-color: #3da8f1;color: #fff;}
                            </style>
                            <div class="invite-box">
                                <div class="ai-content">
                                    <h5>您已参加奖励计划 （不关注微信，或取消关注只得一半话费奖励）</h5>
                                    <div class="ai-left" style="width: auto">
                                        <h5 style="border:none;">奖励进度：已有 <strong class="w-orange">${(data.count6)!0}</strong> 名新学生完成 6 次作业<span style="display:inline-block;margin: 0 10px;font-size:14px;font-weight:normal">（话费在满足条件后72小时内到账）</span></h5>
                                        <div class="ai-progress">
                                            <div class="bar <#if (data.count6 gt 30)!false>active</#if>">30</div>
                                            <div class="inner">
                                                <#if (data.count6 gt 30 && data.count6 lt 90)!false>
                                                    <div class="current" style="width:60%;"></div>
                                                <#elseif (data.count6 gt 90)!false>
                                                    <div class="current" style="width:100%;"></div>
                                                </#if>
                                            </div>
                                            <div class="bar <#if (data.count6 gte 90)!false>active</#if>">90</div>
                                        </div>
                                        <div class="ai-progress-info">
                                            <div class="p-left">＋10元话费（<#if (data.phase gte 1)!false>已达成<#else>未达成</#if>）</div>
                                            <div class="p-right">＋30元话费（<#if (data.phase gte 2)!false>已达成<#else>未达成</#if>）</div>
                                        </div>
                                    </div>
                                </div>
                                <div class="ai-right">
                                    <#--<a href="javascript:void(0);" class="view_btn">查看详细规则</a>-->
                                </div>
                            </div>
                        </div>
                    <#--//content-->
                    </div>
                </div>
            </div>
        </dd>
    </dl>
</#if>

<#--动态广告位-->
<%if(result){%>
<dl class="t-dynamic-module">
    <%var popupItems = result.data[0];%>
    <dt class="t-dynamic-avatar">
        <span class="avatar-icon avatar-icon-5"></span>
    </dt>
    <dd class="t-dynamic-infoBox">
        <div class="dynamic-style">
            <div class="dynamic-style-title"><%=popupItems.description%></div>

            <div class="dynamic-style-content">
                <div style="clear: both;"><%=popupItems.content%></div>
                <%if(popupItems.img){%>
                <a href="<%=result.goLink%>?aid=<%=popupItems.id%>&index=<%=index%>" style="cursor:pointer;"><img src="<%=result.imgDoMain%>/gridfs/<%=popupItems.img%>"></a>
                <%}%>
                <div class="w-clear"></div>
            </div>
            <%if(popupItems.btnContent){%>
            <div class="t-dynamic-last">
                <a href="<%=result.goLink%>?aid=<%=popupItems.id%>&index=<%=index%>" class="w-btn w-btn-mBlue w-btn-mini" style="background-color: #e1f0fc; border-color: #abc1d3;"><%=popupItems.btnContent%></a>
                <div class="w-clear"></div>
            </div>
            <%}%>
        </div>
    </dd>
</dl>
<%}%>

<#--个性化寒假练习-->
<#if currentTeacherDetail.subject?has_content && (currentTeacherDetail.subject == "ENGLISH" || currentTeacherDetail.subject == "MATH") && (ftlmacro.devTestSwitch || (.now gt "2017-01-09 00:00:00"?datetime("yyyy-MM-dd HH:mm:ss") && .now lt "2017-02-12 23:59:59"?datetime("yyyy-MM-dd HH:mm:ss")))!false>
<dl class="t-dynamic-module">
    <dt class="t-dynamic-avatar">
        <span class="avatar-icon avatar-icon-3"></span>
    </dt>
    <dd class="t-dynamic-infoBox">
        <div class="dynamic-style">
            <div data-title="个性化寒假练习">
                <style>
                    .winterVacation-work{background-color: #f6f9fe;padding: 6px 20px;line-height: 26px;font-size: 14px;color: #4e5656;}
                    .winterVacation-work .wTime{padding: 16px 0;font-weight: bold;}
                    .winterVacation-work .wTime .red{font-size: 30px;color: #fc6f43;}
                    .winterVacation-work .w-btn-mBlue{margin-left: 34px;padding: 8px 0px;background-color: #e1f0fc;border-color: #abc1d3;font-size: 16px;}
                </style>
                <div class="dynamic-style-title">个性化寒假作业</div>

                <div class="winterVacation-work">
                    <#if currentTeacherDetail.subject == "ENGLISH">
                        <div class="wText">内容特点1：依据本学期薄弱单词\句型\语法，大数据推荐而成的个性化寒假专练</div>
                        <div class="wText">内容特点2：下学期期新单词预习：前30%单元</div>
                        <div class="wText">内容特点3：每周1次口语专练、每周1个主题绘本</div>
                    <#else>
                        <div class="wText">内容特点1：依据本学期薄弱知识点，大数据推荐而成的个性化寒假专练</div>
                        <div class="wText">内容特点2：数学趣味绘本：当数学遇到绘本，趣味无穷大</div>
                    </#if>
                    <div class="wTime">即日起至2017.02.12，布置立得<span class="red">100</span>园丁豆<a class="w-btn w-btn-mBlue" href="/teacher/vacation/index.vpage?subject=${currentTeacherDetail.subject!}">布置个性化寒假作业</a></div>
                </div>
            </div>
        </div>
    </dd>
</dl>
</#if>
</script>

<script type="text/html" id="T:有奖互助">
    <%if(item){%>
    <dl class="t-dynamic-module">
        <dt class="t-dynamic-avatar">
            <span class="avatar-icon avatar-icon-5"></span>
        </dt>
        <dd class="t-dynamic-infoBox">
            <div class="dynamic-style">
                <div class="dynamic-style-title">有奖互助</div>
                <div class="dynamic-style-content">
                    <div style="clear: both;">奖励进度：
                        <%if(item.oneContent){%>
                            <%=item.oneContent + '+100'%>
                        <%}else{%>
                            <%if(item.mentoringCount){%>
                                您正在帮助<%=item.mentoringCount%>名老师，全部达成可获得<span class="w-orange">500园丁豆</span>
                            <%}else{%>
                                您可帮助4名老师，赢取园丁豆奖励
                            <%}%>
                        <%}%>
                    </div>
                    <ul class="m-invite-dynamic-box">
                        <#--<li class="iv-1">-->
                            <#--<p class="iv-text">期末回馈<br/><span>帮助同校老师，<br/>得300园丁豆！</span></p>-->
                            <#--<div class="iv-btn">-->
                                <#--<div class="iv-back"></div>-->
                                <#--<a href="/teacher/invite/activateteacher.vpage">查看详情</a>-->
                            <#--</div>-->
                        <#--</li>-->
                        <%if(item.twoContent){%>
                        <li class="iv-2">
                            <p class="iv-text"><%==item.twoContent%></p>
                            <div class="iv-btn">
                                <div class="iv-back"></div>
                                <a href="/teacher/invite/activateteacher.vpage">查看详情</a>
                            </div>
                        </li>
                        <%}%>
                        <%if(item.warningCount > 0){%>
                        <li class="iv-3">
                            <p class="iv-text"><%=item.warningCount%>个任务<span>即将过期</span></p>
                            <div class="iv-btn">
                                <div class="iv-back"></div>
                                <a href="/teacher/invite/activateteacher.vpage">查看详情</a>
                            </div>
                        </li>
                        <%}%>
                        <%if(item.mentoringCount > 0){%>
                        <li  class="iv-4">
                            <p class="iv-text"><%=item.mentoringCount%>个老师<span>等待帮助</span></p>
                            <div class="iv-btn">
                                <div class="iv-back"></div>
                                <a href="/teacher/invite/activateteacher.vpage?type=findnoauthenticationteacher">查看详情</a>
                            </div>
                        </li>
                        <%}%>
                    </ul>
                    <div class="w-clear"></div>
                </div>
                <div class="t-dynamic-last">
                    <a href="/teacher/invite/activateteacher.vpage" class="w-btn w-btn-mBlue w-btn-mini" style="background-color: #e1f0fc; border-color: #abc1d3;">查看详情</a>
                    <div class="w-clear"></div>
                </div>
            </div>
        </dd>
    </dl>
<%}%>
</script>
