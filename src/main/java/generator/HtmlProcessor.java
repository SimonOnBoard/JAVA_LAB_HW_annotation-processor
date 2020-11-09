package generator;

import com.google.auto.service.AutoService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("generator.HtmlForm")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class HtmlProcessor extends AbstractProcessor {
    private static final String TYPE_PREFIX = "type_";
    private static final String NAME_PREFIX = "name_";
    private static final String PLACEHOLDER_PREFIX = "placeholder_";

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        String resultPath;
        HtmlInput htmlInputAnnotation;
        HtmlForm htmlFormAnnotation;
        Template template;
        StringWriter result;
        Path out;
        Element currentElement;

        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(HtmlForm.class);
        String directPath = HtmlProcessor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        directPath = File.separator + directPath;
        for (Element element : annotatedElements) {
            Map<String, String> parametersMap = new HashMap<>();
            resultPath = directPath.substring(1) + element.getSimpleName().toString().toLowerCase() + ".html";
            out = Paths.get(resultPath);
            try {
                Configuration configuration = configureFreemarker(directPath);
                result = new StringWriter();
                BufferedWriter writer = new BufferedWriter(new FileWriter(out.toFile()));
                htmlFormAnnotation = element.getAnnotation(HtmlForm.class);
                parametersMap.put("action", htmlFormAnnotation.action());
                parametersMap.put("method", htmlFormAnnotation.method());
                List<? extends Element> elements = element.getEnclosedElements();
                for (int i = 0; i < elements.size(); i++) {
                    currentElement = elements.get(i);
                    if ((htmlInputAnnotation = currentElement.getAnnotation(HtmlInput.class)) != null) {
                        parametersMap.put(TYPE_PREFIX + i, htmlInputAnnotation.type());
                        parametersMap.put(NAME_PREFIX + i, htmlInputAnnotation.name());
                        parametersMap.put(PLACEHOLDER_PREFIX + i, htmlInputAnnotation.placeholder());
                    }
                }
                template = configuration.getTemplate(element.getSimpleName().toString().toLowerCase() + ".ftl");
                template.process(parametersMap, result);
                writer.write(result.toString());
                writer.close();
            } catch (IOException | TemplateException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return true;
    }

    private Configuration configureFreemarker(String path) throws IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
        cfg.setDirectoryForTemplateLoading(new File(path));
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        return cfg;
    }
}