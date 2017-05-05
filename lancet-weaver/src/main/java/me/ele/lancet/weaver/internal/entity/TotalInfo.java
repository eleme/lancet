package me.ele.lancet.weaver.internal.entity;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by gengwanpeng on 17/3/27.
 */
public class TotalInfo {

    public List<ExecuteInfo> executeInfos = new ArrayList<>();
    public List<TryCatchInfo> tryCatchInfos = new ArrayList<>();
    public List<CallInfo> callInfos = new ArrayList<>();
    public List<String> excludes = new ArrayList<>();

    public TotalInfo() {
    }

    public void combine(TotalInfo other) {
        executeInfos.addAll(other.executeInfos);
        tryCatchInfos.addAll(other.tryCatchInfos);
        callInfos.addAll(other.callInfos);
        excludes.addAll(other.excludes);
    }

    public void setExecuteInfos(List<ExecuteInfo> executeInfos) {
        this.executeInfos = executeInfos;
    }

    public void setTryCatchInfos(List<TryCatchInfo> tryCatchInfos) {
        this.tryCatchInfos = tryCatchInfos;
    }

    public void setCallInfos(List<CallInfo> callInfos) {
        this.callInfos = callInfos;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    @Override
    public String toString() {
        StringBuilder content = new StringBuilder();
        if (executeInfos != null) {
            for (ExecuteInfo executeInfo : executeInfos) {
                content.append(executeInfo).append("\n");
            }
        }
        if (callInfos != null) {
            for (CallInfo callInfo : callInfos) {
                content.append(callInfo).append("\n");
            }
        }
        if (tryCatchInfos != null) {
            for (TryCatchInfo tryCatchInfo : tryCatchInfos) {
                content.append(tryCatchInfo).append("\n");
            }
        }
        return content.toString();
    }
}
