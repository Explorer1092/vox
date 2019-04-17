<#import "../../nuwa/teachershellv3.ftl" as temp />
<@temp.page showNav="hide">
    <@app.css href="public/skin/project/ambassador/newamb.css" />
    <div class="m-main" style="padding-top: 15px;">
    <#if (currentTeacherDetail.schoolAmbassador)!false>
        <div class="w-base">
            <div class="w-base-container">
                <div class="Amb-teacher-avatar">
                    <dl class="ata-info w-magT-10">
                        <dt>
                            <img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>">
                        </dt>
                        <dd>
                            <h3><#if (currentTeacherDetail.subject)?? && (currentTeacherDetail.subject) =='ENGLISH'>英语老师 <#else>数学老师 </#if>：${(currentUser.profile.realname)!}</h3>
                            <p>
                                <i class="t-card-${(ambassadorLevel.level)!'SHI_XI'}"></i><span class="w-icon-md w-magR-10">${(ambassadorLevel.level.description)!'实习大使'}</span>
                            </p>
                        </dd>
                    </dl>
                    <dl class="ata-info ata-info-blue">
                        <dt>
                            <i class="Amb-activety-icon"></i>
                        </dt>
                        <dd>
                            <p class="w-magB-10"><span class="w-orange">恭喜您荣获校园大使</span>
                                <#if .now gt '2015-09-01 00:00:00'?datetime('yyyy-MM-dd HH:mm:ss') && ((currentTeacherWebGrayFunction.isAvailable("Ambassador", "Competition"))!false)>
                                （任期：${.now?string("yyyy")}年${.now?string("MM")}月01日至${.now?string("MM")}月<span class="currentMonthCount"></span>日）
                                </#if>
                            </p>
                            <p><a style="width: 140px;" class="w-btn w-btn-mini" href="http://help.17zuoye.com/?p=787" target="_blank">查看校园大使福利</a></p>
                        </dd>
                    </dl>
                    <div class="w-clear"></div>
                </div>
            </div>
        </div>
        <!--w-base本校情况-->
        <div class="Amb-public-colum-box">
            <div class="w-base">
                <div class="w-base-title">
                    <h3>本校情况</h3>
                </div>
                <div class="w-base-container">
                    <div class="Amb-activeAuth-box" style="height: 222px;">
                        <dl style="height: 130px;">
                            <dt><i class="Amb-icon-school"></i></dt>
                            <dd>
                                <p style="padding: 10px 0 8px;">
                                    <a href="http://help.17zuoye.com/?p=779" target="_blank" class="w-blue">本月校园活跃度： <span>${campusActiveLevel!0}%</span></a>
                                    <span style="display: inline-block; margin-left: 10px;">上月校园活跃度：<span class="text_red">${campusActiveLevelLastMonth!0}%</span></span>
                                </p>
                                <p>
                                    本月活跃认证老师：<span class="text_red">${activeCount!0}</span>
                                    <span style="display: inline-block; margin-left: 10px;">当前认证老师总数：<span class="text_red">${(allTeacherList?size)!0}</span></span>
                                </p>
                            </dd>
                        </dl>
                        <div class="Amb-public-btn">
                            <#--<a class="w-btn w-btn-well w-btn-green v-clickAmbChangePopup" href="javascript:void(0);">大使协助换班</a>-->
                            <a class="w-btn w-btn-well" href="/ambassador/schoolteachermgn.vpage">管理本校老师</a>
                        </div>
                    </div>
                </div>
            </div>
            <!--通知中心-->
            <div class="w-base">
                <div class="w-base-title">
                    <h3>通知中心</h3>
                </div>
                <div class="w-base-container">
                    <div class="Amb-journal-list">
                        <ul>
                            <#if messageList?has_content>
                                <#list messageList as mesg>
                                <#if mesg_index lt 4>
                                <li style="cursor: pointer;" onclick="checkMsg();" class="info">
                                    <span class="date">${mesg.createTime?number_to_datetime}</span>
                                    <span class="tag ${(mesg.status == "UNREAD")?string("w-megNew-icon", "w-megNew-icon-noSee")}"></span>
                                    <div class="content" >
                                        ${mesg.payload}
                                    </div>
                                </li>
                                </#if>
                                </#list>
                            <#else>
                                <li style="border: none; text-align: center; padding: 60px 0 0;">
                                    暂无通知
                                </li>
                            </#if>
                        </ul>
                    </div>
                    <div class="Amb-public-btn">
                        <a class="w-btn w-btn-well" href="/ambassador/messagelist.vpage">查看更多消息</a>
                        ${(unReadCount gt 0)?string('<span class="w-redTips-icon w-magL-10">${unReadCount!0}条新消息</span>', '')}
                    </div>
                </div>
            </div>
            <!--大使学院-->
            <div class="w-base">
                <div class="w-base-title">
                    <h3>大使学院</h3>
                </div>
                <div class="w-base-container">
                    <div class="amb-blue-color">完成大使学院全部课程可得 <span class="w-orange">100</span> 园丁豆！</div>
                    <div class="Amb-complain-box" style="padding-top: 10px; border-bottom: 1px solid #dae6ee;">
                        <p class="amc-info">我的学院进度：</p>
                        <div class="amb-line-bar">
                            <div class="line" style="width: ${(20 * (recordType + 1))!0}%;">${(20 * (recordType + 1))!0}%</div>
                        </div>
                        <p class="amc-info w-magB-10">大使学院一共5个阶段，我已完成${(recordType + 1)!0}个阶段 </p>
                    </div>
                    <div class="Amb-public-btn w-marB-10">
                        <a class="w-btn w-btn-well w-btn-disabled" href="javascript:void(0);">进入大使学院</a>
                        <#--<span class="w-redTips-icon w-magL-10">有新课程!</span>-->
                    </div>
                </div>
            </div>
            <div class="w-clear"></div>
        </div>
    </#if>
    </div>
<script type="text/javascript">
    $(".currentMonthCount").text( $17.getMonthTotalDay() );
    function checkMsg(){
        window.location.href = "/ambassador/messagelist.vpage";
    }

    setTimeout(function(){
        location.href = "/";
    }, 200);
</script>
</@temp.page>