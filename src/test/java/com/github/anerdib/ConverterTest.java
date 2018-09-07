package com.github.anerdib;

import java.util.ArrayList;
import java.util.List;

import com.github.anerdib.api.Converter;
import com.github.anerdib.model.SimpleTargetBuilder;
import com.github.anerdib.model.SimpleSourcePojo;
import com.github.anerdib.model.SimpleTargetPojo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConverterTest {
	private SimpleSourcePojo simpleSourcePojo1;
	private SimpleSourcePojo simpleSourcePojo2;
	private SimpleTargetPojo simpleTargetPojo1;
	private SimpleTargetPojo simpleTargetPojo2;
	private Converter<SimpleSourcePojo, SimpleTargetPojo> basicConverter;

	@BeforeEach
	public void init() {
		this.simpleSourcePojo1 = new SimpleSourcePojo();
		this.simpleSourcePojo1.setPrimitiveField(1);
		this.simpleSourcePojo1.setStringField("test1");
		this.simpleSourcePojo1.setReferenceField(new Object());

		this.simpleSourcePojo2 = new SimpleSourcePojo();
		this.simpleSourcePojo2.setPrimitiveField(2);
		this.simpleSourcePojo2.setStringField("test2");
		this.simpleSourcePojo2.setReferenceField(new Object());

		this.simpleTargetPojo1 = new SimpleTargetPojo();
		simpleTargetPojo1.setPrimitiveField(simpleSourcePojo1.getPrimitiveField());
		simpleTargetPojo1.setStringField(simpleSourcePojo1.getStringField());
		simpleTargetPojo1.setReferenceField(simpleSourcePojo1.getReferenceField());

		this.simpleTargetPojo2 = new SimpleTargetPojo();
		simpleTargetPojo2.setPrimitiveField(simpleSourcePojo2.getPrimitiveField());
		simpleTargetPojo2.setStringField(simpleSourcePojo2.getStringField());
		simpleTargetPojo2.setReferenceField(simpleSourcePojo2.getReferenceField());

		this.basicConverter =  ConverterBuilderFactory.of(SimpleSourcePojo.class, SimpleTargetPojo.class)
				                      .withDefaultConstructor().build();
	}

	@Test
	public void test_pojo_1_way() {
		Assertions.assertEquals(basicConverter.convert(simpleSourcePojo1), simpleTargetPojo1);
	}

	@Test
	public void test_pojo_with_methods() {
		Converter<SimpleSourcePojo, SimpleTargetPojo> converter =  ConverterBuilderFactory.of(SimpleSourcePojo.class,
				SimpleTargetPojo.class).withDefaultConstructor().from(SimpleSourcePojo::getStringField)
				                                                          .to(SimpleTargetPojo::setStringField).build();
		Assertions.assertEquals(converter.convert(simpleSourcePojo1), simpleTargetPojo1);
	}

	@Test
	public void test_omit() {
		Converter<SimpleSourcePojo, SimpleTargetPojo> converter = ConverterBuilderFactory.of(SimpleSourcePojo.class,
				SimpleTargetPojo.class).withDefaultConstructor().from(SimpleSourcePojo::getStringField)
				                                                          .to(SimpleTargetPojo::setStringField).omit(SimpleSourcePojo::getReferenceField).build();
		SimpleTargetPojo converted = converter.convert(simpleSourcePojo1);
		Assertions.assertEquals(converted.getStringField(), simpleTargetPojo1.getStringField());
		Assertions.assertNull(converted.getReferenceField());
	}

	@Test
	public void test_pojo_with_methods_modifying() {
		Converter<SimpleSourcePojo, SimpleTargetPojo> converter =  ConverterBuilderFactory.of(SimpleSourcePojo.class,
				SimpleTargetPojo.class).withDefaultConstructor().from(SimpleSourcePojo::getStringField)
				                                                          .to((i, v) -> i.setStringField("ok")).build();
		Assertions.assertEquals(converter.convert(simpleSourcePojo1).getStringField(), "ok");
	}

	@Test
	public void test_collection_list() {
		List<SimpleSourcePojo> list = new ArrayList<>();
		list.add(simpleSourcePojo1);
		list.add(simpleSourcePojo2);

		List<SimpleTargetPojo> target = new ArrayList<>();
		target.add(simpleTargetPojo1);
		target.add(simpleTargetPojo2);

		List<SimpleTargetPojo> converted = basicConverter.convert(list);
		Assertions.assertIterableEquals(target, converted);

	}

	@Test
	public void test_array() {
		SimpleSourcePojo[] provided = new SimpleSourcePojo[]{simpleSourcePojo1, simpleSourcePojo2};
		SimpleTargetPojo[] expected = new SimpleTargetPojo[]{simpleTargetPojo1, simpleTargetPojo2};

		SimpleTargetPojo[] converted = basicConverter.convert(provided);
		Assertions.assertArrayEquals(expected, converted);
	}

	@Test
	public void test_pojo_factory_method() {
		Converter<SimpleSourcePojo, SimpleTargetPojo> converter =  ConverterBuilderFactory.of(SimpleSourcePojo.class,
				SimpleTargetPojo.class).withFactory(SimpleTargetPojo::new).build();
		Assertions.assertEquals(converter.convert(simpleSourcePojo1), simpleTargetPojo1);
	}


	@Test
	public void test_pojo_Builder() {
		Converter<SimpleSourcePojo, SimpleTargetPojo> converter = ConverterBuilderFactory.of(SimpleSourcePojo.class,
				SimpleTargetPojo.class).withBuilder(SimpleTargetBuilder::new).from(SimpleSourcePojo::getReferenceField)
				                                                          .to(SimpleTargetBuilder::setReference).build();
		Assertions.assertEquals(converter.convert(simpleSourcePojo1), simpleTargetPojo1);
	}
}
