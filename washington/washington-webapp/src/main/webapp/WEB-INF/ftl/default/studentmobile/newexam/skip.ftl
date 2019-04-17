<#import "../layout.ftl" as temp >
<@temp.page>
<div class="wr">
    <div id="homeworkStart"></div>
</div>
<script type="text/javascript">
    document.title = '作业详情';
    $(function () {
        //点击开始作业
        $('#homeworkStart').on('click', function () {
            var homework = {
                homework_id: '${newExamId!''}',
                hw_card_source: 'h5',//跳h5还是native
                hw_card_variety: 'newexam',//调用的go api
                is_makeup: false,
                page_viewable: false
            };
            if (window.external && ('doHomework' in window.external)) {
                window.external.doHomework(JSON.stringify(homework));
            }
        });
        $('#homeworkStart').trigger('click');
    });
</script>
</@temp.page>