<#assign env = "student_ugc">
<#include "../../common/config.ftl">

<#import '../../common/layout.ftl' as layout>
<#assign layout = layout>

<#assign headBlock>
    ${buildLoadStaticFileTag(CONFIG.SKINCSSPATH, "css")}
</#assign>

<#function _buildQuestions questions type>

    <#assign questionsHtml>
        <#list questions![] as question>
            <#if type == "MULTISELECT">
                <div class="do_mock_checkbox">${question}</div>
            <#elseif type == "SELECT">
                <option>${question}</option>
            </#if>
        </#list>
    </#assign>

    <#return questionsHtml?trim>

</#function>

<#function _buildInputTag questionObj>

	<#local inputClassName = "short"  questionType = "INPUT">
	<#if ((questionObj.questionName!"")?index_of("#LONG_INPUT#") > -1)>
		<#local inputClassName = "long"  questionType = "LONG_INPUT">
	</#if>

	<#return (questionObj.questionName!"")?string?replace("#${questionType}#", '<input type="text" value class="${inputClassName}Text">')?trim>

</#function>

<#function buildHtmlByQuestion questionObj index>

	<#assign questionType = questionObj.questionType?string?upper_case>

    <#return {
        "INPUT" : _buildInputTag(questionObj),
        "MULTISELECT" : (questionObj.questionName!"")?string?replace("#${questionType}#", '<div class="dataCollect-module do_multiselect">${_buildQuestions(questionObj.options![], "MULTISELECT")}</div>'),
        "SELECT" : (questionObj.questionName!"")?replace(
                "#${questionType}#",
                '<div class="input"><span></span><select><option value="">请选择</option>${_buildQuestions(questionObj.options![], "SELECT")}</select></div>'
            )

    }[questionType]!''>

</#function>

<#assign bottomBlock>
    <script>
		"use strict";

        (function(){
			var toArray = function(mock_arr){
					return mock_arr ? Array.prototype.slice.call(mock_arr) : [];
				},
				send_post_ajax = function(url, json, callback){
					var xhr = new window.XMLHttpRequest();
					xhr.open("POST", url);
					xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
					xhr.send(JSON.stringify(json));
					xhr.onreadystatechange = function () {
						if (xhr.readyState === 4 && xhr.status === 200) {
							callback(JSON.parse(xhr.responseText));
						}
					};
				};

			<#-- 辅助的一个选择器 -->
			var single_callback = function(dom, is_exists_callback){
					if(dom === null){
						return;
					}

					is_exists_callback(dom);
				},
				_$ = function(selector, is_exists_callback, target){
                    target || (target = document);
					single_callback(target.querySelector(selector), is_exists_callback);
				},
				_$all = function(selector, repeat_callback, target){
					target || (target = document);
					toArray(target.querySelectorAll(selector)).forEach(function(dom){
						single_callback(dom, repeat_callback);
					});
				};

			<#-- mock checkbox action -->
			_$all(".do_multiselect", function(dom){
				dom.addEventListener('click',function(e){
					var targetDom = e.target;

					if(targetDom && targetDom.nodeName.toUpperCase()==="DIV" && targetDom.classList.contains("do_mock_checkbox")){
						targetDom.classList.toggle('active');
					}

				}, false);
			});

			<#-- mock select action -->
			_$all("select", function(select){
				select.onchange = function(){
					var span = select.parentNode.querySelector('span');
					span && (span.textContent = select.value);
				};
			});

			_$(".doSubmit", function(dom){

				dom.onclick = function(){

                    var answer_obj_list = [], isvalidated = true;

                    _$all(".do_questions", function(dom){

						if(isvalidated === false){
							return ;
						}

                        var values = [];

                        _$all("input, select, .active", function(form_element){
                            values.push( ("value" in form_element ? form_element.value : form_element.textContent).trim());
                        }, dom);
                        values = values.join('#');

						if(!values){
							isvalidated = false;
							return ;
						}

                        answer_obj_list.push(
                            {
                                questionId : dom.dataset.pid,
                                answer : values
                            }
                        );

                    });

					if(isvalidated === false){
						window.alert("还有问题没有填写哦！");
						return ;
					}

					var record_id;
					_$('input[name="recordId"]', function(dom){
						record_id = dom.value;
					});

					send_post_ajax(
						"/studentMobile/center/saveugcanswer.vpage",
						{
							recordId  : record_id,
							answerMapList : answer_obj_list
						},
						function(res){
							if(res.success){
								window.location.reload(true);
								return ;
							}

							window.alert(res.info);
						}
					);
				};

			});

        })();
    </script>
</#assign>
