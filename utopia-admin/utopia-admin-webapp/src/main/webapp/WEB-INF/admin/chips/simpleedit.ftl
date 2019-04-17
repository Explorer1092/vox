<#-- @ftlvariable name="pageBlockContentList" type="java.util.List<com.voxlearning.utopia.service.config.api.entity.PageBlockContent>" -->
<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='简单页面内容配置管理' page_num=10>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>
<!--/span-->
<div class="span9">
    <h1>配置管理</h1>
    <div>
        <span>
            请选择配置项目：<select class="input-xlarge"
                            data-bind="options:pbcs,optionsText:'text',optionsValue:'value',value:$data.currentPbcId,event:{change:loadPbc}"></select>
        </span>
        <span>数据类型要求：<select disabled data-bind="value:currentPbcType"><option value="normal">字符串</option><option
                value="map">键值对</option></select></span>
        <button style="float:right" data-bind="click:addItem,visible:currentPbcId()!=0" class="btn btn-info">增加</button>
    </div>
    <div data-bind="visible:currentPbcId()!=0">
        <div>
            <table class="table table-hover table-striped table-bordered">
                <thead>
                <tr>
                    <th style="width:10%">值</th>
                    <th style="width:10%">操作</th>
                </tr>
                </thead>
                <tbody data-bind="foreach:currentPbc">
                <tr>
                    <td><input type="text" data-bind="value:$data.item"></td>
                    <td>
                    <#--<button class="btn btn-info" data-bind="click:$parent.updateItem">修改</button>-->
                        <button class="btn btn-danger" data-bind="click:$parent.deleteItem">删除</button>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <div style="float:right">

            <button data-bind="click:savePbc" class="btn btn-success">保存</button>
        </div>

    </div>


</div>
<script type="text/javascript">
    var isTest =${isTest?string};
    var initPbcs = [];
    if (isTest) {
        initPbcs = [
            {"text": "请选择需要配置的项目", "value": 0},
            {"text": "学生端不开启机器硬件加速配置", "value": 147, "type": "normal"},
            {"text": "关闭crosswalk引擎的配置", "value": 148, "type": "map"},
            {"text": "自学乐园关闭crosswalk引擎的配置", "value": 149, "type": "map"}
        ]
    } else {
        initPbcs = [
            {"text": "请选择需要配置的项目", "value": 0},
            {"text": "学生端不开启机器硬件加速配置", "value": 55, "type": "normal"},
            {"text": "关闭crosswalk引擎的配置", "value": 101, "type": "map"},
            {"text": "自学乐园关闭crosswalk引擎的配置", "value": 118, "type": "map"}
        ]
    }
    function ViewModel() {
        this.currentPbcId = ko.observable(0);
        this.currentPbc = ko.observableArray([]);
        this.pbcs = ko.observableArray(initPbcs);
        this.currentPbcType = ko.computed(function () {
            var id = this.currentPbcId();
            return ko.utils.arrayFirst(this.pbcs(), function (item) {
                console.info(item.value + ">" + id);
                return item.value == id;
            }).type;
        }.bind(this));
        this.loadPbc = function () {
            console.info("loadPbc");
            $.get("getpagecontentbyid.vpage", {id: this.currentPbcId()}, function (data) {
                console.info(data);
                this.currentPbc.removeAll();
                var pbcs = JSON.parse(data.pbc.content);
                for (var i = 0; i < pbcs.length; i++) {
                    this.currentPbc.push({item: pbcs[i]});
                }
//                this.currentPbc(JSON.parse(data.pbc.content));
            }.bind(this));
        }.bind(this);
        this.deleteItem = function (item) {
            this.currentPbc.remove(item);
        }.bind(this);
        this.updateItem = function (item, index) {
            console.info("update item");
            console.info(item);
            console.info(index);
        };

        this.addItem = function () {
            this.currentPbc.push({"item": ""});
        };
        this.savePbc = function () {
            if (!confirm("确认保存更改？")) {
                return false;
            }
            console.info("save pbc");
            var type = this.currentPbcType();
            var pbc = this.currentPbc();
            var formated = [];
            for (var j = 0; j < pbc.length; j++) {
                // 检验数据类型
                if (type == "map") {
                    if (pbc[j].item.indexOf(":") == -1) {
                        alert("数据类型必须是键值对:" + pbc[j].item);
                        return false;
                    }
                }
                formated.push(pbc[j].item);
            }
            console.info(formated);
            $.post("updatecontentbyid.vpage", {
                id: this.currentPbcId(),
                content: JSON.stringify(formated)
            }, function (data) {
                if (data.success) {
                    alert("保存成功");
                } else {
                    alert("保存失败");
                }
            })
        }
    }
    var viewModel = new ViewModel();
    ko.applyBindings(viewModel);
</script>
</@layout_default.page>