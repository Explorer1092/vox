<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>
<div id="main_container" class="span9">
    <fieldset>
        <legend>添加二维码消息
        </legend>
    </fieldset>
    <div class="row-fluid">
        <form class="form-horizontal">
            <div class="control-group">
                <label for="type" class="control-label col-sm-4">公众号类型</label>
                <div class="controls">
                    <select id="type" class="col-sm-8"
                            data-bind="enable:typeEnable,options:types,optionsText:'name',value:selectedType,optionsCaption:'选择公众号类型'"></select>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label"></label>
                <div class="controls">
                    <input class="form-control " type="checkbox" data-bind="enable:limitEnable,checked:limit"/> 永久二维码
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">场景ID</label>
                <div class="controls">
                    <input data-bind="enable:sceneEnable,value:sceneId" type="text" class="form-control" id="sceneId"
                           placeholder="场景ID,不填场景ID就是字符型二维码">永久二维码1-100000，临时二维码100001-Integer.MAX_VALUE
                </div>
            </div>
            <div class="control-group">
                <label for="content" class="control-label col-sm-4">消息内容</label>
                <div class="controls">
                    <textarea data-bind="value:content" style="height:200px;" class="col-sm-8 span5"></textarea>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="url">跳转链接</label>
                <div class="controls">
                    <input data-bind="value:url" type="text" class="form-control span9" id="url" placeholder="跳转链接">
                </div>
            </div>
            <div class="checkbox control-group">
                <label class="control-label"></label>
                <div class="controls">
                    <input class="form-control " type="checkbox" data-bind="checked:needLogin"/> 跳转链接需要登录
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="operation">打点操作名称</label>
                <div class="controls">
                    <input data-bind="value:operation" type="text" class="form-control span9" id="operation"
                           placeholder="打点操作名称">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label"></label>
                <div class="controls">
                    <button class="btn btn-primary" data-bind="click:btnAddClicked">提交</button>
                </div>
            </div>
        </form>
    </div>
</div>
    <script type="text/javascript">
        const AddQRCodeMessageModel = function () {
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
            self.selectedType = ko.observable();
            self.limit = ko.observable(false);
            self.sceneId = ko.observable();
            self.content = ko.observable();
            self.url = ko.observable();
            self.needLogin = ko.observable(false);
            self.operation = ko.observable();
            self.typeEnable = ko.observable(true);
            self.limitEnable = ko.observable(true);
            self.sceneEnable = ko.observable(true);

            self.editId = '${id!''}';
            self.editType = '${type!''}';
            self.editIdentify = '${identify!''}';

            self.btnAddClicked = function () {
                if (self.selectedType() === undefined) {
                    alert('请选择公众号类型');
                    return;
                }
                if (self.content() === undefined) {
                    alert('请输入消息内容');
                    return;
                }
                if (self.operation() === undefined) {
                    alert('请输入打点操作名称');
                    return;
                }

                if (self.editType === '') {
                    $.post('/site/wechat/qrcode/message/add.vpage', {
                        type: self.selectedType().value,
                        limit: self.limit(),
                        sceneId: self.sceneId(),
                        content: self.content(),
                        url: self.url(),
                        needLogin: self.needLogin(),
                        operation: self.operation()
                    }, function (data) {
                        if (data.success) {
                            alert('添加成功');
                            window.location.href = '/site/wechat/qrcode/messages.vpage';
                        } else {
                            alert(data.info);
                        }
                    });
                } else {
                    $.post('/site/wechat/qrcode/message/update.vpage', {
                        id: self.editId,
                        type: self.editType,
                        identify: self.editIdentify,
                        content: self.content(),
                        url: self.url(),
                        needLogin: self.needLogin(),
                        operation: self.operation()
                    }, function (data) {
                        if (data.success) {
                            alert('更新成功');
                            window.location.href = '/site/wechat/qrcode/messages.vpage';
                        } else {
                            alert(data.info);
                        }
                    });
                }
            };

            self.init = function () {
                if (self.editType !== '') {
                    self.typeEnable(false);
                    self.limitEnable(false);
                    self.sceneEnable(false);

                    $.get('/site/wechat/qrcode/message/get.vpage?type=' + self.editType + '&idf=' + self.editIdentify, function (data) {
                        if (data.success) {
                            const message = data.message;
                            for (var i = 0; i < self.types().length; i++) {
                                if (message.type === self.types()[i].value) {
                                    self.selectedType(self.types()[i]);
                                    break
                                }
                            }
                            self.limit(data.limit);
                            if (data.sceneId !== 0) {
                                self.sceneId(data.sceneId);
                            }
                            self.content(message.content);
                            self.url(message.url);
                            self.needLogin(message.needLogin);
                            self.operation(message.operation);
                        } else {
                            alert(data.info);
                        }
                    });
                }
            };

            self.init();
        };

        ko.applyBindings(new AddQRCodeMessageModel());
    </script>
</@layout_default.page>