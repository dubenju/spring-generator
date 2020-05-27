/**
 * 
 */
package org.springframework.orm.jpa.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.springframework.orm.jpa.generator.model.Column;
import org.springframework.orm.jpa.generator.model.Schema;
import org.springframework.orm.jpa.generator.model.Table;
import org.springframework.orm.jpa.generator.model.TestCase;
import org.springframework.utils.JDKParser;
import org.springframework.utils.MethodMdl;
import org.springframework.utils.SystemDateTime;
/**
 * @author DBJ
 *
 */
public class Generator {
    private static HashMap<String, String> map = null;
    private static HashMap<String, List<MethodMdl>> mapRMethods = null;
    private static HashMap<String, List<MethodMdl>> mapRCMethods = null;
    static {
        map = new HashMap<String, String>();

        map.put("CHAR", "String");
        map.put("VARCHAR", "String");
        map.put("VARCHAR2", "String");
        map.put("NVARCHAR2", "String");
        map.put("NUMERIC", "Integer");
        map.put("DATE", "LocalDateTime");

        mapRMethods = new HashMap<String, List<MethodMdl>>();
        mapRCMethods = new HashMap<String, List<MethodMdl>>();
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            System.out.println("***** Generator Begin *****");

            String xmlPath        = "";
            String servletName    = "";
            String projectName    = "";
            String projectPackage = "";
            boolean debug_flag = false;
            boolean error_flag = false;

            for (int i = 0; i < args.length; i++){
                if (i == 0) {
                    xmlPath = args[i];
                } else if (i == 1) {
                    servletName = args[i];
                } else if (i == 2) {
                    projectName = args[i];
                } else if (i == 3) {
                    projectPackage = args[i];
                } else if (i == 4) {
                    debug_flag = args[i].equals("1");
                }
            }

            if (xmlPath == null || xmlPath.length() <= 0){
                xmlPath = "./repository/schema/sample-schema.xml";
                error_flag = true;
            }
            System.out.println("xmlPath=" + xmlPath);

            if (servletName == null || servletName.length() <= 0){
                servletName = "";
                error_flag = true;
            }
            System.out.println("servletName=" + servletName);

            if (projectName == null || projectName.length() <= 0){
                projectName = "";
                error_flag = true;
            }
            System.out.println("projectName=" + projectName);

            if (projectPackage == null || projectPackage.length() <= 0){
                projectPackage = "org.springframework.orm.jpa.repository";
            }else{
                if (!projectPackage.substring(projectPackage.length() -1).equals(".")){
                    projectPackage = projectPackage + ".";
                }
            }
            System.out.println("projectPackage=" + projectPackage);
            if (!debug_flag && error_flag) {
            	throw new Exception("ERROR:\napl xml servletname projectname package name debug");
            }

            List<String> javaFiles = new ArrayList<String>();
            File javaFilePath = new File("C:/dao/");
            if (javaFilePath != null) {
                File[] javaFileList = javaFilePath.listFiles();
                for (int index = 0; javaFileList != null && index < javaFileList.length; index ++) {
                    String strAbsoluteFileName = javaFileList[index].getAbsoluteFile().toString();
                    if (strAbsoluteFileName.endsWith("ModelRepository.java")) {
                        String key = strAbsoluteFileName.replaceAll("\\\\", ".");
                        key = key.replaceAll("src.mail.jara", "");
                        key = key.replace(".java", "");
                        mapRMethods.put(key, JDKParser.getMethods(strAbsoluteFileName));
                        
                    }
                    if (strAbsoluteFileName.endsWith("ModelRepositoryCustom.java")) {
                        String key = strAbsoluteFileName.replaceAll("\\\\", ".");
                        key = key.replaceAll("src.mail.jara", "");
                        key = key.replace(".java", "");
                        mapRCMethods.put(key, JDKParser.getMethods(strAbsoluteFileName));
                        
                    }
                }
            }
//            Document xml = getJdomDocument(xmlPath);

            File in = new File(xmlPath);
            SAXBuilder builder = new SAXBuilder();
            Document xml = builder.build(in);
            System.out.println("read xml file end.");
            Schema schema = docFormatToList(xml, projectName, projectPackage);
            // TODO:lombock的toString可能有递归的问题
            //System.out.println(schema);
            System.out.println("parse xml file end.");

            if (!debug_flag) {
                makeSource(servletName, schema);
            }

            System.out.println("***** Generator Normal End *****");

        } catch (Exception e) {
            System.out.println("***** Generator Abend!!! *****");
            System.out.println(e);
        }
	}

    /**
     * getJdomDocument
     *
     * @param (String) strXml
     * @return (Document) Document
     * @throws Exception
     */
    private static Document getJdomDocument(String strXml) throws Exception {
        File in = new File(strXml);
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(in);
        return doc;
    }
    /**
     * docFormatToList
     *
     * @param (Document) xml
     * @return (Schema) Schema
     * @throws Exception
     */
    private static Schema docFormatToList(Document xml, String projectName, String projectPackage) throws Exception {
        //*******************
        //  database tag
        //*******************
        Element root = xml.getRootElement();
        Schema schema = new Schema();
        schema.setName(projectName);
        schema.setPackageName(projectPackage);
        schema.setTables(new ArrayList<Table>());

        //*******************
        // table tag
        //*******************
        List<Element> tableElements = root.getChildren("table");
        Iterator<Element> iterTables = tableElements.iterator();
        while(iterTables.hasNext()) {
            Element eleTable = (Element) iterTables.next();
            Table tbl = new Table();
            tbl.setSchema(schema);
            tbl.setElement(eleTable);
            tbl.setColumns(new ArrayList<Column>());
            tbl.setPrimaryKeys(new ArrayList<Column>());
            tbl.setName(eleTable.getAttributeValue("name"));
            tbl.setFullName(eleTable.getAttributeValue("fullName"));
            tbl.setDataType(false);
            tbl.setBigDecimalType(false);
            tbl.setDataTypePk(false);
            System.out.println(tbl.getName());

            //*******************
            //  column tag
            //*******************
            List<Element> lstColumns = eleTable.getContent(new ElementFilter("column"));
            Iterator<Element> iterColumns = lstColumns.iterator();
            while(iterColumns.hasNext()) {
                Element eleColumn = (Element)iterColumns.next();
                Column column = new Column();
                column.setTable(tbl);
                column.setElement(eleColumn);
                column.setName(eleColumn.getAttributeValue("name"));
                String type = eleColumn.getAttributeValue("type");
                System.out.println("type=" + type);
                column.setType(type);
                String size = eleColumn.getAttributeValue("size");
                column.setSize(size);
                column.setFullName(eleColumn.getAttributeValue("fullName"));
                column.setDescription(eleColumn.getAttributeValue("description"));

                Attribute primaryKey = eleColumn.getAttribute("primaryKey");
                if (primaryKey != null && "true".equalsIgnoreCase(primaryKey.getValue())) {
                    tbl.getPrimaryKeys().add(column);
                    if ("DATE".equalsIgnoreCase(type)) {
                        tbl.setDataTypePk(true);
                    }
                } else {
                    tbl.getColumns().add(column);
                    if ("DATE".equalsIgnoreCase(type)) {
                        tbl.setDataType(true);
                    }
                    if ("NUMERIC".equalsIgnoreCase(type) && size.indexOf(",") > 0) {
                        tbl.setBigDecimalType(true);
                    }
                }
            } // iterColumns
            tbl.setTestCases(makeTestCase(tbl));
            schema.getTables().add(tbl);
        } // iterTables

        return schema;
    }
    

    private static List<TestCase> makeTestCase(Table tbl) {
        String sysDate = SystemDateTime.getDateTime();
        List<TestCase> result = new ArrayList<TestCase>();
        result.add(new TestCase("001", "count", "001", "テーブルの既存内容に影響がないことを確認するために、テスト実施前、対象テーブルの件数情報を取得する。（R処理）", "対象テーブル「」の件数は０件以上になっていること。", "Junit", "OK", sysDate, "BSJ", ""));
        result.add(new TestCase("002", "delete", "001", "", "", "Junit", "OK", sysDate, "BSJ", ""));
        result.add(new TestCase("003", "findById", "001", "", "", "Junit", "OK", sysDate, "BSJ", ""));
        result.add(new TestCase("004", "exists", "001", "", "", "Junit", "OK", sysDate, "BSJ", ""));
        result.add(new TestCase("005", "save", "001", "", "", "Junit", "OK", sysDate, "BSJ", ""));
        result.add(new TestCase("006", "findById", "002", "", "", "Junit", "OK", sysDate, "BSJ", ""));
        result.add(new TestCase("007", "exists", "002", "", "", "Junit", "OK", sysDate, "BSJ", ""));
        result.add(new TestCase("008", "save", "002", "", "", "Junit", "OK", sysDate, "BSJ", ""));
        result.add(new TestCase("009", "delete", "002", "", "", "Junit", "OK", sysDate, "BSJ", ""));
        result.add(new TestCase("010", "count", "002", "", "", "Junit", "OK", sysDate, "BSJ", ""));
        
        String key1 = tbl.getSchema().getPackageName() + "dao." + tbl.getTableId() + "ModelRepository";
        List<MethodMdl> methods1 = mapRMethods.get(key1);
        int cnt = 101;
        if (methods1 != null) {
            for (MethodMdl method : methods1) {
                result.add(new TestCase("" + cnt, method.getMethodName(), "001", "", "", "Junit", "OK", sysDate, "BSJ", ""));
                cnt ++;
            }
        }
        String key2 = tbl.getSchema().getPackageName() + "dao." + tbl.getTableId() + "ModelRepositoryCustom";
        List<MethodMdl> methods2 = mapRCMethods.get(key2);
        cnt = 201;
        if (methods2 != null) {
            for (MethodMdl method : methods2) {
                result.add(new TestCase("" + cnt, method.getMethodName(), "001", "", "", "Junit", "OK", sysDate, "BSJ", ""));
                cnt ++;
            }
        }
        return result;
    }
    /**
     * makeSource
     *
     * @param (String) servletName
     * @return (Schema) schema
     * @throws Exception
     */
    private static void makeSource(String servletName, Schema schema) throws Exception {

        Velocity.init();
        VelocityContext context = new VelocityContext();
        //******************
        // makeDir
        //******************
        String strModelDir = makeDirPath("Model", servletName, schema);
        makeDir(strModelDir);
        System.out.println(strModelDir);
        String strModelIdDir = makeDirPath("ModelId", servletName, schema);
        makeDir(strModelIdDir);
        System.out.println(strModelIdDir);
        String strModelRepositoryDir = makeDirPath("ModelRepository", servletName, schema);
        makeDir(strModelRepositoryDir);
        System.out.println(strModelRepositoryDir);
        String strModelRepositoryTestDir = makeDirPath("ModelRepositoryTest", servletName, schema);
        makeDir(strModelRepositoryTestDir);
        System.out.println(strModelRepositoryTestDir);
        String strTestCaseDir = makeDirPath("TestCase", servletName, schema);
        makeDir(strTestCaseDir);
        System.out.println(strTestCaseDir);

        // Tables
        List<Table> tables = schema.getTables();
        makeFile("./repository/templates/AllRepositoryTests.vm", tables, strModelRepositoryTestDir + "/AllRepositoryTests.java", context);
        for (int h = 0; h < tables.size(); h++) {
            Table table = (Table) tables.get(h);
            System.out.println("Table(" + (h + 1) + ")=" + table.getName());

            //******************
            // makeDir
            //******************
            String strTestCaseSubDir   = strTestCaseDir + table.getTableId() + "ModelRepository_テストケース.files/";
            makeDir(strTestCaseSubDir);
            System.out.println(strTestCaseSubDir);


            //******************
            //  makeFile
            //******************
            // Model
            makeFile("./repository/templates/Model.vm", table, strModelDir   + "/" + getIdFromName(table.getName()) + "Model.java", context);
            // ModelId
            makeFile("./repository/templates/ModelId.vm", table, strModelIdDir   + "/" + getIdFromName(table.getName()) + "ModelId.java", context);
            // ModelRepositorCustom
            context.put("methods200", mapRCMethods);
            makeFile("./repository/templates/ModelRepositoryCustom.vm", table, strModelRepositoryDir   + "/" + getIdFromName(table.getName()) + "ModelRepositoryCustom.java", context);
            // ModelRepository
            context.put("methods100", mapRMethods);
            makeFile("./repository/templates/ModelRepository.vm", table, strModelRepositoryDir   + "/" + getIdFromName(table.getName()) + "ModelRepository.java", context);
            // ModelRepositoryImpl
            makeFile("./repository/templates/ModelRepositoryImpl.vm", table, strModelRepositoryDir   + "/" + getIdFromName(table.getName()) + "ModelRepositoryImpl.java", context);
            // ModelRepositoryTest
            makeFile("./repository/templates/ModelRepositoryTests.vm", table, strModelRepositoryTestDir   + "/" + getIdFromName(table.getName()) + "ModelRepositoryTest.java", context);

            // TestCase
            makeFile("./repository/templates/Testcase.vm", table, strTestCaseDir + getIdFromName(table.getName()) + "ModelRepository_テストケース.htm", context, "SJIS");
            makeFile("./repository/templates/TestcaseSub01.vm", table, strTestCaseSubDir + getIdFromName(table.getName()) + "filelist.xml", context, "SJIS");
            makeFile("./repository/templates/TestcaseSub02.vm", table, strTestCaseSubDir + getIdFromName(table.getName()) + "sheet001.htm", context, "SJIS");
            makeFile("./repository/templates/TestcaseSub03.vm", table, strTestCaseSubDir + getIdFromName(table.getName()) + "sheet002.htm", context, "SJIS");
            makeFile("./repository/templates/TestcaseSub04.vm", table, strTestCaseSubDir + getIdFromName(table.getName()) + "stylesheet.css", context, "SJIS");
            makeFile("./repository/templates/TestcaseSub05.vm", table, strTestCaseSubDir + getIdFromName(table.getName()) + "tabstrip.htm", context, "SJIS");
            
        } // Tables
    }


    /**
     * makeDirPath
     *
     * @param (UseCase) UseCase
     * @return (String) Path
     * @throws Exception
     */
    private static String makeDirPath(String type, String servletName, Schema schema) throws Exception {
        String strDirPath = "";

        String packageName = schema.getPackageName();
        packageName = packageName.replace('.',  '/');
 

        if("Model".equals(type)) {
            strDirPath = "output/" + servletName + "/src/main/java/" + packageName.toLowerCase() + "/entity/";
        } else if ("ModelId".equals(type)) {
            strDirPath = "output/" + servletName + "/src/main/java/" + packageName.toLowerCase() + "/entity/pk/";
        }else if ("ModelRepositoryCustom".equals(type)){
            strDirPath = "output/" + servletName + "/src/main/java/" + packageName.toLowerCase() + "/repository/";
        }else if ("ModelRepository".equals(type)){
            strDirPath = "output/" + servletName + "/src/main/java/" + packageName.toLowerCase() + "/repository/";
        }else if ("RepositoryImpl".equals(type)){
            strDirPath = "output/" + servletName + "/src/main/java/" + packageName.toLowerCase() + "/repository/";
        }else if ("ModelRepositoryTest".equals(type)){
            strDirPath = "output/" + servletName + "/src/test/java/" + packageName.toLowerCase() + "/repository/";
        } else if("TestCase".equals(type)) {
            strDirPath = "output/" + servletName + "/testcase/";
        }

        return strDirPath;
    }

    /**
     * makeDir
     *
     * @param (String) dirPath
     * @return
     * @throws Exception
     */
    private static void makeDir(String dirPath) throws Exception {
        File file = new File(dirPath);
        file.mkdirs();
    }

    /**
     * makeFile
     *
     * @param (String) vmPath
     * @return
     * @throws Exception
     */
    private static void makeFile(String vmPath, Object obj, String filePath,
            VelocityContext context) throws Exception {
        makeFile(vmPath, obj, filePath, context, "UTF8");
    }
    private static void makeFile(String vmPath, Object obj, String filePath,
            VelocityContext context, String charsetName) throws Exception {
        org.apache.velocity.Template tmpVm;
        try {
            tmpVm = Velocity.getTemplate(vmPath, charsetName);
        } catch ( ResourceNotFoundException re ) {
            System.out.println("Template file is not exist!" + re);
            return;
        } catch ( Exception e ) {
            throw e;
        }

        if(obj instanceof Table) {
            context.put("table", obj);
        } else if ( obj instanceof List ){
            context.put("tables", obj);
            @SuppressWarnings("unchecked")
            List<Table> list = (List<Table>) obj;
            if (list.size() > 0) {
                context.put("table", list.get(0));
            }
        }
        System.out.println("@_@obj=" + obj.toString());
        System.out.println("@_@vmPath=" + vmPath);
        System.out.println("@_@filePath=" + filePath);
        System.out.println("");

        StringWriter sw = new StringWriter();
        tmpVm.merge( context, sw );

        FileWriter fw = new FileWriter(filePath);
        fw.write(sw.toString());
        fw.close();
        sw.close();
    }
    public static String getIdFromName(String name) {
        if (name == null) {
            System.out.println("name=" + name);
            return null;
        }
        String lower = name.toLowerCase();
        String upper = name.toUpperCase();
        StringBuffer buf = new StringBuffer();
        int len = name.length();
        int flag = 0;
        char ch = 0;
        for (int idx = 0; idx < len; idx ++) {
            if (idx == 0) {
                flag = 1;
            }
            ch = name.charAt(idx);
            if (ch == '_') {
                flag = 1;
                continue;
            }
            if (flag == 1) {
                buf.append(upper.charAt(idx));
            } else {
                buf.append(lower.charAt(idx));
            }
            flag = 0;
        }
        System.out.println("name=" + name + ",result=" + buf);
        return buf.toString();
    }
    public static String getVarFromName(String name) {
        if (name == null) {
            System.out.println("name=" + name);
            return null;
        }
        String lower = name.toLowerCase();
        String upper = name.toUpperCase();
        StringBuffer buf = new StringBuffer();
        int len = name.length();
        int flag = 0;
        char ch = 0;
        for (int idx = 0; idx < len; idx ++) {
            ch = name.charAt(idx);
            if (ch == '_') {
                flag = 1;
                continue;
            }
            if (flag == 1) {
                buf.append(upper.charAt(idx));
            } else {
                buf.append(lower.charAt(idx));
            }
            flag = 0;
        }
        System.out.println("name=" + name + ",result=" + buf);
        return buf.toString();
    }
    public static String getType(String type) {
        String res = map.get(type);
        if (res == null | res.length() <= 0) {
            System.out.println("------" + type);
        }
        return res;
    }
}

