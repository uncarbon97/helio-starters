package cc.uncarbon.framework.helium.db.fieldcrypto.typehandler;

import cc.uncarbon.framework.helium.db.fieldcrypto.field.FieldCipher;
import cc.uncarbon.framework.helium.db.fieldcrypto.field.FieldCipherContext;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 加密字段 TypeHandler 基类
 *
 * @author Uncarbon
 */
@MappedTypes(String.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public abstract class EncryptedFieldTypeHandler extends BaseTypeHandler<String> {

    /**
     * @return 本字段对应的 columnPath
     */
    public abstract String columnPath();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, FieldCipherContext.getRequired().encrypt(parameter, columnPath()));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decrypt(rs.getString(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decrypt(rs.getString(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decrypt(cs.getString(columnIndex));
    }

    private String decrypt(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        FieldCipher cipher = FieldCipherContext.getRequired();
        return cipher.decrypt(raw, columnPath());
    }
}
