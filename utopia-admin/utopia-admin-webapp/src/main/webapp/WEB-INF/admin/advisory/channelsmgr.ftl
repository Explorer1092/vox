<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-频道管理' page_num=13>

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
<script src="${requestContext.webAppContextPath}/public/js/moment.js"></script>

<div class="span9">
    <button class="btn btn-info" data-bind="click:newChannel">新建频道</button>
    <table class="table  table-bordered">
        <thead>
        <tr>
            <th>频道ID</th>
            <th>名称</th>
            <th>位置</th>
            <th>广告位ID</th>
            <th style="width:15%">覆盖标签</th>
            <th style="width:5%">状态</th>
            <th>描述（备注）</th>
            <th>更新时间</th>
            <th>操作人</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody data-bind="foreach:channels">
        <tr>
            <td data-bind="text:$data.channelId" style="width:7%"></td>
            <td><input data-bind="value:$data.name" class="input-small" maxlength="4" minlength="2"></td>
            <td><input data-bind="value:$data.rank" type="number" class="input-small" min="1"></td>
            <td><input data-bind="value:$data.adSlotId" type="number" class="input-small"></td>
            <td data-bind="text:$parent.genCommaedTagNames($data.tagIds())"></td>
            <td data-bind="text:$parent.parse_status($data.online()),style:{color:$data.online()?'green':'red'}"></td>
            <td><input data-bind="value:$data.description" class="input input-medium"></td>
            <td data-bind="text:$parent.moment_format($data.updateTime())" style="width:8%"></td>
            <td data-bind="text:$data.editor" style="width:10%"></td>
            <td style="width:20%">
                <button class="btn btn-warn btn-small" data-bind="click:$parent.upsertChannel">保存</button>
                <button class="btn btn-info btn-small" data-bind="click:$parent.showTagsModal,visible:$data.id()">选择标签</button>
                <button class="btn btn-info btn-small"
                        data-bind="click:$parent.toggleStatus,text:$parent.parse_toggle_text($data.online()),visible:$data.id()&&$data.tagIds()&&$data.tagIds().length>0">
                    上线
                </button>
                <button class="btn btn-danger btn-small" data-bind="click:$parent.deleteChannel" style="display: none">删除</button>
            </td>
        </tr>
        </tbody>
    </table>
</div>


<#--label tree modal-->
<div class="modal fade" id="labelTreeModal" tabindex="-1" role="dialog" aria-hidden="true"
     style="display: none">
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
                <button type="button" class="btn btn-info" data-dismiss="modal" data-bind="click:chooseTags">
                    选择完毕
                </button>
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
            </div>
        </div><!-- /.modal-content -->
    </div>
</div>

<script type="text/javascript">
    var tags =${tags!""};
    var tagMap = {};
    for (var j = 0; j < tags.length; j++) {
        tagMap[tags[j].id] = tags[j].tagName;
    }
    var tagTree =${tagTree!''};
    var tmp = [{"title": "Node 1", "key": "1"},
        {
            "title": "Folder 2", "key": "2", "folder": true, "children": [
            {"title": "Node 2.1", "key": "3"},
            {"title": "Node 2.2", "key": "4"}
        ]
        }
    ];
    tree = $('#labelFancyTree').fancytree({
        extensions: ["filter"],
        source: tagTree,
        checkbox: true,
        selectMode: 2
    });
    var tree = $("#labelFancyTree").fancytree("getTree");

    function ChannelViewModal(id, channelId, name, online, rank, description, tagIds, editor, createTime, updateTime, disabled) {
        this.id = ko.observable(id);
        this.channelId = ko.observable(channelId);
        this.name = ko.observable(name);
        this.online = ko.observable(online);
        this.rank = ko.observable(rank);
        this.description = ko.observable(description);
        this.tagIds = ko.observable(tagIds);
        this.editor = ko.observable(editor);
        this.createTime = ko.observable(createTime);
        this.updateTime = ko.observable(updateTime);
        this.disabled = ko.observable(disabled);
    }

    function ViewModel() {
        this.currentChannelIndex = ko.observable(-1);
        this.currentChooseTagId = ko.observable(0);
        this.channels = ko.observableArray();
        this.tags = ko.observableArray(tags);
        this.genCommaedTagNames = function (tagIds) {
            if (tagIds) {
                var tagNames = [];
                for (var k = 0; k < tagIds.length; k++) {
                    tagNames.push(tagMap[tagIds[k]]);
                }
                return tagNames.join(",");
            } else {
                return "";
            }

        };
        this.load = function () {
            $.post("loadchannels.vpage", {}, function (data) {
                console.info(data);
                this.channels.removeAll();
                var result = ko.mapping.fromJS(data.channels);
                console.info(result());
                console.info(result().length);
                for (var i = 0; i < result().length; i++) {
                    this.channels.push(result()[i]);
                }
//                ko.utils.arrayForEach((),function (item) {
//                    this.channels.push(item);
//                });
            }.bind(this))
        };
        this.showTagsModal = function (channel, event) {
            console.info("show modal,current channel is ");
            console.info(channel);
            this.currentChannelIndex(channel.channelId());
            tree.visit(function (node) {
                node.setSelected(false);
                node.setExpanded(false);
            });
            var tagIds = channel.tagIds() ? channel.tagIds() : [];
            console.info(tagIds);
            for (var i = 0; i < tagIds.length; i++) {
                console.info(tagIds[i]);
                var key = parseInt(tagIds[i]);
                var node = tree.getNodeByKey("" + key);
                console.info(node);
                if (node) {
                    node.setSelected(true);
                    // expand all the parent
                    var currentNode = node;
                    while (!currentNode.getParent().isRoot()) {
                        currentNode.getParent().setExpanded(true);
                        currentNode = currentNode.getParent();
                    }
                }
            }
            $("#labelTreeModal").modal("show");
        }.bind(this);
        this.toggleStatus = function (channel) {
            if (channel.online()) {
                // 下线流程
                if (!confirm("确定要下线该频道？")) {
                    return false;
                }
            } else {
                // 上线流程
                if (!confirm("确定要上线该频道？")) {
                    return false;
                }
            }
            channel.online(!channel.online());
            this.upsertChannel(channel);
        }.bind(this);
        this.parse_toggle_text = function (online) {
            if (online) {
                return "下线";
            } else {
                return "上线";
            }
        };
        this.chooseTags = function () {
            // show tags modal
            var nodes = tree.getSelectedNodes();
            var tagIds = [];
            $.map(nodes, function (node) {
                tagIds.push(parseInt(node.key));
            });
            console.info(this.currentChannelIndex());
            console.info(tagIds);
            ko.utils.arrayForEach(this.channels(), function (item) {
                if (item.channelId() == this.currentChannelIndex()) {
                    console.info("found it");
                    item.tagIds(tagIds);
                    // save this
                    this.upsertChannel(item);
                }
            }.bind(this));

        };
        this.moment_format = function (timestamp) {
            return moment(new Date(timestamp)).format("YYYY-MM-DD HH:mm:ss");
        };
        this.parse_status = function (status) {
            if (status) {
                return "在线";
            } else {
                return "离线";
            }
        };
        this.deleteChannel = function (channel) {
            if(!confirm("确认删除？")){
                return false;
            }
            channel.disabled = true;
            this.upsertChannel(channel);
        }.bind(this);
        this.newChannel = function () {
            this.channels.unshift(new ChannelViewModal(null, 0, "", false, 10000, "", [], "", "", "", false));
        };
        this.upsertChannel = function (channel) {
            console.info("channel is:");
            console.info(channel);
            // rank 不能小于1
            if(channel.rank()<1){
                alert("位置不能小于1");
                return false;
            }
            // rank 排重
            var dumplicated = false;
            ko.utils.arrayForEach(this.channels(), function (item) {
                if (item.rank() == channel.rank() && item.channelId() != channel.channelId()) {
                    dumplicated = true;
                }
            });
            if (dumplicated) {
                alert("位置重复，请检查");
                return false;
            }
            var channelStr = JSON.stringify(ko.toJS(channel));
            console.info(channelStr);
            $.post("upsertchannel.vpage", {channel: channelStr}, function (data) {
                console.info(data);
                if (data.success) {
                    alert("成功");
                    this.load();
                } else {
                    alert("失败:" + data.info);
                }
            }.bind(this))
        }.bind(this);
    }
    viewModel = new ViewModel();
    viewModel.load();
    ko.applyBindings(viewModel);
</script>
</@layout_default.page>