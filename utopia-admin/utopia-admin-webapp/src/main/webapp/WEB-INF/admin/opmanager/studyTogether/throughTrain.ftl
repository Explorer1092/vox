<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="直通车配置管理" page_num=9 jqueryVersion ="1.7.2">
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        直通车配置管理
    </legend>
    <div class="row-fluid">

        <div class="span12">
            <a href="https://oss-data.17zuoye.com/studytogether/test/2018/10/23/20181023162047798337.csv">点击此处
                下载配置模板</a>
            <div class="well">
                <li>在这里上传 csv 格式配置文件（!一定要是 csv 格式的！）</li>
                <button class="btn btn-info" style="position:relative" id="uploadFile">
                    <input class="fileUpBtn" type="file"
                           accept="text/csv" name="file"
                           size="10"
                           style="opacity: 0;position: absolute;left: 0;top: 0;width: 80px;"
                    />上传文件
                </button>

                <li>配置将在上传后最多5分钟后生效！</li>
            </div>
           <#if lastUrl?? >
            <a href="${lastUrl}">点此下载最近一次上传的（最新上传的可能还未生效）的配置文件</a>
           </#if>
            <li>---------我是分割线---------</li>
        </div>

        <div class="span10">
            <div class="well">
                以下是已生效的配置
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>来源课程 id</th>
                        <th>直通车地址</th>
                        <th>列表课程 id</th>
                    </tr>
                    </thead>
                    <tbody>
                            <#if content?? && content?size gt 0>
                                <#list content as  pageInfo>
                                <tr>
                                    <td>${pageInfo.lessonId!''}</td>
                                    <td>
                                        /view/mobile/parent/17direct_train/index.vpage?from_lesson_id=${pageInfo.lessonId!''}&rel=
                                    </td>
                                    <td>${pageInfo.lessonIdList!''}</td>
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
</div>

<script type="text/javascript">
    $(".fileUpBtn").on("change", function () {

        var $this = $(this);
        var ext = $this.val().split('.').pop().toLowerCase();
        if ($this.val() != '') {
            if ($.inArray(ext, ['csv']) == -1) {
                alert("仅支持以下格式【'csv'】");
                return false;
            }

            var formData = new FormData();
            formData.append('file', $this[0].files[0]);
            $.ajax({
                url: 'upload.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (data) {
                    if (data.success) {
                        alert("上传成功！最多5分钟后生效！请耐心等待！");
                        location.reload(true);
                    } else {
                        alert(data.info);
                    }
                }
            });
        }
    });
</script>
</@layout_default.page>

