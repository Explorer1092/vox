<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-关键词管理' page_num=13>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>
<div class="span9">

    关键词管理
    <hr>
    <input type="text" data-bind="value:newKeyword">&nbsp;&nbsp;
    <button class="btn btn-info" data-bind="click:addKeyword">添加</button>
    <br>
    <br>
    <table class="table table-hover table-striped table-bordered">
        <thead>
        <tr>
            <th>关键词</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody data-bind="foreach:keywords">
            <tr>
                <td data-bind="text:$data.keyword"></td>
                <td>
                    <button class="btn btn-danger" data-bind="click:$parent.deleteKeyword">删除</button>
                </td>
            </tr>
        </tbody>
    </table>

</div>

<script type="text/javascript">
    var firstId = "${firstId}";
    var secondId = "${secondId}";
    function ViewModel() {
        this.firstId = ko.observable(firstId);
        this.secondId = ko.observable(secondId);
        this.keywords = ko.observableArray();
        this.addKeyword = function () {
            var newKeyword = this.newKeyword();
            if (!newKeyword) {
                alert("请填入关键词");
                return false;
            }
            $.post("add_tag_keywords.vpage", {
                "firstId": this.firstId(),
                "secondId": this.secondId(),
                "keyWord": newKeyword
            }, function (data) {
                console.info(data);
                this.loadCurrentKeywords();
            }.bind(this))
        }.bind(this);
        this.newKeyword = ko.observable("");
        this.inputKeyword = ko.observable("");
        this.loadCurrentKeywords = function () {
            console.info("loadCurrentKeywords");
            $.post('load_tag_keywords.vpage', {"secondId": this.secondId()}, function (data) {
                console.info(data);
                this.keywords.removeAll();
                if (data.success) {
                    for (var i = 0; i < data.keyWordsList.length; i++) {
                        this.keywords.push(data.keyWordsList[i]);
                    }
//                    console.info(this.keywords());
                } else {
                    alert(data.info);
                }
            }.bind(this));
        }.bind(this);
        this.deleteKeyword = function (item) {
            console.info("deleteKeyword");
            $.post("del_tag_keywords.vpage", {"secondId": this.secondId(), "delKeyWords": item.id}, function (data) {
                console.info(data);
                this.loadCurrentKeywords();
            }.bind(this));
            console.info(item);
        }.bind(this);
        this.init = function () {
            console.info("init");
            this.loadCurrentKeywords();
        }.bind(this);
    }
    var viewModel = new ViewModel();
    viewModel.init();
    ko.applyBindings(viewModel);

</script>
</@layout_default.page>