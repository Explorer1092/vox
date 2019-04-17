<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='展示用户标签树' page_num=20>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>
<script src="//cdn.17zuoye.com/public/plugin/jquery/jquery-1.7.1.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<!--/span-->
<div class="span9">
    <div>
        用户id:<input data-bind="value:userId">&nbsp;&nbsp;&nbsp;
        <button class="btn btn-info" data-bind="click:searchTags">查询标签</button>
    </div>
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
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
            </div>
        </div><!-- /.modal-content -->
    </div>
</div>
<script type="application/javascript">
    var labelTree =${labelTree!''};
    function ViewModel() {
        this.userId = ko.observable();
        // 所有的二级过滤器，二级过滤器中的都是或关系，二级过滤器之间是且关系，所有的二级过滤器组成一级过滤器
        // 大数据的标签树数据
        this.labelTree = ko.observable(labelTree);
        // 逗号分割标签
        this.commaJoinTags = function (tags) {
            return tags.join(',');
        };
        // 重新初始化标签树，伴有初选的标签选中态
        this.reinitLabelTree = function (chosenTags) {
            $('#labelFancyTree').fancytree({
                extensions: ["filter"],
                source: this.labelTree(),
                checkbox: true,
                selectMode: 2
            });
            var tree = $("#labelFancyTree").fancytree("getTree");
            // 选中标签，展开父标签树
            for (var i = 0; i < chosenTags.length; i++) {
                var node = tree.getNodeByKey(parseInt(chosenTags[i]));
                if (node) {
                    node.setSelected(true);
                    // expand all the parent
                    var currentNode = node;
                    while (!currentNode.getParent().isRoot()) {
                        currentNode.getParent().setExpanded();
                        currentNode = currentNode.getParent();
                    }
                }
            }
        }.bind(this);
        // 查询标签
        this.searchTags = function () {
            $.post("getusertagset.vpage", {userId: this.userId()}, function (data) {
                var labelSet = data.labelSet;
                // string to long
                var labelSetLong = [];
                for (var i = 0; i < labelSet.length; i++) {
                    labelSetLong.push(parseInt(labelSet[i]));
                }
                this.reinitLabelTree(labelSetLong);
                $("#labelTreeModal").modal("show");
            }.bind(this));
        }.bind(this)
    }
    var viewModel = new ViewModel();
    ko.applyBindings(viewModel);

</script>
</@layout_default.page>