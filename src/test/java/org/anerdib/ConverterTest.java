package org.anerdib;

import org.anerdib.api.Converter;
import org.anerdib.model.SimpleSourcePojo;
import org.anerdib.model.SimpleTargetPojo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConverterTest {
	private SimpleSourcePojo simpleSourcePojo;
	private SimpleTargetPojo simpleTargetPojo;


	@BeforeEach
	public void init() {
		this.simpleSourcePojo = new SimpleSourcePojo();
		this.simpleSourcePojo.setPrimitiveField(4);
		this.simpleSourcePojo.setStringField("test");
		this.simpleSourcePojo.setReferenceField(new Object());
		this.simpleTargetPojo = new SimpleTargetPojo();
		simpleTargetPojo.setPrimitiveField(simpleSourcePojo.getPrimitiveField());
		simpleTargetPojo.setStringField(simpleSourcePojo.getStringField());
		simpleTargetPojo.setReferenceField(simpleSourcePojo.getReferenceField());
	}

	@Test
	public void test_pojo_1_way() {
		Converter<SimpleSourcePojo, SimpleTargetPojo> converter = new ConverterBuilder<>(SimpleSourcePojo.class, SimpleTargetPojo.class)
				                                                          .withDefaultConstructors().build();
		Assertions.assertEquals(converter.convert(simpleSourcePojo), simpleTargetPojo);
	}

	@Test
	public void test_pojo_factory_method() {
		Converter<SimpleSourcePojo, SimpleTargetPojo> converter = new ConverterBuilder<>(SimpleSourcePojo.class, SimpleTargetPojo.class)
				                                                          .withFactory(SimpleTargetPojo::new).build();
		Assertions.assertEquals(converter.convert(simpleSourcePojo), simpleTargetPojo);
	}

}
