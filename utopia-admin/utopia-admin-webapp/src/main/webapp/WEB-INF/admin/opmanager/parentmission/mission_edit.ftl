<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="运营活动管理" page_num=9>
    <script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/extends/Date.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-fileDownload/jquery.fileDownload.js"></script>

    <div id="main_container" class="span9">
        <legend>
            <strong data-bind="visible:isAdd">添加任务</strong>&nbsp;&nbsp;&nbsp;&nbsp;
            <strong data-bind="visible:isEdit">编辑任务</strong>
        </legend>
        <div class="modal-body">
            <label class="control-label" for="name">名称</label>
            <div class="controls">
                <input id="name" data-bind="value:title" class="span12"/>
            </div>
        </div>
        <div class="modal-body">
            <label class="control-label" for="identification">标识</label>
            <div class="controls">
                <span data-bind="text:identificationPrefix"></span>
                -<input id="identification" data-bind="value:identificationTail"
                        placeholder="业务方自定义，建议业务缩写+编号"/>
            </div>
        </div>
        <div class="modal-body">
            <label class="control-label" for="type">任务分类</label>
            <div class="controls">
                <select id="type"
                        data-bind="options:categories,optionsText:'title',optionsValue:'value',optionsCaption:'请选择任务类型',value:selectedCategory,event:{change:selectedCategoryChange()}"></select>
            </div>
        </div>
        <div class="modal-body">
            <label class="control-label" for="type">任务类型</label>
            <div class="controls">
                <select id="type"
                        data-bind="options:types,optionsText:'title',optionsValue:'value',optionsCaption:'请选择任务类型',value:selectedType"></select>
            </div>
        </div>
        <div class="modal-body">
            <label class="control-label" for="userTypes">用户类型</label>
            <div class="controls">
                <!-- ko foreach: userTypes -->
                <input type="checkbox"
                       data-bind="checkedValue: $data, checked: $root.selectedUserTypes"/>
                <span data-bind="text: title"></span>
                <!-- /ko -->
            </div>
        </div>
        <div class="modal-body">
            <label class="control-label" for="policies">显示策略</label>
            <div class="controls">
                <!-- ko foreach: policies -->
                <input type="checkbox"
                       data-bind="checkedValue: $data, checked: $root.selectedPolicies"/>
                <span data-bind="text: title"></span>
                <!-- /ko -->
            </div>
        </div>
        <div class="modal-body">
            <label class="control-label" for="type">任务奖励类型</label>
            <div class="controls">
                <select id="type"
                        data-bind="options:rewardTypes,optionsText:'title',optionsValue:'value',optionsCaption:'请选择任务类型',value:selectedRewardType"></select>
            </div>
        </div>
        <div class="modal-body">
            <label class="control-label" for="sort">奖励</label>
            <div class="controls">
                <input id="sort" data-bind="value:reward"/>
            </div>
        </div>
        <div class="modal-body">
            <label class="control-label" for="icon">icon</label>
            <div class="controls">
                <input id="icon" data-bind="value:icon" class="span12"/>
            </div>
        </div>
        <div class="modal-body">
            <label class="control-label" for="thumb">分享海报</label>
            <div class="controls">
                <input id="thumb" data-bind="value:thumb" class="span12"/>
            </div>
        </div>
        <div class="modal-body">
            <label class="control-label" for="url">业务详情链接</label>
            <div class="controls">
                <input id="url" data-bind="value:url" class="span12"/>
            </div>
        </div>
        <div class="modal-body">
            <label class="control-label" for="desc">描述</label>
            <div class="controls">
                <input id="desc" data-bind="value:desc" class="span12"/>
            </div>
        </div>
        <div class="modal-body">
            <label class="control-label" for="progress">进度模板</label>
            <div class="controls">
                <input id="progress" data-bind="value:progress" class="span12"/>
            </div>
        </div>
        <div class="modal-body">
            <label class="control-label" for="needReceive">是否需领取</label>
            <div class="controls">
                <input id="needReceive" type="checkbox" data-bind="checked:needReceive"/>
            </div>
        </div>
        <div class="modal-body">
            <label class="control-label" for="expireDays">领取后过期天数</label>
            <div class="controls">
                <input id="expireDays" data-bind="value:expireDays"/>
            </div>
        </div>
        <div class="modal-body">
            <label class="control-label" for="inviteeCoupon">被邀请人优惠券</label>
            <div class="controls">
                <input id="inviteeCoupon" data-bind="value:inviteeCoupon" class="span12"/>
            </div>
        </div>
        <div class="modal-body">
            <label class="control-label" for="expireDate">任务过期时间</label>
            <div class="controls">
                <input id="expireDate" data-bind="value:expireDate"/>
            </div>
        </div>
        <div class="modal-body">
            <label class="control-label" for="financeSources">奖学金发放类型</label>
            <div class="controls">
                <select data-bind="options:financeSources,optionsText:'title',optionsValue:'value',optionsCaption:'请选择奖学金发放类型',value:selectedFinanceSource"></select>
            </div>
        </div>
        <div class="modal-footer">
            <button data-bind="click:submit" type="button" class="btn btn-primary">提交</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">关 闭</button>
        </div>
    </div>
    <script type="text/javascript">
        var MissionEditModel = function () {
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

            self.id = ko.observable('${id!''}');
            self.isAdd = ko.computed(function () {
                return self.id() === undefined || self.id() === null || self.id().length === 0;
            });
            self.isEdit = ko.computed(function () {
                return !self.isAdd();
            });
            self.title = ko.observable();
            self.identificationTail = ko.observable();
            self.selectedType = ko.observable();
            self.selectedRewardType = ko.observable();
            self.selectedUserTypes = ko.observableArray();
            self.selectedPolicies = ko.observableArray();
            self.selectedFinanceSource = ko.observable();
            self.reward = ko.observable();
            self.icon = ko.observable();
            self.thumb = ko.observable();
            self.url = ko.observable();
            self.desc = ko.observable();
            self.progress = ko.observable();
            self.needReceive = ko.observable();
            self.expireDays = ko.observable();
            self.inviteeCoupon = ko.observable();
            self.expireDate = ko.observable();

            self.identificationPrefix = ko.computed(function () {
                if (self.selectedCategory() === undefined || self.selectedCategory() === null) {
                    return "";
                } else {
                    for (var i = 0; i < self.categories().length; i++) {
                        if (self.categories()[i].value === self.selectedCategory()) {
                            return self.categories()[i].prefix;
                        }
                    }
                }
            });

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

                        if (self.id() !== '') {
                            $.get('/opmanager/parentmission/mission/info.vpage?id=' + self.id(), function (data) {
                                if (data.success) {
                                    var mission = data.info;
                                    self.id(mission.id);
                                    self.title(mission.title);
                                    self.selectedCategory(mission.categoryId);
                                    self.identificationTail(mission.identification.substring(self.identificationPrefix().length + 1, mission.identification.length));
                                    self.selectedType(mission.type);
                                    self.selectedRewardType(mission.rewardType);
                                    self.reward(mission.reward);
                                    self.icon(mission.icon);
                                    self.thumb(mission.thumb);
                                    self.url(mission.url);
                                    self.desc(mission.desc);
                                    self.progress(mission.progress);
                                    self.needReceive(mission.needReceive);
                                    self.expireDays(mission.expireDays);
                                    self.inviteeCoupon(mission.inviteeCoupon);
                                    self.selectedFinanceSource(mission.financeSource);
                                    if (mission.expireDate === undefined || mission.expireDate === null) {
                                        self.expireDate('');
                                    } else {
                                        self.expireDate(new Date(mission.expireDate).format('yyyy-MM-dd'));
                                    }
                                    self.selectedUserTypes([]);
                                    self.selectedPolicies([]);
                                    if (mission.userTypes !== undefined && mission.userTypes !== null) {
                                        for (var i = 0; i < mission.userTypes.length; i++) {
                                            for (var j = 0; j < self.userTypes().length; j++) {
                                                if (mission.userTypes[i] === self.userTypes()[j].value) {
                                                    self.selectedUserTypes.push(self.userTypes()[j]);
                                                }
                                            }
                                        }
                                    }
                                    if (mission.policies !== undefined && mission.policies !== null) {
                                        for (var i = 0; i < mission.policies.length; i++) {
                                            for (var j = 0; j < self.policies().length; j++) {
                                                if (mission.policies[i] === self.policies()[j].value) {
                                                    self.selectedPolicies.push(self.policies()[j]);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    alert(data.info);
                                }
                            });
                        }
                    } else {
                        alert(data.info);
                    }
                });
            };

            self.submit = function () {
                if (self.title() === undefined || self.title().length === 0) {
                    alert('请输入名称');
                    return;
                }
                if (self.identificationTail() === undefined || self.identificationTail().length === 0) {
                    alert('请输入标识');
                    return;
                }
                if (self.selectedCategory() === undefined || self.selectedCategory().length === 0) {
                    alert('请选择分类');
                    return;
                }
                if (self.selectedType() === undefined || self.selectedType().length === 0) {
                    alert('请选择任务类型');
                    return;
                }
                if (self.selectedUserTypes() === undefined || self.selectedUserTypes().length === 0) {
                    alert('请选择用户类型');
                    return;
                }
                if (self.selectedPolicies() === undefined || self.selectedPolicies().length === 0) {
                    alert('请选择显示策略');
                    return;
                }
                if (self.selectedRewardType() === undefined || self.selectedRewardType().length === 0) {
                    alert('请选择奖励类型');
                    return;
                }
                if (self.reward() === undefined || self.reward().length === 0) {
                    alert('请输入奖励内容');
                    return;
                }
                if (self.selectedRewardType() === 'Scholarship' && (self.selectedFinanceSource() === undefined || self.selectedFinanceSource().length === 0)) {
                    alert('奖学金类型奖励必须选择奖学金发放类型');
                    return;
                }

                var uts = '';
                for (var i = 0; i < self.selectedUserTypes().length; i++) {
                    uts = uts + self.selectedUserTypes()[i].value + ",";
                }
                uts = uts.substr(0, uts.length - 1);
                var pcs = '';
                for (var i = 0; i < self.selectedPolicies().length; i++) {
                    pcs = pcs + self.selectedPolicies()[i].value + ",";
                }
                pcs = pcs.substr(0, pcs.length - 1);

                if (self.isAdd()) {
                    $.post('/opmanager/parentmission/mission/add.vpage', {
                        userTypes: uts,
                        identification: self.identificationPrefix() + "-" + self.identificationTail(),
                        categoryId: self.selectedCategory(),
                        type: self.selectedType(),
                        policies: pcs,
                        rewardType: self.selectedRewardType(),
                        reward: self.reward(),
                        title: self.title(),
                        desc: self.desc(),
                        progress: self.progress(),
                        url: self.url(),
                        icon: self.icon(),
                        thumb: self.thumb(),
                        expireDays: self.expireDays(),
                        inviteeCoupon: self.inviteeCoupon(),
                        needReceive: self.needReceive(),
                        financeSource: self.selectedFinanceSource(),
                        expireDate: self.expireDate()
                    }, function (data) {
                        if (data.success) {
                            alert('操作成功');
                            window.location.href = '/opmanager/parentmission/missions.vpage';
                        } else {
                            alert(data.info);
                        }
                    });
                } else {
                    if (self.id() === undefined || self.id().length === 0) {
                        alert('id未知');
                        return;
                    }
                    $.post('/opmanager/parentmission/mission/edit.vpage', {
                        id: self.id(),
                        userTypes: uts,
                        identification: self.identificationPrefix() + "-" + self.identificationTail(),
                        categoryId: self.selectedCategory(),
                        type: self.selectedType(),
                        policies: pcs,
                        rewardType: self.selectedRewardType(),
                        reward: self.reward(),
                        title: self.title(),
                        desc: self.desc(),
                        progress: self.progress(),
                        url: self.url(),
                        icon: self.icon(),
                        thumb: self.thumb(),
                        expireDays: self.expireDays(),
                        inviteeCoupon: self.inviteeCoupon(),
                        needReceive: self.needReceive(),
                        financeSource: self.selectedFinanceSource(),
                        expireDate: self.expireDate()
                    }, function (data) {
                        if (data.success) {
                            alert('操作成功');
                            $('#modal').modal('hide');

                            self.reload();
                        } else {
                            alert(data.info);
                        }
                    });
                }
            };

            self.selectedCategoryChange = function () {
            };

            $("#expireDate").datepicker({
                dateFormat: 'yy-mm-dd',  //日期格式，自己设置
                monthNames: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                monthNamesShort: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
                dayNamesMin: ["日", "一", "二", "三", "四", "五", "六"],
                defaultDate: new Date(),
                numberOfMonths: 1,
                changeMonth: false,
                changeYear: false,
                onSelect: function (selectedDate) {
                    self.expireDate(selectedDate);
                }
            });

            self.reload();
        };

        ko.applyBindings(new MissionEditModel());
    </script>
</@layout_default.page>