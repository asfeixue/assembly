package com.feixue.assembly.mybatis;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

public class GeneratorGlobalJSONFormat extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        if ("TIMESTAMP".equalsIgnoreCase(introspectedColumn.getJdbcTypeName())) {
            process(field, topLevelClass, introspectedColumn);
        }

        return super.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }

    private void process(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn) {
        String jsonType =  properties.getProperty("jsonType");
        String datePattern = properties.getProperty("datePattern");
        if(null != datePattern) {
            String prefix = "@JsonFormat(pattern = ";

            if(null == jsonType) {
                jsonType = "com.fasterxml.jackson.annotation.JsonFormat";
                prefix = "@JsonFormat(pattern = ";
            } else if ("jackson".equalsIgnoreCase(jsonType)) {
                jsonType = "com.fasterxml.jackson.annotation.JsonFormat";
                prefix = "@JsonFormat(pattern = ";
            } else if ("fastjson".equalsIgnoreCase(jsonType)) {
                jsonType = "com.alibaba.fastjson.annotation.JSONField";
                prefix = "@JSONField(format = ";
            } else {
                return;
            }
            topLevelClass.addImportedType(jsonType);

            field.addAnnotation(prefix + "\"" + datePattern + "\")");
        }
    }
}
