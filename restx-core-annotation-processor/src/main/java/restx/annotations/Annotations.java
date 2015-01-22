package restx.annotations;

import com.sun.tools.javac.code.Attribute;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author fcamblor
 */
public class Annotations {
    public static String[] getAnnotationClassValuesAsFQCN(VariableElement p, Class annotationClazz, String methodName) {
        List<? extends AnnotationMirror> annotationMirrors = p.getAnnotationMirrors();
        for(AnnotationMirror annotationMirror : annotationMirrors){
            if(annotationMirror.getAnnotationType().toString().equals(annotationClazz.getCanonicalName())){
                for(Map.Entry<? extends ExecutableElement,? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()){
                    if(entry.getKey().getSimpleName().contentEquals(methodName)){
                        Attribute.Array array = (Attribute.Array) entry.getValue();

                        List<String> fqcns = new ArrayList<>();
                        for(Attribute attribute : array.getValue()) {
                            DeclaredType type = (DeclaredType) attribute.getValue();
                            fqcns.add(type.toString());
                        }
                        return fqcns.toArray(new String[fqcns.size()]);
                    }
                }
            }
        }
        return null;
    }
}
