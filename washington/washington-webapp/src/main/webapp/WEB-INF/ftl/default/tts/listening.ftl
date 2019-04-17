<#import "module.ftl" as temp />
<@temp.page level="听力材料">
<!-- 制作听力 -->
<div class="tts-title-box">
    <div class="inner">
        <div class="font">
            <p>听力材料</p>
        </div>
        <div class="btn tts-fl-right">
            <a style="padding: 12px 0;" href="/tts/flash.vpage" class="tts-btn tts-create w-circular-5 w-border-blue w-btn-mini">制作听力材料</a>
        </div>
        <div class="tts-clear"></div>
    </div>
</div>
<div class="tts-contain">
    <div class="inner">
        <div class="title">
            我的听力材料
            <div class="slide tts-fl-right">
                <span>标题</span>
                <input type="text" value="" name="search-title" class="search-title w-int"/>
                <a id="tts-filter" style="width: 64px; height: 25px; padding: 0; line-height: 25px; color: #fff;" class="tts-btn w-circular-5 w-border-blue" href="javascript:void (0)">搜索</a>
            </div>
        </div>
        <div class="tts-table" id="listeningList">
        </div>
    </div>
</div>

<script type="text/html" id="t:teacherTtSGuide">
    <div class="tts-popup"></div>
</script>
<script type="text/javascript">
    var PaperList = null;
    var sharePaperList = null;
    var showDown = ${showDown!};
    // 禁掉老师权限检查
    showDown = 1;
    $(function () {

        $17.tongji('TTS页面访问');

        if (!$17.getCookieWithDefault("tts_first")) {
            $17.setCookieOneDay("tts_first", "1", 60);
            $.prompt(template("t:teacherTtSGuide", {}), {
                prefix: "null-popup",
                title: '系统提示',
                buttons: {},
                classes: {
                    fade: 'jqifade',
                    close: 'w-hide',
                    title: 'w-hide'
                }
            });
        }

        $('.tts-popup').live('click', function () {
            $.prompt.close();
        });


        <#if currentUser?has_content && (!currentUser.isResearchStaff()) && (currentUser.isEnglishTeacher() && ((currentUser.regionCode?has_content && currentUser.regionCode == 110112) || (currentUser.cityCode?has_content && currentUser.cityCode == 370700)))>
            $('#offlineList').html('<div style="padding: 50px 0; text-align: center;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>')
                    .show().load('/tts/offlineList.vpage?pageNum=1');
        </#if>
        $('#listeningList').html('<div style="padding: 50px 0; text-align: center;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>')
                .show().load('/tts/listeningList.vpage?pageNum=1');
        $("a.delete_listening").die().live("click", function () {
            var $self = $(this);
            $.prompt("是否确认要删除？", {
                title: "系统提示",
                focus: 1,
                buttons: { "取消": false, "确定": true },
                submit: function (e, v) {
                    if (v) {
                        $.post("/tts/listening/delete.vpage", {
                            // jQuery bug, fixed in 1.8
                            // #7579: jQuery.data() truncates numbers taken from data-xxx attributes
                            // 用attr代替data
                            id: $self.attr("data-id")
                        }, function (data) {
                            if (data.success) {
                                setTimeout(function () {
                                    location.reload();
                                }, 500);
                            }
                        });

                    }
                }
            });
            return false;
        });

        $(".v-down-TTS").live("click", function(){
            if ($(this).closest('#sharingList').size() > 0)
                $17.tongji('TTS_共享材料下载次数', '');
            else
                $17.tongji('TTS_我的材料下载次数', '');
            if(showDown){
                // 用attr代替data
                location.href = "/tts_download.vpage?paperId=" + $(this).attr("data-id") + "&_rand="+Math.random();
            }else{
                $17.alert("教师等级达到1级后，才可以将听力材料下载使用。<a href='${(ProductConfig.getUcenterUrl())!}/teacher/center/mylevel.vpage' class='w-blue'>查看教师等级</a> ");
                return;
            }
        });

        var downloading = false;
        $('a.v-down-MP3').live("click", function(){
            if ($(this).closest('#sharingList').size() > 0)
                $17.tongji('TTS_共享MP3下载次数', '');
            else
                $17.tongji('TTS_我的MP3下载次数', '');
            if (!showDown){
                $17.alert("教师等级达到1级后，才可以将听力材料下载使用。<a href='${(ProductConfig.getUcenterUrl())!}/teacher/center/index.vpage#/teacher/center/mylevel.vpage' class='w-blue'>查看教师等级</a> ");
                return;
            }
            var target = $(this);
            if(downloading){
                return;
            }
            downloading = true;
            target.text('生成中...');
            $.post("/tts/listening/getCompleteVoice.vpage", {
                // 用attr代替data
                paperId:  $(this).attr('data-id')
            }, function (data) {
                downloading=false;
                target.text('下载MP3');
                if (data.success) {
                    if (data && data.value)
                        window.location=data.value+"&_rand="+Math.random();
                }
            });
        });

        <#--#15737 - TTS - 广州越秀区  未认证提示去认证-->
        $(document).on("click", ".v-down-info", function(){
            $17.alert("<p style='font-size: 14px;'>只有认证教师才可下载MP3， <a href='${(ProductConfig.getUcenterUrl())!}/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage?ref=tts' style='color: #39f; text-decoration: underline;'>点击了解认证条件</a>。</p>");
        });



        $('#tts-filter').on('click', function(){
            $('#listeningList').html('<div style="padding: 50px 0; text-align: center;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>')
                    .show().load('/tts/listeningList.vpage?pageNum=1&title='+encodeURIComponent($('.search-title').val()));
        });
    });
</script>
</@temp.page>