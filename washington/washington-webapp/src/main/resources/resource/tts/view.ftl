<!DOCTYPE html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="renderer" content="webkit" />
    <meta http-equiv="X-UA-Compatible" content="IE=Edge, chrome=1"/>
    <meta content="no-cache,must-revalidate" http-equiv="Cache-Control">
    <meta content="no-cache" http-equiv="pragma">
    <meta content="0" http-equiv="expires">
    <style>
        ul, ol, li { list-style: outside none none; }
        .quiz_ugc_box { margin-left: auto; margin-right: auto; width: 800px;}
        .listen-box { border: 2px solid #ddd; padding: 30px 20px 20px 20px;}
        .text_black { font-size: 22px; font-weight: normal; text-align: center;}
        .listen-list { width: 100%; overflow-x: hidden; overflow-y: auto; font-weight: bold; color: #333; }
        .title { font-size: 16px; padding-bottom: 20px; }
        .topic { font-size: 22px; float: left; }
        .title p { margin-left: 80px; line-height: 24px; }
        .container { font-size: 16px; padding: 0 0 32px 32px; }
        .container p { font-weight: normal; float: left; font-size: 24px; margin: 5px 0 0;}
        .container ul { margin-left: 0px; }
        .container li { width: 98%; word-break: normal; line-height: 22px; padding: 2px 0; font-family: arial; font-size: 18px; }
        #tts_play_id, #tts_stop_id { color: #fff; padding: 10px 20px; font: 14px/1.125 arial; display: inline-block; vertical-align: middle; margin: 0 5px 2px; border-radius: 4px; outline: none; text-align: center; cursor: pointer; background-color: #5c8afe; border: 1px solid #4e73ca; box-shadow: 0 0 0 1px #769cfd inset; text-decoration: none; }
        #tts_play_id:hover, #tts_stop_id:hover { border-color: #6984c5; color: #FFF; background-color: #8aabff; }
        #tts_play_id:active, #tts_stop_id:active { background-color: #698eec; }
        li.tts_li_play {line-height:180%;}
        li.mark_playing .text{ background-color: #98d6ff }
        li.mark_hover {position: relative}
        li.mark_hover .text { border: 1px solid #189cfb; padding: 0; }
        li.mark_hover input.tts_play { display: block;}
        input.tts_play { display: none;}
        .quiz_footPopUpMenu { position: fixed; bottom: 0; left: 0; width: 100%; z-index: 8; _position: absolute; _top:expression(documentElement.scrollTop+document.documentElement.clientHeight-64);height: 68px; overflow: hidden; }
        .quiz_footPopUpMenu .inner { width: 100%; background-color: #ededee; border-top: 2px solid #ccc; text-align: center; padding: 14px 0; left: 0;}
        .quiz_footPopUpMenu_clear .inner { position: relative; }
        .lis_step1_play{background-color: #189cfb;border: medium none;color: #ffffff; height: 20px; padding-left: 14px;width: 70px; }

        .role_m{background-image: url(js/m.png);height: 38px; width: 38px;display: inline-block;}
        .role_f{background-image: url(js/f.png);height: 38px; width: 38px;display: inline-block;}
        .role_c{background-image: url(js/c.png);height: 38px; width: 38px;display: inline-block;}
        .role_bf{background-image: url(js/bf.png);height: 38px; width: 38px;display: inline-block;}
        .role_bm{background-image: url(js/bm.png);height: 38px; width: 38px;display: inline-block;}
    </style>
    <script src="js/jquery-1.7.1.min.js"></script>
    <script src="js/jquery.jplayer.min.js"></script>
</head>
<body>

<div class="quiz_ugc_box">
<div class="print_title" style="height: 50px; background: url(js/zuoye_logo.png) no-repeat 34px center #00b4ff;margin-bottom: 10px;"></div>
<#if paper??>
    <div class="listen-box">
        <h2 class="text_black">${paper.title!}</h2>

        <div class="listen-list">
            <#if paper.questions??>
            <#list paper.questions as question>
                <div class="list-box">
                    <div class="title">
                        <span class="topic" style="">第${question_index + 1}题</span>
                        <br/>
                        <p>
                            <#if question.tipSentence??>
                                <li class="tts_li_play" id="${question.tipSentence.voice!}">
                                                <span style="width:550px;display: inline-block">
                                                ${question.tipSentence.content!}
                                                </span>
                                    <input type="button" class="lis_step1_play tts_play" value="播放" style="position: absolute;right: 0;bottom: 0;"/>
                                </li>
                            </#if>
                        </p>
                    </div>
                    <div class="content">
                        <#list question.subQuestions as subQuestion>
                            <div class="container">
                                <p>${subQuestion_index + 1}. </p>
                                <ul>
                                    <#list subQuestion.sentences as sentence>
                                        <#if sentence??>
                                            <li class="tts_li_play" id="${sentence.voice!}">
                                                            <div style="width:550px;display: inline-block" class="text">
                                                                <div class="role_${sentence.role!}">&nbsp;</div>:
                                                                ${sentence.content!}
                                                                <#--<input class="lis_step1_play tts_play" type="button" value="播放"/>-->
                                                            </div>
                                                <input type="button" class="lis_step1_play tts_play" value="播放" style="position: absolute;right: 0;bottom: 0;"/>
                                            </li>
                                        </#if>
                                    </#list>
                                </ul>
                            </div>
                        </#list>
                    </div>
                </div>
            </#list>
            </#if>
        </div>

    </div>
    <div id="jquery_jplayer_1" class="jp-jplayer"></div>
<#else>
    找不到该听力试卷
</#if>
</div>
<div class="quiz_footPopUpMenu">
    <div class="inner">
        <a id="tts_play_id" style="" href="javascript:void(0);">播放</a>
        <a id="tts_stop_id" class="quizBtn quizBtn_blue quizBtn_well lis_next" href="javascript:void(0);">停止</a>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        var TApp = ( function () {
            // 初始化jPlayer
            var jPlayer = $('#jquery_jplayer_1').jPlayer({
                        ready: function (event) {
                        },
                        pause: function () {
                        },
                        ended: function () {
                            var currObj = data[ index ],
                                    nextObj = data[ ++index ];
                            // 还有要播放的音频
                            if (nextObj) {
                                // 标记要播放的语句
                                TApp.markPlaying( nextObj.voice );
                                // 填充音频
                                jPlayer.jPlayer("setMedia", { mp3: 'js/' + nextObj.voice + ".mp3"});
                                // 间隔播放
                                disState.taskId = setTimeout(function () {
                                    // 设为jplayer状态
                                    disState.setJplayer();
                                    app._play();
                                }, currObj.pause * 1000);
                                // 播放结束
                            } else {
                                // 回置索引
                                index = -1;
                                app.togglePlayElemValue();
                            }
                        },
                        swfPath: "js",
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
                    // 移上句子的样式变化
                    $('body').on('mouseenter mouseleave', 'li.tts_li_play', function ( e ) {
                        e.type === 'mouseenter' ? $( this).addClass( markerHoverSel ) :
                               $( this).removeClass( markerHoverSel );
                    } );

                    // 点击播放
                    $('body').on( 'click', 'input.tts_play', function () {
                        var elem = $( this).closest( 'li');

                        // 停止
                        app.stop( true );
                        index = +elem.attr('index') - 1;
                        app.play( true );
                        app.markPlaying( elem.attr( 'id' ) );
                    } );
                },
                // 播放按钮值的切换
                togglePlayElemValue: function (value) {
                    app.playElem.text(value || ( app.playElem.text() === '播放' ? '暂停' : '播放' ));
                },
                playOrPause: function () {
                    $(this).text() === '播放' ? app.play() : app.pause();
                },
                play: function (nostart) {
                    app.togglePlayElemValue();
                    // 没开始播放
                    if ( !app.started() || nostart ) {
                        ++index;
                        // 填音频对象
                        jPlayer.jPlayer("setMedia", { mp3: 'js/' + data[ index ].voice + ".mp3" });
                        // 标记要播放的语句
                        this.markPlaying( data[ index ].voice );
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
                pause: function (value/* 播放按钮的值 */) {
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
                stop: function () {
                    // 开始播了
                    if (app.started()) {
                        app.pause('播放');
                        index = -1;
                    }
                }
            };
        }() );
        TApp.init();
    });

</script>
</body>
</html>