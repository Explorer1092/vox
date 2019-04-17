<#import "../../../layout/project.module.student.ftl" as temp />
<@temp.page title="队长争夺战" header="show">
<@app.css href="public/skin/project/captainbattle/skin.css" />
<div class="main">
    <div class="head">
        <div class="inner">
            <#--<#if ftlmacro.isInJuneForInvite>
                <a class="push-start-btn push-start-btn-disabled" href="javascript:void (0);">开始抢夺</a>
            <#else>
                <a id="startBut" class="push-start-btn" href="javascript:void (0);">开始抢夺</a>
            </#if>-->
            <a class="push-start-btn push-start-btn-disabled" href="javascript:void (0);">开始抢夺</a>
            <a id="ruleBut" class="push-rule-btn" href="javascript:void (0);">查看规则</a>
            <!--规则介绍-->
            <div class="push-rule-alert">
                <p class="pr-title">规则介绍</p>
                <p><span class="num">1</span>抢夺一名队员需花费5学豆; 队长排行数量一样按时间排序;</p>
                <p><span class="num">2</span>一名被抢夺的同学在24小时内完成作业，队长即可获得20学豆;</p>
                <p><span class="num">3</span>发出抢夺后，如果24小时内该队员未做作业，则该抢夺失效;</p>
                <p><span class="num">4</span>活动结束后前3名队长，可分别额外获得80/50/20颗学豆;</p>
                <p><span class="num">5</span>队员数少于3人，不获得额外奖励；每天最多发出10次抢夺。</p>
            </div>
        </div>
    </div>
    <div class="content">
        <div class="inner">
            <div class="push-revaState-box">
                <div class="push-state-box">
                    <div class="title">队长排行榜 <span style="font-size: 12px;">(每天更新)</span> </div>
                    <div class="push-table-box">
                        <table>
                            <tbody>
                                <tr>
                                    <td style="background-color: #FFF6BD; width: 144px;">排名</td>
                                    <td style="background-color: #FFF6BD; width: 90px;">姓名</td>
                                    <td style="background-color: #FFF6BD;">队员数量</td>
                                </tr>
                                <#if rankList?? && rankList?size gt 0>
                                    <#list rankList as rl>
                                        <tr>
                                            <td class="first_${rl_index + 1}" style="width: 144px;"><#if rl_index gt 2>NO.${rl_index + 1}</#if> </td>
                                            <td style="width: 90px;">${(rl.userName)!''}</td>
                                            <td> ${(rl.COUNT)!0}名队员</td>
                                        </tr>
                                    </#list>
                                <#else>
                                    <tr>
                                        <td colspan="3">还没有人登上榜单哦，各位队长快去抢夺队员吧！</td>
                                    </tr>
                                </#if>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="push-state-box">
                    <div class="title">我的抢夺进度</div>
                    <#if ftlmacro.isInJuneForInvite>
                        <#if (myGrabInfo.rewardFlag)!false>
                            <!--恭喜你：-->
                            <div class="title-small title-succeed">
                                <span class="red">恭喜你！</span>
                                <p  class="st">
                                    因为你杰出的队长魅力，被选为
                                    <#if (myGrabInfo.rank)??>
                                        <#if myGrabInfo.rank == 1>
                                            领袖
                                        <#elseif myGrabInfo.rank == 2>
                                            超凡
                                        <#elseif myGrabInfo.rank == 3>
                                            精英
                                        </#if>
                                    </#if>
                                    队长！额外奖励你${(myGrabInfo.rewardCoin)!0}学豆，还有班级空间专属气泡哦！快去签到炫一下吧~
                                </p>
                            </div>
                        <#else>
                            <#if (myGrabInfo.rank)?? && (myGrabInfo.rank lte 3)>
                                <!--亲爱的同学-->
                                <div class="title-small title-info">
                                    亲爱的同学：<br>
                                    <p class="st">
                                        很遗憾，你在”队长争夺战“中队员数少于3人，没有拿到额外奖励，但你还可以继续唤醒同学，去挣得更多学豆哦
                                    </p>
                                </div>
                            <#else>
                                <!--亲爱的同学-->
                                <div class="title-small title-info">
                                    亲爱的同学：<br>
                                    <p class="st">
                                        很遗憾，你在“队长抢夺战”中没有进入前三名，没有拿到额外奖励，但你还可以继续唤醒同学，去挣得更多学豆哦
                                    </p>
                                </div>
                            </#if>
                        </#if>
                    <#else>
                        <div class="title-small">
                            抢夺<span class="red">${(myGrabInfo.totalCount)!0}</span>次，
                            成功抢夺队员<span class="red">${(myGrabInfo.successCount)!0}</span>人
                            <div>快去催同学在倒计时结束前做作业吧！</div>
                        </div>
                    </#if>

                    <!--亲爱的同学-->
                    <#macro countdown endDate studentId>
                        <div class="count-timeItem-box">
                            <span class="out-time t_h_${studentId}">00</span>时
                            <span class="out-time t_m_${studentId}">00</span>分
                            <span class="out-time t_s_${studentId}">00</span>秒
                            <span class="unfinished">未完成</span>
                        </div>
                        <script type="text/javascript">
                            var NowTimeTemp_${studentId} = new Date("${.now}").getTime();
                            function countdown() {
                                var EndTime = new Date('${endDate}');
                                    NowTimeTemp_${studentId} += 1000;
                                var t = EndTime.getTime() - NowTimeTemp_${studentId};
                                var d = Math.floor(t / 1000 / 60 / 60 / 24);
                                var h = Math.floor(t / 1000 / 60 / 60 % 24);
                                var m = Math.floor(t / 1000 / 60 % 60);
                                var s = Math.floor(t / 1000 % 60);

                                if (s < 0) {
                                    return false;
                                }

                                if (d < 10) {
                                    d = "0" + d;
                                }
                                if (h < 10) {
                                    h = "0" + h;
                                }
                                if (m < 10) {
                                    m = "0" + m;
                                }
                                if (s < 10) {
                                    s = "0" + s;
                                }
                                $('.t_h_'+${studentId}).html(h);
                                $('.t_m_'+${studentId}).html(m);
                                $('.t_s_'+${studentId}).html(s);
                            }

                            countdown();
                            setInterval(countdown,1000);
                        </script>
                    </#macro>

                    <div class="push-table-box push-table-small-box">
                        <table>
                            <#if (myGrabInfo.detailList)?? && myGrabInfo.detailList?size gt 0>
                                <#list myGrabInfo.detailList as myGrabInfo>
                                    <tr>
                                        <td style="width: 130px;">
                                            <i class="ico"><img src="<@app.avatar href="${myGrabInfo.imgUrl}"/>" style="width: 30px; height: 30px" alt=""/></i>
                                            <span style="width: 70px; text-align: left; vertical-align: middle; display: inline-block; text-overflow: ellipsis; white-space: nowrap; overflow: hidden;">${myGrabInfo.userName!''}</span>
                                        </td>
                                        <td style="text-indent: 0; text-align: center;">
                                            <#if ftlmacro.isInJuneForInvite>
                                                作业状态：<#if myGrabInfo.success>已完成<#else>未完成</#if>
                                            <#else>
                                                <#if myGrabInfo.success>
                                                    作业状态：已完成
                                                <#else>
                                                    <@countdown endDate= (myGrabInfo.endDate)!'2015/06/10 00:00:00' studentId = myGrabInfo.userId />
                                                </#if>
                                            </#if>
                                        </td>
                                    </tr>
                                </#list>
                            <#else>
                                <tr>
                                    <td colspan="2">亲爱的队长，你还没有发出过抢夺哦~</td>
                                </tr>
                            </#if>
                        </table>
                    </div>
                </div>
            </div>
            <div class="push-font-box">
                <p>温馨提示：</p>
                <p>1、主办方将对所有参与者进行严格审核，任何恶意注册、重复注册、虚假信息等均视为舞弊，一经查出，除取消获奖资格外，还将从系统中扣除所有学豆和奖品兑换资格；</p>
                <p>2、 一起作业网拥有对此次活动的最终解释。</p>
            </div>
        </div>
    </div>
</div>
<!--end//-->
<div id="footerPablic"></div>

<!--抢夺队员-->

<script type="text/html" id="studentsListBox">
    <div class="push-name-item-alert">
        <div class="pn-title"><a class="close" href="javascript:$.prompt.close(function(){});"></a>抢夺队员</div>
        <div class="push-name-item-inner">
            <div class="ps-title">付出5学豆，获得20学豆！前三名队长还有最高80学豆的额外奖励~</div>
            <div class="content-list">
                <ul id="studentsList">
                    <#if grabList?? && grabList?size gt 0>
                        <#list grabList as grabList>
                            <li data-user_id="${grabList.userId!''}">
                                <div class="avctor"><img src="<@app.avatar href="${grabList.userAvatar!''}"/>" alt=""/></div>
                                <p class="name">${grabList.userName!''}</p>
                                <i class="arrow"></i>
                            </li>
                        </#list>
                    <#else>
                        <div>队员们还没有准备好，请改天再来看看吧~(^_^)</div>
                    </#if>
                </ul>
            </div>
            <div class="pn-info">小提示：抢夺一名队员需要扣除5学豆哦；邀请该队员尽快上线做作业吧！<div class="tipBox"></div></div>
            <div class="pn-btn"><a id="submitBut" href="javascript:void (0);" class="disabled"></a></div>
        </div>
    </div>
</script>

<script type="text/javascript">

    $(function () {
        //
        $('#startBut').on('click',function(){
            $.prompt(template("studentsListBox",{}),{
                prefix : "null-popup",
                buttons : {},
                classes : {
                    fade: 'jqifade'

                },
                close : function(){
                    location.reload();
                }
            });

            $17.tongji('队长争夺战','宣传页','开始抢夺按钮');
        });

        //选择同学
        var userId, tipBox = $('.tipBox') ;
        $(document).on('click', '#studentsList li', function () {
            var $this = $(this);
            $this.addClass('active').siblings().removeClass('active');
            $('#submitBut').removeClass('disabled');
            userId = $this.data('user_id');
            $17.tongji('队长争夺战','宣传页','选择队员');
        });

        //确认抢夺
        $(document).on('click','#submitBut',function(){
            var $this = $(this);
            if($this.hasClass('disabled')){return false;}

            $.post('/student/invite/grabmember.vpage',{memberId : userId},function(data){
                if(data.success){
                    $("#studentsList li.active").remove();
                    $('#submitBut').addClass('disabled');
                }
                $this.parent().siblings('div').find('.tipBox').text(data.info);
                setTimeout(function(){$this.parent().siblings('div').find('.tipBox').text('')},3000);
            });

            $17.tongji('队长争夺战','宣传页','确认抢夺按钮');
        });

        //查看规则
        $('#ruleBut').hover(function(){
            $(".push-rule-alert").show();
            $17.tongji('队长争夺战','宣传页','查看规则');
        },function(){
            $(".push-rule-alert").hide();
        });
    });
</script>
</@temp.page>