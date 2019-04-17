<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-实验列表' page_num=20>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/moment.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>
<div class="span9">
    <fieldset>
        <legend>实验管理</legend>
    </fieldset>

    <span>
        <button data-bind="click:newExperiment" class="btn btn-default">新建实验</button>
        <button class="btn btn-default" data-bind="click:upsertExperiment">保存</button>
        <!-- ko if:currentExperiment().status!=1 -->
        <button class="btn btn-default" data-bind="click:toggleExperimentStatus">下线</button>
        <!-- /ko -->
        <!-- ko if:currentExperiment().status==1 -->
        <button class="btn btn-default" data-bind="click:toggleExperimentStatus">上线</button>
        <!-- /ko -->
        <button class="btn btn-default" data-bind="click:deleteExperiment">删除</button>
        <button class="btn btn-default"><a data-bind="attr:{href:'choosetag.vpage?id='+currentExperiment().id}"
                                           target="_blank">配置标签</a></button>
        <button class="btn btn-default"><a data-bind="attr:{href:'setschoolfilter.vpage?id='+currentExperiment().id}"
                                           target="_blank">配置学校</a></button>
        <button class="btn btn-default"><a data-bind="attr:{href:'plans.vpage?id='+currentExperiment().id}"
                                           target="_blank">配置方案</a></button>
        <button class="btn btn-default"><a data-bind="attr:{href:'groups.vpage?id='+currentExperiment().id}"
                                           target="_blank">配置分组</a></button>
    </span>
    <table class="table table-hover table-striped table-bordered">
        <thead>
        <tr>
            <th>选择</th>
            <th>id</th>
            <th>名称</th>
            <th>目的</th>
            <th>状态</th>
            <th>分流截止时间</th>
            <th>有效期至</th>
            <th>假设</th>
            <th>描述</th>
            <th>业务线</th>
            <th>实验类型</th>
        </tr>
        </thead>
        <tbody data-bind="foreach:experiments">
        <tr>
            <td><input type="radio" name="radio_current_index"
                       data-bind="value:$index,checked:$parent.currentExperimentIndex"></td>
            <td><input data-bind="value:$data.id" readonly></td>
            <td><input data-bind="value:$data.name"></td>
            <td><input data-bind="value:$data.purpose"></td>
            <td><input class="input-small" data-bind="value:$data.status!=1?'online':'offline'" readonly></td>
            <td><input class="input-large" data-bind="value:$data.shardEndTime,click:$parent.setDatePicker"></td>
            <td><input class="input-large" data-bind="value:$data.expireDatetime,click:$parent.setDatePicker"></td>
            <td><input data-bind="value:$data.assumption"></td>
            <td><input data-bind="value:$data.description"></td>
            <td><input class="input-small" data-bind="value:$data.business"></td>
            <td><input class="input-small" data-bind="value:$data.category"></td>
        </tr>
        </tbody>
    </table>
    <div class="message_page_list"></div>
</div>
</div>
<script>
    function ExperimentsViewModel() {
        this.currentExperimentIndex = ko.observable(0);
        this.experiments = ko.observableArray([]);
        this.setDatePicker = function (item, event) {
            $(event.target).datetimepicker({format: 'yyyy-mm-dd hh:ii:ss'}).datetimepicker("show");
        };
        this.showStatusText = function (status) {
            if (status != 1) {
                return "online";
            } else {
                return "offline";
            }
        };
        this.currentExperiment = ko.computed(function () {
            if (this.experiments().length > 0) {
                return this.experiments()[this.currentExperimentIndex()];
            } else {
                return {};
            }

        }.bind(this));
        this.currentPage = ko.observable(1);
        this.loadCurrentPageExperiments = function () {
            $.post("loadcurrentpageexperiments.vpage", {currentPage: this.currentPage()}, function (data) {
                var experiments = data['experiments']['content'];
                for (var t = 0; t < experiments.length; t++) {
                    var item = experiments[t];
                    item.expireDatetime = moment(new Date(item.expireDatetime)).format("YYYY-MM-DD HH:mm:ss");
                    item.shardEndTime = moment(new Date(item.shardEndTime)).format("YYYY-MM-DD HH:mm:ss");
                }
                this.experiments(experiments);
                $(".message_page_list").page({
                    total: data.experiments.totalPages,
                    current: data.experiments.number + 1,
                    autoBackToTop: false,
                    jumpCallBack: function (index) {
                        this.currentPage(index);
                        this.loadCurrentPageExperiments();
                    }.bind(this)
                });
            }.bind(this))
        }.bind(this);
        this.newExperiment = function () {
            this.experiments.unshift({"disabled": false, 'status': "offline"});
            this.currentExperimentIndex(0);
        };
        this.upsertExperiment = function () {
            var experiment = this.currentExperiment();
            if(!experiment.expireDatetime){
                alert("请设置实验过期时间");
                return false;
            }
            $.post("upsertexperiment.vpage", {'experiment': JSON.stringify(experiment)}, function (data) {
                this.loadCurrentPageExperiments();
            }.bind(this))
        }.bind(this);
        this.deleteExperiment = function () {
            if(!confirm("确认删除?")){
                return false;
            }
            var experiment = this.currentExperiment();
            experiment.disabled = true;
            this.upsertExperiment(experiment);
        }.bind(this);
        this.toggleExperimentStatus = function () {
            var experiment = this.currentExperiment();
            experiment.status = experiment.status == 1 ? 2 : 1;
            this.upsertExperiment(experiment);
        }.bind(this);
        this.chooseTag = function () {
            var experiment = this.currentExperiment();
            var id = experiment.id;
            window.location.href = "choosetag.vpage?id=" + id;
        };
        this.addPlan = function () {
            var experiment = this.currentExperiment();
            var id = experiment.id;
            window.location.href = "plans.vpage?id=" + id;
        };
        this.setGroups = function () {
            var experiment = this.currentExperiment();
            var id = experiment.id;
            window.location.href = "groups.vpage?id=" + id;
        };
    }
    viewModel = new ExperimentsViewModel();
    viewModel.loadCurrentPageExperiments();
    ko.applyBindings(viewModel);
</script>
</@layout_default.page>