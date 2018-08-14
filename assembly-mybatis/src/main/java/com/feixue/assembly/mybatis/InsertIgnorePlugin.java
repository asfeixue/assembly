package com.feixue.assembly.mybatis;

import com.feixue.assembly.mybatis.utils.FormatTools;
import com.feixue.assembly.mybatis.utils.JavaElementGeneratorTools;
import com.feixue.assembly.mybatis.utils.XmlElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;
import org.mybatis.generator.config.GeneratedKey;

import java.util.List;

public class InsertIgnorePlugin extends BasePlugin {

    public static final String METHOD_INSERT_IGNORE = "insertIgnore";  // 方法名

    @Override
    public boolean validate(List<String> warnings) {
        // 该插件只支持MYSQL
        if ("com.mysql.jdbc.Driver".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false
                && "com.mysql.cj.jdbc.Driver".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false) {
            warnings.add("assembly mybatis:插件" + this.getClass().getTypeName() + "只支持MySQL数据库！");
            return false;
        }
        return super.validate(warnings);
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        Method mUpsert = JavaElementGeneratorTools.generateMethod(
                METHOD_INSERT_IGNORE,
                JavaVisibility.DEFAULT,
                FullyQualifiedJavaType.getIntInstance(),
                new Parameter(JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable), "record")
        );

        // interface 增加方法
        FormatTools.addMethodWithBestPosition(interfaze, mUpsert);

        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        XmlElement insertIgnoreElement = new XmlElement("insert");

        //添加ID
        insertIgnoreElement.addAttribute(new Attribute("id", METHOD_INSERT_IGNORE));
        // 参数类型
        FullyQualifiedJavaType parameterType = JavaElementGeneratorTools.getModelTypeWithoutBLOBs(introspectedTable);

        insertIgnoreElement.addAttribute(new Attribute("parameterType", //$NON-NLS-1$
                parameterType.getFullyQualifiedName()));

//        insertIgnoreElement.addAttribute(new Attribute("parameterType", "map"));

        GeneratedKey gk = introspectedTable.getGeneratedKey();
        if (gk != null) {
            IntrospectedColumn introspectedColumn = introspectedTable
                    .getColumn(gk.getColumn());
            // if the column is null, then it's a configuration error. The
            // warning has already been reported
            if (introspectedColumn != null) {
                if (gk.isJdbcStandard()) {
                    insertIgnoreElement.addAttribute(new Attribute(
                            "useGeneratedKeys", "true")); //$NON-NLS-1$ //$NON-NLS-2$
                    insertIgnoreElement.addAttribute(new Attribute(
                            "keyProperty", introspectedColumn.getJavaProperty())); //$NON-NLS-1$
                    insertIgnoreElement.addAttribute(new Attribute(
                            "keyColumn", introspectedColumn.getActualColumnName())); //$NON-NLS-1$
                } else {
                    insertIgnoreElement.addElement(new XMLElementGenerator().getSelectKeyPublic(introspectedColumn, gk));
                }
            }
        }

        //insert
        insertIgnoreElement.addElement(new TextElement("insert ignore into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));

        for (Element element : XmlElementGeneratorTools.generateKeys(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), true)) {
            insertIgnoreElement.addElement(element);
        }

        insertIgnoreElement.addElement(new TextElement("values"));

        for (Element element : XmlElementGeneratorTools.generateValues(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), "")) {
            insertIgnoreElement.addElement(element);
        }

        document.getRootElement().addElement(insertIgnoreElement);

        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    private static class XMLElementGenerator extends AbstractXmlElementGenerator {

        public XmlElement getSelectKeyPublic(IntrospectedColumn introspectedColumn,
                                       GeneratedKey generatedKey) {
            return this.getSelectKey(introspectedColumn, generatedKey);
        }

        @Override
        public void addElements(XmlElement parentElement) {

        }
    }
}
