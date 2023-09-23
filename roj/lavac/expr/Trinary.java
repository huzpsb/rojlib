package roj.lavac.expr;

import roj.asm.type.IType;
import roj.lavac.parser.MethodWriterL;

import javax.annotation.Nonnull;

/**
 * @author Roj234
 * @since 2023/9/18 0018 9:06
 */
public class Trinary implements Expression {
	private Expression val, ok, fail;

	public Trinary(Expression val, Expression ok, Expression fail) {
		this.val = val;
		this.ok = ok;
		this.fail = fail;
	}

	@Override
	public IType type() { return ok.type() == fail.type() ? ok.type() : null; } // todo common super class or Object

	@Override
	public void write(MethodWriterL cw, boolean noRet) {

	}

	@Nonnull
	@Override
	public Expression resolve() {
		ok = ok.resolve();
		fail = fail.resolve();
		if ((val = val.resolve()).isConstant()) {
			// todo checkcast
			return (boolean) val.constVal() ? ok : fail;
		}
		return this;
	}

	@Override
	public String toString() { return val.toString()+" ? "+ok+" : "+fail; }

	@Override
	public boolean equals(Object left) {
		if (this == left) return true;
		if (!(left instanceof Trinary)) return false;
		Trinary tripleIf = (Trinary) left;
		return tripleIf.val.equals(val) && tripleIf.ok.equals(ok) && tripleIf.fail.equals(fail);
	}

	@Override
	public int hashCode() {
		int result = val.hashCode();
		result = 31 * result + ok.hashCode();
		result = 31 * result + fail.hashCode();
		return result;
	}
}