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
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes("generator.HtmlForm")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class HtmlProcessor extends AbstractProcessor {

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
        String resultPath;
        HtmlForm htmlFormAnnotation;
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(HtmlForm.class);
        String directPath = File.separator + HtmlProcessor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        for (Element element : annotatedElements) {
            Map<String, Object> parametersMap = new HashMap<>();
            resultPath = directPath.substring(1) + element.getSimpleName().toString().toLowerCase() + ".html";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(resultPath).toFile()))) {
                configureFreemarker(directPath, cfg);
                htmlFormAnnotation = element.getAnnotation(HtmlForm.class);
                parametersMap.put("action", htmlFormAnnotation.action());
                parametersMap.put("method", htmlFormAnnotation.method());
                List<? extends Element> elements = element.getEnclosedElements();
                parametersMap.put("inputs", elements.stream().filter(elem -> elem.getAnnotation(HtmlInput.class) != null)
                        .map(elem -> elem.getAnnotation(HtmlInput.class)).collect(Collectors.toList()));
                cfg.getTemplate(element.getSimpleName().toString().toLowerCase() + ".ftl").process(parametersMap, writer);
            } catch (IOException | TemplateException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return true;
    }

    private void configureFreemarker(String path, Configuration cfg) throws IOException {
        cfg.setDirectoryForTemplateLoading(new File(path));
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    }
}