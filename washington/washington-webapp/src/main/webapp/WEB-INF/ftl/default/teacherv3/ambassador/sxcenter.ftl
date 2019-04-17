<#import "../../nuwa/teachershellv3.ftl" as temp />
<@temp.page showNav="hide">
    <#assign hasAuthAmb=false/><#--是否为正式大使-->
    <@app.css href="public/skin/project/schoolambassador/activateteacher.css" />

    <#--个人信息-->
    <div class="w-base">
        <div class="w-base-container">
            <div class="Amb-teacher-avatar">
                <dl class="ata-info">
                    <dt>
                        <img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>">
                        <p style="text-align: center; white-space: nowrap; text-overflow: ellipsis; overflow: hidden; height: 40px;">${(currentUser.profile.realname)!}</p>
                    </dt>
                    <dd>
                        <h3>
                            当前身份：<i class="t-card-SHI_XI"></i><span class="w-icon-md w-magR-10">实习大使</span>
                        </h3>
                        <h3>实习期：<span style="color: #999; font-size: 14px;">${sxStr!'-----'}</span></h3>
                    </dd>
                </dl>
                <dl class="ata-info ata-info-blue">
                    <dd>
                        <p><a class="w-btn w-btn-mini data-resignationSubmit" href="javascript:void(0);">申请辞任</a></p>
                        <p style="padding: 12px 0 0; ">
                            <#if (unReadCount gt 0)!false>
                                <a href="/ambassador/messagelist.vpage" class="w-blue" style="float: right; text-decoration: underline;">您有新的大使消息</a>
                            </#if>
                            <span style="font-size: 16px;">
                                <#if (dataMap.competitionCount gt 0)!false>
                                    本学科已有${dataMap.competitionCount!0}位
                                     <a href="javascript:void(0)" class="cur-tips-main">
                                         <span class="w-blue">预备大使</span>
                                         <span class="tips-box" style="width: 180px;">预备大使是同科目优秀老师，当现任大使下任时，优先上任。<span></span></span>
                                     </a>
                                <#else>
                                    同科目
                                     <a href="javascript:void(0)" class="cur-tips-main">
                                         <span class="w-blue">预备大使</span>
                                         <span class="tips-box" style="width: 180px;">预备大使是同科目优秀老师，当现任大使下任时，优先上任。<span></span></span>
                                     </a>
                                    已开放报名
                                </#if>
                            </span>
                        </p>
                    </dd>
                </dl>
                <div class="w-clear"></div>
            </div>
        </div>
    </div>

<div class="w-base">
    <#--module 1-->
    <div class="campusAm-1" style="height: 100px; border-bottom: 1px #dae6ee dashed;">
        <div class="campusHd">
            <em class="ico-1"></em>
            <em class="ico-2"></em>
            <div class="inner" style="font-size: 25px; position: relative;">
                <span style="position: absolute; right: 20px; font-size: 14px;">当前认证老师总数：${(dataMap.teacherList?size + 1)!1}</span>
                实习期任务
            </div>
        </div>
        <p style="color: #fa7252; text-align: center; font-size: 16px; padding: 5px;">实习期间，您必须完成以下三个任务，否则将变回普通老师。</p>
    </div>
    <#--module 2-->
    <h2 class="campusAm-title">任务一<span style="display: inline-block; margin-left: 50px;">学习大使体系基本概念</span></h2>
    <div class=" campusAm-2">
        <div class="campusModule" style="padding: 0;">
            <dl style="height: 120px; padding: 20px 0; ">
                <dt style="float: left; width: 250px; text-align: center;">
                    <img src="<@app.link href="public/skin/project/schoolambassador/images/school/amb-icon.png"/>" alt=""/>
                    <p style="padding: 10px 0;">校园大使</p>
                </dt>
                <dd style="margin: 0 100px 0 250px;">
                    <p style="font-size: 18px; margin-bottom: 20px; line-height: 150%;">恭喜你成为光荣的“校园大使”大家庭中的一员，下面我们一起学习一下校园大使的基本概念，要仔细奥！</p>
                    <p style="text-align: right;"><a href="/ambassador/collegeminute.vpage" class="w-btn w-btn-mini">点击学习</a></p>
                </dd>
            </dl>
        </div>
    </div>
    <#--module 3-->
    <h2 class="campusAm-title">任务二<span style="display: inline-block; margin-left: 50px;">本人使用以下所有功能</span> <span style="font-size: 14px; color: #999;">（功能图标变亮，表示已使用）</span></h2>
    <div class=" campusAm-2">
        <#if (dataMap.myInfo)?has_content>
            <#assign pageMyinfo = dataMap.myInfo/>
            <div class="campusModule">
                <div class="hd-pro">
                    <img src="<@app.avatar href='${pageMyinfo.img!}'/>" width="138" height="138">
                    <p class="name" style="line-height: 40px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${pageMyinfo.name!'----'}</p>
                </div>
                <div class="list clearfix">
                    <div style="height: 30px;"></div>
                    <div title="布置作业并检查，一个班有10名及以上学生完成" class="mod mod-01 <#if (pageMyinfo.AMBASSADOR_MENTOR_HOMEWORK)??><#if pageMyinfo.AMBASSADOR_MENTOR_HOMEWORK == "F_Y" || pageMyinfo.AMBASSADOR_MENTOR_HOMEWORK == "Y"><#else>mod-01-gray</#if><#else>mod-01-gray</#if>">
                        <div class="pic">
                            <#if (pageMyinfo.AMBASSADOR_MENTOR_HOMEWORK)??><#if pageMyinfo.AMBASSADOR_MENTOR_HOMEWORK == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">作业</div>
                    </div>

                    <div title="在教师论坛发帖or回帖" class="mod mod-07 <#if (pageMyinfo.AMBASSADOR_MENTOR_BBS)??><#if pageMyinfo.AMBASSADOR_MENTOR_BBS == "F_Y" || pageMyinfo.AMBASSADOR_MENTOR_BBS == "Y"><#else>mod-07-gray</#if><#else>mod-07-gray</#if>">
                        <div class="pic">
                            <#if (pageMyinfo.AMBASSADOR_MENTOR_BBS)??><#if pageMyinfo.AMBASSADOR_MENTOR_BBS == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">论坛<br/><a class="w-blue" href="/ucenter/partner.vpage?url=${ProductConfig.getBbsSiteBaseUrl()}/open.php?mod=register&teacherType=2" target="_blank" style="margin-top: -10px; display: block;">去论坛</a></div>
                    </div>
                    <div class="mod mod-11 <#if (pageMyinfo.AMBASSADOR_BIND_WECHAT)??><#if pageMyinfo.AMBASSADOR_BIND_WECHAT == "F_Y" || pageMyinfo.AMBASSADOR_BIND_WECHAT == "Y"><#else>mod-11-gray</#if><#else>mod-11-gray</#if>" style="width: 100px; text-align: center;">
                        <div class="pic">
                            <#if (pageMyinfo.AMBASSADOR_BIND_WECHAT)??><#if pageMyinfo.AMBASSADOR_BIND_WECHAT == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <#if (pageMyinfo.AMBASSADOR_BIND_WECHAT)??>
                            <#if pageMyinfo.AMBASSADOR_BIND_WECHAT == "F_Y" || pageMyinfo.AMBASSADOR_BIND_WECHAT == "Y"><#else>
                                <div class="hd">绑定大使微信<br/><a href="javascript:void(0);" class="data-clickGetAmbWeChat w-blue" style="margin-top: -10px; display: block;">点击获取二维码</a></div>
                            </#if>
                        <#else>
                            <div class="hd">绑定大使微信<br/><a href="javascript:void(0);" class="data-clickGetAmbWeChat w-blue" style="margin-top: -10px; display: block;">点击获取二维码</a></div>
                        </#if>
                    </div>
                    <p style="font-size: 12px; clear: both; color: #fb692f;">注：对于作业和测验，一个班有10名及以上学生完成，才显示使用</p>
                </div>
            </div>
        </#if>
    </div>

    <h2 class="campusAm-title">任务三    实习大使经验值达到40 &nbsp;&nbsp;&nbsp; 经验值明细：<a href="javascript:void(0);" class="w-blue data-clickScore">${(dataMap.totalScore)!0}</a>（经验值规则如下）</h2>
    <div class=" campusAm-2">
    <#--//list start-->
        <#include "level-rule.ftl"/>
    <#--end//-->
    <#--<div class="campusFt"><div class="prev"></div><span><em>1</em>/10</span><div class="next"></div></div>-->
        <#if (dataMap.teacherList?size gt 4)!false>
            <div class="t-show-box data-teacherCertification">
                <div class="w-turn-page-list">
                    <a href="javascript:void (0)">查看更多<span class="w-icon-arrow"></span></a>
                </div>
            </div>
        </#if>
    </div>
</div>
    <#include "template.ftl"/>
<script type="text/javascript">
    $(function(){
        if(!$17.getCookieWithDefault("sxcenter")){
            $17.setCookieOneDay("sxcenter", "30", 30);
            $17.alert("大使导师团为您准备了3个小任务，<br/>分分钟帮您成为好大使，让我们开始吧。");
        }
    });
</script>
</@temp.page>