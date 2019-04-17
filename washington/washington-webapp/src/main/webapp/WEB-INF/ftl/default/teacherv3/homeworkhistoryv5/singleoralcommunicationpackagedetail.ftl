<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main" showNav="">
    <@sugar.capsule js=["jquery.flashswf","plugin.venus-pre"] css=["plugin.venus-pre","homeworkv3.homework"] />
<div class="h-homeworkCorrect">
    <h4 class="link">
        <a href="/">首页</a>&gt;<a href="/teacher/new/homework/report/list.vpage">检查作业</a>&gt;<a href="/teacher/new/homework/report/detail.vpage?homeworkId=${homeworkId}">作业报告</a>&gt;<span>作业详情</span>
    </h4>
    <div class="w-base" style="border-top: 0;" id="tabContentHolder">
        <div data-bind="if:!$root.loading(),visible:!$root.loading()">
            <div class="J_mainContentHolder hc-main">
                <div class="w-base-title" style="background-color: #e1f0fc;">
                    <h3 data-bind="text: $root.content() && $root.content().topicName ? $root.content().topicName : ''"></h3>
                </div>
                <div class="h-basicExercises" data-bind="if:$root.content().studentRecords && $root.content().studentRecords.length > 0">
                    <table class="table-cell02" cellpadding="0" cellspacing="0">
                        <thead>
                        <tr>
                            <td class="time">序号</td>
                            <td class="apps">题目内容</td>
                            <td class="grade">得分</td>
                            <td class="operation">录音</td>
                        </tr>
                        </thead>
                        <tbody data-bind="foreach:{data:$root.content().studentRecords,as:'record'}">
                        <tr>
                            <td class="lesson"><span class="name" data-bind="text:($index() + 1)"></span></td>
                            <td class="apps" data-bind="text:record.questionContent ? record.questionContent : ''">&nbsp;</td>
                            <td class="grade" data-bind="text:record.score != null ? record.score : ''">&nbsp;</td>
                            <td class="operation">
                                <span class="time" data-bind="click:$root.playAudio.bind($data,$root)">
                                    播放 <i class="h-playIcon voicePlayer" data-bind="css:{'h-playStop':$root.playingStoneId() == record.uuid}"></i>
                                 </span>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        <div style="height: 200px; background-color: white; width: 98%;" data-bind="if:$root.loading(),visible:$root.loading()">
            <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="display:block;margin: 0 auto;" />
        </div>
    </div>
</div>

<div id="jquery_jplayer_1" class="jp-jplayer"></div>
<script type="text/javascript">
    var answerDetailData = {
        env : <@ftlmacro.getCurrentProductDevelopment />,
        imgDomain : '${imgDomain!''}',
        domain : '${requestContext.webAppBaseUrl}/',
        defaultPicUrl:"<@app.link href="public/skin/teacherv3/images/homework/upflie-img.png"/>",
        movie: "<@app.link href="resources/apps/flash/voicePlayer/VoiceReplayer.swf"/>",
        flashPlayerUrl      : "<@app.link href='public/skin/project/about/images/flvplayer.swf'/>"
    };
</script>
<@sugar.capsule js=["ko","jplayer","homeworkv5.singleoralcommunicationdetail"] />
</@shell.page>