package me.ele.lancet.weaver.internal.entity;


import java.util.*;

/**
 * Created by gengwanpeng on 17/3/27.
 */
public class TotalInfo {

    public Map<String, List<ExecuteInfo>> executeInfos = new HashMap<>();
    public List<TryCatchInfo> tryCatchInfos = new ArrayList<>();
    public List<CallInfo> callInfos = new ArrayList<>();
    public Set<String> excludes = new HashSet<>();

    public TotalInfo(List<String> classes) {
        excludes.addAll(classes);
    }

    public void combine(TotalInfo other) {
        other.executeInfos.forEach((key, value) -> executeInfos.computeIfAbsent(key, k -> new LinkedList<>())
                .addAll(value));

        tryCatchInfos.addAll(other.tryCatchInfos);
        callInfos.addAll(other.callInfos);
        excludes.addAll(other.excludes);
    }

    public void addExecute(ExecuteInfo e) {
        executeInfos.computeIfAbsent(e.targetClass, k -> new LinkedList<>())
                .add(e);
    }

    public void addCall(CallInfo c) {
        callInfos.add(c);
    }

    public void addTryCatch(TryCatchInfo t) {
        tryCatchInfos.add(t);
    }

    public void setExecuteInfos(Map<String, List<ExecuteInfo>> executeInfos) {
        this.executeInfos = executeInfos;
    }

    public void setTryCatchInfos(List<TryCatchInfo> tryCatchInfos) {
        this.tryCatchInfos = tryCatchInfos;
    }

    public void setCallInfos(List<CallInfo> callInfos) {
        this.callInfos = callInfos;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }

    @Override
    public String toString() {
        StringBuilder content = new StringBuilder();
        if (executeInfos != null) {
            content.append("\nInsert:\n");
            for (Map.Entry<String, List<ExecuteInfo>> executeList : executeInfos.entrySet()) {
                content.append(' ').append(executeList.getKey()).append(":\n");
                executeList.getValue().forEach(e -> {
                    content.append("  ").append(e).append("\n");
                });
            }
        }
        if (callInfos != null) {
            content.append("Proxy:\n");
            for (CallInfo callInfo : callInfos) {
                content.append(' ').append(callInfo).append("\n");
            }
        }
        if (tryCatchInfos != null) {
            content.append("TryCatch:\n");
            for (TryCatchInfo tryCatchInfo : tryCatchInfos) {
                content.append(' ').append(tryCatchInfo).append("\n");
            }
        }
        if (excludes != null) {
            content.append("Excludes:\n");
            for (String exclude : excludes) {
                content.append(' ').append(exclude).append("\n");
            }
        }
        return content.toString();
    }
}
