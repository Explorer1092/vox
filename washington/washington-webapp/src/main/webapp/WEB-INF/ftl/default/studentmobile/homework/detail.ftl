<#import "../layout.ftl" as temp >
<@temp.page>
<#assign isQuiz = homeworkType?? && homeworkType?contains('QUIZ')/>
<div class="wr">
    <#if (currentStudentDetail.clazz.classLevel)??>
        <#if valid>
            <#if (supportType == 'SUPPORTED')>
                <div class="info-state1">
                    <div class="bg">
                        <div class="hd">${units!''}</div>
                        <div class="time">${startDate!''}${isQuiz?string('测验','作业')}</div>
                        <div class="num">
                            <#if isQuiz>
                                <strong style="font-weight: normal;">出题人：${author!""}</strong>
                            </#if>
                            <#if homeworkType?? && (homeworkType != 'MATH' && homeworkType != 'CHINESE')>
                                共<span>${totalCount!0}</span>道题
                            </#if>
                        </div>
                    </div>
                </div>
                <#if note?? && note?has_content>
                    <div class="info-review clearfix">
                        <div class="head"><img src="<@app.avatar href="${(teacher.profile.imgUrl)!''}"/>" alt=""></div>
                        <div class="text">
                            <p>老师点评：</p>

                            <p>${note}</p>
                        </div>
                    </div>
                </#if>
                <div class="info-title"><span class="step1">${isQuiz?string('测验','作业')}内容：</span></div>
                <ul class="info-list">
                    <#if categoryPracticeCount?? && categoryPracticeCount?size gt 0>
                        <#list categoryPracticeCount?keys as list>
                            <li>
                                <div class="txt">${list!''}</div>
                                <div class="num">${(categoryPracticeCount[list])!''}道</div>
                            </li>
                        </#list>
                    </#if>
                </ul>

                <#if allowStartHomework!false>
                    <div style="height: 140px; clear: both;"></div>
                    <#if startDateTime?? && (.now lt (startDateTime?string)?datetime('yyyy/MM/dd HH:mm:ss'))>
                        <div class="info-submit not-begin" style="position: static;">
                            <div class="submitInner">
                                <div class="submitBox">作业还未开始</div>
                            </div>
                        </div>
                    <#else>
                        <div id="homework_but" class="info-submit" style="position: static;">
                            <div class="submitInner">
                                <div class="submitBox">开始${isQuiz?string('测验','作业')}</div>
                            </div>
                        </div>
                    </#if>
                </#if>
            <#elseif supportType == 'NEW_VERSION_SUPPORTED'>
                <div class="no-record" style="line-height: 50px;padding: 237px 0 0; font-size: 36px;">
                    <div>你目前的版本过低，请下载新版本！</div>
                </div>
                <div class="info-submit" style="position: static;">
                    <a href="http://wx.17zuoye.com/download/17studentapp?cid=102018">
                        <div class="submitInner">
                            <div class="submitBox">点击下载新版</div>
                        </div>
                    </a>
                </div>
            <#elseif supportType == 'NOT_SUPPORTED'>
                <div class="no-record" style="line-height: 50px;padding: 237px 0 0; font-size: 36px;">
                    <div>作业暂不支持</div>
                    <div>请用电脑访问www.17zuoye.com完成你的作业</div>
                </div>
            </#if>
        <#else >
            <div>
                <div class="no-record">作业已过期</div>
            </div>
        </#if>
    <#else>
        <div class="no-record">加入班级后才可查看哦~</div>
    </#if>
</div>


<script type="text/javascript">
    document.title = '作业详情';
    $(function () {
        //点击开始作业
        $('#homework_but').on('click', function () {
            var homework = {
                homework_type: '${homeworkType!''}',
                homework_id: '${homeworkId!''}',
                hw_card_source: 'h5',//跳h5还是native
                hw_card_variety: 'homework',//调用的go api
                is_makeup: false,
                page_viewable: false
            };
            if (window.external && ('doHomework' in window.external)) {
                window.external.doHomework(JSON.stringify(homework));
            } else {
                $M.promptAlert('开始作业失败，请联系客服');
            }
            //log
            $M.appLog('homework', {
                app: "17homework_my",
                type: "log_normal",
                module: "user",
                operation: "page_homework_detail_click"
            });
        });

        //log
        $M.appLog('homework', {
            app: "17homework_my",
            type: "log_normal",
            module: "user",
            operation: "page_homework_detail"
        });
    });
</script>
</@temp.page>