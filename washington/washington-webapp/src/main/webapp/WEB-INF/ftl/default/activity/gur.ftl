<#import "../layout/project.module.ftl" as temp />
<@temp.page header="show" title="作业单元">
    <@sugar.capsule js=["ko", "examCore"] css=[]/>
<style>
    .report-title{background-color: #edf5fa; color: #666; font-size: 20px; padding: 20px; text-align: center; margin: 10px 0; font-weight: normal;}
</style>
<div class="w-base" style="margin-top: -1px;">
<#--//start-->
    <h3 class="report-title">${(urd.unitName)!'---'}</h3>
    <div style="margin: 15px;">
    <#--单元重点-->
        <div class="w-base">
            <div class="w-base-title">
                <h3>单元重点</h3>
            </div>
            <div class="w-table">
                <table>
                    <thead>
                    <tr>
                        <th style="width: 60px">序号</th>
                        <td>本章重点</td>
                        <th style="width: 120px">易考例题</th>
                    </tr>
                    </thead>
                    <tbody>
                            <#if (urd.points?size gt 0)!false>
                                <#list urd.points as content>
                                <tr class="${(content_index%2 == 0)?string("", "odd")}" <#if (content_index + 1)gt 10 >style="display: none"</#if>>
                                    <th>${content_index + 1}</th>
                                    <td>${content.pointName}</td>
                                    <#if content.eid?? && content.eid != "">
                                        <th><a href="javascript:void(0);" data-content="${content.eid!''}|${content.pointId!''}|${content.subject!''}" name="check" type="key" onclick="$17.tongji('PC单元报告', '点击单元重点查看');">查看</a></th>
                                    <#else>
                                        <th><a href="javascript:void(0);"  style="cursor: default; color: #666;" data-content="${content.eid!''}|${content.pointId!''}|${content.subject!''}"  onclick="$17.tongji('PC单元报告', '点击单元重点查看');">查看</a></th>
                                    </#if>
                                </tr>
                                </#list>
                            <#else>
                            <tr><th></th><td style="text-align: center">本单元没有重点题目</td><th></th></tr>
                            </#if>
                            <#if (urd.points?size gt 10)!false>
                                <tr><th><td><div style="text-align:center">
                                    <a href="javascript:void (0)" method="showMore">
                                        查看更多
                                        <span class="w-icon-arrow"></span>
                                    </a>
                                </div></td></th></tr>
                            </#if>
                    </tbody>
                </table>
            </div>
        </div>

    <#--本班情况-->
        <div class="w-base">
            <div class="w-base-title">
                <h3>本班情况</h3>
            </div>
            <div class="w-table">
                <table>
                    <thead>
                    <tr>
                        <th style="width: 60px">序号</th>
                        <td>高频错题</td>
                        <th style="width: 120px">错题详情</th>
                    </tr>
                    </thead>
                    <tbody>
                       <#if (urd.wql?size gt 0)!false>
                            <#list urd.wql as content>
                            <tr class="${(content_index%2 == 0)?string("", "odd")}" <#if (content_index + 1)gt 10 >style="display: none"</#if>>
                                <th>${content_index + 1!0}</th>
                                <td><div id="${content.eid!''}"></div></td>
                                <#if content.eid?? && content.eid != "">
                                    <th><a href="javascript:void(0);"  data-content="${content.eid!''}|${content.pointId!''}|${content.subject!''}" data-name="error">${content.rate!0}%</a></th>
                                <#else>
                                    <th><a href="javascript:void(0);"  style="cursor: default; color: #666;" data-content="${content.eid!''}|${content.pointId!''}|${content.subject!''}" data-name="error">${content.rate!0}%</a></th>
                                </#if>
                            </tr>
                            </#list>
                       <#else>
                          <tr><th></th><td style="text-align: center">本单元没有高频错题</td><th></th></tr>
                       </#if>

                        <#if (urd.wql?size gt 10)!false>
                            <tr><th><td><div style="text-align:center">
                                <a href="javascript:void (0)" method="showMore">
                                    查看更多
                                    <span class="w-icon-arrow"></span>
                                </a>
                            </div></td></th></tr>
                        </#if>

                    </tbody>
                </table>
            </div>
        </div>

    <#--预习安排-->
    <#if (urd.nextPoints?size gt 0)!false>
        <div class="w-base">
            <div class="w-base-title">
                <h3>预习安排</h3>
            </div>
            <h3 class="report-title" style="margin: 0;">${(urd.nextUnitName)!'---'}</h3>
            <div class="w-table">
                <table>
                    <thead>
                    <tr>
                        <th style="width: 60px">序号</th>
                        <td>本章重点</td>
                        <th style="width: 120px">易考例题</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if (urd.nextPoints?size gt 0)!false>
                            <#list urd.nextPoints as content>
                                <#if content??>
                                <tr class="${(content_index%2 == 0)?string("", "odd")}" <#if (content_index + 1)gt 10 >style="display: none"</#if>>
                                    <th>${content_index + 1}</th>
                                    <td>${content.pointName!''}</td>
                                    <#if content.eid?? && content.eid != "">
                                        <th><a href="javascript:void(0);" data-content="${content.eid!''}|${content.pointId!''}|${content.subject!''}" name="check" type="next" onclick="$17.tongji('PC单元报告', '点击预习安排查看');">查看</a></th>
                                    <#else>
                                        <th><a href="javascript:void(0);" style="cursor: default; color: #666;" data-content="${content.eid!''}|${content.pointId!''}|${content.subject!''}" onclick="$17.tongji('PC单元报告', '点击预习安排查看');">查看</a></th>
                                    </#if>
                                </tr>
                                </#if>
                            </#list>
                        <#else>
                        <tr><th></th><td style="text-align: center">本单元没有预习题目</td><th></th></tr>
                        </#if>

                        <#if (urd.nextPoints?size gt 10)!false>
                            <tr><th><td><div style="text-align:center">
                                <a href="javascript:void (0)" method="showMore">
                                    查看更多
                                    <span class="w-icon-arrow"></span>
                                </a>
                            </div></td></th></tr>
                        </#if>

                    </tbody>
                </table>
            </div>
        </div>
    </#if>

    <#--end//-->
    </div>
<#--end//-->
</div>
<script type="text/javascript">
    $(function(){
        //高频错题
        var ids = [];
        <#if (urd.wql?size gt 0)!false>
            <#list urd.wql as id>
                <#if id?? && id.eid??>
                    ids.push("${id.eid!''}");
                </#if>
            </#list>
        </#if>
        //获取题目列表
        getSubject(ids);
        function getSubject(ids){
            try {
                var node ;
                vox.exam.create(function (data) {
                    for(var i=0;i<ids.length;i++){
                        if(ids[i]&&ids[i]!==""){
                           createQ(data,ids[i],false);
                        }
                    }
                });
            } catch (exception) {
                $17.voxLog({
                    module: 'vox_exam_create',
                    op: 'examCoreJs_error',
                    errMsg: exception.message
                });
                $17.tongji('voxExamCreate', 'examCoreJs_error', exception.message);
            }
        };

        //点击查看

        var loadedProject ={};
        $(document).on("click","a[name='check']",function(){
            var dataArr = $(this).attr("data-content");
            var type=$(this).attr("type");
            dataArr = dataArr.split("|");
            var key=null;
            var title = type==="key" ? "本单元重点题目" : type==="next" ? "下一单元预习题目" : "题目预览";
            var placeHolder = "<div id='"+dataArr[0]+"'></div>"
            $.prompt(placeHolder,{
                title : title,
                buttons : {"确定":true},
                position : { width: 760},
                close:function(){
                    closeAudio();
                },
                submit:function(){
                    closeAudio();
                },
                loaded :function(){
                        renderQ(dataArr[0]);
                }
            });
        });
        //关掉音频
        function closeAudio(){
            try{
                vox.task.StopAudioFactory();
            }
            catch(e){
                $17.voxLog({
                    module: 'vox_exam_create',
                    op: 'examCoreJs_error',
                    errMsg: e.message
                });
            }
        };

        //渲染一个题目
        function renderQ(id){
            var node = document.getElementById(id);
            try{
                vox.exam.render(node, 'teacher_preview', {
                    ids: [id],
                    imgDomain: '${imgDomain!''}',
                    domain : '${requestContext.webAppBaseUrl}/',
                    env: <@ftlmacro.getCurrentProductDevelopment />
                });
            }
            catch(e){
                $17.voxLog({
                    module: 'vox_exam_create',
                    op: 'examCoreJs_error',
                    errMsg: exception.message
                });
                $17.tongji('voxExamCreate', 'examCoreJs_error', exception.message);
            }
        }

        //创建题目
        function createQ(data,id,isCache){
                if (data.success) {
                    if (!id) {
                        $('#' + id).html('<div style="padding: 50px 0; text-align: center;">数据加载失败</div>');
                        return false;
                    }
                    renderQ(id);
                    if(isCache)loadedProject[id] = true;
                }
                else {
                    $('#' +id).html('<div style="padding: 50px 0; text-align: center;">数据加载失败</div>');
                    $17.voxLog({
                        module: 'vox_exam_create',
                        op: 'create_error'
                    });
                    $17.tongji('voxExamCreate', 'error', location.pathname);
                }
        }
        //查看更多
        $(document).on("click","a[method='showMore']",function(){
            $(this).hide();
            $(this).parents("table").find("tr").show();
        });

        //test
    });
</script>
</@temp.page>