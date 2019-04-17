<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="渠道管理" page_num=24>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<style>
</style>
<span class="span9" style="font-size: 14px">
    <h1>渠道管理</h1>
    <select data-bind="options:categories,value:filterCategory,optionsText:'text',optionsValue:'value'"></select>
    <input type="text" class="form-control" data-bind="value:filterDescription" placeholder="渠道描述">
    <select data-bind="options:labelsWithNull,optionsText:'label',optionsValue:'id',value:filterLabel">
    </select>
    <button style="margin-top: -10px" class="btn btn-default" type="button">搜索</button>

    <p style="float: right; display: inline-block" class="btn-group btn-group-justified" role="group">
        <button class="btn btn-info" data-bind="click:showAddModal">新增渠道</button>
        <button class="btn btn-info" data-bind="click:showAddLabelModal">活动标签维护</button>
    </p>

    <table class="table table-hover table-striped table-bordered">

        <thead>
        <tr>
            <th style="width:10%">渠道ID</th>
            <th style="width:20%">描述</th>
            <th style="width:15%">类别</th>
            <th>活动标签</th>
            <th>创建时间</th>
            <th>更新时间</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody data-bind="foreach:channels">
            <tr>
                <td data-bind="text:$data.id"></td>
                <td data-bind="text:$data.description"></td>
                <td><select class="input-small"
                            data-bind="options:$parent.categories,value:$data.category,optionsText:'text',optionsValue:'value'"
                            disabled></select></td>
                <td data-bind="text:$parent.generateLabelNames($data.label)"></td>
                <td data-bind="text:momentDatetime($data.createDatetime)"></td>
                <td data-bind="text:momentDatetime($data.updateDatetime)"></td>
                <td>
                    <button class="btn btn-info" data-bind="click:$parent.showUpdateModal">编辑</button>
                </td>
            </tr>
        </tbody>
    </table>
</span>

<div class="modal fade" id="addChannelModal" tabindex="-1" role="dialog" aria-hidden="true" style="display: none">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="myModalLabel">添加或编辑渠道</h4>
            </div>
            <div class="modal-body">

                <form class="form-horizontal">
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label" for="input01">描述</label>
                            <div class="controls">
                                <input type="text" placeholder="" class="input-xlarge"
                                       data-bind="value:currentChannel().description"/>
                                <p class="help-block"></p>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="control-label" for="input01">类别</label>
                            <div class="controls">
                                <select class="input-xlarge" data-bind="options:categories,value:currentChannel().category,optionsText:'text',optionsValue:'value'"></select>
                                <p class="help-block"></p>
                            </div>
                        </div>

                        <!-- 增加活动标签 -->
                        <div class="control-group">
                            <div style="width:65%; float: right;" class="alert alert-danger" role="alert">
                                <p><small>为保证数据统计准确性，请添加两个标签：</small></p>
                                <p><small>1.活动内付费渠道/活动导产品渠道</small></p>
                                <p><small>2.活动ID</small></p>
                            </div>
                            <p class="help-block"></p>

                            <label class="control-label" for="input01">活动标签</label>
                            <div class="controls">
                                <!-- 点击弹窗动作 -->
                                <div data-bind="foreach:labels">
                                    <div>
                                        <span><input data-bind="value:$data.id,checked:$parent.checkedLabels" type="checkbox"></span>
                                        <span data-bind="text:$data.label"></span>
                                    </div>
                                </div>

                                <p class="help-block"></p>
                            </div>
                        </div>
                    </fieldset>
                </form>

            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-info" data-dismiss="modal" data-bind="click:addChannel">
                    提交
                </button>
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
            </div>
        </div><!-- /.modal-content -->
    </div>
</div>

<div class="modal fade" id="showAddLabelModal" tabindex="-1" role="dialog" aria-hidden="true" style="display: none">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="myModalLabel">添加或编辑标签</h4>
            </div>

            <div class="modal-body">
                <form class="form-horizontal">
                    <fieldset>
                        <!-- 事件绑定 -->
                        <div class="control-group">
                            <div class="input-group">
                                <input class="input-group form-control" placeholder="请输入您想添加的标签" data-bind="value:newLabel">
                                <span class="input-group-btn">
                                    <button class="btn btn-default" type="button" data-bind="click:addNewLabel">添加</button>
                                </span>
                            </div>
                        </div>

                        <div>
                            <table class="table table-hover table-striped table-bordered">
                                <thead>
                                <tr>
                                    <th>标签描述</th>
                                    <th>操作</th>
                                </tr>
                                </thead>
                                <tbody data-bind="foreach:labels">
                                <tr>
                                    <td data-bind="text:$data.label"></td>
                                    <td>
                                        <button class="btn btn-info" data-bind="click: $parent.removeLabel">删除</button>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </fieldset>
                </form>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
            </div>
        </div>
    </div>
</div>

<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/moment.js"></script>
<script>

    function momentDatetime(date) {
        return moment(new Date(date)).format("YYYY-MM-DD HH:mm:ss");
    }

    function copy(obj) {
        return JSON.parse(JSON.stringify(obj));
    }

    function ViewModel() {
        this.filterCategory = ko.observable();
        this.filterDescription = ko.observable();
        this.filterLabel = ko.observable();
        this.categories = ko.observableArray([{"text": "学生", "value": 1}, {"text": "家长", "value": 2}]);
        this.allChannels = ko.observableArray([]);
        this.checkedLabels=ko.observableArray([]);
        this.generateLabelNames=function (labelStr) {
            var labels=this.labels();
            var labelIds;
            if(labelStr){
                labelIds=labelStr.split(",");
            }else{
                labelIds=[];
            }
            var targets=ko.utils.arrayFilter(labels,function(item){
                return labelIds.indexOf(item.id)!=-1;
            });
            var names=[];
            for(var i=0;i<targets.length;i++){
                names.push(targets[i].label);
            }
            console.info(names);
            return names.join(",");
        };
        this.channels = ko.computed(function () {
            var filterCategory = this.filterCategory();
            var filterDescription = this.filterDescription();
            var filterLabel = this.filterLabel();
            return ko.utils.arrayFilter(this.allChannels(), function (item) {
                var b1 = true, b2 = true, b3 = true;
                if (filterCategory) {
                    b1 = item.category == filterCategory;
                }
                if (filterDescription) {
                    b2 = item.description.indexOf(filterDescription) != -1;
                }
                //标签过滤
                if (filterLabel) {
                    if(!item.label){
                        b3=false;
                    }else{
                        b3 = item.label.indexOf(filterLabel) != -1;
                    }
                }
                return b1 && b2 && b3;
            })
        }.bind(this));

        //标签资源
        this.labels = ko.observableArray([]);
        this.newLabel = ko.observable();

        this.labelsWithNull=ko.computed(function () {
            var labels=copy(this.labels());
            labels.unshift({"label":"全部",id:""});
            return labels;
        }.bind(this));

        this.currentChannel = ko.observable({});

        this.loadAll = function () {
            $.post('queryall.vpage', function (data) {
                console.info(data);
                var dataList = data.channels;
                var labels = data.channelLabel;

                this.allChannels(dataList.reverse());
                this.labels(labels);
            }.bind(this))
        }.bind(this);

        this.showAddModal = function () {
            console.info("showAddModal");
            this.currentChannel({});
            $("#addChannelModal").modal("show");
        }.bind(this);

        //修改或编辑标签弹窗弹出
        this.showAddLabelModal = function () {
            console.info("showLabelModal");
            this.checkedLabels([]);
            $("#showAddLabelModal").modal("show");
        }.bind(this);

        //增加标签
        this.addNewLabel = function () {
            var label = this.newLabel();
            console.info(label);
            $.post("addrelationlabel.vpage", {label: label}, function (data) {
                console.info(data);
                if (data.success) {
                    this.loadAll();
                } else {
                    alert(data.info);
                }
            }.bind(this))
        }.bind(this);

        //移除标签
        this.removeLabel = function (label) {
            var r = confirm("确定要删除此标签么？删除后会可能会导致渠道统计的收入不准确");
            if (r !== true) {
                return;
            }
            var labelId = label.id;
            console.info(labelId);
            $.post("removelabel.vpage", {labelId: labelId}, function (data) {
                console.info(data);
                if (data.success) {
                    this.loadAll();
                } else {
                    alert(data.info);
                }
            }.bind(this))
        }.bind(this);

        this.showUpdateModal = function (channel) {
            if(channel.label){
                if(!Array.isArray(channel.label)) {
                    channel.label = channel.label.split(",");
                }
            }else{
                channel.label=[];
            }
            this.checkedLabels(channel.label);
            this.currentChannel(channel);
            $("#addChannelModal").modal("show");
        }.bind(this);

        //添加渠道
        this.addChannel = function () {
            var channel = this.currentChannel();
            channel.label=this.checkedLabels();
            console.info(channel);
            $.post("upsert.vpage", {channel: JSON.stringify(channel)}, function (data) {
                console.info(data);
                if (data.success) {
                    this.loadAll();
                } else {
                    alert(data.info);
                }
            }.bind(this))
        }.bind(this)
    }

    var viewModel = new ViewModel();
    viewModel.loadAll();
    ko.applyBindings(viewModel);
</script>
</@layout_default.page>