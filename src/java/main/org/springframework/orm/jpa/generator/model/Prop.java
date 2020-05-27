package org.springframework.orm.jpa.generator.model;

import org.jdom2.Element;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Prop {

    private String key;
    private String value;
    private View view;
    private Element element;
}
