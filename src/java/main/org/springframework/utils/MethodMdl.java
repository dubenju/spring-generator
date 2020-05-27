package org.springframework.utils;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MethodMdl {
    private String className;
    private String returnType;
    private String methodName;
    private List<String> paramsType;
    private List<String> paramsVar;
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.className);
        buf.append("\n");
        buf.append(this.returnType);
        buf.append(" ");
        buf.append(this.methodName);
        buf.append(" ");
        for (int idx = 0; idx < this.paramsType.size(); idx ++) {
            buf.append(this.paramsType.get(idx));
            buf.append(" ");
            buf.append(this.paramsVar.get(idx));
            buf.append(" ");
        }
        buf.append("\n");
        return buf.toString();
    }
}
