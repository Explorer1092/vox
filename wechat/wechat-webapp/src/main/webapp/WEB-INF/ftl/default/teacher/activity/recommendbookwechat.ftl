<#import "../layout_view.ftl" as activityMain>
<@activityMain.page title="发现开学新教材" pageJs="recommendbookwechat">
    <@sugar.capsule css=["recommendbook"] />
<div data-bind="visible:notdone" style="display: none">
    <div id="div_choose" data-bind="visible:currentDivNo()==1">
        <div class="textbookPush">
            <div class="tb-tips">老师您好，请为[<span data-bind="text:clazz().clazzName"
                                               style="color: red"></span>]选择新学期将要使用的教材；本次反馈，有助于一起作业更精准的为您推送教材！只需2步，轻松反馈！
            </div>
            <div class="tb-searchBox">
                <i class="search-icon"></i><input type="search" placeholder="输入教材名称" data-bind="textInput:filterString">
            </div>
            <div class="tb-bookBox">
                <div class="tips" data-bind="visible:books().length<=0">未找到您搜索的结果请手动上传教材</div>
                <!--无结果提示-->
                <ul class="tb-bookList" data-bind="foreach:books"><!--教材列表-->
                    <li data-bind="click:$parent.selectBook">
                        <div class="pic"><img data-bind="attr:{src: $parent.calcBookCoverUrl($data.imgUrl)}"></div>
                        <div class="info">
                            <p class="text" data-bind="text:$data.cname"></p>
                        </div>
                    </li>
                </ul>
            </div>
        </div>
        <div class="tb-footer">
            <div class="empty"></div>
            <div class="tb-fixFooter">
                <a href="javascript:void(0)" class="btn btn-green" id="add" data-bind="click:showDivUpload">手动上传</a>
                <!-- ko if: selectedBook -->
                <a href="javascript:void(0)" class="btn" id="choose" data-bind="click:chooseBook">提交</a>
                <!-- /ko -->
                <!-- ko ifnot: selectedBook -->
                <a href="javascript:void(0)" class="btn btn-disabled" id="choose" data-bind="click:chooseBook">提交</a>
                <!-- /ko -->
            </div>
        </div>
    </div>
    <div id="div_upload" data-bind="visible:currentDivNo()==2">
        <div class="textbookPush">
            <div class="tb-tips">输入您将要使用的教材名称，并上传教材面照片</div>
            <div class="tb-iptBox">
                <input type="search" placeholder="输入教材名称" data-bind="textInput:uploadBookName">
            </div>
            <div class="tb-bookBox">
                <!--上传文件点击区域-->
                <div class="uploadBox" style="overflow: hidden;" data-bind="visible:!imageUploaded(),click:uploadWechatImage">
                    <i class="icon"></i>

                    <p>点击添加封面照片</p>
                    <#--<input type="file" name="upfile"-->
                           <#--data-bind="event:{change:uploadImage($element.files[0])}"-->
                           <#--style="font-size: 200px; position: absolute; top: 0; left: 0; opacity: 0; cursor: pointer;"-->
                           <#--accept="image/*">-->
                </div>
                <!--图片预览区域-->
                <div class="uploadPic" data-bind="visible:imageUploaded">
                    <img data-bind="attr:{src:previewImageUrl}">
                    <button data-bind="click:deleteImage" class="deletePic">删除重传</button>
                </div>
            </div>
        </div>
        <div class="tb-footer">
            <div class="empty"></div>
            <div class="tb-fixFooter">
                <a href="javascript:void(0)" class="btn btn-green" data-bind="click:showDivChoose">取消</a>
                <!-- ko if: canUpload-->
                <a href="javascript:void(0)" class="btn" data-bind="click:uploadBook">提交</a>
                <!-- /ko -->
                <!-- ko ifnot: canUpload -->
                <a href="javascript:void(0)" class="btn btn-disabled" data-bind="click:uploadBook">提交</a>
                <!-- /ko -->
            </div>
        </div>
    </div>
</div>
<div data-bind="visible:!notdone()" style="display: table-cell;width:100%; line-height: 25; text-align: center;height:100%;position:absolute;font-size:1rem;display: none;">提交成功，感谢您的参与！</div>
<script type="text/javascript">
    var rootRegionCode =${(currentTeacherDetail.rootRegionCode)!};
    var level = 2;
    //    var recommendModel;
    var subject = "${(currentTeacherDetail.subject)!}";
    var cdnBase = "${cdnBase!}";
    <#if ret?has_content>
        var wechatConfig = ${json_encode(ret)};
    </#if>
</script>
</@activityMain.page>