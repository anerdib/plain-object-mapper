package com.github.anerdib;

import com.github.anerdib.api.Converter;
import com.github.anerdib.model.Getter;
import com.github.anerdib.model.Setter;
import lombok.extern.java.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

@Log
public class ConverterImpl<S, D, I> implements Converter<S, D> {

	private final Supplier<I> supplier;
	private final Function<I, D> finalizer;
	private final Class<S> sourceClass;
	private final Class<D> destinationClass;
	private final Class<I> intermediaryClass;

	private Collection<Map.Entry<Getter<S, Object>, Setter<I, Object>>> setters;

	@SuppressWarnings("unchecked")
	public ConverterImpl(Class<S> sourceClass, Class<D> destinationClass, Class<I> intermediaryClass, Supplier<I> supplier, Function<I, D> finalizer) {
		this.supplier = supplier;
		this.finalizer = finalizer;
		this.sourceClass = sourceClass;
		this.destinationClass = destinationClass;
		this.intermediaryClass = intermediaryClass;
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

	public void setSetters(Collection<Map.Entry<Getter<S, Object>, Setter<I, Object>>> values) {
		this.setters = values;
	}

}

