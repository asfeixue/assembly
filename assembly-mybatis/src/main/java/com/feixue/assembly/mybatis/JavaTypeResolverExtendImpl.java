package com.feixue.assembly.mybatis;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;
import org.mybatis.generator.internal.util.StringUtility;

import java.sql.Types;
import java.util.Properties;

public class JavaTypeResolverExtendImpl extends JavaTypeResolverDefaultImpl {

    protected boolean tinyint2Boolean;

    private static final String TYPE_RESOLVER_TINYINT_BOOLEAN = "tinyint2Boolean";

    public JavaTypeResolverExtendImpl() {
        super();
    }

    @Override
    public void addConfigurationProperties(Properties properties) {
        tinyint2Boolean = StringUtility
                .isTrue(properties
                        .getProperty(TYPE_RESOLVER_TINYINT_BOOLEAN));

        if (tinyint2Boolean) {
            typeMap.put(Types.TINYINT, new JdbcTypeInformation("TINYINT", //$NON-NLS-1$
                    new FullyQualifiedJavaType(Boolean.class.getName())));
        }

        super.addConfigurationProperties(properties);
    }
}
