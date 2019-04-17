<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-抓取过滤配置' page_num=13>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/moment.js"></script>
<span class="span9">
    敏感词：<input class="input-large" data-bind="value:filter_word">
    类别：<select data-bind="options:categorys,optionsText:'name',optionsValue:'value',optionsCaption:'不限',value:filter_category"></select>
    <button class="btn btn-info" data-bind="click:reload">查询</button>
    <button class="btn btn-success" data-bind="click:newBadword">新建</button>
    <table class="table table-hover table-striped table-bordered table-large">
        <thead>
        <tr>
            <#--<th>id</th>-->
                <th>名称</th>
            <th>类别</th>
            <th>创建时间</th>
            <th>创建人</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody data-bind="foreach:badwords">
        <tr>
            <#--<td data-bind="text:$data.id"></td>-->
                <td><input class="input-small" data-bind="value:$data.word"></td>
            <td><select data-bind="options:$parent.categorys,optionsText:'name',optionsValue:'value',optionsCaption:'不限',value:$data.category"></td>
            <td data-bind="text:$parent.moment_format_timestamp($data.create_datetime)"></td>
            <td data-bind="text:$data.editor"></td>
            <td>
                <button class="btn btn-danger" data-bind="click:$parent.deleteBadword">删除</button>
                <button class="btn btn-info" data-bind="click:$parent.upsertBadword">保存</button>
            </td>
        </tr>
        </tbody>
    </table>
    <div class="message_page_list"></div>
    </div>
</span>

<!-- 模态框（Modal） -->
<div id="previewModal" class="modal hide fade" tabindex="-1" style="width: 430px;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-body" style="max-height: 900px; width: 400px;" id="previewBox">
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>

<script type="text/html" id="previewBox_tem">
    <div class="device" style="" id="layoutInDevice">
        <div class="device-content">
            <div id="iwindow">
                <iframe width="320" height="569" frameborder="0" src="<%=url%>"></iframe>
            </div>
        </div>
    </div>
</script>

<script>
    function ViewModel() {
        this.currentPage = ko.observable(1);
        this.filter_word = ko.observable("");
        this.filter_category = ko.observable();
        this.size = ko.observable(30);
        this.badwords = ko.observableArray();
        this.moment_format_timestamp = function (timestamp) {
            return moment(new Date(timestamp)).format("YYYY-MM-DD HH:mm:ss")
        };
        this.categorys = [
            {name: "标题", value: 1},
            {name: "正文", value: 2},
            {name: "段落", value: 3},
        ];
        this.newBadword = function () {
            this.badwords.unshift({category:1});
        };
        this.load = function () {
            // load next page
            $.post("loadwechatmassbadwords.vpage", {
                currentPage: this.currentPage(),
                SIZE: this.size(),
                word: this.filter_word(),
                category: this.filter_category()
            }, function (data) {
                console.info(data);
                this.badwords(data.badwords.content);
                $(".message_page_list").page({
                    total: data.badwords.totalPages,
                    current: data.badwords.number + 1,
                    autoBackToTop: false,
                    jumpCallBack: function (index) {
                        this.currentPage(index);
                        this.load();
                    }.bind(this)
                });
            }.bind(this))
        }.bind(this);
        this.deleteBadword = function (badword) {
            badword.disabled = true;
            this.upsertBadword(badword);
        }.bind(this);
        this.upsertBadword = function (badword) {
            if (!badword.word) {
                alert("word不能为空");
                return false;
            }
            badword = JSON.stringify(badword)
            console.info(badword);
            $.post("upsertwechatmassbadword.vpage", {badword: badword}, function (data) {
                console.info(data);
                if (data.success) {
                    alert("成功");
                    this.load();
                } else {
                    alert("失败，" + data.msg);
                }
            }.bind(this))
        }.bind(this);
        this.reload=function () {
            this.currentPage(1);
            this.load();
        }
    }
    var viewModel = new ViewModel();
    viewModel.load();
    ko.applyBindings(viewModel);

</script>
</@layout_default.page>