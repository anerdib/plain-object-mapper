package com.github.anerdib;

public class Test {

	public static class TestInner {
		public String get() {
			return "Fifi fufu";
		}
	}

	public static void main(String[] args) {


		/*	WorkQueueProcessor<String> processor = WorkQueueProcessor.create("test",128);
			FluxSink<String> sink = processor.sink();
			processor.map(s -> ":::::::::::" + s).subscribe( s -> System.out.println("----> "+ s));

			Stream<Integer> stream = Stream.generate(() -> {
				int nr = new  Random().nextInt();
					
				return nr;
			});
			
			stream.forEach(a -> sink.next(""+a));*/


	}
}
