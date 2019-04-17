<#import "../layout.ftl" as temp >
<@temp.page>
<div class="wr">
    <#if (currentStudentDetail.clazz.classLevel)??>
        <div class="info-state2">
            <div class="bg">
                <div class="hd">
                    <#if subject == 'ENGLISH'>
                        英语
                    <#elseif subject == 'MATH'>
                        数学
                    <#elseif subject == 'CHINESE'>
                        语文
                    </#if>作业得分
                </div>
                <div class="time">${createDate!''}作业</div>
                <div class="score"><span>${(homeworkModuleScore['studentAvgScore'])!0}</span>分</div>
            </div>
        </div>

        <#if comment?? && comment['commentContent']?has_content>
            <div class="info-review clearfix">
                <div class="head"><img src="<@app.avatar href="${(comment['teacherImg'])!}"/>" alt=""></div>
                <div class="text">
                    <p>老师点评：</p>
                    <p>${comment['commentContent']}</p>
                </div>
            </div>
        </#if>

        <div class="info-title">
            <span class="step2">作业</span>
            <span class="step3">成绩</span>
        </div>
        <ul class="info-list">
            <#if scoreList?? && scoreList?size gt 0>
                <#list scoreList as scoreList>
                    <li>
                        <#if (scoreList.audio?? && scoreList.audio?has_content)>
                            <div class="first">
                                <div class="w-public-audio w-public-audio-mini w-public-audio-stop audioList" data-audio="${scoreList.audio!''}"><div class="wp-inner"></div></div>
                            </div>
                        </#if>
                        <div class="txt">${(scoreList.practiceType)!''}</div>
                        <#if scoreList.finished >
                            <#if scoreList.isSubjective>
                                <#if scoreList.corrected>
                                    <div class="num">${(scoreList.corrections)!''}</div>
                                <#else>
                                    <div class="num">已完成</div>
                                </#if>
                            <#else>
                                <div class="num">${(scoreList.score)!''}分</div>
                            </#if>
                        <#else >
                            <div class="num">未完成</div>
                        </#if>
                    </li>
                </#list>
            </#if>
            <#if (homeworkModuleScore.haveReading)!false>
                <li>
                    <div class="txt">阅读绘本</div>
                    <div class="num">${(homeworkModuleScore.reading)!''}分</div>
                </li>
            </#if>

            <#if (homeworkModuleScore.mandatory)!false>
                <li>
                    <div class="txt">同步习题</div>
                    <div class="num">${(homeworkModuleScore.mandatoryScore)!''}分</div>
                </li>
            </#if>
        </ul>
    <#else>
        <div class="no-record">加入班级后才可查看哦~</div>
    </#if>
</div>
    <script type="text/javascript">
        document.title = '作业详情';

        //音频播放
        var vox = vox || {};
        vox.task = vox.task || {};
        var playlist = [], playIndex = 0,currentUrl = '',isPlaying = true;

        //播放
        function playAudio(playlistId){
            var url = playlist[playlistId];
            currentUrl = url; //停止播放时 使用
            if(isPlaying){
                if(window.external && ('playAudio' in window.external)){
                    window.external.playAudio(url);
                }else{
                    $M.appLog('homework',{
                        app: "17homework_my",
                        type: "log_normal",
                        module: "playAudio_error",
                        operation: "playAudio null"
                    });
                }
            }
        }

        //停止
        function stopAudio(url){
            //初始化
            isPlaying = false;
            playIndex = 0;

            if(window.external && ('stopAudio' in window.external)){
                window.external.stopAudio(url);
            }
        }
        //app 播放回调调用接口
        vox.task.playAudioState = function (url, state) {
            /*String state: 当前状态，""=无（默认）, "loading"=下载中, "playing"=播放中. "paused"=暂停, "ended"=停止, "error"=错误（不区分）*/
            var audioList = $('.audioList');
            switch (state){
                case "ended":
                        playIndex++;
                        //播放列表最后一个时，播放--->暂停
                        if(playIndex == playlist.length ){
                            playIndex = 0;
                            audioList.removeClass('w-public-audio-play');
                        }else{
                            playAudio(playIndex);
                        }
                    break;
                case "error":
                        //播放失败时，自动播放下一段音频

                        playIndex++;
                        //播放列表最后一个时，播放--->暂停
                        if(playIndex == playlist.length ){
                            playIndex = 0;
                            audioList.removeClass('w-public-audio-play');
                        }else{
                            playAudio(playIndex);
                        }

                        $M.appLog('audio',{
                            app: "17homework_my",
                            type: "log_normal",
                            module: "audio_load_error_h5",
                            operation: playlist[playIndex] || ''
                        });
                    break;
                case "paused":
                        audioList.removeClass('w-public-audio-play');
                    break;
            }
        };

        $(function(){

            //音频播放
            $('.audioList').on('click',function(){
                var $this = $(this);
                var li = $this.closest('li');
                var audio = $this.data('audio');
                var audioList = audio.split('|'),audioLists = [];
                //音频地址拼接
                for(var i = 0; i < audioList.length;i++){
                    if(audioList[i].indexOf('http://') == -1){
                        audioLists.push("http://"+audioList[i]+".mp3");
                    }else{
                        audioLists.push(audioList[i]);
                    }
                }
                playlist = audioLists;

                //当前按钮播放or暂停  其他按钮均设置为暂停。
                $this.toggleClass("w-public-audio-play");
                li.siblings('li').find('div.audioList').removeClass("w-public-audio-play");
                if(!$this.hasClass("w-public-audio-play")){
                    //暂停
                    stopAudio(currentUrl);
                }else{
                    //播放
                    isPlaying = true;
                    playAudio(0);
                }
            });

            //log
            $M.appLog('homework',{
                app: "17homework_my",
                type: "log_normal",
                module: "user",
                operation: "page_homework_report_detail"
            });
        });
    </script>
</@temp.page>