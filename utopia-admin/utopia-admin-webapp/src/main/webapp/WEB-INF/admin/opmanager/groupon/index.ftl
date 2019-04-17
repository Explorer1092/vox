<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="运营活动管理" page_num=9>
    <script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/extends/Date.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-fileDownload/jquery.fileDownload.js"></script>
    <div id="main_container" class="span9">
        <legend>
            <strong>团购活动管理</strong>&nbsp;&nbsp;&nbsp;&nbsp;
            <a data-bind="click:addGroupon" type="button" class="btn btn-info">添加团购项目</a>
        </legend>
        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>ID</th>
                <th>名称</th>
                <th>开始时间</th>
                <th>结束时间</th>
                <th>成团人数</th>
                <th>最大人数</th>
                <th>状态</th>
                <th>过期天数</th>
                <th>适用年级</th>
                <th>允许参加多团</th>
                <th>允许拼多个产品</th>
                <th>限新用户发起</th>
                <th>限新用户参团</th>
                <th>快递只发一单</th>
                <th>创建时间</th>
                <th>最后更新</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody data-bind="foreach:groupons">
            <tr>
                <td data-bind="text:id"></td>
                <td data-bind="text:name"></td>
                <td data-bind="text:start"></td>
                <td data-bind="text:end"></td>
                <td data-bind="text:count"></td>
                <td data-bind="text:maxCount"></td>
                <td data-bind="text:stateText"></td>
                <td data-bind="text:expireStr"></td>
                <td data-bind="text:levels"></td>
                <td data-bind="text:multiGroupText"></td>
                <td data-bind="text:multiProductText"></td>
                <td data-bind="text:creatorNew"></td>
                <td data-bind="text:memberNew"></td>
                <td data-bind="text:singleShippingAddressText"></td>
                <td data-bind="text:createDateStr"></td>
                <td data-bind="text:updateDateStr"></td>
                <td>
                    <button data-bind="visible:online==false,click:$parent.online" type="button" class="btn btn-danger">
                        上线
                    </button>
                    <button data-bind="visible:online==true,click:$parent.offline" type="button"
                            class="btn btn-warning">下线
                    </button>
                    <button data-bind="click:$parent.editGroupon" type="button" class="btn btn-default">编辑</button>
                    <button data-bind="click:$parent.bindProduct" type="button" class="btn btn-default">产品管理</button>
                    <button data-bind="click:$parent.data" type="button" class="btn btn-default">统计数据</button>
                    <button data-bind="click:$parent.search" type="button" class="btn btn-default">数据检索</button>
                    <button data-bind="click:$parent.exportXls" type="button" class="btn btn-default">地址导出</button>
                </td>
            </tr>
            </tbody>
        </table>
        <div id="modalGroupon" class="modal fade hide">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <h4 data-bind="visible:isAdd" class=" button btn-default">添加团购项目</h4>
                        <h4 data-bind="visible:isEdit" class="modal-title">编辑团购项目</h4>
                    </div>
                    <div class="form-horizontal">
                        <div class="modal-body">
                            <label class="control-label" for="name">团购项目名称</label>
                            <div class="controls">
                                <input type="text" data-bind="value:name" placeholder="团购项目名称"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="start">活动开始时间</label>
                            <div class="controls">
                                <input type="text" data-bind="value:start" placeholder="格式：2018-09-06 12:00:00"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="end">活动结束时间</label>
                            <div class="controls">
                                <input type="text" data-bind="value:end" placeholder="格式：2018-09-06 12:00:00"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="count">成团需要人数</label>
                            <div class="controls">
                                <input type="text" data-bind="value:count" placeholder="仅限数字"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="count">团最大人数</label>
                            <div class="controls">
                                <input type="text" data-bind="value:maxCount" placeholder="选填，仅限数字"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="expireDays">发起后过期天数</label>
                            <div class="controls">
                                <input type="text" data-bind="value:expireDays" placeholder="选填，限数字"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="clazzLevels">适用年级</label>
                            <div class="controls">
                                <input type="text" data-bind="value:clazzLevels" placeholder="多个年级以,分隔"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="multiGroup">允许用户同时参与多个团</label>
                            <div class="controls">
                                <input id="multiGroup" type="checkbox" data-bind="checked:multiGroup"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="multiProduct">允许成员选择不同产品</label>
                            <div class="controls">
                                <input id="multiProduct" type="checkbox" data-bind="checked:multiProduct"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="creatorNew">发起人必须是新用户</label>
                            <div class="controls">
                                <input id="creatorNew" type="checkbox" data-bind="checked:creatorNew"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="memberNew">参与人必须是新用户</label>
                            <div class="controls">
                                <input id="memberNew" type="checkbox" data-bind="checked:memberNew"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="multiProduct">是否只发一个快递</label>
                            <div class="controls">
                                <input id="multiProduct" type="checkbox" data-bind="checked:singleShippingAddress"/>
                                <p style="color:red;">注意：只发一个快递将会发给发起人</p>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button data-bind="click:submit" type="button" class="btn btn-primary">提交</button>
                            <button type="button" class="btn btn-default" data-dismiss="modal">关 闭</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div id="modalStatistics" class="modal fade hide">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <h4 class="modal-title">统计数据</h4>
                    </div>
                    <div class="form-horizontal">
                        <div class="modal-body">
                            <label class="control-label" for="name">发起拼团数量</label>
                            <div class="controls">
                                <label data-bind="text:groupCount"></label>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label">拼团成功数量</label>
                            <div class="controls">
                                <label data-bind="text:successGroupCount"></label>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label">总参与人数</label>
                            <div class="controls">
                                <label data-bind="text:joinedCount"></label>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div id="modalExport" class="modal fade hide">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <h4 class="modal-title">地址导出</h4>
                    </div>
                    <div class="form-horizontal">
                        <div class="modal-body">
                            <label class="control-label" for="name">开始时间</label>
                            <div class="controls">
                                <input name="startDate" data-bind="value:exportStartDate" id="startDate" type="text"
                                       placeholder="格式：2013-11-04" readonly="readonly"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label">结束时间</label>
                            <div class="controls">
                                <input name="endDate" data-bind="value:exportEndDate" id="endDate" type="text"
                                       placeholder="格式：2013-11-04" readonly="readonly"/>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <p style="color:red;">注意：时间跨度太大有可能导出失败</p>
                            <button data-bind="click:exportSubmit" type="button" class="btn btn-primary">提交</button>
                            <button type="button" class="btn btn-default" data-dismiss="modal">关 闭</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script type="text/javascript">
        var GrouponModel = function () {
            var self = this;

            self.id = ko.observable();
            self.name = ko.observable();
            self.start = ko.observable();
            self.end = ko.observable();
            self.count = ko.observable();
            self.maxCount = ko.observable();
            self.expireDays = ko.observable();
            self.clazzLevels = ko.observable();
            self.expireStr = ko.observable('');
            self.levels = ko.observable('');
            self.multiGroup = ko.observable(false);
            self.multiProduct = ko.observable(false);
            self.creatorNew = ko.observable(false);
            self.memberNew = ko.observable(false);
            self.singleShippingAddress = ko.observable(false);
            self.isAdd = ko.observable(true);
            self.isEdit = ko.observable(false);
            self.grouponId = '';

            self.groupons = ko.observableArray([]);

            self.groupCount = ko.observable(0);
            self.successGroupCount = ko.observable(0);
            self.joinedCount = ko.observable(0);

            self.exportGrouponId = ko.observable();
            self.exportStartDate = ko.observable();
            self.exportEndDate = ko.observable();

            self.reloadGroupon = function () {
                self.groupons.removeAll();

                $.get('/opmanager/groupon/list.vpage', function (data) {
                    if (data.success) {
                        var groupons = data.groupons;
                        for (var i = 0; i < groupons.length; i++) {
                            var groupon = groupons[i];
                            groupon['start'] = new Date(groupon.startDate).format('yyyy-MM-dd HH:mm:ss');
                            groupon['end'] = new Date(groupon.endDate).format('yyyy-MM-dd HH:mm:ss');
                            groupon['createDateStr'] = new Date(groupon.createDatetime).format('yyyy-MM-dd HH:mm:ss');
                            groupon['updateDateStr'] = new Date(groupon.updateDatetime).format('yyyy-MM-dd HH:mm:ss');
                            if (groupon['expireDays'] == null || groupon['expireDays'] === 0) {
                                groupon['expireStr'] = '--';
                            } else {
                                groupon['expireStr'] = groupon['expireDays'];
                            }
                            var levels = groupon['clazzLevels'];
                            if (levels === null) {
                                groupon['levels'] = '--';
                            } else {
                                var lvlStr = '';
                                for (var j = 0; j < levels.length; j++) {
                                    lvlStr = lvlStr + levels[j] + ',';
                                }
                                if (lvlStr.length > 0) {
                                    lvlStr = lvlStr.substr(0, lvlStr.length - 1);
                                }
                                console.log(lvlStr);
                                groupon['levels'] = lvlStr;
                            }
                            groupon['stateText'] = groupon['state'] === 'ONLINE' ? '已上线' : '已下线';
                            groupon['online'] = groupon['state'] === 'ONLINE';
                            groupon['multiGroupText'] = groupon['multiGroup'] ? '是' : '否';
                            groupon['multiProductText'] = groupon['multiProduct'] ? '是' : '否';
                            groupon['creatorNew'] = groupon['creatorIsNew'] ? '是' : '否';
                            groupon['memberNew'] = groupon['memberIsNew'] ? '是' : '否';
                            groupon['singleShippingAddressText'] = groupon['singleShippingAddress'] ? '是' : '否';
                            self.groupons.push(groupons[i]);
                        }
                    } else {
                        alert(data.info);
                    }
                });
            };

            self.addGroupon = function () {
                self.id('');
                self.name('');
                self.start('');
                self.end('');
                self.count('');
                self.maxCount('');
                self.expireDays('');
                self.clazzLevels('');
                self.multiGroup(false);
                self.multiProduct(false);
                self.creatorNew(false);
                self.memberNew(false);
                self.singleShippingAddress(false);
                self.isAdd(true);
                self.isEdit(false);

                $('#modalGroupon').modal('show');
            };
            self.editGroupon = function (groupon) {
                self.id(groupon.id);
                self.name(groupon.name);
                self.start(groupon.start);
                self.end(groupon.end);
                self.count(groupon.count);
                self.maxCount(groupon.maxCount);
                self.expireDays(groupon.expireDays);
                self.clazzLevels(groupon.clazzLevels);
                self.multiProduct(groupon.multiProduct);
                self.multiGroup(groupon.multiGroup);
                self.creatorNew(groupon.creatorIsNew);
                self.memberNew(groupon.memberIsNew);
                self.singleShippingAddress(groupon.singleShippingAddress);
                self.isAdd(false);
                self.isEdit(true);

                $('#modalGroupon').modal('show');
            };
            self.submit = function () {
                if (self.name() === undefined || self.name().length === 0) {
                    alert('请输入团购名称');
                    return;
                }
                if (self.start() === undefined || self.start().length === 0) {
                    alert('请输入开始时间');
                    return;
                }
                if (self.end() === undefined || self.end().length === 0) {
                    alert('请输入结束时间');
                    return;
                }
                if (self.count() === undefined) {
                    alert('请输入成团需要人数');
                }

                if (self.isAdd()) {
                    $.post('/opmanager/groupon/add.vpage', {
                        name: self.name(),
                        start: self.start(),
                        end: self.end(),
                        count: self.count(),
                        maxCount:self.maxCount(),
                        expireDays: self.expireDays(),
                        levels: self.clazzLevels(),
                        multiProduct: self.multiProduct(),
                        multiGroup: self.multiGroup(),
                        creatorNew: self.creatorNew(),
                        memberNew: self.memberNew(),
                        singleShippingAddress: self.singleShippingAddress()
                    }, function (data) {
                        if (data.success) {
                            alert('添加成功');
                            self.reloadGroupon();
                            $('#modalGroupon').modal('hide');
                        } else {
                            alert(data.info);
                        }
                    });
                } else {
                    if (self.id() === undefined || self.id().length === 0) {
                        alert('无效的ID');
                        return;
                    }
                    $.post('/opmanager/groupon/update.vpage', {
                        id: self.id(),
                        name: self.name(),
                        start: self.start(),
                        end: self.end(),
                        count: self.count(),
                        maxCount:self.maxCount(),
                        expireDays: self.expireDays(),
                        levels: self.clazzLevels(),
                        multiProduct: self.multiProduct(),
                        multiGroup: self.multiGroup(),
                        creatorNew: self.creatorNew(),
                        memberNew: self.memberNew(),
                        singleShippingAddress: self.singleShippingAddress()
                    }, function (data) {
                        if (data.success) {
                            alert('操作成功');
                            self.reloadGroupon();
                            $('#modalGroupon').modal('hide');
                        } else {
                            alert(data.info);
                        }
                    });
                }
            };

            self.online = function (groupon) {
                if (!confirm('确定要上线吗？')) {
                    return;
                }
                $.post('/opmanager/groupon/online.vpage', {id: groupon.id}, function (data) {
                    if (data.success) {
                        alert('操作成功');
                        self.reloadGroupon();
                    } else {
                        alert(data.info);
                    }
                });
            };
            self.offline = function (groupon) {
                if (!confirm('确定要下线吗？')) {
                    return;
                }
                $.post('/opmanager/groupon/offline.vpage', {id: groupon.id}, function (data) {
                    if (data.success) {
                        alert('操作成功');
                        self.reloadGroupon();
                    } else {
                        alert(data.info);
                    }
                });
            };

            self.reloadGroupon();

            //产品管理
            self.bindProduct = function (groupon) {
                window.open('/opmanager/groupon/product.vpage?grouponId=' + groupon.id);
            };
            //统计数据
            self.data = function (groupon) {
                $.get('/opmanager/groupon/data.vpage?grouponId=' + groupon.id, function (data) {
                    if (data.success) {
                        self.groupCount(data.groupCount);
                        self.successGroupCount(data.successGroupCount);
                        self.joinedCount(data.joinedCount);

                        $('#modalStatistics').modal('show');
                    } else {
                        alert(data.info);
                    }
                });
            };
            //数数检索
            self.search = function (groupon) {
                window.open('/opmanager/groupon/search.vpage?grouponId=' + groupon.id);
            };

            //快递地址Excel导出
            self.exportXls = function (groupon) {
                self.exportGrouponId(groupon.id);

                $('#modalExport').modal('show');
            };

            self.exportSubmit = function () {
                if (self.exportGrouponId() === undefined) {
                    alert('项目id不能为空');
                    return;
                }
                if (self.exportStartDate() === undefined) {
                    alert('开始时间不能为空');
                    return;
                }
                if (self.exportEndDate() === undefined) {
                    alert('结束时间不能为空');
                    return;
                }

                $('#modalExport').modal('hide');

                $.fileDownload('/opmanager/groupon/shipping/export.vpage', {
                    data: {
                        grouponId: self.exportGrouponId(),
                        startDate: self.exportStartDate(),
                        endDate: self.exportEndDate()
                    },
                    httpMethod: "POST",
                    successCallback: function (url) {
                    },
                    failCallback: function (html, url) {
                        alert('Your file download just failed for this URL:' + url + '\r\n' +
                            'Here was the resulting error HTML: \r\n' + html
                        );
                    }
                });
            };

            $("#startDate").datepicker({
                dateFormat: 'yy-mm-dd',  //日期格式，自己设置
                monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
                defaultDate: new Date(),
                numberOfMonths: 1,
                changeMonth: false,
                changeYear: false,
                onSelect: function (selectedDate) {
                    self.exportStartDate(selectedDate);
                }
            });

            $("#endDate").datepicker({
                dateFormat: 'yy-mm-dd',  //日期格式，自己设置
                monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
                defaultDate: new Date(),
                numberOfMonths: 1,
                changeMonth: false,
                changeYear: false,
                onSelect: function (selectedDate) {
                    self.exportEndDate(selectedDate);
                }
            });
        };

        ko.applyBindings(new GrouponModel());


    </script>
</@layout_default.page>
