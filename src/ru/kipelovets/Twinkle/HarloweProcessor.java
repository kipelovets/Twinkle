package ru.kipelovets.Twinkle;

import org.attoparser.ParseException;
import org.attoparser.config.ParseConfiguration;
import org.attoparser.simple.AbstractSimpleMarkupHandler;
import org.attoparser.simple.SimpleMarkupParser;
import ru.kipelovets.Twinkle.Novel.Novel;
import ru.kipelovets.Twinkle.Novel.Option;
import ru.kipelovets.Twinkle.Novel.Stitch;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HarloweProcessor extends AbstractSimpleMarkupHandler {

    private String document;
    private StringBuffer text;
    private String currentStitchName;
    private String currentStitchPid;
    private Boolean inElement;
    private String firstStitchPid;

    private Novel novel;

    public HarloweProcessor(String document) {
        super();
        this.document = document;
        this.inElement = false;
        novel = new Novel();
    }

    public Novel getNovel() throws ParseException {
        SimpleMarkupParser parser = new SimpleMarkupParser(ParseConfiguration.htmlConfiguration()); // this is thread-safe and can be reused

        parser.parse(document, this);

        return novel;
    }

    @Override
    public void handleOpenElement(String elementName, Map<String, String> attributes, int line, int col) throws ParseException {
        if (elementName.equals("tw-passagedata")) {
            inElement = true;
            text = new StringBuffer();
            currentStitchName = attributes.get("name");
            currentStitchPid = attributes.get("pid");

            System.err.println("OpenElement: passage " + currentStitchName + ", id " + currentStitchPid);
        } else if (elementName.equals("tw-storydata")) {
            firstStitchPid = attributes.get("startnode");
            novel.setName(attributes.get("name"));

            System.err.println("OpenElement: first stitch " + firstStitchPid + ", story name " + novel.getName());
        }
    }

    @Override
    public void handleText(char[] buffer, int offset, int len, int line, int col) throws ParseException {
        if (inElement) {
            text.append(buffer, offset, len);
        }
    }

    @Override
    public void handleCloseElement(String elementName, int line, int col) throws ParseException {
        if (inElement) {
            System.err.println("CloseElement: parsing...");

            Stitch stitch = new Stitch(expandHtmlEntities(text.toString()));
            stitch.setName(currentStitchName);

            extractFlags(stitch);
            extractDivert(stitch);
            extractAnswers(stitch);
            extractConditions(stitch);
            extractConditionalText(stitch);
            extractImage(stitch);
            extractVariables(stitch);

            novel.getStitches().add(stitch);

            if (currentStitchPid.equals(firstStitchPid)) {
                novel.setFirstStitch(stitch);
            }

            inElement = false;

            System.err.println("CloseElement: ok");
        }
    }

    private String expandHtmlEntities(String text) {
        return text.replaceAll("&gt;", ">")
                .replaceAll("&lt;", "<")
                .replaceAll("&quot;", "\"")
                ;
    }

    private void extractFlags(Stitch stitch) throws ParseException {
        Pattern setVariablePattern = Pattern.compile("\\(set:([^)]+)\\s+to\\s+([^)]+)\\)");
        Matcher m = setVariablePattern.matcher(stitch.getText());
        while (m.find()) {
            String variable = m.group(1)
                    .trim()
                    .replaceAll("\\$", "");

            if (variable.equals("timeout")) {
                continue;
            }
            String value = m.group(2).trim().replaceAll("\\$", "");

            if (Pattern.matches("^\\d+$", value)) {
                if (value.equals("1")) {
                    stitch.addFlag(variable);
                } else {
                    stitch.addFlag(variable + " = " + value);
                }
            } else {
                String regex = Pattern.quote(variable) + "\\s+[-+]\\s+\\d+";
                if (Pattern.matches(regex, value)) {
                    stitch.addFlag(value);
                } else {
                    throw new ParseException("Unknown flag expression: " + m.group(0));
                }
            }
        }
        stitch.setText(m.replaceAll(""));
    }

    private void extractDivert(Stitch stitch) {
        Matcher m = Pattern.compile("\\[\\[divert->([^\\]]+)\\]\\]", Pattern.DOTALL)
                .matcher(stitch.getText());
        if (m.find()) {
            stitch.setDivert(m.group(1));
            stitch.setText(m.replaceAll("").trim());
        }
    }

    private void extractAnswers(Stitch stitch) throws ParseException {
        Matcher condAnswersMatcher = Pattern.compile("\\(if:([^)]+)\\)\\[\\s*\\[\\[([^\\]]+)\\]\\]\\s*\\]", Pattern.DOTALL)
                .matcher(stitch.getText());
        Pattern linkPattern = Pattern.compile("(.+)->(.+)");
        while (condAnswersMatcher.find()) {

            String link = condAnswersMatcher.group(2);
            Matcher linkMatcher = linkPattern.matcher(link);
            Option option;
            if (linkMatcher.matches()) {
                option = new Option(linkMatcher.group(1), linkMatcher.group(2));
            } else {
                option = new Option(link, link);
            }

            option.addIfCondition(parseCondition(condAnswersMatcher.group(1)));
            stitch.addOption(option);
        }

        stitch.setText(condAnswersMatcher.replaceAll(""));

        Matcher answersMatcher = Pattern.compile("\\[\\[([^\\]]+)\\]\\]", Pattern.DOTALL).matcher(stitch.getText());
        while (answersMatcher.find()) {
            String link = answersMatcher.group(1);
            Matcher linkMatcher = linkPattern.matcher(link);
            Option option;
            if (linkMatcher.matches()) {
                option = new Option(linkMatcher.group(1), linkMatcher.group(2));
            } else {
                option = new Option(link, link);
            }

            stitch.addOption(option);
        }
        stitch.setText(answersMatcher.replaceAll(""));
    }

    private String parseCondition(String twineCondition) throws ParseException {
        Pattern eqCondPattern = Pattern.compile("\\s*(\\S+)\\s+is\\s+(.*)");
        Pattern neqCondPattern = Pattern.compile("(\\S+)\\s+(>|<|>=|<=|!=)\\s+(\\d+)");
        String inkleCondition;
        String condition = twineCondition.trim().replaceAll("\\$", "");
        Matcher eqCondMather = eqCondPattern.matcher(condition);
        if (eqCondMather.matches()) {
            String variable = eqCondMather.group(1);
            String value = eqCondMather.group(2).trim();
            if (value.equals("1")) {
                inkleCondition = variable;
            } else {
                if (Pattern.matches("^\\d+$", value)) {
                    inkleCondition = variable + " = " + value;
                } else {
                    throw new ParseException("Unknown answer eq condition: " + condition);
                }
            }
        } else {
            Matcher neqCondMatcher = neqCondPattern.matcher(condition);
            if (neqCondMatcher.matches()) {
                inkleCondition = condition;
            } else {
                throw new ParseException("Unknown answer neq condition: " + condition);
            }
        }
        return inkleCondition;
    }

    private void extractConditions(Stitch stitch) throws ParseException {
        Pattern pattern = Pattern.compile("\\(if:([^)]+)\\)\\[(.+)\\]", Pattern.DOTALL);

        String text = stitch.getText().trim();
        while (true) {
            Matcher condMatcher = pattern.matcher(text);
            if (!condMatcher.matches()) {
                break;
            }
            text = condMatcher.group(2).trim();

            stitch.getIfConditions().add(parseCondition(condMatcher.group(1)));
        }

        stitch.setText(text);
    }

    private void extractConditionalText(Stitch stitch) throws ParseException {
        Pattern pattern = Pattern.compile("\\(if:([^)]+)\\)\\[([^\\[]+)\\](\\(else:\\)\\[(.*)\\])?", Pattern.DOTALL);
        String text = stitch.getText();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            // {торг > 1:братишка|командир}
            String inkleCondition = parseCondition(matcher.group(1));

            StringBuilder newText = new StringBuilder(stitch.getText().length());
            newText.append(text.substring(0, Integer.max(0, matcher.start())));
            newText.append(String.format("{%s:%s%s}",
                    inkleCondition,
                    matcher.group(2),
                    matcher.groupCount() >= 4 ? "|" + matcher.group(4) : ""
            ));
            newText.append(text.substring(matcher.end()));

            text = newText.toString();
            matcher = pattern.matcher(text);
        }

        stitch.setText(text);
    }

    private void extractImage(Stitch stitch) throws ParseException {
        String text = stitch.getText();
        Matcher matcher = Pattern.compile("<img src=\"([^\"]+)\"[^>]+>").matcher(text);
        int matchCount = 0;
        while (matcher.find()) {
            matchCount++;
            if (matchCount > 1) {
                throw new ParseException("Too much images in a stitch");
            }
            stitch.setImage(matcher.group(1));
            text = text.substring(0, Integer.max(0, matcher.start())) +
                    text.substring(matcher.end());
        }

        stitch.setText(text);
    }

    private void extractVariables(Stitch stitch) throws ParseException {
        String text = stitch.getText();
        Matcher matcher = Pattern.compile("\\$(\\w+)").matcher(text);
        while (matcher.find()) {
            text = text.substring(0, Integer.max(0, matcher.start())) +
                    String.format("[value:%s]", matcher.group(1)) +
                    text.substring(matcher.end());
        }
        stitch.setText(text);
    }
}
