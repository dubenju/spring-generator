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
import org.springframework.orm.jpa.generator.model.Event;
import org.springframework.orm.jpa.generator.model.Gyomu;
import org.springframework.orm.jpa.generator.model.Project;
import org.springframework.orm.jpa.generator.model.Prop;
import org.springframework.orm.jpa.generator.model.Schema;
import org.springframework.orm.jpa.generator.model.SubView;
import org.springframework.orm.jpa.generator.model.Table;
import org.springframework.orm.jpa.generator.model.View;

public class GeneratorView {

    public static void main(String[] args) throws Exception {
        System.out.println("***** Generator Begin *****");
        String xmlPath = "";
        String servletName = "";
        String projectName = "";
        String projectPackage = "";
        boolean debug_flag = false;
        boolean error_flag = false;
        for (int i = 0; i < args.length; i ++) {
            if (i ==0) {
                xmlPath = args[i];
            } else if (i == 1) {
                servletName  = args[i];
            } else if (i == 2) {
                projectName  = args[i];
            } else if (i == 3) {
                projectPackage  = args[i];
            } else if (i == 4) {
                debug_flag  = args[i].equals("1");
            }
        }

        if (xmlPath == null || xmlPath.length() <= 0){
            xmlPath = "./usecase/schema/schema.xml";
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
            projectPackage = "org.springframework";
        }else{
            if (!projectPackage.substring(projectPackage.length() -1).equals(".")){
                projectPackage = projectPackage + ".";
            }
        }
        System.out.println("projectPackage=" + projectPackage);
        if (!debug_flag && error_flag) {
            throw new Exception("ERROR:\napl xml servletname projectname packagename debug");
        }
        File in = new File(xmlPath);
        SAXBuilder builder = new SAXBuilder();
        Document xml = builder.build(in);
        System.out.println("read xml file end.");
        Project schema = docFormatToList(xml, projectName, projectPackage);
        // TODO:lombock的toString可能有递归的问题
        //System.out.println(schema);
        System.out.println("parse xml file end.");
        if (!debug_flag) {
            makeSource(servletName, schema);
        }

        System.out.println("***** Generator Normal End *****");

    }
    private static Project docFormatToList(Document xml, String projectName, String projectPackage) throws Exception {
        //*******************
        //  database tag
        //*******************
        Element root = xml.getRootElement();
        Project project = new Project();
        project.setName(projectName);
        project.setPackageName(projectPackage);
        project.setGyomus(new ArrayList<Gyomu>());

        //*******************
        // Gyomu tag
        //*******************
        List<Element> lstGyomus = root.getChildren("gyomu");
        Iterator<Element> iterGyomus = lstGyomus.iterator();
        while(iterGyomus.hasNext()) {
            Element eleGyomu = (Element) iterGyomus.next();
            Gyomu gyomu = new Gyomu();
            gyomu.setProject(project);
            gyomu.setElement(eleGyomu);
            gyomu.setViews(new ArrayList<View>());
            gyomu.setFnctionId(eleGyomu.getAttributeValue("FnctionId"));
            gyomu.setFnctionName(eleGyomu.getAttributeValue("FnctionName"));
            gyomu.setExtendsClass(eleGyomu.getAttributeValue("extends"));

            //*******************
            //  View tag
            //*******************
            List<Element> lstViews = eleGyomu.getContent(new ElementFilter("view"));
            Iterator<Element> iterViews = lstViews.iterator();
            while(iterViews.hasNext()) {
                Element eleView = (Element)iterViews.next();
                View view = new View();
                view.setGyomu(gyomu);
                view.setElement(eleView);
                view.setViewId(eleView.getAttributeValue("viewId"));
                view.setViewName(eleView.getAttributeValue("viewName"));
                view.setControl(eleView.getAttributeValue("control"));
                view.setEvents(new ArrayList<Event>());
                view.setProps(new ArrayList<Prop>());
                view.setSubViews(new ArrayList<SubView>());

                //*******************
                //  Event tag
                //*******************
                List<Element> lstEvents = eleView.getContent(new ElementFilter("event"));
                Iterator<Element> iterEvents = lstEvents.iterator();
                while(iterEvents.hasNext()) {
                    Element eleEvent = (Element)iterEvents.next();
                    Event event = new Event();
                    event.setView(view);
                    event.setElement(eleEvent);
                    event.setName(eleEvent.getAttributeValue("name"));
                    event.setValue(eleEvent.getAttributeValue("value"));
                    event.setMethod(eleEvent.getAttributeValue("method"));
                    event.setIndex(eleEvent.getAttributeValue("index"));
                    view.getEvents().add(event);
                }
                //*******************
                //  Prop tag
                //*******************
                List<Element> lstProps = eleView.getContent(new ElementFilter("prop"));
                Iterator<Element> iterProps = lstProps.iterator();
                while(iterProps.hasNext()) {
                    Element eleProp = (Element)iterProps.next();
                    Prop prop = new Prop();
                    prop.setView(view);
                    prop.setElement(eleProp);
                    prop.setKey(eleProp.getAttributeValue("name"));
                    prop.setValue(eleProp.getAttributeValue("value"));
                    view.getProps().add(prop);
                }
                //*******************
                //  SubView tag
                //*******************
                List<Element> lstSubViews = eleView.getContent(new ElementFilter("subView"));
                Iterator<Element> iterSubViews = lstSubViews.iterator();
                while(iterSubViews.hasNext()) {
                    Element eleSubView = (Element)iterSubViews.next();
                    SubView subView = new SubView();
                    subView.setView(view);
                    subView.setElement(eleSubView);
                    subView.setControl(eleSubView.getAttributeValue("control"));
                    subView.setEvents(new ArrayList<Event>());
                    //*******************
                    //  event tag
                    //*******************
                    List<Element> lstEvents2 = eleSubView.getContent(new ElementFilter("event"));
                    Iterator<Element> iterEvents2 = lstEvents2.iterator();
                    while(iterEvents2.hasNext()) {
                        Element eleEvent2 = (Element)iterEvents2.next();
                        Event event2 = new Event();
                        event2.setView(view);
                        event2.setElement(eleEvent2);
                        event2.setName(eleEvent2.getAttributeValue("name"));
                        event2.setValue(eleEvent2.getAttributeValue("value"));
                        event2.setMethod(eleEvent2.getAttributeValue("method"));
                        event2.setIndex(eleEvent2.getAttributeValue("index"));
                        subView.getEvents().add(event2);
                    }
                    view.getSubViews().add(subView);
                }
                gyomu.getViews().add(view);
            } // iterColumns
            project.getGyomus().add(gyomu);
        } // iterTables

        return project;
    }
    private static void makeSource(String servletName, Project project) throws Exception {

        Velocity.init();
        VelocityContext context = new VelocityContext();
        List<Gyomu> gyomus = project.getGyomus();
        for (int h0 = 0; h0 < gyomus.size();h0 ++ ) {
            Gyomu gyomu = gyomus.get(h0);
            System.out.println("Gyomu(" + (h0 + 1) + ")=" + gyomu.getFnctionId());
            //******************
            // makeDir
            //******************
            String strViewHtmlDir = makeDirPath("ViewHtml", servletName, project, gyomu);
            makeDir(strViewHtmlDir);
            System.out.println(strViewHtmlDir);
            String strViewJsDir = makeDirPath("ViewJs", servletName, project, gyomu);
            makeDir(strViewJsDir);
            System.out.println(strViewJsDir);
            String strControllerWebDir = makeDirPath("ControllerWeb", servletName, project, gyomu);
            makeDir(strControllerWebDir);
            System.out.println(strControllerWebDir);
            String strControllerBeanDir = makeDirPath("ControllerBean", servletName, project, gyomu);
            makeDir(strControllerBeanDir);
            System.out.println(strControllerBeanDir);
            String strModelServiceDir = makeDirPath("ModelService", servletName, project, gyomu);
            makeDir(strModelServiceDir);
            System.out.println(strModelServiceDir);
            String strModelBeanDir = makeDirPath("ModelBean", servletName, project, gyomu);
            makeDir(strModelBeanDir);
            System.out.println(strModelBeanDir);

            // Tables
            List<View> views = gyomu.getViews();
            makeFile("./usecase/templates/ControllerWeb.vm", gyomu, strControllerWebDir + "/" + gyomu.getFnctionId() + "Controller.java", context);
            for (int h = 0; h < views.size(); h++) {
                View view = (View) views.get(h);
                System.out.println("View(" + (h + 1) + ")=" + view.getViewName());
                makeFile("./usecase/templates/ViewHtml.vm", view, strViewHtmlDir + "/" + view.getViewId() + ".html", context);
                makeFile("./usecase/templates/ViewJs.vm", view, strViewJsDir + "/" + view.getViewId() + ".js", context);
                makeFile("./usecase/templates/ControllerWebView.vm", view, strControllerWebDir + "/" + view.getViewId() + view.getControl() + ".java", context);
                List<SubView> subViews = view.getSubViews();
                for (int k = 0; k < subViews.size(); k ++) {
                    SubView subView = subViews.get(k);
                    makeFile("./usecase/templates/ControllerWebSubView.vm", subView, strControllerWebDir + "/" + view.getViewId() + subView.getControl() + ".java", context);
                    List<Event> events1 = subView.getEvents();
                    for (int cntE1 = 0; cntE1 < events1.size(); cntE1 ++) {
                        Event event1 = events1.get(cntE1);
                        makeFile("./usecase/templates/ModelInputBean.vm", event1, strModelBeanDir + "/" + view.getViewId() + event1.getVarname() + "InputBean.java", context);
                        makeFile("./usecase/templates/ModelOutputBean.vm", event1, strModelBeanDir + "/" + view.getViewId() + event1.getVarname() + "OutputBean.java", context);
                    }
                }
                //******************
                //  makeFile
                //******************
                // ControllerBean
                makeFile("./usecase/templates/ControllerBean.vm", view, strControllerBeanDir   + "/" + view.getViewId() + "Form.java", context);
                List<Event> events1 = view.getEvents();
                for (int cntE1 = 0; cntE1 < events1.size(); cntE1 ++) {
                    Event event1 = events1.get(cntE1);
                    // System.out.println("事件：" + event1);
                    makeFile("./usecase/templates/ModelInputBean.vm", event1, strModelBeanDir + "/" + view.getViewId() + event1.getVarname() + "InputBean.java", context);
                    makeFile("./usecase/templates/ModelOutputBean.vm", event1, strModelBeanDir + "/" + view.getViewId() + event1.getVarname() + "OutputBean.java", context);
                }
                // Service
                makeFile("./usecase/templates/ModelService.vm", gyomu, strModelServiceDir   + "/" + gyomu.getFnctionId() + "Service.java", context);
            } // Gyomus
        }
        String strControllerPathInfoDir = makeDirPath("ControllerPathInfo", servletName, project, null);
        makeFile("./usecase/templates/BaseControllerPathInfo.vm", gyomus, strControllerPathInfoDir + "/BaseControllerPathInfo.java", context);
        String strPropDIr = makeDirPath("Prop", servletName, project, null);
        makeFile("./usecase/templates/Prop.vm", gyomus, strPropDIr + "/application.properties", context);
    }
    private static String makeDirPath(String type, String servletName, Project schema, Gyomu gyomu) throws Exception {
        String strDirPath = "";

        String packageName = schema.getPackageName();
        packageName = packageName.replace('.',  '/');
 

        if("ControllerWeb".equals(type)) {
            strDirPath = "output/" + servletName + "/src/main/java/" + packageName.toLowerCase() + "/controller/web/" + gyomu.getFnctionId().toUpperCase();
        } else if ("ControllerBean".equals(type)) {
            strDirPath = "output/" + servletName + "/src/main/java/" + packageName.toLowerCase() + "/controller/bean/"+ gyomu.getFnctionId().toUpperCase();
        }else if ("ModelService".equals(type)){
            strDirPath = "output/" + servletName + "/src/main/java/" + packageName.toLowerCase() + "/model/service/"+ gyomu.getFnctionId().toUpperCase();
        }else if ("ModelBean".equals(type)){
            strDirPath = "output/" + servletName + "/src/main/java/" + packageName.toLowerCase() + "/model/bean/"+ gyomu.getFnctionId().toUpperCase();
        }else if ("ViewHtml".equals(type)){
            strDirPath = "output/" + servletName + "/src/main/resources/templates/" + gyomu.getFnctionId().toUpperCase();
        }else if ("ViewJs".equals(type)){
            strDirPath = "output/" + servletName + "/src/main/resources/static/js/" + gyomu.getFnctionId().toUpperCase();
        } else if("ControllerPathInfo".equals(type)) {
            strDirPath = "output/" + servletName + "/src/main/java/" + packageName.toLowerCase() + "/controller/web/";
        } else if("Prop".equals(type)) {
            strDirPath = "output/" + servletName + "/src/main/resources/";
        }

        return strDirPath;
    }
    private static void makeDir(String dirPath) throws Exception {
        File file = new File(dirPath);
        file.mkdirs();
    }
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

        if(obj instanceof Gyomu) {
            context.put("gyomu", obj);
        } else if(obj instanceof View) {
            context.put("view", obj);
        } else if(obj instanceof SubView) {
            context.put("subView", obj);
        } else if(obj instanceof Event) {
            context.put("event", obj);
        } else if ( obj instanceof List ){
            context.put("gyomus", obj);
            @SuppressWarnings("unchecked")
            List<Table> list = (List<Table>) obj;
            if (list.size() > 0) {
                context.put("gyomu", list.get(0));
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
    public static String getVarFromName(String name) {
        if (name == null) {
            System.out.println("name=" + name);
            return name;
        }
        if (name.length() <= 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
