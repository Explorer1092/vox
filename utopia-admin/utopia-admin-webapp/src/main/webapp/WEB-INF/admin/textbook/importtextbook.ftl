<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='教材详细配置' page_num=4 jqueryVersion ="1.7.2">
<style>
    .form-horizontal .control-label {
        float: left;
        width: 206px;
        padding-top: 5px;
        text-align: right;
    }
</style>
<div class="span9">
    <fieldset>
        <legend>教材导入()</legend>
    </fieldset>

    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="productName">bookId：</label>
                        <div class="controls">
                            <label for="title">
                                <textarea name="bookIds" id="bookIds"
                                        class="input" style="width: 50%"></textarea>
                            </label>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <input type="button" id="saveBtn" value="保存配置" class="btn btn-large btn-primary">
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
</div>

<script type="text/javascript">


    $(function () {

        $('#saveBtn').on('click', function () {
            var bookIds = $('#bookIds').val();
            bookIds=bookIds.replace(/\ +/g,"");//去掉空格
            bookIds=bookIds.replace(/[ ]/g,"");    //去掉空格
            bookIds=bookIds.replace(/[\r\n]/g,"");//去掉回车换行
            var postData = {
                bookIds: bookIds
            };

            //数据校验
            if (bookIds == '') {
                alert("bookIds不能为空");
                return false;
            }

            $.post('importBook.vpage', postData, function (data) {
                if (data.success) {
                    location.href = 'textbooklist.vpage?currentPage=' + 1;
                } else {
                    alert(data.info);
                }
            });
        });
    });
</script>
</@layout_default.page>