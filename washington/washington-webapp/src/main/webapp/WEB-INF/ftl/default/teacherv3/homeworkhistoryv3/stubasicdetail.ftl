<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main" showNav="">
    <@sugar.capsule js=["ko",'homework2nd',"jplayer","util.hardcodeurl"] css=["homeworkv3.homework","plugin.flexslider"]/>

<div class="h-homeworkCorrect" id="studentbasicdetail">
    <h4 class="link">
        <a href="/">首页</a>&gt;<a href="/teacher/new/homework/report/list.vpage">检查作业</a>&gt;<a href="/teacher/new/homework/report/detail.vpage?homeworkId=${homeworkId}">作业报告</a>&gt;<span>作业详情</span>
    </h4>
    <div class="w-base" style="border-top: 0;">
        <div class="hc-main" data-bind="if:!imageLoading(),visible:!imageLoading()">
            <div class="w-base-title" style="background-color: #e1f0fc;" data-bind="attr:{title : description()}">
                <h3 data-bind="text:categoryName() + '详情'">&bbsp;</h3>
                <!--ko if:$root.description && $root.description()-->
                <span data-bind="text:'(' + description() + ')'" style="display: inline-block; width: 600px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; padding: 17px 0 0 17px;">&nbsp;</span>
                <!--/ko-->
                <div class="w-base-right" style="display: none;" data-bind="visible:$root.needRecord && $root.needRecord()">
                    <span class="hoverText">分数计算说明</span>
                    <div class="hoverInfo">
                        <span class="jf-arrow"></span>
                        <p>1、本练习总成绩为所有题目平均分</p>
                        <p>2、等级计算关系</p>
                        <table>
                            <tr>
                                <td>A</td><td>B</td><td>C</td><td>D</td>
                            </tr>
                            <tr>
                                <td>100分</td><td>90分</td><td>75分</td><td>60分</td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>
            <!--ko ifnot:$root.tongueTwister()-->
            <!--ko foreach:{data:detailList,as:'detail'}-->
            <div class="h-set-homework">
                <div class="stemDetails" data-bind="text:$root.getSentenceText(detail.sentences)">&nbsp;</div>
                <!--ko if:!detail.needRecord-->
                <div class="t-error-info w-table" data-bind="if:!detail.needRecord,visible:!detail.needRecord">
                    <table>
                        <thead>
                        <tr class="odd">
                            <td style="padding-left: 35px;" data-bind="css:{'txt-red' : !!!detail.answerInfo,'txt-green' : !!detail.answerInfo},text: detail.answerResultWord">&nbsp;</td>
                        </tr>
                        </thead>
                    </table>
                </div>
                <!--/ko-->
            </div>
            <!--ko if:detail.needRecord && detail.recordInfo-->
            <div class="h-basicExercises" style="margin-top: -16px;">
                <div class="bExercises-box"><!--全部展开时添加类show-all，tr的个数大于5时。设置高度为180px-->
                    <table cellpadding="0" cellspacing="0" class="table-cell03" style="height:100%">
                        <thead>
                        <tr>
                            <td><span class="grade" style="width: 44%;">分数</span><span class="time"  style="width: 44%;">播放</span></td>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td class="last-td">
                                <span class="grade" style="width: 44%;" data-bind="text:detail.recordInfo.score" data-title="本字段统一从后端取，有可能是分数，有可能显示等级">&nbsp;</span>
                                <!--ko if:detail.recordInfo.voiceScoringMode == 'Normal'-->
                                    <span class="time" style="width: 44%;" data-bind="click:$root.playAudio.bind($data,$element,$root)">
                                        <i class="h-playIcon voicePlayer" data-bind="css:{'h-playDisabled' : !$root.haveAudio(detail.recordInfo.userVoiceUrl)}"></i>
                                    </span>
                                <!--/ko-->
                                <!--ko ifnot:detail.recordInfo.voiceScoringMode == 'Normal'-->
                                <span class="time" style="width: 44%;">(听读模式)</span>
                                <!--/ko-->
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <!--/ko-->
            <!--/ko-->
            <!--/ko-->
            <!--ko if:$root.tongueTwister()-->
            <!--ko foreach:{data:detailList,as:'detail'}-->
            <!--ko foreach:{data:detail.data,as:'subDetail'}-->
            <div class="h-set-homework">
                <div class="stemDetails" data-bind="text:subDetail.text">&nbsp;</div>
                <div class="t-error-info w-table">
                    <table>
                        <thead>
                        <tr class="odd">
                            <td style="padding-left: 35px;" class="txt-green" data-bind="text: subDetail.appOralScoreLevel">&nbsp;</td>
                        </tr>
                        </thead>
                    </table>
                </div>
            </div>
            <!--/ko-->
            <!--/ko-->
            <!--/ko-->
            <!--/ko-->
        </div>
        <div style="height: 200px; background-color: white; width: 98%;" data-bind="if:imageLoading(),visible:imageLoading()">
            <img src="<@app.link href='public/skin/teacherv3/images/loading.gif' />" style="margin-top: 25px; margin-left: 40%;" />
        </div>
    </div>
</div>

<div id="jquery_jplayer_1" class="jp-jplayer"></div>
<script type="text/javascript">
    var answerDetailData = {
        detailUrl       : '${detailUrl!''}',
        movie           : "<@app.link href="resources/apps/flash/voicePlayer/VoiceReplayer.swf"/>"
    };

    $(function(){
        var viewModel = {
            homeworkId   : $17.getQuery("hid"),
            homeworkType : null,
            categoryId   : $17.getQuery("categoryId"),
            studentId    : $17.getQuery("studentId"),
            categoryName : ko.observable(""),
            description  : ko.observable(""),
            imageLoading : ko.observable(true),
            needRecord  : ko.observable(false),
            detailList  : ko.observableArray([]),
            tongueTwister : ko.observable(false),  //是否属于自然拼读形式中绕口令这类东西
            getSentenceText : function(sentenceList){
                var sentenceText = "";
                if(!$.isArray(sentenceList)){
                    return sentenceText;
                }
                for(var t = 0,tLen = sentenceList.length; t < tLen; t++){
                    sentenceText += sentenceList[t].sentenceContent;
                }
                return sentenceText;
            },
            haveAudio       : function(userVoiceUrl){
                return $.isArray(userVoiceUrl) && userVoiceUrl.length > 0;
            },
            playAudio       : function(element,rootObj){
                var that = this.recordInfo; //this -> question object
                if(rootObj.haveAudio(that.userVoiceUrl)){
                    var showFiles = that.userVoiceUrl || [];
                    var $voicePlayer = $(element).find(".voicePlayer");
                    if($voicePlayer.hasClass("h-playStop")){
                        $voicePlayer.removeClass("h-playStop");
                        $("#jquery_jplayer_1").jPlayer("clearMedia");
                    }else{
                        var playIndex = 0;
                        $("#jquery_jplayer_1").jPlayer("destroy");
                        setTimeout(function(){
                            $("#jquery_jplayer_1").jPlayer({
                                ready: function (event) {
                                    rootObj.playSpecialAudio(showFiles[playIndex]);
                                },
                                error : function(event){
                                    playIndex = rootObj.playNextAudio(playIndex,showFiles);
                                },
                                ended : function(event){
                                    playIndex = rootObj.playNextAudio(playIndex,showFiles);
                                },
                                volume: 0.8,
                                solution: "html,flash",
                                swfPath: "/public/plugin/jPlayer",
                                supplied: "mp3"
                            });
                        },200);
                        $(".voicePlayer").removeClass("h-playStop");
                        $voicePlayer.addClass("h-playStop");
                    }
                    $17.voxLog({
                        module: "m_Odd245xH",
                        op    : "stu_reportdetails_play_recording_click",
                        s0    : "ENGLISH",
                        s1    : rootObj.homeworkType,
                        s2    : "BASIC_APP",
                        s3    : rootObj.categoryId,
                        s4    : rootObj.studentId
                    });
                }else{
                    $17.info("音频数据为空");
                }
            },
            playNextAudio    : function(playIndex,audioArr){
                if(playIndex >= audioArr.length - 1){
                    $(".voicePlayer").removeClass("h-playStop");
                    $(this).jPlayer("destroy");
                }else{
                    playIndex++;
                    this.playSpecialAudio(audioArr[playIndex]);
                }
                return playIndex;
            },
            playSpecialAudio : function(url){
                if(url){
                    $("#jquery_jplayer_1").jPlayer("setMedia", {
                        mp3: $17.utils.hardCodeUrl(url)
                    }).jPlayer("play");
                }
            },
            init        : function(){
                var vm = this;
                $.get(answerDetailData.detailUrl,function(data){
                    if(data.success){
                        vm.description(data.description || "");
                        vm.homeworkType = data.homeworkType;
                        vm.categoryName(data.categoryName || '');
                        vm.needRecord(data.needRecord || false);
                        vm.tongueTwister(data.tongueTwister || false);
                        vm.detailList(data.questionInfoMapper || []);
                        vm.imageLoading(false);
                    }else{
                        $17.alert(data.info || "获取数据失败");
                    }
                });
            }
        };
        viewModel.init();
        ko.applyBindings(viewModel,document.getElementById("studentbasicdetail"));
    });


</script>

</@shell.page>