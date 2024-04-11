package ch.qos.logback.core;

import ch.qos.logback.core.joran.spi.ConsoleTarget;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.WarnStatus;
import ch.qos.logback.core.util.Loader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ConsoleAppender<E> extends OutputStreamAppender<E> {
    protected ConsoleTarget target;
    protected boolean withJansi;
    private static final String AnsiConsole_CLASS_NAME = "org.fusesource.jansi.AnsiConsole";
    private static final String JANSI2_OUT_METHOD_NAME = "out";
    private static final String JANSI2_ERR_METHOD_NAME = "err";
    private static final String wrapSystemOut_METHOD_NAME = "wrapSystemOut";
    private static final String wrapSystemErr_METHOD_NAME = "wrapSystemErr";
    private static final Class<?>[] ARGUMENT_TYPES = new Class[]{PrintStream.class};

    public ConsoleAppender() {
        this.target = ConsoleTarget.SystemOut;
        this.withJansi = false;
    }

    public void setTarget(String value) {
        ConsoleTarget t = ConsoleTarget.findByName(value.trim());
        if (t == null) {
            this.targetWarn(value);
        } else {
            this.target = t;
        }

    }

    public String getTarget() {
        return this.target.getName();
    }

    private void targetWarn(String val) {
        Status status = new WarnStatus("[" + val + "] should be one of " + Arrays.toString(ConsoleTarget.values()), this);
        status.add(new WarnStatus("Using previously set target, System.out by default.", this));
        this.addStatus(status);
    }

    public void start() {
        OutputStream targetStream = this.target.getStream();
        if (this.withJansi) {
            targetStream = this.wrapWithJansi(targetStream);
        }

        this.setOutputStream(targetStream);
        super.start();
    }

    private OutputStream wrapWithJansi(OutputStream targetStream) {
        try {
            this.addInfo("Enabling JANSI AnsiPrintStream for the console.");
            ClassLoader classLoader = Loader.getClassLoaderOfObject(this.context);
            Class<?> classObj = classLoader.loadClass("org.fusesource.jansi.AnsiConsole");
            String methodNameJansi2 = this.target == ConsoleTarget.SystemOut ? "out" : "err";
            Optional<Method> optOutMethod = Arrays.stream(classObj.getMethods()).filter((m) -> {
                return m.getName().equals(methodNameJansi2);
            }).filter((m) -> {
                return m.getParameters().length == 0;
            }).filter((m) -> {
                return Modifier.isStatic(m.getModifiers());
            }).filter((m) -> {
                return PrintStream.class.isAssignableFrom(m.getReturnType());
            }).findAny();
            if (optOutMethod.isPresent()) {
                Method outMethod = (Method)optOutMethod.orElseThrow(() -> {
                    return new NoSuchElementException("No value present");
                });
                return (PrintStream)outMethod.invoke((Object)null);
            } else {
                String methodName = this.target == ConsoleTarget.SystemOut ? "wrapSystemOut" : "wrapSystemErr";
                Method method = classObj.getMethod(methodName, ARGUMENT_TYPES);
                return (OutputStream)method.invoke((Object)null, new PrintStream(targetStream));
            }
        } catch (Exception var8) {
            this.addWarn("Failed to create AnsiPrintStream. Falling back on the default stream.", var8);
            return targetStream;
        }
    }

    public boolean isWithJansi() {
        return this.withJansi;
    }

    public void setWithJansi(boolean withJansi) {
        this.withJansi = withJansi;
    }
}
