package me.ele.lancet.weaver.internal.entity;


import java.util.List;

/**
 * Created by gengwanpeng on 17/3/27.
 */
public class TotalInfo {

    public List<ExecuteInfo> executeInfos;
    public List<TryCatchInfo> tryCatchInfos;
    public List<CallInfo> callInfos;
    public List<String> excludes;

    public TotalInfo() {
    }

    public TotalInfo(List<ExecuteInfo> executeInfos, List<TryCatchInfo> tryCatchInfos, List<CallInfo> callInfos) {
        this.executeInfos = executeInfos;
        this.tryCatchInfos = tryCatchInfos;
        this.callInfos = callInfos;
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
        String content = "";
        if (executeInfos!=null){
            for (ExecuteInfo executeInfo : executeInfos) {
                content+=executeInfo+"\n";
            }
        }
        if (callInfos!=null){
            for (CallInfo callInfo : callInfos) {
                content+=callInfo+"\n";
            }
        }
        if (tryCatchInfos!=null){
            for (TryCatchInfo tryCatchInfo : tryCatchInfos) {
                content+=tryCatchInfo+"\n";
            }
        }
        return content;
    }
}
