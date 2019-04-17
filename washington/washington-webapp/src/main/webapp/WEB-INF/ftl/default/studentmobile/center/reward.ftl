<#import "../layout.ftl" as temp >
<@temp.page>
    <style>
        html, body{ height: 100%;overflow: hidden !important;}
        .outer{
            position: absolute;
            left: 0;
            width: 100%;
            height: 100%;
            display: -webkit-box;
            display: -webkit-flex;
            display: -ms-flexbox;
            display: flex;
            -ms-flex-direction:column;
            -webkit-box-orient:vertical;
            box-orient:vertical;
            -webkit-flex-direction:column;
            flex-direction:column;
        }

        #rewardList{
            -webkit-box-flex: 1;
            -webkit-flex: 1;
            -ms-flex: 1;
            flex: 1;
            overflow-y: auto;
            -webkit-overflow-scrolling: touch;
        }
        /* dropload */
        .dropload-refresh,.dropload-update,.dropload-load{
            position: fixed;
            left: 0;
            bottom: 0;
            width: 100%;
            height: 50px;
            line-height: 50px;
            text-align: center;
            background-color: #EEF0F3;
        }
        .dropload-load .loading{
            display: inline-block;
            height: 15px;
            width: 15px;
            border-radius: 100%;
            border: 2px solid #666;
            border-bottom-color: transparent;
            vertical-align: middle;
            -webkit-animation: rotate 0.75s 0 linear infinite;
            animation: rotate 0.75s 0 linear infinite;
        }
        @-webkit-keyframes rotate{0%{-webkit-transform:rotate(0deg)}50%{-webkit-transform:rotate(180deg)}100%{-webkit-transform:rotate(360deg)}}@keyframes rotate{0%{transform:rotate(0deg)}50%{transform:rotate(180deg)}100%{transform:rotate(360deg)}}
    </style>

    <div class="wr" style="height: 100%;">
        <div class="outer">
            <div id="categoriesBox" class="gift-selec">全部</div>
            <div id="categoriesList" class="gift-class" style="display:none;">
                <#--<div class="label active" data-category_id="0">全部分类</div>-->
                <ul class="btn-list clearfix">
                    <#if categories??>
                        <li data-category_id="0">全部</li>
                        <#list categories as categories>
                            <li data-category_id="${(categories.id)!''}">${(categories.categoryName)!''}</li>
                        </#list>
                    </#if>
                </ul>
            </div>
            <div id="rewardList">
                <ul class="gift-list clearfix" id="rewardListBox"></ul>
                <div id="iosTip" style="padding: 10px 0; text-align: center; font-size: 16px; color: #91949d; display: none;">所有活动及奖品均与苹果公司无关</div>
            </div>
        </div>
    </div>

    <script type="text/html" id="rewardListTemplateBox">
        <%if(rewardListData.rows.length > 0){%>
            <%for(var i = 0; i < rewardListData.rows.length; i++){%>
                <li data-price="<%=rewardListData.rows[i].vipPrice%>" style="cursor: pointer;">
                    <div class="pic" style="height: 144px;"><img src="<@app.avatar href='<%=rewardListData.rows[i].image%>'/>" alt=""></div>
                    <div class="hd"><%=rewardListData.rows[i].productName%></div>
                    <div class="bean"><%=rewardListData.rows[i].discountPrice%></div>
                </li>
            <%}%>
        <%}else{%>
            <div class="no-record">没有发现这个类型的奖品</div>
        <%}%>
    </script>

    <script src="/public/skin/mobile/pc/js/dropload.min.js"></script>

    <script type="text/javascript">
        //设置title
        document.title = '教学用品中心';

        $(function(){
            var categoryIdObj = 0, currentPage = 0 ,totalPage = 0, rewardListBox = $('#rewardListBox');

            //下拉选择框
            $('#categoriesBox').on('click',function(){
                var box = $('#categoriesList');
                box.toggle();
                $(this).toggleClass('active');
            });

            //选择分类
            $('#categoriesList [data-category_id]').on('click' ,function(){
                var $this = $(this),categoriesBox = $('#categoriesBox');
                categoriesBox.toggleClass('active');
                var categoryId = $this.data('category_id');
                $('#categoriesList').hide();

                categoriesBox.text($this.text());
                $.ajax({
                    url: "/studentMobile/center/rewardList.vpage",
                    async: false,//改为同步方式
                    type: "POST",
                    data: {categoryId:categoryId, pageNum : 0},
                    success: function (data) {
                        if(data.success){
                            currentPage = 0 ;
                            totalPage = data.totalPage;
                            categoryIdObj = categoryId;
                            rewardListBox.empty().append(template('rewardListTemplateBox',{rewardListData : data}));
                            totalPage <= 1 ? dropLoad.lock() : dropLoad.unlock();
                            dropLoad.resetload();
                            $('.dropload-load').closest('div').remove();
                        }else{
                            loginInvalid(data);
                        }
                    }
                });
            });


            //下拉加载数据
            var dropLoad = $('#rewardList').dropload({
                domDown : {
                    domRefresh : '<div class="dropload-refresh">↑上拉加载更多</div>',
                    domUpdate  : '<div class="dropload-update">↓释放加载</div>',
                    domLoad    : '<div class="dropload-load"><span class="loading"></span>加载中...</div>'
                },
                distance : 20,
                loadDownFn : function(event){
                    if (currentPage == totalPage - 1) {
                        $('.dropload-load').closest('div').remove();
                        return false;
                    }

                    $.ajax({
                        url: "/studentMobile/center/rewardList.vpage",
                        async: false,//改为同步方式
                        type: "POST",
                        data: {categoryId:categoryIdObj, pageNum : currentPage+1},
                        success: function (data) {
                            if(data.success){
                                totalPage = data.totalPage;
                                currentPage = data.pageNum;
                                rewardListBox.append(template('rewardListTemplateBox',{rewardListData : data}));
                                event.resetload();
                                $('.dropload-load').closest('div').remove();
                            }else{
                                loginInvalid(data);
                            }
                        }
                    });
                }
            });


            //load
            rewardListBox.html('<div style="text-align: center; padding: 3rem 0;">加载中...</div>');
            $.ajax({
                url: "/studentMobile/center/rewardList.vpage",
                async: false,//改为同步方式
                type: "POST",
                data: {categoryId:0},
                success: function (data) {
                    if(data.success){
                        rewardListBox.html(template('rewardListTemplateBox',{rewardListData : data}));
                        totalPage = data.totalPage;
                        totalPage <= 1 ? dropLoad.lock() : dropLoad.unlock();
                    }else{
                        loginInvalid(data);
                    }

                },
                error : function(data){
                    //loginInvalid(data);
                }
            });

            //点击购买
            $(document).on('click', '#rewardListBox li', function () {
                var price = $(this).data('price');
                var totalIntegral = '${(currentStudentDetail.userIntegral.usable)!0}';
                if (totalIntegral - price <= 0) {
                    $M.promptAlert('学豆不足');
                } else {
                    $M.promptAlert('请用电脑访问www.17zuoye.com兑换奖品');
                }

                //log
                $M.appLog('reward',{
                    app: "17homework_my",
                    type: "log_normal",
                    module: "integralandreward",
                    operation: "award_click"
                });

            });

            //ios系统提示
            if ($M.getMobileOperatingSystem() == 'iOS') {
                $('#iosTip').show();
            }

            //log
            $M.appLog('reward',{
                app: "17homework_my",
                type: "log_normal",
                module: "integralandreward",
                operation: "page_award_center"
            });
        });
    </script>
</@temp.page>
