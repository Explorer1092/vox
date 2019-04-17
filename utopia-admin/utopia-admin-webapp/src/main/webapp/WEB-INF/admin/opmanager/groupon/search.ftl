<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="运营活动管理" page_num=9>
    <script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/extends/Date.js"></script>
    <div id="main_container" class="span9">
        <legend>
            <strong>团购数据检索:${grouponName}</strong>&nbsp;&nbsp;&nbsp;&nbsp;
        </legend>
        <div class="row-fluid">
            <div class="span12">
                <div class="well">
                    <h4>用户参团数据</h4>
                    <form id="activity-query" class="form-horizontal">
                        <ul class="inline">
                            <li>
                                <label>用户id&nbsp;
                                    <input type="text" data-bind="value:userId"/>
                                </label>
                            </li>
                            <li>
                                <button data-bind="click:search" type="button" id="filter" class="btn btn-primary">查询
                                </button>
                            </li>
                        </ul>
                    </form>
                    <table class="table table-striped table-bordered">
                        <thead>
                        <tr>
                            <th>项目名称</th>
                            <th>拼团id</th>
                            <th>开始时间</th>
                            <th>状态</th>
                            <th>成团人数</th>
                            <th>最大人数</th>
                            <th>当前人数</th>
                            <th>操作</th>
                        </tr>
                        </thead>
                        <tbody data-bind="foreach:datas">
                        <tr>
                            <td data-bind="text:name"></td>
                            <td data-bind="text:id"></td>
                            <td data-bind="text:startDateStr"></td>
                            <td data-bind="text:state"></td>
                            <td data-bind="text:totalCount"></td>
                            <td data-bind="text:maxTotalCount"></td>
                            <td data-bind="text:currentCount"></td>
                            <td>
                                <button data-bind="click:$parent.showMembers">显示成员</button>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <table data-bind="visible:memberVisible" class="table table-striped table-bordered">
                        <thead>
                        <tr>
                            <th>id</th>
                            <th>用户id</th>
                            <th>称呼</th>
                            <th>产品名称</th>
                            <th>状态</th>
                            <th>是发起人</th>
                            <th>更新时间</th>
                        </tr>
                        </thead>
                        <tbody data-bind="foreach:members">
                        <tr>
                            <td data-bind="text:id"></td>
                            <td data-bind="text:userId"></td>
                            <td data-bind="text:callName"></td>
                            <td data-bind="text:productName"></td>
                            <td data-bind="text:state"></td>
                            <td data-bind="text:owner?'是':'否'"></td>
                            <td data-bind="text:updateDateStr"></td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        <div class="row-fluid">
            <div class="span12">
                <div class="well">
                    <h4>用户地址数据</h4>
                    <form id="activity-query" class="form-horizontal">
                        <ul class="inline">
                            <li>
                                <label>用户id&nbsp;
                                    <input type="text" data-bind="value:addressUserId"/>
                                </label>
                            </li>
                            <li>
                                <label>拼团id&nbsp;
                                    <input type="text" data-bind="value:addressGroupId"/>
                                </label>
                            </li>
                            <li>
                                <button data-bind="click:addressSearch" type="button" id="filter"
                                        class="btn btn-primary">查询
                                </button>
                            </li>
                        </ul>
                    </form>
                    <h4>地址库</h4>
                    <table class="table table-striped table-bordered">
                        <thead>
                        <tr>
                            <th>id</th>
                            <th>收货人</th>
                            <th>手机</th>
                            <th>省份</th>
                            <th>城市</th>
                            <th>地区</th>
                            <th>详细地址</th>
                        </tr>
                        </thead>
                        <tbody data-bind="foreach:addresses">
                        <tr>
                            <td data-bind="text:id"></td>
                            <td data-bind="text:name"></td>
                            <td data-bind="text:mobile"></td>
                            <td data-bind="text:province"></td>
                            <td data-bind="text:city"></td>
                            <td data-bind="text:district"></td>
                            <td data-bind="text:addr"></td>
                        </tr>
                        </tbody>
                    </table>
                    <h4>拼团快递地址</h4>
                    <table class="table table-striped table-bordered">
                        <thead>
                        <tr>
                            <th>id</th>
                            <th>收货人</th>
                            <th>手机</th>
                            <th>省份</th>
                            <th>城市</th>
                            <th>地区</th>
                            <th>详细地址</th>
                        </tr>
                        </thead>
                        <tbody data-bind="foreach:groupAddresses">
                        <tr>
                            <td data-bind="text:id"></td>
                            <td data-bind="text:name"></td>
                            <td data-bind="text:mobile"></td>
                            <td data-bind="text:province"></td>
                            <td data-bind="text:city"></td>
                            <td data-bind="text:district"></td>
                            <td data-bind="text:addr"></td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
    <script type="text/javascript">
        var GroupDataModel = function () {
            var self = this;
            var grouponId = '${grouponId}';

            self.datas = ko.observableArray();
            self.members = ko.observableArray();
            self.memberVisible = ko.computed(function () {
                return self.members().length > 0;
            }, this);

            self.userId = ko.observable();

            self.search = function () {
                self.datas.removeAll();
                self.members.removeAll();

                if (self.userId === undefined) {
                    return;
                }

                $.get('/opmanager/groupon/query.vpage?uid=' + self.userId() + '&grouponId=' + grouponId, function (data) {
                    if (data.success) {
                        if (data.datas !== undefined) {
                            for (var i = 0; i < data.datas.length; i++) {
                                var d = data.datas[i];
                                self.datas.push(d);
                            }
                        }
                    } else {
                        alert(data.info);
                    }
                });
            };

            self.showMembers = function (data) {
                self.members.removeAll();

                $.get('/opmanager/groupon/group/member.vpage?groupId=' + data.id, function (data) {
                    if (data.success) {
                        if (data.members !== undefined) {
                            for (var i = 0; i < data.members.length; i++) {
                                var member = data.members[i];
                                self.members.push(member);
                            }
                        }
                    } else {
                        alert(data.info);
                    }
                });
            };

            //地址查询
            self.addressUserId = ko.observable();
            self.addressGroupId = ko.observable();
            self.addresses = ko.observableArray();
            self.groupAddresses = ko.observableArray();

            self.addressSearch = function () {
                self.addresses.removeAll();
                self.groupAddresses.removeAll();

                if (self.addressUserId() === undefined) {
                    return;
                }

                $.get('/opmanager/groupon/address/search.vpage?uid=' + self.addressUserId() + '&groupId=' + self.addressGroupId(), function (data) {
                    if (data.success) {
                        if (data.grouponAddress !== undefined) {
                            self.groupAddresses.push(data.grouponAddress);
                        }

                        if (data.address !== undefined) {
                            for (var i = 0; i < data.address.length; i++) {
                                self.addresses.push(data.address[i]);
                            }
                        }
                    } else {
                        alert(data.info);
                    }
                });
            };
        };

        ko.applyBindings(new GroupDataModel());
    </script>
</@layout_default.page>