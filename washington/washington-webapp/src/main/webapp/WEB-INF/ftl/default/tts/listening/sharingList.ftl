<#if shareList?? && shareList.content?has_content>
<table>
    <thead>
    <tr>
        <th style="width: 260px;">标题</th>
        <th style="width: 80px;">所属年级</th>
        <th style="width: 270px;">所属教材</th>
        <th style="width: 68px;">作者</th>
        <th style="width: 70px;">播放时长</th>
        <th>操作</th>
    </tr>
    </thead>
    <tbody>
        <#list shareList.content as data>
        <tr <#if data_index % 2==1> class="odd" </#if>>
            <td>${data.title!}</td>
            <td>${data.fetchClassLevelString()!""}</td>
            <td>${data.bookName!""}</td>
            <td><#if data.authorName?has_content>${(data.authorName)!?substring(0, 1)}</#if>老师</td>
            <td>${data.fetchDurationString()!}</td>
            <td>
                <a href="/tts_view.vpage?id=${data.id!}" target="_blank"
                   onclick="javascript:$17.tongji('TTS_在线查看次数', '');">查看</a>
                <#if (data.format)?? && data.format != 1>
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
<div class="message_page_list" id="sharingPage">
</div>
<#else>
<div class="text_center text_big text_gray_6" style="padding:20px;text-align: center;">没有找到听力材料
</div>
</#if>
<script>
    $(function () {
        $('.tts-book').val(${bookId!0});
        $('.tts-classLevel').val(${classLevel!0});
        $("#sharingPage").page({
            total: ${shareList.totalPages!},
            current: ${shareList.number!}+1,
            showTotalPage: false,
            jumpCallBack: function (index) {
                $('#sharingList').html('<div style="padding: 50px 0; text-align: center;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>')
                        .show().load("/tts/sharingList.vpage?pageNum=" + index + "&classLevel=" + $(".tts-classLevel").val() + "&bookId=" + $(".tts-book").val());
            }
        });
    });
</script>