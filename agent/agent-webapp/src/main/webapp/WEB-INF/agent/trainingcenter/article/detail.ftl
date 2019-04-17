<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='文章详情' page_num=17>

<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<div class="row-fluid sortable ui-sortable box-content">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>文章详情</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content form-horizontal">
            <div class="control-group">
                <label class="control-label" for="focusedInput">标题</label>
                <div class="controls">
                    ${articleInfo.title!''}
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="focusedInput">栏目</label>
                <div class="controls columnList">
                    ${articleInfo.oneLevelColumnName!''}    ${articleInfo.twoLevelColumnName!''}
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">封面</label>
                <div class="controls" id="sourceFileWrap">
                    <#if articleInfo??>
                        <img src="${articleInfo.coverImgUrl!''}" alt="" class="upload_image" style="width: 200px;height:150px;display: block; ">
                    </#if>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">阅读情况</label>
                <div class="controls">
                    <div class="dataTables_wrapper">
                        <table class="table table-striped table-bordered bootstrap-datatable">
                            <thead>
                            <tr>
                                <th class="sorting" style="width: 50px;">总浏览次数</th>
                                <th class="sorting" style="width: 50px;">天玑内浏览次数</th>
                                <th class="sorting" style="width: 50px;">天玑内送达人数</th>
                                <th class="sorting" style="width: 50px;">天玑内浏览人数</th>
                                <th class="sorting" style="width: 50px;">天玑内阅读率</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td>${articleInfo.viewsNumAll!''}</td>
                                <td>${articleInfo.viewsNumTj!''}</td>
                                <td>${articleInfo.servicePersonNum!''}</td>
                                <td>${articleInfo.viewsPersonNumTj!''}</td>
                                <td>${articleInfo.viewsRateTj!''}%</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                    <div>
                        <a href="/trainingcenter/article/export_article_views_info.vpage?id=${articleInfo.id!''}" class="btn btn-primary">下载明细数据</a>
                    </div>
                </div>
            </div>
            <div class="control-group noedit-item">
                <label class="control-label">发布对象</label>
                <div class="controls">
                    <#list articleInfo.roleTypeList as list>
                    <span style="margin-right: 5px;">${list.roleName!''}</span>
                    </#list>
                </div>
            </div>
            <div class="control-group noedit-item">
                <label class="control-label">发布部门</label>
                <div class="controls">
                    <div id="useUpdateDep_con_dialog" class="span4"></div>
                </div>
            </div>
            <div class="control-group noedit-item">
                <label class="control-label">是否跳至APP内打开</label>
                <div class="controls">
                    ${articleInfo.openInAPP!''}
                </div>
            </div>
            <div class="control-group noedit-item">
                <label class="control-label">文章链接</label>
                <div class="controls">
                    ${articleInfo.jumpUrl!''}
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">正文</label>
                <div class="controls">
                    <script id="content_area"  type="text/plain"></script>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.config.js?v=20180706"></script>
<script src="${requestContext.webAppContextPath}/public/js/ueditor1_4_3/ueditor.all.js?v=20180706"></script>
<script type="text/javascript">
    $(function () {
        var ue = UE.getEditor('content_area', {
            serverUrl: "/workspace/appupdate/ueditorcontroller.vpage",
            zIndex: 999,
            fontsize: [8, 9, 10, 13, 16, 18, 20, 22, 24, 26],
            toolbars: [[
                'fullscreen', 'source', '|', 'undo', 'redo', '|',
                'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc', '|',
                'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
                'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
                'directionalityltr', 'directionalityrtl', 'indent', '|',
                'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
                'link', 'unlink', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
                'simpleupload','insertvideo', 'pagebreak', 'template', 'background', '|',
                'horizontal', 'date', 'time', 'spechars', '|', 'searchreplace'
            ]]
        });

        var content = '${articleInfo.content?replace('[\r\n]','','r')!""}';
        //发布部门列表
        var groupIds = '';
        var groupIdsArr = [];
        <#list articleInfo.groupIdList as list>
        groupIdsArr.push(${list!0});
        </#list>
        groupIds = groupIdsArr.toString();

        var build_video_html = function (src, poster) {
            return '<video webkit-playsinline="true" height="195" width="400" preload="none" playsinline="true" controlsList="nodownload" controls="" src='+ src +' poster='+ poster +' ></video>'
        }

        setTimeout(function () {
            ue.setContent(
                // 修复视频标签问题
                // 正则匹配embed标签 match输出内容所有的embed(内容可能有多个)
                content.replace(/<embed[^>]*>/gi, function (match) {
                    // 正则匹配embed标签 替换成video标签同时赋值src
                    match = match.replace(/<embed.+src\s*=\s*['"]([^"]*)['"].*?\/>/ig, '<video src="$1"></video>');
                    return match;
                })
                .replace(/<video.*?>.*?<\/video>/ig, function (match) {
                    match = match.replace(/<video.+src\s*=\s*['"]([^"]*)['"].*?>.*?<\/video>/ig,function (a, src) {
                        return build_video_html(src,src.replace('v.17zuoye.cn', '17zy-content-video.oss-cn-beijing.aliyuncs.com')+'?x-oss-process=video/snapshot,t_2000,f_jpg,m_fast,w_360');
                    });
                    return match;
                }));
        },500);

        $("#useUpdateDep_con_dialog").fancytree({
            source: {
                url: "get_all_department_tree.vpage?selectedGroupIds="+groupIds,
                cache:true
            },
            checkbox: true,
            autoCollapse:true,
            selectMode: 3
        });

    });
</script>
</@layout_default.page>