<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="运营活动管理" page_num=9>
    <script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/extends/Date.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-fileDownload/jquery.fileDownload.js"></script>

    <div id="main_container" class="span9">
        <legend>
            <strong>任务管理</strong>&nbsp;&nbsp;&nbsp;&nbsp;
            <a data-bind="click:add" type="button" class="btn btn-info">添加任务</a>
        </legend>
        <ul class="inline">
            <li>
                <label>分类&nbsp;
                    <select data-bind="options:categories,value:selectedCategory,optionsText:'title',optionsValue:'value',optionsCaption:'请选择分类'">
                    </select>
                </label>
            </li>
            <li>
                <button data-bind="click:query" type="button" id="filter" class="btn btn-primary">查 询</button>
            </li>
        </ul>
        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>ID</th>
                <th>名称</th>
                <th>标识</th>
                <th>用户类型</th>
                <th>类型</th>
                <th>奖励类型</th>
                <th>奖励</th>
                <th>显示策略</th>
                <th>是否需要领取</th>
                <th>领取后过期天数</th>
                <th>描述</th>
                <th>进度模板</th>
                <th>icon</th>
                <th>thumb</th>
                <th>业务详情链接</th>
                <th>邀请优惠券</th>
                <th>任务过期时间</th>
                <th>奖学金发放类型</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody data-bind="foreach:missions">
            <tr>
                <td data-bind="text:id"></td>
                <td data-bind="text:title"></td>
                <td data-bind="text:identification"></td>
                <td data-bind="text:userTypesTxt"></td>
                <td data-bind="text:typeTxt"></td>
                <td data-bind="text:rewardTypeTxt"></td>
                <td data-bind="text:reward"></td>
                <td data-bind="text:policiesTxt"></td>
                <td data-bind="text:needReceive"></td>
                <td data-bind="text:expireDays"></td>
                <td data-bind="text:desc"></td>
                <td data-bind="text:progress"></td>
                <td data-bind="text:icon"></td>
                <td data-bind="text:thumb"></td>
                <td data-bind="text:url"></td>
                <td data-bind="text:inviteeCoupon"></td>
                <td data-bind="text:expireDateTxt"></td>
                <td data-bind="text:financeSource"></td>
                <td>
                    <button data-bind="click:$parent.edit" type="button" class="btn btn-default">编辑</button>
                </td>
            </tr>
            </tbody>
        </table>
 </div>
    <script type="text/javascript">
        var MissionModel = function () {
            var self = this;

            self.categories = ko.observableArray();
            self.selectedCategory = ko.observable();
            self.missions = ko.observableArray();
            self.policies = ko.observableArray();
            self.types = ko.observableArray();
            self.rewardTypes = ko.observableArray();
            self.financeSources = ko.observableArray();
            self.userTypes = ko.observableArray([
                {
                    title: '家长',
                    value: 2
                },
                {
                    title: '学生',
                    value: 3
                }, {
                    title: '老师',
                    value: 1
                }
            ]);

            self.reload = function () {
                var slCategory = self.selectedCategory();

                self.categories.removeAll();
                self.policies.removeAll();
                self.types.removeAll();
                self.financeSources.removeAll();

                $.get('/opmanager/parentmission/mission/categories.vpage', function (data) {
                    if (data.success) {
                        var categories = data.categories;
                        for (var i = 0; i < categories.length; i++) {
                            var category = {
                                title: categories[i].title,
                                value: categories[i].value,
                                prefix: categories[i].prefix
                            };
                            self.categories.push(category);
                        }

                        var policies = data.policies;
                        for (var j = 0; j < policies.length; j++) {
                            var policy = {
                                title: policies[j].title,
                                value: policies[j].value
                            };
                            self.policies.push(policy);
                        }

                        var types = data.types;
                        for (var k = 0; k < types.length; k++) {
                            var type = {
                                title: types[k].title,
                                value: types[k].value
                            };
                            self.types.push(type);
                        }

                        var rewardTypes = data.rewardTypes;
                        for (var m = 0; m < rewardTypes.length; m++) {
                            var rewardType = {
                                title: rewardTypes[m].title,
                                value: rewardTypes[m].value
                            };
                            self.rewardTypes.push(rewardType);
                        }

                        var financeSources = data.financeSources;
                        for (var q = 0; q < financeSources.length; q++) {
                            var source = {
                                title: financeSources[q].title,
                                value: financeSources[q].value
                            };
                            self.financeSources.push(source);
                        }

                        if (slCategory !== undefined) {
                            self.selectedCategory(slCategory);
                            self.query();
                        }
                    } else {
                        alert(data.info);
                    }
                });
            };

            self.add = function () {
                window.open('/opmanager/parentmission/missions/edit.vpage');
            };

            self.edit = function (mission) {
                window.open('/opmanager/parentmission/missions/edit.vpage?id='+mission.id);
            };

            self.query = function () {
                if (self.selectedCategory() === undefined || self.selectedCategory().length === 0) {
                    alert('请选择分类');
                    return;
                }

                self.missions.removeAll();
                $.get('/opmanager/parentmission/mission/list.vpage', {cid: self.selectedCategory()}, function (data) {
                    if (data.success) {
                        var missions = data.missions;
                        for (var i = 0; i < missions.length; i++) {
                            var mission = missions[i];
                            var ut = '';
                            for (var j = 0; j < mission.userTypes.length; j++) {
                                for (var m = 0; m < self.userTypes().length; m++) {
                                    if (self.userTypes()[m].value === mission.userTypes[j]) {
                                        ut = ut + self.userTypes()[m].title + ",";
                                        break
                                    }
                                }
                            }
                            mission.userTypesTxt = ut;
                            for (var j = 0; j < self.types().length; j++) {
                                if (self.types()[j].value === mission.type) {
                                    mission.typeTxt = self.types()[j].title;
                                    break;
                                }
                            }
                            for (var j = 0; j < self.rewardTypes().length; j++) {
                                if (self.rewardTypes()[j].value === mission.rewardType) {
                                    mission.rewardTypeTxt = self.rewardTypes()[j].title;
                                    break;
                                }
                            }
                            var pt = '';
                            for (var j = 0; j < mission.policies.length; j++) {
                                for (var m = 0; m < self.policies().length; m++) {
                                    if (self.policies()[m].value === mission.policies[j]) {
                                        pt = pt + self.policies()[m].title + ",";
                                        break;
                                    }
                                }
                            }
                            mission.policiesTxt = pt;
                            mission.expireDateTxt = '';
                            if (mission.expireDate !== undefined && mission.expireDate !== null) {
                                mission.expireDateTxt = new Date(mission.expireDate).format('yyyy-MM-dd');
                            }
                            self.missions.push(mission);
                        }
                    } else {
                        alert(data.info);
                    }
                });
            };

            self.reload();
        };

        ko.applyBindings(new MissionModel());
    </script>
</@layout_default.page>
