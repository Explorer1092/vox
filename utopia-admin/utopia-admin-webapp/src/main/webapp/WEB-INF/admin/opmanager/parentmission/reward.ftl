<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="运营活动管理" page_num=9>
    <script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/extends/Date.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-fileDownload/jquery.fileDownload.js"></script>

    <div id="main_container" class="span9">
        <legend>
            <strong>奖励信息查询</strong>&nbsp;&nbsp;&nbsp;&nbsp;
        </legend>
        <ul class="inline">
            <li>
                <label>用户ID&nbsp;
                    <input data-bind="value:userId"/>
                </label>
            </li>
            <li>
                <button data-bind="click:query" type="button" id="filter" class="btn btn-primary">查 询</button>
            </li>
        </ul>
        <legend>
            <strong>奖励数据总计</strong>&nbsp;&nbsp;&nbsp;&nbsp;
        </legend>
        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>ID</th>
                <th>奖励类型</th>
                <th>总数量</th>
                <th>完成次数</th>
            </tr>
            </thead>
            <tbody data-bind="foreach:rewards">
            <tr>
                <td data-bind="text:id"></td>
                <td data-bind="text:typeTxt"></td>
                <td data-bind="text:value"></td>
                <td data-bind="text:count"></td>
            </tr>
            </tbody>
        </table>
        <legend>
            <strong>奖励数据分类统计</strong>&nbsp;&nbsp;&nbsp;&nbsp;
        </legend>
        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>ID</th>
                <th>分类</th>
                <th>奖励类型</th>
                <th>总数量</th>
                <th>完成次数</th>
            </tr>
            </thead>
            <tbody data-bind="foreach:categoryRewards">
            <tr>
                <td data-bind="text:id"></td>
                <td data-bind="text:categoryTxt"></td>
                <td data-bind="text:typeTxt"></td>
                <td data-bind="text:value"></td>
                <td data-bind="text:count"></td>
            </tr>
            </tbody>
        </table>
    </div>
    <script type="text/javascript">
        var RewardModel = function () {
            var self = this;

            self.userId = ko.observable();
            self.rewards = ko.observableArray();
            self.categoryRewards = ko.observableArray();

            self.query = function () {
                if (self.userId() === undefined || self.userId().length === 0) {
                    alert('请输入用户ID');
                    return;
                }

                self.rewards.removeAll();
                self.categoryRewards.removeAll();

                $.get('/opmanager/parentmission/missions/reward/query.vpage?userId=' + self.userId(), function (data) {
                    if (data.success) {
                        var categories = data.categories;
                        var rewardTypes = data.rewardTypes;

                        var rewards = data.rewards;
                        for (var i = 0; i < rewards.length; i++) {
                            var reward = rewards[i];
                            for (var j = 0; j < rewardTypes.length; j++) {
                                if (rewardTypes[j].value === reward["type"]) {
                                    reward["typeTxt"] = rewardTypes[j].title;
                                    break
                                }
                            }
                            self.rewards.push(reward);
                        }

                        var categoryRewards = data.categoryRewards;
                        for (var i = 0; i < categoryRewards.length; i++) {
                            var catreward = categoryRewards[i];
                            for (var j = 0; j < rewardTypes.length; j++) {
                                if (rewardTypes[j].value === catreward["type"]) {
                                    catreward["typeTxt"] = rewardTypes[j].title;
                                    break;
                                }
                            }
                            for (var k = 0; k < categories.length; k++) {
                                if (categories[k].id === catreward["categoryId"]) {
                                    catreward["categoryTxt"] = categories[k].title;
                                }
                            }
                            self.categoryRewards.push(catreward);
                        }
                    } else {
                        alert(data.info);
                    }
                });
            };
        };

        ko.applyBindings(new RewardModel());
    </script>
</@layout_default.page>