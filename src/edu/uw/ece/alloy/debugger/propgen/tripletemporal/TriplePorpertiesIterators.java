package edu.uw.ece.alloy.debugger.propgen.tripletemporal;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;

import org.reflections.Reflections;

public class TriplePorpertiesIterators {

	public final static String packageName = "edu.uw.ece.alloy.debugger.propgen.tripletemporal";

	public final TripleBuilder tripleBuilder;

	public TriplePorpertiesIterators(final TripleBuilder tripleBuilder) {
		this.tripleBuilder = tripleBuilder;
	}

	public class SizeIterator extends PropertyIterator<SzPrpty, SizeIterator> {

		final Lclty local;
		final Emptnes empty;

		public SizeIterator(final TripleBuilder builder, final Lclty local,
				final Emptnes empty) {
			super(builder);
			this.local = local;
			this.empty = empty;
		}

		@Override
		protected SzPrpty convertNext(Class t) {
			try {
				return builder.createSizeInstance(t, local, empty);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot be created");
			}
		}

		@Override
		protected SizeIterator makeSelf() {
			return new SizeIterator(builder, local, empty);
		}

	}

	public class LocalityIterator
			extends PropertyIterator<Lclty, LocalityIterator> {

		final Sd side;

		public LocalityIterator(final TripleBuilder builder, final Sd side) {
			super(builder);
			this.side = side;
		}

		@Override
		protected Lclty convertNext(Class t) {
			try {
				return builder.createLocalityInstance(t, side);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot be created");
			}
		}

		@Override
		protected LocalityIterator makeSelf() {
			return new LocalityIterator(builder, side);
		}

	}

	public class SideIterator extends PropertyIterator<Sd, SideIterator> {

		public SideIterator(final TripleBuilder builder) {
			super(builder);
		}

		@Override
		protected Sd convertNext(Class t) {
			try {
				return builder.createSideInstance(t);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot be created");
			}
		}

		@Override
		protected SideIterator makeSelf() {
			return new SideIterator(builder);
		}

	}

	public class EmptinessIterator
			extends PropertyIterator<Emptnes, EmptinessIterator> {

		public EmptinessIterator(final TripleBuilder builder) {
			super(builder);
		}

		@Override
		protected Emptnes convertNext(Class t) {
			try {
				return builder.createEmptinessInstance(t);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot be created");
			}
		}

		@Override
		protected EmptinessIterator makeSelf() {
			return new EmptinessIterator(builder);
		}

	}

	public class OrderIterator extends PropertyIterator<Ord, OrderIterator> {

		final SzPrpty size;

		public OrderIterator(final TripleBuilder builder, final SzPrpty size) {
			super(builder);
			this.size = size;
		}

		@Override
		protected Ord convertNext(Class t) {
			try {
				return builder.createOrderInstance(t, size);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot be created");
			}
		}

		@Override
		protected OrderIterator makeSelf() {
			return new OrderIterator(builder, size);
		}

	}

	public class CompositeOrdersIterator
			extends PropertyIterator<CmpstOrds, CompositeOrdersIterator> {

		final Ord order1, order2;

		public CompositeOrdersIterator(final TripleBuilder builder,
				final Ord order1, final Ord order2) {
			super(builder);
			this.order1 = order1;
			this.order2 = order2;
		}

		@Override
		protected CmpstOrds convertNext(Class t) {
			try {
				return builder.createCompositeOrdersInstance(t, order1, order2);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot be created");
			}
		}

		@Override
		protected CompositeOrdersIterator makeSelf() {
			return new CompositeOrdersIterator(builder, order1, order2);
		}

	}

	public class CompositeSizesIterator
			extends PropertyIterator<CmpstSz, CompositeSizesIterator> {

		final SzPrpty size1, size2;

		public CompositeSizesIterator(final TripleBuilder builder,
				final SzPrpty size1, final SzPrpty size2) {
			super(builder);
			this.size1 = size1;
			this.size2 = size2;
		}

		@Override
		protected CmpstSz convertNext(Class t) {
			try {
				return builder.createCompositeSizesInstance(t, size1, size2);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot be created");
			}
		}

		@Override
		protected CompositeSizesIterator makeSelf() {
			return new CompositeSizesIterator(builder, size1, size2);
		}

	}

	public abstract class PropertyIterator<T, S extends Iterable<T>>
			implements Iterator<T>, Iterable<T> {

		final TripleBuilder builder;

		final Iterator iterator;

		public PropertyIterator(final TripleBuilder builder) {
			this.builder = builder;
			Type sooper = getClass().getGenericSuperclass();
			Type t = ((ParameterizedType) sooper).getActualTypeArguments()[0];

			try {
				iterator = (new Reflections(packageName)
						.getSubTypesOf(Class.forName(t.getTypeName()))).stream()
								.filter(a -> !Modifier.isAbstract(a.getModifiers()))
								.sorted((Class c1, Class c2) -> c1.getName()
										.compareTo(c2.getName()))
								.iterator();
				// iterator = (new Reflections(packageName).getSubTypesOf(Class.forName(
				// t.getTypeName() ))).iterator();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException("Class not found");
			}
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@SuppressWarnings("unchecked")
		@Override
		public T next() {
			return convertNext((Class<T>) iterator.next());
		}

		protected abstract T convertNext(Class<? extends T> t);

		@Override
		public Iterator<T> iterator() {
			return (Iterator<T>) makeSelf();
		}

		protected abstract S makeSelf();

	}

	/*
	 * public Set<Class<? extends Side>> getAllSizeSide(){ return new
	 * Reflections(packageName).getSubTypesOf(Side.class); }
	 * 
	 * public Set<Class<? extends Order>> getAllOrder(){ return new
	 * Reflections(packageName).getSubTypesOf(Order.class); }
	 * 
	 * public Set<Class<? extends Emptiness>> getAllEmpty(){ return new
	 * Reflections(packageName).getSubTypesOf(Emptiness.class); }
	 * 
	 * public Set<Class<? extends Locality>> getAllLocality(){ return new
	 * Reflections(packageName).getSubTypesOf(Locality.class); }
	 */

}
