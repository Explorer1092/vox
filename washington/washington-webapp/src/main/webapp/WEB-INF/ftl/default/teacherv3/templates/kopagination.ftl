<script type="text/html" id="T:PAGE_TEMPLATE">
    <div class="system_message_page_list homework_page_list" style="width: 100%; background: #edf5fa; padding:15px 0; text-align: center;">

        <a data-bind="css:{'disable' : currentPage() <= 1,'enable' : currentPage() > 1},click:page_click.bind($data,$data,currentPage() - 1)" href="javascript:void(0);" v="prev"><span>上一页</span></a>

        <!--ko if:totalPage() <= 7-->
        <!--ko foreach:ko.utils.range(1,totalPage())-->
        <a data-bind="css:{'this':$data == $parent.currentPage()},click:$parent.page_click.bind($data,$parent,$data)" href="javascript:void(0);">
            <span data-bind="text:$data"></span>
        </a>
        <!--/ko-->
        <!--/ko-->

        <!--ko if:totalPage() > 7 && currentPage() <= 4-->
        <!--ko foreach:ko.utils.range(1,6)-->
        <a data-bind="css:{'this':$data == $parent.currentPage()},click:$parent.page_click.bind($data,$parent,$data)">
            <span data-bind="text:$data"></span>
        </a>
        <!--/ko-->
        <span class="points">...</span>
        <a data-bind="click:page_click.bind($data,$data,totalPage())">
            <span data-bind="text:totalPage()"></span>
        </a>
        <!--/ko-->

        <!--ko if:totalPage() > 7 && currentPage() > 4-->
        <a data-bind="click:page_click.bind($data,$data,1)"><span>1</span></a>
        <span class="points">...</span>

        <!--ko if:(totalPage() - currentPage()) <= 3-->
        <!--ko foreach:ko.utils.range(totalPage() - 5,totalPage())-->
        <a data-bind="css:{'this':$data == $parent.currentPage()},click:$parent.page_click.bind($data,$parent,$data)"><span data-bind="text:$data"></span></a>
        <!--/ko-->
        <!--/ko-->

        <!--ko if:(totalPage() - currentPage()) > 3-->
        <!--ko foreach:ko.utils.range(currentPage() - 2,currentPage())-->
        <a data-bind="css:{'this':$data == $parent.currentPage()},click:$parent.page_click.bind($data,$parent,$data)"><span data-bind="text:$data"></span></a>
        <!--/ko-->

        <!--ko foreach:ko.utils.range(currentPage() + 1,currentPage() + 2)-->
        <a data-bind="click:$parent.page_click.bind($data,$parent,$data)"><span data-bind="text:$data"></span></a>
        <!--/ko-->
        <span class="points">...</span>
        <a data-bind="click:page_click.bind($data,$data,totalPage())"><span data-bind="text:totalPage()"></span></a>
        <!--/ko-->
        <!--/ko-->

        <a data-bind="css:{'disable' : totalPage() <= 1 || currentPage() >= totalPage(), 'enable' : totalPage() > 1 && currentPage() < totalPage()},click:page_click.bind($data,$data,currentPage() + 1)" href="javascript:void(0);" v="next"><span>下一页</span></a>
        <div class="pageGo">
            <input value="" type="text" data-bind="textInput:userInputPage" /><span class="goBtn" data-bind="click:goSpecifiedPage">GO</span>
        </div>
    </div>
</script>