<!-- 通天塔更换教材。 -->
<script type="text/html" id="t:通天塔更换教材">
    <div class="t-student-materials">
        <div class="nav">
            <input id="search_input" class="w-input" type="text" placeholder="输入关键字搜索教材">
            <div id="clazzLevelListBox" class="nav-list">
                <#-- 大于等于所在年级的班级列表才显示 -->
                <#assign numberList = ['一年级','二年级','三年级','四年级','五年级','六年级']/>
                <#assign levelList = ['FIRST_GRADE','SECOND_GRADE','THIRD_GRADE','FOURTH_GRADE','FIFTH_GRADE','SIXTH_GRADE']/>
                <#list 1..numberList?size as clazzLevel>
                    <a data-level="${levelList[clazzLevel_index]}"  class="<#if (currentStudentDetail.clazz.fetchClazzLevel().level) == clazzLevel>active</#if>" href="javascript:void (0)">${numberList[clazzLevel_index]}</a>
                </#list>
            </div>
        </div>
        <div id="babel_book_list_box" class="t-learn-mater">
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
                        <%if(bookData[i].latestVersion || bookData[i].versions == '1'){%>
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
        search_input.fastLiveFilter("#babel_book_list_box", {
            callback: function() {
            }
        });
        search_input.val(search_input.val());
        search_input.trigger('change');
    }

    $(function(){
        var statesdemo = {
            state0: {
                title       : "通天塔更换教材",
                html : template("t:通天塔更换教材", {}),
                buttons     : {"取消" : false , "提交": true},
                position : {width: '850'},
                focus       : 1,
                submit:function(e,v,m,f){
                    e.preventDefault();
                    if(v){
                        var bookId = $("#babel_book_list_box dl.hover").data('book_id');
                        if($17.isBlank(bookId)){
                            $.prompt.goToState('state1');
                            return false;
                        }

                        $.post('/student/babel/api/save/updateMyBook.vpage',{bookId : bookId , subject : '${subject!'ENGLISH'}'}, function(data){
                            if(data.success){
                                $17.voxLog({
                                    module: 'changebook',
                                    subject: '${subject!'ENGLISH'}',
                                    bookId:  bookId
                                }, "babel");
                                setTimeout(function(){location.href = '/student/babel/api/index.vpage'},200);
                            }else{
                                $17.alert('通天塔更换教材失败');
                            }
                        });
                    }
                }
            },
            state1: {
                title : "通天塔更换教材",
                html : '请选择你要更换的通天塔教材',
                buttons : { "确定": true},
                focus : 0,
                submit:function(e,v,m,f){
                    e.preventDefault();
                    if(v){
                        $.prompt.goToState('state0');
                    }

                }
            }
        };

        $.prompt(statesdemo, {
            loaded : function(){
                //根据年级加载课本
                var babelBooksCache = {};
                $("#clazzLevelListBox a").on('click', function(){
                    var $this = $(this);

                    $this.addClass('active').siblings().removeClass('active');

                    var clazzLevel = $this.data('level');
                    var babel_book_list_box = $("#babel_book_list_box");

                    //根据clazzLevel加载cache中的课本
                    if(babelBooksCache[clazzLevel] != undefined){
                        babel_book_list_box.html(template("t:根据年级加载课本", {bookData : babelBooksCache[clazzLevel]}));
                        searchInput();
                        return;
                    }
                    babel_book_list_box.html('<div style="padding: 50px 0; text-align: center;"><img src="<@app.link href="public/app/default/images/loadding.gif"/>" alt="加载中..." /> 数据加载中…</div>');
                    $.post('/student/babel/api/booklist.vpage?clazzLevel='+clazzLevel+"&subject=${subject!'ENGLISH'}",function(data){
                        if(data.success){
                            babel_book_list_box.html(template("t:根据年级加载课本", {bookData : data.bookList}));
                            babelBooksCache[clazzLevel] = [].concat(data.bookList);
                            searchInput();
                        }else{
                            babel_book_list_box.html('课本加载失败');
                        }
                    });
                });

                //隐藏弹窗的取消按钮
                $("#jqi_state0_button取消").hide();

                //加载当前年级的课本
                $("#clazzLevelListBox a.active").trigger('click');

                $(document).on('click','#babel_book_list_box dl', function(){
                    $(this).addClass('hover').siblings().removeClass('hover');
                });

                //毕业班级（默认成6年级）
                <#if (currentStudentDetail.clazz.fetchClazzLevel().level) == 99>
                    $("#clazzLevelListBox a[data-level=SIXTH_GRADE]").show().click();
                </#if>
            }
        });
    });
</script>