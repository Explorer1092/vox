<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='组管理' page_num=20>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>
<div class="span9">
    <div class="hero-unit">
        <h1>请为实验(${id!""})配置实验分组</h1>
    </div>
    <button data-bind="click:newGroup" class="btn btn-default">新建分组</button>
    <table class="table table-hover table-striped table-bordered">
        <thead>
        <tr>
            <th>groupName</th>
            <th>groupDesc</th>
            <th>groupPortion</th>
            <th>planId</th>
            <th>operation</th>
        </tr>
        </thead>
        <tbody data-bind="foreach:groups">
        <tr>
            <td><input data-bind="value:$data.groupName"></td>
            <td><input data-bind="value:$data.groupDesc"></td>
            <td><input data-bind="value:$data.groupPortion" type="text" class="input-small"></td>
            <td>
                <select data-bind="options:$parent.plans,optionsCaption:'选择方案',optionsText:function(item){return item.planName+'('+item.planDesc+')'},optionsValue:function(item){return item.id},value:$data.planId"></select>
            </td>
            <td>
                <button class="btn btn-default" data-bind="click:$parent.upsertGroup">保存</button>
                <button class="btn btn-default" data-bind="click:$parent.deleteGroup">删除</button>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<script type="application/javascript">
    function GroupsViewModel() {
        this.experimentId = ko.observable("${id}");
        this.groups = ko.observableArray();
        this.plans = ko.observableArray(${plans!''});
        this.loadGroups = function () {
            console.info("load groups");
            $.post("loadgroups.vpage", {experimentId: this.experimentId()}, function (data) {
                console.info(data);
                this.groups(data['groups']);
            }.bind(this))
        }.bind(this);
        this.upsertGroup = function (group) {
            $.post("upsertgroup.vpage", {'group': JSON.stringify(group)}, function (data) {
                console.info(data);
                this.loadGroups();
            }.bind(this))
        }.bind(this);
        this.deleteGroup = function (group) {
            group.disabled = true;
            this.upsertGroup(group);
        }.bind(this);
        this.newGroup = function () {
            this.groups.unshift({"experimentId": this.experimentId(), 'disabled': false});
        };
    }
    viewModel = new GroupsViewModel();
    viewModel.loadGroups();
    ko.applyBindings(viewModel);
    console.info("apply binding");
    setTimeout(function () {

    }, 5000);


</script>
</@layout_default.page>