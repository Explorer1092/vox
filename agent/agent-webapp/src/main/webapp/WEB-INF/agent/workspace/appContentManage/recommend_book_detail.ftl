<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='推荐书籍' page_num=1>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-info"></i>添加推荐书籍</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>

            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                <div class="pull-right">
                    <a id="add_content" class="btn btn-success" href="javascript:submitRecommendBook()">
                        <i class="icon-plus icon-white"></i>
                        添加
                    </a>
                </div>
            </#if>
        </div>
        <#if errorMessage??>
            <div class="alert alert-error">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>出错啦！ ${errorMessage!}</strong>
            </div>
        </#if>
        <div class="box-content">
            <form id="add-r-b" method="post" class="form-horizontal" action="save_recommend_book.vpage"
                  enctype="multipart/form-data">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">推荐对象:</label>
                    <select id="role-selected" name="role">
                        <option value="0">请选择</option>
                        <#list roleList as data>
                            <option value="${data.id!0}">${data.roleName!''}</option>
                        </#list>
                    </select>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">推荐书籍:</label>
                    <input type="text" id="iBookName" name="bookName">
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">书籍封面:</label>
                    <div class="controls">
                        <input id="sourceFile" type="file" name="bookCover">
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>
<script type="text/javascript">
    function submitRecommendBook() {
        if (blankStringOrZero($("#role-selected").val())) {
            alert("请选择推荐对象！");
            return;
        }

        if (blankString($("#iBookName").val())) {
            alert("请选填写推荐书籍！");
            return;
        }
        var sourceFile = $("#sourceFile").val();
        if (blankString(sourceFile)) {
            alert("请上传书籍封面！");
            return;
        }
        var fileParts = sourceFile.split(".");
        var fileExt = fileParts.length < 2 ? null : fileParts[fileParts.length - 1].toLowerCase();
        if (fileExt != "jpg" && fileExt != "jpeg" && fileExt != "png") {
            alert("请上传正确格式的文件！(jpg,jpeg格式)");
            return;
        }
        $("#add-r-b").submit();
    }
</script>
</@layout_default.page>