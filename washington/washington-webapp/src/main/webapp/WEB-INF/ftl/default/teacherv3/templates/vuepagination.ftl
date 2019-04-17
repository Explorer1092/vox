<script type="text/html" id="T:VUE_PAGE_TEMPLATE">
    <div class="system_message_page_list homework_page_list" v-if="totalPage > 1" style="width: 100%; background: #edf5fa; padding:15px 0; text-align: center;">
        <a v-bind:class="{'disable' : currentPage <= 1,'enable' : currentPage > 1}" v-on:click="page_click(currentPage() - 1)" href="javascript:void(0);" v="prev"><span>上一页</span></a>
        <template v-if="pageList.length > 0">
            <template v-for="(pn,index) in pageList">
                <a v-if="pn.isPage" v-bind:class="{'this': pn.pageNo == currentPage}" v-on:click="page_click(pn.pageNo)" href="javascript:void(0);">
                    <span v-text="pn.pageNo"></span>
                </a>
                <span v-if="!pn.isPage" class="points">...</span>
            </template>
        </template>
        <a v-bind:class="{'disable' : totalPage <= 1 || currentPage >= totalPage, 'enable' : totalPage > 1 && currentPage < totalPage}" v-on:click="page_click(currentPage + 1)" href="javascript:void(0);" v="next"><span>下一页</span></a>
        <div class="pageGo">
            <input value="" type="text" v-model="userInputPage" /><span class="goBtn" v-on:click="goSpecifiedPage">GO</span>
        </div>
    </div>
</script>

<script type="text/javascript">
    (function () {
        "use strict";

        var vuePages = {
            template : template("T:VUE_PAGE_TEMPLATE",{}),
            data : function(){
                return {
                    currentPage : this.pageNo,
                    userInputPage : "",
                    displayPageCount : 5  // 1..五个页码...总页数
                };
            },
            computed : {
                pageList : function(){
                    var vm = this;
                    var displayPageCount = vm.displayPageCount,currentPage = vm.currentPage,totalPage = vm.totalPage;
                    var pageList;
                    if(totalPage <= (displayPageCount + 2)){
                        pageList = vm.range(1,totalPage).map(function(pageNumberText){
                            return vm.getPageEntity(pageNumberText,true);
                        });
                    }else if(totalPage > (displayPageCount + 2) && currentPage <= (displayPageCount - 1)){
                        pageList = vm.range(1,displayPageCount + 1).map(function(pageNumberText){
                            return vm.getPageEntity(pageNumberText,true);
                        });
                        pageList.push(vm.getPageEntity("...",false));
                        //最后添加最后一个页码
                        pageList.push(vm.getPageEntity(totalPage,true));
                    }else if(totalPage > (displayPageCount + 2) && currentPage > (displayPageCount - 1)){
                        pageList = [];
                        pageList.push(vm.getPageEntity(1,true));
                        pageList.push(vm.getPageEntity("...",false));
                        if((totalPage - currentPage) <= (displayPageCount - 2)){
                            vm.range(totalPage - displayPageCount,totalPage).forEach(function(pageNumberText){
                                pageList.push(vm.getPageEntity(pageNumberText,true));
                            });
                        }else{
                            vm.range(currentPage - 2,currentPage + 2).forEach(function(pageNumberText){
                                pageList.push(vm.getPageEntity(pageNumberText,true));
                            });
                            pageList.push(vm.getPageEntity("...",false));
                            pageList.push(vm.getPageEntity(totalPage,true));
                        }
                    }
                    return pageList;
                }
            },
            props : {
                totalPage : {
                    type : Number,
                    require : true,
                    default : 0
                },
                pageNo : {
                    type : Number,
                    require : true,
                    default : 1
                }
            },
            methods : {
                getPageEntity : function(pageNo,isPage){
                    // isPage : 是否是页码，使用此字段区分是页面，还是[...]
                    // pageNo : 页码文本或【...】文本
                    return {
                         pageNo : pageNo,
                         isPage : !!isPage
                    };
                },
                range : function(start,end){
                    var arr = [];
                    for(var i = start; i <= end; i++){
                        arr.push(i);
                    }
                    return arr;
                },
                goSpecifiedPage : function(){
                    var self = this; //TermPages
                    var totalPage = (+self.totalPage || 0);
                    var pageNo = (+self.userInputPage || 0);
                    if((pageNo <= 0) || (pageNo > totalPage)){
                        self.userInputPage = "";
                    }else{
                        self.page_click(pageNo);
                    }
                },
                page_click : function(pageNo){
                    var self = this;
                    pageNo = +pageNo || 0;
                    if(pageNo < 1 || pageNo > self.totalPage || pageNo === self.currentPage){
                        return false;
                    }
                    self.currentPage = pageNo;
                    self.$emit("page-click",pageNo);
                }
            },
            created : function(){}
        };

        $17.pagination = $17.pagination || {};
        $17.extend($17.pagination, {
            vuePages : vuePages
        });
    })();
</script>