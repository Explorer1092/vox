<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>

<div class="span9">
    <fieldset>
        <legend><span style="color: #00a0e9">轻课馆/课程推荐位管理</span></legend>
    </fieldset>

    <a class="btn btn-danger" href="https://oss-data.17zuoye.com/studytogether/test/2019/01/28/20190128120002036921.csv">模板下载</a>
    &nbsp;&nbsp;
    <button class="btn btn-success" style="position:relative">
        <input id="uploadData" type="file" accept=".csv" name="file" size="10"
               style="opacity: 0;position: absolute;left: 0;top: 0;width: 80px;"
        />导入推荐位数据
    </button>
    &nbsp;&nbsp;
    <a class="btn btn-info" id="exportDataButton">导出推荐位数据</a>
    &nbsp;&nbsp;
    <#if lastUrl??><a class="btn btn-warning" href="${lastUrl}">点此下载最近一次上传的文件</a></#if>

    <br>
    <br>
    <div class="panel panel-danger">
        <div class="panel-heading">
            <h3 class="panel-title">关于模板填写的几项说明</h3>
        </div>
        <div class="panel-body">
            <p>1）ID必须是大于零的数值型</p>
            <p>2）时间字段的格式必须是: 2019-01-14 12:00:00 或者 2019/01/14 12:00:00</p>
            <p>3）是否在适龄模块展示填写值必须为：是 或者 否</p>
            <p>4）权重值必须是：(0,100] 区间内的数值</p>
            <p>5）不同字段的间隔符号必须是英文逗号：,</p>
            <p>6）上传失败后，再次上传文件时需要先刷新一下此页面</p>
            <p>7）使用Excel编辑CSV文件时，日期默认格式是：2019/01/14 12:00，此处必须设置单元格式</p>
        </div>
    </div>

    <br>
    <br>

    <div id="error_id" class="panel panel-danger" hidden>
        <div class="panel-heading">
            <h3 class="panel-title">错误描述</h3>
        </div>
        <div class="panel-body">
            <table class="table table-hover table-striped table-bordered">
                <thead>
                <tr>
                    <th>行号</th>
                    <th>错误信息</th>
                </tr>
                </thead>
                <tbody class="tbody_tr" id="tbody_tr"></tbody>
            </table>
        </div>
    </div>

</div>
<script type="text/javascript">
    $(function () {

        //导入数据
        $("#uploadData").on('change', function () {
            var $this = $(this);
            var ext = $this.val().split('.').pop().toLowerCase();
            if ($this.val()) {
                if (ext != 'csv') {
                    alert("仅支持【csv】格式");
                    return false;
                }
                var formData = new FormData();
                formData.append('file', $this[0].files[0]);
                $.ajax({
                    url: 'batch_upload.vpage',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data.success) {
                            $("#error_id").hide();
                            alert("上传成功");
                            location.href= 'index.vpage';
                        } else {
                            alert("上传失败");
                            $("#error_id").show();
                            var content = createTable(data);
                            $("#tbody_tr").html(content);
                        }
                    }
                });
            }
        });

        //导出数据
        $("#exportDataButton").on('click', function () {
            location.href = "/opmanager/studytogether/recommend/batch_export.vpage";
        });

    });

    //创建table
    function createTable(data) {
        data = data.errors;
        var htmlContent = "";
        for (var i = 0; i < data.length; i++) {
            var row = data[i].row;
            if ("undefined" === typeof (row) || null === row) {
                row = " ";
            }
            var message = data[i].message;
            if ("undefined" === typeof (message) || null === message) {
                message = " ";
            }
            htmlContent += "<tr>";
            htmlContent += "<td>" + row + "</td>";
            htmlContent += "<td>" + message + "</td>";
            htmlContent += "</tr>";
        }
        return htmlContent;
    }

</script>
</@layout_default.page>