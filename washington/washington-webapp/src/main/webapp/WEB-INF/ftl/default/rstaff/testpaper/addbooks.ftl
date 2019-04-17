<script id="t:添加教材" type="text/html">
    <div id="rstaff_books_list_box" class="spacing_vox">
        <div class="tabListBox">
            <h6 class="titleTab">请选择教材的年级：(应试题仅对3至6年级教材开放)</h6>
            <ul id="clazz_list_box">
                <li class="active"><a data-level="3" data-term="${lastTerm.key!''}" href="javascript:void(0);">三年级（${lastTerm.brief!''}）</a></li>
                <li><a data-level="4" data-term="${lastTerm.key!''}" href="javascript:void(0);">四年级（${lastTerm.brief!''}）</a></li>
                <li><a data-level="5" data-term="${lastTerm.key!''}" href="javascript:void(0);">五年级（${lastTerm.brief!''}）</a></li>
                <li><a data-level="6" data-term="${lastTerm.key!''}" href="javascript:void(0);">六年级（${lastTerm.brief!''}）</a></li>
                <li><a data-level="3" data-term="${nextTerm.key!''}" href="javascript:void(0);">三年级（${nextTerm.brief!''}）</a></li>
                <li><a data-level="4" data-term="${nextTerm.key!''}" href="javascript:void(0);">四年级（${nextTerm.brief!''}）</a></li>
                <li><a data-level="5" data-term="${nextTerm.key!''}" href="javascript:void(0);">五年级（${nextTerm.brief!''}）</a></li>
                <li><a data-level="6" data-term="${nextTerm.key!''}" href="javascript:void(0);">六年级（${nextTerm.brief!''}）</a></li>
            </ul>
            <div class="clear"></div>
        </div>
        <h6 class="titleTab">请点击选中要添加的教材：</h6>
        <div id="books_list_box" class="booksList">
            <#-- 课本显示 -->
        </div>

        <div class="addBookInfo">已添<b id="select_book_num_box"></b>册教材</div>
        <div id="selected_book_list" class="booksList">
            <#--已选择课本-->
        </div>
    </div>
</script>

<div style="display:none">
    <div id="book_item_select_sign_green_template">
        <div class="book_item_select_sign" style="top: 0;position: absolute; margin-left: 80px">
            <i class='icon_rstaff icon_rstaff_15'></i>
        </div>
    </div>
</div>

<script>
    /** 已选择的教材 */
    var selectedBooks = ${selectedBooksList!'[]'};
    var Book = function(id,cname,imgUrl){
        this.id = id;
        this.cname = cname;
        this.imgUrl = imgUrl;
    };

    /** 删除所选的课本*/
    function removeSelectedBooks(id){
        for(var i = 0; i < selectedBooks.length; i++){
            var obj = selectedBooks[i];
            if(obj.id == id){
                selectedBooks.splice(i,1);
            }
        }
    }

    /** 更新选择课本列表 */
    function refreshSelectedBooks(){
        var html = '<ul class="jcarousel-skin-tango">';
        $("#select_book_num_box").text(selectedBooks.length);
        for(var i = 0; i < selectedBooks.length; i++){
            var obj = selectedBooks[i];
            var imgUrl;
            imgUrl = ( !$17.isBlank(obj.imgUrl)) ? obj.imgUrl : "";
            imgUrl = (imgUrl.indexOf("upload") < 0) ? '<@app.book href="'+imgUrl+'"/>': imgUrl;
            html += '<li title="点击删除" data-bookid="'+obj.id+'"><div style="position: absolute;margin-left: 44px; cursor: pointer;"><i class="icon_rstaff icon_rstaff_16"></i></div><div><img alt="" height="100" src="'+imgUrl+'"/></div><h4 style="height: 140px">'+obj.cname+'</h4></li>';
        }
        html += '</ul>';
        $("#selected_book_list").html(html);
        $('#selected_book_list .jcarousel-skin-tango').jcarousel({start : ( selectedBooks.length - 1 ),itemFallbackDimension: 300});
        $("#selected_book_list li").on("click", function(){
            removeSelectedBooks($(this).data("bookid"));
            $(this).remove();
            var o = $(".book_list_item[v='"+$(this).data("bookid")+"']").eq(0);
            o.attr("s",0);
            o.find(".book_item_select_sign").remove();
            refreshSelectedBooks();
        });
    }

    function bookListBox(level, term, _this){
        $("books_list_box").html('<div style="height:240px;">数据加载中...<div>');
        $.getJSON('/rstaff/book/sortbook.vpage?level=' + level + '-' + term, function(data){
            resetBookList(data, _this);
        });
    }

    function resetBookList(data, _this){
        var html = '';
        $('#books_list_box').empty();
        if(data.total > 0){
            searchBookList = data.rows;
            html += '<ul class="jcarousel-skin-tango">';
            $.each(data.rows,function(){
                var imgUrl = '<@app.book href="'+this.imgUrl+'"/>';
                var press = this.press == 'null'? '' : this.press;
                html += '<li title="点击添加" style="cursor: pointer" class="book_list_item" v="'+this.id+'" imgUrl="'+imgUrl+'" cn="'+this.cname+'"><div><img width="100px" height="140px" src="'+imgUrl+'"/></div><div class="book_item_add_sign"></div><h4 style="height: 140px">'+this.cname+'</h4></li>';
            });
            html += '</ul">';
        }else{
            html = '<div class="info-ms-ks">没有找到相关教材</div>';
        }
        $('#books_list_box').html(html);
        if(data.total > 0){
            $(".book_list_item").on("click",function(){
                var s = $(this).attr("s");
                if(s == "1"){
                    $(this).find(".book_item_select_sign").remove();
                    $(this).attr("s",0);
                    removeSelectedBooks($(this).attr("v"));
                }else{
                    var html = $("#book_item_select_sign_green_template").html();
                    $(html).appendTo($(this)).attr("v",$(this).attr("v"));
                    $(this).attr("s",1);
                    selectedBooks.push(new Book($(this).attr("v"),$(this).attr("cn"),$(this).attr("imgUrl")));
                }
                refreshSelectedBooks();
            });


            $('#books_list_box .jcarousel-skin-tango').jcarousel({
                itemFallbackDimension: 300
            });
            refreshSelectedBooks();
            refreshSelectedBooksToList();
        }
    }

    /** 标记已选过的课本 */
    function refreshSelectedBooksToList(){
        for(var i = 0; i < selectedBooks.length; i++){
            var obj = selectedBooks[i];
            var item = $(".book_list_item[v='"+obj.id+"']");
            if(item){
                item.attr("s",1);
                $($("#book_item_select_sign_green_template").html()).appendTo(item);
            }
        }
    }

    $(function(){
        $(".rstaff_add_books_but").on("click",function(){
            var states = {
                addBooks : {
                    html        : template("t:添加教材",{}),
                    title       : "添加教材",
                    position    : {width:800},
                    focus       : 1,
                    buttons     : { "取消": false, "完成添加": true },
                    submit      : function(e,v,m,f){
                        e.preventDefault();
                        if(v){
                            var _booksId = '';
                            for(var i = 0; i < selectedBooks.length; i++){
                                var obj = selectedBooks[i];
                                _booksId += ","+obj.id;
                            }
                            if(selectedBooks.length > 0){
                                _booksId = _booksId.substring(1);
                            }

                            if($17.isBlank(_booksId)){
                                $.prompt.goToState("unSelectedBookTip");
                            }

                            $.post("/rstaff/book/saveReseachStaffBook.vpage", {bookIds: _booksId}, function( data ){
                                if(data.success){
                                    $.prompt.goToState("addBooksResult");
                                }
                            });

                        }else{
                            $.prompt.close();
                        }
                    }
                },
                unSelectedBookTip : {
                    html    : "请选择课本",
                    title   : "提示",
                    focus   : 1,
                    buttons : {"确定" : true},
                    submit  : function(e,v,m,f){
                        e.preventDefault();
                        $.prompt.goToState("addBooks");
                    }
                },
                addBooksResult  : {
                    html    : "课本添加成功",
                    title   : "提示",
                    focus   : 1,
                    position:{width : 350},

                    buttons : {"确定" : true},
                    submit  : function(e,v,m,f){
                        setTimeout(function(){ location.href = location.href; }, 200);
                        return false;
                    }
                }
            };
            $.prompt(states,{
                loaded : function(e){
                   var levelTerm = $("#clazz_list_box li.active a");
                   var level = levelTerm.data("level");
                   var term = levelTerm.data("term");
                   bookListBox(level,term,levelTerm);
                },
                top : '5%',
                promptspeed : 'fast'
            });
        });

        $("#clazz_list_box li").live("click",function(){
            $(this).addClass("active").siblings().removeClass("active");
            var levelTerm = $("#clazz_list_box li.active a");
            var level = levelTerm.data("level");
            var term = levelTerm.data("term");
            bookListBox(level,term,levelTerm);
        });
    });
</script>