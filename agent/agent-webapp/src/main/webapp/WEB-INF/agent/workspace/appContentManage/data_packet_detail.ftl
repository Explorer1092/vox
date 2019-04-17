<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='资料包录入' page_num=1>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-info"></i>资料包录入</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <#if errorMessage??>
            <div class="alert alert-error">
                <button type="button" class="close" data-dismiss="alert">×</button>
                <strong>出错啦！ ${errorMessage!}</strong>
            </div>
        </#if>

        <div class="box-content">

            <div class="control-group">
                <label class="control-label" for="focusedInput">类型:</label>
                <select id="type-selected" name="datumType">
                    <option value="0">请选择</option>
                    <#list dataPacketType as data>
                        <option value="${data.id!0}">${data.desc!''}</option>
                    </#list>
                </select>
            </div>
            <div class="control-group">
                <label class="control-label" for="focusedInput">适用角色:(必填)</label>
                <div class="controls">
                    <#list applyRole  as data>
                        <input type="checkbox" id="d-p-r-${data.id!0}"
                               class="apply-role" value="${data.id!0}"/>${data.roleName!""}&nbsp;&nbsp;
                    </#list>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="focusedInput">标题:</label>
                <input id="contentTitle">
            </div>
            <div class="control-group">
                <label class="control-label" for="focusedInput">内容:</label>
                <script id="content_area"  type="text/plain"></script>
            </div>
            <#if requestContext.getCurrentUser().isCountryManager() || requestContext.getCurrentUser().isAdmin()>
                <div class="form-actions">
                    <button id="save_content" type="button" class="btn btn-primary">保存</button>
                    <a class="btn" href="/workspace/appupdate/data_packet_manage.vpage"> 取消 </a>
                </div>
            </#if>
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
        $.post("edit_data_packet.vpage", {id: id}, function (res) {
            if (res.success) {
                $("#type-selected").val(res.datumType);
                $("#contentTitle").val(res.contentTitle);
                var applyRole = res.applyRoles;
                for (var i = 0; i < applyRole.length; i++) {
                    var scope = $("#d-p-r-" + applyRole[i]);
                    scope.parent("span").addClass("checked");
                    scope.attr("checked", true);
                }

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
            var datumType = $("#type-selected").val();
            if (blankStringOrZero(datumType)) {
                alert("请选择类别！");
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

            var applyRole = "";
            $(".apply-role").each(function () {
                if ($("#" + this.id).attr("checked")) {
                    applyRole += this.value + ","
                }
            });
            if (blankStringOrZero(applyRole)) {
                alert("请选择适应的角色");
                return;
            }
            var data = {
                id:id,
                datumType: datumType,
                content: content,
                contentTitle: contentTitle,
                applyRole: applyRole
            };

            $.post("save_data_packet.vpage", data, function (res) {
                if (res.success) {
                    location.href = "data_packet_manage.vpage?typeId=" + datumType;
                } else {
                    alert(res.info);
                }
            });
        })
    });
</script>
</@layout_default.page>