<#import "/common/config.ftl" as app>
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:Lesson xmlns:ns2="http://jaxb.vlesson.vlt.com">
<#if lesson?exists>
<lessonId>${lesson.id}</lessonId>
<lessonName>${lesson.ename}</lessonName>
<hasDialog>${lesson.dialog?string}</hasDialog>
<lessonImg><@app.link href="${lesson.imgUrl!''}"/></lessonImg>
<load_swf><@app.link href="resources/apps/flash/swf/" /></load_swf >
<farther_deal_with>/flash/study/fartherParse.vpage</farther_deal_with>
<save_history>/flash/study/saveHistory.vpage</save_history>
<load_configure>/flash/study/getPlayerConfig.vpage</load_configure>
<save_configure>/flash/study/savePlayerConfig.vpage</save_configure>
<query_rank>/flash/study/queryArenaRank.vpage</query_rank>
<upload_data>/flash/study/uploadArenaData.vpage</upload_data>
<query_challenge_rank>/flash/study/queryLessonRank.vpage</query_challenge_rank>
<upload_challenge_sound>/flash/study/uploadLessonData.vpage</upload_challenge_sound>
<upload_score>/flash/study/uploadLessonScore.vpage</upload_score>
<getPhonetic>/flash/VoxPronounceStudy/getphonet.vpage</getPhonetic>
<cdn_upload>/uploadfile/voice.vpage</cdn_upload>
<sentenceWord>
<#list sentences as sent>
<sentence>
	<sentenceId>${sent.id}</sentenceId>
	<dialogRole>${sent.dialogRole}</dialogRole>
	<isWordFlag>false</isWordFlag>
	<!--晨读增加(单句练习)是否调用声音文件  wuyuefeng-->
	<isWordWave>${sent.type}</isWordWave>
	<startWave><@app.link href="/wave/Good_morning_china/startFollow.wav"/></startWave>
	<repeatWave><@app.link href="/wave/Good_morning_china/startRepeat.wav"/></repeatWave>
	<sentenceImg><@app.link href="${sent.sentenceImg?default('')}"/></sentenceImg>
	<Data>
		<language>CHN</language>
		<text>${sent.cnText?default('')}</text>
		<wavePlayTime>0</wavePlayTime>
		<slowWavePlayTime>0</slowWavePlayTime>
		<syllableNum>0</syllableNum>
	</Data>
	<Data>
		<language>ENG</language>
		<text>${sent.enText}</text>
		<keyWord>${sent.keyWord?default('')}</keyWord>
		<waveFileLocator><@app.wave href="${sent.waveUri}"/></waveFileLocator>
		<wavePlayTime>${sent.wavePlayTime}</wavePlayTime>
		<slowWaveFileLocator><@app.wave href="${sent.slowWaveUri!''}"/></slowWaveFileLocator>
		<slowWavePlayTime>${sent.slowWavePlayTime!0}</slowWavePlayTime>
		<metadata>${sent.metadata!''}</metadata>
		<syllableNum>${sent.syllableNum!0}</syllableNum>
	</Data>
	<#if sent.sentenceWordMapper?exists>
	<WordTest>
		  <tempEnText>${sent.sentenceWordMapper.enText}</tempEnText>
		  <tempCnText>${sent.sentenceWordMapper.cnText}</tempCnText>
		  <tempSlowUrl><@app.wave href="${sent.sentenceWordMapper.slowUrl}"/></tempSlowUrl>
		  <tempQuickUrl><@app.wave href="${sent.sentenceWordMapper.quickUrl}"/></tempQuickUrl>
		  <tempPicUrl><@app.link href="${sent.sentenceWordMapper.picUrl}"/></tempPicUrl>
		  <scoreText>${sent.sentenceWordMapper.scoreText}</scoreText>
		  <breakPoint>${sent.sentenceWordMapper.breakPoint}</breakPoint>
	</WordTest>
	<#else>
	<WordTest>null</WordTest>
	</#if>
	
</sentence>
</#list>
</sentenceWord>
<sentence>
<#list sentences1 as sent>
<sentence>
	<sentenceId>${sent.id}</sentenceId>
	<dialogRole>${sent.dialogRole}</dialogRole>
	<isWordFlag>false</isWordFlag>
	<!--晨读增加(单句练习)是否调用声音文件  wuyuefeng-->
	<isWordWave>${sent.type}</isWordWave>
	<startWave><@app.wave href="/wave/Good_morning_china/startFollow.wav"/></startWave>
	<repeatWave><@app.wave href="/wave/Good_morning_china/startRepeat.wav"/></repeatWave>
	<sentenceImg><@app.link href="${sent.sentenceImg?default('')}"/></sentenceImg>
	<Data>
		<language>CHN</language>
		<text>${sent.cnText?default('')}</text>
		<wavePlayTime>0</wavePlayTime>
		<slowWavePlayTime>0</slowWavePlayTime>
		<syllableNum>0</syllableNum>
	</Data>
	<Data>
		<language>ENG</language>
		<text>${sent.enText}</text>
		<keyWord>${sent.keyWord?default('')}</keyWord>
		<waveFileLocator><@app.wave href="${sent.waveUri}"/></waveFileLocator>
		<wavePlayTime>${sent.wavePlayTime}</wavePlayTime>
		<slowWaveFileLocator><@app.wave href="${sent.slowWaveUri!''}"/></slowWaveFileLocator>
		<slowWavePlayTime>${sent.slowWavePlayTime!0}</slowWavePlayTime>
		<metadata>${sent.metadata!''}</metadata>
		<syllableNum>${sent.syllableNum!0}</syllableNum>
	</Data>	
	
</sentence>
</#list>
</sentence>
<#else>
<lessonId>null</lessonId>
</#if>
</ns2:Lesson>