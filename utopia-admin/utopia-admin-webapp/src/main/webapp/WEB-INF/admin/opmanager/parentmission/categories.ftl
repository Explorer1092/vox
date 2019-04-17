<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="运营活动管理" page_num=9>
    <script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/extends/Date.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-fileDownload/jquery.fileDownload.js"></script>
    <div id="main_container" class="span9">
        <legend>
            <strong>任务分类管理</strong>&nbsp;&nbsp;&nbsp;&nbsp;
            <a data-bind="click:addCategory" type="button" class="btn btn-info">添加分类</a>
        </legend>
        <table class="table table-striped table-bordered">
            <thead>
            <tr>
                <th>ID</th>
                <th>名称</th>
                <th>标识前缀</th>
                <th>描述</th>
                <th>类型</th>
                <th>标签</th>
                <th>排序码</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody data-bind="foreach:categories">
            <tr>
                <td data-bind="text:id"></td>
                <td data-bind="text:title"></td>
                <td data-bind="text:prefix"></td>
                <td data-bind="text:desc"></td>
                <td data-bind="text:tagTxt"></td>
                <td data-bind="text:label"></td>
                <td data-bind="text:sort"></td>
                <td>
                    <button data-bind="click:$parent.editCategory" type="button" class="btn btn-default">编辑</button>
                </td>
            </tr>
            </tbody>
        </table>

        <div id="modalCategory" class="modal fade hide">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <h4 data-bind="visible:isAdd" class="modal-title">添加分类</h4>
                        <h4 data-bind="visible:isEdit" class="modal-title">编辑分类</h4>
                    </div>
                    <div class="form-horizontal">
                        <div class="modal-body">
                            <label class="control-label" for="name">名称</label>
                            <div class="controls">
                                <input id="name" data-bind="value:name"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="prefix">标识前缀</label>
                            <div class="controls">
                                <input id="prefix" data-bind="value:prefix" />
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="desc">描述</label>
                            <div class="controls">
                                <input id="desc" data-bind="value:desc"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="label">标签</label>
                            <div class="controls">
                                <input id="label" data-bind="value:label"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="sort">排序</label>
                            <div class="controls">
                                <input id="sort" data-bind="value:sort"/>
                            </div>
                        </div>
                        <div class="modal-body">
                            <label class="control-label" for="type">类型</label>
                            <div class="controls">
                                <select id="type"
                                        data-bind="options:types,optionsText:'title',optionsValue:'name',optionsCaption:'选择类型',value:selectedType"></select>
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
        var CategoryModel = function () {
            var self = this;

            self.isAdd = ko.observable(true);
            self.isEdit = ko.observable(false);

            self.id = ko.observable();
            self.name = ko.observable();
            self.prefix = ko.observable();
            self.desc = ko.observable();
            self.label = ko.observable();
            self.sort = ko.observable();
            self.selectedType = ko.observable();

            self.categories = ko.observableArray();
            self.types = ko.observableArray();

            self.reloadCategories = function () {
                self.categories.removeAll();
                self.types.removeAll();

                $.get('/opmanager/parentmission/category/list.vpage', function (data) {
                    if (data.success) {
                        var tags = data.tags;
                        for (var j = 0; j < tags.length; j++) {
                            var type = {
                                name: tags[j].name,
                                title: tags[j].title
                            };
                            self.types.push(type);
                        }

                        var categories = data.categories;
                        for (var i = 0; i < categories.length; i++) {
                            var category = categories[i];
                            for (var j = 0; j < tags.length; j++) {
                                if (tags[j].name === category.tag) {
                                    category.tagTxt = tags[j].title;
                                    break;
                                }
                            }
                            self.categories.push(category);

                        }
                    } else {
                        alert(data.info);
                    }
                });
            };

            self.addCategory = function () {
                self.name('');
                self.prefix('');
                self.desc('');
                self.label('');
                self.sort('');
                self.selectedType('');
                self.isAdd(true);
                self.isEdit(false);

                $('#modalCategory').modal('show');
            };

            self.editCategory = function (category) {
                self.isEdit(true);
                self.isAdd(false);
                self.id(category.id);
                self.name(category.title);
                self.prefix(category.prefix);
                self.desc(category.desc);
                self.label(category.label);
                self.sort(category.sort);
                self.selectedType(category.tag);

                $('#modalCategory').modal('show');
            };

            self.submit = function () {
                if (self.name() === undefined || self.name().length === 0) {
                    alert('请输入分类名称');
                    return;
                }
                if(self.prefix() === undefined || self.prefix().length === 0){
                    alert('请输入标识前缀');
                    return;
                }
                if (self.desc() === undefined || self.desc().length === 0) {
                    alert('请输入分类描述');
                    return;
                }
                if (self.label() === undefined || self.label().length === 0) {
                    alert('请输入标签');
                    return;
                }
                if (self.selectedType() === undefined || self.selectedType().length === 0) {
                    alert('请选择类型');
                    return;
                }
                if (self.sort() === undefined || self.sort().length === 0) {
                    alert('请输入排序码');
                    return;
                }
                if (self.isAdd()) {
                    $.post('/opmanager/parentmission/category/add.vpage', {
                        name: self.name(),
                        prefix: self.prefix(),
                        desc: self.desc(),
                        label: self.label(),
                        type: self.selectedType(),
                        sort: self.sort()
                    }, function (data) {
                        if (data.success) {
                            $('#modalCategory').modal('hide');

                            alert('操作成功');
                            self.reloadCategories();
                        } else {
                            alert(data.info);
                        }
                    });
                } else {
                    if (self.id() === undefined || self.id().length === 0) {
                        alert('分类id不能为空');
                        return;
                    }
                    $.post('/opmanager/parentmission/category/edit.vpage', {
                        id: self.id(),
                        name: self.name(),
                        prefix: self.prefix(),
                        desc: self.desc(),
                        label: self.label(),
                        type: self.selectedType(),
                        sort: self.sort()
                    }, function (data) {
                        if (data.success) {
                            $('#modalCategory').modal('hide');

                            alert('操作成功');
                            self.reloadCategories();
                        } else {
                            alert(data.info);
                        }
                    });
                }
            };

            self.reloadCategories();
        };

        ko.applyBindings(new CategoryModel());
    </script>
</@layout_default.page>