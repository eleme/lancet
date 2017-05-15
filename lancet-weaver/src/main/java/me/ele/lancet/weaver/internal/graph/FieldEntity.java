package me.ele.lancet.weaver.internal.graph;

/**
 * Created by gengwanpeng on 17/5/11.
 */
public class FieldEntity {

    public int access;
    public String name;
    public String desc;

    public FieldEntity(int access, String name, String desc) {
        this.access = access;
        this.name = name;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "FieldEntity{" +
                "access=" + access +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
