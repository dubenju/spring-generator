package org.springframework.orm.jpa.generator.model;

import java.util.List;

import org.jdom2.Element;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class View {
    private String viewId;
    private String viewName;
    private String control;
    
    private Gyomu gyomu;
    private Element element;
    private List<Event> events;
    private List<SubView> subViews;
    private List<Prop> props;
}
