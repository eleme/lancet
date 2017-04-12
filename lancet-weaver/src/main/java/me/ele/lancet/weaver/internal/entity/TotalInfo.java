package me.ele.lancet.weaver.internal.entity;


import java.util.List;

/**
 * Created by gengwanpeng on 17/3/27.
 */
public class TotalInfo {

    public List<ExecuteInfo> executeInfos;
    public List<TryCatchInfo> tryCatchInfos;
    public List<CallInfo> callInfos;

    public TotalInfo(List<ExecuteInfo> executeInfos, List<TryCatchInfo> tryCatchInfos, List<CallInfo> callInfos) {
        this.executeInfos = executeInfos;
        this.tryCatchInfos = tryCatchInfos;
        this.callInfos = callInfos;
    }
}
