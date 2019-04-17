package com.voxlearning.washington.service.tts;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.business.api.mapper.TtsListeningTag;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Junjie Zhang
 * @since 2016-03-17
 */
public class TtsXmlParser {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Stack<String> role = new Stack<>();
    private Stack<Integer> speed = new Stack<>();
    private Stack<Integer> volume = new Stack<>();
    private Stack<Integer> loop = new Stack<>();
    private int isMute = 0;
    private String xml;
    private List<TtsListeningTag> tagList = new ArrayList<>();

    // 文件名放在这里，方便以后修改配置
    //"Spring-30秒","Spring-60秒","Summer-30秒","Summer-60秒","瓦妮莎的微笑-30秒","瓦妮莎的微笑-60秒",
    // "夜的钢琴曲五-30秒","夜的钢琴曲五-60秒","致爱丽丝-30秒","致爱丽丝-60秒"
    // "Spring-15秒","Summer-15秒","致爱丽丝-15秒","夜的钢琴曲-15秒","瓦妮莎的微笑-15秒"
    private final static String[] MUSIC_ADDR = {"546eb82cce23505c08000068", "546eb82dce23505c08000070", "546eb82dce23505c08000075",
            "546eb82dce23505c08000079", "546eb82ece23505c0800007d", "546eb82fce23505c08000081", "546eb830ce23505c08000085",
            "546eb830ce23505c0800008a", "546eb831ce23505c0800008f", "546eb82dce23505c0800006c", "56fe22c2a3de9f26e4f24aff",
            "56fe22c8a3de9f26e4f24b02", "56fe22cea3de9f26e4f24b05", "56fe22d4a3de9f26e4f24b08", "56fe22daa3de9f26e4f24b0b"};
    private final static Float[] MUSIC_DURATION = {33.62f, 65.48f, 30.47f, 60.53f, 30.19f, 60.52f, 31.16f, 98.42f, 31.22f, 61.61f,
            15.19f, 15.98f, 15.15f, 17.28f, 15.23f};

    //"dang1","dang2","dang3"
    private final static String[] VOICE_ADDR = {"57551cc7a3de9f66e82f8969", "57551cc7a3de9f66e82f896c", "57551cc7a3de9f66e82f896f"};

    private final static Float[] VOICE_DURATION = {0.88f, 1.30f, 1.18f};

    //"上课铃声","电话铃声","汽车喇叭","火车鸣笛","敲门声","下雨声","打雷声",
    // "刮风声","狗叫声","猫叫声","鸟叫声","山羊叫声","丛林鸟叫","孩子笑声","孩子吵闹声","街道嘈杂声"
    private final static String[] SOUND_ADDR = {"546eb832ce23505c08000093", "546eb832ce23505c080000b3", "546eb833ce23505c080000b7",
            "546eb833ce23505c080000bb", "546eb833ce23505c080000bf", "546eb833ce23505c080000c3",
            "546eb833ce23505c080000c7", "546eb833ce23505c080000cb", "546eb833ce23505c080000cf",
            "546eb832ce23505c08000097", "546eb832ce23505c0800009b", "546eb832ce23505c0800009f",
            "546eb832ce23505c080000a3", "546eb832ce23505c080000a7", "546eb832ce23505c080000ab",
            "546eb832ce23505c080000af"};
    private final static Float[] SOUND_DURATION = {3.18f, 4.62f, 1.74f, 2.96f, 2.10f, 3.61f, 3.47f, 4.41f, 2.32f, 2.25f,
            2.81f, 2.53f, 3.15f, 3.25f, 3.54f, 4.41f};


    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\u4e00-\u9fa5]");
    private static final Pattern ENGLISH_PATTERN = Pattern.compile("[a-zA-Z]");

    //中文(包含全角标点符号)，IVONA不支持字符
    private static final Pattern INVONA_CHINESE_PATTERN = Pattern.compile("[\u0080-\uffff]+");

    public TtsXmlParser(String xml) {
        this.xml = xml;
    }

    /**
     * 全角空格为12288，半角空格为32
     * 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
     * <p>
     * 将字符串中的全角字符转为半角
     *
     * @param src 要转换的包含全角的任意字符串
     * @return 转换之后的字符串
     */
    public static String toSemiangle(String src) {
        if (src == null)
            return src;
        char[] c = src.toCharArray();
        for (int index = 0; index < c.length; index++) {
            if (c[index] == 12288) {// 全角空格
                c[index] = (char) 32;
            } else if (c[index] > 65280 && c[index] < 65375) {// 其他全角字符
                c[index] = (char) (c[index] - 65248);
            } else {
                switch (c[index]) {
                    case '“':
                    case '”':
                        c[index] = '"';
                        break;
                    case '。':
                        c[index] = '.';
                        break;
                    case '‘':
                    case '’':
                        c[index] = '\'';
                        break;
                    case '、':
                        c[index] = ',';
                        break;
                    default:
                        if (c[index] > 127) {
                            //非ascii字符转换为空格
                            c[index] = (char) 32;
                        }
                        break;
                }
            }
        }
        return String.valueOf(c);
    }

    private void pushOrPopString(Stack<String> stack, String value, String imgId) throws Exception {
        if (StringUtils.endsWith(imgId, "_S")) {
            stack.push(value);
        } else if (StringUtils.endsWith(imgId, "_E")) {
            String old = stack.pop();
            if (old == null || !old.equals(value))
                throw new Exception("您设置的标签不匹配，请调整");
        }
    }

    private void pushOrPopInteger(Stack<Integer> stack, Integer value, String imgId, Integer maxValue) throws Exception {
        if (value == null) {
            throw new Exception("您设置的标签错误，请调整");
        }
        if (StringUtils.endsWith(imgId, "_S")) {
            if (value <= 0)
                throw new Exception("您设置的标签错误，值不能小于等于0，请调整");
            if (value > maxValue)
                throw new Exception("您设置的标签错误，值不能大于" + maxValue + "，请调整");
            stack.push(value);
        } else if (StringUtils.endsWith(imgId, "_E")) {
            Integer old = stack.pop();
            if (old == null || !old.equals(value))
                throw new Exception("您设置的标签不匹配，请调整");
        }
    }

    private void parseRoleTag(String imgId) throws Exception {
        String tag = "";
        if (StringUtils.startsWith(imgId, "ROLE_CN_M")) {
            tag = "pm";
        } else if (StringUtils.startsWith(imgId, "ROLE_CN_N")) {
            tag = "p";
        } else if (StringUtils.startsWith(imgId, "ROLE_UN_M")) {
            tag = "m";
        } else if (StringUtils.startsWith(imgId, "ROLE_UN_N")) {
            tag = "f";
        } else if (StringUtils.startsWith(imgId, "ROLE_UN_C")) {
            tag = "c";
        } else if (StringUtils.startsWith(imgId, "ROLE_EN_M")) {
            tag = "bm";
        } else if (StringUtils.startsWith(imgId, "ROLE_EN_N")) {
            tag = "bf";
        }

        pushOrPopString(role, tag, imgId);
    }

    private void parseSpeedTag(String imgId) throws Exception {
        String temp = imgId.replace("SPEED_", "");
        if (temp.length() < 2)
            return;
        Integer value = Integer.parseInt(temp.substring(0, temp.length() - 2));
        pushOrPopInteger(speed, value, imgId, 5);

    }

    private void parseVolumeTag(String imgId) throws Exception {
        String temp = imgId.replace("VOLUME_", "");
        if (temp.length() < 2)
            return;
        Integer value = Integer.parseInt(temp.substring(0, temp.length() - 2));
        pushOrPopInteger(volume, value, imgId, 5);
    }

    private void parseLoopTag(String imgId) throws Exception {
        String temp = imgId.replace("LOOP_", "");
        if (temp.length() < 2)
            return;
        Integer value = Integer.parseInt(temp.substring(0, temp.length() - 2));
        pushOrPopInteger(loop, value, imgId, 3);
        tagList.add(TtsListeningTag.getLoopTag(value));
    }

    private void parsePauseTag(String imgId) throws Exception {
        String temp = imgId.replace("PAUSE", "");
        Integer value = Integer.parseInt(temp);
        if (value < 0 || value > 10) {
            throw new Exception("您设置的停顿标签错误，请调整");
        }
        if (value == 0) {
            // PAUSE0表示0.5秒
            tagList.add(TtsListeningTag.getPauseTag(0.5f));
        } else {
            tagList.add(TtsListeningTag.getPauseTag(Float.valueOf(value)));
        }
    }

    private void parseMusicTag(String imgId) throws Exception {
        String temp = imgId.replace("MUSIC", "");
        int value = Integer.parseInt(temp);
        if (value < 1 || value > MUSIC_ADDR.length) {
            throw new Exception("您设置的音乐标签错误，请调整");
        }
        tagList.add(TtsListeningTag.getSentenceTag(MUSIC_ADDR[value - 1], MUSIC_DURATION[value - 1]));
    }

    private void parseVoiceTag(String imgId) throws Exception {
        String temp = imgId.replace("VOICE", "");
        int value = Integer.parseInt(temp);
        if (value < 1 || value > VOICE_ADDR.length) {
            throw new Exception("您设置的提示音标签错误，请调整");
        }
        tagList.add(TtsListeningTag.getSentenceTag(VOICE_ADDR[value - 1], VOICE_DURATION[value - 1]));
    }

    private void parsePhoneTag(String imgId) throws Exception {
        String temp = imgId.replace("VOWELS", "").replace("M", "").replace("F", "");

        int value = Integer.parseInt(temp);
        //男音在后，女音在前
        if (imgId.contains("M"))
            value += PHONE_ADDR.length / 2;
        if (value < 1 || value > PHONE_ADDR.length) {
            throw new Exception("您设置的音标标签错误，请调整");
        }
        tagList.add(TtsListeningTag.getSentenceTag(PHONE_ADDR[value - 1], 0.5f));
    }

    private void parseMuteTag(String imgId) throws Exception {
        if ("MUTE_S".equals(imgId)) {
            isMute++;
        } else if ("MUTE_E".equals(imgId)) {
            if (isMute > 0) {
                isMute--;
            }
        }
    }

    private void parseSoundTag(String imgId) throws Exception {
        String temp = imgId.replace("SOUND", "");
        int value = Integer.parseInt(temp);
        if (value < 1 || value > SOUND_ADDR.length) {
            throw new Exception("您设置的音效标签错误，请调整");
        }
        tagList.add(TtsListeningTag.getSentenceTag(SOUND_ADDR[value - 1], SOUND_DURATION[value - 1]));
    }

    private void parseTag(String imgId) throws Exception {
        if (imgId == null)
            return;
        if (StringUtils.contains(imgId, "ROLE")) {
            parseRoleTag(imgId);
        } else if (StringUtils.contains(imgId, "SPEED")) {
            parseSpeedTag(imgId);
        } else if (StringUtils.contains(imgId, "VOLUME")) {
            parseVolumeTag(imgId);
        } else if (StringUtils.contains(imgId, "LOOP")) {
            parseLoopTag(imgId);
        } else if (StringUtils.contains(imgId, "MUSIC")) {
            parseMusicTag(imgId);
        } else if (StringUtils.contains(imgId, "VOICE")) {
            parseVoiceTag(imgId);
        } else if (StringUtils.contains(imgId, "SOUND")) {
            parseSoundTag(imgId);
        } else if (StringUtils.contains(imgId, "PAUSE")) {
            parsePauseTag(imgId);
        } else if (StringUtils.contains(imgId, "MUTE")) {
            parseMuteTag(imgId);
        } else if (StringUtils.contains(imgId, "VOWELS")) {
            parsePhoneTag(imgId);
        }

    }

    private int countChineseChars(String str) {
        if (StringUtils.isEmpty(str))
            return 0;
        Matcher matcher = CHINESE_PATTERN.matcher(str);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private int countEnglishChars(String str) {
        if (StringUtils.isEmpty(str))
            return 0;
        Matcher matcher = ENGLISH_PATTERN.matcher(str);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    public String translate() {

        try {
            if (StringUtils.isBlank(xml))
                return "";
            xml = xml.replace("\u000B", " ").replace("\u200B", " ");  //过滤特殊字符
            xml = xml.replace("<p>", "").replace("</p>", "");
            xml = xml.replace("<tab/>", "");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new InputSource(new StringReader(xml)));
            NodeList nodeList;
            if (document.getElementsByTagName("TextFlow").getLength() == 0) {
                nodeList = document.getChildNodes();
            } else {
                nodeList = document.getElementsByTagName("TextFlow").item(0).getChildNodes();
            }
            if (nodeList == null)
                return "";
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeName().equals("img")) {
                    String id = ((Element) node).getAttribute("id");
                    //跳过静音
                    if (isMute > 0 && !"MUTE_E".equals(id) && !"MUTE_S".equals(id))
                        continue;
                    parseTag(id);
                } else if (node.getNodeName().equals("span")) {
                    //跳过静音
                    if (isMute > 0)
                        continue;
                    String text = node.getTextContent();
                    if (StringUtils.isBlank(text))
                        continue;
                    String roleValue;
                    Integer speedValue = 3, volumeValue = 3;
                    if (!role.isEmpty())
                        roleValue = role.peek();
                    else {
                        if (CHINESE_PATTERN.matcher(text).find()) {
                            //中文女音
                            roleValue = "p";
                        } else {
                            //美音女音
                            roleValue = "f";
                        }
                    }
                    if (!speed.isEmpty())
                        speedValue = speed.peek();
                    if (!volume.isEmpty())
                        volumeValue = volume.peek();

                    if (roleValue.equals("p") || roleValue.equals("pm")) {
                        //中文不能超过100个字
                        if (countChineseChars(text) > 100)
                            return "您设置的内容过多，请将内容拆分后重新设置，中文不能超过100汉字";
                    } else {
                        text = toSemiangle(text);
                        //英文不能超过1000个字
                        if (countEnglishChars(text) > 1000)
                            return "您设置的内容过多，请将内容拆分后重新设置，英文不能超过1000字母";

                    }
                    tagList.add(TtsListeningTag.getSentenceTag(roleValue, volumeValue, speedValue, text));
                }
            }
        } catch (Exception e) {
            return "您设置的标签错误，请调整";
        }

        return "";
    }

    public List<TtsListeningTag> getTagList() {
        return tagList;
    }

    //男女音 共 96 个音素 先女音，后男音
    private final static String[] PHONE_ADDR = {
            "5a7c155da3de9f7be055e964", "5a7c1582a3de9f7be055e96a", "5a7c15a8a3de9f7be055e970", "5a7c15cba3de9f7be055e976",
            "5a7c15efa3de9f7be055e97c", "5a7c1612a3de9f7be055e982", "5a7c1636a3de9f7be055e988", "5a7c165aa3de9f7be055e98e",
            "5a7c167da3de9f7be055e994", "5a7c16a0a3de9f7be055e99a", "5a7c16c5a3de9f7be055e9a0", "5a7c16e9a3de9f7be055e9a6",
            "5a7c170ea3de9f7be055e9ac", "5a7c1731a3de9f7be055e9b2", "5a7c1756a3de9f7be055e9b8", "5a7c177aa3de9f7be055e9be",
            "5a7c179ea3de9f7be055e9c4", "5a7c17c1a3de9f7be055e9ca", "5a7c17e7a3de9f7be055e9d0", "5a7c180ba3de9f7be055e9d6",
            "5a7c182ea3de9f7be055e9dc", "5a7c1852a3de9f7be055e9e2", "5a7c1876a3de9f7be055e9e8", "5a7c1899a3de9f7be055e9ee",
            "5a7c18bca3de9f7be055e9f4", "5a7c18e0a3de9f7be055e9fa", "5a7c1904a3de9f7be055ea00", "5a7c1928a3de9f7be055ea06",
            "5a7c194da3de9f7be055ea0c", "5a7c1972a3de9f7be055ea12", "5a7c1997a3de9f7be055ea18", "5a7c19bca3de9f7be055ea1e",
            "5a7c19e0a3de9f7be055ea24", "5a7c1a05a3de9f7be055ea2a", "5a7c1a29a3de9f7be055ea30", "5a7c1a4ea3de9f7be055ea36",
            "5a7c1a72a3de9f7be055ea3c", "5a7c1a97a3de9f7be055ea42", "5a7c1abda3de9f7be055ea48", "5a7c1ae2a3de9f7be055ea4e",
            "5a7c1b06a3de9f7be055ea54", "5a7c1b26a3de9f7be055ea5a", "5a7c1b48a3de9f7be055ea60", "5a7c1b6aa3de9f7be055ea66",
            "5a7c1b8da3de9f7be055ea6c", "5a7c1bafa3de9f7be055ea72", "5a7c1bd1a3de9f7be055ea78", "5a7c1bf4a3de9f7be055ea7e",
            "5a7c0e7da3de9f7be055e844", "5a7c0ea2a3de9f7be055e84a", "5a7c0ec6a3de9f7be055e850", "5a7c0eeaa3de9f7be055e856",
            "5a7c0f0fa3de9f7be055e85c", "5a7c0f33a3de9f7be055e862", "5a7c0f59a3de9f7be055e868", "5a7c0f7ea3de9f7be055e86e",
            "5a7c0fa2a3de9f7be055e874", "5a7c0fc7a3de9f7be055e87a", "5a7c0feda3de9f7be055e880", "5a7c1012a3de9f7be055e886",
            "5a7c1037a3de9f7be055e88c", "5a7c105da3de9f7be055e892", "5a7c1081a3de9f7be055e898", "5a7c10a5a3de9f7be055e89e",
            "5a7c10c9a3de9f7be055e8a4", "5a7c10eea3de9f7be055e8aa", "5a7c1111a3de9f7be055e8b0", "5a7c1136a3de9f7be055e8b6",
            "5a7c115aa3de9f7be055e8bc", "5a7c117fa3de9f7be055e8c2", "5a7c11a5a3de9f7be055e8c8", "5a7c11c9a3de9f7be055e8ce",
            "5a7c11eea3de9f7be055e8d4", "5a7c1215a3de9f7be055e8da", "5a7c1239a3de9f7be055e8e0", "5a7c125ea3de9f7be055e8e6",
            "5a7c1284a3de9f7be055e8ec", "5a7c12a9a3de9f7be055e8f2", "5a7c12cca3de9f7be055e8f8", "5a7c12f0a3de9f7be055e8fe",
            "5a7c1314a3de9f7be055e904", "5a7c1339a3de9f7be055e90a", "5a7c135da3de9f7be055e910", "5a7c1380a3de9f7be055e916",
            "5a7c13a3a3de9f7be055e91c", "5a7c13c9a3de9f7be055e922", "5a7c13eca3de9f7be055e928", "5a7c1411a3de9f7be055e92e",
            "5a7c1436a3de9f7be055e934", "5a7c145ba3de9f7be055e93a", "5a7c1480a3de9f7be055e940", "5a7c14a5a3de9f7be055e946",
            "5a7c14c9a3de9f7be055e94c", "5a7c14eca3de9f7be055e952", "5a7c1512a3de9f7be055e958", "5a7c1538a3de9f7be055e95e"
    };
}
