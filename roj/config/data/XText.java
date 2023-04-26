package roj.config.data;

import roj.config.XMLParser;
import roj.config.serial.CVisitor;
import roj.text.CharList;

/**
 * @author Roj234
 * @since 2020/11/22 15:53
 */
public final class XText extends XEntry {
	public static final byte NEVER_ENCODE = 2, ALWAYS_ENCODE = 1, CHECK_ENCODE = 0;

	public String value;
	public byte CDATA_flag;

	public XText(String text) { value = text; }

	@Override
	public boolean isString() { return true; }
	@Override
	public String asString() { return value; }

	@Override
	public void toJSON(CVisitor cc) { cc.value(value); }

	protected void toXML(CharList sb, int depth) { toCompatXML(sb); }
	protected void toCompatXML(CharList sb) {
		if (CDATA_flag == ALWAYS_ENCODE || (CDATA_flag == CHECK_ENCODE && !XMLParser.literalSafe(value))) sb.append("<![CDATA[").append(value).append("]]>");
		else sb.append(value);
	}
}
