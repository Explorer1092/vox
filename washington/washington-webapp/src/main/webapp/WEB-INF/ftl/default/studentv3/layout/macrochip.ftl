
<!-- FLASH作业增加统一操作框架
  引用时要传practiceType：
 -->
<#macro flashFooter homeworkType>
    <#if true>
    <div id="html_voice_help" style="display: none;">
        <div style="line-height: 150%;">
            <div style="display: none ; color: #f00; padding: 0 0 15px;" id="chromeInfo">
                <img src="<@app.link href="public/skin/default/images/help/chrome_info.png"/>"/>
                您正在使用谷歌Chrome浏览器，如果页面上方出现类似这样的提示，请点击“允许”
            </div>

            <div>
                <p>1.无法录音，请扫码安装一起作业手机版。</p>
                <div style=" padding: 10px 20px 0 80px;">
                    <img src="<@app.link href="public/skin/studentv3/images/publicbanner/app-ref-102014.png"/>" />
                </div>
                <p style="padding: 20px 0;">2.没有手机，<a href="http://cdn.17zuoye.com/download/17zuoye_liebao_20150624.exe" target="_blank">请点此升级浏览器：【猎豹浏览器】</a>。</p>
            </div>
        </div>
    </div>
    <@sugar.capsule js=["VoxExternalPlugin"] />
    <script type="text/javascript">
        $(function(){
            $(".macrHelpInfo").on('click', function(){
                $.prompt($('#html_voice_help').html(), {
                    title: "作业帮助",
                    focus: 1,
                    position: {width:450},
                    buttons: { "知道了": true }
                });
                $17.tongji("学生端-点击作业下方帮助");
            });

            if(VoxExternalPluginExists()) {
                $('.download_client_tip').hide();
            }
        });
    </script>

    <div class="ah-homework-info w-ag-center">
        <a id="start_stop_but" <#if homeworkType =='english' || homeworkType =='chinese'> style="display: none;"</#if> href="javascript:void(0);"><span class="w-icon-detail w-soundPlay-arrow w-icon-md"></span><strong>暂停</strong></a>
        <a id="close_music_but" href="javascript:void(0);"><span class="w-icon-detail w-soundClose-arrow w-icon-md"></span><strong>关闭声音</strong></a>
        <a id="homework_suggest_but" class="w-btn-dic w-btn-gray-well" href="javascript:void(0);">反馈意见</a>
        <a class="w-btn-dic w-btn-gray-well macrHelpInfo" href="javascript:void(0);">
            <span class="w-icon-detail w-mark-arrow"></span><span class="w-icon-md">帮助</span>
        </a>
    </div>

    <script type="text/javascript">
        //作业游戏切换时，添加log
        function changeGameLog(prevGameName,currentGameName){
            if (!$17.isBlank(prevGameName)) {
                $17.traceLog({
                    op: "changeGame",
                    app: prevGameName,
                    changeFrom: prevGameName,
                    changeTo: currentGameName
                });
            }
        }

        var flashFooterParams = {};

        function flashFooterSetParam(k, v) {
            flashFooterParams[k] = v;
        }

        //flash 背景音乐控制开关及flash开始暂停
        function flashBackgroundMusicSwitch(obj,playType){
            var iframe = document.getElementById('iframe_homework');
            var win = iframe.contentWindow || iframe;
            var o = win.getHomeworkFlashObject();
            if(o[playType]){
                o[playType](obj);
            }
        }

        function flashOperationObject(_this,classObjA,classObjB,contentA,contentB,obj){
            var span = _this.children('span');
            var strong = _this.children('strong');

            if(span.hasClass(classObjB)){
                span.removeClass(classObjB).addClass(classObjA);
                strong.addClass('w-green').html(contentA);
                if(obj == 'flashGameStartOrStop'){
                    flashBackgroundMusicSwitch(true,'setGamePause');
                    $(".flash_stop_show_box").css({display : 'block'});
                }else{
                    flashBackgroundMusicSwitch(false,'flashVoicePlay');
                }
            }else{
                span.removeClass(classObjA).addClass(classObjB);
                strong.removeClass('w-green').html(contentB);
                if(obj == 'flashGameStartOrStop'){
                    flashBackgroundMusicSwitch(false,'setGamePause');
                    $(".flash_stop_show_box").css({display : 'none'});
                }else{
                    flashBackgroundMusicSwitch(true,'flashVoicePlay');
                }
            }
        }

        $(function(){
            //反馈建议。
            $("#homework_suggest_but").click(function(){
                var reqParams = {
                    homeworkType: '${homeworkType}'
                };

                //目前examId和lessonId公用一个扩展字段extStr1记录
                if( 'homeworkId' in flashFooterParams) reqParams['extStr2'] = flashFooterParams['homeworkId'];
                if( 'lessonId' in flashFooterParams) reqParams['extStr1'] = flashFooterParams['lessonId'];
                if( 'practiceType' in flashFooterParams) reqParams['practiceType'] = flashFooterParams['practiceType'];
                if( 'refUrl' in flashFooterParams) reqParams['refUrl'] = flashFooterParams['refUrl'];

                var url = '/ucenter/feedback.vpage?' + $.param(reqParams);
                var html = "<iframe class='vox17zuoyeIframe' class='vox17zuoyeIframe' width='600' height='430' frameborder=0 src='" + url + "'></iframe>";
                $.prompt(html, { title: "给一起作业提建议", position : { width:660 }, buttons: {} } );
                return false;
            });

            //开始游戏/暂停游戏
            $("#start_stop_but").click(function(){
                flashOperationObject($(this),'w-mark-play','w-soundPlay-arrow','开始','暂停','flashGameStartOrStop');
                return false;
            });

            // 开启/关闭 背景音乐 music
            $("#close_music_but").click(function(){
                flashOperationObject($(this),'w-sound-arrow','w-soundClose-arrow','开启声音','关闭声音','flashBackgroundMusic');
                return false;
            });
        });
    </script>
    </#if>
</#macro>