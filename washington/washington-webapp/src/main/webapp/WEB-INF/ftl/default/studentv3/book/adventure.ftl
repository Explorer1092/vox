<!-- 沃克单词冒险更换教材。 -->
<script type="text/html" id="t:沃克单词冒险更换教材">
    <div class="t-student-materials">
        <div style="padding-bottom: 10px; color: #666;">当前沃克单词冒险课本：<span id="currentBookName"></span> </div>
        <div class="nav">
            <input id="search_input" class="w-input" type="text" placeholder="输入关键字搜索教材">
            <div id="clazzLevelListBox" class="nav-list">
            <#assign numberList = ['一年级','二年级','三年级','四年级','五年级','六年级']/>
            <#list 1..numberList?size as clazzLevel>
                <a data-level="${clazzLevel}" class="<#if (currentStudentDetail.clazz.fetchClazzLevel().level == clazzLevel)!false>active</#if>" href="javascript:void (0)">${numberList[clazzLevel_index]}</a>
            </#list>
                <a data-level="-1" href="javascript:void (0)">更换记录</a>
            </div>
        </div>
        <div id="nekketsu_book_list_box" class="t-learn-mater">
        <#--根据年级加载课本-->
        </div>
    </div>
</script>

<script type="text/html" id="t:根据年级加载课本">
    <%if(bookData.length > 0){%>
        <%for(var i=0; i < bookData.length; i++){%>
            <dl data-book_id="<%=bookData[i].id%>" <%if('${adventureBookId!''}' == bookData[i].id){%> class="hover" <%}%> >
                <dt>
                    <span class="w-build-image w-build-image-<%=bookData[i].color%>">
                        <strong class="wb-title"><%=bookData[i].viewContent%></strong>
                        <%if(bookData[i].latestVersion){%>
                            <span class="wb-new"></span>
                        <%}%>
                    </span>
                </dt>
                <dd>
                    <p><%=bookData[i].cname%></p>
                </dd>
            </dl>
        <%}%>
    <%}else{%>
        <div class="t-learn-inner">
            <div class="learn-con  w-fl-right">
                <div class="his-info">
                    <p>暂无该年级教材</p>
                </div>
            </div>
        </div>
    <%}%>
</script>

<@sugar.capsule js=["fastLiveFilter"] />

<script type="text/javascript">
    function searchInput(){
        var search_input = $('#search_input');
        search_input.fastLiveFilter("#nekketsu_book_list_box", {
            callback: function() {
            }
        });
        search_input.val(search_input.val());
        search_input.trigger('change');
    }

    $(function(){
        var statesdemo = {
            state0: {
                title : "沃克单词冒险更换教材",
                html : template("t:沃克单词冒险更换教材", {}),
                buttons : {"取消" : false , "提交": true},
                focus : 1,
                position : {width: '850'},
                submit:function(e,v,m,f){
                    e.preventDefault();
                    if(v){
                        var bookId = $("#nekketsu_book_list_box dl.hover").data('book_id');
                        if($17.isBlank(bookId)){
                            $.prompt.goToState('state1');
                            return false;
                        }

                        $.post('/student/nekketsu/adventure/changebook.vpage',{bookId : bookId}, function(data){
                            if(data.success){
                                setTimeout(function(){location.href = '/student/nekketsu/adventure.vpage'},200);
                            }else{
                                $17.alert('沃克单词冒险更换教材失败');
                            }
                        });
                    }
                }
            },
            state1: {
                title : "沃克单词冒险更换教材",
                html :'请选择你要更换的沃克单词冒险教材',
                buttons : { "确定": true},
                focus : 0,
                submit:function(e,v,m,f){
                    e.preventDefault();
                    $.prompt.goToState('state0');
                }
            }
        };

        $.prompt(statesdemo, {
            loaded : function(){


                //根据年级加载课本
                var nekketsuBooksCache = {};
                $("#clazzLevelListBox a").on('click', function(){
                    var $this = $(this);
                    var search_input = $('#search_input');

                    $this.addClass('active').siblings().removeClass('active');

                    var clazzLevel = $this.data('level');
                    var nekketsu_book_list_box = $("#nekketsu_book_list_box");

                    //根据clazzLevel加载cache中的课本
                    if(nekketsuBooksCache[clazzLevel] != undefined){
                        nekketsu_book_list_box.html(template("t:根据年级加载课本", {bookData :  nekketsuBooksCache[clazzLevel]}));
                        searchInput();
                        return;
                    }


                    var url = clazzLevel == -1 ? '/student/nekketsu/adventure/gethistorybooks.vpage' : '/student/nekketsu/adventure/getavailablebooks.vpage?clazzLevel='+clazzLevel;
                    nekketsu_book_list_box.html('<div style="padding: 50px 0; text-align: center;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>');

                    $.get(url, function(data){
                        if(data.success){
                            nekketsu_book_list_box.html(template("t:根据年级加载课本", {bookData : data.books}));
                            searchInput();
                            nekketsuBooksCache[clazzLevel] = [].concat(data.books)
                        }else{
                            nekketsu_book_list_box.html('课本加载失败');
                        }
                    });
                });

                //隐藏弹窗的取消按钮 todo
                $("#jqi_state0_button取消").hide();

                //加载当前年级的课本
                $("#clazzLevelListBox a.active").trigger('click');

                //当前使用课本
                $('#currentBookName').html('${adventureBookName!''}');

                $(document).on('click','#nekketsu_book_list_box dl', function(){
                    $(this).addClass('hover').siblings().removeClass('hover');
                });
            }
        });
    });
</script>
