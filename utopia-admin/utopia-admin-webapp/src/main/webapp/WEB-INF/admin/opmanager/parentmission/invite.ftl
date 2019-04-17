<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="运营活动管理" page_num=9>
    <script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/extends/Date.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-fileDownload/jquery.fileDownload.js"></script>

    <div id="main_container" class="span9">
        <legend>
            <strong>邀请信息查询</strong>&nbsp;&nbsp;&nbsp;&nbsp;
        </legend>
        <ul class="inline">
            <li>
                <label>邀请人&nbsp;
                    <input data-bind="value:inviter"/>
                </label>
            </li>
            <li>
                <button data-bind="click:inviterQuery" type="button" id="filter" class="btn btn-primary">查 询</button>
            </li>
            <li>
                <label>被邀请人&nbsp;
                    <input data-bind="value:invitee"/>
                </label>
            </li>
            <li>
                <button data-bind="click:inviteeQuery" type="button" id="filter" class="btn btn-primary">查 询</button>
            </li
        </ul>
        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>ID</th>
                <th>邀请人</th>
                <th>被邀请人</th>
                <th>任务ID</th>
                <th>任务名称</th>
                <th>任务标识</th>
                <th>是否完成</th>
                <th>创建时间</th>
                <th>更新时间</th>
            </tr>
            </thead>
            <tbody data-bind="foreach:invites">
            <tr>
                <td data-bind="text:id"></td>
                <td data-bind="text:inviter"></td>
                <td data-bind="text:invitee"></td>
                <td data-bind="text:missionId"></td>
                <td data-bind="text:missionName"></td>
                <td data-bind="text:identification"></td>
                <td data-bind="text:finish"></td>
                <td data-bind="text:ct"></td>
                <td data-bind="text:ut"></td>
            </tr>
            </tbody>
        </table>
    </div>
    <script type="text/javascript">
        var InviteModel = function () {
            var self = this;

            self.inviter = ko.observable();
            self.invitee = ko.observable();
            self.invites = ko.observableArray();

            self.inviterQuery = function () {
                if (self.inviter() === undefined || self.inviter().length === 0) {
                    alert('请输入邀请人id');
                    return;
                }

                self.invites.removeAll();
                $.get('/opmanager/parentmission/mission/inviter/search.vpage?inviter=' + self.inviter(), function (data) {
                    if (data.success) {
                        for (var i = 0; i < data.refs.length; i++) {
                            var ref = data.refs[i];
                            ref["ct"] = new Date(ref.createDatetime).format('yyyy-MM-dd HH:mm:ss');
                            ref["finish"] = ref.finish == null ? 'false' : ref.finish;

                            ref["ut"] = new Date(ref.updateDatetime).format('yyyy-MM-dd HH:mm:ss');
                            for (var j = 0; j < data.missions.length; j++) {
                                if (data.missions[j].id === ref.missionId) {
                                    ref["missionName"] = data.missions[j].title;
                                    ref["identification"] = data.missions[j].identification;
                                    break;
                                }
                            }

                            self.invites.push(ref);
                        }
                    } else {
                        alert(data.info);
                    }
                });
            };

            self.inviteeQuery = function () {
                if (self.invitee() === undefined || self.invitee().length === 0) {
                    alert('请输入被邀请人id');
                    return;
                }

                self.invites.removeAll();
                $.get('/opmanager/parentmission/mission/invitee/search.vpage?invitee=' + self.invitee(), function (data) {
                    if (data.success) {
                        for (var i = 0; i < data.refs.length; i++) {
                            var ref = data.refs[i];
                            ref["ct"] = new Date(ref.createDatetime).format('yyyy-MM-dd HH:mm:ss');
                            ref["ut"] = new Date(ref.updateDatetime).format('yyyy-MM-dd HH:mm:ss');
                            ref["finish"] = ref.finish == null ? 'false' : ref.finish;

                            for (var j = 0; j < data.missions.length; j++) {
                                if (data.missions[j].id === ref.missionId) {
                                    ref["missionName"] = data.missions[j].title;
                                    ref["identification"] = data.missions[j].identification;
                                    break;
                                }
                            }

                            self.invites.push(ref);
                        }
                    } else {
                        alert(data.info);
                    }
                });
            };
        };

        ko.applyBindings(new InviteModel());
    </script>
</@layout_default.page>