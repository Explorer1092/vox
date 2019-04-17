<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='资讯-文章筛选' page_num=13>
<style>
    .modal {
        background-color: inherit;
    !important;
    }

    .modal.fade.in {
        top: 1%
    }

    .device {
        background-image: url("/public/img/device-sprite.png");
        background-position: 0 0;
        background-repeat: no-repeat;
        background-size: 300% auto;
        display: block;
        font-family: "Helvetica Neue", sans-serif;
        height: 813px;
        position: relative;
        transition: background-image 0.1s linear 0s;
        width: 395px;
    }

    .device .device-content {
        background: #eeeeee none repeat scroll 0 0;
        font-size: 0.85rem;
        height: 569px;
        left: 37px;
        line-height: 1.05rem;
        overflow: hidden;
        position: absolute;
        top: 117px;
        width: 321px;
    }
</style>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>
<span class="span9">
    文章状态：<select id="filter_status" data-bind="value:filterStatus">
                <option value="waiting">待筛选</option>
                <option value="deleted">已删除</option>
                </select>
    一级分类：<select id="filter_first_level_tag"
                 data-bind="options:allTags,optionsText:'parentTagName',optionsValue:'parentTagId',optionsCaption:'全部',value:filterFirstLevelTag,event:{change:firstLevelTagSelected}"></select>
    二级分类：<select id="filter_second_level_tag"
                 data-bind="options:secondLevelTags,optionsText:'tagName',optionsValue:'id',optionsCaption:'全部',value:filterSecondLevelTag,event{change:goToFirstPageNo}"></select><br>
    文章源等级：<select id="filter_source_grade"
                  data-bind="options:sourceGrades,optionsText:'name',optionsValue:'value',optionsCaption:'全部',value:sourceGrade,event:{change:goToFirstPageNo"></select>
<#--文章源：<select id="filter_publisher"-->
<#--data-bind="options:gradedPublishers,optionsText:'newsSourceName',optionsValue:'newsSourceName',optionsCaption:'全部',value:filterPublisher,event:{change:goToFirstPageNo"></select>-->
    文章源：<input data-bind="value:filterPublisher">
    <br>
    原文发布时间：<input id="startTime" name="sendTime" type="text" class="input-xlarge" placeholder="最早"
                  data-bind="value:filterStartTime,event:{change:goToFirstPageNo}">至
    <input id="endTime" name="sendTime" type="text" class="input-xlarge" placeholder="最晚"
           data-bind="value:filterEndTime,event:{change:goToFirstPageNo">（时间段相差最好不要大于三天）<br>
    <span style="display:none;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;已编辑：<input type="checkbox" id=""
                                                                               style="display: none"
                                                                               data-bind="checked:filter_edited,event:{ change: editedChanged}"></span>
<#--文章标题：<input id="filter_article_title" name="articleTitle" type="text" class="input-xlarge" placeholder="可空"-->
<#--data-bind="value:filterArticleTitle,event:{change:goToFirstPageNo}">-->
    <span style="">
        <button data-bind="click:startFilter" class="btn btn-default">查询</button>&nbsp;&nbsp;
        <button data-bind="click:resetFilters" class="btn btn-default">重置查询条件</button>&nbsp;&nbsp;
        <button data-bind="click:deleteCurrentPageRawArticles" class="btn btn-danger">删除当前页文章</button>
        <select data-bind="options:newsTypes,optionsText:'text',optionsValue:'value',value:chooseBatchOnlineType">
        </select>
        <button class="btn btn-success" data-bind="click:batchOnline">批量发布选中文章</button>
    </span>

    <table class="table table-hover table-striped table-bordered">
        <thead>
        <tr>
            <th></th>
            <th>id</th>
            <th>title</th>
            <th>publisher</th>
            <th>push_time</th>
            <th>operation</th>
        </tr>
        </thead>
        <tbody data-bind="foreach:articles">
        <tr>
            <td><input type="checkbox" data-bind="checked:$parent.batchOnlineIds,value:$data.id"></td>
            <td data-bind="text:$data.id"></td>
            <td data-bind="text:$data.title"></td>
            <td data-bind="text:$data.publisher"></td>
            <td data-bind="text:$parent.generate_time_str($data.publish_datetime)"></td>
            <td>
                <a target="_blank" data-bind="attr: { href:$data.url }" class="btn btn-success">原文</a>
                &nbsp;
                <!-- ko ifnot: $data.edited_id -->
                <a target="_blank" data-bind="attr:{href:'/advisory/contentedit.vpage?rid='+$data.id}"
                   class="btn btn-success">编辑</a>
                <!-- /ko -->
                <!-- ko if: $data.edited_id -->
                <a target="_blank" data-bind="attr:{href:'/advisory/contentupdateedit.vpage?id='+$data.edited_id}"
                   class="btn btn-info">已编辑</a>
                <!-- /ko -->
                &nbsp;
                <!-- ko ifnot: $data.disabled -->
                <button class="btn btn-danger" data-bind="click:$parent.deleteRawArticle">删除</button>
                <!-- /ko -->
                <button class="btn btn-info" data-bind="click:$parent.showPreviewModal">预览</button>
            </td>
        </tr>
        </tbody>
    </table>
    <div id="loading"
         style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 100%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
        <p style="text-align: center;top: 30%;position: relative;">正在查询，请等待……</p>
    </div>
    <div class="message_page_list"></div>
    <ul class="pager" style="display: none">
        <li data-bind="visible:showPrevious"><a href="#" data-bind="click:previousPage">上一页</a></li>
        <li data-bind="visible:showNext"><a href="#" data-bind="click:nextPage">下一页</a></li>
        <li>当前第 <span data-bind="text:currentPage()+1"></span> 页</li>
    </ul>
    </div>
</span>

<!-- 模态框（Modal） -->
<div id="previewModal" class="modal hide fade" tabindex="-1" style="width: 430px;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-body" style="max-height: 900px; width: 400px;" id="previewBox">
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>

<script type="text/html" id="previewBox_tem">
    <div class="device" style="" id="layoutInDevice">
        <div class="device-content">
            <div id="iwindow">
                <iframe width="320" height="569" frameborder="0" src="<%=url%>"></iframe>
            </div>
        </div>
    </div>
</script>

<script>
    var mainSiteBaseUrl = "${mainSiteBaseUrl!}";
    var allTags = JSON.parse('${tags}');
    var tagsMap = {};

    $('#startTime').datetimepicker({
        format: 'yyyy-mm-dd hh:ii:ss'
    });
    $('#endTime').datetimepicker({
        format: 'yyyy-mm-dd hh:ii:ss'
    });

    function ArticlesViewModel() {
        this.batchOnlineIds = ko.observableArray();
        this.allTags = ko.mapping.fromJS(allTags);
        this.filterFirstLevelTag = ko.observable("");
        this.filterSecondLevelTag = ko.observable("");
        this.secondLevelTags = ko.computed(function () {
            var filterFirstLevelTag = this.filterFirstLevelTag();
            var allTags = this.allTags();
            var theTag = ko.utils.arrayFilter(allTags, function (item) {
                        return item.parentTagId() == filterFirstLevelTag;
                    }
            );
            if (theTag.length > 0) {
                return theTag[0].childList();
            } else {
                return [];
            }
            return false;
        }.bind(this));


        this.firstLevelTagSelected = function () {
        }.bind(this);
        this.targetTags = ko.computed(function () {
            return [];
        });
        this.filterStatus = ko.observable("waiting");
        this.filter_edited = ko.observable(false);
        this.editedChanged = function () {
            this.currentPage(0);
            this.loadArticles(this.currentPage());
        };
        this.filter_name = ko.observable("");
        this.allPublishers = ko.observableArray([]);
        this.publishers = ko.observableArray([]);
        this.currentPage = ko.observable(1);
        this.articles = ko.observableArray();
        this.filterPublisher = ko.observableArray();
        this.filterStartTime = ko.observable("");
        this.filterEndTime = ko.observable("");
        this.filterArticleTitle = ko.observable("");
        this.resetFilters = function () {
            console.info("reset all filters");
            this.filterStatus("waiting");
            this.filterFirstLevelTag("");
            this.filterSecondLevelTag("");
            this.sourceGrade("");
            this.filterPublisher("");
            this.filterStartTime("");
            this.filterEndTime("");
            this.filterArticleTitle("");
        };
        this.publisherChanged = function () {
            // 重新加载页面
            this.currentPage(0);
            this.loadArticles(this.currentPage());
        };
        this.showPrevious = ko.computed(function () {
            return this.currentPage() > 0;
        }, this);
        this.showNext = ko.computed(function () {
            return this.articles().length >= this.size;
        }, this);
        this.size = 30;
        this.sourceGrades = [{"name": "A", "value": "A"}, {"name": "B", "value": "B"}, {"name": "C", "value": "C"}];
        this.sourceGrade = ko.observable("");
        this.changeSourceGrade = function () {
            this.filterSecondLevelTag("");
        }.bind(this);
        this.gradedPublishers = ko.computed(function () {
            this.filterPublisher("");
            var sourceGrade = this.sourceGrade();
            var items = this.allPublishers();
            var gradedPublishers;
            if (!sourceGrade) {
                gradedPublishers = items;
            } else {
                gradedPublishers = ko.utils.arrayFilter(items, function (item) {
                    if (sourceGrade) {
                        if (item.sourceGrade != sourceGrade) {
                            return false;
                        }
                    }
                    return true;
                });
            }
            return gradedPublishers;
        }.bind(this));
        this.targetPublishers = ko.computed(function () {
            var sourceGrade = this.sourceGrade();
            var publisher = this.filterPublisher();
            if (!sourceGrade && !publisher) {
                return [];
            }
            var gradedPublishers = this.gradedPublishers();
            if (publisher) {
                return [publisher];
            } else {
                var publishers = ko.utils.arrayMap(gradedPublishers, function (item) {
                    return item.newsSourceName;
                });
                return publishers;
            }
        }.bind(this));
        this.startFilter = function () {
            this.currentPage(1);
            this.loadArticles(this.currentPage());
        }.bind(this);
        this.goToFirstPageNo = function () {
            this.currentPage(0);
        };
        this.load_all_publishers = function () {
            $.post("loadallnewssource.vpage", function (data) {
                this.allPublishers.remove();
                for (var i = 0; i < data.zyParentNewsSources.length; i++) {
                    this.allPublishers.push(data.zyParentNewsSources[i]);
                }
//                this.allPublishers = ko.mapping.fromJS(data.zyParentNewsSources);
            }.bind(this))
        }.bind(this);
        this.generate_time_str = function (timestamp) {
            var a = new Date(timestamp - 8 * 3600 * 1000);
            var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
            var year = a.getFullYear();
            var month = a.getMonth() + 1;
            var date = a.getDate();
            var hour = a.getHours();
            var min = a.getMinutes();
            var sec = a.getSeconds();
            var time = year + '-' + month + '-' + date + ' ' + hour + ':' + min + ':' + sec
            return time;
        };
        this.loadArticles = function (page) {
            $("#loading").show();
            var status = this.filterStatus();
            var disabled, edited;
            if (status == "waiting") {
                disabled = false;
                edited = false;
            } else {
                disabled = true;
                edited = false;
            }
            $.post("/advisory/loadrawarticles.vpage", {
                currentPage: page,
                SIZE: this.size,
                publishers: this.targetPublishers().join(','),
                edited: edited,
                startTime: this.filterStartTime(),
                endTime: this.filterEndTime(),
                firstLevelTagId: this.filterFirstLevelTag(),
                secondLevelTagId: this.filterSecondLevelTag(),
                disabled: disabled,
                title: this.filterArticleTitle()
            }, function (data) {
                $("#loading").hide();
                if (data.success) {
                    //console.info('got ' + data.articles.content.length + ' articles');
                    this.articles.removeAll();
                    for (var i = 0; i < data.articles.content.length; i++) {
                        // console.info("push article "+data.articles[i].id);
                        this.articles.push(data.articles.content[i]);
                    }
                    $(".message_page_list").page({
                        total: data.articles.totalPages,
                        current: data.articles.number + 1,
                        autoBackToTop: false,
                        jumpCallBack: function (index) {
                            this.currentPage(index);
                            this.loadArticles(this.currentPage());
                        }.bind(this)
                    });
                }
            }.bind(this))
        }.bind(this);
        this.nextPage = function () {
            this.currentPage(this.currentPage() + 1);
            this.loadArticles(this.currentPage());
        };
        this.previousPage = function () {
            this.currentPage(this.currentPage() - 1);
            if (this.currentPage() < 0) {
                this.currentPage(0);
            }
            this.loadArticles(this.currentPage());
        };
        this.init = function () {
            this.load_all_publishers();
            this.loadArticles(this.currentPage());
        };
        this.deleteRawArticle = function (rawArticle) {
            var id = rawArticle.id;
            var confirm = window.confirm("确定删除？");
            if (!confirm) {
                return false;
            }
            $.post('deleterawarticlebyid.vpage', {id: id}, function (data) {
                if (data.success) {
                    alert("删除成功");
                    this.loadArticles(this.currentPage());
                } else {
                    alert("删除失败");
                }
            }.bind(this))

        }.bind(this);
        this.showPreviewModal = function (article) {
            //预览
            var get_url_pre = function () {
                var pre = $('<a>', {href: '/'})[0].href,
                        env = /admin\.(\w+)\./.exec(pre)[1];

                return env === '17zuoye' ? 'http://www.17zuoye.com/' : 'http://www.' + env + '.17zuoye.net/';

            };
            $("#previewBox").html(template("previewBox_tem", {url: mainSiteBaseUrl + '/view/mobile/parent/information/preview?type=raw&id=' + article.id}));
            $("#previewModal").modal("show");
        };
        this.deleteCurrentPageRawArticles = function () {
            var articles = this.articles();
            var articleIds = ko.utils.arrayMap(articles, function (item) {
                return item.id;
            });
            var confirm = window.confirm("确定删除？");
            if (!confirm) {
                return false;
            }
            $.post('deleterawarticlebyids.vpage', {ids: articleIds.join(',')}, function (data) {
                if (data.success) {
                    alert("批量删除成功");
                    this.loadArticles(this.currentPage());
                } else {
                    alert("批量删除失败");
                }
            }.bind(this))

        }.bind(this);
        this.newsTypes=ko.observableArray([
            {"text":"资讯","value":'NEWS'},
            {"text":"同步教材","value":'SYNC_TEACHING_MATERIAL'},
            {"text":"第三方同步教材","value":'EXTERNAL_SYNC_TEACHING_MATERIAL'},
        ]);
        // 默认选中的是资讯
        this.chooseBatchOnlineType = ko.observable("NEWS");
        this.batchOnline = function () {
            // 批量上线文章
            var ids = this.batchOnlineIds();
            console.info(ids);
            if (!confirm("确定批量上线这些文章？")) {
                return false;
            }
            $.post("batchonlinerawarticles.vpage", {ids: ids.join(","),type:this.chooseBatchOnlineType()}, function (data) {
                if (data.success) {
                    this.batchOnlineIds.removeAll();
                    this.loadArticles();
                    alert("成功上线" + data.count + "篇文章!");
                } else {
                    alert("批量上线失败!");
                }
            }.bind(this))
        }.bind(this);
    }
    viewModel = new ArticlesViewModel();
    viewModel.init();
    ko.applyBindings(viewModel);

</script>
</@layout_default.page>