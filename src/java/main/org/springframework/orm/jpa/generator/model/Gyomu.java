package org.springframework.orm.jpa.generator.model;

import java.util.List;

import org.jdom2.Element;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Gyomu {
    private String fnctionId;
    private String fnctionName;
    private String extendsClass;
    
    private Project project;
    private Element element;
    private List<View> views;
}
