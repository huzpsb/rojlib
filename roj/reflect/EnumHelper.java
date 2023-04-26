package roj.reflect;

import roj.asm.Opcodes;
import roj.asm.Parser;
import roj.asm.cst.CstClass;
import roj.asm.cst.CstInt;
import roj.asm.cst.CstRef;
import roj.asm.tree.ConstantData;
import roj.asm.tree.MethodNode;
import roj.asm.tree.attr.AttrCode;
import roj.asm.tree.attr.Attribute;
import roj.asm.tree.insn.*;
import roj.asm.type.Type;
import roj.asm.type.TypeHelper;
import roj.asm.util.AccessFlag;
import roj.asm.util.InsnHelper;
import roj.asm.visitor.CodeVisitor;
import roj.collect.SimpleList;
import roj.collect.ToLongMap;
import roj.concurrent.OperationDone;

import java.util.List;
import java.util.Map;

import static roj.asm.Opcodes.*;

/**
 * @see <a href="https://github.com/SpongePowered/Mixin/issues/387">Add support for adding enum constants</a>
 * @author Roj234
 * @since 2023/4/19 2:06
 */
public final class EnumHelper extends CodeVisitor {
	public static final CDirAcc cDirAcc;
	public interface CDirAcc { Map<String, Enum<?>> enumConstantDirectory(Class<? extends Enum<?>> clazz); }
	static { cDirAcc = DirectAccessor.builder(CDirAcc.class).unchecked().delegate(Class.class, "enumConstantDirectory", "enumConstantDirectory").build(); }
	// 以上都没用

	private final ToLongMap<String> parPos = new ToLongMap<>();

	private final ConstantData ref;
	private AttrCode staticInit;

	private CstInt len;
	private final InsnList toAdd = new InsnList();
	private int addPos, lvid;

	public EnumHelper(ConstantData klass) {
		if ((klass.access & AccessFlag.ENUM) == 0) throw new IllegalStateException("Not enum class: " + klass.name());

		ref = klass;
		klass.normalize();

		SimpleList<MethodNode> methods = klass.methods;
		for (int i = 0; i < methods.size(); i++) {
			MethodNode mn = methods.get(i);
			if (mn.name().equals("<init>")) {
				try {
					visit(klass.cp, Parser.reader(mn.attrByName("Code")));
				} catch (OperationDone found) {
					SimpleList<Type> param = new SimpleList<>(mn.parameters());
					param.remove(Math.max(nameId, ordinalId));
					param.remove(Math.min(nameId, ordinalId));
					param.add(Type.std(Type.VOID));

					long v = 0;
					v |= (long)i << 24;
					v |= nameId << 16;
					v |= ordinalId << 8;
					v |= param.size()+1;
					parPos.put(TypeHelper.getMethod(param), v);
				}
			} else if (mn.name().equals("<clinit>")) {
				staticInit = mn.parsedAttr(klass.cp, Attribute.Code);

				InsnList list = staticInit.instructions;
				int j = 0;

				loop:
				for (; j < list.size(); j++) {
					InsnNode node = list.get(j);
					lvid = Math.max(InsnHelper.getVarId(node), lvid);
					if (node.getOpcode() == ANEWARRAY) {
						ClassInsnNode cin = ((ClassInsnNode) node);
						if (cin.owner.equals(klass.name)) {
							node = list.get(j-1);
							switch (node.getOpcode()) {
								case ICONST_0: case ICONST_1: case ICONST_2: case ICONST_3: case ICONST_4: case ICONST_5:
									len = new CstInt(node.getOpcode()-3);
								break;
								case BIPUSH: case SIPUSH:
									len = new CstInt(((IIndexInsnNode) node).getIndex());
									break;
								case LDC:
								case LDC_W:
									len = ((CstInt) ((LdcInsnNode) node).c);
									break loop;
								default: throw new IllegalArgumentException("what is " + node);
							}
							list.set(j-1, new LdcInsnNode(len));

							addPos = j+1;
							break;
						}
					}
				}
			}
		}
	}

	private int state, nameId, ordinalId;

	@Override
	protected void one(byte code) {
		decompressVar(code);
	}

	// protected Enum(String name, int ordinal)
	@Override
	@SuppressWarnings("fallthrough")
	protected void var(byte code, int id) {
		switch (state) {
			case 3:
				state = 0;
			case 0:
				if (code == Opcodes.ALOAD && id == 0) {
					state = 1;
				}
				break;
			case 1:
				if (code == Opcodes.ALOAD) {
					nameId = id-1;
					state = 2;
				} else {
					state = 0;
				}
				break;
			case 2:
				if (code == Opcodes.ILOAD) {
					ordinalId = id-1;
					state = 3;
				} else {
					state = 0;
				}
				break;
		}
	}

	@Override
	protected void invoke(byte code, CstRef method) {
		if (method.className().equals("java/lang/Enum") && method.desc().name().str().equals("<init>")) {
			assert state == 3;
			throw OperationDone.INSTANCE;
		}
	}

	/**
	 * @return 新枚举的ordinal
	 */
	public int add(String name, Class<?>[] types, Object... param) {
		return add(name, TypeHelper.class2asm(types, void.class), param);
	}
	public int add(String name, String desc, Object... param) {
		long mi = parPos.getOrDefault(desc, 0);
		if (mi == 0) throw new IllegalStateException("no such constructor: " + desc);

		int nid = (int)(mi>>>16)&0xFF, oid = (int)(mi>>>8)&0xFF, len = (int)mi&0xFF;

		InsnList l = toAdd;
		l.one(DUP);
		l.ldc(this.len.value);
		l.clazz(NEW, ref.name);
		l.one(DUP);

		int stackSize = 7 + TypeHelper.paramSize(desc);
		if (staticInit.stackSize < stackSize) {
			staticInit.stackSize = (char) stackSize;
		}

		List<Type> types = TypeHelper.parseMethod(desc);
		int j = 0;
		for (int i = 0; i < len; i++) {
			if (i == nid) {
				l.ldc(name);
				continue;
			} else if (i == oid) {
				l.ldc(this.len.value);
				continue;
			} else if (param[j] == null) {
				l.one(ACONST_NULL);
			} else {
				Type klass = types.get(j);
				Object v = param[j];
				if (klass.isPrimitive()) {
					switch (klass.type) {
						case Type.BOOLEAN: l.ldc((boolean)v ? 1 : 0); break;
						case Type.CHAR: l.ldc((char)v); break;
						default:
						case Type.INT: l.ldc(((Number)v).intValue()); break;
						case Type.LONG: l.ldc(((Number)v).longValue()); break;
						case Type.FLOAT: l.ldc(((Number)v).floatValue()); break;
						case Type.DOUBLE: l.ldc(((Number)v).doubleValue()); break;
					}
				} else {
					switch (klass.owner()) {
						case "java/lang/String":
						case "java/lang/CharSequence": l.ldc(v.toString()); break;
						case "java/lang/Class": l.ldc(new CstClass(v.toString())); break;
						default: l.field(GETSTATIC, (String) v);
					}
				}
			}

			j++;
		}
		l.invoke(INVOKESPECIAL, ref, (int)(mi>>>24));
		l.one(AASTORE);

		return this.len.value++;
	}

	public void commit() {
		if (toAdd.isEmpty()) return;
		staticInit.instructions.addAll(addPos, toAdd);
		addPos += toAdd.size();
		toAdd.clear();
	}
}
