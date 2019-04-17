<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='选择标签' page_num=20>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>
<script src="//cdn.17zuoye.com/public/plugin/jquery/jquery-1.7.1.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-2.18.0-all.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.ui-contextmenu.min.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<#--<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>-->
<#--<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>-->
<!--/span-->
<style type="text/css">
    span.fancytree-node.or > span.fancytree-title {
        color: maroon;
        font-family: "Audiowide";
    }
    span.fancytree-node.or > span.fancytree-icon {
        background-position: 0 0;
        background-position: 0px -112px;
    }
</style>
<div class="span9">
    <div class="hero-unit">
        <h1>请为实验(${id!""})选择标签</h1>
    </div>
    <button class="btn btn-info" data-bind="click:saveTagsFilters" style="float:right">保存</button>
    <button class="btn btn-info" data-bind="click:addOrFilter" style="float:right">增加条件</button>
    <table class="table table-hover table-striped table-bordered">
        <thead>
        <tr>
            <th>序号</th>
            <th>已选标签</th>
            <th>配置</th>
        </tr>
        </thead>
        <tbody data-bind="foreach:tagsFilters">
        <tr>
            <td data-bind="text:$index()+1"></td>
            <td><textarea data-bind="value:$parent.commaJoinTags($data)" readonly></textarea></td>
            <td>
                <button class="btn btn-info"
                        data-bind="click:function(data,event){$parent.showLabelTreeModal($index(),data)}">选择标签
                </button>
                <button class="btn btn-danger" data-bind="click:function(data,event){$parent.deleteFilter($index)}">
                    删除此条件
                </button>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<div id="loadingDiv"
     style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 100%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">正在查询，请等待……</p>
</div>

<#--label tree modal-->
<div class="modal fade" id="labelTreeModal" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="myModalLabel">选择标签</h4>
            </div>
            <div class="modal-body">
                <div id="labelFancyTree"></div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-info" data-dismiss="modal" data-bind="click:saveOrFilterTags">
                    选择完毕
                </button>
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
            </div>
        </div><!-- /.modal-content -->
    </div>
</div>

<script type="application/javascript">
    CLIPBOARD = null;
    var tagsFilters = ${tagsFilters![]};
    var labelTree =${labelTree!''};
    var id = "${id}";

    $('#labelFancyTree').fancytree({
        extensions: ["edit"],
        source: labelTree,
        checkbox: true,
        selectMode: 2,
        edit: {
            triggerStart: ["f2", "shift+click", "mac+enter"],
            close: function (event, data) {
                console.info(event);
                console.info(data);
                if (data.save && data.isNew) {
                    // Quick-enter: add new nodes until we hit [enter] on an empty title
                    var currentNode = data.node;
                    var parentNode = data.node.parent;
                    var newTagName = currentNode.title;
                    var parentId = parentNode.data.id;
                    var parentName = parentNode.data.name;
                    console.info("add new tag " + newTagName + " to parent " + parentName);
                    // trick,更改这个node data中的id，实现和后端库同步
                } else {
                    var newTagName = data.node.title;
                    var tagId = data.node.data.id;
                    console.info("rename " + tagId + " to " + newTagName);
                }
            }
        }
    });


    function TagsViewModel() {
        // 实验id
        this.experimentId = ko.observable(id);
        // 所有的二级过滤器，二级过滤器中的都是或关系，二级过滤器之间是且关系，所有的二级过滤器组成一级过滤器
        this.tagsFilters = ko.observableArray();
        // 当前正在选择标签的过滤器序号
        this.currentFilterIndex = ko.observable(0);
        // 大数据的标签树数据
        this.labelTree = ko.observable(labelTree);
        // 点击选择标签，显示选择标签树的modal
        this.showLabelTreeModal = function (index, orFilterTags) {
            console.info("showLabelTreeModal");
            console.info(index);
            console.info(orFilterTags);
            this.currentFilterIndex(index);
            this.reinitLabelTree(orFilterTags);
            $("#labelTreeModal").modal("show");
        }.bind(this);
        // 增加或过滤器
        this.addOrFilter = function () {
            this.tagsFilters.unshift(ko.observable([]));
        };
        // 逗号分割标签
        this.commaJoinTags = function (tags) {
            console.info(tags);
            return tags.join(',');
        };
        // 重新初始化标签树，伴有初选的标签选中态
        this.reinitLabelTree = function (chosenTags) {
            console.info("reinitLabelTree");
            var tree = $("#labelFancyTree").fancytree("getTree");
            tree.reload(labelTree);
            // 选中标签，展开父标签树
            $("#labelFancyTree").contextmenu({
                delegate: "span.fancytree-node",
                menu: [
                    {title: "改变是非", cmd: "changeLogic", uiIcon: "ui-icon-pencil"}
                ],
                beforeOpen: function (event, ui) {
                    console.info("what!clicked");
                    var node = $.ui.fancytree.getNode(ui.target);
                    node.extraClasses = node.extraClasses=="or"?"":"or";
                    node.renderTitle();
                    $("#tree").contextmenu("enableEntry", "paste", !!CLIPBOARD);
                    node.setActive();
                },
                select: function (event, ui) {
                    var that = this;
                    // delay the event, so the menu can close and the click event does
                    // not interfere with the edit control
                    setTimeout(function () {
                        $(that).trigger("nodeCommand", {cmd: ui.cmd});
                    }, 100);
                }
            });
            for (var i = 0; i < chosenTags.length; i++) {
                console.info(chosenTags[i]);
                var key=parseInt(chosenTags[i]);
                var node = tree.getNodeByKey(""+Math.abs(key));
                if (node) {
                    node.setSelected(true);
                    if(key<0){
                        node.extraClasses="or";
                        node.renderTitle();
                    }
                    // expand all the parent
                    var currentNode = node;
                    while (!currentNode.getParent().isRoot()) {
                        currentNode.getParent().setExpanded();
                        currentNode = currentNode.getParent();
                    }
                }
            }
        }.bind(this);
        // 删除或标签条件
        this.deleteFilter = function (index) {
            this.tagsFilters.splice(index, 1);
        };
        // 使用树上选中的标签
        this.saveOrFilterTags = function () {
            var tagList = [];
            var tree = $("#labelFancyTree").fancytree("getTree");
            var nodes = tree.getSelectedNodes();
            $.map(nodes, function (node) {
                var b=node.extraClasses=="or"?false:true;
                if(b){
                    tagList.push(parseInt(node.key));
                }else{
                    tagList.push(-node.key);
                }

            });
            console.info("change filter " + this.currentFilterIndex() + " to " + tagList.join(','));
//            this.tagsFilters[replace(this.currentFilterIndex(), tagList);
            var currentFilterIndex = this.currentFilterIndex();
            console.info(this.tagsFilters()[currentFilterIndex]);
            this.tagsFilters()[currentFilterIndex](tagList);
//            this.tagsFilters.unshittagList;
        }.bind(this);
        this.saveTagsFilters = function () {
            var tagsFilters = ko.mapping.toJS(this.tagsFilters());
            console.info("tagsFilters is " + JSON.stringify(tagsFilters));
            $.post("savetags.vpage", {tagsFilters: JSON.stringify(tagsFilters), id: id}, function (data) {
                console.info(data);
                window.location.reload();
            })
        };
        this.init = function () {
            for (var i = 0; i < tagsFilters.length; i++) {
                this.tagsFilters.push(ko.observable(tagsFilters[i]));
            }
        }
    }
    var viewModel = new TagsViewModel();
    viewModel.init();
    ko.applyBindings(viewModel);

</script>
</@layout_default.page>