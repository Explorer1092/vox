<#if dataList?? && dataList.content?has_content>
<table>
    <thead>
    <tr>
        <th style="width: 320px;">标题</th>
        <th style="width: 290px;">时长 (估计值）</th>
        <th>操作</th>
    </tr>
    </thead>
    <tbody>
        <#list dataList.content as data>
        <tr <#if data_index % 2==1> class="odd" </#if>>
            <td>${data.title!}</td>
            <td>${data.fetchDurationString()!}</td>
            <td>
                <a href="/tts_view.vpage?id=${data.id!}" target="_blank"
                       onclick="javascript:$17.tongji('TTS_在线查看次数', '');">查看</a>
                <a href="/tts/listening/edit.vpage?id=${data.id!}">编辑</a>
                <a data-id="${data.id!}" class="delete_listening" href="javascript:void(0);">删除</a>
                <#if (data.format != 1)!false>
                    <a href="javascript:void(0);" class="v-down-TTS" data-id="${data.id!}">下载材料</a>
                </#if>
                <#if (currentUser.userType == 1)!false>
                    <#--#15737 - TTS - #16018限制广州市 未认证提示去认证-->
                    <#if ([440100]?seq_contains(currentTeacherDetail.cityCode) && currentUser.fetchCertificationState() != "SUCCESS")!false>
                        <a href="javascript:void(0);" class="v-down-info" data-id="${data.id!}">下载MP3</a>
                    <#else>
                        <a href="javascript:void(0);" class="v-down-MP3" data-id="${data.id!}">下载MP3</a>
                    </#if>
                <#else>
                    <a href="javascript:void(0);" class="v-down-MP3" data-id="${data.id!}">下载MP3</a>
                </#if>
            </td>
        </tr>
        </#list>
    </tbody>
</table>
<div class="message_page_list" id="listeningPage">
</div>
<#else>
<div class="text_center text_big text_gray_6" style="padding:20px;text-align: center;">您还没有制作过听力，快去制作吧！
</div>
</#if>
<script>
    $(function () {
        $("#listeningPage").page({
            total: ${dataList.totalPages!},
            current: ${dataList.number!}+1,
            jumpCallBack: function(index){
                $('#listeningList').html('<div style="padding: 50px 0; text-align: center;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>')
                        .show().load('/tts/listeningList.vpage?pageNum='+index+"&title="+encodeURIComponent($('.search-title').val()));
            }
        });
    });
</script>