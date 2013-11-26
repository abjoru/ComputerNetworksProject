package edu.fit.cs.computernetworks.utils;

/**
 * Represents a generic tuple of data.
 * 
 * @author Andreas Bjoru
 *
 * @param <T1> first type parameter
 * @param <T2> second type parameter
 */
public class Tuple<T1, T2> {
	public final T1 _1;
	public final T2 _2;
	
	/**
	 * Factory method for creating a new tuple of the provided types.
	 * 
	 * @param t1
	 * @param t2
	 * @return
	 */
	public static <T1, T2> Tuple<T1, T2> of(final T1 t1, final T2 t2) {
		return new Tuple<T1, T2>(t1, t2);
	}
	
	/*
	 * Hidden constructor since all access should be through the static
	 * factory method. This preserves type safety and clean code in that
	 * we do not have to specify type arguments on the right side of the
	 * assignment operator.
	 */
	private Tuple(final T1 t1, final T2 t2) {
		this._1 = t1;
		this._2 = t2;
	}
}
