<#import "../layout.ftl" as homework>
<@homework.page title="作业详情" pageJs="homeworkDetail">
<@sugar.capsule css=['jbox'] />
<div class="main">
    <!--title-->
    <#macro dateFormat sconds = 0>
        ${(sconds/60)?int!''}分${(sconds%60)?int!''}秒
    </#macro>
    <#if detail??>
        <div class="title_box">
            <div class="tb_detail">
                <h2> ${detail.isQuiz?string("测验","作业")}报告 </h2>
                <p class="info">
                    <span>布置日期：${detail.ct!0}</span>
                </p>
            </div>
        </div>
        <div class="homework_English_box" >
            <#if detail.homeworkType=="MATH">
                <p style="text-align: center; padding: 100px 0;">学生作业详情正在升级中，敬请期待！</p>
            <#else>
            <div class="report-fraction-box" style="margin-top: 30px;">
                <div class="rf-left">
                    <h3 class="k-title">总得分</h3>
                    <div class="k-fraction">
                    ${detail.score!0}
                    </div>
                </div>
                <div class="rf-right">
                    <table class="report-table">
                        <tbody>
                        <tr class="title">
                            <td>
                                <#if detail.hundredCount gt 0>
                                    满分人数
                                <#else>
                                    最高分
                                </#if>
                            </td>
                            <td>个人用时</td>
                        </tr>
                        <tr>
                            <td>
                                <#if detail.hundredCount gt 0>
                                ${detail.hundredCount!0}
                            <#else>
                                ${detail.maxScore!0}
                                </#if>
                            </td>
                            <td><@dateFormat (detail.duration)!0></@dateFormat></td>
                        </tr>
                        <tr class="title">
                            <td>平均分</td>
                            <td>平均用时</td>
                        </tr>
                        <tr>
                            <td>${detail.averageScore!0}</td>
                            <td><@dateFormat (detail.averageDuration)!0></@dateFormat></td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            </#if>


            <#--老师评语-->
                <#if detail.comment?? && detail.comment?has_content>
                    <h3 class="font_28 text_blue"><i class="icon_blue icon_blue_04"></i>老师评语</h3>
                    <div style="margin: 0;" class="homework_history">
                        <ul class="list">
                            <li>
                                <div class="content">
                                    <p style="text-indent: 2em; font-size: 24px;">${detail.comment!''}</p>
                                    <p style="text-align: right;">${detail.teacherName!''}</p>
                                </div>
                            </li>
                        </ul>
                    </div>
                </#if>

            <#--孩子朗读-->
                <#if detail.audios?? && detail.audios?size gt 0>
                    <div id="playersList"><h3 class="font_28 text_blue"><i class="icon_blue icon_blue_02"></i>孩子朗读</h3>
                        <div data-widget="table" class="w_table">
                            <table>
                                <thead>
                                <tr>
                                    <td>题型</td>
                                    <td>录音</td>
                                </tr>
                                </thead>
                                <tbody>
                                    <#list detail.audios as audio>
                                    <tr>
                                        <td>${audio.practiceType!''}</td>
                                        <td>
                                            <i class="icon-c icon-n-play playAudioBtn" data-audio_src="${audio.audio?join("|")}" data-smid="${audio_index}"></i>
                                        </td>
                                    </tr>
                                    </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </#if>

                <#if detail.prize?? && detail.prize gt 0>
                    <h3 class="font_28 text_blue"><i class="icon_blue icon_blue_12"></i>老师奖励</h3>
                    <div style="border: 1px solid #d1cfcf; padding: 30px; margin-bottom: 30px;">
                        <table>
                            <tr>
                                <td style="width: 90%;line-height: 30px;">获得了老师设置的<br />${detail.prize!0}学豆奖励</td>
                                <td>
                                <span style="background-color: #bad55e; padding: 10px; border-radius: 5px; width: 225px; float: right;">
                                    <a href="/parent/integral/order.vpage?sid=${detail.studentId!0}" style="color: #ffffff;">给班级贡献学豆</a>
                                </span>
                                </td>
                            </tr>
                        </table>
                    </div>
                </#if>

                <#if detail.wrongList?? && detail.wrongList?size gt 0>
                    <div class="report-box">
                        <h2 class="title-back-5">本次错题</h2>
                        <div class="write">
                            <div style="float: left;"><p><span>${detail.wrongList?size}</span>道错题</p></div>
                            <div style="float: right;">
                                <#if detail.homeworkType != 'MATH' && detail.afentiAvilable>
                                    <a id="resetBut" data-vip="${detail.vip?string}" href="javascript:void (0);">错题重做</a>
                                </#if>
                                <a id="errorDetailBut" data-wrong_list='${json_encode(detail.wrongList)}' href="javascript:void (0);" style="background-color: #359bff;">查看</a>
                                <form action="/parent/homework/errordetail.vpage" method="post">
                                    <input type="hidden" name="wrongList" value=""/>
                                    <input type="hidden" name="index" value="0"/>
                                    <input type="hidden" name="sid" value=""/>
                                    <input type="hidden" name="ht" value="${detail.homeworkType!}"/>
                                    <input type="hidden" name="hid" value=""/>
                                </form>
                            </div>
                        </div>
                    </div>
                </#if>

            </div>


    <#else>
        <div class="title_box">
            <div class="tb_detail">
                <h2> 作业报告 </h2>
            </div>
        </div>
        <div class="homework_English_box">
            <p class="content_p">
                未查询到作业报告
                <a href="javascript:history.back();" data-rel="back" class="ui-btn ui-mini ui-btn-inline ui-btn-corner-all ui-btn-b">返回</a>
            </p>
        </div>
    </#if>
</div>
    <script type="text/javascript">
        var homeworkType = '${(detail.homeworkType)!"noSubject"}';
    </script>
</@homework.page>