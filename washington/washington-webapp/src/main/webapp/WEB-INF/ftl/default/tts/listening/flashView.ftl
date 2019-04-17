<#import "../module.ftl" as temp />
<@temp.page level="查看" headTitle=((paper.title + '_英语听力材料-一起作业老师')!'一起作业，一起作业网，一起作业学生')>
<style>
    .listen-box .quiz_ugc_box { margin-left: auto; margin-right: auto; }
    .listen-box { border: 1px solid #ddd; padding: 30px; }
    .listen-box .text_black { font-size: 22px; font-weight: normal; text-align: center; }
    .listen-list { width: 100%; font-weight: bold; color: #333; }
    .main-content { }
    .listen-box .title { font-size: 16px; padding-bottom: 20px; }
    .listen-box .topic { font-size: 22px; line-height: 32px; }
    .listen-box .title p { margin-left: 80px; line-height: 24px; }
    .listen-box .container { font-size: 16px; padding: 0 0 32px 32px;}
    .listen-box .container p { font-weight: normal; float: left; font-size: 24px; }
    .listen-box .container ul { margin-left: 48px; }
    .listen-box .container li { width: 98%; word-break: normal; line-height: 22px; padding: 2px 0; font-family: arial; font-size:18px; }
    .listen-box .quiz_ugc_box h2 { font-size: 24px; font-weight: normal; margin: 0 0 15px; }
    .listen-box .back { color: #189cfb; float: right; font-size: 18px; font-weight: normal;  padding: 14px 14px 0;}
    .listen-box li.tts_li_play {line-height:180%; position: relative; }
    .listen-box li.mark_playing .text { background-color: #98d6ff }
    .listen-box li.mark_hover {position: relative}
    .listen-box li.mark_hover .text { border: 1px solid #189cfb; padding: 0; }
    .listen-box li.mark_hover input.tts_play { display: block; bo}
    .listen-box input.tts_play { display: none;}
</style>
<div class="m-main">
    <div class="w-base" style="margin-top: 15px;padding-top:1px;">
        <div class="w-base-container">
            <div class="main-content" style="margin: 15px;">
                <div style="float:right;margin: 20px;">
                    <a style="font-size:14px;" class="link" href="/tts/listening.vpage">返回</a>
                </div>
                <#if paper??>
                    <div class="listen-box">
                        <h2 class="text_black">${paper.title!}</h2>

                        <div class="listen-richtext">
                                ${text!}
                        </div>
                    </div>
                    <div id="jquery_jplayer_1" class="jp-jplayer"></div>
                <#else>
                    找不到该听力试卷
                </#if>
            </div>

            <div class="w-clear"></div>
        </div>
    </div>
</div>
<div class="quiz_footPopUpMenu">
    <div class="inner">
        <a id="tts_play_id" class="quizBtn quizBtn_blue quizBtn_well lis_next" href="javascript:void(0);">播放</a>
        <a id="tts_stop_id" class="quizBtn quizBtn_blue quizBtn_well lis_next" href="javascript:void(0);">停止</a>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $('.listen-list img').each(function(){
           $(this).attr('src', $(this).attr('id').toLocaleLowerCase());
        });
        var TApp = ( function () {
            // 初始化jPlayer
            var jPlayer = $('#jquery_jplayer_1').jPlayer({
                        ready: function (event) {
                        },
                        pause: function () {
                        },
                        ended: function () {
                            var pause = 0;
                            var nextObj;
                            while (index + 1 < data.length){
                                index += 1;
                                nextObj = data[ index ];
                                if (data[index-1].pause)
                                    pause += data[index-1].pause;
                                if (nextObj.voice)
                                    break;
                            }
                            // 还有要播放的音频
                            if (nextObj && index < data.length) {
                                // 标记要播放的语句
                                //TApp.markPlaying( nextObj.voice );
                                // 填充音频
                                jPlayer.jPlayer("setMedia", { mp3: '${listeningUrl!}' + nextObj.voice });
                                // 间隔播放
                                disState.taskId = setTimeout(function () {
                                    // 设为jplayer状态
                                    disState.setJplayer();
                                    app._play();
                                }, pause * 1000);
                                // 播放结束
                            } else {
                                // 回置索引
                                index = -1;
                                app.togglePlayElemValue();
                            }
                        },
                        swfPath: "/public/plugin/jPlayer",
                        supplied: "mp3",
                        preload: "none",
                        wmode: "window",
                        keyEnabled: true
                    }),
            // 为了区分jplayer(taskId为空时) 和 setTimeout.
                    disState = {
                        taskId: "",
                        setJplayer: function () {
                            this.taskId = "";
                        },
                        isJplayer: function () {
                            return !this.taskId;
                        }
                    },
            // 音频索引
                    index = -1,
            // 要播放的音频
                    data = ${playList!},
            // 标记正在播放的样式
                    markPlaySel = 'mark_playing',
            // 标记移动上去的样式
                    markerHoverSel = 'mark_hover',
                    app;

            return app = {
                init: function () {
                    var i = 0, l = data.length, voice , selector;

                    app.playElem = $('#tts_play_id');
                    app.playElem.on('click', TApp.playOrPause);
                    $('#tts_stop_id').on('click', TApp.stop);

                    // 添加index属性，便于从某句开始播放
                    for ( ; i < l; i++ ){
                        selector = '#'+data[i].voice;
                        if ( !$( selector ).attr( 'index' ) ) {
                            $( selector ).attr( 'index', i );
                        }
                    }

                    // 点击播放
                    $('body').on( 'click', 'input.tts_play', function () {
                        var elem = $( this).closest( 'li');

                        // 停止
                        app.stop( true );
                        index = +elem.attr('index') - 1;
                        app.play( true );
                        //app.markPlaying( elem.attr( 'id' ) );
                    } );
                },
                // 播放按钮值的切换
                togglePlayElemValue: function (value) {
                    app.playElem.text(value || ( app.playElem.text() === '播放' ? '暂停' : '播放' ));
                },
                playOrPause: function () {
                    $(this).text() === '播放' ? app.play() : app.pause();
                },
                play: function ( nostart ) {
                    app.togglePlayElemValue();
                    // 没开始播放
                    if ( !app.started() || nostart ) {
                        ++index;
                        // 填音频对象
                        if (data[index].voice)
                            jPlayer.jPlayer("setMedia", { mp3: '${listeningUrl!}' + data[ index ].voice });
                        else {
                            jPlayer.trigger($.jPlayer.event.ended);
                            return;
                        }
                        // 标记要播放的语句
                        //this.markPlaying( data[ index ].voice );
                    }
                    // 播放
                    app._play();
                },
                markPlaying: function ( id ) {
                    $('li').removeClass( markPlaySel );
                    $( '#' + id ).addClass( markPlaySel );
                },
                // jplayer play
                _play: function () {
                    jPlayer.jPlayer('play');
                },
                // 只要播放了data第一个数据就算开始，即index !== -1
                started: function () {
                    return index === -1 ? false : true;
                },
                pause: function (value /* 播放按钮的值 */ ) {
                    app.togglePlayElemValue(value);
                    // 正播放单个音频
                    if (disState.isJplayer()) {
                        jPlayer.jPlayer('pause');
                        // setTimeout 进行中
                    } else {
                        clearTimeout(disState.taskId);
                        disState.setJplayer();
                    }
                },
                stop: function ( flag ) {
                    // 开始播了
                    if (app.started() || flag ) {
                        app.pause('播放');
                        index = -1;
                        $('li').removeClass( markPlaySel );
                    }
                }
            };
        }() );
        TApp.init();
    });

</script>
</@temp.page>