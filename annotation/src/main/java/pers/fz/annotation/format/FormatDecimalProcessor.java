package pers.fz.annotation.format;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * created by fz on 2024/9/29 8:46
 * describe:java中AbstractProcessor自定义注解处理器演示demo，自动生成代码
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.casic.titan.annotation.format.FormatDecimal")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class FormatDecimalProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(FormatDecimal.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(FormatDecimal.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                error(element, "Only fields can be annotated with @%s", FormatDecimal.class.getSimpleName());
                return true;
            }
            FormatDecimal formatDecimal = element.getAnnotation(FormatDecimal.class);
            int decimal = formatDecimal.value();
            String className = element.getEnclosingElement().getSimpleName().toString();
            String name = element.getSimpleName().toString();
            String type = element.asType().toString();
            String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            generateCode(packageName, className, name, type, decimal);
        }
        return true;
    }

    private void generateCode(String packageName, String className, String name, String type, int decimal) {
        try {
            JavaFileObject file = filer.createSourceFile(packageName + ".FormatDecimal_" + className);
            Writer writer = file.openWriter();
            writer.write("package " + packageName + ";\n\n");
            writer.write("public class FormatDecimal_" + className + " {\n\n");
            writer.write("  public void format(" + packageName + "." + className + " activity) {\n");
            writer.write("activity." + name + " = new java.math.BigDecimal(" + "activity." + name + ").setScale(" + decimal + ", java.math.RoundingMode.HALF_UP).toString();\n");
            writer.write("  }\n\n");
            writer.write("}\n");
            writer.close();
        } catch (IOException e) {
            error(null, e.getMessage());
        }
    }

    private void error(Element element, String message, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(message, args), element);
    }
}

