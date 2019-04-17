<#import "../layout.ftl" as temp >
<@temp.page>

<div class="wr">
    <#if (supportType?has_content && supportType == 'NEW_VERSION_SUPPORTED')>
        <div class="no-record" style="line-height: 50px;padding: 237px 0 0; font-size: 36px;">
            <div>你目前的版本过低，请下载新版本！</div>
        </div>
        <div class="info-submit" style="position: static;">
            <a href="http://wx.17zuoye.com/download/17studentapp?cid=102018">
                <div class="submitInner">
                    <div class="submitBox">点击下载新版</div>
                </div>
            </a>
        </div>
    <#elseif (supportType?has_content && supportType == 'NOT_SUPPORTED')>
        <div class="no-record" style="line-height: 50px;padding: 237px 0 0; font-size: 36px;">
            <div>作业暂不支持</div>
            <div>请用电脑访问www.17zuoye.com完成你的作业</div>
        </div>
    </#if>
    <div id="homeworkStart"></div>
</div>
<script type="text/javascript">
    document.title = '作业详情';
    $(function () {
        //点击开始作业
        $('#homeworkStart').on('click', function () {
            var homework = {
                homework_id: '${packageId!''}',
                homework_type:'${homeworkType!''}',
                hw_card_source: 'h5',//跳h5还是native
                hw_card_variety: 'vh_homework',//调用的go api
                is_makeup: false,
                page_viewable: false
            };
            if (window.external && ('doHomework' in window.external)) {
                window.external.doHomework(JSON.stringify(homework));
            }
        });
        <#if supportType?has_content && supportType == 'SUPPORTED'>
            $('#homeworkStart').trigger('click');
        </#if>
    });
</script>
</@temp.page>