<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<div class="span9">
    <fieldset>
        <legend>push文案编辑</legend>
    </fieldset>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>课程 id</th>
                        <th>push文案</th>
                    </tr>
                    </thead>
                    <tbody id="table">
                        <#if lessonIds?? && lessonIds?size gt 0>
                            <#list lessonIds as  lessonId>
                            <tr>
                                <td>${lessonId!''}</td>
                                <td style="display: none;"><#if content??&&content?size gt 0&&content["${lessonId!''}"]??>${content["${lessonId!''}"].defaultText!}<#else>一起学古诗训练营：每天10分钟，掌握一首诗。一定要陪孩子坚持学习哦！</#if></td>
                                <td><textarea><#if content??&&content?size gt 0&&content["${lessonId!''}"]??>${content["${lessonId!''}"].text!}<#else></#if></textarea></td>
                            </tr>
                            </#list>
                        <#else>
                        <tr>
                            <td colspan="7" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <button class="btn btn-primary" type="button" id="save_record">保存</button>
</div>

<script type="text/javascript">


    $(function () {

        //保存
        $("#save_record").on('click', function () {
            var pushTextArray = [];
            var isFlag = true;
            $("#table").find("tr").each(function (i) {
                var pushTextObject = {};
                $(this).children("td").each(function (j) {
                    if (j == 0) {
                        pushTextObject.lessonId = $(this).text().trim();
                    }
                    if (j == 1) {
                        pushTextObject.defaultText = $(this).text().trim();
                    }
                    if (j == 2) {
                        var text = $(this).children('textarea').val().trim();
                        if (!text) {
                            alert("文案不能为空");
                            isFlag = false;
                            return false;
                        }
                        pushTextObject.text = text;
                    }
                });
                if (!isFlag) {
                    return false;
                }
                console.log(pushTextObject);
                pushTextArray.push(pushTextObject);
                console.log(pushTextArray);
            });
            if (!isFlag) {
                return;
            }
            $.ajax({
                url: 'savePushText.vpage',
                type: 'POST',
                async: false,
                data: {"jobTextList": JSON.stringify(pushTextArray)},
                success: function (data) {
                    if (data.success) {
                        alert("保存成功");
                    } else {
                        console.log("data error");
                    }
                }
            });

        });
    });
</script>
</@layout_default.page>