package roj.asm.tree.attr;

import roj.asm.cp.ConstantPool;
import roj.util.DynByteBuf;

import java.util.List;

/**
 * @author Roj234
 * @since 2022/10/22 0022 15:48
 */
public class AttrLavaSpec extends Attribute {
	protected AttrLavaSpec(DynByteBuf buf, ConstantPool pool) {}

	// method
	List<String> defaultValue;

	@Override
	public String name() {
		return "LavaSpec";
	}

	@Override
	public String toString() {
		return null;
	}

	public int switchable() {
		return -1;
	}

	public int[] operatorOverride() {
		return null;
	}

	public int[] enumConstructor() {
		return null;
	}
}