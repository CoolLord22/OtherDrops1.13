package com.gmail.zariust.otherdrops.things;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ODMatch {
    private String msg;

    public ODMatch(String msg) {
        this.msg = msg;
    }

    public String match(String patternString, ODMatchRunner runner) {
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(msg);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, runner.runMatch(matcher.group(1)));
        }
        matcher.appendTail(sb);
        msg = sb.toString();
        return msg;
    }            
}
