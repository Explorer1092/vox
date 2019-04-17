package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.galaxy.service.tobbit.api.TobbitCRMLoader;
import com.voxlearning.galaxy.service.tobbit.api.TobbitCRMService;
import com.voxlearning.galaxy.service.tobbit.api.constant.ExerciseViewType;
import com.voxlearning.galaxy.service.tobbit.api.constant.GameType;
import com.voxlearning.galaxy.service.tobbit.api.entity.TobbitCourse;
import com.voxlearning.galaxy.service.tobbit.api.entity.TobbitGameMapping;
import com.voxlearning.galaxy.service.tobbit.api.support.game.*;
import com.voxlearning.galaxy.service.tobbit.api.support.vo.*;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping(value = "/opmanager/tobbit")
public class TobbitCRMController extends OpManagerAbstractController {

    @ImportService(interfaceClass = TobbitCRMLoader.class)
    private TobbitCRMLoader tobbitCRMLoader;

    @ImportService(interfaceClass = TobbitCRMService.class)
    private TobbitCRMService tobbitCRMService;

    @RequestMapping(value = "/courses.vpage", method = RequestMethod.GET)
    public String courses(Model model) {

        model.addAttribute("subjects", Subject.values()).addAttribute("levels", ClazzLevel.values());

        Subject subject = Subject.safeParse(getRequestString("subject"));
        ClazzLevel level = ClazzLevel.parse(getRequestInt("clazzLevel", 0));
        Boolean trail = requestTrail();
        String name = getRequestString("name");
        int page = getRequestInt("page", 1);
        int size = 20;

        PageRequest request = new PageRequest(page - 1, size);

        Page<TobbitCourse> coursePage = tobbitCRMLoader.findCourse(subject, level, trail, name, request);

        String url = "?name=" + requestString("name", "") + "&trail=" + requestString("trail", "") + "&subject=" + requestString("subject", "") + "&clazzLevel=" + requestString("clazzLevel", "") + "&page=";

        List<TobbitCourse> courses = coursePage.getContent();
        List<Map<String, Object>> list = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(courses)) {
            for (TobbitCourse course : courses) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", course.getId());
                map.put("name", course.getName());
                map.put("sequence", course.getSequence());
                map.put("subject", course.getSubject() == null ? "" : course.getSubject().getValue());
                map.put("level", course.getClazzLevel() == null ? "" : course.getClazzLevel().getDescription());
                map.put("trail", course.getTrial() != null && course.getTrial() ? "是" : "否");
                map.put("online", course.checkOnlineStatus()?"上线":"下线");

                List<TobbitGame> games = tobbitCRMLoader.loadTobbitGame(course.getId());
                if (CollectionUtils.isNotEmpty(games)) {
                    map.put("game", games.get(0).getGameType());
                } else {
                    map.put("game", "");
                }
                list.add(map);
            }
        }


        int pageIndex = getRequestInt("pageIndex", 1);
        if (pageIndex <= 0) {
            pageIndex = 1;
        }

        int start = ((pageIndex - 1) / 10) * 10 + 1;
        int end = ((pageIndex - 1) / 10 + 1) * 10;

        model.addAttribute("query", url)
                .addAttribute("subject", getRequestString("subject"))
                .addAttribute("level", getRequestString("clazzLevel"))
                .addAttribute("trail", getRequestString("trail"))
                .addAttribute("name", getRequestString("name"))
                .addAttribute("pageIndex", page)
                .addAttribute("pageCount", coursePage.getTotalPages())
                .addAttribute("courses", list)
                .addAttribute("pageCount", coursePage.getTotalPages())
                .addAttribute("start", start)
                .addAttribute("end", end)
                .addAttribute("games", GameType.values());

        if (pageIndex > (coursePage.getTotalPages() / 10) * 10) {
            model.addAttribute("end", coursePage.getTotalPages());
        }

        return "opmanager/tobbit/courses";
    }

    @RequestMapping("/editor.vpage")
    public String courseEditor(Model model) {

        model.addAttribute("subjects", Subject.values())
                .addAttribute("levels", ClazzLevel.values());

        String id = getRequestString("id");
        TobbitCourse course = null;
        if (StringUtils.isNotBlank(id)) {
            course = tobbitCRMLoader.loadCourse(id);
        }
        if (null != course) {
            model.addAttribute("id", id)
                    .addAttribute("name", course.getName())
                    .addAttribute("level", course.getClazzLevel() == null ? "0" : course.getClazzLevel().getLevel())
                    .addAttribute("subject", course.getSubject())
                    .addAttribute("trial", course.getTrial())
                    .addAttribute("convert", course.getConvert())
                    .addAttribute("convertGirl", course.getConvertGirl())
                    .addAttribute("sequence", course.getSequence())
                    .addAttribute("video", course.getVideo())
                    .addAttribute("videoGirl", course.getVideoGirl())
                    .addAttribute("keyPoint", course.getKeyPoint())
                    .addAttribute("online", course.checkOnlineStatus())
                    .addAttribute("videoImage", course.getVideoImage())
                    .addAttribute("videoImageGirl", course.getVideoImageGirl());
            int index = 1;
            List<TobbitExercise> exercises = course.getExercises();
            if (CollectionUtils.isNotEmpty(exercises)) {

                for (TobbitExercise exercise : exercises) {
                    model.addAttribute("workId" + index, exercise.getWorkId());
                    model.addAttribute("viewType" + index, exercise.getViewType() == ExerciseViewType.square ? "1" : "2");
                    index += 1;
                }
            }
        } else {
            Subject subject = Subject.safeParse(getRequestString("subject"));
            if (null == subject) {
                subject = Subject.MATH;
            }
            model.addAttribute("subject", subject);
        }

        return "opmanager/tobbit/editor";
    }

    @RequestMapping("/save.vpage")
    @ResponseBody
    public MapMessage courseSave() {

        TobbitCourse course = new TobbitCourse();
        course.setId(getRequestString("id"));
        if (StringUtils.isEmpty(course.getId())) {
            course.setId(RandomUtils.nextObjectId());
        }
        course.setClazzLevel(ClazzLevel.parse(getRequestInt("level")));
        if (course.getClazzLevel() == null) {
            return MapMessage.errorMessage("年级错误");
        }
        course.setSubject(Subject.of(getRequestString("subject")));
        if (course.getSubject() == null) {
            return MapMessage.errorMessage("学科错误");
        }
        course.setName(getRequestString("name"));
        if (StringUtils.isEmpty(course.getName())) {
            return MapMessage.errorMessage("课程名称为空");
        }
        course.setTrial(getRequestBool("trail"));
        course.setVideo(getRequestString("video"));
        course.setVideoGirl(getRequestString("videoGirl"));
        course.setConvert(getRequestString("convert"));
        course.setConvertGirl(getRequestString("convertGirl"));
        course.setSequence(getRequestInt("sequence"));
        course.setKeyPoint(getRequestString("keyPoint"));
        course.setOnline(requestBoolean("online"));
        course.setVideoImage(requestString("videoImage"));
        course.setVideoImageGirl(requestString("videoImageGirl"));

        List<String> keyPoints = new ArrayList<>(4);
        for (int i = 1; i <= 4; i++) {
            String keyPoint = getRequestString("keyPoint" + i);
            if (StringUtils.isEmpty(keyPoint)) {
                break;
            }

            keyPoints.add(keyPoint);
        }
        if (CollectionUtils.isNotEmpty(keyPoints)) {
            course.setKeyPoints(keyPoints);
        }

        List<TobbitExercise> exercises = new ArrayList<>(5);
        for (int i = 1; i <= 5; i++) {
            String workId = getRequestString("workId" + i);
            if (StringUtils.isEmpty(workId)) {
                break;
            }
            String viewType = getRequestString("viewType" + i);
            TobbitExercise exercise = new TobbitExercise();
            exercise.setWorkId(workId);
            exercise.setViewType("1".equals(viewType) ? ExerciseViewType.square : ExerciseViewType.oblong);
            exercises.add(exercise);
        }
        if (CollectionUtils.isNotEmpty(exercises)) {
            course.setExercises(exercises);
        }
        Set<String> workIds = exercises.stream()
                .map(TobbitExercise::getWorkId)
                .collect(Collectors.toSet());

        List<NewQuestion> questions = questionLoaderClient
                .loadQuestionByDocIds(workIds);
        if (CollectionUtils.isEmpty(questions) || questions.size() != exercises.size()) {
            return MapMessage.errorMessage("参数错误，有题目不存在。");
        }

        if (questions.stream().anyMatch(x -> x.getSubjectId() == null || x.getSubjectId() != course.getSubject().getId())) {
            return MapMessage.errorMessage("题目学科错误");
        }

        if (questions.stream().anyMatch(x -> x.getContentTypeId() == null || x.getContentTypeId() != 1021001)) {
            return MapMessage.errorMessage("题型错误");
        }

        tobbitCRMService.saveCourse(course);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage courseDelete() {
        String id = requestString("id");
        if (StringUtils.isEmpty(id)) {
            return MapMessage.errorMessage("参数错误");
        }
        MapMessage mapMessage = MapMessage.successMessage();
        tobbitCRMService.delete(id);
        return mapMessage;
    }

    private Boolean requestTrail() {
        String trailStr = getRequestString("trail");
        Boolean trail = null;
        if ("true".equals(trailStr)) {
            trail = true;
        } else if ("false".equals(trailStr)) {
            trail = false;
        }
        return trail;
    }

    @RequestMapping(value = "/game.vpage", method = RequestMethod.GET)
    public String game(Model model) {
        String courseId = getRequestString("id");
        String gameTypeStr = getRequestString("type");
        GameType type = GameType.of(gameTypeStr);
        if (StringUtils.isEmpty(courseId) || null == type) {
            return "opmanager/tobbit/error";
        }

        TobbitCourse course = tobbitCRMLoader.loadCourse(courseId);
        if (null == course) {
            return "opmanager/tobbit/error";
        }

        model.addAttribute("courseId", courseId)
                .addAttribute("type", type)
                .addAttribute("course", course.getName())
                .addAttribute("subject", course.getSubject())
                .addAttribute("level", course.getClazzLevel());


        List<TobbitGame> games = tobbitCRMLoader.loadTobbitGame(courseId);
        if (CollectionUtils.isNotEmpty(games)) {
            TobbitGame tobbitGame = games.get(0);
            model.addAttribute("id", tobbitGame.getId());
            if (tobbitGame.getGameType() == type) {
                Game game = tobbitGame.getGame();
                if (null != game) {
                    model.addAttribute("game", JsonUtils.toJson(game));
                }
            }
        }

        return "opmanager/tobbit/" + type.name();
    }

    @RequestMapping(value = "/game/save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage gameSave() {

        GameType type = GameType.of(getRequestString("type"));
        if (null == type) {
            return MapMessage.errorMessage("参数错误");
        }
        Game game = null;
        switch (type) {
            case balloon:
                game = requestBalloon();
                break;
            case pesticide:
                game = requestPesticide();
                break;
            case arrow:
                game = requestArrow();
                break;
            case doll:
                game = requestDoll();
                break;
            case color:
                game = requestColor();
                break;
            case frog:
                game = requestFrog();
                break;
            case tangram:
                game = requestTangram();
                break;
            case smallchange:
                game = requestSmallChange();
                break;
            case wardrobe:
                game = requestWardrobe();
                break;
            case board:
                game = requestBoard();
                break;
            case cumulative:
                game = requestCumulative();
                break;
            case law:
                game = requestLaw();
                break;
        }
        if (null == game) {
            return MapMessage.errorMessage("游戏数据错误");
        }

        TobbitGame tobbitGame = new TobbitGameMapping();

        tobbitGame.setGame(game);
        tobbitGame.setCourseId(requestString("courseId"));

        tobbitGame.setId(requestString("id"));
        tobbitGame.setGameType(type);


        if (StringUtils.isEmpty(tobbitGame.getCourseId())) {
            return MapMessage.errorMessage("课程id错误");
        }

        tobbitCRMService.saveGame(tobbitGame);

        return MapMessage.successMessage();
    }

    private List<Problem> requestProblem() {
        String question = requestString("question");
        String audio = requestString("audio");
        String answer = requestString("answer");

        if (StringUtils.isEmpty(question) || StringUtils.isEmpty(answer)) {
            return Collections.emptyList();
        }
        String[] qs = question.split(",");
        String[] as = answer.split(",");


        if (as.length != qs.length) {
            return Collections.emptyList();
        }

        String[] os = StringUtils.isEmpty(audio) ? null : audio.split(",");

        List<Problem> problems = new LinkedList<>();
        int length = qs.length;
        for (int i = 0; i < length; i++) {
            Problem problem = new Problem();
            problem.setQuestion(qs[i]);
            problem.setAnswer(as[i]);
            if (null != os && os.length == length) {
                problem.setVoice(os[i]);
            }

            problems.add(problem);
        }

        return problems;
    }

    private List<String> requestDistracter() {
        String str = requestString("distractors");
        return StringUtils.isEmpty(str) ? Collections.emptyList() : Arrays.asList(str.split(","));
    }

    private Balloon requestBalloon() {
        List<String> strings = requestDistracter();
        List<Problem> problems = requestProblem();
        if (null == problems) {
            return null;
        }

        Balloon balloon = new Balloon();
        balloon.setProblems(problems);
        balloon.setDistracters(strings);

        return !balloon.valid() ? null : balloon;

    }

    private Pesticide requestPesticide() {
        Pesticide pesticide = new Pesticide();
        pesticide.setProblems(requestProblem());
        pesticide.setVoice(requestString("voice"));
        return pesticide.valid() ? pesticide : null;
    }

    private Arrow requestArrow() {
        Arrow arrow = new Arrow();
        arrow.setProblems(requestProblem());
        arrow.setDistracters(requestDistracter());
        return arrow.valid() ? arrow : null;
    }

    private Doll requestDoll() {
        Doll doll = new Doll();
        doll.setProblems(requestProblem());
        doll.setDistracters(requestDistracter());
        return doll.valid() ? doll : null;
    }

    private Color requestColor() {
        Color color = new Color();
        color.setProblems(requestProblem());
        color.setVoice(requestString("voice"));
        return color.valid() ? color : null;
    }

    private Frog requestFrog() {
        Frog frog = new Frog();
        frog.setProblems(requestProblem());
        frog.setDistracters(requestDistracter());
        frog.setVoice(requestString("voice"));
        return frog.valid() ? frog : null;
    }

    private Tangram requestTangram() {
        Tangram tangram = new Tangram();
        tangram.setVoice(requestString("voice"));
        return tangram.valid() ? tangram : null;
    }

    private Wardrobe requestWardrobe() {
        Wardrobe wardrobe = new Wardrobe();
        wardrobe.setVoice(getRequestString("voice"));
        wardrobe.setGroup(requestItems("group_name", "group_resource"));
        wardrobe.setOption(requestItems("option_name", "option_resource"));
        return wardrobe.valid() ? wardrobe : null;
    }

    private List<Plank> requestPlank() {
        String front = requestString("front");
        String back = requestString("back");
        String voice = requestString("voice");

        if (StringUtils.isEmpty(front) || StringUtils.isEmpty(back) || StringUtils.isEmpty(voice)) {
            return Collections.emptyList();
        }
        String[] fs = front.split(",");
        String[] bs = back.split(",");
        String[] vs = voice.split(",");


        if (fs.length != bs.length || vs.length != fs.length) {
            return Collections.emptyList();
        }

        List<Plank> planks = new LinkedList<>();
        int length = fs.length;
        for (int i = 0; i < length; i++) {
            Plank plank = new Plank();
            plank.setBack(bs[i]);
            plank.setFront(fs[i]);
            plank.setVoice(vs[i]);

            planks.add(plank);
        }

        return planks;
    }

    private Board requestBoard() {
        Board board = new Board();
        board.setWidth(requestInteger("width"));
        board.setHeight(requestInteger("height"));
        board.setImage(requestString("image"));
        board.setPlanks(requestPlank());
        return board.valid() ? board : null;
    }

    private List<Item> requestItems(String name, String resource) {
        String names = requestString(name);
        String resources = requestString(resource);

        if (StringUtils.isEmpty(names) || StringUtils.isEmpty(resources)) {
            return Collections.emptyList();
        }
        String[] ns = names.split(",");
        String[] rs = resources.split(",");


        if (ns.length != rs.length) {
            return Collections.emptyList();
        }

        List<Item> items = new LinkedList<>();
        int length = ns.length;
        for (int i = 0; i < length; i++) {
            Item item = new Item();
            item.setName(ns[i]);
            item.setResource(rs[i]);

            items.add(item);
        }

        return items;
    }

    private SmallChange requestSmallChange() {
        SmallChange smallChange = new SmallChange();
        smallChange.setBackground(requestString("background"));
        smallChange.setVoice(requestString("voice"));
        smallChange.setProblems(requestProblem());
        return smallChange.valid() ? smallChange : null;
    }

    private List<Digit> requestDigits() {
        String image = requestString("image");
        String value = requestString("value");

        if (StringUtils.isEmpty(image) || StringUtils.isEmpty(value)) {
            return Collections.emptyList();
        }
        String[] is = image.split(",");
        String[] vs = value.split(",");


        if (is.length != vs.length) {
            return Collections.emptyList();
        }


        List<Digit> digits = new LinkedList<>();
        int length = vs.length;
        for (int i = 0; i < length; i++) {
            Digit digit = new Digit();
            digit.setImage(is[i]);
            digit.setValue(SafeConverter.toInt(vs[i]));

            digits.add(digit);
        }

        return digits;
    }

    private Cumulative requestCumulative() {
        Cumulative cumulative = new Cumulative();
        cumulative.setCount(getRequestInt("count"));
        cumulative.setValue(getRequestInt("total"));
        cumulative.setVoice(getRequestString("voice"));
        cumulative.setDigits(requestDigits());

        return cumulative.valid() ? cumulative : null;
    }

    private Law requestLaw() {
        Law law = new Law();

        law.setImage(requestString("background"));
        law.setVoice(requestString("voice"));
        law.setQuestion(requestString("question"));
        List<Image> images = requestImages("p_i", "p_x", "p_y", "p_w", "p_h");
        if (CollectionUtils.isEmpty(images)) {
            return null;
        }
        law.setProblem(images.get(0));

        List<Image> as = requestImages("a_i", "a_x", "a_y", "a_w", "a_h");
        List<Image> qs = requestImages("q_i", "q_x", "q_y", "q_w", "q_h");
        List<Image> ds = requestImages("d_i", "d_x", "d_y", "d_w", "d_h");
        if (CollectionUtils.isEmpty(qs) || CollectionUtils.isEmpty(as)
                || as.size() != qs.size()) {
            return null;
        }

        List<LawItem> items = new LinkedList<>();
        for (int i = 0; i < qs.size(); i++) {
            LawItem item = new LawItem();
            item.setQuestion(qs.get(i));
            item.setAnswer(as.get(i));
            items.add(item);
        }

        law.setItems(items);
        law.setOthers(ds);

        return law.valid() ? law : null;
    }

    private List<Image> requestImages(String p, String x, String y, String w, String h) {

        String[] ps = StringUtils.split(requestString(p), ",");
        String[] xs = StringUtils.split(requestString(x), ",");
        String[] ys = StringUtils.split(requestString(y), ",");
        String[] ws = StringUtils.split(requestString(w), ",");
        String[] hs = StringUtils.split(requestString(h), ",");
        if (ps == null || ps.length == 0
                || xs == null || xs.length == 0 || ys == null || ys.length == 0
                || ws == null || ws.length == 0 || hs == null || hs.length == 0) {
            return Collections.emptyList();
        }

        int length = ps.length;
        if (xs.length != length || ys.length != length || ws.length != length || hs.length != length) {
            return Collections.emptyList();
        }

        List<Image> images = new LinkedList<>();
        for (int i = 0; i < length; i++) {
            Image image = new Image();
            image.setPath(ps[i]);
            image.setX(SafeConverter.toInt(xs[i]));
            image.setY(SafeConverter.toInt(ys[i]));
            image.setW(SafeConverter.toInt(ws[i]));
            image.setH(SafeConverter.toInt(hs[i]));
            images.add(image);
        }
        return images;
    }

    @RequestMapping(value = "/upload.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadBackground(MultipartFile inputFile) {
        try {
            String path = AdminOssManageUtils.upload(inputFile, "tobbit");
            return MapMessage.successMessage().add("path", path);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }
}