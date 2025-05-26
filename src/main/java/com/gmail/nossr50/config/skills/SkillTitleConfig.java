package com.gmail.nossr50.config.skills;

import java.util.ArrayList;
import java.util.List;

import com.gmail.nossr50.config.BukkitConfig;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.util.text.StringUtils;

public class SkillTitleConfig extends BukkitConfig {

    public static final String SKILL_STEP_KEY = "SkillTitleStep";
    public static final String POWERLEVEL_STEP_KEY = "PowerLevelTitleStep";
    public static final String POWERLEVEL_TITLES_KEY = "PowerLevelTitles";
    public static final String SKILL_TITLE_LOCALE = "Broadcasts.SkillTitleName";
    public static final String POWERLEVEL_TITLE_LOCALE = "Broadcasts.PowerLevelTitleName";

    private static class Singleton {
        public static final SkillTitleConfig INSTANCE = new SkillTitleConfig();
    }

    public static SkillTitleConfig getInstance() {
        return Singleton.INSTANCE;
    }

    private SkillTitleConfig() {
        super("skilltitles.yml");
        validate();
    }

    @Override
    protected void loadKeys() {
    }

    @Override
    protected boolean validateKeys() {
        List<String> reason = new ArrayList<>();

        int skillStep = getSkillStep();
        if (skillStep < 1) {
            reason.add(String.format("The %s can't be 0 or negative", SKILL_STEP_KEY));
        }

        int powerLevelStep = getPowerLevelStep();
        if (powerLevelStep < 1) {
            reason.add(String.format("The %s can't be 0 or negative", POWERLEVEL_STEP_KEY));
        }

        for (PrimarySkillType skill : PrimarySkillType.values()) {
            List<String> titles = getSkillTitlesFor(skill);
            if (titles.isEmpty()) {
                reason.add(String.format(
                        "The list of title names for the skill %s is empty. (It should have at least 1)",
                        StringUtils.getCapitalized(skill.name())));
            }
        }

        List<String> powerLevelTitles = getPowerLevelTitles();
        if (powerLevelTitles.isEmpty()) {
            reason.add(String.format("The list of %s is empty. (It should have at least 1)", POWERLEVEL_TITLES_KEY));
        }

        return noErrorsInConfig(reason);
    }

    public String getSkillTitleName(PrimarySkillType primarySkillType, int level) {
        return getTitleName(getSkillTitlesFor(primarySkillType), level,
                getSkillStep());
    }

    public static String getSkillTitleMessage(String skillTitleName) {
        return LocaleLoader.getString(SKILL_TITLE_LOCALE, skillTitleName);
    }

    public String getPowerLevelTitleName(int powerLevel) {
        return getTitleName(getPowerLevelTitles(), powerLevel, getPowerLevelStep());
    }

    public static String getPowerLevelTitleMessage(String powerLevelTitleName) {
        return LocaleLoader.getString(POWERLEVEL_TITLE_LOCALE, powerLevelTitleName);
    }

    private String getTitleName(List<String> titles, int level, int step) {
        int titleIndex = level / step;
        if (titleIndex >= titles.size()) {
            return titles.get(titles.size() - 1);
        }
        return titles.get(titleIndex);
    }

    public int getSkillStep() {
        return config.getInt(SKILL_STEP_KEY);
    }

    public int getPowerLevelStep() {
        return config.getInt(POWERLEVEL_STEP_KEY);
    }

    public List<String> getSkillTitlesFor(PrimarySkillType primarySkillType) {
        return config.getStringList(StringUtils.getCapitalized(primarySkillType.name()));
    }

    public List<String> getPowerLevelTitles() {
        return config.getStringList(POWERLEVEL_TITLES_KEY);
    }
}
