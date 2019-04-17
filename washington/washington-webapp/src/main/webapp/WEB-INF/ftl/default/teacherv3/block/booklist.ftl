<@sugar.capsule js=["fastLiveFilter"] />
<div class="w-background-gray w-ag-right">
    <input id="20140922_Books_search" type="text" value="" placeholder="输入关键字搜索素材" class="w-int">
</div>
<div class="t-homework-form">
    <dl>
        <dt>年级</dt>
        <dd>
            <div class="w-border-list t-homeworkClass-list">
                <ul>
                    <li data-tablevel="1" class="v-level">一年级</li>
                    <li data-tablevel="2" class="v-level">二年级</li>
                    <li data-tablevel="3" class="v-level">三年级</li>
                    <li data-tablevel="4" class="v-level">四年级</li>
                    <li data-tablevel="5" class="v-level">五年级</li>
                    <li data-tablevel="6" class="v-level">六年级</li>
                </ul>
            </div>
        </dd>
    </dl>
</div>
<div class="v-book-list w-border-list t-teachingMaterial">
    <ul id="search_book_list_1" data-books="1" class="bookListBox" style="display: none;"></ul>
    <ul id="search_book_list_2" data-books="2" class="bookListBox" style="display: none;"></ul>
    <ul id="search_book_list_3" data-books="3" class="bookListBox" style="display: none;"></ul>
    <ul id="search_book_list_4" data-books="4" class="bookListBox" style="display: none;"></ul>
    <ul id="search_book_list_5" data-books="5" class="bookListBox" style="display: none;"></ul>
    <ul id="search_book_list_6" data-books="6" class="bookListBox" style="display: none;"></ul>
</div>
<div class="w-clear"></div>
<script id="t:20140922_booklist_书本模板" type="text/html">
    <%for(var i = 0; i < rows.length; i++){%>
        <% if(term == 0 || term == rows[i].term){ %>
            <li class="v-book" data-index="<%= i %>">
                <dl class="w-imageText-list">
                    <dt>
                        <span class="v_20140922_booklist w-build-image w-build-image-<%= rows[i].color %>" data-bookid="<%= rows[i].id %>" data-bookname="<%= rows[i].name %>">
                            <strong class="wb-title"><%= rows[i].viewContent %></strong>
                            <%if(rows[i].latestVersion || rows[i].versions == '1'){%>
                                <span class="wb-new"></span>
                            <%}%>
                        </span>
                    </dt>
                    <dd>
                        <div class="build-name"><%=rows[i].name%></div>
                    </dd>
                </dl>
            </li>
        <% } %>
    <%}%>
</script>
<script type="text/javascript">
    var BookList = null;

    $(function(){
        BookList = new $17.Model({
            disableLevels   : [],           //要过滤的年级
            subject         : "ENGLISH",    //学科
            term            : 0,            //不过滤：0，上册：1，下册：2
            index           : 1             //默认显示的年级
        });
        BookList.extend({
            showLevel: function(){
                var $this = this;
                var $target = $("ul[data-books='" + $this.index + "']");

                if($target.size() > 0){
                    $("ul[data-books]").hide();

                    if($target.find("li").size() == 0){
                        App.postJSON("/book/selectbook.vpage", {
                            level   : $this.index,
                            subject : $this.subject
                        }, function(data){
                            if(data.success){
                                $("ul[data-books='" + $this.index + "']").html(template("t:20140922_booklist_书本模板", {
                                    rows    : data.rows,
                                    term    : $this.term,
                                    newDate : new Date().getTime()
                                })).show();

                                $("#20140922_Books_search").fastLiveFilter("#search_book_list_" + $this.index).trigger("change");
                            }
                        });
                    }else{
                        $("ul[data-books='" + $this.index + "']").show();
                    }

                    $("li[data-tablevel='" + $this.index + "']").radioClass("current");
                }
            },
            init: function(obj){
                var $this = this;

                $this.disableLevels = typeof obj.disableLevels  == "undefined" ? $this.disableLevels    : obj.disableLevels;
                $this.subject       = typeof obj.subject        == "undefined" ? $this.subject          : obj.subject;
                $this.term          = typeof obj.term           == "undefined" ? $this.term             : obj.term;
                $this.index         = typeof obj.index          == "undefined" ? $this.index            : obj.index;

                for(var i = 0, l = $this.disableLevels.length; i < l; i++){
                    $("li[data-tablevel='" + $this.disableLevels[i] + "']").remove();
                    $("ul[data-books='" + $this.disableLevels[i] + "']").remove();
                }

                $this.showLevel();

                <#--年级Tab事件-->
                $("li[data-tablevel]").on("click", function(){
                    var $self = $(this);

                    $self.radioClass("current");

                    $this.index = $self.attr("data-tablevel");

                    $this.showLevel();
                });

                <#--广播书本被点事件-->
                $(".w-imageText-list").die().live("click", function(){
                    var $item = $(this).find('.v_20140922_booklist');
                    $("body").trigger("booklist.click", [$item.attr("data-bookid"), $item.attr("data-bookname")]);
                });
            }
        });
    });
</script>