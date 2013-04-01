package orego.util;

/* Pair is a simple immutable product type T1 * T2 */
public final class Pair<T1, T2> {
	public final T1 fst;
	public final T2 snd;
	
	public static <A, B> Pair<A, B> fromValues(A fst, B snd) {
		return new Pair<A, B>(fst, snd);
	}
	
	public Pair(T1 fst, T2 snd) {
		this.fst = fst;
		this.snd = snd;
	}
			
	public String toString() {
		return new StringBuilder().append("<").append(this.fst.toString()).append(", ").append(this.snd.toString()).append(">").toString();
	}
}
