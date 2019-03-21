/**
 * 
 */
package org.springframework.orm.jpa.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.ArrayList;
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
/**
 * @author DBJ
 *
 */
public class Generator {

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
                xmlPath = "..\\repository\\schema\\sample-schema.xml";
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

            Document xml = getJdomDocument(xmlPath);
            Schema schema = docFormatToList(xml, projectName, projectPackage);
            // TODO:lombock的toString可能有递归的问题
            //System.out.println(schema);

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

                tbl.getColumns().add(column);
                Attribute primaryKey = eleColumn.getAttribute("primaryKey");
                if (primaryKey != null && "true".equalsIgnoreCase(primaryKey.getValue())) {
                    tbl.getPrimaryKeys().add(column);
                }
            } // iterColumns
            schema.getTables().add(tbl);
        } // iterTables

        return schema;
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

        // Tables
        List<Table> tables = schema.getTables();
        for (int h = 0; h < tables.size(); h++) {
            Table table = (Table) tables.get(h);
            System.out.println("Table(" + (h + 1) + ")=" + table.getName());

            //******************
            // makeDir
            //******************
            String strCsvVmDir   = makeDirPath("CSV",     servletName, useCase);
            makeDir(strCsvVmDir);


            //******************
            //  makeFile
            //******************
            // Model
            makeFile("templates/Model.vm", download, strCsvVmDir   + "/" + download.getDownloadID() + "Model.java", context);
            // ModelId
            makeFile("templates/ModelId.vm", download, strCsvVmDir   + "/" + download.getDownloadID() + "ModelId.java", context);
            // Repositor
            makeFile("templates/Repositor.vm", download, strCsvVmDir   + "/" + download.getDownloadID() + "Repositor.java", context);
            // Repositor
            makeFile("templates/RepositorCustom.vm", download, strCsvVmDir   + "/" + download.getDownloadID() + "RepositorCustom.java", context);
        } // Tables
    }


    /**
     * makeDirPath
     *
     * @param (UseCase) UseCase
     * @return (String) Path
     * @throws Exception
     */
    private static String makeDirPath(String Pl, String servletName, UseCase useCase) throws Exception {
        String plDirPath = "";

        SubSystem subSystem = useCase.getSubSystem();
        BasePackage basePackage = subSystem.getBasePackage();
        Project project = basePackage.getProject();

        String projectPackage = project.getProjectPackage();
        projectPackage = projectPackage.replace('.','/');

        String usecasePackage = useCase.getUsecasePackage();
        if (usecasePackage == null || usecasePackage.length() <= 0){
            usecasePackage = "";
        }else{
            usecasePackage = usecasePackage.replace('.','/');
        }

        if(Pl.equals("System")) {
            plDirPath = "output/" + servletName + "/" + "WEB-INF/src/java/" + projectPackage + basePackage.getName() + "/" + "modules/system/" + subSystem.getName() + "/" + usecasePackage + useCase.getName().toLowerCase();
        } else if (Pl.equals("Action")) {
            plDirPath = "output/" + servletName + "/WEB-INF/src/java/" + projectPackage + basePackage.getName() + "/modules/actions/" + subSystem.getName() + "/" + usecasePackage + useCase.getName().toLowerCase();

        }else if (Pl.equals("Object")){
            plDirPath = "output/" + servletName + "/WEB-INF/src/java/" + projectPackage + basePackage.getName() + "/modules/objects/" + subSystem.getName() + "/" + usecasePackage + useCase.getName().toLowerCase();

        }else if (Pl.equals("Screen")){
            plDirPath = "output/" + servletName + "/WEB-INF/src/java/" + projectPackage + basePackage.getName() + "/modules/screens/" + subSystem.getName() + "/" + usecasePackage + useCase.getName().toLowerCase();

        }else if (Pl.equals("JS")){
            plDirPath = "output/" + servletName + "/resources/ui/skins/" + basePackage.getName() + "/images/" + subSystem.getName() + "/" + usecasePackage + useCase.getName().toLowerCase();

        }else if (Pl.equals("VM")){
            plDirPath = "output/" + servletName + "/templates/" + basePackage.getName() + "/screens/" + subSystem.getName() + "/" + usecasePackage + useCase.getName().toLowerCase();
        } else if(Pl.equals("PDF")) {
            plDirPath = "output/" + servletName + "/" + "templates/" + project.getName() + "/" + "screens/" + subSystem.getName() + "/download/pdf";
        } else if(Pl.equals("EXCEL")) {
            plDirPath = "output/" + servletName + "/" + "templates/" + project.getName() + "/" + "screens/" + subSystem.getName() + "/download/excel";
        } else if(Pl.equals("CSV")) {
            plDirPath = "output/" + servletName + "/" + "templates/" + project.getName() + "/" + "screens/" + subSystem.getName() + "/download/csv";
        }

        return plDirPath;
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
        org.apache.velocity.Template tmpVm;
        try {
            tmpVm = Velocity.getTemplate(vmPath, "SJIS");
        } catch ( ResourceNotFoundException re ) {
            return;
        } catch ( Exception e ) {
            throw e;
        }

        if(obj instanceof DownLoad) {
            context.put("download", obj);
        } else if ( obj instanceof Template ){
            context.put("template", obj);
        } else if ( obj instanceof UseCase ){
            context.put("useCase", obj);
            context.put("subSystem", ((UseCase)obj).getSubSystem());
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
}
