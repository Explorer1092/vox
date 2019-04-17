<#if hasAuthAmb!false>
<div class="w-base">
    <div class="w-base-title">
        <h3>等级奖励规则</h3>
        <div class="w-base-right">
            <a href="/ambassador/collegeminute.vpage" target="_blank" style="color: #39f; line-height: 180%;">查看更多奖励</a>
        </div>
    </div>
    <div class="w-base-container">
        <#--//start-->
            <div class="ambassador-box" id="levelRuleMain">
                <div class="ambBox-inner">
                    <div class="ambBox-hd">
                        <span class="title">等级奖励</span>
                        <span>本月达到规定经验值，下月初奖励对应数量园丁豆！</span>
                    </div>
                    <div class="ambBox-mn">
                        <div class="ambBox-rankBar">
                            <ul>
                                <li>
                                <#if (level.isObservation)!false>
                                    <p class="name">被系统下任</p>
                                <#else>
                                    <p class="name">
                                        进入
                                        <a href="javascript:void(0)" class="cur-tips-main">
                                            <span class="w-blue">大使观察期</span>
                                            <span class="tips-box" style="width: 410px;">大使观察期：如果当月未达到规定的最低经验值，下个月将进入大使观察期；观察期内再次无法达到，将被系统取消大使资格。(1、2、7、8月除外)<span></span></span>
                                        </a>
                                    </p>
                                </#if>

                                    <div class="rBar rBar01">经验值40以下</div>
                                </li>
                                <li>
                                    <p class="name"><i class="medal-icon medal-icon-2"></i>铜牌大使＋30园丁豆</p>
                                    <div class="rBar rBar02">40－100</div>
                                </li>
                                <li>
                                    <p class="name"><i class="medal-icon medal-icon-1"></i>银牌大使＋100园丁豆</p>
                                    <div class="rBar rBar03">101－200</div>
                                </li>
                                <li>
                                    <p class="name"><i class="medal-icon medal-icon-0"></i>金牌大使＋200园丁豆</p>
                                    <div class="rBar rBar04">201－300</div>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
</#if>
                <div class="ambBox-inner">
                    <#if hasAuthAmb!false>
                    <div class="ambBox-hd">
                        <span class="title">经验值规则</span>
                    </div>
                    </#if>
                    <div class="ambBox-mn">
                        <div class="ambBox-bzWork">
                            <div class="left">
                                <span class="ds-icon ds-icon-8"></span>
                                <p class="name">布置作业</p>
                            </div>
                            <div class="right">
                                <ul>
                                    <li>
                                        <p class="title">电脑布置＋1／微信布置＋2</p>
                                        <div class="bar bar01">完成作业10-19人</div>
                                    </li>
                                    <li>
                                        <p class="title">电脑布置＋2／微信布置＋4</p>
                                        <div class="bar bar02">完成作业20-29人</div>
                                    </li>
                                    <li>
                                        <p class="title">电脑布置＋3／微信布置＋6</p>
                                        <div class="bar bar03">完成作业30及以上</div>
                                    </li>
                                </ul>
                                <p class="tips">使用电脑或微信布置作业，根据每班完成作业人数给予相应的奖励！</p>
                            </div>
                        </div>
                        <div class="ambBox-tab">
                            <ul>
                                <li>
                                    <span class="ds-icon ds-icon-0"></span>
                                    <div class="text">
                                        <p>唤醒老师<span class="font16">＋20</span></p>
                                    </div>
                                    <a href="/teacher/invite/activateteacher.vpage">去唤醒</a>
                                </li>
                                <li>
                                    <span class="ds-icon ds-icon-1"></span>
                                    <div class="text">
                                        <p class="describe">智慧课堂奖励5名学生<span class="font16">＋2</span></p>
                                        <p class="intro">（每月10经验封顶）</p>
                                    </div>
                                    <a href="/teacher/smartclazz/list.vpage">打开智慧课堂</a>
                                </li>
                                <li>
                                    <span class="ds-icon ds-icon-2"></span>
                                    <div class="text">
                                        <p class="describe">论坛发帖<span class="font16">＋1</span></p>
                                        <p class="intro">（每月5经验封顶）</p>
                                    </div>
                                    <a href="/ucenter/bbspartner.vpage?url=${ProductConfig.getBbsSiteBaseUrl()}/open.php?mod=register&teacherType=2" target="_blank">去论坛</a>
                                </li>
                                <li>
                                    <span class="ds-icon ds-icon-4"></span>
                                    <div class="text">
                                        <p class="describe">评论作业<span class="font16">＋1</span></p>
                                        <p class="intro">（每月5经验封顶）</p>
                                    </div>
                                </li>
                                <li class="data-helpAuth">
                                    <span class="ds-icon ds-icon-5"></span>
                                    <div class="text">
                                        <p>邀请老师认证<span class="font16">＋50</span></p>
                                    </div>
                                    <a href="javascript:void(0)">去邀请</a>
                                </li>
                                <li class="noBorder data-clickGetAmbWeChat">
                                    <span class="ds-icon ds-icon-7"></span>
                                    <div class="text">
                                        <p>关注大使微信<span class="font16">＋20</span></p>
                                    </div>
                                    <#if (!isBinding)!true>
                                        <a href="javascript:void(0);">获取二维码</a>
                                    </#if>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
<#if hasAuthAmb!false>
            <div class="t-show-box">
                <div class="w-turn-page-list">
                    <a href="javascript:void (0)" id="showLevelRule"><span class="text" data-show="展开" data-hide="收起">收起</span><span class="w-icon-arrow"></span></a>
                </div>
            </div>
            <script type="text/javascript">
                $(function(){
                    $("#showLevelRule").on({
                        click : function(){
                            var $thisText = $(this).find(".text");
                            var $levelRuleMain = $("#levelRuleMain");

                            if( $levelRuleMain.is(':visible') ){
                                $thisText.text( $thisText.attr("data-show") );
                                $levelRuleMain.hide();
                            }else{
                                $thisText.text( $thisText.attr("data-hide") );
                                $levelRuleMain.show();
                            }
                        }
                    });
                });
            </script>
        <#--end//-->
    </div>
</div>
</#if>