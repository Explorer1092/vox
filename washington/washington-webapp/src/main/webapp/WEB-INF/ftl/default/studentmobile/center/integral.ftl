<#import "../layout.ftl" as temp >
<@temp.page>
<style>
    html, body{ overflow: hidden !important; width: 100%; height: 100%;}
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

    #integralList{
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
        width: 100%;
        bottom: 0;
        height: 60px;
        line-height: 60px;
        text-align: center;
        background-color: #EEF0F3;
        font-size: 28px;
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
        <div class="prize-area">
            <div class="box">
                <div class="txt">${(currentStudentDetail.userIntegral.usable)!0}<span>学豆</span></div>
                <a href="/studentMobile/center/reward.vpage"><div class="btn">兑换奖品</div></a>
            </div>
        </div>

        <div id="integralList">
            <ul class="bean-list" id="integralListBox"></ul>
            <div id="endBox" style="display: none; text-align: center; color: #A7A7A7;">加载完毕</div>
        </div>
    </div>
</div>


<script type="text/html" id="integralLisTemplateBox">
    <%for(var i = 0; i < data.pagination.content.length; i++){%>
    <li>
        <div class="text"><%=data.pagination.content[i].comment%></div>
        <p><%=data.pagination.content[i].dateYmdString%></p>
        <div class="bean"><span class="num"><%=data.pagination.content[i].integral%></span><i class="bean-small"></i></div>
    </li>
    <%}%>
</script>

<script src="/public/skin/mobile/pc/js/dropload.min.js"></script>

<script type="text/javascript">
    //设置title
    document.title = '我的学豆';

    $(function(){
        var currentPage = 0 ,totalPage = 0, integralListBox = $('#integralListBox');
        var dropLoad = $('#integralList').dropload({
            domDown : {
                domRefresh : '<div class="dropload-refresh">↑上拉加载更多</div>',
                domUpdate  : '<div class="dropload-update">↓释放加载</div>',
                domLoad    : '<div class="dropload-load"><span class="loading"></span>加载中...</div>'
            },
            distance : 20,
            loadDownFn : function(event){
                if (currentPage == totalPage+1) {
                    $('.dropload-load').closest('div').remove();
                    return false;
                }
                getIntegralData(currentPage+1);
                event.resetload();
                $('.dropload-load').closest('div').remove();

            }
        });

        function getIntegralData(pageNumber){
            if(pageNumber == 1){
                integralListBox.html('<div style="text-align: center; padding: 3rem 0;">加载中...</div>');
            }

            $.ajax({
                url: "/studentMobile/center/integralchip.vpage",
                async: false,//改为同步方式
                type: "POST",
                data: {pageNumber :pageNumber},
                success: function (data) {
                    if(data.success){
                        totalPage = data.pagination.totalPages;
                        currentPage = pageNumber;
                        totalPage <= 1 ? dropLoad.lock() : dropLoad.unlock();

                        if(pageNumber == 1){
                            integralListBox.empty();
                        }
                        if(pageNumber == 1 && data.pagination.content.length == 0){
                            integralListBox.html('<div class="no-record">完成作业即可得学豆</div>');
                            return false;
                        }

                        if(data.pagination.content.length > 0){
                            integralListBox.append(template('integralLisTemplateBox',{data : data}));
                        }
                    }else{
                        loginInvalid(data);
                    }
                }
            });
        }

        getIntegralData(1);

        //log
        $M.appLog('integral',{
            app: "17homework_my",
            type: "log_normal",
            module: "integralandreward",
            operation: "page_my_integral"
        });
    });
</script>

</@temp.page>
