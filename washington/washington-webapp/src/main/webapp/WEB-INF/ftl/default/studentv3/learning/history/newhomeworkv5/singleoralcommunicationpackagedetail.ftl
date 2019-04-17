<#import '../../../layout/layout.ftl' as temp>
<@temp.page pageName='homeworkreport'>
    <@sugar.capsule js=["jquery.flashswf"] css=["homeworkhistory.report"] />
<style type="text/css">
    .w-tableBox{ margin:23px 0; overflow: hidden; clear: both;}
    .w-tableBox table caption{ color: #383a4b; font-size: 14px; border-top: 1px solid #eaeaea; background-color: #eef6f9; padding: 12px; text-align: left; font-weight: bold;}
    .w-tableBox table{ border-collapse: collapse; margin: 0 0 0 -1px; padding: 0; background-color: #fff; width: 100%;}
    .w-tableBox table td, .w-tableBox table th{ padding: 10px 27px; border: solid #eaeaea; border-width: 1px 0 1px 1px;}
    .w-tableBox table thead td, .w-tableBox table thead th{ color: #383a4b; font-weight: bold; line-height: 120%;}
    .w-tableBox table tbody td, .w-tableBox table tbody th{ color: #999; font-weight: normal; font-size: 12px; line-height: 150%; }
    .w-tableBox table th{ font-weight: normal;}
    .w-tableBox table tbody td a, .w-tableBox table tbody th a{ color: #00aced; text-decoration: none; display: inline-block; margin-right: 10px; }
</style>
<div class="t-center-container">
    <div class="breadcrumb" style="padding: 15px 0;">
        <span><a class="w-blue" href="/student/index.vpage">首页</a> &gt;</span>
        <span><a class="w-blue" href="/student/learning/history/newhomework/homeworkreport.vpage?homeworkId=${homeworkId}&subject=${subjectName!}">作业详情</a> &gt;</span>
        <span>答题详情</span>
    </div>
    <div class="h-answerDetails h-historyBox" id="tabContentHolder">
        <div data-bind="if:!$root.loading(),visible:!$root.loading()">
            <div class="h-header" style="margin-bottom: 15px;">
                <div class="h-title-2"><span class="left-text" data-bind="text: $root.content() && $root.content().topicName ? $root.content().topicName : ''">作业题目 -- 答题详情</span></div>
            </div>
            <div class="w-tableBox" data-bind="if:$root.content().studentRecords && $root.content().studentRecords.length > 0">
                <table>
                    <thead>
                    <tr>
                        <td>序号</td>
                        <#--<td>题目</td>-->
                        <td>得分</td>
                        <td>录音</td>
                    </tr>
                    </thead>
                    <tbody data-bind="foreach:{data:$root.content().studentRecords,as:'record'}">
                        <tr>
                            <td class="lesson"><span class="name" data-bind="text:($index() + 1)"></span></td>
                            <#--<td class="apps" data-bind="text:record.questionContent ? record.questionContent : ''">&nbsp;</td>-->
                            <td class="grade" data-bind="text:record.score != null ? record.score : ''">&nbsp;</td>
                            <td class="operation">
                                <span class="time" data-bind="click:$root.playAudio.bind($data,$root)">
                                    播放 <i class="h-playIcon voicePlayer" data-bind="css:{'h-playStop': $root.playingStoneId() == record.uuid}"></i>
                                 </span>
                                <span class="audio-play"  style="display: none;"></span>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

    </div>
</div>

<div id="jquery_jplayer_1" class="jp-jplayer"></div>
<script type="text/javascript">
    var answerDetailData = {
        env : <@ftlmacro.getCurrentProductDevelopment />,
        domain : '${requestContext.webAppBaseUrl}/'
    };
</script>
    <@sugar.capsule js=["ko","jplayer","studentreport.singleoralcommunicationdetail"] />
</@temp.page>




