package org.springframework.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

import com.sun.tools.javac.util.Context;

public class JDKParser {
    private ParserFactory factory;
    public JDKParser() {
        this.factory = getParserFactory();
    }
    public List<MethodMdl> paseMethodDefs(String file) throws IOException {
        JCCompilationUnit unit = parse(file);
        MethodScanner scanner = new MethodScanner();
        scanner.setFile(file);
        return scanner.visitCompilationUnit(unit, new ArrayList<MethodMdl>());
    }
    public JCCompilationUnit parse(String file) throws IOException {
        Parser parser = factory.newParser(readFile(file), true, false, true);
        return parser.parseCompilationUnit();
    }
    private ParserFactory getParserFactory() {
        Context context = new Context();
        JavacFileManager.preRegister(context);
        ParserFactory factory = ParserFactory.instance(context);
        return factory;
    }
    private CharSequence readFile(String file) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        FileChannel ch = fin.getChannel();
        ByteBuffer buffer = ch.map(MapMode.READ_ONLY, 0, ch.size());
        return Charset.defaultCharset().decode(buffer);
    }
    static class MethodScanner extends TreeScanner<List<MethodMdl>, List<MethodMdl>> {
        private String file;
        public void setFile(String file) {
            this.file = file;
        }
        @Override
        public List<MethodMdl> visitMethod(MethodTree node, List<MethodMdl> p) {
            MethodMdl mdl = new MethodMdl();
            mdl.setClassName(this.file);
            mdl.setMethodName(node.getName().toString());
            if ("void".equalsIgnoreCase(node.getReturnType().toString())) {
                mdl.setReturnType("");
            } else {
                mdl.setReturnType(node.getReturnType().toString());
            }
            mdl.setParamsType(new ArrayList<String>());
            mdl.setParamsVar(new ArrayList<String>());
            
            for (VariableTree param : node.getParameters()) {
                String strParam = param.toString();
                String[] aryParam = strParam.split(" ");
                mdl.getParamsType().add(aryParam[0].replaceAll("\r\n", " "));
                mdl.getParamsVar().add(aryParam[1]);
            }
            p.add(mdl);
            return p;
        }
    }
    public static List<MethodMdl> getMethods(String file) {
        try {
            JDKParser parser = new JDKParser();
            return parser.paseMethodDefs(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<MethodMdl>();
    }
}
