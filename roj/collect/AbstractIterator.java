package roj.collect;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Roj234
 * @since 2020/8/12 13:04
 */
public abstract class AbstractIterator<T> implements Iterator<T> {
	protected T result;
	protected int stage = INITIAL;

	public static final int INITIAL = 0;
	public static final int GOTTEN = 1;
	public static final int CHECKED = 2;
	public static final int ENDED = 3;

	@Override
	public final boolean hasNext() {
		check();
		return stage != ENDED;
	}

	protected abstract boolean computeNext();

	@Override
	public final T next() {
		check();
		if (stage == ENDED) {
			throw new NoSuchElementException();
		}
		stage = GOTTEN;
		return result;
	}

	private void check() {
		if (stage <= 1) {
			if (!computeNext()) {
				stage = ENDED;
			} else {
				stage = CHECKED;
			}
		}
	}

	@Override
	public final void remove() {
		if (stage != GOTTEN) throw new IllegalStateException();
		remove(result);
		stage = INITIAL;
	}

	protected void remove(T obj) {
		throw new UnsupportedOperationException();
	}

	public void reset() {
		throw new UnsupportedOperationException();
	}
}
