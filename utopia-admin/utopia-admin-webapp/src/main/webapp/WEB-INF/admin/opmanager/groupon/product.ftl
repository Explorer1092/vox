<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="运营活动管理" page_num=9>
    <script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/extends/Date.js"></script>
    <div id="main_container" class="span9">
        <legend>
            <strong>团购产品管理</strong>&nbsp;&nbsp;&nbsp;&nbsp;
            <a data-bind="click:addProduct" type="button" class="btn btn-info">添加产品</a>
        </legend>
        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>ID</th>
                <th>业务标记</th>
                <th>名称</th>
                <th>总库存量</th>
                <th>当前库存里</th>
                <th>适用年级</th>
                <th>Icon</th>
                <th>封面图片</th>
                <th>创建时间</th>
                <th>最后更新</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody data-bind="foreach:products">
            <tr>
                <td data-bind="text:id"></td>
                <td data-bind="text:identifyId"></td>
                <td data-bind="text:name"></td>
                <td data-bind="text:totalCount"></td>
                <td data-bind="text:currentCount"></td>
                <td data-bind="text:levels"></td>
                <td data-bind="text:icon"></td>
                <td data-bind="text:cover"></td>
                <td data-bind="text:createDateStr"></td>
                <td data-bind="text:updateDateStr"></td>
                <td>
                    <#--<button data-bind="click:$parent.delete" type="button" class="btn btn-warning">删除-->
                    <#--</button>-->
                    <button data-bind="click:$parent.editProduct" type="button" class="btn btn-primary">编辑</button>
                </td>
            </tr>
            </tbody>
        </table>
        <div id="modalProduct" class="modal fade hide">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <h4 data-bind="visible:isAdd" class=" button btn-default">添加产品</h4>
                        <h4 data-bind="visible:isEdit" class="modal-title">编辑产品</h4>
                    </div>
                    <div class="form-horizontal">
                        <div class="modal-body">
                            <label class="control-label" for="name">产品名称</label>
                            <div class="controls">
                                <input type="text" data-bind="value:name" placeholder="产品名称"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="identifyId">业务方标识</label>
                            <div class="controls">
                                <input type="text" data-bind="value:identifyId" placeholder="必填"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="totalCount">总库存量</label>
                            <div class="controls">
                                <input type="text" data-bind="value:totalCount" placeholder="必填"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="clazzLevels">适用年级</label>
                            <div class="controls">
                                <input type="text" data-bind="enable:isAdd,value:clazzLevels" placeholder="多个年级用,分隔"/>
                                <p style="color:#dc3545!important;">注意！设置后不能修改</p>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="icon">Icon图片地址</label>
                            <div class="controls">
                                <input type="text" data-bind="value:icon" placeholder="必填"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="cover">封面图片地址</label>
                            <div class="controls">
                                <input type="text" data-bind="value:cover" placeholder="必填"/>
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
    </div>
    <script type="text/javascript">
        var ProductModel = function () {
            const self = this;

            self.id = ko.observable();
            self.identifyId = ko.observable();
            self.name = ko.observable();
            self.type = ko.observable();
            self.totalCount = ko.observable();
            self.currentCount = ko.observable();
            self.clazzLevels = ko.observable();
            self.icon = ko.observable();
            self.cover = ko.observable();
            self.isAdd = ko.observable(true);
            self.isEdit = ko.observable(false);

            self.products = ko.observableArray([]);

            var grouponId = '${grouponId}';

            self.reloadProducts = function () {
                self.products.removeAll();

                $.get('/opmanager/groupon/product/list.vpage?grouponId=' + grouponId, function (data) {
                    if (data.success) {
                        const products = data.products;
                        for (var i = 0; i < products.length; i++) {
                            var product = products[i];
                            var levels = product['clazzLevels'];
                            if (levels === null) {
                                product['levels'] = '--';
                            } else {
                                var lvlStr = '';
                                for (var j = 0; j < levels.length; j++) {
                                    lvlStr = lvlStr + levels[j] + ',';
                                }
                                if (lvlStr.length > 0) {
                                    lvlStr = lvlStr.substr(0, lvlStr.length - 1);
                                }
                                product['levels'] = lvlStr;
                            }
                            product['createDateStr'] = new Date(product.createDatetime).format('yyyy-MM-dd HH:mm:ss');
                            product['updateDateStr'] = new Date(product.updateDatetime).format('yyyy-MM-dd HH:mm:ss');
                            self.products.push(product);
                        }
                    } else {
                        //todo: error msg
                    }
                });
            };

            self.addProduct = function () {
                self.isAdd(true);
                self.isEdit(false);
                self.name('');
                self.id('');
                self.identifyId('');
                self.type('');
                self.totalCount('');
                self.clazzLevels('');
                self.icon('');
                self.cover('');

                $('#modalProduct').modal('show');
            };
            self.editProduct = function (product) {
                self.isAdd(false);
                self.isEdit(true);
                self.id(product.id);
                self.name(product.name);
                self.identifyId(product.identifyId);
                self.type(product.type);
                self.totalCount(product.totalCount);
                self.icon(product.icon);
                self.cover(product.cover);

                var levels = product.clazzLevels;
                if (levels === null) {
                    self.clazzLevels('');
                } else {
                    var lvlStr = '';
                    for (var j = 0; j < levels.length; j++) {
                        lvlStr = lvlStr + levels[j] + ',';
                    }
                    if (lvlStr.length > 0) {
                        lvlStr = lvlStr.substr(0, lvlStr.length - 1);
                    }
                    self.clazzLevels(lvlStr);
                }

                $('#modalProduct').modal('show');
            };
            self.submit = function () {
                if (self.name() === undefined || self.name().length === 0) {
                    alert('请输入产品名称');
                    return;
                }
                if (self.identifyId() === undefined || self.identifyId().length === 0) {
                    alert('请输入产品业务标识');
                    return;
                }
                if (self.totalCount() === undefined || self.totalCount().length === 0) {
                    alert('请输入总库存量');
                    return;
                }
                if (self.icon() === undefined || self.icon().length === 0) {
                    alert('请输入icon图片地址');
                    return;
                }

                if (self.isAdd()) {
                    //add product
                    $.post('/opmanager/groupon/product/add.vpage', {
                        grouponId: grouponId,
                        name: self.name(),
                        identifyId: self.identifyId(),
                        totalCount: self.totalCount(),
                        clazzLevels: self.clazzLevels(),
                        icon: self.icon(),
                        cover: self.cover()
                    }, function (data) {
                        if (data.success) {
                            alert('操作成功');
                            self.reloadProducts();
                            $('#modalProduct').modal('hide');
                        } else {
                            alert(data.info);
                        }
                    });
                } else {
                    //edit product
                    if (self.id() === undefined || self.id().length === 0) {
                        alert('无效的产品ID');
                        return;
                    }

                    $.post('/opmanager/groupon/product/update.vpage', {
                        id: self.id(),
                        name: self.name(),
                        identifyId: self.identifyId(),
                        totalCount: self.totalCount(),
                        icon: self.icon(),
                        cover: self.cover(),
                        levels: self.clazzLevels()
                    }, function (data) {
                        if (data.success) {
                            alert('操作成功');
                            self.reloadProducts();
                            $('#modalProduct').modal('hide');
                        } else {
                            alert(data.info);
                        }
                    });
                }
            };

            self.reloadProducts();
        };

        ko.applyBindings(new ProductModel());
    </script>
</@layout_default.page>