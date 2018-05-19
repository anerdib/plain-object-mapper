package org.anerdib;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.anerdib.api.Converter;
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

	private Collection<Entry<Getter<S, Object>, Setter<I, Object>>> setters;

	public ConverterImpl(Supplier<I> supplier, Function<I, D> finalizer) {
		this.supplier = supplier;
		this.finalizer = finalizer;

		initMapping();
	}

	private void initMapping() {
	}

	protected I applyMappings(S source, I instance) {
		setters.stream().forEach(e -> {
			Object value = e.getKey().apply(source);
			e.getValue().apply(instance, value);
		});
		return instance;
	}

	public D convert(S source) {
		I instance = supplier.get();
		instance = applyMappings(source, instance);
		return finalizer.apply(instance);
	}

	public void setSetters(Collection<Entry<Getter<S, Object>, Setter<I, Object>>> values) {
		this.setters = values;
	}
}