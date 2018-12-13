package com.feixue.assembly.mybatis;

import com.feixue.assembly.mybatis.utils.FormatTools;
import com.feixue.assembly.mybatis.utils.JavaElementGeneratorTools;
import com.feixue.assembly.mybatis.utils.XmlElementGeneratorTools;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;
import org.mybatis.generator.config.GeneratedKey;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BatchInsertPlugin extends BasePlugin {

    public static final String METHOD_BATCH_INSERT = "batchInsert";
    public static final String METHOD_BATCH_INSERT_SELECTIVE = "batchInsertSelective";
    public static final String METHOD_BATCH_INSERT_IGNORE = "batchInsertIgnore";
    public static final String METHOD_BATCH_INSERT_IGNORE_SELECTIVE = "batchInsertIgnoreSelective";
    public static final String PRO_ALLOW_MULTI_QUERIES = "allowMultiQueries";   // property allowMultiQueries
    private boolean allowMultiQueries = false;  // 是否允许多sql提交

    @Override
    public boolean validate(List<String> warnings) {
        // 该插件只支持MYSQL
        if ("com.mysql.jdbc.Driver".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false
                && "com.mysql.cj.jdbc.Driver".equalsIgnoreCase(this.getContext().getJdbcConnectionConfiguration().getDriverClass()) == false) {
            warnings.add("assembly mybatis:插件" + this.getClass().getTypeName() + "只支持MySQL数据库！");
            return false;
        }

        // 插件是否开启了多sql提交
        Properties properties = this.getProperties();
        String allowMultiQueries = properties.getProperty(PRO_ALLOW_MULTI_QUERIES);
        this.allowMultiQueries = allowMultiQueries == null ? false : StringUtility.isTrue(allowMultiQueries);
        if (this.allowMultiQueries) {
            // 提示用户注意信息
            warnings.add("itfsw:插件" + this.getClass().getTypeName() + "插件您开启了allowMultiQueries支持，注意在jdbc url 配置中增加“allowMultiQueries=true”支持（不怎么建议使用该功能，开启多sql提交会增加sql注入的风险，请确保你所有sql都使用MyBatis书写，请不要使用statement进行sql提交）！");
        }

        return super.validate(warnings);
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addBatchInsertMethod(interfaze);

        addBatchInsertIgnoreMethod(interfaze);

        addBatchInsertSelective(interfaze);

        addBatchInsertIgnoreSelective(interfaze);

        return true;
    }

    private void addBatchInsertIgnoreSelective(Interface interfaze) {
        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType selectiveType = FullyQualifiedJavaType.getNewListInstance();
        Method mBatchInsertSelective = JavaElementGeneratorTools.generateMethod(
                METHOD_BATCH_INSERT_IGNORE_SELECTIVE,
                JavaVisibility.DEFAULT,
                FullyQualifiedJavaType.getIntInstance(),
                new Parameter(listType, "list", "@Param(\"list\")"),
                new Parameter(selectiveType, "selective", "@Param(\"selective\")")
        );
        FormatTools.addMethodWithBestPosition(interfaze, mBatchInsertSelective);
    }

    private void addBatchInsertIgnoreMethod(Interface interfaze) {
        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        Method mUpsert = JavaElementGeneratorTools.generateMethod(
                METHOD_BATCH_INSERT_IGNORE,
                JavaVisibility.DEFAULT,
                FullyQualifiedJavaType.getIntInstance(),
                new Parameter(listType, "list", "@Param(\"list\")")
        );

        // interface 增加方法
        FormatTools.addMethodWithBestPosition(interfaze, mUpsert);
    }

    private void addBatchInsertSelective(Interface interfaze) {
        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType selectiveType = FullyQualifiedJavaType.getNewListInstance();
        Method mBatchInsertSelective = JavaElementGeneratorTools.generateMethod(
                METHOD_BATCH_INSERT_SELECTIVE,
                JavaVisibility.DEFAULT,
                FullyQualifiedJavaType.getIntInstance(),
                new Parameter(listType, "list", "@Param(\"list\")"),
                new Parameter(selectiveType, "selective", "@Param(\"selective\")")
        );
        FormatTools.addMethodWithBestPosition(interfaze, mBatchInsertSelective);
    }

    private void addBatchInsertMethod(Interface interfaze) {
        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        Method mUpsert = JavaElementGeneratorTools.generateMethod(
                METHOD_BATCH_INSERT,
                JavaVisibility.DEFAULT,
                FullyQualifiedJavaType.getIntInstance(),
                new Parameter(listType, "list", "@Param(\"list\")")
        );

        // interface 增加方法
        FormatTools.addMethodWithBestPosition(interfaze, mUpsert);
    }

    private void addBatchInsertSqlMap(Document document, IntrospectedTable introspectedTable) {
        // 1. batchInsert
        XmlElement batchInsertEle = new XmlElement("insert");
        batchInsertEle.addAttribute(new Attribute("id", METHOD_BATCH_INSERT));
        // 参数类型
        batchInsertEle.addAttribute(new Attribute("parameterType", "map"));

        // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
        XmlElementGeneratorTools.useGeneratedKeys(batchInsertEle, introspectedTable);

        batchInsertEle.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        for (Element element : XmlElementGeneratorTools.generateKeys(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), true)) {
            batchInsertEle.addElement(element);
        }

        // 添加foreach节点
        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("separator", ","));

        for (Element element : XmlElementGeneratorTools.generateValues(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), "item.")) {
            foreachElement.addElement(element);
        }

        // values 构建
        batchInsertEle.addElement(new TextElement("values"));
        batchInsertEle.addElement(foreachElement);
        document.getRootElement().addElement(batchInsertEle);
        logger.debug("itfsw(批量插入插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加batchInsert实现方法。");
    }

    private void addBatchInsertIgnoreSqlMap(Document document, IntrospectedTable introspectedTable) {
        // 1. batchInsert
        XmlElement batchInsertEle = new XmlElement("insert");
        batchInsertEle.addAttribute(new Attribute("id", METHOD_BATCH_INSERT_IGNORE));
        // 参数类型
        batchInsertEle.addAttribute(new Attribute("parameterType", "map"));

        // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
        XmlElementGeneratorTools.useGeneratedKeys(batchInsertEle, introspectedTable);

        batchInsertEle.addElement(new TextElement("insert ignore into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        for (Element element : XmlElementGeneratorTools.generateKeys(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), true)) {
            batchInsertEle.addElement(element);
        }

        // 添加foreach节点
        XmlElement foreachElement = new XmlElement("foreach");
        foreachElement.addAttribute(new Attribute("collection", "list"));
        foreachElement.addAttribute(new Attribute("item", "item"));
        foreachElement.addAttribute(new Attribute("separator", ","));

        for (Element element : XmlElementGeneratorTools.generateValues(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), "item.")) {
            foreachElement.addElement(element);
        }

        // values 构建
        batchInsertEle.addElement(new TextElement("values"));
        batchInsertEle.addElement(foreachElement);
        document.getRootElement().addElement(batchInsertEle);
        logger.debug("itfsw(批量插入插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加batchInsert实现方法。");
    }

    private void addBatchInsertSelectiveSqlMap(Document document, IntrospectedTable introspectedTable) {
        XmlElement batchInsertSelectiveEle = new XmlElement("insert");

        batchInsertSelectiveEle.addAttribute(new Attribute("id", METHOD_BATCH_INSERT_SELECTIVE));
        // 参数类型
        batchInsertSelectiveEle.addAttribute(new Attribute("parameterType", "map"));

        // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
        XmlElementGeneratorTools.useGeneratedKeys(batchInsertSelectiveEle, introspectedTable);

        // 支持原生字段非空判断
        if (this.allowMultiQueries) {
            XmlElement chooseEle = new XmlElement("choose");

            // selective 增强
            XmlElement selectiveEnhancedEle = new XmlElement("when");
            selectiveEnhancedEle.addAttribute(new Attribute("test", "selective != null and selective.length > 0"));
            chooseEle.addElement(selectiveEnhancedEle);

            selectiveEnhancedEle.getElements().addAll(this.generateSelectiveEnhancedEles(introspectedTable, false));

            // 原生非空判断语句
            XmlElement selectiveNormalEle = new XmlElement("otherwise");
            chooseEle.addElement(selectiveNormalEle);

            XmlElement foreachEle = new XmlElement("foreach");
            selectiveNormalEle.addElement(foreachEle);
            foreachEle.addAttribute(new Attribute("collection", "list"));
            foreachEle.addAttribute(new Attribute("item", "item"));
            foreachEle.addAttribute(new Attribute("separator", ";"));


            foreachEle.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));

            XmlElement insertTrimElement = new XmlElement("trim");
            foreachEle.addElement(insertTrimElement);
            insertTrimElement.addElement(XmlElementGeneratorTools.generateKeysSelective(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), "item."));

            foreachEle.addElement(new TextElement("values"));

            XmlElement valuesTrimElement = new XmlElement("trim");
            foreachEle.addElement(valuesTrimElement);
            valuesTrimElement.addElement(XmlElementGeneratorTools.generateValuesSelective(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), "item."));

            batchInsertSelectiveEle.addElement(chooseEle);
        } else {
            batchInsertSelectiveEle.getElements().addAll(this.generateSelectiveEnhancedEles(introspectedTable, false));
        }

        document.getRootElement().addElement(batchInsertSelectiveEle);
        logger.debug("itfsw(批量插入插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加batchInsertSelective实现方法。");
    }

    private void addBatchInsertIgnoreSelectiveSqlMap(Document document, IntrospectedTable introspectedTable) {
        XmlElement batchInsertSelectiveEle = new XmlElement("insert");

        batchInsertSelectiveEle.addAttribute(new Attribute("id", METHOD_BATCH_INSERT_IGNORE_SELECTIVE));
        // 参数类型
        batchInsertSelectiveEle.addAttribute(new Attribute("parameterType", "map"));

        // 使用JDBC的getGenereatedKeys方法获取主键并赋值到keyProperty设置的领域模型属性中。所以只支持MYSQL和SQLServer
        XmlElementGeneratorTools.useGeneratedKeys(batchInsertSelectiveEle, introspectedTable);

        // 支持原生字段非空判断
        if (this.allowMultiQueries) {
            XmlElement chooseEle = new XmlElement("choose");

            // selective 增强
            XmlElement selectiveEnhancedEle = new XmlElement("when");
            selectiveEnhancedEle.addAttribute(new Attribute("test", "selective != null and selective.length > 0"));
            chooseEle.addElement(selectiveEnhancedEle);

            selectiveEnhancedEle.getElements().addAll(this.generateSelectiveEnhancedEles(introspectedTable, true));

            // 原生非空判断语句
            XmlElement selectiveNormalEle = new XmlElement("otherwise");
            chooseEle.addElement(selectiveNormalEle);

            XmlElement foreachEle = new XmlElement("foreach");
            selectiveNormalEle.addElement(foreachEle);
            foreachEle.addAttribute(new Attribute("collection", "list"));
            foreachEle.addAttribute(new Attribute("item", "item"));
            foreachEle.addAttribute(new Attribute("separator", ";"));


            foreachEle.addElement(new TextElement("insert ignore into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));

            XmlElement insertTrimElement = new XmlElement("trim");
            foreachEle.addElement(insertTrimElement);
            insertTrimElement.addElement(XmlElementGeneratorTools.generateKeysSelective(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), "item."));

            foreachEle.addElement(new TextElement("values"));

            XmlElement valuesTrimElement = new XmlElement("trim");
            foreachEle.addElement(valuesTrimElement);
            valuesTrimElement.addElement(XmlElementGeneratorTools.generateValuesSelective(ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns()), "item."));

            batchInsertSelectiveEle.addElement(chooseEle);
        } else {
            batchInsertSelectiveEle.getElements().addAll(this.generateSelectiveEnhancedEles(introspectedTable, true));
        }

        document.getRootElement().addElement(batchInsertSelectiveEle);
        logger.debug("itfsw(批量插入插件):" + introspectedTable.getMyBatis3XmlMapperFileName() + "增加batchInsertSelective实现方法。");
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        // 1. batchInsert
        addBatchInsertSqlMap(document, introspectedTable);

        addBatchInsertIgnoreSqlMap(document, introspectedTable);

        // 2. batchInsertSelective
        addBatchInsertSelectiveSqlMap(document, introspectedTable);

        addBatchInsertIgnoreSelectiveSqlMap(document, introspectedTable);

        return true;
    }

    /**
     * 生成insert selective 增强的插入语句
     * @param introspectedTable
     * @return
     */
    private List<Element> generateSelectiveEnhancedEles(IntrospectedTable introspectedTable, boolean hasIgnore) {
        List<Element> eles = new ArrayList<>();

        if (hasIgnore) {
            eles.add(new TextElement("insert ignore into " + introspectedTable.getFullyQualifiedTableNameAtRuntime() + " ("));
        } else {
            eles.add(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime() + " ("));
        }

        XmlElement foreachInsertColumns = new XmlElement("foreach");
        foreachInsertColumns.addAttribute(new Attribute("collection", "selective"));
        foreachInsertColumns.addAttribute(new Attribute("item", "column"));
        foreachInsertColumns.addAttribute(new Attribute("separator", ","));
        foreachInsertColumns.addElement(new TextElement("${column.escapedColumnName}"));

        eles.add(foreachInsertColumns);

        eles.add(new TextElement(")"));

        // values
        eles.add(new TextElement("values"));

        // foreach values
        XmlElement foreachValues = new XmlElement("foreach");
        foreachValues.addAttribute(new Attribute("collection", "list"));
        foreachValues.addAttribute(new Attribute("item", "item"));
        foreachValues.addAttribute(new Attribute("separator", ","));

        foreachValues.addElement(new TextElement("("));

        // foreach 所有插入的列，比较是否存在
        XmlElement foreachInsertColumnsCheck = new XmlElement("foreach");
        foreachInsertColumnsCheck.addAttribute(new Attribute("collection", "selective"));
        foreachInsertColumnsCheck.addAttribute(new Attribute("item", "column"));
        foreachInsertColumnsCheck.addAttribute(new Attribute("separator", ","));

        // 所有表字段
        List<IntrospectedColumn> columns = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
        List<IntrospectedColumn> columns1 = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
        for (int i = 0; i < columns1.size(); i++) {
            IntrospectedColumn introspectedColumn = columns.get(i);
            XmlElement check = new XmlElement("if");
            check.addAttribute(new Attribute("test", "'" + introspectedColumn.getActualColumnName() + "'.toString() == column.value"));
            check.addElement(new TextElement(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, "item.")));

            foreachInsertColumnsCheck.addElement(check);
        }
        foreachValues.addElement(foreachInsertColumnsCheck);

        foreachValues.addElement(new TextElement(")"));

        eles.add(foreachValues);

        return eles;
    }
}
