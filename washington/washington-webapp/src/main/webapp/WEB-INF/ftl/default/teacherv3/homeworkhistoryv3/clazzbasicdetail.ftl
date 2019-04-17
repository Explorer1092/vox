<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main" showNav="">
<@sugar.capsule js=["ko",'homework2nd',"jplayer","util.hardcodeurl"] css=["homeworkv3.homework","plugin.flexslider"]/>

<div class="h-homeworkCorrect" id="clazzbasicdetail">
    <h4 class="link">
        <a href="/">首页</a>&gt;<a href="/teacher/new/homework/report/list.vpage">检查作业</a>&gt;<a href="/teacher/new/homework/report/detail.vpage?homeworkId=${homeworkId}">作业报告</a>&gt;<span>作业详情</span>
    </h4>
    <div class="w-base" style="border-top: 0;">
        <div class="hc-main" data-bind="if:!imageLoading(),visible:!imageLoading()">
            <div class="w-base-title" style="background-color: #e1f0fc;">
                <h3 data-bind="text:categoryName() + '详情'">&bbsp;</h3>
                <div class="pb-details" style="display: none;" data-bind="visible:$root.needRecord && $root.needRecord()">
                    <div class="w-base-right">
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
            </div>
            <!--ko foreach:{data:detailList,as:'detail'}-->
            <div class="h-set-homework">
                <div class="stemDetails" data-bind="text:$root.getSentenceText(detail.sentences)">&nbsp;</div>

                <div class="t-error-info w-table" data-bind="if:!detail.needRecord,visible:!detail.needRecord">
                    <table>
                        <thead>
                            <tr>
                                <td style="width: 190px;">答案</td>
                                <td>对应同学</td></tr>
                        </thead>
                        <tbody>
                            <tr class="odd" data-bind="if:detail.answerErrorInfo && detail.answerErrorInfo.length > 0,visible:detail.answerErrorInfo && detail.answerErrorInfo.length > 0">
                                <td class="txt-red">答案错误</td><td data-bind="text:detail.answerErrorInfo.join('，')">&nbsp;</td>
                            </tr>
                            <tr data-bind="if:detail.answerRightInfo && detail.answerRightInfo.length > 0,visible:detail.answerRightInfo && detail.answerRightInfo.length > 0">
                                <td class="txt-green">答案正确</td><td data-bind="text:detail.answerRightInfo.join('，')">&nbsp;</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <!--ko if:detail.needRecord && detail.recordInfo && detail.recordInfo.length > 0-->
            <div class="h-basicExercises" style="margin-top: -16px; border-bottom: 0;">
                <div class="bExercises-box show-all" data-bind="style:{height: detail.recordInfo.length > 15 ? '216px' : '100%'}"><!--全部展开时添加类show-all，tr的个数大于5时。设置高度为216px-->
                    <table cellpadding="0" cellspacing="0" class="table-cell03">
                        <thead>
                        <tr>
                            <td><span class="name">学生</span><span class="grade">分数</span><span class="time">播放</span></td>
                            <td><span class="name">学生</span><span class="grade">分数</span><span class="time">播放</span></td>
                            <td class="last-td"><span class="name">学生</span><span class="grade">分数</span><span class="time">播放</span></td>
                        </tr>
                        </thead>
                        <tbody>
                        <!--ko foreach:ko.utils.range(1,(detail.recordInfo.length/3 + 1))-->
                        <tr data-bind="css:{'even' : $index() % 2 != 0}">
                            <!--ko foreach:{data:detail.recordInfo.slice($index() * 3,($index() * 3 + 3)),as:'record'}-->
                            <td data-bind="css:{'last-td' : ($index() + 1) % 3 == 0}">
                                <span class="name" data-bind="text:record.userName">&nbsp;</span>
                                <span class="grade" data-bind="text:record.score" data-title="本字段统一从后端取，有可能是分数，有可能显示等级">&nbsp;</span>
                                <!--ko if: record.voiceScoringMode == "Normal"-->
                                    <span class="time" data-bind="click:$root.playAudio.bind($data,$element,$root)">
                                        <i class="h-playIcon voicePlayer" data-bind="css:{'h-playDisabled' : !$root.haveAudio(record.userVoiceUrl)}"></i>
                                    </span>
                                <!--/ko-->
                                <!--ko ifnot:record.voiceScoringMode == "Normal"-->
                                <span class="time">(听读模式)</span>
                                <!--/ko-->
                            </td>
                            <!--/ko-->
                        </tr>
                        <!--/ko-->
                        </tbody>
                    </table>
                </div>
                <div class="bExercises-more" data-bind="visible:detail.recordInfo.length > 15,click:$root.showOrHide.bind($data,$root,$element)">
                    <span>查看更多</span><i class="w-icon-arrow"></i>
                </div>
            </div>
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
            categoryName : ko.observable(""),
            imageLoading : ko.observable(true),
            needRecord  : ko.observable(false),
            detailList  : ko.observableArray([]),
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
                if(userVoiceUrl){
                    return userVoiceUrl.split("|").length > 0;
                }
                return false;
            },
            playAudio       : function(element,rootObj){
                var that = this; //this -> user object
                if(rootObj.haveAudio(that.userVoiceUrl)){
                    var showFiles = that.userVoiceUrl.split("|") || [];
                    var $voicePlayer = $(element).find("i.voicePlayer");
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
                                solution: "html,flash ",
                                swfPath: "/public/plugin/jPlayer",
                                supplied: "mp3"
                            });
                        },200);
                        $(".voicePlayer").removeClass("h-playStop");
                        $voicePlayer.addClass("h-playStop");
                    }
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
                    url = $17.utils.hardCodeUrl(url);
                    $("#jquery_jplayer_1").jPlayer("setMedia", {
                        mp3: url
                    }).jPlayer("play");
                }
            },
            showOrHide  : function(self,element){
                var $element = $(element), $span = $element.find("span"),$i = $element.find("i"),$contentBox = $element.siblings("div.show-all");
                if($i.hasClass("w-icon-arrow-topBlue")){
                    //向上箭头
                    $span.text("查看更多");
                    $i.removeClass("w-icon-arrow-topBlue");
                    $contentBox.css("height","216px");
                }else{
                    $span.text("收起");
                    $i.addClass("w-icon-arrow-topBlue");
                    $contentBox.css("height","auto");
                }
            },
            init        : function(){
                var vm = this;
                $.get(answerDetailData.detailUrl,function(data){
                    if(data.success){
                        vm.categoryName(data.categoryName || '');
                        vm.needRecord(data.needRecord || false);
                        vm.detailList(data.questionInfoMapper || []);
                        vm.imageLoading(false);
                    }else{
                        $17.alert(data.info || "获取数据失败");
                    }
                });
            }
        };
        viewModel.init();
        ko.applyBindings(viewModel,document.getElementById("clazzbasicdetail"));
    });


</script>

</@shell.page>