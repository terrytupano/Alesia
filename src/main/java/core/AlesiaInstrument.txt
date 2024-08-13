package core;

import org.javalite.instrumentation.*;

public class AlesiaInstrument {
     public static void instrument() {
        Instrumentation instrumentation = new Instrumentation();
		instrumentation.setOutputDirectory("bin");
		instrumentation.instrument();
     }
}
