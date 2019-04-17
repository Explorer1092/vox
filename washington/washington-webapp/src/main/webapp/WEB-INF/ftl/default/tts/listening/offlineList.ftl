<#if offlineList?? && offlineList.content?has_content>
<table>
    <tbody>
        <#list offlineList.content as data>
        <tr <#if data_index % 2==1> class="odd" </#if>>
            <td>${data.name!}</td>
            <td class="tts-fl-right">
                <a href="javascript:void(0);" class="v-preview" data-id="${data.id!}">预览</a>
                <a href="javascript:void(0);" class="v-down-paper" data-id="${data.id!}">下载试卷</a>
                <a id="v-down-mp3" href="javascript:void(0);" data-id="${data.id!}">下载听力MP3</a>
                <a href="javascript:void(0);" class="v-down-qr" data-id="${data.id!}">下载听力二维码</a>
            </td>
        </tr>
        </#list>
    </tbody>
</table>
<div class="message_page_list" id="offlinePage" >
</div>
<#else>
<div class="text_center text_big text_gray_6" style="padding:20px;text-align: center;">没有找到听力材料
</div>
</#if>
<script>
    $(function () {
        <#if offlineList?? && offlineList.content?has_content>
            $("#offlinePage").page({
                total: ${offlineList.totalPages!},
                current: ${offlineList.number!}+1,
                jumpCallBack: function(index){
                    $('#offlineList').html('<div style="padding: 50px 0; text-align: center;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>')
                            .show().load("/tts/offlineList.vpage?pageNum="+index);
                }
            });
        </#if>
        $(".v-down-paper").live("click", function(){
            $17.tongji("O2O-TTS-Paper-" + $(this).data("id"), "下载试卷", "${currentUser.id}");
            if(showDown){
                location.href = "/tts_downloadO2OPaper.vpage?paperId=" + $(this).data("id") + "&_rand="+Math.random();
            }else{
                $17.alert("教师等级达到1级后，才可以将听力材料下载使用。<a href='${(ProductConfig.getUcenterUrl())!}/teacher/center/index.vpage#/teacher/center/mylevel.vpage' class='w-blue'>查看教师等级</a> ");
                return;
            }
        });
        $(".v-down-qr").live("click", function(){
            $17.tongji("O2O-TTS-Paper-" + $(this).data("id"), "下载二维码", "${currentUser.id}");
            if(showDown){
                location.href = "/tts_downloadQR.vpage?paperId=" + $(this).data("id") + "&_rand="+Math.random();
            }else{
                $17.alert("教师等级达到1级后，才可以将听力材料下载使用。<a href='${(ProductConfig.getUcenterUrl())!}/teacher/center/index.vpage#/teacher/center/mylevel.vpage' class='w-blue'>查看教师等级</a> ");
                return;
            }
        });
        $('#v-down-mp3').live("click", function(){
            $17.tongji("O2O-TTS-Paper-" + $(this).data("id"), "下载MP3", "${currentUser.id}");
            if (!parent.showDown){
                $17.alert("教师等级达到1级后，才可以将听力材料下载使用。<a href='${(ProductConfig.getUcenterUrl())!}/teacher/center/index.vpage#/teacher/center/mylevel.vpage' class='w-blue'>查看教师等级</a> ");
                return;
            }
            var target = $(this);
            if(parent.downloading){
                return;
            }
            downloading = true;
            target.text('生成中...');
            $.post("/tts/listening/getCompleteVoice.vpage", {
                paperId:  $(this).data('id')
            }, function (data) {
                downloading=false;
                target.text('下载听力MP3');
                if (data.success) {
                    if (data && data.value)
                        window.location=data.value+"&_rand="+Math.random();
                }
            });
        });
        $(".v-preview").live("click", function(){
            $17.tongji("O2O-TTS-Paper-" + $(this).data("id"), "预览", "${currentUser.id}");
            var url = "/tts/offlinePreview.vpage?id=" + $(this).data("id");
            var data = '<iframe class="vox17zuoyeIframe" id="v-frame" src="' + url + '" width="750" marginwidth="0" height="450" marginheight="0" scrolling="no" frameborder="0"></iframe>';

            $.prompt(data, {
                position: { width: 800, height: 500 },
                buttons : {},
                close   : function(){
//                    window.location.reload();
                    $("#v-frame").remove();
                }
            });
        });
    });
    function closePrompt(){
        $.prompt.close();
    }
</script>