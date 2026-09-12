package miscar.annotation;

import java.util.ArrayList;
import org.objectweb.asm.*;

public class ConstructorAnnotationChecker {

  static class ConstructorAnnotationVisitor extends MethodVisitor {
    private final String annotationName;
    boolean isAnnotated = false;
    private final ArrayList<String> variableNames = new ArrayList<>();

    public ConstructorAnnotationVisitor(MethodVisitor mv, String annotationName) {
      super(Opcodes.ASM9, mv);
      this.annotationName = annotationName;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
      String fullName = descriptor.substring(1, descriptor.length() - 1).replace('/', '.');
      String simpleName = fullName.substring(fullName.lastIndexOf('.') + 1);
      if (annotationName.equals(simpleName)) {
        isAnnotated = true;
      }
      return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start,
        Label end, int index) {
      if (index > 0 && isAnnotated) {
        variableNames.add(name);
      }
      super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    public ArrayList<String> getVariableNames() {
      return variableNames;
    }
  }
}
