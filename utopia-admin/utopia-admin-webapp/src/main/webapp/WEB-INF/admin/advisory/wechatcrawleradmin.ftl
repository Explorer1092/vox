<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='微信公众号抓取配置' page_num=13>
<div class="span9">
    <button class="btn btn-success" data-bind="click:add">添加+</button>
    &nbsp;&nbsp;
    <button class="btn btn-success" data-bind="click:add">抓取全部</button>
    <table class="table table-hover table-striped table-bordered">
        <thead>
        <tr>
            <th>序号</th>
            <th>公众号名称</th>
            <th>biz</th>
            <th>uin</th>
            <th>key</th>
            <th>状态</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody data-bind="foreach:crawlers">
        <tr>
            <td data-bind="text:$data.id"></td>
            <td><input type="text" data-bind="value:$data.name"></td>
            <td><input type="text" data-bind="value:$data.biz"></td>
            <td><input type="text" data-bind="value:$data.uin"></td>
            <td><input type="text" data-bind="value:$data.key"></td>
            <td data-bind="text:$data.running_text"></td>
            <td>
                <button class="btn btn-danger" data-bind="click:upsert">
                    保存
                </button>
                <!-- ko if:$data.id -->
                <button class="btn btn-danger" data-bind="visible:!$data.running(),click:$data.startCrawlNow">
                    抓取
                </button>
                <button class="btn btn-danger" data-bind="visible:$data.running(),click:$data.stopCrawlNow">
                    停止
                </button>
                <!-- /ko -->
            </td>
        </tr>
        </tbody>
    </table>
    <div class="message_page_list"></div>
</div>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script type="text/javascript" src="http://ajax.aspnetcdn.com/ajax/knockout/knockout-3.3.0.js"></script>
<script>
    // view model
    function CrawlerViewModel(id, name, biz, uin, key, key_valid, running, error, disabled, last_crawled_msg_id) {
        this.id = ko.observable(id);
        this.name = ko.observable(name);
        this.biz = ko.observable(biz);
        this.uin = ko.observable(uin);
        this.key = ko.observable(key);
        this.key_valid = ko.observable(key_valid);
        this.running = ko.observable(running);
        this.running_text = ko.computed(function () {
            if (this.running()) {
                return "正在运行";
            } else {
                return "未运行";
            }
        }.bind(this));
        this.error = ko.observable(error);
        this.disabled = ko.observable(disabled);
        this.last_crawled_msg_id = ko.observable(last_crawled_msg_id);
        this.upsert = function (crawler) {
            var crawlerJson = ko.toJSON(crawler);
            console.info(crawlerJson);
            $.post("upsertwechatcrawler.vpage", JSON.parse(crawlerJson), function (data) {
                console.info(data);
                if (data.success) {
                    alert("成功");
                    viewModel.loadCrawlers();
                } else {
                    alert("更新失败");
                }
            })
        }.bind(this);
        this.startCrawlNow = function (crawler) {
            // 开始抓取，通过http请求来完成
            console.info("start crawl now," + crawler.id());
            $.post("startwechatsogoucrawler.vpage", {_id: this.id(), type: 'wechat_token'}, function (data) {
                console.info(data);
                if (data.success) {
                    alert("启动成功");
                    viewModel.loadCrawlers();
                } else {
                    alert("启动失败");
                }
            });
        }.bind(this);
        this.stopCrawlNow = function (crawler) {
            // 停止抓取，这里是一个假的抓取其实
            console.info("stop crawl now," + crawler.id());
            $.post("stopwechatsogoucrawler.vpage", {_id: this.id(), type: 'wechat_token'}, function (data) {
                console.info(data);
                if (data.success) {
                    alert("停止成功");
                    viewModel.loadCrawlers();
                } else {
                    alert("失败失败");
                }
            });
        }.bind(this);
    }
    function CrawlersViewModel() {
        // 公众号列表
        this.crawlers = ko.observableArray([]);
        // 当前页
        this.currentPage = ko.observable();
        this.loadCrawlers = function () {
            console.info('load page ' + this.currentPage());
            $.post("/advisory/loadwechatcrawlers.vpage", {
                currentPage: this.currentPage(),
                size: 10
            }, function (data) {
                console.info(data);
                if (data.success) {
                    console.info('got ' + data.crawlers.content.length + ' crawlers');
                    this.crawlers.removeAll();
                    for (var i = 0; i < data.crawlers.content.length; i++) {
                        var crawler = data.crawlers.content[i];
                        this.crawlers.push(new CrawlerViewModel(crawler.id, crawler.name, crawler.biz, crawler.uin, crawler.key, crawler.key_valid, crawler.running, crawler.error, crawler.disabled, crawler.last_crawled_msg_id))
                    }
                    console.info(this.crawlers());
                    $(".message_page_list").page({
                        total: data.crawlers.totalPages,
                        current: data.crawlers.number + 1,
                        autoBackToTop: false,
                        jumpCallBack: function (index) {
                            this.currentPage(index);
                            this.loadCrawlers(this.currentPage());
                        }.bind(this)
                    });
                }
            }.bind(this))
        }.bind(this);
        this.init = function () {
            this.loadCrawlers(1);
        }.bind(this);
        this.add = function () {
            this.crawlers.unshift(new CrawlerViewModel());
        }.bind(this);
    }
    viewModel = new CrawlersViewModel();
    // binding
    viewModel.init();
    ko.applyBindings(viewModel);
</script>
</@layout_default.page>