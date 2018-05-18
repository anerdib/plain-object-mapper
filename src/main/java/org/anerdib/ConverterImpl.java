package org.anerdib;

import lombok.extern.java.Log;
import org.anerdib.api.Converter;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @param <S>
 * @param <D>
 * @author anerdib
 */
@Log
public class ConverterImpl<S, D, I> implements Converter<S, D> {


	private final Supplier<I> supplier;
	private final Function<I, D> finalizer;

	private Class<S> sourceClass;
	private Class<I> intermediaryClass;

	public ConverterImpl(Supplier<I> supplier, Function<I, D> finalizer) {
		this.supplier = supplier;
		this.finalizer = finalizer;

		initMapping();
	}

	private void initMapping() {
	}





	/*public Converter(Supplier<S> first, Supplier<D> second) {
		this.firstConstructor = first;
		this.secondConstructor = second;

		firstClass = first.getClass().getMethods()[0].getGenericReturnType();
		secondClass = second.getClass().getMethods()[0].getGenericReturnType();
	}*/
/*
	private ConverterImpl(Converter<D, S, S> dtConverter) {
		this.reverseConverter = dtConverter;
		this.firstClass = new GenericTypeReference<S>() {
		}.getGenericClass();
		this.secondClass = new GenericTypeReference<D>() {
		}.getGenericClass();
	}
*/

	protected I applyMappings(I instance) {
			return instance;
	}
	public D convert(S source) {
		I instance = supplier.get();
		instance = applyMappings(instance);
		return finalizer.apply(instance);
	}
}