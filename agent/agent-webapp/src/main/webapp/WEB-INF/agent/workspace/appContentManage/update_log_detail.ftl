<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='平台更新日志' page_num=1>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-info"></i>日志录入</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <div class="control-group">
                <label class="control-label" for="focusedInput">涉及产品:</label>
                <#list referProduct as data>
                    <input id="p-t-${data.id!0}" type="checkbox" value="${data.id!0}"
                           class="product-type">${data.entranceName!""}&nbsp;&nbsp;
                </#list>
            </div>
            <div class="control-group">
                <label class="control-label" for="focusedInput">标题:</label>
                <input id="contentTitle">
            </div>
            <div class="control-group">
                <label class="control-label" for="focusedInput">内容:</label>
                <script id="content_area" type="text/plain"></script>
            </div>
            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                <div class="form-actions">
                    <button id="save_content" type="button" class="btn btn-primary">保存</button>
                    <a class="btn" href="/workspace/appupdate/update_log_manage.vpage"> 取消 </a>
                </div>
            </#if>
            <input type="hidden" id="iReferProduct" name="referProduct">
        </div>
    </div>
</div>

<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.config.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.all.js"></script>

<script type="text/javascript">

    $(function () {
        var options = {
            filterMode: true, //true时根据 htmlTags 过滤HTML代码，false时允许输入任何代码。
            items: [
                'source', '|', 'undo', 'redo', '|', 'preview', 'template', 'cut', 'copy', 'paste',
                'plainpaste', 'wordpaste', '|', 'justifyleft', 'justifycenter', 'justifyright',
                'justifyfull', 'insertorderedlist', 'insertunorderedlist', 'indent', 'outdent', 'subscript',
                'superscript', 'clearhtml', 'quickformat', 'selectall', '|', 'fullscreen', '/',
                'formatblock', 'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold',
                'italic', 'underline', 'strikethrough', 'lineheight', 'removeformat', '|', 'image',
                'table', 'hr', 'emoticons', 'pagebreak',
                'anchor', 'link', 'unlink', '|'
            ],
            uploadJson: 'edituploadimage.vpage',
            fileManagerJson: 'edituploadimage.vpage',
            allowFileManager: true
        };

        var ue = UE.getEditor('content_area', {
            serverUrl: "ueditorcontroller.vpage",
            zIndex: 999,
            fontsize: [8, 9, 10, 13, 16, 18, 20, 22, 24, 26],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload', 'pagebreak', 'template', 'background', '|',
                'horizontal', 'date', 'time', 'spechars', 'snapscreen', '|', 'preview', 'searchreplace'
            ]]
        });

        var id = "${id!''}";
        $.post("edit_update_log.vpage", {id: id}, function (res) {
            if (res.success) {
                var referProduct = res.referProduct;
                for (var i = 0; i < referProduct.length; i++) {
                    // 这里选不上~~~~~
                    $("#p-t-" + referProduct[i]).parent("span").addClass("checked");
                    $("#p-t-" + referProduct[i]).attr("checked", true);
                }
                $("#contentTitle").val(res.contentTitle);
                ue.ready(function () {
                    ue.setContent(res.content.replace(/\n/g, '<br />'));
                });
                if (res.disabled) {
                    $(".form-actions").hide();
                }
            } else {
                alert(res.info);
            }
        });


        $('#save_content').on('click', function () {
            var referProduct = "";
            $(".product-type").each(function () {
                if ($("#" + this.id).attr("checked")) {
                    referProduct += this.value + ","
                }
            });
            if (blankStringOrZero(referProduct)) {
                alert("请选择涉及得产品！");
                return;
            }
            var contentTitle = $("#contentTitle").val();
            if (blankString(contentTitle)) {
                alert("请填写标题");
                return;
            }
            var content = ue.getContent();
            if (blankString(content)) {
                alert("内容不能为空");
                return false;
            }
            var data = {
                id: id,
                referProduct: referProduct,
                content: content,
                contentTitle: contentTitle
            };
            $.post("save_update_log.vpage", data, function (res) {
                if (res.success) {
                    location.href = "update_log_manage.vpage";
                } else {
                    alert(res.info);
                }
            });

        });
    });

</script>
</@layout_default.page>