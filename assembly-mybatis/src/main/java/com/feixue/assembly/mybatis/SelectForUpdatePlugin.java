package com.feixue.assembly.mybatis;

import com.feixue.assembly.mybatis.utils.FormatTools;
import com.feixue.assembly.mybatis.utils.JavaElementGeneratorTools;
import com.feixue.assembly.mybatis.utils.XmlElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.Set;
import java.util.TreeSet;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

public class SelectForUpdatePlugin extends BasePlugin {
    public static final String METHOD_SELECT_FORUPDATE_BY_EXAMPLE = "selectForUpdateByExample";  // 方法名
    public static final String METHOD_SELECT_FORUPDATE_BY_EXAMPLE_WITH_BLOBS = "selectForUpdateByExampleWithBLOBs";  // 方法名
    private XmlElement selectForUpdateByExampleEle;
    private XmlElement selectForUpdateByExampleWithBLOBsEle;

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        super.initialized(introspectedTable);
        // bug:26,27
        this.selectForUpdateByExampleWithBLOBsEle = null;
        this.selectForUpdateByExampleEle = null;
    }

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        // 方法生成 selectForUpdateByExample
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        importedTypes.add(type);
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());

        Method selectForUpdateMethod = new Method();
        selectForUpdateMethod.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType listType;
        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            listType = new FullyQualifiedJavaType(introspectedTable.getRecordWithBLOBsType());
        } else {
            // the blob fields must be rolled up into the base class
            listType = new FullyQualifiedJavaType(introspectedTable
                    .getBaseRecordType());
        }

        importedTypes.add(listType);
        returnType.addTypeArgument(listType);
        selectForUpdateMethod.setReturnType(returnType);
        selectForUpdateMethod.setName(METHOD_SELECT_FORUPDATE_BY_EXAMPLE_WITH_BLOBS);
        selectForUpdateMethod.addParameter(new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example"));

        FormatTools.addMethodWithBestPosition(interfaze, selectForUpdateMethod);

        return super.clientSelectByExampleWithBLOBsMethodGenerated(selectForUpdateMethod, interfaze, introspectedTable);
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        // 方法生成 selectForUpdateByExample
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        importedTypes.add(type);
        importedTypes.add(FullyQualifiedJavaType.getNewListInstance());

        Method selectForUpdateMethod = new Method();
        selectForUpdateMethod.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType listType;
        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            listType = new FullyQualifiedJavaType(introspectedTable.getRecordWithBLOBsType());
        } else {
            // the blob fields must be rolled up into the base class
            listType = new FullyQualifiedJavaType(introspectedTable
                    .getBaseRecordType());
        }

        importedTypes.add(listType);
        returnType.addTypeArgument(listType);
        selectForUpdateMethod.setReturnType(returnType);
        selectForUpdateMethod.setName(METHOD_SELECT_FORUPDATE_BY_EXAMPLE);
        selectForUpdateMethod.addParameter(new Parameter(new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example"));

        FormatTools.addMethodWithBestPosition(interfaze, selectForUpdateMethod);

        return super.clientSelectByExampleWithoutBLOBsMethodGenerated(selectForUpdateMethod, interfaze, introspectedTable);
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        // ------------------------------------ selectForUpdateByExample ----------------------------------
        // 生成查询语句
        XmlElement selectForUpdateElement = new XmlElement("select");

        // 添加ID
        selectForUpdateElement.addAttribute(new Attribute("id", METHOD_SELECT_FORUPDATE_BY_EXAMPLE));
        // 添加返回类型
        selectForUpdateElement.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
        // 添加参数类型
        selectForUpdateElement.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));
        selectForUpdateElement.addElement(new TextElement("select"));

        StringBuilder sb = new StringBuilder();
        if (stringHasValue(introspectedTable.getSelectByExampleQueryId())) {
            sb.append('\'');
            sb.append(introspectedTable.getSelectByExampleQueryId());
            sb.append("' as QUERYID,");
            selectForUpdateElement.addElement(new TextElement(sb.toString()));
        }
        selectForUpdateElement.addElement(XmlElementGeneratorTools.getBaseColumnListElement(introspectedTable));

        sb.setLength(0);
        sb.append("from ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        selectForUpdateElement.addElement(new TextElement(sb.toString()));
        selectForUpdateElement.addElement(XmlElementGeneratorTools.getExampleIncludeElement(introspectedTable));

        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "orderByClause != null"));  //$NON-NLS-2$
        ifElement.addElement(new TextElement("order by ${orderByClause}"));
        selectForUpdateElement.addElement(ifElement);

        // 只查询一条
        selectForUpdateElement.addElement(new TextElement("for update"));
        this.selectForUpdateByExampleEle = selectForUpdateElement;
        return super.sqlMapSelectByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        // 生成查询语句
        XmlElement selectForUpdateWithBLOBsElement = new XmlElement("select");

        // 添加ID
        selectForUpdateWithBLOBsElement.addAttribute(new Attribute("id", METHOD_SELECT_FORUPDATE_BY_EXAMPLE_WITH_BLOBS));
        // 添加返回类型
        selectForUpdateWithBLOBsElement.addAttribute(new Attribute("resultMap", introspectedTable.getResultMapWithBLOBsId()));
        // 添加参数类型
        selectForUpdateWithBLOBsElement.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));
        // 添加查询SQL
        selectForUpdateWithBLOBsElement.addElement(new TextElement("select"));

        StringBuilder sb = new StringBuilder();
        if (stringHasValue(introspectedTable.getSelectByExampleQueryId())) {
            sb.append('\'');
            sb.append(introspectedTable.getSelectByExampleQueryId());
            sb.append("' as QUERYID,");
            selectForUpdateWithBLOBsElement.addElement(new TextElement(sb.toString()));
        }

        selectForUpdateWithBLOBsElement.addElement(XmlElementGeneratorTools.getBaseColumnListElement(introspectedTable));
        selectForUpdateWithBLOBsElement.addElement(new TextElement(","));
        selectForUpdateWithBLOBsElement.addElement(XmlElementGeneratorTools.getBlobColumnListElement(introspectedTable));

        sb.setLength(0);
        sb.append("from ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        selectForUpdateWithBLOBsElement.addElement(new TextElement(sb.toString()));
        selectForUpdateWithBLOBsElement.addElement(XmlElementGeneratorTools.getExampleIncludeElement(introspectedTable));

        XmlElement ifElement1 = new XmlElement("if");
        ifElement1.addAttribute(new Attribute("test", "orderByClause != null"));  //$NON-NLS-2$
        ifElement1.addElement(new TextElement("order by ${orderByClause}"));
        selectForUpdateWithBLOBsElement.addElement(ifElement1);

        // 只查询一条
        selectForUpdateWithBLOBsElement.addElement(new TextElement("for update"));

        this.selectForUpdateByExampleWithBLOBsEle = selectForUpdateWithBLOBsElement;
        return super.sqlMapSelectByExampleWithBLOBsElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        if (selectForUpdateByExampleEle != null) {
            FormatTools.addElementWithBestPosition(document.getRootElement(), selectForUpdateByExampleEle);
        }

        if (selectForUpdateByExampleWithBLOBsEle != null) {
            FormatTools.addElementWithBestPosition(document.getRootElement(), selectForUpdateByExampleWithBLOBsEle);
        }

        return true;
    }
}
