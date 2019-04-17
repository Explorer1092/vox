<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-标签树管理' page_num=13>
<script src="//cdn.17zuoye.com/public/plugin/jquery/jquery-1.7.1.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.ui-contextmenu.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-2.18.0-all.js"></script>
<span class="span9">
    <div id="tagTree" class="sampletree"></div>
</span>
<script>
    CLIPBOARD = null;
    var tagTree =${tagTree!''};
    $(function () {
        $('#tagTree').fancytree({
            extensions: ["filter", "edit", "dnd"],
            source:${tagTree!},
            checkbox: true,
            selectMode: 2,
            dnd: {
                preventVoidMoves: true,
                preventRecursiveMoves: true,
                autoExpandMS: 400,
                dragStart: function (node, data) {
                    console.info("drag start");
                    return true;
                },
                dragEnter: function (node, data) {
                    // return ["before", "after"];
                    console.info("dragEnter");
                    return true;
                },
                dragDrop: function (node, data) {
                    data.otherNode.moveTo(node, data.hitMode);
                    console.info(data);
                    var toId = node.data.id;
                    var fromId = data.otherNode.data.id;
                    var toName = node.data.name;
                    var fromName = data.otherNode.data.name;
                    console.info("move tag " + fromName + " to " + toName);
                    moveTag(fromId, toId);
                }
            },
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
                        addTag(newTagName, parentId, data.node);
                    } else {
                        var newTagName = data.node.title;
                        var tagId = data.node.data.id;
                        console.info("rename " + tagId + " to " + newTagName);
                        renameTag(tagId, newTagName);
                    }
                }
            }
        }).on("nodeCommand",
                function (event, data) {
                    // Custom event handler that is triggered by keydown-handler and  context menu:
                    var refNode, moveMode, tree = $(this).fancytree("getTree"),
                            node = tree.getActiveNode();

                    switch (data.cmd) {
                        case "moveUp":
                            refNode = node.getPrevSibling();
                            if (refNode) {
                                node.moveTo(refNode, "before");
                                node.setActive();
                            }
                            break;
                        case "moveDown":
                            refNode = node.getNextSibling();
                            if (refNode) {
                                node.moveTo(refNode, "after");
                                node.setActive();
                            }
                            break;
                        case "indent":
                            refNode = node.getPrevSibling();
                            if (refNode) {
                                node.moveTo(refNode, "child");
                                refNode.setExpanded();
                                node.setActive();
                            }
                            break;
                        case "outdent":
                            if (!node.isTopLevel()) {
                                node.moveTo(node.getParent(), "after");
                                node.setActive();
                            }
                            break;
                        case "rename":
                            console.info("rename node");
                            node.editStart();
                            break;
                        case "remove":
                            var data = node.data;
                            console.info(data);
                            var tagId = data.id;
                            var name = data.name;
                            console.info("remove tag " + name + " with id " + tagId);
                            deleteTag(tagId);
                            refNode = node.getNextSibling() || node.getPrevSibling() || node.getParent();
                            node.remove();
                            if (refNode) {
                                refNode.setActive();
                            }
                            break;
                        case "addChild":
                            console.info("add child");
                            node.editCreateNode("child", "");
                            break;
                        case "addSibling":
                            console.info("add sibling");
                            node.editCreateNode("after", "");
                            break;
                        case "cut":
                            CLIPBOARD = {
                                mode: data.cmd,
                                data: node
                            };
                            break;
                        case "copy":
                            CLIPBOARD = {
                                mode: data.cmd,
                                data: node.toDict(function (n) {
                                    delete n.key;
                                })
                            };
                            break;
                        case "clear":
                            CLIPBOARD = null;
                            break;
                        case "paste":
                            if (CLIPBOARD.mode === "cut") {
                                // refNode = node.getPrevSibling();
                                CLIPBOARD.data.moveTo(node, "child");
                                CLIPBOARD.data.setActive();
                            } else if (CLIPBOARD.mode === "copy") {
                                node.addChildren(CLIPBOARD.data).setActive();
                            }
                            break;
                        default:
                            alert("Unhandled command: " + data.cmd);
                            return;
                    }

                    // }).on("click dblclick", function(e){
                    //   console.log( e, $.ui.fancytree.eventToString(e) );
                });
        $("#tagTree").contextmenu({
            delegate: "span.fancytree-node",
            menu: [
                {title: "Edit <kbd>[F2]</kbd>", cmd: "rename", uiIcon: "ui-icon-pencil"},
                {title: "New sibling <kbd>[Ctrl+N]</kbd>", cmd: "addSibling", uiIcon: "ui-icon-plus"},
                {title: "New child <kbd>[Ctrl+Shift+N]</kbd>", cmd: "addChild", uiIcon: "ui-icon-arrowreturn-1-e"}
            ],
            beforeOpen: function (event, ui) {
                var node = $.ui.fancytree.getNode(ui.target);
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
        function renameTag(tagId, newTagName) {
            $.post("renametag.vpage", {tagId: tagId, newTagName: newTagName}, function (data) {
                console.info(data);
                if (data.success) {
                    alert("更新成功");
                } else {
                    alert("更新失败");
                }
            })
        }

        function deleteTag(tagId) {
            $.post("deletetag.vpage", {id: tagId}, function (data) {
                console.info(data);
                if (data.success) {
                    alert("删除成功");
                } else {
                    alert("删除失败");
                }
            })
        }

        function addTag(tagName, parentId, node) {
            $.post("addtag.vpage", {name: tagName, parentId: parentId}, function (data) {
                console.info(data);
                if (data.success) {
                    node.data.id = data.tagId;
                    node.data.name = tagName;
                    alert("添加成功");
                } else {
                    alert("添加失败");
                }
            });
        }

        function moveTag(fromId, toId) {
            $.post("movetag.vpage", {fromId: fromId, toId: toId}, function (data) {
                console.info(data);
                // 挪动标签后强制刷新一下页面
                window.location.reload();
            })
        }

//        var tree = $("#tagTree").fancytree("getTree");
    });
</script>
</@layout_default.page>