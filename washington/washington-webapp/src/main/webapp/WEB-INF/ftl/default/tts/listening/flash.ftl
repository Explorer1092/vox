<#import "../module.ftl" as temp />
<@temp.page level="听力材料">
<div class="tts-title-box">
    <div class="inner" style="900px">
        <div class="font">
            <p>
                <span class="tts-icon tts-pen"></span>
                <input class="tts-input" style="border: 1px #ccc solid" placeholder="请输入听力材料名称" type="text"
                       class="title">
            </p>

            <p class="info" id="ttsTextbook"></p>
        </div>
        <div class="btn tts-fl-right">
            <p>
                <a class="link" href="/tts/listening.vpage">返回</a>
            </p>

            <p>
                <input type="hidden" id="ttsClassLevel"/>
                <input type="hidden" id="ttsBookId"/>
                <a class="tts-btn tts-change-book" href="javascript:void (0)"><i
                        class="tts-icon tts-refresh"></i>换教材</a>
            </p>
        </div>
        <div class="tts-clear"></div>
    </div>
</div>
<div class="tts-contain">

    <div id="movie" style="margin: 0 auto;width: 900px; border: 1px solid #000;"></div>
    <div style="width: 100%;margin: 30px auto 20px;text-align: center;background-color: #ffffff;padding: 20px 0;">
        <a class="tts-btn tts-btn-big tts-save" href="javascript:void (0)">保存</a>
        <input type="hidden" value="1" id="tts_share"/>
    </div>
</div>
<script id="t:jiathis" type="text/html">
    <div style="font-size: x-large;text-align: center;width: 450px;margin-top: 20px;">恭喜！保存成功</div><br/>
</script>
<div id="help-div">
    <div class="help-content" style="display: none;">
    </div>
</div>

<script>
    var listeningUrl = '${listeningUrl!}';
    var paperId = '${id!}';
    $(function(){

        $('#ttsTextbook').html('<span style="color:#ff0000">请选择教材</span>');
        <#if paper??>
            $('.tts-title-box .tts-input').val('${paper.title!}');
            $('#ttsTextbook').text('${paper.bookName!}');
            $('#ttsClassLevel').val(${paper.classLevel!0});
            $('#ttsBookId').val(${paper.bookId!0});
        </#if>
        $('.m-footer').hide();
        var p = {};
        p.domain = '${requestContext.webAppBaseUrl}/';
        p.isEdit = true;
        p.first = true;

        $("#movie").getFlash({
            id       : "TTSEditor",
            width    : 900,
            height   : 600,
            movie    : '<@flash.plugin name="TTSEditor"/>',
            flashvars: p,
            wmode    : 'transparent'
        });

        $(".help-content").getFlash({
            id       : "help-movie",
            width    : 960,
            height   : 500,
            movie    : '//cdn.17zuoye.com/static/project/tts/tts0907.swf',
            flashvars: p,
            wmode    : 'transparent'
        });


        //换教材
        $(".tts-change-book").on('click', function(){
            $.get("/tts/changebook.vpage", function(data){
                $.prompt(data, {
                    title    : '更换教材',
                    position : { width: '850' },
                    buttons  : {},
                    loaded   : function(){
                        BookList.init({
                            index: $('#ttsClassLevel').val() || ${defaultGrade!1}  //默认年级
                        });
                    },
                    useiframe: true
                });
            });
        });

        $("body").on("booklist.click", function(event, bookId, bookName){
            $('#ttsTextbook').text(bookName);
            $('#ttsClassLevel').val(BookList.index);
            $('#ttsBookId').val(bookId);
            $.prompt.close();
        });

        $('.tts-save').on("click", function(){
            var title = $('.tts-title-box .tts-input').val();
            if(title == ''){
                $17.alert("材料名字不能为空");
                return;
            }
            var bookId = $('#ttsBookId').val();
            if(bookId == 0){
                $('#ttsTextbook').html('<span style="color:#ff0000">请选择教材</span>');
                $17.alert("请选择教材");
                return;
            }
            $('#TTSEditor')[0].saveResult();
        });
    });

    //保存听力材料
    function save(content){
        var title = $('.tts-title-box .tts-input').val();
        var classLevel = $('#ttsClassLevel').val();
        var bookId = $('#ttsBookId').val();
        var bookName = $('#ttsTextbook').text();
        var share = $('#tts_share').val();
        //检查content不能为空
        var text = content.replace(/<\/?TextFlow[^>]*>/g, '').replace(/<\/?p>/g, '').replace(/<\/?span>/g, '').replace(/(^\s*)/g, "");
        if(!text || text.length <= 0){
            $17.alert("听力材料内容不能为空");
            return;
        }

        var data = { "title": title, "bookName": bookName, "bookId": bookId, "classLevel": classLevel, "richText": content, "share": share };
        if(paperId != "")
            data.id = paperId;

        $.prompt("<div class='w-ag-center'><p>保存中，请等待...</p>" + '<img src="<@app.link href="public/skin/teacherv3/images/ttsloading.gif"/>"/>' + "</div>", {
            title: "系统提示",
            position: {width: 500},
            buttons: {},
            classes: {
                fade: 'jqifade',
                close: 'w-hide'
            }
        });

        $.ajax({
            type       : 'post',
            url        : '/tts/listening/generatePaperFromFlash.vpage',
            data       : $.toJSON(data),
            success    : function(res){
                if(res && res.success && res.value){
                    $17.tongji('TTS_flash工具完成次数', '');
                    paperId = res.value;
                    // 保存时触发MP3生成操作，不需要等待返回结果
                    $.ajax({
                        type   : 'POST',
                        url    : "/tts/listening/getCompleteVoice.vpage",
                        data   : {
                            paperId: paperId
                        },
                        timeout: 200
                    });
                    var html = template("t:jiathis", {});
                    html += '<script> var jiathis_config={'
                            + 'url: "http://www.17zuoye.com/tts_view.vpage?id=' + paperId + '",'
                            + 'data_track_clickback:true,'
                            + 'title: "#一起作业网免费制作听力材料#",'
                            + 'summary:"我在@一起作业网 免费制作了听力材料，录入文字系统生成声音，发音标准流畅。大家快来听一下吧。",'
                            + 'shortUrl:false,'
                            + 'hideMore:false'
                            + '};</' + 'script>';

                    html += '<script type="text/javascript" src="http://v3.jiathis.com/code/jia.js?uid=1613716" charset="utf-8" />';

                    setTimeout(function() {
                        $.prompt(html, {
                            title: '提示',
                            buttons: {"继续制作": false, "关闭": true},
                            submit: function (e, v) {
                                if (v) {
                                    window.location.href = "/tts/listening.vpage";
                                } else {
                                    window.location.href = "/tts/flash.vpage";
                                }
                            },
                            useiframe: true
                        });
                    },1000);
                }else{
                    setTimeout(function(){
                        $17.alert(res.info || "生成音频失败，请稍候再试。");
                        return { "success": false };
                    },1000);
                }
            },
            error      : function(){
                setTimeout(function(){
                    $17.alert("生成音频失败，请稍候再试。");
                    return { "success": false };
                },1000);
            },
            timeout    : 30000,
            dataType   : 'json',
            contentType: 'application/json;charset=UTF-8'
        });
    }

    function generateVoice(content, tryListen){
        $.ajax({
            type       : 'post',
            url        : '/tts/listening/generateVoiceFromFlash.vpage',
            data       : content,
            success    : function(res){
                if(res && res.success && res.value){
                    for(var i = 0; i < res.value.length; i++){
                        if(res.value[i].voice)
                            res.value[i].voice = listeningUrl + res.value[i].voice;
                    }
                    if(tryListen)
                        $('#TTSEditor')[0].tryListen(res.value);
                }else{
                    $17.alert(res.info || "生成音频失败，请稍候再试。");
                    if(tryListen)
                        $('#TTSEditor')[0].tryListen([]);
                }

            },
            error      : function(){
                $17.alert("生成音频失败，请稍候再试。");
                if(tryListen)
                    $('#TTSEditor')[0].tryListen([]);
            },
            timeout    : 20000,
            dataType   : 'json',
            contentType: 'application/json;charset=UTF-8'
        });
    }

    //生成语音，播放
    function play(content){
        generateVoice(content, 1);
    }

    //生成语音，不播放
    function generate(content){
        generateVoice(content, 0);
    }

    function getContent(){
        <#if paper??>
            return '${paper.richText!}';
        <#else>
            <#include "./flashContent.ftl" />
        </#if>

    }

    //显示帮助页面
    function showHelpPage(){
        $.prompt($("#help-div").html(), {
            title   : "帮助",
            position: { width: 1000 },
            buttons : {}
        });
        $(".jqibox .help-content").show();
    }
</script>
</@temp.page>