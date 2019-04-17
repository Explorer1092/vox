<script type="text/html" id="T:BOOK_UNIT">
    <div id="bookInfo" class="t-homework-form" style="overflow: visible;">
        <dl style="overflow: visible; z-index: 12;">
            <dt>教材</dt>
            <dd  style=" position: relative;">
                <div class="text">
                    <span v-text="bookName">人教版 </span>
                    <a class="w-blue" href="javascript:void(0);" style="margin-left: 50px;" v-on:click="changeBook">
                        更换教材<span class="w-icon-public w-icon-switch w-icon-switchBlue" style="margin-right: -5px; margin-left: 10px; *margin: 3px 0 0 10px;"></span>
                    </a>
                </div>
            </dd>
            <dt data-bind="if: focusUnit() != null">单元</dt>
            <dd style="position: relative; zoom: 1; height: 24px;">
                <div class="text"><span style="float: left;" v-text="focusUnitName">1单元 你好！</span>
                    <div class="h-slide">
                        <span class="slideText">更换单元<em id="arrowUnit" class="w-icon-arrow w-icon-arrow-blue"></em></span><!--向上w-icon-arrow-topBlue-->
                        <div class="h-slide-box allunit">
                            <label style="cursor: pointer;" v-for="(unitObj,index) in unitList" v-on:click="changeUnit(unitObj)" v-bind:class="{'w-radio-current':unitObj.unitId == focusUnitId}">
                                <span class="w-radio"></span>
                                <span class="line-txt-ellipsis w-icon-md" style="width: 200px;" v-text="unitObj.cname" v-bind:title="unitObj.cname"></span>
                            </label>
                        </div>
                    </div>
                </div>
            </dd>
        </dl>
    </div>
</script>

<script type="text/html" id="T:AUTO_UPDATE_TERM_BOOK">
    <div class='w-ag-center' style='font-size: 16px; line-height: 32px;'>
        亲爱的老师，新学期已至，您正在使用的教材为<br/>
        <strong style='color: #f00;'><%=bookName%></strong>，是否要将其更换为新学期教材？
        <div style="padding: 10px 0 0;">
            <span class="v-change-book w-build-image w-build-image-<%=color%>" style="cursor: pointer;">
                <strong class="wb-title"><%=remindBookPress%></strong>
                <span class="wb-new"></span>
            </span>
            <p><%=remindBookName%></p>
        </div>
    </div>
</script>

<script id="t:换课本" type="text/html">
    <div id="bookListV5" class="h-homework-dialog04 h-homework-dialog">
        <div class="inner">
            <p><span class="iname">册别：</span>
                <label style="cursor: pointer;" v-for="(termObj,index) in termList" v-bind:class="{'w-radio-current' : termObj.term == term}" v-on:click="changeBookTermClick(termObj)">
                    <span class="w-radio"></span> <span class="w-icon-md" v-html="termObj.name + '&nbsp;&nbsp;'">上册&nbsp;&nbsp;</span>
                </label>
            </p>
            <p><span class="iname">年级：</span>
                <label style="cursor: pointer;" v-for="(levelObj,index) in levelList" v-bind:class="{'w-radio-current' : levelObj.level == level}" v-on:click="changeBookLevelClick(levelObj)">
                    <span class="w-radio"></span> <span class="w-icon-md" v-html="levelObj.name + '&nbsp;&nbsp;'"></span>
                </label>
            </p>
            <div class="list-box">
                <div class="list-hd">
                    <p class="hdl">教材列表</p>
                    <p class="hdr"><input id="searchText" v-model="searchText" type="text" v-on:keyup="filterBook" placeholder="输入关键字搜索，如人教、苏教"></p>
                </div>
                <div class="list-mn" v-if="!noFilterRes">
                    <p v-show="bookList.length == 0" class="tips-grey" style="padding-left:0;">未找到相关教材</p>
                    <template v-if="bookList.length > 0">
                        <a href="javascript:void(0)" v-for="(book,index) in bookList" v-bind:class="{'active':book.id == selectBookId}" v-bind:style="{'display':book.isShow ? '' : 'none'}" v-bind:title="book.name" v-text="book.name" v-on:click="bookClick(book)"></a>
                    </template>
                </div>
                <div style="padding:86px 0;" v-if="noFilterRes">
                    <p class="tips-grey" style="padding-left:0;">对不起，没有找到“<span data-bind="text:searchText"></span>”相关教材，请更换关键词重新查询或点击下方的操作将您想要查找的教材告诉我们，我们会尽快提供相应教材。</p>
                    <p>没有教材？<a v-on:click="noBookFeedBack" href="javascript:void(0)" style="color: #189cfb">点击这里</a></p>
                </div>
            </div>
        </div>
        <div class="bottom" v-if="!noFilterRes">
            <div class="bot-left">
                <p><span v-if="selectBookId != null">确认要将</span>现在使用的“<span v-text="bookName"></span>”</p>
                <p v-if="selectBookId != null">改为“<span v-text="selectBookName"></span>”？</p>
            </div>
            <a href="javascript:void(0)" class="w-btn btn" v-on:click="saveChangeBook">确定</a>
        </div>
    </div>
</script>