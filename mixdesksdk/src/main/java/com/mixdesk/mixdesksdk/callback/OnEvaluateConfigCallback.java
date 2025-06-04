package com.mixdesk.mixdesksdk.callback;

import com.mixdesk.core.bean.MXEvaluateConfig;

import java.util.List;

public interface OnEvaluateConfigCallback extends OnFailureCallBack{
    void onSuccess(List<MXEvaluateConfig> config);
}
