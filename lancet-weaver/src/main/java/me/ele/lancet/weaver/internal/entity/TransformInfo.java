package me.ele.lancet.weaver.internal.entity;


import java.util.*;

/**
 * Created by gengwanpeng on 17/3/27.
 *
 * A data sets store all data for transform operation.
 *
 */
public class TransformInfo {

    /**
     * Key for target class's name.
     * Value for all InsertInfo want insert into the target class.
     */
    public Map<String, List<InsertInfo>> executeInfo = new HashMap<>();
    public List<ProxyInfo> proxyInfo = new ArrayList<>();
    public List<TryCatchInfo> tryCatchInfo = new ArrayList<>();
    public Set<String> hookClasses = new HashSet<>();

    public TransformInfo(List<String> hookClasses) {
        this.hookClasses.addAll(hookClasses);
    }

    public void combine(TransformInfo other) {
        other.executeInfo.forEach((key, value) -> executeInfo.computeIfAbsent(key, k -> new LinkedList<>())
                .addAll(value));

        tryCatchInfo.addAll(other.tryCatchInfo);
        proxyInfo.addAll(other.proxyInfo);
        hookClasses.addAll(other.hookClasses);
    }

    public void addInsertInfo(InsertInfo e) {
        executeInfo.computeIfAbsent(e.targetClass, k -> new LinkedList<>())
                .add(e);
    }

    public void addProxyInfo(ProxyInfo c) {
        proxyInfo.add(c);
    }

    public void addTryCatch(TryCatchInfo t) {
        tryCatchInfo.add(t);
    }

    public void setInsertInfo(Map<String, List<InsertInfo>> executeInfo) {
        this.executeInfo = executeInfo;
    }

    public void setTryCatchInfo(List<TryCatchInfo> tryCatchInfo) {
        this.tryCatchInfo = tryCatchInfo;
    }

    public void setProxyInfo(List<ProxyInfo> proxyInfo) {
        this.proxyInfo = proxyInfo;
    }

    public void setHookClasses(Set<String> hookClasses) {
        this.hookClasses = hookClasses;
    }

    @Override
    public String toString() {
        StringBuilder content = new StringBuilder();
        if (executeInfo != null) {
            content.append("\nInsert:\n");
            for (Map.Entry<String, List<InsertInfo>> executeList : executeInfo.entrySet()) {
                content.append(' ').append(executeList.getKey()).append(":\n");
                executeList.getValue().forEach(e -> {
                    content.append("  ").append(e).append("\n");
                });
            }
        }
        if (proxyInfo != null) {
            content.append("Proxy:\n");
            for (ProxyInfo proxyInfo : this.proxyInfo) {
                content.append(' ').append(proxyInfo).append("\n");
            }
        }
        if (tryCatchInfo != null) {
            content.append("TryCatch:\n");
            for (TryCatchInfo tryCatchInfo : this.tryCatchInfo) {
                content.append(' ').append(tryCatchInfo).append("\n");
            }
        }
        if (hookClasses != null) {
            content.append("HookClasses:\n");
            for (String hookClasses : this.hookClasses) {
                content.append(' ').append(hookClasses).append("\n");
            }
        }
        return content.toString();
    }
}
