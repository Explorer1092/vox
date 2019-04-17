<!--前半页-->
<div class="quizHeader" style="margin-bottom: 25px">
    <div class="show_block">
        <div class="inner">
            <div class="select_block">
                <h3>前半页</h3>
            </div>
        </div>
    </div>
</div>
<div id="sentence_pool_front" class="quiz_ugc_font">
    <dt style="width: 16%;">正文: </dt>
    <dd style="margin-left: 20%; padding-bottom: 20px;">
        <% if(sentence_front.length == 0){ %><span class="text_gray_9" style="margin-left: 20px;">正文内容请通过下面的表单一段一段添加。</span><% } %>
        <% for(var i = 0; i < sentence_front.length; i++){ %>
            <% if(i >0 && sentence_front[i].paragraph != sentence_front[i - 1].paragraph){ %><br><% } %>
            <span class="sentence_item">
                <span class="sentence"><%= sentence_front[i].entext %></span>
                <span class="quiz_ugc_old">
                    <a href="javascript:void(0);" class="btn_mark btn_mark_small addSentencePlusBtn" style="padding: 0 4px;">
                        <i class="icon_vox icon_vox_blue icon_vox_344"></i>
                    </a>
                    <span class="quiz_ugc_ys_box buttons" style="display: none;">
                        <span href="javascript:void(0);" class="quiz_ugc_vcil_ys">
                            <a href="javascript:void(0);" data-sentenceid="<%= i %>" class="addFrontSentenceBtn" style="border-top: none;"><i class="icon_vox quiz_back_icon"></i>添一段</a>
                            <a href="javascript:void(0);" data-sentenceid="<%= i %>" class="editFrontSentenceBtn"><i class="icon_vox icon_vox_blue icon_vox_285"></i>编辑</a>
                            <a href="javascript:void(0);" data-sentenceid="<%= i %>" class="deleteFrontSentenceBtn"><i class="icon_general icon_general_48"></i>删除</a>
                        </span>
                        <i class="spaceAeeow"></i>
                    </span>
                </span>
            </span>
        <% } %>
    </dd>
</div>
<div id="sentence_editer_front" data-sentenceid="0" data-actiontype="add" <% if(sentence_front.length > 0){ %>style="display: none;"<% } %>>
    <dl class="horizontal_vox" style="margin-bottom: 20px;padding-left: 20px;">
        <dt style="margin-top:30px; "><span class="text_red">[必填] </span>添加英文：</dt>
        <dd>
            <textarea id="front_en_box" class="int_vox"  style="margin:0 10px; width: 500px!important; height: 78px;"></textarea>
        </dd>
    </dl>
    <dl class="horizontal_vox" style="margin-bottom: 20px;padding-left: 20px;">
        <dt style="margin-top:30px; "><span class="text_red">[必填] </span>添加译文：</dt>
        <dd>
            <textarea id="front_cn_box" class="int_vox"  style="margin:0 10px; width: 500px!important; height: 78px;"></textarea>
        </dd>
    </dl>
    <dl class="horizontal_vox" style="padding-left: 20px;">
        <dt>&nbsp;</dt>
        <dd>
            <div>
                <input type="checkbox" value="生成音频" checked="checked">生成音频
                <input type="checkbox" value="上传音频" >上传音频
                <div class="生成音频">
                    <div style="padding-bottom: 20px;">
                        角色：
                        <select class="lis_role w-int" style="width: 95px; height: 22px;">

                            <option value="m">美音男音</option>
                            <option value="f">美音女音</option>
                            <option value="c">美音童音</option>
                        </select>
                        音量：
                        <select class="lis_volume w-int" style="width: 90px; height: 22px;">
                            <option value="1">1</option>
                            <option value="2">2</option>
                            <option selected="" value="3">3</option>
                            <option value="4">4</option>
                            <option value="5">5</option>
                        </select>
                        语速：
                        <select class="lis_speed w-int" style="width: 90px; height: 22px;">
                            <option value="1">1</option>
                            <option value="2">2</option>
                            <option selected="" value="3">3</option>
                            <option value="4">4</option>
                            <option value="5">5</option>
                        </select>
                        <input class="lis_step1_play" type="button" value="生成">
                    </div>
                </div>
                <div class="上传音频" style="display: none;">
                    <input id="audio_upload_one_input" class="int_vox"style="margin:0 10px;">
                    <span id="audio_upload_one" data-userid="${currentUser.id}" data-uploadimageurl="${uploadfile!}" style="vertical-align: -16px;"></span>
                </div>
            </div>
        </dd>
    </dl>
    <div class="foot_ib">
        <a id="front_done_button" href="javascript:void(0);" style="padding: 0 45px" class="btn_mark btn_mark_well btn_mark_primary"><strong>确定添加</strong></a>
    </div>
</div>
<div class="line_show" style="margin: 0 0 25px 0;"></div>
<dl class="horizontal_vox" style="margin: 20px;padding-left: 10px;">
    <dt style="width: 14%;">本页重点词：</dt>
    <dd>
        <p class="select_info keyMaps keyMapsFrount" style="width: 467px;">
            <% for(var i = 0; i < frontKeywords.length; i++){ %>
                <span class="left keyMap" data-keymapindex="<%= i %>">
                    <input class="int_vox keyMap_enbox" style="margin:0 10px;" placeholder="英文单词" value="<%= frontKeywords[i].entext %>">
                    <input class="int_vox keyMap_cnbox" style="margin:0 10px;" placeholder="中文翻译" value="<%= frontKeywords[i].cntext %>">
                    <span data-keymapindex="<%= i %>" class="quiz_ugc_right_ys keyMapFrontMinus" <% if(frontKeywords.length == 1){ %>style="display: none;"<% } %>></span>
            </span>
            <% } %>
        </p>
        <i class="icon_vox icon_vox_blue icon_vox_344 keyMapFrontPlusBtn" style="cursor: pointer;"></i>
    </dd>
</dl>
<!--后半页-->
<div class="quizHeader" style="margin: 25px 0">
    <div class="show_block">
        <div class="inner">
            <div class="select_block">
                <h3>后半页</h3>
            </div>
            <div class="btn_block" style="padding-right:45px;">
                <span class="checkboxs"></span>
                设为最后页
            </div>
        </div>
    </div>
</div>
<div id="sentence_pool_back" class="quiz_ugc_font">
    <dt style="width: 16%;">正文: </dt>
    <dd style="margin-left: 20%; padding-bottom: 20px;">
        <% if(sentence_back.length == 0){ %><span class="text_gray_9" style="margin-left: 20px;">正文内容请通过下面的表单一段一段添加。</span><% } %>
        <% for(var i = 0; i < sentence_back.length; i++){ %>
            <% if(i >0 && sentence_back[i].paragraph != sentence_back[i - 1].paragraph){ %><br><% } %>
            <span class="sentence_item">
                <span class="sentence"><%= sentence_back[i].entext %></span>
                <span class="quiz_ugc_old">
                    <a href="javascript:void(0);" class="btn_mark btn_mark_small addSentencePlusBtn" style="padding: 0 4px;">
                        <i class="icon_vox icon_vox_blue icon_vox_344"></i>
                    </a>
                    <span class="quiz_ugc_ys_box buttons" style="display: none;">
                        <span href="javascript:void(0);" class="quiz_ugc_vcil_ys">
                            <a href="javascript:void(0);" data-sentenceid="<%= i %>" class="addBackSentenceBtn" style="border-top: none;"><i class="icon_vox quiz_back_icon"></i>添一段</a>
                            <a href="javascript:void(0);" data-sentenceid="<%= i %>" class="editBackSentenceBtn"><i class="icon_vox icon_vox_blue icon_vox_285"></i>编辑</a>
                            <a href="javascript:void(0);" data-sentenceid="<%= i %>" class="deleteBackSentenceBtn"><i class="icon_general icon_general_48"></i>删除</a>
                        </span>
                        <i class="spaceAeeow"></i>
                    </span>
                </span>
            </span>
        <% } %>
    </dd>
</div>
<div id="sentence_editer_back" data-sentenceid="0" data-actiontype="add" <% if(sentence_back.length > 0){ %>style="display: none;"<% } %>>
    <dl class="horizontal_vox" style="margin-bottom: 20px;padding-left: 20px;">
        <dt style="margin-top:30px; "><span class="text_red">[必填] </span>添加英文：</dt>
        <dd>
            <textarea id="back_en_box" class="int_vox"  style="margin:0 10px; width: 500px!important; height: 78px;"></textarea>
        </dd>
    </dl>
    <dl class="horizontal_vox" style="margin-bottom: 20px;padding-left: 20px;">
        <dt style="margin-top:30px; "><span class="text_red">[必填] </span>添加译文：</dt>
        <dd>
            <textarea id="back_cn_box" class="int_vox"  style="margin:0 10px; width: 500px!important; height: 78px;"></textarea>
        </dd>
    </dl>
    <dl class="horizontal_vox" style="padding-left: 20px;">
        <dt>&nbsp;</dt>
        <dd>
            <div>
                <input type="checkbox" value="生成音频" checked="checked">生成音频
                <input type="checkbox" value="上传音频" >上传音频
                <div class="生成音频">
                    <div style="padding-bottom: 20px;">
                        角色：
                        <select class="lis_role w-int" style="width: 95px; height: 22px;">

                            <option value="m">美音男音</option>
                            <option value="f">美音女音</option>
                            <option value="c">美音童音</option>
                        </select>
                        音量：
                        <select class="lis_volume w-int" style="width: 90px; height: 22px;">
                            <option value="1">1</option>
                            <option value="2">2</option>
                            <option selected="" value="3">3</option>
                            <option value="4">4</option>
                            <option value="5">5</option>
                        </select>
                        语速：
                        <select class="lis_speed w-int" style="width: 90px; height: 22px;">
                            <option value="1">1</option>
                            <option value="2">2</option>
                            <option selected="" value="3">3</option>
                            <option value="4">4</option>
                            <option value="5">5</option>
                        </select>
                        <input class="lis_step1_play" type="button" value="生成" data-callback-fn="audio_upload_two_callback">
                    </div>
                </div>
                <div class="上传音频" style="display: none;">
                    <input id="audio_upload_two_input" class="int_vox"style="margin:0 10px;">
                    <span id="audio_upload_two" data-userid="${currentUser.id}" data-uploadimageurl="${uploadfile!}" style="vertical-align: -16px;"></span>
                </div>
            </div>
        </dd>
    </dl>
    <div class="foot_ib">
        <a id="back_done_button" href="javascript:void(0);" class="btn_mark btn_mark_well btn_mark_primary"  style="padding: 0 45px"><strong>确定添加</strong></a>
    </div>
</div>
<div class="line_show" style="margin: 0 0 25px 0;"></div>
<dl class="horizontal_vox" style="margin: 20px;padding-left:10px;">
    <dt style="width: 14%;">本页重点词：</dt>
    <dd>
        <p class="select_info keyMaps keyMapsBack" style="width: 467px;">
            <% for(var i = 0; i < backKeywords.length; i++){ %>
                <span class="left keyMap" data-keymapindex="<%= i %>">
                    <input class="int_vox keyMap_enbox" style="margin:0 10px;" placeholder="英文单词" value="<%= backKeywords[i].entext %>">
                    <input class="int_vox keyMap_cnbox" style="margin:0 10px;" placeholder="中文翻译" value="<%= backKeywords[i].cntext %>">
                    <span data-keymapindex="<%= i %>" class="quiz_ugc_right_ys keyMapBackMinus" <% if(backKeywords.length == 1){ %>style="display: none;"<% } %>></span>
            </span>
            <% } %>
        </p>
        <i class="icon_vox icon_vox_blue icon_vox_344 keyMapBackPlusBtn" style="cursor: pointer;"></i>
    </dd>
</dl>