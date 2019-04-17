<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='方案管理' page_num=20>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>
<div class="span9">
    <div class="hero-unit">
        <h1>请为实验(${id!""})配置实验方案</h1>
    </div>
    <button data-bind="click:newPlan" class="btn btn-default">新建方案</button>
    <span data-bind="visible:!defaultPlanId()" style="float:right;color:red">请设置默认方案</span>
    <table class="table table-hover table-striped table-bordered">
        <thead>
        <tr>
            <th>ID</th>
            <th>名称</th>
            <th>描述</th>
            <th>是否默认</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody data-bind="foreach:plans">
        <tr>
            <td><input data-bind="value:$data.id" readonly></td>
            <td><input data-bind="value:$data.planName"></td>
            <td><input data-bind="value:$data.planDesc"></td>
            <td data-bind="text:$data.isDefault"></td>
            <td>
                <button class="btn btn-default" data-bind="click:$parent.upsertPlan">保存</button>
                <button class="btn btn-default" data-bind="click:$parent.deletePlan">删除</button>
                <button class="btn btn-default" data-bind="click:$parent.setDefault,visible:!$data.isDefault">设为默认</button>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<script type="application/javascript">
    function PlansViewModel() {
        this.experimentId = ko.observable("${id}");
        this.plans = ko.observableArray();
        this.loadPlans = function () {
            $.post("loadplans.vpage", {experimentId: this.experimentId()}, function (data) {
                console.info(data);
                this.plans(data['plans']);
            }.bind(this))
        }.bind(this);
        this.defaultPlanId=ko.computed(function () {
            for(var x in this.plans()){
                console.info(this.plans()[x].isDefault);
                if(this.plans()[x].isDefault){
                    return this.plans()[x].id;
                }
            }
            return false;
        }.bind(this));
        this.upsertPlan=function (plan) {
            $.post("upsertplan.vpage",{'plan':JSON.stringify(plan)},function (data) {
                console.info(data);
                this.loadPlans();
            }.bind(this))
        }.bind(this);
        this.deletePlan=function (plan) {
            plan.disabled=true;
            this.upsertPlan(plan);
        }.bind(this);
        this.setDefault=function (plan) {
            var planId=plan.id;
            if(planId){
                $.post("setdefaultplan.vpage",{"experimentId":this.experimentId(),"planId":planId},function (data) {
                    console.info(data);
                    this.loadPlans();
                }.bind(this))
            }
        }.bind(this);
        this.newPlan=function () {
            this.plans.unshift({"experimentId":this.experimentId(),'disabled':false,'isDefault':false});
        };
    }
    viewModel = new PlansViewModel();
    viewModel.loadPlans();
    ko.applyBindings(viewModel);
</script>
</@layout_default.page>