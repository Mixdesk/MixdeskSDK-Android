package com.mixdesk.mixdesksdk.model;

import com.mixdesk.core.bean.MXEvaluateConfig;

import java.util.List;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/1/31 下午5:14
 * 描述:
 */
public class EvaluateMessage extends BaseMessage {
    public static final int EVALUATE_GOOD = 2;
    public static final int EVALUATE_MEDIUM = 1;
    public static final int EVALUATE_BAD = 0;

    private final int level;
    private final List<MXEvaluateConfig.Tag> selectTagIds;
    private final int evaluateLevel;
    private final int isSolved;
    private final List<MXEvaluateConfig> evaluateConfig;

    public EvaluateMessage(int isSolved, int level,
                           List<MXEvaluateConfig.Tag> selectTagIds, String content,
                           int evaluateLevel, List<MXEvaluateConfig> evaluateConfig) {
        this.isSolved = isSolved;
        this.level = level;
        this.selectTagIds = selectTagIds;
        this.evaluateLevel = evaluateLevel;
        this.evaluateConfig = evaluateConfig;
        setContent(content);
        setItemViewType(TYPE_EVALUATE);
    }

    public int getLevel() {
        return level;
    }

    public List<MXEvaluateConfig.Tag> getSelectTagIds() {
        return selectTagIds;
    }

    public int getEvaluateLevel() {
        return evaluateLevel;
    }

    public int isSolved() {
        return isSolved;
    }

    public List<MXEvaluateConfig> getEvaluateConfig() {
        return evaluateConfig;
    }
}
