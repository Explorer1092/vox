<@sugar.capsule js=["fastLiveFilter"] />
<div id="201502281350_booklist"></div>
<script id="t:20140319_booklist_书本列表" type="text/html">
    <div class="container_title container_public" style="border-width: 0 0 1px;">

        <ul class="inline_vox row_vox_right container_tab">
            <li data-tablevel="1"><a href="javascript:void(0);"><strong>一年级</strong></a></li>
            <li data-tablevel="2"><a href="javascript:void(0);"><strong>二年级</strong></a></li>
            <li data-tablevel="3"><a href="javascript:void(0);"><strong>三年级</strong></a></li>
            <li data-tablevel="4"><a href="javascript:void(0);"><strong>四年级</strong></a></li>
            <li data-tablevel="5"><a href="javascript:void(0);"><strong>五年级</strong></a></li>
            <li data-tablevel="6"><a href="javascript:void(0);"><strong>六年级</strong></a></li>
        </ul>

        <input id="20140319_Books_search" placeholder="输入关键字搜索教材" value="" class="int_vox" style="width: 120px; margin: 0 0 0 5px; padding: 6px 10px !important;">
    </div>
    <div class="container_summary" style="border-width: 0">
        <div>
            <div style="padding:20px 30px;"><strong class="text_gray_6">设置教材：</strong></div>
            <ul id="gradeBooks_1" data-books="1" class="bookListBox" style="display: none; height:422px; margin:0 auto; overflow-y: auto; overflow-x: hidden;"></ul>
            <ul id="gradeBooks_2" data-books="2" class="bookListBox" style="display: none; height:422px; margin:0 auto; overflow-y: auto; overflow-x: hidden;"></ul>
            <ul id="gradeBooks_3" data-books="3" class="bookListBox" style="display: none; height:422px; margin:0 auto; overflow-y: auto; overflow-x: hidden;"></ul>
            <ul id="gradeBooks_4" data-books="4" class="bookListBox" style="display: none; height:422px; margin:0 auto; overflow-y: auto; overflow-x: hidden;"></ul>
            <ul id="gradeBooks_5" data-books="5" class="bookListBox" style="display: none; height:422px; margin:0 auto; overflow-y: auto; overflow-x: hidden;"></ul>
            <ul id="gradeBooks_6" data-books="6" class="bookListBox" style="display: none; height:422px; margin:0 auto; overflow-y: auto; overflow-x: hidden;"></ul>
        </div>
    </div>
</script>
<script id="t:20140319_booklist_书本模板" type="text/html">
    <%for(var i = 0; i < rows.length; i++){%>
        <% if(term == 0 || term == rows[i].term){ %>
            <li class="bookCoverBox">
                <p>
                    <a href="javascript:void(0);" class="20140319_booklist" data-bookid="<%=rows[i].id%>" data-bookname="<%=rows[i].name%>" data-imgurl="<@app.book href='<%=rows[i].imgUrl%>'/>">
                        <img width="200" height="280" src="<@app.book href='<%=rows[i].imgUrl%>'/>">
                        <% if((newDate - rows[i].createTime*1)/(1000*60*60*24*30) < 1){ %>
                            <span>
                                <label class="tag"><i class="icon_general icon_general_90" style="display: none;"></i></label>
                            </span>
                        <% } %>
                    </a>
                </p>
                <h5><%=rows[i].name%></h5>
            </li>
        <% } %>
    <%}%>
</script>
<script type="text/javascript">
    var BookList = null;

    $(function(){
        $("#201502281350_booklist").empty().html(template("t:20140319_booklist_书本列表", {}));

        BookList = new $17.Model({
            disableLevels   : [],           //要过滤的年级
            subject         : "ENGLISH",    //学科
            term            : 1,            //不过滤：0，上册：1，下册：2
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
                                $("ul[data-books='" + $this.index + "']").html(template("t:20140319_booklist_书本模板", {
                                    rows    : data.rows,
                                    term    : $this.term,
                                    newDate : new Date().getTime()
                                })).show();

                                $("#20140319_Books_search").fastLiveFilter("#gradeBooks_" + $this.index).trigger("change");
                            }
                        });
                    }else{
                        $("ul[data-books='" + $this.index + "']").show();
                    }

                    $("li[data-tablevel='" + $this.index + "']").radioClass("active");
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

                    $self.radioClass("active");

                    $this.index = $self.attr("data-tablevel");

                    $this.showLevel();
                });

                <#--广播书本被点事件-->
                $(".20140319_booklist").die().live("click", function(){
                    var $self = $(this);
                    $("body").trigger("booklist.click", [$self.attr("data-bookid"), $self.attr("data-bookname"), $self.attr("data-imgurl")]);
                });
            }
        });
    });
</script>