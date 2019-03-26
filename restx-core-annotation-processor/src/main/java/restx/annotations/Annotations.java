package restx.annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

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
                    	
                        List<String> fqcns = new ArrayList<>();
                        entry.getValue().accept(new AnnotationValueVisitor<Void, List<String>>() {

                            @Override
                            public Void visit(AnnotationValue av, List<String> p) {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            @Override
                            public Void visit(AnnotationValue av) {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            @Override
                            public Void visitBoolean(boolean b, List<String> p) {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            @Override
                            public Void visitByte(byte b, List<String> p) {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            @Override
                            public Void visitChar(char c, List<String> p) {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            @Override
                            public Void visitDouble(double d, List<String> p) {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            @Override
                            public Void visitFloat(float f, List<String> p) {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            @Override
                            public Void visitInt(int i, List<String> p) {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            @Override
                            public Void visitLong(long i, List<String> p) {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            @Override
                            public Void visitShort(short s, List<String> p) {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            @Override
                            public Void visitString(String s, List<String> p) {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            @Override
                            public Void visitType(TypeMirror t, List<String> p) {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            @Override
                            public Void visitEnumConstant(VariableElement c, List<String> p) {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            @Override
                            public Void visitAnnotation(AnnotationMirror a, List<String> p) {
                                // TODO Auto-generated method stub
                                return null;
                            }

                            @Override
                            public Void visitArray(List<? extends AnnotationValue> vals, List<String> p) {
                                for (AnnotationValue v : vals) {
                                    DeclaredType type = (DeclaredType) v.getValue();
                                    p.add(type.toString());

                                }
                                return null;
                            }

                            @Override
                            public Void visitUnknown(AnnotationValue av, List<String> p) {
                                // TODO Auto-generated method stub
                                return null;
                            }

                        }, fqcns);
//                        Attribute.Array array = (Attribute.Array) entry.getValue();
//
//                        for(Attribute attribute : array.getValue()) {
//                            
//                        }
                        return fqcns.toArray(new String[fqcns.size()]);
                    }
                }
            }
        }
        return null;
    }
}
