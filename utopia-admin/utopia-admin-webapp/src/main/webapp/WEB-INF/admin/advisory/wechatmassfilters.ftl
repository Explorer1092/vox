<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-公众号敏感词' page_num=13>
<style>
    .modal {
        background-color: inherit;
    !important;
    }

    .modal.fade.in {
        top: 1%
    }

    .device {
        background-image: url("/public/img/device-sprite.png");
        background-position: 0 0;
        background-repeat: no-repeat;
        background-size: 300% auto;
        display: block;
        font-family: "Helvetica Neue", sans-serif;
        height: 813px;
        position: relative;
        transition: background-image 0.1s linear 0s;
        width: 395px;
    }

    .device .device-content {
        background: #eeeeee none repeat scroll 0 0;
        font-size: 0.85rem;
        height: 569px;
        left: 37px;
        line-height: 1.05rem;
        overflow: hidden;
        position: absolute;
        top: 117px;
        width: 321px;
    }
</style>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/moment.js"></script>
<span class="span9">
    公众号名称：<input class="input-large" data-bind="value:filter_name">
    <button class="btn btn-info" data-bind="click:reload">查询</button>
    <button class="btn btn-success" data-bind="click:newFilter">新建</button>
    <table class="table table-hover table-striped table-bordered table-large">
        <thead>
        <tr>
            <#--<th>id</th>-->
                <th>名称</th>
            <th>biz</th>
            <th>图片</th>
            <th>引导词</th>
            <th>创建时间</th>
            <th>创建人</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody data-bind="foreach:filters">
        <tr>
            <#--<td data-bind="text:$data.id"></td>-->
                <td><input class="input-small" data-bind="value:$data.name"></td>
            <td><input class="input-small" data-bind="value:$data.biz"></td>
            <td><textarea data-bind="value:$data.images"></textarea></td>
            <td><textarea data-bind="value:$data.badwords"></textarea></td>
            <td data-bind="text:$parent.moment_format_timestamp($data.create_datetime)"></td>
            <td data-bind="text:$data.editor"></td>
            <td>
                <button class="btn btn-danger" data-bind="click:$parent.deleteFilter">删除</button>
                <button class="btn btn-info" data-bind="click:$parent.upsertFilter">保存</button>
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
        this.filter_name = ko.observable("");
        this.size = ko.observable(30);
        this.filters = ko.observableArray();
        this.moment_format_timestamp = function (timestamp) {
            return moment(new Date(timestamp)).format("YYYY-MM-DD HH:mm:ss")
        };
        this.newFilter = function () {
            this.filters.unshift({});
        };
        this.load = function () {
            // load next page
            $.post("loadwechatmassfilters.vpage", {
                currentPage: this.currentPage(),
                SIZE: this.size(),
                name: this.filter_name()
            }, function (data) {
                console.info(data);
                this.filters(data.filters.content);
                ko.utils.arrayForEach(this.filters(),function(item){
                    item.images=item.images.join(",");
                    item.badwords=item.badwords.join(",");
                });
                $(".message_page_list").page({
                    total: data.filters.totalPages,
                    current: data.filters.number + 1,
                    autoBackToTop: false,
                    jumpCallBack: function (index) {
                        this.currentPage(index);
                        this.load();
                    }.bind(this)
                });
            }.bind(this))
        }.bind(this);
        this.deleteFilter = function (filter) {
            filter.disabled = true;
            this.upsertFilter(filter);
        }.bind(this);
        this.upsertFilter = function (filter) {
            if(!filter.biz){
                alert("biz不能为空");
                return false;
            }
            filter = JSON.stringify(filter)
            console.info(filter);
            $.post("upsertwechatmassfilter.vpage", {filter: filter}, function (data) {
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