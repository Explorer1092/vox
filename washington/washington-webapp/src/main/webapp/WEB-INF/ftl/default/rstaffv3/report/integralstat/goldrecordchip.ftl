<#if pagination?? && pagination.getNumberOfElements() gt 0>
<table class="table_vox table_vox_bordered">
    <thead>
    <tr>
        <th>获取时间</th>
        <th>园丁豆</th>
        <th> 来 源</th>
    </tr>
    </thead>
    <tbody>
        <#list pagination.getContent() as i>
        <tr>
            <td>${i.getDateYmdString()!''}</td>
            <td>${i.integral!''}</td>
            <td>${i.comment!''}</td>
        </tr>
        </#list>
    </tbody>
</table>
<div class="common_pagination message_page_list" style="float: right;"></div>
<script>
    $(function () {
        $(".message_page_list").page({
            total: ${pagination.getTotalPages()!''},
            current: ${pagination.getNumber() + 1!''},
            jumpCallBack: createGoldList
        });
    });
</script>
<#else>
    <#if currentUser.isResearchStaffForCounty()>
    <div class="testpaperBox text_gray_6">
        <h6><a href="/rstaff/testpaper/index.vpage">创建教研员试卷</a>，本学区老师布置并检查后，您就可以根据学生完成数量获得相应的园丁豆了。</h6>
    </div>
    <#else>
    <div class="testpaperBox text_gray_6">
        <h6>暂无数据</h6>
    </div>
    </#if>
</#if>
