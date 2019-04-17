<!-- 阿分题更换教材。 -->
<script type="text/html" id="t:阿分题更换教材">
    <div class="t-student-materials">
        <div style="padding-bottom: 10px; color: #666;">当前阿分题课本：<span>${afentiBook.cname!''}</span> </div>
        <div class="nav">
            <input id="search_input" class="w-input" type="text" placeholder="输入关键字搜索教材">
            <div id="clazzLevelListBox" class="nav-list">
                <#if subject == "MATH">
                    <a href="javascript:void (0)" data-level="1" <#if (currentStudentDetail.clazz.fetchClazzLevel().level) == 1> class="active" </#if>>一年级</a>
                    <a href="javascript:void (0)" data-level="2" <#if (currentStudentDetail.clazz.fetchClazzLevel().level) == 2> class="active" </#if>>二年级</a>
                </#if>
                <a href="javascript:void (0)" data-level="3" <#if (currentStudentDetail.clazz.fetchClazzLevel().level) == 3> class="active" </#if>>三年级</a>
                <a href="javascript:void (0)" data-level="4" <#if (currentStudentDetail.clazz.fetchClazzLevel().level) == 4> class="active" </#if>>四年级</a>
                <a href="javascript:void (0)" data-level="5" <#if (currentStudentDetail.clazz.fetchClazzLevel().level) == 5> class="active" </#if>>五年级</a>
                <a href="javascript:void (0)" data-level="6" <#if (currentStudentDetail.clazz.fetchClazzLevel().level) == 6> class="active" </#if>>六年级</a>
                <a href="javascript:void (0)" data-level="-1">更换记录</a>
            </div>
        </div>
        <div id="afenti_book_list_box" class="t-learn-mater">
            <#--根据年级加载课本-->
        </div>
    </div>
</script>

<script type="text/html" id="t:根据年级加载课本">
    <%if(bookData.length > 0){%>
        <%for(var i=0; i < bookData.length; i++){%>
            <dl data-book_id="<%=bookData[i].id%>">
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
        search_input.fastLiveFilter("#afenti_book_list_box", {
            callback: function() {
            }
        });
        search_input.val(search_input.val());
        search_input.trigger('change');
    }

    $(function(){
        var statesdemo = {
            state0 : {
                html : template("t:阿分题更换教材", {}),
                title : "阿分题更换教材",
                position : {width: '850'},
                buttons : {"取消" : false , "提交": true},
                focus : 1,
                submit:function(e,v,m,f){
                    e.preventDefault();
                    if(v){
                        var bookId = $("#afenti_book_list_box dl.hover").data('book_id');
                        if($17.isBlank(bookId)){
                            $.prompt.goToState('state1');
                            return false;
                        }
                        App.postJSON('/student/book/afenti.vpage?subject=${subject!''}',{bookId : bookId}, function(data){
                            if(data.success){
                                setTimeout(function(){location.href = '/afenti/api/index.vpage?subject=${subject!''}'},200);
                            }else{
                                $17.alert('阿分题更换教材');
                            }
                        });
                    }
                }
            },
            state1: {
                html : '请选择你要更换的阿分题教材',
                title : "阿分题更换教材",
                buttons : { "确定": true},
                focus : 0,
                submit : function(e,v,m,f){
                    e.preventDefault();
                    $.prompt.goToState('state0');
                }
            }
        };

        $.prompt(statesdemo, {
            loaded : function(){
                //根据年级加载课本
                var afentiBooksCache = {};
                $("#clazzLevelListBox a").on('click', function(){
                    var $this = $(this);
                    var search_input = $('#search_input');
                    $this.addClass('active').siblings().removeClass('active');

                    var clazzLevel = $this.data('level');
                    var afenti_book_list_box = $("#afenti_book_list_box");
                    var url = clazzLevel == -1 ? '/student/book/afentichangebookhistory.vpage?subject=${subject!''}' : '/student/book/afentichip.vpage?subject=${subject!''}&clazzLevel='+clazzLevel;

                    //根据clazzLevel加载cache中的课本
                    if(afentiBooksCache[clazzLevel] != undefined){
                        afenti_book_list_box.html(template("t:根据年级加载课本", {bookData : afentiBooksCache[clazzLevel]}));
                        searchInput();
                        return;
                    }

                    afenti_book_list_box.html('<div style="padding: 50px 0; text-align: center;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>');
                    $.get(url,function(data){
                        if(data.success){
                            afenti_book_list_box.html(template("t:根据年级加载课本", {bookData : data.bookList}));
                            afentiBooksCache[clazzLevel] = [].concat(data.bookList);
                            searchInput();
                        }
                    });
                });

                //隐藏弹窗的取消按钮 todo
                $("#jqi_state0_button取消").hide();

                //加载当前年级的课本
                $("#clazzLevelListBox a.active").trigger('click');

                $(document).on('click','#afenti_book_list_box dl', function(){
                    $(this).addClass('hover').siblings().removeClass('hover');
                });
            }
        });
    });
</script>