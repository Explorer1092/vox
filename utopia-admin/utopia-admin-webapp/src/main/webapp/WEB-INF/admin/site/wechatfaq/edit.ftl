<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='CRM' page_num=4>
<div id="main_container" class="span9">
    <legend>Add Or Edit</legend>
    <div class="row-fluid">
        <div class="span12">
            <form class="well form-horizontal" style="background-color: #fff;">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label">分类：</label>
                        <div class="controls">
                            <select name="catalogId" id="catalogId">
                                <#list catalogs as catalog>
                                    <option <#if faq?? && faq.catalogId == catalog.id>selected="selected" </#if>  value="${catalog.id }">${catalog.name! }</option>
                                </#list>
                            </select>
                            <span style="color: red">*</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">标题：</label>
                        <div class="controls">
                            <input type="text" id="title" name="title" <#if faq??>value="${faq.title!''}"</#if>/>
                            <span style="color: red">*</span>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">简介：</label>
                        <div class="controls">
                            <input type="text" id="description" name="description" <#if faq??>value="${faq.description!''}"</#if>/>
                            <span style="color: red">*</span>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">关键词：</label>
                        <div class="controls">
                            <input type="text" id="keyWord" name="keyWord" <#if faq??>value="${faq.keyWord!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">标题图片：</label>
                        <div class="controls">
                            <input type="text" id="picUrl" name="picUrl" <#if faq??>value="${faq.picUrl!''}"</#if>/>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">内容：</label>
                        <div class="controls">
                            <textarea id="content" name="content" style="width: 400px;height: 500px"><#if faq??>${faq.content!''}</#if></textarea>
                            <span style="color: red">*</span>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">状态：</label>
                        <div class="controls">
                            <select id="status" name="status">
                                <option  <#if faq?? && (faq.status == 'draft')> selected="selected" </#if> value="draft">草稿</option>
                                <option  <#if faq?? && (faq.status == 'published')> selected="selected" </#if> value="published">发布</option>
                            </select>
                            <span style="color: red">*</span>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label">类别：</label>
                        <div class="controls">
                            <select id="type" name="type">
                                <option  <#if faq?? && (faq.type == 0)> selected="selected" </#if> value="0">微信家长通</option>
                                <option  <#if faq?? && (faq.type == 1)> selected="selected" </#if> value="1">微信老师端</option>
                                <option  <#if faq?? && (faq.type == 2)> selected="selected" </#if> value="2">微信校园大使端</option>
                            </select>
                            <span style="color: red">*</span>
                        </div>
                    </div>

                    <div class="control-group">
                        <div class="controls">
                            <input type="button" id="addFaqBtn" value="提交" class="btn btn-large btn-primary">
                            <input type="button" value="预览" class="btn btn-large btn-primary viewBut">
                        </div>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>
</div>
<div id="add_dialog" class="modal fade hide" style="width: 60%; left: 40%;">
    <div class="modal-dialog">
        <div class="modal-content" style="padding: 10px 10px;" >


        </div>
    </div>
</div>


<script type="text/javascript">
    $(function(){
        $("#addFaqBtn").on("click", function(){
            var faqMapper = {
                catalogId: $("#catalogId").val(),
                title : $("#title").val(),
                description: $("#description").val(),
                keyWord: $("#keyWord").val(),
                picUrl: $("#picUrl").val(),
                content: $("#content").val(),
                status: $("#status").val(),
                type: $("#type").val()
                <#if faq??>,id : '${(faq.id)!''}'</#if>
            };

            var reg = /^[0-9]+$/;
            if(faqMapper.title == undefined || faqMapper.title.trim() == ''){
                alert("请输入标题");
                return false;
            }

            if(faqMapper.description == undefined || faqMapper.description.trim() == ''){
                alert("请输入简介");
                return false;
            }

            if(faqMapper.content == undefined || faqMapper.content.trim() == ''){
                alert("请输入内容");
                return false;
            }

            if(faqMapper.status == undefined || faqMapper.status.trim() == ''){
                alert("请选择状态");
                return false;
            }

            if(faqMapper.type == undefined || faqMapper.type.trim() == ''){
                alert("请选择类别");
                return false;
            }

            appPostJson("addfaq.vpage", faqMapper, function(data){
                if(data.success) {
                    location.href = "list.vpage?id="+data.id;
                } else {
                    alert(data.info);
                }
            });
        });

        $('.viewBut').on('click',function(){
            var viewHtml = $('#content').val();
            if(viewHtml == '') {
                return;
            }
            $('#add_dialog').modal('show').find('.modal-content').html(viewHtml.replace(/\.\.\/\.\.\/static/g,'http://wx.17zuoye.com/static'));
        });
    });

    function appPostJson( url, data, callback, error, dataType ) {
        dataType = dataType || "json";
        return $.ajax( {
            type : 'post',
            url : url,
            data : JSON.stringify(data),
            success : callback,
            error : error,
            dataType : dataType,
            contentType : 'application/json;charset=UTF-8'
        } );
    }
</script>
</@layout_default.page>