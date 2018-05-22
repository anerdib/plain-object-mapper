package org.anerdib;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

import org.anerdib.api.Converter;
import org.anerdib.model.GenericTypeReference;
import org.anerdib.model.Getter;
import org.anerdib.model.Setter;

import lombok.extern.java.Log;

/**
 * @param <S>
 * @param <D>
 * @author anerdib
 */
@Log
public class ConverterImpl<S, D, I> implements Converter<S, D> {

	private final Supplier<I> supplier;
	private final Function<I, D> finalizer;
	private final Class<S> sourceClass;
	private final Class<D> destinationClass;
	private final Class<I> intermediaryClass;

	private Collection<Entry<Getter<S, Object>, Setter<I, Object>>> setters;

	@SuppressWarnings("unchecked")
	public ConverterImpl(Supplier<I> supplier, Function<I, D> finalizer) {
		this.supplier = supplier;
		this.finalizer = finalizer;
		sourceClass = (Class<S>) GenericTypeReference.getGenericTypes(supplier)[0];
		intermediaryClass = (Class<I>) GenericTypeReference.getGenericTypes(finalizer)[0];
		destinationClass = (Class<D>) GenericTypeReference.getGenericTypes(finalizer)[1];
	}

	protected I applyMappings(S source, I instance) {
		setters.stream().forEach(e -> {
			Object value = e.getKey().apply(source);
			e.getValue().apply(instance, value);
		});
		return instance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Collection<D>> C convert(Collection<S> source) {
		C newCol = null;
		if (source == null)
			return null;
		try {
			newCol = (C) source.getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			log.log(Level.SEVERE, "Error when creating target collection", e);
			newCol = (C) new ArrayList<D>();
		}

		for (S el : source) {
			newCol.add(convert(el));
		}
		return newCol;
	}

	public D convert(S source) {
		I instance = supplier.get();
		instance = applyMappings(source, instance);
		return finalizer.apply(instance);
	}

	@SuppressWarnings("unchecked")
	@Override
	public D[] convert(S[] source) {
		if (source == null)
			return null;
		D[] newArray = (D[]) Array.newInstance(destinationClass, source.length);
		for (int i = 0; i < source.length; i++) {
			newArray[i] = convert(source[i]);
		}
		return newArray;
	}

	public void setSetters(Collection<Entry<Getter<S, Object>, Setter<I, Object>>> values) {
		this.setters = values;
	}

}