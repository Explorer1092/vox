<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>
<div class="span9">
    <fieldset>
        <legend>运营二维码列表
            <button class="btn btn-primary" data-bind="click:btnAddQRCodeClicked">添加二维码</button>
        </legend>
    </fieldset>

    <table class="table">
        <tr>
            <th>ID</th>
            <th>公众号类型</th>
            <th>标识</th>
            <th>内容</th>
            <th>链接</th>
            <th>是否登录</th>
            <th>打点操作名称</th>
            <th>操作</th>
        </tr>
        <tbody data-bind="foreach:messages">
        <tr>
            <td data-bind="text:id"></td>
            <td data-bind="text:typeName"></td>
            <td data-bind="text:identify"></td>
            <td data-bind="text:content"></td>
            <td data-bind="text:url"></td>
            <td data-bind="text:needLogin"></td>
            <td data-bind="text:operation"></td>
            <td>
                <button class="btn btn-default" data-bind="click:$parent.btnEditClicked">编辑</button>
                <button class="btn btn-default" data-bind="click:$parent.btnShowQRCodeClicked">下载二维码</button>
            </td>
        </tr>
        </tbody>
    </table>
    <div id="download" class="modal fade" tabindex="-1" role="dialog">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                            aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title">Modal title</h4>
                </div>
                <div class="modal-body">
                    <p data-bind="text:downloadText"></p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->
</div>
<script type="text/javascript">
    const MessageModel = function () {
        const self = this;

        self.types = ko.observableArray([
            {
                'name': '家长通',
                'value': 'PARENT'
            }, {
                'name': "一起学",
                'value': 'YIQIXUE'
            }
        ]);
        self.messages = ko.observableArray([]);
        self.downloadText = ko.observable('');

        self.reloadMessages = function () {
            self.messages.removeAll();

            $.get('/site/wechat/qrcode/messages/list.vpage', function (data) {
                if (data.success) {
                    if (data.messages !== undefined) {
                        for (var i = 0; i < data.messages.length; i++) {
                            const msg = data.messages[i];
                            for (var j = 0; j < self.types().length; j++) {
                                if (msg.type === self.types()[j].value) {
                                    msg["typeName"] = self.types()[j].name;
                                }
                            }
                            self.messages.push(msg);
                        }
                    }
                } else {
                    alert(data.info);
                }
            });
        };

        self.btnAddQRCodeClicked = function () {
            window.location.href = '/site/wechat/qrcode/message/add.vpage';
        };

        self.btnShowQRCodeClicked = function (message) {
            $.get('/site/wechat/qrcode/message/download.vpage?type=' + message.type + "&identify=" + message.identify, function (data) {
                if (data.success) {
                    if (data.url !== undefined) {
                        self.downloadText(data.url);
                    }
                    if (data.text !== undefined) {
                        self.downloadText(data.text);
                    }
                    $('#download').modal('show');
                } else {
                    alert(data.info);
                }
            });

        };
        self.btnEditClicked = function(message){
            window.location.href='/site/wechat/qrcode/message/add.vpage?type='+message.type+'&idf='+message.identify;
        };

        self.reloadMessages();
    };

    ko.applyBindings(new MessageModel());
</script>
</@layout_default.page>