<#import "../../layout/webview.layout.ftl" as layout/>
<@layout.page
pageJs=['jquery', 'YQ','knockout','voxLogs']
pageCssFile={"init" : ["public/skin/teacherv3/css/basev1"]}
>
<style>
    body{background:#fff;}
    .w-base{background:#fff;}
    .w-base .w-base-title {border-bottom: 1px solid #dfdfdf; height: 45px;}
    .w-base .w-base-title h3 {font-size: 18px;color: #4e5656;padding: 14px 0 0 14px;float: left;}
    .w-base-switch{float:right; line-height:45px; padding:0px 15px;}
    .w-base-switch a{display:inline-block}
    .my_experience_coupon_box { width: 930px;  margin: 15px auto 0; border: 1px solid #ccc; border-radius: 4px; -moz-border-radius: 4px; -webkit-border-radius: 4px; }
    .my_experience_coupon_box p.title, .my_experience_coupon_box table thead tr { height: 40px; border-bottom: 1px solid #ccc; }
    .my_experience_coupon_box p.title { height: 40px; font: 16px/40px "微软雅黑"; text-indent: 14px; }
    .resoure-box{ width: 930px; margin: 15px auto 0; }
    .resoure-box span{ display: inline-block; width: 150px; height: 30px; text-align: center; line-height: 30px; cursor: pointer; border: 1px solid #ccc; border-radius: 4px; box-shadow: 0px 2px 5px #ccc; }
    .resoure-box span a{ color: #555555; }
    .resoure-box span a:hover{ color: #555555; }
    .resoure-box span.activeTab{ background-color: #a1a1a1; }
    .resoure-box span.activeTab a{ color: #ffffff; }
    .my_experience_coupon_box table { width: 100%; }
    .my_experience_coupon_box table thead th { font-weight: normal; background-color: #efeeee; color: #535353; }
    .my_experience_coupon_box table tbody tr { height: 48px; border-bottom: 1px solid #ccc; }
    .my_experience_coupon_box table tbody td { text-align: center; }
    .J_blue_btn {display: inline-block;padding: 6px 12px;color: #fff;font-size: 12px;border: 1px solid #56a6de;background-color: #48ade1;border-radius: 4px; -moz-border-radius: 4px;-webkit-border-radius: 4px;}
    .J_blue_btn:hover { background-color: #0586c8;color:#fff; }
</style>
<div class="m-header">
    <div class="m-inner">
        <div class="logo" style="width: 200px;"><a href="/"></a></div>
    </div>
</div>
<div style="width:1000px;margin:30px auto;">
    <div class="w-base">
        <div class="w-base-title">
            <h3>我的资源</h3>
            <#if (currentTeacherDetail.subject) != "CHINESE">
            <div class="w-base-right w-base-switch">
                <ul>
                    <li><a href="/teacher/resource/list.vpage">旧版资源</a></li>
                </ul>
            </div>
            </#if>
        </div>
        <div class="resoure-box">
            <span data-bind="click: toZiyuanResource">
                <a href="javascript:void(0)" >教学课件</a>
            </span>
            <span class="activeTab">
                <a href="javascript:void(0)">课外拓展与教研师训</a>
            </span>
        </div>
        <div class="my_experience_coupon_box">
            <table>
                <thead>
                <tr>
                    <th>课件名称</th>
                    <th>领取日期</th>
                    <th>使用</th>
                </tr>
                </thead>
                <#--课件大赛-->
                <tbody data-bind="foreach: courseList(), visible:courseList().length > 0" style="display:none;">
                    <tr>
                        <td data-bind="text: resourceName"></td>
                        <td data-bind="text: createTime"></td>
                        <td>
                            <a class="J_blue_btn" href="javascript:;" data-bind="click: $root.receiveBtn.bind($data, 2)">
                                <strong>立即使用</strong>
                            </a>
                        </td>
                    </tr>
                </tbody>
                <#--老资源-->
                <tbody data-bind="foreach: list(), visible:list().length > 0" style="display:none;">
                    <tr>
                        <td data-bind="text: name"></td>
                        <td data-bind="text: onlineAt?onlineAt.split(' ')[0]:''"></td>
                        <td>
                            <a class="J_blue_btn" href="javascript:;" data-bind="click: $root.receiveBtn.bind($data, 1)">
                                <strong>立即使用</strong>
                            </a>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
        <div style="display:none;line-height:100px;padding-bottom:30px;font-size:16px;text-align: center" data-bind="visible:list().length == 0">暂无教学课件资源哦！</div>
    </div>

</div>
<div class="m-footer">
    <div class="m-inner">
        <div class="w-fl-left" style="float:left;">
            <div class="copyright">
                <p>Copyright © 2011-${.now?string('yyyy')} 17ZUOYE Corporation. All Rights Reserved.</p>
                <p>ICP证沪B2-20150026 <a href="http://www.miitbeian.gov.cn" style="color: inherit; text-decoration: none">沪ICP备13031855号-2</a> <a href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=11010502032354" target="_blank" style="color:#999"><img src="https://cdn.17zuoye.com/static/project/icp-beian.png" style="display: inline-block;vertical-align: middle;">京公网安备 11010502032354号</a></p>

            </div>
            <div class="link">
                <a class="spare-icon spare-weibo" href="http://weibo.com/yiqizuoye" target="_blank" title="微博"></a>
            </div>
        </div>
        <div class="m-foot-link w-fl-right" style="float:right;">

            <div class="m-code">
                <p class="c-image"></p>
                <p class="c-title">关注我们</p>
            </div>
        </div>
    </div>
</div>
<script>
    signRunScript = function($, YQ,ko){
        function resourceListMode() {
            var _this = this;
            _this.list = ko.observableArray([]);
            _this.courseList = ko.observableArray([]);
            // 立即使用
            _this.receiveBtn = function (type, data) {
                YQ.voxLogs({
                    database:'web_teacher_logs',
                    module: 'm_vphxkyNG',
                    op : 'o_WrtYi4fG',
                    s0 : data.id
                });
                if (type === 1) { // 老资源
                    window.open(data.fileUrl);
                } else { // 课件大赛
                    window.open(data.url);
                }
            };
            // 跳转资源库链接
            _this.toZiyuanResource = function () {
                window.location.href = '/redirector/apps/go.vpage?app_key=ResourcePlatform&return_url=/#/resource/myResource';
            };

            // 请求课外拓展与教研师训数据
            $.post('my_resources.vpage',function (data) {
                if (data.success) {
                    if (data.sourceList.length > 0){
                        _this.list(data.sourceList);
                    }
                }
            });
            // 请求课件大赛领取数据
            $.get('/courseware/contest/personalResources.vpage', function(data) {
                if (data.success) {
                    if (data.data.length > 0){
                        _this.courseList(data.data);
                    }
                }
            });
        }
            
        ko.applyBindings(new resourceListMode());
    }
</script>
</@layout.page>