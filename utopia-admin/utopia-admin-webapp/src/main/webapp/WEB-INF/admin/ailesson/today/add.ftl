<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑短期课" page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js"></script>
<#--<script src="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.js"></script>-->
<#--<link href="${requestContext.webAppContextPath}/public/js/bootstrap/bootstrap.min.css" rel="stylesheet">-->
<style>
    [v-cloak]{
        display: none;
    }
    .wd600{
        width:600px !important;
    }
</style>

<div id="box" v-cloak class="span9">
    <div class="form-horizontal">
        <h3 class="h3 text-center"><#if id?? && id != ''>编辑<#else>添加</#if></h3>
        <div class="control-group">
            <label class="control-label">标题：</label>
            <div class="controls">
                <input v-model="data.title" type="text" class="wd600" placeholder="标题">
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">unitId：</label>
            <div class="controls">
                <input v-model="data.unitId" type="text" class="wd600" placeholder="unitId">
            </div>
        </div>
        <hr>
        <div class="control-group">
            <label class="control-label">今日主题：</label>
            <div class="controls">
                <textarea v-model="data.subject" placeholder="今日主题" style="width: 600px;height:60px;"></textarea>
                <span style="color: #bbbbbb;font-size: 14px ;">注：需要换行的结尾加“&lt;br&gt;”</span>
            </div>
        </div>
        <div class="well">
            <h4>今日优秀视频</h4>
            <div class="control-group">
                <label class="control-label">视频介绍文案：</label>
                <div class="controls">
                    <textarea v-model="data.videoDesc" placeholder="视频介绍文案" style="width: 600px;height:60px;"></textarea>
                    <span style="color: #bbbbbb;font-size: 14px ;">注：需要换行的结尾加“&lt;br&gt;”</span>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">视频URL：</label>
                <div class="controls">
                    <input v-model="data.videoUrl" type="text" class="wd600" placeholder="视频URL">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">首帧图片URL：</label>
                <div class="controls">
                    <input v-model="data.videoImg" type="text" class="wd600" placeholder="首帧图片URL">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">视频点评文案：</label>
                <div class="controls">
                    <textarea v-model="data.videoContent" placeholder="视频点评文案" style="width: 600px;height:60px;"></textarea>
                    <span style="color: #bbbbbb;font-size: 14px ;">注：需要换行的结尾加“&lt;br&gt;”</span>
                </div>
            </div>
        </div>

        <div class="well">
            <h4>今日彩蛋</h4>
            <div class="control-group">
                <label class="control-label">彩蛋文案：</label>
                <div class="controls">
                    <textarea v-model="data.eggContent" placeholder="彩蛋文案" style="width: 600px;height:60px;"></textarea>
                    <span style="color: #bbbbbb;font-size: 14px ;">注：需要换行的结尾加“&lt;br&gt;”</span>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">彩蛋视频URL：</label>
                <div class="controls">
                    <input v-model="data.eggVideoUrl" type="text" class="wd600" placeholder="视频URL">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">首帧图片URL：</label>
                <div class="controls">
                    <input v-model="data.eggImg" type="text" class="wd600" placeholder="首帧图片URL">
                </div>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">热度排行出炉文案：</label>
            <div class="controls">
                <textarea v-model="data.hotContent" placeholder="今天的热度排行出台，恭喜{name} {count}位同学,视频获得点击量前三名" style="width: 600px;height:60px;"></textarea>
                <span style="color: #bbbbbb;font-size: 14px ;">注：名字部分用{name}占位，数量用{count}占位</span>
            </div>
        </div>
        <div class="well">
            <h4>今日学习数据总结</h4>
            <div class="control-group">
                <label class="control-label">等级点评：</label>
                <div class="controls">
                    <textarea v-model="data.summaryContent" placeholder="等级点评" style="width: 600px;height:60px;"></textarea>
                    <span style="color: #bbbbbb;font-size: 14px ;">注：需要换行的结尾加“&lt;br&gt;”</span>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">更多详情链接：</label>
                <div class="controls">
                    <input v-model="data.summaryLink" type="text" class="wd600" placeholder="更多详情链接">
                </div>
            </div>
        </div>

        <div class="well">
            <h4>小提示</h4>
            <div class="control-group">
                <label class="control-label">视频URL：</label>
                <div class="controls">
                    <input v-model="data.tipsVideoUrl" type="text" class="wd600" placeholder="视频URL">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">首帧图片URL：</label>
                <div class="controls">
                    <input v-model="data.tipsVideoImg" type="text" class="wd600" placeholder="首帧图片URL">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">下节课介绍：</label>
                <div class="controls">
                    <textarea v-model="data.tipsNextClass" placeholder="下节课介绍" style="width: 600px;height:60px;"></textarea>
                    <span style="color: #bbbbbb;font-size: 14px ;">注：需要换行的结尾加“&lt;br&gt;”</span>
                </div>
            </div>
        </div>
        <hr>
        <div class="text-center">
            <span class="btn btn-info btn-large" @click="preview" >预览</span>
            <span class="btn btn-success btn-large" @click="save">保存</span>
        </div>
    </div>

    <!-- Modal -->
    <div id="myModal" class="modal" :class="{hide:!model,fade:!model}" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
        <div class="modal-header">
            <button type="button" class="close" @click="closeModel">×</button>
            <h3 id="myModalLabel">用手机扫描二维码预览</h3>
        </div>
        <div class="modal-body">
            <div id="sharecode_box" style="text-align: center"></div>
        </div>
    </div>

    <div :class="{in:model,'modal-backdrop':model,fade:model}"></div>

</div>



<script type="text/javascript">
    var vm = new Vue({
        el:'#box',
        data:{
            previewStatus:false,
            model:false,
            data:{
                bookId:'',
                title:'',
                unitId:'',
                subject:'',
                videoDesc:'',
                videoUrl:'',
                videoImg:'',
                videoContent:'',
                eggContent:'',
                eggVideoUrl:'',
                eggImg:'',
                hotContent:'',
                summaryContent:'',
                summaryLink:'',
                tipsVideoUrl:'',
                tipsVideoImg:'',
                tipsNextClass:'',
            }
        },
        methods:{
            save:function(){
                var _this = this;
                $.post('${requestContext.webAppContextPath}/chips/ai/todaylesson/detail.vpage', {
                    data:JSON.stringify(_this.data)
                }, function (res) {
                    if (res.success) {
                        _this.previewStatus = true;
                        alert("保存成功");

                    } else {
                        alert(res.info);
                    }
                });
            },
            preview:function(){
                var _this = this;

                if(_this.previewStatus){
                    var hostName = window.location.host;
                    var _map = {
                        'admin.test.17zuoye.net':'wechat.test.17zuoye.net',
                        'admin.17zuoye.net':'wechat.17zuoye.com',
                        'admin.dc.17zuoye.net': 'wechat.17zuoye.com',
                        'admin.staging.17zuoye.net': 'wechat.staging.17zuoye.net',
                    }

                    hostName = _map[hostName] ? _map[hostName] : hostName;

                    if(hostName.indexOf('8085') > -1){
                        hostName = hostName.replace(/8085/g,"8180")
                    }
                    var url = "http://"+hostName+"/chips/center/todaystudy.vpage?preview=1&unitId="+_this.data.unitId

                    var codeImgSrc = "https://www.17zuoye.com/qrcode?m="+encodeURIComponent(url);

                    var imgObj = new Image();
                    imgObj.src = codeImgSrc;
                    imgObj.style.width = "200px";
                    imgObj.style.height = "200px";
                    $("#sharecode_box").html('')
                    $("#sharecode_box").append(imgObj);
                    _this.previewStatus = false;
                    _this.model = true;
                }else{
                    alert("请保存后预览")
                }
            },
            closeModel:function(){
                var _this = this;
                _this.model = false;
            }
        },
        created:function(){
            var _this = this;
            var id = "${id!}";
            if(id){
                $.get('${requestContext.webAppContextPath}/chips/ai/todaylesson/detail.vpage', {
                    id:id
                }, function (res) {
                    console.log(res)
                    _this.data = $.extend(_this.data,res.data)
                });
            }
        }
    })

</script>

</@layout_default.page>