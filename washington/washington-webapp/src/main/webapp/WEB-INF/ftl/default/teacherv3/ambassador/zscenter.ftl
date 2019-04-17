<#import "../../nuwa/teachershellv3.ftl" as temp />
<@temp.page showNav="hide">
    <@app.css href="public/skin/project/schoolambassador/activateteacher.css" />
   <#--是否为正式大使-->
<style>
    .active-down{width:917px;height:635px;margin:20px 0 70px;background-image:url(<@app.link href="public/skin/project/schoolambassador/images/xydsdown.jpg"/>);background-repeat:no-repeat;}
</style>
    <div class="active-down">
    </div>
<#--
    <div style="margin-bottom: 15px;padding: 10px 20px;background-color:#feef94;">
        <p style="padding-bottom:10px;">尊敬的校园大使：</p>
        <p style="padding-bottom:10px;">您好！感谢您对一起作业平台的使用和信任，以及在校园大使岗位上的努力和付出。由于一起作业平台整体的产品线升级，我们将对现有的校园大使产品进行调整。2017年5月12日24时后，校园大使的相关任务及奖励将予以暂停，同时会下线校园大使相关的功能，但您的大使等级还会继续保留，还请您谅解。
            再次感谢您一如既往的理解和支持！</p>
        <p>一起作业运营团队</p>
    </div>

    &lt;#&ndash;个人信息&ndash;&gt;
    <div class="w-base">
        <div class="w-base-container">
            <div class="Amb-teacher-avatar">
                <dl class="ata-info">
                    <dt>
                        <img src="<@app.avatar href='${currentUser.fetchImageUrl()}'/>">
                    </dt>
                    <dd>
                        <h3>${(currentUser.profile.realname)!}</h3>
                        <p>
                            <#if (level.isObservation)!false>
                                本月处于
                                <a href="javascript:void(0)" class="cur-tips-main">
                                    <span class="w-blue">大使观察期</span>
                                    <span class="tips-box" style="width: 410px;">大使观察期：如果当月未达到规定的最低经验值，下个月将进入大使观察期；观察期内再次无法达到，将被系统取消大使资格。(1、2、7、8月除外)<span></span></span>
                                </a>
                            <#else>
                                本月荣誉：
                                <i class="t-card-${(level.level)!'SHI_XI'}"></i><span class="w-icon-md">${(level.level.description)!'实习大使'}</span>
                            </#if>
                            &nbsp;&nbsp;
                            本月经验值：<a href="javascript:void(0);" class="data-clickScore w-blue">${(dataMap.totalScore)!0}</a>
                            &lt;#&ndash;&nbsp;&nbsp;<a href="javascript:void(0);" class="curmonth-rule data-clickLlValue"><i></i> 规则</a>&ndash;&gt;
                        </p>
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

    &lt;#&ndash;等级奖励规则&ndash;&gt;
    <#include "level-rule.ftl"/>

    <div class="w-base">
        <h2 class="campusAm-title" style="position: relative; z-index: 3;">
            <#if (dataMap.teacherList?size gt 0)!false>
            <p style="margin: -3px 0 0; float: right;">
                <a href="javascript:void(0);" class="w-btn w-btn-mini w-btn-red data-keyReminder cur-tips-main" style="margin:0 30px; width: 80px;">一键提醒<span class="tips-box" style="width: 180px;">提醒本月所有未布置有效作业的老师进行布置<span></span></span></a>
                <a href="javascript:void(0);" class="w-btn w-btn-mini data-likeKeyPoint cur-tips-main" style="width: 80px;">一键点赞<span class="tips-box" style="width: 180px; left: -120px;">给本月已布置有效作业的老师点赞<span style="left: 160px;"></span></span></a>
            </p>
            </#if>
            <span style="display: inline-block; width: 350px; ">本月已布置
                 <a href="javascript:void(0)" class="cur-tips-main">
                     <span class="w-blue">有效作业</span>
                     <span class="tips-box" style="top:30px; width: 290px;">有效作业:每次作业，一个班有10名及以上学生完成。<span></span></span>
                 </a>老师：${(dataMap.effTotalCount)!0}
            </span>
            <span style="display: inline-block; width: 280px;">当前同科认证老师：${(dataMap.teacherList?size + 1)!1}</span>
        </h2>
        <div class=" campusAm-2">
        <#if (dataMap.myInfo)?has_content>
            <#assign pageMyinfo = dataMap.myInfo/>
            <div class="campusModule">
                <div class="hd-pro">
                    <img src="<@app.avatar href='${pageMyinfo.img!}'/>" width="138" height="138">
                    <p class="name" style="line-height: 40px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${pageMyinfo.name!'----'}</p>
                    &lt;#&ndash;<p><a href="javascript:void(0);" style="width: 140px;" class="w-btn w-btn-mini data-cancelAuth" data-type="2" data-userid="${pageMyinfo.userId!0}" data-username="${pageMyinfo.name!0}">申请取消该老师认证 </a></p>&ndash;&gt;
                </div>
                <div class="list clearfix">
                    <div style="color: #4e5656; font-size: 18px; background-color: #edf5fa; padding: 15px;">
                        <span style="width: 280px; display: inline-block;">本月布置有效作业次数：${pageMyinfo.effCount!'0'}</span>
                        <#if (pageMyinfo.overCount gt 0)!false><span>已有${pageMyinfo.overCount!0}名老师超过了我</span></#if>
                    </div>
                    <h4 style="text-align: center; font-size: 20px;color: #4e5656; padding: 20px; font-weight: normal;">本月功能使用情况</h4>
                    <div title="布置作业并检查，一个班有10名及以上学生完成" class="mod mod-01 <#if (pageMyinfo.AMBASSADOR_MENTOR_HOMEWORK)??><#if pageMyinfo.AMBASSADOR_MENTOR_HOMEWORK == "F_Y" || pageMyinfo.AMBASSADOR_MENTOR_HOMEWORK == "Y"><#else>mod-01-gray</#if><#else>mod-01-gray</#if>">
                        <div class="pic">
                            <#if (pageMyinfo.AMBASSADOR_MENTOR_HOMEWORK)??><#if pageMyinfo.AMBASSADOR_MENTOR_HOMEWORK == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">作业</div>
                    </div>
                    <div title="布置测验并检查，一个班有10名及以上学生完成" class="mod mod-02 <#if (pageMyinfo.AMBASSADOR_MENTOR_QUIZ)??><#if pageMyinfo.AMBASSADOR_MENTOR_QUIZ == "F_Y" || pageMyinfo.AMBASSADOR_MENTOR_QUIZ == "Y"><#else>mod-02-gray</#if><#else>mod-02-gray</#if>">
                        <div class="pic">
                            <#if (pageMyinfo.AMBASSADOR_MENTOR_QUIZ)??><#if pageMyinfo.AMBASSADOR_MENTOR_QUIZ == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">测验</div>
                    </div>
                    <div title="微信布置作业并检查，一个班有10名及以上学生完成" class="mod mod-03 <#if (pageMyinfo.AMBASSADOR_MENTOR_WECHAT_HOMEWORK)??><#if pageMyinfo.AMBASSADOR_MENTOR_WECHAT_HOMEWORK == "F_Y" || pageMyinfo.AMBASSADOR_MENTOR_WECHAT_HOMEWORK == "Y"><#else>mod-03-gray</#if><#else>mod-03-gray</#if>">
                        <div class="pic">
                            <#if (pageMyinfo.AMBASSADOR_MENTOR_WECHAT_HOMEWORK)??><#if pageMyinfo.AMBASSADOR_MENTOR_WECHAT_HOMEWORK == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">微信布置</div>
                    </div>
                    <div title="使用智慧课堂奖励5名及以上学生" class="mod mod-04 <#if (pageMyinfo.AMBASSADOR_MENTOR_SMART_CLAZZ)??><#if pageMyinfo.AMBASSADOR_MENTOR_SMART_CLAZZ == "F_Y" || pageMyinfo.AMBASSADOR_MENTOR_SMART_CLAZZ == "Y"><#else>mod-04-gray</#if><#else>mod-04-gray</#if>">
                        <div class="pic">
                            <#if (pageMyinfo.AMBASSADOR_MENTOR_SMART_CLAZZ)??><#if pageMyinfo.AMBASSADOR_MENTOR_SMART_CLAZZ == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">智慧课堂</div>
                    </div>
                    <div title="使用布置作业抽大奖抽奖" class="mod mod-05 <#if (pageMyinfo.AMBASSADOR_MENTOR_DO_LOTTERY)??><#if pageMyinfo.AMBASSADOR_MENTOR_DO_LOTTERY == "F_Y" || pageMyinfo.AMBASSADOR_MENTOR_DO_LOTTERY == "Y"><#else>mod-05-gray</#if><#else>mod-05-gray</#if>">
                        <div class="pic">
                            <#if (pageMyinfo.AMBASSADOR_MENTOR_DO_LOTTERY)??><#if pageMyinfo.AMBASSADOR_MENTOR_DO_LOTTERY == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">抽奖</div>
                    </div>
                    <div title="在奖品中心兑换奖品" class="mod mod-06 <#if (pageMyinfo.AMBASSADOR_MENTOR_REWARD_ORDER)??><#if pageMyinfo.AMBASSADOR_MENTOR_REWARD_ORDER == "F_Y" || pageMyinfo.AMBASSADOR_MENTOR_REWARD_ORDER == "Y"><#else>mod-06-gray</#if><#else>mod-06-gray</#if>">
                        <div class="pic">
                            <#if (pageMyinfo.AMBASSADOR_MENTOR_REWARD_ORDER)??><#if pageMyinfo.AMBASSADOR_MENTOR_REWARD_ORDER == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">兑换奖品</div>
                    </div>
                    <div title="在教师论坛发帖or回帖" class="mod mod-07 <#if (pageMyinfo.AMBASSADOR_MENTOR_BBS)??><#if pageMyinfo.AMBASSADOR_MENTOR_BBS == "F_Y" || pageMyinfo.AMBASSADOR_MENTOR_BBS == "Y"><#else>mod-07-gray</#if><#else>mod-07-gray</#if>">
                        <div class="pic">
                            <#if (pageMyinfo.AMBASSADOR_MENTOR_BBS)??><#if pageMyinfo.AMBASSADOR_MENTOR_BBS == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">论坛</div>
                    </div>
                    <div title="检查作业时，为学生写评语" class="mod mod-08 <#if (pageMyinfo.AMBASSADOR_MENTOR_COMMENT)??><#if pageMyinfo.AMBASSADOR_MENTOR_COMMENT == "F_Y" || pageMyinfo.AMBASSADOR_MENTOR_COMMENT == "Y"><#else>mod-08-gray</#if><#else>mod-08-gray</#if>">
                        <div class="pic">
                            <#if (pageMyinfo.AMBASSADOR_MENTOR_COMMENT)??><#if pageMyinfo.AMBASSADOR_MENTOR_COMMENT == "F_Y"><em></em></#if><#else><em></em></#if>
                        </div>
                        <div class="hd">评语</div>
                    </div>
                    <#if (currentTeacherDetail.subject == "ENGLISH")!false>
                        <div title="布置一次带有阅读绘本的作业" class="mod mod-10 <#if (pageMyinfo.AMBASSADOR_MENTOR_READING)??><#if pageMyinfo.AMBASSADOR_MENTOR_READING == "F_Y" || pageMyinfo.AMBASSADOR_MENTOR_READING == "Y"><#else>mod-10-gray</#if><#else>mod-10-gray</#if>">
                            <div class="pic">
                                <#if (pageMyinfo.AMBASSADOR_MENTOR_READING)??><#if pageMyinfo.AMBASSADOR_MENTOR_READING == "F_Y"><em></em></#if><#else><em></em></#if>
                            </div>
                            <div class="hd">绘本作业</div>
                        </div>
                    </#if>
                    <p style="font-size: 12px; clear: both; color: #fb692f;">注：对于作业和测验，一个班有10名及以上学生完成，才显示使用</p>
                </div>
            </div>
        </#if>
        </div>
        <h2 class="campusAm-title" style="padding: 0; height: 35px; overflow: hidden;">&lt;#&ndash;content&ndash;&gt;</h2>
        <div class=" campusAm-2">
            &lt;#&ndash;//list start&ndash;&gt;
            <#list dataMap.teacherList as t>
                <div class="campusModule" <#if t_index gt 4>style="display: none;"</#if>>
                    <div class="hd-pro">
                        <img src="<@app.avatar href='${t.img!}'/>" width="138" height="138">
                        <p class="name" style="line-height: 40px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${t.name!'----'}</p>
                        <p><a href="javascript:void(0);" style="width: 140px;" class="w-btn w-btn-mini data-cancelorstopAuth" data-type="2" data-userid="${t.userId!0}" data-username="${t.name!0}">取消/暂停该认证老师 </a></p>
                    </div>
                    <div class="list clearfix">
                        <div style="color: #4e5656; font-size: 18px; background-color: #edf5fa; padding: 15px;">
                            本月布置有效作业次数：${t.effCount!'0'}
                        </div>
                        <h4 style="text-align: center; font-size: 20px;color: #4e5656; padding: 20px; font-weight: normal;">本月功能使用情况</h4>
                        <div title="布置作业并检查，一个班有10名及以上学生完成" class="mod mod-01 <#if (t.AMBASSADOR_MENTOR_HOMEWORK)??><#if t.AMBASSADOR_MENTOR_HOMEWORK == "F_Y" || t.AMBASSADOR_MENTOR_HOMEWORK == "Y"><#else>mod-01-gray</#if><#else>mod-01-gray</#if>">
                            <div class="pic">
                                <#if (t.AMBASSADOR_MENTOR_HOMEWORK)??><#if t.AMBASSADOR_MENTOR_HOMEWORK == "F_Y"><em></em></#if><#else><em></em></#if>
                            </div>
                            <div class="hd">作业</div>
                        </div>
                        <div title="布置测验并检查，一个班有10名及以上学生完成" class="mod mod-02 <#if (t.AMBASSADOR_MENTOR_QUIZ)??><#if t.AMBASSADOR_MENTOR_QUIZ == "F_Y" || t.AMBASSADOR_MENTOR_QUIZ == "Y"><#else>mod-02-gray</#if><#else>mod-02-gray</#if>">
                            <div class="pic">
                                <#if (t.AMBASSADOR_MENTOR_QUIZ)??><#if t.AMBASSADOR_MENTOR_QUIZ == "F_Y"><em></em></#if><#else><em></em></#if>
                            </div>
                            <div class="hd">测验</div>
                        </div>
                        <div title="微信布置作业并检查，一个班有10名及以上学生完成" class="mod mod-03 <#if (t.AMBASSADOR_MENTOR_WECHAT_HOMEWORK)??><#if t.AMBASSADOR_MENTOR_WECHAT_HOMEWORK == "F_Y" || t.AMBASSADOR_MENTOR_WECHAT_HOMEWORK == "Y"><#else>mod-03-gray</#if><#else>mod-03-gray</#if>">
                            <div class="pic">
                                <#if (t.AMBASSADOR_MENTOR_WECHAT_HOMEWORK)??><#if t.AMBASSADOR_MENTOR_WECHAT_HOMEWORK == "F_Y"><em></em></#if><#else><em></em></#if>
                            </div>
                            <div class="hd">微信布置</div>
                        </div>
                        <div title="使用智慧课堂奖励5名及以上学生" class="mod mod-04 <#if (t.AMBASSADOR_MENTOR_SMART_CLAZZ)??><#if t.AMBASSADOR_MENTOR_SMART_CLAZZ == "F_Y" || t.AMBASSADOR_MENTOR_SMART_CLAZZ == "Y"><#else>mod-04-gray</#if><#else>mod-04-gray</#if>">
                            <div class="pic">
                                <#if (t.AMBASSADOR_MENTOR_SMART_CLAZZ)??><#if t.AMBASSADOR_MENTOR_SMART_CLAZZ == "F_Y"><em></em></#if><#else><em></em></#if>
                            </div>
                            <div class="hd">智慧课堂</div>
                        </div>
                        <div title="使用布置作业抽大奖抽奖" class="mod mod-05 <#if (t.AMBASSADOR_MENTOR_DO_LOTTERY)??><#if t.AMBASSADOR_MENTOR_DO_LOTTERY == "F_Y" || t.AMBASSADOR_MENTOR_DO_LOTTERY == "Y"><#else>mod-05-gray</#if><#else>mod-05-gray</#if>">
                            <div class="pic">
                                <#if (t.AMBASSADOR_MENTOR_DO_LOTTERY)??><#if t.AMBASSADOR_MENTOR_DO_LOTTERY == "F_Y"><em></em></#if><#else><em></em></#if>
                            </div>
                            <div class="hd">抽奖</div>
                        </div>
                        <div title="在奖品中心兑换奖品" class="mod mod-06 <#if (t.AMBASSADOR_MENTOR_REWARD_ORDER)??><#if t.AMBASSADOR_MENTOR_REWARD_ORDER == "F_Y" || t.AMBASSADOR_MENTOR_REWARD_ORDER == "Y"><#else>mod-06-gray</#if><#else>mod-06-gray</#if>">
                            <div class="pic">
                                <#if (t.AMBASSADOR_MENTOR_REWARD_ORDER)??><#if t.AMBASSADOR_MENTOR_REWARD_ORDER == "F_Y"><em></em></#if><#else><em></em></#if>
                            </div>
                            <div class="hd">兑换奖品</div>
                        </div>
                        <div title="在教师论坛发帖or回帖" class="mod mod-07 <#if (t.AMBASSADOR_MENTOR_BBS)??><#if t.AMBASSADOR_MENTOR_BBS == "F_Y" || t.AMBASSADOR_MENTOR_BBS == "Y"><#else>mod-07-gray</#if><#else>mod-07-gray</#if>">
                            <div class="pic">
                                <#if (t.AMBASSADOR_MENTOR_BBS)??><#if t.AMBASSADOR_MENTOR_BBS == "F_Y"><em></em></#if><#else><em></em></#if>
                            </div>
                            <div class="hd">论坛</div>
                        </div>
                        <div title="检查作业时，为学生写评语" class="mod mod-08 <#if (t.AMBASSADOR_MENTOR_COMMENT)??><#if t.AMBASSADOR_MENTOR_COMMENT == "F_Y" || t.AMBASSADOR_MENTOR_COMMENT == "Y"><#else>mod-08-gray</#if><#else>mod-08-gray</#if>">
                            <div class="pic">
                                <#if (t.AMBASSADOR_MENTOR_COMMENT)??><#if t.AMBASSADOR_MENTOR_COMMENT == "F_Y"><em></em></#if><#else><em></em></#if>
                            </div>
                            <div class="hd">评语</div>
                        </div>
                        <#if (currentTeacherDetail.subject == "ENGLISH")!false>
                            <div title="布置一次带有阅读绘本的作业" class="mod mod-10 <#if (t.AMBASSADOR_MENTOR_READING)??><#if t.AMBASSADOR_MENTOR_READING == "F_Y" || t.AMBASSADOR_MENTOR_READING == "Y"><#else>mod-10-gray</#if><#else>mod-10-gray</#if>">
                                <div class="pic">
                                    <#if (t.AMBASSADOR_MENTOR_READING)??><#if t.AMBASSADOR_MENTOR_READING == "F_Y"><em></em></#if><#else><em></em></#if>
                                </div>
                                <div class="hd">绘本作业</div>
                            </div>
                        </#if>
                    </div>
                </div>
            </#list>
            &lt;#&ndash;end//&ndash;&gt;
            &lt;#&ndash;<div class="campusFt"><div class="prev"></div><span><em>1</em>/10</span><div class="next"></div></div>&ndash;&gt;
            <#if (dataMap.teacherList?size gt 4)!false>
                <div class="t-show-box data-teacherCertification">
                    <div class="w-turn-page-list">
                        <a href="javascript:void (0)">查看更多<span class="w-icon-arrow"></span></a>
                    </div>
                </div>
            </#if>
        </div>
    </div>

    <script type="text/javascript">
        // 2017-04-27 校园大使下线前提示
        $(function(){
            if(!$17.getCookieWithDefault("amboff")){
                $17.setCookieOneDay("amboff", 15, 15);
                $17.alert("<p style='padding-bottom:10px;text-align: left'>尊敬的校园大使：</p><p style='text-align: left'>您好！感谢您对一起作业平台的使用和信任，以及在校园大使岗位上的努力和付出。" +
                        "由于一起作业平台整体的产品线升级，我们将对现有的校园大使产品进行调整。2017年5月12日24时后，校园大使的相关任务及奖励将予以暂停，但您的大使等级还会继续保留，还请您谅解。</p>" +
                        "<p style='text-align: left;padding-bottom:10px'>再次感谢您一如既往的理解和支持！</p><p style='text-align: left'>一起作业运营团队</p>"
                );
            }
        });
    </script>

    <#include "template.ftl"/>-->

</@temp.page>