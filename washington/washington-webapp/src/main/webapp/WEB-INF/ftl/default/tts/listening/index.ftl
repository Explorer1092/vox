<#import "../module.ftl" as temp />
<@temp.page level="制作">
<style type="text/css">
    .quiz_ys_border { overflow: hidden;  *zoom:1;padding: 35px 0 35px 15px; }
    .quiz_ugc_box .horizontal_vox dt { float: left; width: 14%; line-height: 28px; }
    .tts_disable_color { background-color: #ccc; }
    .tts_red_color { background-color: #ffefef; border-color: #ff3333; }
    .quiz_ugc_box .horizontal_vox dd { float: left; margin-left: 0; width: 85%; }
    hr { border-top: none; border-bottom: 1px solid #ccc; border-left: none; border-right: none; }
    .select_vox { z-index: 10; position: relative; }
    .select_vox ul { background-color: #fff; position: relative; z-index: 999; margin-top: -20px; margin-left: -5px; border: 0 none; }
    .select_vox .area { overflow: hidden;  *zoom:1;}
    .quiz_ugc_box .select_vox { *display:inline; width: 250px!important;  *width: 250px; }
    .area .arrow {  *display: inline;}
    .quiz_ys_border dd {  *margin-bottom:10px; }
    .back { color: #189cfb; float: right; font-size: 18px; font-weight: normal; padding: 14px 14px 0; }
    /*修改更换英语教材样式*/
    .t-teachingMaterial{height:390px;overflow-y:auto;}
    div.jqi .jqimessage{
        margin-top:20px;
    }
    .w-background-gray input{
        float:left;
    }
</style>
<link href="http://v3.jiathis.com/code/css/jiathis_share.css" rel="stylesheet" type="text/css">
<!--prompt use iframe, fix select overlap bug in IE6-->
<!-- opts.useiframe && ($('object, applet').length > 0) -->
<!--[if IE 6]>
    <object></object>
<![endif]-->

<!--//start-->
<div class="m-main">
    <div class="w-base" style="margin-top: 15px;">
        <div class="w-base-container">
            <!-- jplayer -->
            <div id="jquery_jplayer_1" class="jp-jplayer"></div>
            <div id="tts_content" class="quiz_ugc_box"></div>
            <div class="quiz_footPopUpMenu" style="height: 66px;">
                <!--[if IE 6]>
                <iframe style="position:absolute;top:0;left:0;width:100%;height:100%;filter:alpha(opacity=0);"></iframe>
                <![endif]-->
                <div class="inner">
                    <a id="tts_make_complete" class="quizBtn quizBtn_blue quizBtn_well lis_next lis_make_complete" href="javascript:void(0);">完成 并 生成音频</a>
                    <#if enableShare??>
                        <input type="checkbox" value="1" id="tts_share" <#if share?? && share==0>  <#else> checked="checked" </#if> /><label for="tts_share">共享给其他老师</label>
                    <#else>
                        <input type="checkbox" value="1" id="tts_share" style="display: none"/>
                    </#if>
                </div>
            </div>
            <div class="quiz_footPopUpMenu_contrast"></div>
            <div id="points" class="quiz_ys_border" style="display: none; width: 500px; height: 400px; position: relative; top: -450px; left: 33%; background-color: white; z-index: 9999; overflow-y: scroll;"></div>
        </div>
    </div>
</div>

<!--end//-->
<script id="t:jiathis" type="text/html">
    <div style="font-size: x-large;text-align: center;width: 450px;margin-top: 20px;">恭喜！保存成功</div><br/>
</script>
<script id="t:TTS" type="text/html">
    <div>
        <div style="float:right;">
            <a style="font-size:14px;" class="link" href="/tts/listening.vpage">返回</a>
        </div>
        <h2 class="text_black">1、设置试卷基本属性</h2>
        <dl class="horizontal_vox quiz_ys_border">
            <dt><span class="text_red">[必填] </span>标题：</dt>
            <dd style="padding-bottom: 20px;">
                <input id="tts_title" placeholder="请输入标题" class="w-int check_mistake">
            </dd>
            <dt>考前播放音：</dt>
            <dd>
                <div id="tts_preface" class="select_vox" style="z-index: 10">
                    <a href="javascript:void(0);" class="area"><i class="arrow"></i><b class="title"></b></a>
                        <ul>
                            <li class="initPlay">无</li>
                            <li>系统内置提示语</li>
                            <li class="lis_make" data-make-title="制作卷首语">我要制作生成</li>
                        </ul>
                </div>
                <input class="lis_step1_play" type="button" value="播放"/>
            </dd>
            <dt>考后播放音：</dt>
            <dd>
                <div id="tts_afterword" class="select_vox" style="z-index: 8">
                    <a href="javascript:void(0);" class="area"><i class="arrow"></i><b class="title"></b></a>
                    <ul>
                        <li class="initPlay">无</li>
                        <li>系统内置提示语</li>
                        <li class="lis_make" data-make-title="制作卷尾语">我要制作生成</li>
                    </ul>
                </div>
                <input class="lis_step1_play" type="button" value="播放"/>
            </dd>
            <dt>每道题提示音：</dt>
            <dd>
                <div id="tts_reminder" class="select_vox" style="z-index: 8">
                    <a href="javascript:void(0);" class="area"><i class="arrow"></i><b class="title"></b></a>
                    <ul>
                        <li class="initPlay">提示音1</li>
                        <li>提示音2</li>
                        <li>提示音3</li>
                        <li>无</li>
                    </ul>
                </div>
                <input class="lis_step1_play" type="button" value="播放"/>
            </dd>
            <dt style="height:28px;">所属教材：</dt>
            <dd>
                <span id="ttsTextbook" style="line-height: 28px;width: 262px;display: block;float: left;overflow: hidden;height: 28px;">无</span>
                <input type="hidden" id="ttsClassLevel" />
                <input type="hidden" id="ttsBookId" />
                <a class=" more_books_but quizBtn quizBtn_blue quizBtn_well lis_next lis_make_complete" href="javascript:void(0);" style="padding: 3px 8px;margin-top: 3px;">切换教材</a>
            </dd>
        </dl>
    </div>
    <div style="margin-top:20px;">
        <h1 style="font-size: 24px; font-weight: normal;margin: 10px 0 15px 0">2、添加大题</h1>
        <div style="border: 1px solid #ccc;  padding: 10px;">
            <div class="lis_big_topic" style="font-size: 22px; ">
                <p style="padding-left: 20px;"> 第<span class="big_topic_index">1</span>大题：</p>
                <!-- 题目设置 -->
                <div style="font-size: 14px; font-weight: normal; padding: 21px 44px;">
                    <span style="font-size: 16px; display: block; height: 30px;">题目设置：</span>
                    <div style="padding-left: 30px">
                        <div style="padding-bottom: 15px;">
                            <span>提示语： </span>
                            <div class="select_vox tts_tip">
                                <a href="javascript:void(0);" class="area"><i class="arrow"></i><b class="title"></b></a>
                                <ul style="z-index: 999;">
                                    <li class="initPlay tts_url_key">无</li>
                                    <li class="lis_make" data-make-title="制作提示语">我要制作生成</li>
                                </ul>
                            </div>
                            <input class="lis_step1_play" type="button" value="播放"/>
                        </div>
                        <div  style="padding-bottom: 15px;">
                            每道题播放次数：
                            <input class="tts_change_count" type="button" value="-"/>
                            <input class="play_count_text" disabled type="text" value="1"/>
                            <input class="tts_change_count" type="button" value="+"/>
                            遍
                        </div>
                        <div  style="padding-bottom: 15px;">
                            每道题作答时间：
                            <input class="tts_change_count" type="button" value="-"/>
                            <input class="split_time_text" disabled type="text" value="1"/>
                            <input class="tts_change_count" type="button" value="+"/>
                            秒
                        </div>
                    </div>
                </div>
                <hr/>
                <!-- 第一题 -->
                <div class="lis_small_topic" style=" border: 1px  solid #ccc; margin-bottom: 20px;">
                    <p style="font-size: 16px; font-weight: normal; padding: 15px 0 25px 0;margin-left: 22px;">第<span class="small_topic_index">1</span>题：
                        <span style="color:red">(请按角色添加对话内容，点击“添加”按钮，选择角色并录入对话内容。)</span>
                    </p>
                    <div class="lis_paragraph" style="margin-bottom: 40px; padding: 5px 0 5px 40px; font-size: 14px; font-weight: normal;" >
                        <div style="border: 1px solid #ccc; width: 584px;_width:590px; padding:10px 5px;">
                            <div style="padding-bottom: 20px;">
                                角色：
                                <select class="lis_role w-int" style="width: 95px; height: 22px;">
                                    <option class="tts_chinese" >中文</option>
                                    <option>美音男音</option>
                                    <option>美音女音</option>
                                    <option>美音童音</option>
                                    <option>英音男音</option>
                                    <option>英音女音</option>
                                </select>
                                音量：
                                <select class="lis_volume w-int" style="width: 90px; height: 22px;">
                                    <option>1</option>
                                    <option>2</option>
                                    <option selected>3</option>
                                    <option>4</option>
                                    <option>5</option>
                                </select>
                                语速：
                                <select class="lis_speed w-int" style="width: 90px; height: 22px;">
                                    <option>1</option>
                                    <option>2</option>
                                    <option selected>3</option>
                                    <option>4</option>
                                    <option>5</option>
                                </select>
                                <span class="tts_parse" style="opacity: 0;">
                                    尾音停顿：
                                    <select class="lis_pause w-int" style="width: 75px; height: 22px;">
                                        <option selected>0.5秒</option>
                                        <option>1秒</option>
                                        <option>1.5秒</option>
                                        <option>2秒</option>
                                        <option>3秒</option>
                                        <option>4秒</option>
                                        <option>5秒</option>
                                        <option>6秒</option>
                                        <option>7秒</option>
                                        <option>8秒</option>
                                        <option>9秒</option>
                                        <option>10秒</option>
                                    </select>
                                </span>
                            </div>
                            <div>
                                <span style="vertical-align: top;">内容：</span>
                                <textarea class="small_topic_content" style="width:390px;height:70px; overflow-y: auto; overflow-x: hidden;" ></textarea>
                                <span style="vertical-align: top;">
                                    试听:<input class="lis_audition" type="button" value="播放" style="vertical-align: top; margin: 15px;"/>
                                </span>
                                <span class="tts_span_tip" style="margin-left: 46px; color: red;"></span>
                            </div>
                        </div>
                        <p style=" margin: 10px; text-align: right;">
                            <a  class="add_paragraph quizBtn quizBtn_blue quizBtn_well lis_next" href="javascript:void(0);">添加</a>

                        </p>

                    </div>
                    <div style="text-align: center; margin-top: 40px; padding:10px;">
                        <a class="listen_small_topic quizBtn quizBtn_blue quizBtn_well lis_next" href="javascript:void(0);">试听小题</a>
                        <a class="add_small_topic quizBtn quizBtn_blue quizBtn_well lis_next" href="javascript:void(0);">添加小题</a>
                    </div>
                </div>
                <div style="text-align: center;">
                    <a class="add_big_topic quizBtn quizBtn_blue quizBtn_well lis_next" href="javascript:void(0);">添加大题</a>
                </div>
            </div>
        </div>
    </div>
</script>
<script type="text/javascript">
var ListenView, ListenController, ListenModel;
$( function () {
    var
    // 控制器的原型
            ctrolProto;

    // 模型（负责整个Listen制作工具的数据、逻辑）
    ListenModel = function () {
        var
        // 一次性加载播放音
                cache = this.playCache = {
                    keys : 'preface_without preface_default afterword_without afterword_default reminder_sound1 reminder_sound2 reminder_sound3 reminder_sound4'.split( " " ),
                    values: ',53f481a180adb98d1e8b46e2,,53f481b680adb9451c8b462c,53f4814580adb98d1e8b46e0,53f4817180adb9e01b8b461c,53f4818980adb9df1b8b465e,'.split( ',' )
                };
        // 给this.playCache添加属性
        $.each( cache.keys, function ( key, value ) {
            cache[ value ] = cache.values[ key ];
        } );
    };
    // 视图（负责显示，给控制器发送指令）
    ListenView = function ( model, controller, options ) {
        this.model = model;
        this.controller = controller;
        $.extend( this, options );
    };
    // 制作音频弹出窗口的“确定”和"取消"按钮的html
    ListenView.popBtnHtml = '<div style="margin-top:20px;margin-bottom:20px;text-align: center;"><a class="make_pop_cancle_btn quizBtn quizBtn_blue quizBtn_well" '+
             'href="javascript:void(0);">取消</a><a class="make_pop_confirm_btn quizBtn quizBtn_blue quizBtn_well" href="javascript:void(0);">确定</a></div>';
    $.extend( ListenView.prototype, {
        update: function ( model ) {
        },
        init: function ( options ) {
            // 渲染
            this.ttsContent.html( template( this.ttsView, {} ) );
            // 注册事件
            this.registerEvent();
            // 相关属性初始化
            this.relDataInit( options );
            // 初始化试卷
            this.controller.initData( this );
        },
        // 记录模板，方便添加、删除等操作
        rememberTemp: function () {
            var elemJ = $( this.paraSel );
            // 我要制作 的 html
            this.makeTempStr = elemJ.clone().find( this.addParaSel ).remove().end()[0].outerHTML;
            elemJ.find( 'option.tts_chinese').remove().end().find( 'span.tts_parse').css( 'opacity', '1' );
            // 小片段
            this.smallPart = elemJ.clone().find( this.addParaSel )
                    .after( '<a  class="del_paragraph quizBtn quizBtn_blue quizBtn_well lis_next" href="javascript:void(0);" >删除</a> ').end();
            // 小题
            this.smallTopic = $( this.smallSel ).clone().find( this.lisSmallTopicSel )
                    .after( '<a class="del_small_topic quizBtn quizBtn_blue quizBtn_well lis_next" href="javascript:void(0);">删除本小题</a>').end();
            // 大题
            this.bigTopic = $( this.bigSel ).clone().find( this.addGigSel )
                    .after( '<a class="del_big_topic quizBtn quizBtn_blue quizBtn_well lis_next" href="javascript:void(0);">删除大题</a>').end();
        },
        // 选择播放音辅助设置 作用于 考前播放音 考后播放音 每道题提示音
        assistPlay: function ( elem /* 处理提示语 */ ) {
            elem ? elem.data( this.controller.urlKey, "") :
                    this.controller.assistPlay( $.merge( $.merge( this.preface.find( 'li:lt(2)'), this.afterword.find( 'li:lt(2)') ), this.reminder.find( 'li') ) );
        },
        // 注册及初始化
        registerAndInit: function ( elem ) {
            $.vox.select( elem, -1);
            $( this.initPlaySel ).click();
        },
        // 添加及初始化
        addAndInit: function ( elem, tip ) {
            $.vox.select( elem, -1);
            $(tip).find( this.initPlaySel ).click();
        },
        // 相关数据初始化
        relDataInit: function ( options ) {
            var self = this,
                    c = self.controller,
                    attrs = 'preface afterword reminder'.split( " " );

            $.each( '#tts_preface #tts_afterword #tts_reminder'.split( " " ), function ( i, value ) {
                // 注册 卷首语、卷尾语、提示音的文本框
                $.vox.select( value, -1);
                // 给对象添加三个属性 preface afterword reminder
                self[ attrs[ i ] ] = $( value ).children( 'ul' );
            } );
            // 8个li.tts_url_key初始化
            self.assistPlay( $( self.tipSel + " " + self.initPlaySel ) );
            self.assistPlay();
            self.registerAndInit( $( self.tipSel ) );
            // 段内初始化
            self.paragraphInInit();
            // 记录模板
            self.rememberTemp();
            // 初始化jplayer
            c.jPlayInit( self.jplayer );
            // 将options传给ctroller
            c.extendOptions( options, self );
        },
        // 给段内的角色、音量、语速、尾音、内容设置值
        paragraphInInit: function () {
            var t = this,
                    sel = 'option',
            // 设置 音量、语速、段内角色、尾音停顿的value
                    values = [,, 'p m f c bm bf'.split( ' ' ), '0.5 1 1.5 2 3 4 5 6 7 8 9 10'.split( ' ') ];

            $.each( [ $( t.lisVolumeSel ), $( t.lisSpeedSel ), $( t.lisRoleSel ), $( t.lisPauseSel ) ], function ( i ) {
                this.children( sel ).each( function ( k ) {
                    $( this ).val( values[ i ] ? values[ i ][ k ] : k + 1 );
                } );
            } );
            // 设置内容的value
            $( t.paraContentSel ).val( '' );
        },
        registerEvent: function () {
            var body = $( 'body'),
            // 播放按钮状态函数
                    towStatePlay,
                    self = this,
                    ctrol = self.controller,
            // 添加小题 添加大题 删除本小题 删除大题 添加片段 删除片段
                    names = "add_small_topic add_big_topic del_small_topic del_big_topic add_paragraph del_paragraph".split( " " ),
                    mergeSelector = ( function () {
                        return $.map( names, function ( v ) {
                            return "a." + v;
                        } ).join( "," );
                    } () ),
            // 小题选择器
                    smaSel = self.smallSel,
            // 片段选择器
                    paraSel = self.paraSel,
            // 大题选择器
                    bigSel = self.bigSel,
            // 片段中的内容的选择器
                    contentSel = self.paraContentSel,
            // 请求音频
                    requestRadio = function () {
                        // 只生成音频
                        ctrol.playSentence( $( this ).closest( paraSel ), function ( elem /* jQuery:paragraph or ul*/, data ) {
                            // 设置value
                            ctrol.setUrlKeyData( elem, data);
                            ctrol.setDurationKeyData( elem, data);
                        } );
                    },
            // 请求音频处理函数
                    requestRadioFn = function () {
                        // 记录新申请音频
                        ctrol.ApplingAudioObj.addOne();
                        ctrol.deferrTask( requestRadio, this/* 事件源 */ );
                    },
                    lisMakeFn = function ( e, t, title ) {
                        var t = arguments[1] || $( this ).parent(),
                            paragraph;
                        $.prompt( self.makeTempStr + ListenView.popBtnHtml, {
                            title: title,
                            buttons: {},
                            position: { width: 699 },
                            useiframe: true
                        } );
                        // 给确定、取消按钮注册(多次注册不知是否影响效率)
                        $('a.make_pop_confirm_btn').click( function () {
                            var tip = paragraph.find( ctrol.spanTipSel);
                            // 没内容，则提示
                            if ( !jQuery.trim(paragraph.find('textarea').val()).length ) {
                                tip.text('请输入内容');
                            }
                            // 有提示(请输入内容 or 字数过多 or 英文中有汉字)
                            if ( tip.text() ) {
                                return;
                            }
                            // 保存数据,以便接着之前的数据编辑
                            ctrol.saveMake( t, paragraph );
                            // 清除,防止请求中点击播放而播放上一个音频
                            ctrol.setUrlKeyData( t, "" );
                            // 生成音频，保存音频地址
                            ctrol.playSentence( paragraph, function ( elem /* jQuery:paragraph or ul*/, data ) {
                                // 设置value
                                ctrol.setUrlKeyData( t, data );
                                ctrol.setDurationKeyData( t, data );
                                // 失败也保存，让后台再生成
                                t.data( ctrol.objKey, ctrol.getParaData( elem ) );
                                t.data( ctrol.objKey).duration = t.data( ctrol.durationKey );
                            } );
                            jQuery.prompt.close();
                        });

                        $('a.make_pop_cancle_btn, div.jqiclose').click( function () {
                            // 选项改成"无"
                            var flag = !t.data(this.urlKey).tts_url_key && !jQuery.trim(paragraph.find('textarea').val()).length;
                            // ctrol.clearObjKey( t, true/* 清除urlKey */ );
                            jQuery.prompt.close();
                            if ( flag ) {
                                t.find('li').eq(0).click();
                            }
                        });
                        // 显示之前设置的数据
                        paragraph = $( 'div.jqistates '+ self.paraSel );
                        ctrol.showParaData( t, paragraph );
                        // 验证字数是否过多
                        paragraph.find( self.paraContentSel).keyup();
                    },
                    // 多功能函数：添加删除大小题及句子
                    addOrDelTopic = ( function () {
                        var
                        // 建立函数映射
                                map = {},
                                fns = [
                                    function () { // 添加小题
                                        var targetElem = $( this).closest( smaSel );
                                        targetElem.after( self.smallTopic.clone() );
                                        // 设置音量、语速默认值
                                        ctrol.dealInitValue( targetElem.next() );
                                    },
                                    function () { // 添加大题
                                        var bigTopic = self.bigTopic.clone(),
                                                targetElem = $( this ).closest( bigSel),
                                                next,
                                                tip;
                                        targetElem.after( bigTopic );
                                        next = targetElem.next();
                                        tip = next.find( self.tipSel );
                                        // 初始化提示语
                                        self.assistPlay( tip.find( self.initPlaySel ) );
                                        self.addAndInit( tip, next );
                                        // 设置音量、语速默认值
                                        ctrol.dealInitValue( next );
                                    },
                                    function () { // 删除小题
                                        // 本大题有>1的小题才能删除
                                        if ( $( this).closest( bigSel ).find( smaSel ).length > 1 ) {
                                            $( this).closest( smaSel ).remove();
                                        }
                                    },
                                    function () { // 删除大题
                                        // 本大题有>1的小题才能删除
                                        if ( $( bigSel ).length > 1 ) {
                                            $( this ).closest( bigSel ).remove();
                                        }
                                    },
                                    function () { // 添加句子
                                        var targetElem = $( this ).closest( paraSel),
                                                newElem = self.smallPart.clone();
                                        targetElem.after( newElem );
                                        // 男音后是女音，反之也可以

                                        targetElem.find( self.lisRoleSel ).val() === 'm' && newElem.find( self.lisRoleSel ).val( 'f' );
                                        // 更新音频
                                        $( this).parent().find( contentSel ).blur();
                                        // 设置音量、语速默认值
                                        ctrol.dealInitValue( targetElem.next() );
                                    },
                                    function () { // 删除句子
                                        $( this ).closest( paraSel ).remove();
                                    }
                                ];
                        $.each( names, function ( i ) {
                            map[ this ] = fns[ i ];
                        } );
                        function getFn( t ) {
                            var r;
                            $.each( map, function ( key, value ) {
                                if ( t.hasClass( key ) ) {
                                    r = value;
                                    return false;
                                }
                            } );
                            return r;
                        }
                        return function ( e ) {
                            getFn( $( this ) ).call( this );
                            // 刷新索引
                            ctrol.refreshIndex();
                        };
                    } () );
            // 以下是step1
            // 选择播放音。7个li 播放音
            body.on( 'click', self.li7Sel, function () {
                var t = $( this );
                ctrol.choicePrompt( t );
                ctrol.clearObjKey( t.parent() );
            });
            towStatePlay = function ( elem ) {
                // 停止 播放 两种状态的改变
                var t = $( this ),
                        v = t.val(),
                        state3 = ctrol.threeState;
                if ( v === '播放' ) {
                    // 记录正在操作的按钮
                    state3.remember( t );
                    ctrol.playSentence( elem, 'not_generate'/* 不生成，直接播放 */ );
                    // 停止音乐
                } else {
                    state3.stop( t );
                }
            };
            // 给step1中3个“播放“注册
            body.on( 'click', self.playBtnSel, function () {
                towStatePlay.call( this, $( this ).prev().find( 'ul' ) );
            } );
            // 以下是step2
            // 给step2中“提示语”右侧的“播放”注册
            body.on( 'click', self.playBtn2Sel, function () {
                towStatePlay.call( this, $( this ).prev() );
            } );
            // 调整播放次数 每次加减1，间隔时间可以是小数，播放次数是整数
            body.on( 'click', self.changeCountSel, function ( e ) {
                ctrol.mPositiveInteger( $( this ), $( this).parent().find( self.splitSel ).length ? true : false )
            } );
            // 添加小题 添加大题 删除本小题 删除大题 添加片段 删除片段
            body.on( 'click', mergeSelector, function ( evt ) {
                var isDel = $( this ).text().indexOf( '删除' ) > -1 ,
                    self = this;
                if ( isDel ) {
                    $.prompt("是否确认要删除？", {
                        title: "系统提示",
                        buttons: { "取消": false, "确定": true },
                        submit: function( e,v ){
                            if ( v ) {
                                addOrDelTopic.call( self, evt );
                            }
                        },
                        useiframe:true
                    });
                } else {
                    addOrDelTopic.call( self, evt );
                }
            } );
            // 试听小题
            body.on( 'click', self.lisSmallTopicSel, function () {
                ctrol.audition( $( this ).closest( smaSel ).find( paraSel ) );
            } );
            // 试听片段
            body.on( 'click', self.lisParaSel, function () {
                // 有提示(请输入内容 or 字数过多 or 英文中有汉字)
                if ( $(this).closest( paraSel ).find( ctrol.spanTipSel).text() ) {
                    return;
                }
                var contentElem, t = $( this ),
                    v = t.val(),
                    state3 = ctrol.threeState;

                if ( v === '播放' ) {
                    // 记录正在操作的按钮
                    state3.remember( t );
                    state3.playFlag( true ); // 标记 请求中点击时的问题 ( 要放在remember下面)
                    ctrol.playSentence( t.closest( paraSel ), function ( elem /* jQuery:paragraph or ul*/, data ) {
                        // 设置value
                        ctrol.setUrlKeyData( elem, data );
                        ctrol.setDurationKeyData( elem, data );
                        // 请求时问题仍存在
                        state3.playFlag() && ctrol.playSentence( elem, 'not_generate' );
                    }, true/*标记是通过点击“播放”*/  );
                } else if ( v === '播放中'){
                    state3.stop( t );
                }
            });
            // 解决生成音频事件过长 强制让用户完成字数过多的句子
            body.on( 'blur', contentSel, function () {
                var t = $( this );
                if ( !ctrol.isCrossBorder( t ) ) {
                    requestRadioFn.call( this );
                } else {
                    t.focus();
                }
            } );
            // 字数过多 or 英文中有中文的提示
            body.on( 'keyup', contentSel, function () {
                ctrol.crossBorder( $( this ) );
            } );
            body.on( 'change', [ self.lisRoleSel, self.lisVolumeSel, self.lisSpeedSel ].join( ',' ), requestRadioFn );
            // 3个我要制作
            body.on( 'click', self.lisMakeSel, function ( e ) {
                lisMakeFn.call( this, e, null, $( this ).attr( 'data-make-title' ) );
            } );
            // 给"我要制作生成"注册事件
            body.on( 'click', self.makeCompleteSel, function () {
                self.controller.makeComplete();
            } );
        }
    } );

    // 控制器(接受视图的用户输入，给模型发送指令)
    ListenController = function ( model ) {
        this.model = model;
        this.initThreeState( this );
    };
    ctrolProto = ListenController.prototype;
    $.extend( ctrolProto, {
        englishHasChineseMes: '英文中不能有汉字',
        moreContentMes: '录入内容过多，请删减',
        chineseRe : /[\u4e00-\u9fa5]/, //中文正则
        // 初始化视图数据
        initData: function ( v ) {
            var data;
            if ( !this.model.pageData ) {
                return;
            }
            data = this.model.pageData;
            // 初始化标题
            $( this.titleSel ).val( data.title );
            // 初始化考前播放音
            this._initPlay( v.preface, data, 'beginningSentence', 'beginningVoice' );
            // 初始化考后播放音
            this._initPlay( v.afterword, data, 'endingSentence', 'endingVoice' );
            // 初始化提示音
            this._initReminder( v.reminder, data );

            // 初始化教材
            $('#ttsClassLevel').val(data.classLevel);
            $('#ttsBookId').val(data.bookId);
            $('#ttsTextbook').text(data.bookName);

            // 初始化大题
            this._initBigTopicCtrol( data );
            // 验证字数是否过多
            $( this.paraContentSel ).keyup();
        },
        // 初始化考前播放音、考后播放音
        _initPlay: function ( elem, data, sentenceName, voiceName ) {
            var sentence = data[ sentenceName ],
                 voice = data[ voiceName ];

            if ( sentence ) { // 制作
                // 设置 音频value
                elem.data( this.urlKey, voice );
                // 设置 objKey
                elem.data( this.objKey, sentence );
                // 设置 makeKey
                elem.data( this.makeKey, sentence );
                // 改变select选项
                elem.prev().find( 'b').text( '我要制作生成' );
            } else if ( voice ) { // 默认音
                elem.data( this.urlKey, voice );
                elem.prev().find( 'b').text( '系统内置提示语' );
            }
        },
        _initReminder: function ( elem, data ) {
            var voice = data.intervalVoice,
                 self = this;
            // 设置 音频value
            elem.data( self.urlKey, voice );
            // 选中li
            elem.find( 'li').each( function () {
                if ( $( this ).data( self.urlKey ) === voice ) {
                    elem.prev().find( 'b').text( $( this ).text() );
                    return false;
                }
            } );
        },
        // 初始化大题控制器
        _initBigTopicCtrol: function ( data ) {
            var elem,
                datas = data.questions,
                i = 1,
                len = datas.length;
            // 初始化句子
            elem = $( this.bigSel );
            // 初始化第一道大题
            this._initBigTopic( elem, datas[ 0 ] );
            for ( ; i < len; i++ ) {
                // 添加大题
                elem.find( this.addGigSel ).click();
                elem = elem.next();
                this._initBigTopic( elem, datas[ i ] );
            }
        },
        // 初始化大题
        _initBigTopic: function ( elem/*大题的元素*/, data/*大题的数据*/ ) {
            // 初始化 题目设置
            this._initPlay( elem.find( this.tipSel + " > ul"), data, 'tipSentence', 'tip' ); // 初始化 提示语
            elem.find( this.playCSel ).val( data.playTimes ); // 初始化 播放次数
            elem.find( this.splitSel ).val( data.interval ); // 初始化 间隔时间

            // 初始化小题控制器
            var datas = data.subQuestions,
                i = 1,
                len = datas.length;
            // 初始化第一小题
            elem = elem.find( this.smallSel );
            this._initSmallTopic( elem, datas[0] );
            for ( ; i < len; i++ ) {
                // 添加小题
                elem.find( this.addSmallSel ).click();
                elem = elem.next();
                this._initSmallTopic( elem, datas[ i ] );
            }
        },
        // 初始化小题
        _initSmallTopic: function ( elem/*小题的元素*/, data/*小题的数据*/ ) {
            var datas = data.sentences,
                i = 1,
                len = datas.length;
            // 初始化句子
            elem = elem.find( this.paraSel );
            // 初始化第一个句子
            this._initSentence( elem, datas[0] );
            for ( ; i < len; i++ ) {
                // 添加句子
                elem.find( this.addParaSel).click();
                elem = elem.next();
                this._initSentence( elem, datas[ i ] );
            }
        },
        // 初始化句子
        _initSentence: function ( elem/*句子的元素*/, data/*句子的数据*/ ) {
            // 设置角色 音量
            elem.find( this.lisRoleSel ).val( data.role );
            elem.find( this.lisVolumeSel ).val( data.volume );
            elem.find( this.lisSpeedSel ).val( data.speed );
            elem.find( this.lisPauseSel ).val( data.pause );
            elem.find( 'textarea' ).val( data.content );
            elem.data( this.urlKey, data.voice );
            elem.data( this.durationKey, data.duration );

        },
        // 音量、语速默认为3
        dealInitValue: function ( elem ) {
            elem.find( this.lisVolumeSel + "," + this.lisSpeedSel).val( 3 );
        },
        // 选择音频(无需发送，一开始就都保存在li的data中)
        choicePrompt: function ( target ) {
            var data = {};
            // 获取音频
            data[ this.urlKey ] = target.data( this.urlKey );
            // 设置数据
            target.parent().data( data );
        },
        // 开始/停止 等待 播放 状态的对象
        initThreeState: function ( ctrol ) {
            var _elem,
                    __elem,
            // 解决请求中时的问题
                    flag;
            ctrol.threeState = {
                remember: function ( elem ) {
                    // 先stop其它正在播放的音频
                    this.stop();
                    __elem = _elem = elem;
                },
                switchState: function ( state, notDelete ) {
                    if ( _elem ) {
                        _elem.val( state || '播放中' );
                        state
                                ? _elem.addClass('lis_step1_play_waiting')
                                : (_elem.removeClass('lis_step1_play_waiting'),_elem.addClass('lis_step1_play_active'));
                        !notDelete && this.clear();
                    }
                },
                clear: function () {
                    _elem = null;
                },
                stop: function ( elem ) {
                    elem = elem || __elem || _elem;
                    if ( elem ) {
                        ctrol.playerInstance.jPlayer( 'stop' );
                        elem.val( '播放' );
                        elem.removeClass('lis_step1_play_active');
                        elem.removeClass('lis_step1_play_waiting');
                        _elem = __elem = null;
                    }
                    this.playFlag( false );
                },
                playFlag: function ( v ) {
                    return v !== undefined ? ( flag = v ) : flag;
                }
            };
        },
        // 清除objKey
        clearObjKey: function ( elem, flag/* 清除urlKey */ ) {
            elem.data( this.objKey, null );
            flag && elem.data( this.urlKey, "" );
        },
        // 继承属性
        extendOptions: function ( o, view ) {
            $.extend( ctrolProto, o );
            view && ( this.view = view );
        },
        // data相关方法
        // 既是data中的key，也是7个li 播放音 的class
        urlKey: 'tts_url_key',
        // duration的key
        durationKey: 'durationKey',
        // 考前播放、考后播放、提示音：放对象的缓存key
        objKey: 'tts_obj_key',
        // 保存制作的数据(其实可以看成是objKey的备份，但不对应后端。objKey要时刻做好与后端通信的准备)
        makeKey: 'tts_make_key',
        setUrlKeyData: function ( elem, data ) {
            elem.data( this.urlKey, typeof data === 'string' ? data : data.success ? data.info : "" );
        },
        setDurationKeyData: function ( elem, data ) {
            elem.data( this.durationKey, data.success ? data.value : "" );
        },
        // 初始化jplayer
        jPlayInit: function ( playerElem ) {
            var self = this;
            self.playerInstance =  playerElem.jPlayer( {
                ready: function (event) {},
                pause: function() {
                    $(this).jPlayer("clearMedia");
                },
                ended: function() {
                    var currP,nextP;
                    // 切换成停止样式
                    self.threeState.stop();
                    // 排除试听小题以外：例如点击播放
                    if ( self.parasCacheNum() === undefined ) {
                        return;
                    }
                    currP = self.currentParagraph();
                    nextP = self.nextParagraph();
                    // 有下一个
                    if ( nextP ) {
                        window.setTimeout( function () { self.playSentence( nextP, 'not_generate' ); },
                                ( +$( currP ).find( self.lisPauseSel ).val() ) * 1000 );
                        // 没下一个了
                    } else {
                        self.parasCacheNum( undefined );
                    }
                },
                solution:"flash, html",
                swfPath: "/public/plugin/jPlayer/",
                supplied: "mp3",
                preload: "none",
                wmode: "window",
                keyEnabled: true
            } );
        },
        // 推迟执行
        deferrTask: function ( fn, context ) {
            var obj = this.clearDeferrTask;
            obj.taskId = setTimeout( function () {
                fn.call( context );
                delete obj.taskId;
            }, 200 );
        },
        // 清除要延迟的任务
        clearDeferrTask: function () {
            clearTimeout( this.clearDeferrTask.taskId );
            delete this.clearDeferrTask.taskId;
        },
        // 是正整数(判断 num >= integer 的正整数)
        isPosInteger: function ( num, integer, flag/* 是正数 */ ) {
            num = +num;
            integer = num > 0 && num >= integer;
            return flag ? integer : integer && Math.floor( num ) === Math.ceil( num );
        },
        // 调整播放次数
        mPositiveInteger: function ( t, flag/* 正数 */ ) {
            var
                    isPlus = t.val() === "+",
                    dom = isPlus ? t.prev() : t.next(),
                    count = +dom.val();
            isPlus ? this.isPosInteger( count, 1, flag ) && dom.val( count + 1 ) : this.isPosInteger( count, 2, flag ) && dom.val( count - 1 );
        },
        // 播放音辅助操作 添加class便于注册 缓存key便于获取播放音
        assistPlay: function ( j_elems ) {
            var
                    values = this.model.playCache.values,
                    data = {},
                    sk = this.urlKey;

            j_elems.each( function ( i ) {
                data[ sk ] = values[ i ];
                $( this ).data( data ).addClass( sk );
            } );
        },
        assisTopic: function ( select ) {
            select.data( this.urlKey, ""); // 默认为“无”
        },
        // 以下是step2
        // 是否正在申请音频
        ApplingAudioObj: ( function () {
            // 正在申请数
            var count = 0;
            return {
                // 增加申请数量
                addOne: function () {
                    ++count;
                },
                reduceOne: function () {
                    count > 0 && --count;
                },
                isWait: function () {
                    return count ? true : false;
                }
            };
        } () ),
        refreshIndex: function () {
            var view = this.view;
            // 大题索引
            $( view.bigIndexSel ).each( function ( i ) {
                var b = $( this );
                b.text( i + 1 );
                // 小题索引
                b.closest( view.bigSel ).find( view.smallIndexSel ).each( function ( i ) {
                    $( this ).text( i + 1 );
                } );
            } );
        },
        // 保存数据
        saveMake: function ( target, paragraph ) {
            target.data( this.makeKey, this.getParaData( paragraph ) );
        },
        // 得到句子的数据
        getParaData: function ( part,content, fn ) {
            var t = this, r;
            content = content || $.trim( part.find( 'textarea').val() );

            r = { role: part.find( t.lisRoleSel).val(),
                volume : +part.find( t.lisVolumeSel ).val(),
                speed: +part.find( t.lisSpeedSel ).val(),
                content: content
            };
            fn && fn( r, part);
            return r;
        },
        // 显示段之前的数据
        showParaData: function ( target, paragraph ) {
            var t = this;
            var data = target.data( t.makeKey );
            if ( data ) {
                paragraph.find( t.lisRoleSel ).val( data.role );
                paragraph.find( t.lisVolumeSel ).val( data.volume );
                paragraph.find( t.lisSpeedSel ).val( data.speed );
                paragraph.find( 'textarea' ).val( data.content );
            }
        },
        getSmallTopicData: function ( smaTopic ) {
            var self = this;
            return { sentences:
                    $.makeArray( smaTopic.find( self.paraSel ).map( function () {
                        return self.getParaData( $( this ), null, function ( r, part ) {
                            r.pause = part.find( self.lisPauseSel ).val();
                            r.voice = part.data( self.urlKey );
                            r.duration = part.data( self.durationKey );
                        } );
                    } ) )
            };
        },
        getBigTopicData: function ( bigTopic) {
            var self = this,
                    tipJ = bigTopic.find( self.li7Sel).parent(),
                    r = {};
            r.tip = tipJ.data( self.urlKey );
            r.tipSentence = tipJ.data( self.objKey )|| null;
            r.playTimes = bigTopic.find( self.playCSel).val();
            r.interval = bigTopic.find( self.splitSel).val();
            r.subQuestions = $.makeArray( bigTopic.find( self.smallSel ).map( function () {
                return self.getSmallTopicData( $( this ) );
            } ) );
            return r;
        },
        // 试听小题
        audition: function ( paragraphs ) {
            var self = this;
            // 未请求完音频
            if ( self.ApplingAudioObj.isWait() ) {
                self.callbacks = $.Callbacks();
                self.callbacks.add( self.playSmallTopic );
                self.paragraphs = paragraphs;
                // 播放本小题
            } else {
                self.playSmallTopic( paragraphs );
            }
        },
        // 缓存段
        parasCache: [],
        // 试听小题
        playSmallTopic: function ( paragraphs ) {
            var self = this;
            // 小题为空
            if ( paragraphs.length === 0 ) {
                return;
            }
            // 缓存段，用以控制段与段之间的间隔音
            self.parasCache = $.makeArray( paragraphs ).slice();
            self.parasCache.num = 0;
            // 播放第一段
            self.playSentence( paragraphs[ 0 ], 'not_generate' );
        },
        // 获取下一个
        nextParagraph: function () {
            if ( this.parasCache.num >= 0 ) {
                return this.getParagraph( ++this.parasCache.num );
            }
        },
        currentParagraph: function () {
            return this.getParagraph( this.parasCache.num );
        },
        // get and set
        parasCacheNum: function ( val ) {
            return val ? ( this.parasCache.num = val ) :  this.parasCache.num;
        },
        getParagraph: function ( index ) {
            return this.parasCache[ index ];
        },
        // 播放句子
        playSentence: function ( elem, flag/* 不生成，直接播放 */, isPlay /* 是人工播放操作，作用于样式切换 */ ) {
            var self = this, ajaxComplete, content;

            elem = $( elem );
            // 直接播放
            if ( typeof flag === 'string' ) {
                self._playSentence( elem );
                return;
            }
            content = $.trim( elem.find( 'textarea').val() );
            ajaxComplete = function ( data ) {
                self.ApplingAudioObj.reduceOne();
                // 回调函数
                $.isFunction( flag ) && flag( elem, data );
                // 执行 试听本小题
                if ( !self.ApplingAudioObj.isWait() && self.callbacks ) {
                    self.callbacks.fireWith( self, [ self.paragraphs ] );
                    delete self.callbacks;
                    delete self.paragraphs;
                }
            };
            // 有内容且验证通过才申请音频
            if ( content && !self.crossBorder( elem.find('textarea'), true ) ) {
                // 清除旧的url
                $.isFunction( flag ) && self.setUrlKeyData( elem, "" );
                if ( isPlay ) {
                    // 没删除，可能会导致有问题
                    self.threeState.switchState( '请求中', true/* 不删除 */ );
                }
                // 清除要延迟的任务
                self.clearDeferrTask.taskId && self.clearDeferrTask();
                $.ajax( {
                    type : 'post',
                    url : '/tts/listening/generateSentence.vpage',
                    data : $.toJSON( self.getParaData( elem, content ) ),
                    success : ajaxComplete,
                    error : function () {
                        // 超时，停止
                        self.threeState.stop();
                        // 信息提示
                        self.overTimeTip( elem.find( self.spanTipSel ), 3000 );
                    },
                    timeout: 6000,
                    dataType : 'json',
                    contentType : 'application/json;charset=UTF-8'
                } );
                // 清空音频url
            } else {
                self.ApplingAudioObj.reduceOne();
                elem.data( self.urlKey, content );
                // 清除
                self.threeState.clear();
            }
        },
        overTimeTip: function ( elem, time ) {
            elem.text( '语音仍在生成中，请稍候再播放' );
            setTimeout( function () {
                elem.text( '' );
            }, time )
        },
        _playSentence: function ( elem ) {
            var v = elem.data( this.urlKey ), p;
            if( v ) {
                // 切换状态
                this.threeState.switchState();
                p = this.playerInstance;
                p.jPlayer( "setMedia", { mp3: '${listeningUrl!}' + v } );
                p.jPlayer( 'play' );
            }
            // 清除
            this.threeState.clear();
        },
        // tts 验证
        ttsValidate: function () {
            var self = this;
            return !$.trim( $( self.titleSel ).val() ) ? '标题不能为空' :
                    !( function () {
                        var r = true;
                        $( self.playCSel ).each( function () {
                            if ( !self.isPosInteger( $( this).val(), 1 ) ) {
                                return r = false;
                            }
                        } );
                        return r;
                    } () ) ? '播放次数为正整数' :
                            !( function () {
                                var r = true;
                                $( self.splitSel ).each( function () {
                                    if ( !self.isPosInteger( $( this).val(), 1, true ) ) {
                                        return r = false;
                                    }
                                } );
                                return r;
                            } () ) ? '播放时间为正数' :
                                    !( function () {
                                        var r = true;
                                        $( self.paraContentSel ).each( function () {
                                            if ( !$.trim( $( this ).val() ) ) {
                                                return r = false;
                                            }
                                        } );
                                        return r;
                                    } () ) ? '内容不能为空' :
                                            !( function () {
                                                var r = true;
                                                // 考前播放音 考后播放音 所有提示语
                                                $( self.lisMakeSel ).parent().each( function () {
                                                    var obj = $( this).data( self.objKey );
                                                    if ( obj && obj.role !== 'p' && obj.content.length && self.chineseRe.test( obj.content ) ) {
                                                        return r = false;
                                                    }
                                                } );
                                                if ( r ) {
                                                    $( self.paraContentSel ).each( function () {
                                                        if ( self.chineseRe.test( $( this).val() ) ) {
                                                            return r = false;
                                                        }
                                                    } );
                                                }
                                                return r;
                                            } () ) ? self.englishHasChineseMes : !( function () {
                                                var ret = true,
                                                    tip,
                                                    tips = $( self.spanTipSel),
                                                    len = tips.length,
                                                    i = 0,
                                                    text;
                                                for ( ; i < len; i++) {
                                                    tip = tips.eq( i );
                                                    text = tip.text() || "";
                                                    if ( text.indexOf( self.moreContentMes ) > -1 ) {
                                                        tip.parent().find( 'textarea').focus();
                                                        ret = false;
                                                        break;
                                                    }
                                                }
                                                return ret;
                                            } () ) ? self.moreContentMes :"";
        },
        makeComplete: function () {
            var self = this, r = self.ttsValidate();
            if (!$('#ttsBookId').val() || $('#ttsBookId').val()==0){
                $17.alert("教材不能为空")
                return;
            }
            // 检验录入内容是否过多
            if ( r === self.moreContentMes ) {
                return;
            }
            r ? $17.alert( r ) : self._makeComplete();
        },
        _makeComplete: function () {
            var self = this,
                    bigTopic,smallTopic,
            // 考前播放音 考后播放音 提示音 的变量
                    temp5Arr = 'beginningVoice beginningSentence endingVoice endingSentence intervalVoice'.split( ' ' ),
                    r = { title: $.trim( $( self.titleSel ).val() ) };
            r.classLevel = $('#ttsClassLevel').val();
            r.bookId = $('#ttsBookId').val();
            r.bookName =$('#ttsTextbook').text();
            temp5Arr.index = 0;
            // 考前播放音 考后播放音 提示音
            $( self.li7Sel ).parent().each( function () {
                // 每次执行一对
                var t = $( this ),
                        temp = t.data( self.objKey );
                r[ temp5Arr[ temp5Arr.index++ ] ] = t.data( self.urlKey );
                // 有下一个
                temp5Arr[ temp5Arr.index + 1 ] && ( r[ temp5Arr[ temp5Arr.index++ ] ] = temp ? temp : null );
            } );
            r.questions = $.makeArray ( $( self.bigSel).map( function () {
                return self.getBigTopicData( $( this ) );
            } ) );
            <#if paper??>
                    r.id = '${id!}';
            </#if>
            r.share = $('#tts_share').is(':checked') ? 1 : 0;
            App.postJSON(
                    '/tts/listening/generatePaper.vpage',
                    r,
                    function ( data ) {
                        $17.tongji("TTS_完成次数", "");
                        if (!data.success){
                            $17.alert( '失败' );
                            return;
                        }
                        // 保存时触发MP3生成操作，不需要等待返回结果
                        $.ajax({
                            type: 'POST',
                            url: "/tts/listening/getCompleteVoice.vpage",
                            data: {
                                paperId:  data.value
                            },
                            timeout: 200
                        });
                        $17.tongji('TTS_html工具完成次数', '');
                        var html = template( "t:jiathis", {});
                        html += '<script> var jiathis_config={'
                                +'url: "http://www.17zuoye.com/tts_view.vpage?id='+data.value+'",'
                                +'data_track_clickback:true,'
                                +'title: "#一起作业网免费制作听力材料#",'
                                +'summary:"我在@一起作业网 免费制作了听力材料，录入文字系统生成声音，发音标准流畅。大家快来听一下吧。",'
                                +'shortUrl:false,'
                                +'hideMore:false'
                                +'};</'+'script>';

                        html += '<script type="text/javascript" src="http://v3.jiathis.com/code/jia.js?uid=1613716" charset="utf-8" />';

                        $.prompt( html , {
                            title: '提示',
                            buttons: { "继续制作": false, "关闭": true },
                            submit: function ( e, v ) {
                                if (v){
                                    window.location.href="/tts/listening.vpage";
                                }else{
                                    window.location.href="/tts/listening/index.vpage";
                                }
                            },
                            useiframe:true
                        } );

                    }
            );
        },
        // 提示字数过多
        crossBorder: function ( elem/* textarea */, isBoolean ) {
            var isEnglish = elem.closest( this.paraSel).find( this.lisRoleSel ).val() !== 'p',
                val = ( elem.val() || "" ).replace( /\s+/g, ""),
                hasChinese = isEnglish && this.chineseRe.test(val), /* 英文中存在中文*/
                paragraph = elem.closest( this.paraSel),
                play = paragraph.find( this.lisParaSel ),
                textarea = paragraph.find( 'textarea' ),
                tip = paragraph.find( this.spanTipSel),
                length,
                mes;

            length = isEnglish ? 1000 : 100;
            mes = val.length > length ? this.moreContentMes: hasChinese ? this.englishHasChineseMes : "";
            if ( isBoolean ) {
                return !!mes;
            }
            if ( mes ) {
                tip.text( mes );
                play.addClass( this.disableColor );
                textarea.addClass( this.redColor );
            } else {
                tip.text( '' );
                play.removeClass( this.disableColor );
                textarea.removeClass( this.redColor );
            }
        },
        isCrossBorder: function ( elem/* textarea */ ) {
            return jQuery.trim( elem.val() ).length > ( this.isEnglish( elem ) ? 1000 : 100 );
        },
        isEnglish: function ( elem /* textarea */ ) {
            return elem.closest( this.paraSel).find( this.lisRoleSel ).val() !== 'p';
        }
    } );
    ( function () {
        var
                options = {
                    ttsView: 't:TTS',
                    // 我要制作的选择器
                    lisMakeSel: 'li.lis_make',
                    // 默认播放音选择器
                    initPlaySel: 'li.initPlay',
                    // 提示语选择器
                    tipSel: 'div.tts_tip',
                    // 超时提示选择器
                    spanTipSel: 'span.tts_span_tip',
                    // 标题选择器
                    titleSel: '#tts_title',
                    // 播放次数选择器
                    playCSel: 'input.play_count_text',
                    // 间隔时间选择器
                    splitSel: 'input.split_time_text',
                    // tts的容器
                    ttsContent: $( '#tts_content' ),
                    // 7个li的选择器
                    li7Sel: 'li.tts_url_key',
                    // 小题选择器
                    smallSel: 'div.lis_small_topic',
                    // 片段选择器
                    paraSel: 'div.lis_paragraph',
                    // 大题选择器
                    bigSel: 'div.lis_big_topic',
                    // 试听小题选择器
                    lisSmallTopicSel: 'a.listen_small_topic',
                    // 试听片段选择器
                    lisParaSel: 'input.lis_audition',
                    // 大题索引选择器
                    bigIndexSel: 'span.big_topic_index',
                    // 小题索引选择器
                    smallIndexSel: 'span.small_topic_index',
                    // step1 中“播放”按钮的选择器
                    playBtnSel: 'input.lis_step1_play',
                    // step2 中“播放”按钮的选择器
                    playBtn2Sel: 'input.lis_step2_play',
                    // 调整次数
                    changeCountSel: 'input.tts_change_count',
                    // 完成 并 生成音频
                    makeCompleteSel: '#tts_make_complete',
                    // 增加段选择器
                    addParaSel: 'a.add_paragraph',
                    // 添加小题
                    addSmallSel: 'a.add_small_topic',
                    // 添加大题
                    addGigSel: 'a.add_big_topic',
                    // 段内角色选择器
                    lisRoleSel: 'select.lis_role',
                    // 段内音量选择器
                    lisVolumeSel: 'select.lis_volume',
                    // 段内语速选择器
                    lisSpeedSel: 'select.lis_speed',
                    // 段内尾音停顿选择器
                    lisPauseSel: 'select.lis_pause',
                    // 段内内容选择器
                    paraContentSel: 'textarea.small_topic_content',
                    // jplayer
                    jplayer: $("#jquery_jplayer_1"),
                    // 不可用的灰色class
                    disableColor : 'tts_disable_color',
                    // 字数过多的class
                    redColor : 'tts_red_color'
                },
                m = new ListenModel(),
                c = new ListenController( m ),
                v = new ListenView( m, c, options );
                <#if paper??>
                    m.pageData = ${paper};
                </#if>
        v.init( options );
        //换教材
        $(".more_books_but").on('click', function(){
            $.get("/tts/changebook.vpage", function(data){
                $.prompt(data, {
                    title : '更换教材',
                    position : {width: '850'},
                    buttons: {},
                    loaded : function(){
                        BookList.init({
                            index: $('#ttsClassLevel').val() || 1  //默认年级
                        });
                    },
                    useiframe:true
                });
            });
        });

        $("body").on("booklist.click", function(event, bookId, bookName){
            $('#ttsTextbook').text(bookName);
            $('#ttsClassLevel').val(BookList.index);
            $('#ttsBookId').val(bookId);
            $.prompt.close();
        });
} () );
} );
</script>
</@temp.page>