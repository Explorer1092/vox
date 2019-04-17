<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑模板" page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js" xmlns="http://www.w3.org/1999/html"></script>
<script src="https://cdn.bootcss.com/lodash.js/4.17.11/lodash.core.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/recorder/record.js"></script>
<script type="text/javascript" src="/public/js/html2canvas/html2canvas.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/clipboard/clipboard.js"></script>
<style>
    [v-cloak] {
        display: none;
    }
</style>

<div id="box" v-cloak class="span9">
    <div class="form-horizontal">
        <h3>{{ commonTemplateId }} 续费提醒首次模板编辑</h3>
        <div class="well">
            <div class="control-group">
                <label class="control-label">定级报告介绍：</label>
                <div class="controls">
                    <textarea id="id-openingRemarks"  v-model="pojo.openingRemarks" readonly="readonly"  style="width: 600px;height:100px;"></textarea>
                    <span class="btn btn-success btn-large copy-btn"  data-clipboard-target="#id-openingRemarks">复制</span>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">定级报告：</label>
                <div class="controls">
                    <div id="sharecode_box" style="width: 600px;height:200px;text-align: center"></div>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">等级介绍：</label>
                <div class="controls">
                    <textarea id="id-levelIntroduction"  v-model="pojo.levelIntroduction" readonly="readonly" style="width: 600px;height:100px;"></textarea>
                    <span class="btn btn-success btn-large copy-btn"  data-clipboard-target="#id-levelIntroduction">复制</span>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">孩子本期成绩介绍：</label>
                <div class="controls">
                    <textarea id="id-scoreIntroduction" v-model="pojo.scoreIntroduction" readonly="readonly" style="width: 600px;height:100px;"></textarea>
                    <span class="btn btn-success btn-large copy-btn"  data-clipboard-target="#id-scoreIntroduction">复制</span>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">孩子成绩图片：</label>
                <div class="controls" style="display: flex;align-items: center;">
                    <#--<textarea id="id-scoreIntroduction" v-model="pojo.scoreIntroduction"  style="width: 600px;height:100px;"></textarea>-->
                    <#--<span class="btn btn-success btn-large" @click="copyToClipBoard('scoreIntroduction')">复制</span>-->
                        <div v-if="userScoreList && userScoreList.length > 0" style="color: #3a3a3a;">
                            <#--<div style="display: inline-block;background: rgb(231, 237, 255);color: #3a3a3a;font-size: 0.6rem;padding: 0.2rem 1rem;border-radius: 1rem;font-weight: bold;margin: 1rem 0 0.3rem 0;">本期成绩单</div>-->
                            <table id="scoreTable"  style="width: 614px;">
                                <tr class="thead" style="border: 1px solid black;background: #EBF1FE;">
                                    <td style="border: 1px solid black;"></td>
                                    <td style="border: 1px solid black;text-align: center;">孩子的成绩</td>
                                    <td style="border: 1px solid black;text-align: center;">班级平均分</td>
                                </tr>
                                <tr v-for="(item, index) in userScoreList" style="color: rgb(213, 73, 25);border: 1px solid black;">
                                    <td style="text-align: center;border: 1px solid black;background-color: #f6f9ff;font-weight: bold;color: #3a3a3a;">DAY{{ index+1 }}</td>
                                    <td style="text-align: center;border: 1px solid black;">{{item.userScore}}</td>
                                    <td style="text-align: center;border: 1px solid black;">{{item.avgScore}}</td>
                                </tr>
                            </table>
                        </div>
                        <span class="btn btn-success btn-large" @click="takeScreenshot" style="margin-left: 5px">下载</span>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">孩子成绩解读：</label>
                <div class="controls">
                    <textarea id="id-scoreRemark"  v-model="pojo.scoreRemark" readonly="readonly" style="width: 600px;height:100px;"></textarea>
                    <span class="btn btn-success btn-large copy-btn"  data-clipboard-target="#id-scoreRemark">复制</span>
                </div>
            </div>
            <template v-for="(item, i) in pojo.weekPointList">
                <div class="control-group">
                    <label class="control-label">薄弱点：</label>
                    <div class="controls">
                        <span  style="width: 600px;height:100px;">{{item.weekPointDesc}}</span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">薄弱点解读：</label>
                    <div class="controls">
                        <textarea v-bind:id="'id-remark' + i" v-model="item.remark" readonly="readonly" style="width: 600px;height:100px;"></textarea>
                        <span class="btn btn-success btn-large copy-btn"  v-bind:data-clipboard-target="'#id-remark' + i">复制</span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">薄弱点提升：</label>
                    <div class="controls">
                        <textarea  v-bind:id="'id-promote' + i" v-model="item.promote" readonly="readonly" style="width: 600px;height:100px;"></textarea>
                        <span class="btn btn-success btn-large copy-btn"  v-bind:data-clipboard-target="'#id-promote' + i">复制</span>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label">推课：</label>
                    <div class="controls">
                        <textarea  v-bind:id="'id-pushLesson' + i" v-model="item.pushLesson" readonly="readonly" style="width: 600px;height:100px;"></textarea>
                        <span class="btn btn-success btn-large copy-btn"  v-bind:data-clipboard-target="'#id-pushLesson' + i">复制</span>
                    </div>
                </div>
            </template>
            <div class="control-group">
                <label class="control-label">定级样例视频：</label>
                <div class="controls">
                    <div>
                        <video v-if="pojo.levelVideo" v-bind:src="pojo.levelVideo" preload="auto" autoplay="autoplay" loop="loop" muted="muted" webkit-playsinline="true" playsinline="true" x5-playsinline="true" x5-video-player-type="h5" x5-video-player-fullscreen="false" x5-video-orientation="portraint"></video>
                    </div>
                </div>
            </div>
        </div>
        <canvas  id="cavasimg" ></canvas>
    </div>
</div>

<div class="layer loading_layer" id="loading_layer"></div>
<div class="loading" id="loading"></div>

<script src="${requestContext.webAppContextPath}/public/js/clipboard/clipboard.min.js"></script>
<script type="text/javascript">
    //$('#dialog').modal('show');

    var clipboard = new Clipboard('.copy-btn');
//    clipboard.on('success', function (e) {
//        alert("复制成功");
//    });
//    clipboard.on('error', function (e) {
//        alert("复制失败，请手动复制");
//    });
    </script>
<script type="text/javascript">
    var vm = new Vue({
        el: '#box',
        model: false,
        data: {
            pojo:'',
            userId:"",
            bookId:"",
            userScoreList:[],
            commonTemplateId:"",
            userName:""
        },
        methods: {
        },
        created: function () {
            var _this = this;
            _this.userId = getUrlParam("userId");
            $.get('${requestContext.webAppContextPath}/chips/ai/active/service/queryOtherServiceRenewUserTemplate.vpage', {
                userId: getUrlParam("userId"),
                clazzId:getUrlParam("clazzId"),
                bookId: _this.bookId
            }, function (res) {
                if(res.success) {
                    _this.pojo = res.pojo;
                    _this.commonTemplateId = res.commonTemplateId;
                    _this.bookId = res.bookId;
                    _this.userScoreList = res.userScoreList;
                    _this.userName = res.userName;
                    console.log("userID:" ,_this.userId)
                    console.log("bookId:" ,_this.bookId)

                    var url = res.url;
                    console.log(url)
                    var codeImgSrc = "https://www.17zuoye.com/qrcode?m=" + encodeURIComponent(url);

                    var imgObj = new Image();
                    imgObj.src = codeImgSrc;
                    imgObj.style.width = "200px";
                    imgObj.style.height = "200px";
                    $("#sharecode_box").html('')
                    $("#sharecode_box").append(imgObj);
                    _this.model = true;
                }
            });
        }
    });

    function getUrlParam (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURIComponent(r[2]); return null;
    }

//    function copyToClipBoard(id) { //复制到剪切板
//        console.log("copyToClipBoard:",id)
//        var range = document.createRange();
//        range.selectNodeContents(document.getElementById('id-' + id));
//
//        const selection = window.getSelection();
//        if(selection.rangeCount > 0) selection.removeAllRanges();
//        selection.addRange(range);
//        document.execCommand("Copy");
//    }


    function takeScreenshot() {
        console.log('test');
        html2canvas(document.getElementById('scoreTable')).then(function(canvas) {
//
            //cavas 保存图片到本地  js 实现
            //------------------------------------------------------------------------
            //1.确定图片的类型  获取到的图片格式 data:image/Png;base64,......
            var type ='jpg';//你想要什么图片格式 就选什么吧
            var imgdata = canvas.toDataURL('jpg');
            //2.0 将mime-type改为image/octet-stream,强制让浏览器下载
            var fixtype=function(type){
                type=type.toLocaleLowerCase().replace(/jpg/i,'jpeg');
                var r=type.match(/png|jpeg|bmp|gif/)[0];
                return 'image/'+r;
            };
            imgdata=imgdata.replace(fixtype(type),'image/octet-stream');
            //3.0 将图片保存到本地
            var savaFile=function(data,filename)
            {
                var save_link=document.createElementNS('http://www.w3.org/1999/xhtml', 'a');
                save_link.href=data;
                save_link.download=filename;
                var event=document.createEvent('MouseEvents');
                event.initMouseEvent('click',true,false,window,0,0,0,0,0,false,false,false,false,0,null);
                save_link.dispatchEvent(event);
            };
            var filename=vm.userName + "_" + vm.userId + "_用户成绩" +'.'+type;
            //我想用当前秒是可以解决重名的问题了 不行你就换成毫秒
            savaFile(imgdata,filename);


        });
    }

</script>

</@layout_default.page>