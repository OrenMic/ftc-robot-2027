package miscar.annotation;

import com.ctre.phoenix.motorcontrol.can.SlotConfiguration;
import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Constants.Mode;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Scanner;
import java.util.Set;
import javax.lang.model.element.Modifier;
import miscar.annotation.ConstructorAnnotationChecker.ConstructorAnnotationVisitor;
import miscar.configs.Config;
import miscar.configs.motors.Phoenix5Config;
import miscar.configs.motors.TalonFXConfig;
import miscar.mecsIOs.features.ModeOnDisable;
import miscar.util.DCMotorUtil;
import miscar.util.PIDControllerUtil;
import miscar.util.PhoenixControlTypes;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * An annotation processor which automatically creates constants files
 */
public class ConstantsCreator {

  /**
   * A map between any possible field type and it's default initializer
   */
  static HashMap<Class<?>, String> typeToInitializerMap = new HashMap<>();

  // map the possible field types and their default initializes
  static {
    typeToInitializerMap.put(int.class, "1");
    typeToInitializerMap.put(double.class, "1");
    typeToInitializerMap.put(boolean.class, "false");
    typeToInitializerMap.put(String.class, "\"\"");
    typeToInitializerMap.put(Transform3d.class, "new Transform3d()");
    typeToInitializerMap.put(Pose3d.class, "new Pose3d()");
    typeToInitializerMap.put(TalonFXConfiguration.class, "new TalonFXConfiguration()\r\n" + //
        "          .withSlot0(new com.ctre.phoenix6.configs.Slot0Configs().withKP(7).withKI(0).withKD(0))\r\n"
        + //
        "          .withMotorOutput(\r\n" + //
        "              new com.ctre.phoenix6.configs.MotorOutputConfigs()\r\n" + //
        "                  .withNeutralMode(com.ctre.phoenix6.signals.NeutralModeValue.Brake))");
    typeToInitializerMap.put(SparkMaxConfig.class, "new SparkMaxConfig()");
    typeToInitializerMap.put(PIDControllerUtil.class, "new PIDControllerUtil(8,0,0)");
    typeToInitializerMap.put(DCMotorUtil.class, "DCMotorUtil.from(DCMotorUtil.getKrakenX60(1))");
    typeToInitializerMap.put(PhoenixControlTypes.PoseControl.class,
        "PhoenixControlTypes.PoseControl.PositionVoltage");
    typeToInitializerMap.put(PhoenixControlTypes.VelocityControl.class,
        "PhoenixControlTypes.VelocityControl.VelocityVoltage");
    typeToInitializerMap.put(PhoenixControlTypes.OpenLoopControl.class,
        "PhoenixControlTypes.OpenLoopControl.Regular");
    typeToInitializerMap.put(SlotConfiguration.class, "new SlotConfiguration()");
    typeToInitializerMap.put(ModeOnDisable.NeutralMode.class, "ModeOnDisable.NeutralMode.COAST");
    typeToInitializerMap.put(Phoenix5Config.Type.class, "Phoenix5Config.Type.TALON_SRX");
    typeToInitializerMap.put(CANcoderConfiguration.class, "new CANcoderConfiguration()");
  }

  /**
   * A list of all the field names we already created, so that we don't
   * create the same field twice
   */
  static ArrayList<String> createdFieldNames = new ArrayList<>();

  private record ConstantsParentClassesName(String FieldName, String className) {}

  /**
   * A list of all the parent constants classes we created so that we
   * can instate them
   */
  static ArrayList<ConstantsParentClassesName> createdConstantsParentClassesNames =
      new ArrayList<>();

  /**
   * A list of all the {@code port} field names, this is used so that we
   * can create a separate constants file for all the {@code ports}
   */
  private record PortPair(String PortFieldName, String javaDoc) {}

  static Set<PortPair> portFieldNames = new HashSet<>();

  /**
   * @param objects - All the {@code objects} we want to auto create a
   *        constants file for
   * @apiNote - This will create a constants file, one for each
   *          {@code object}, as long as you don't have the
   *          {@link DoNotOverride} annotation on the constants class
   */
  public static void createConstantsFiles(Object... objects) {
    if (Constants.currentMode != Mode.SIM)
      return;

    for (Object object : objects) {
      processObjectRecursive(object.getClass());
    }
    processPorts();
  }

  private static void processObjectRecursive(Class<?> classz) {
    processObject(classz);
    System.out.println(classz.getSimpleName());
    // recursive fields
    for (Field field : classz.getDeclaredFields()) {
      Class<?> fieldType = field.getType();
      if (fieldType.isArray()) {
        fieldType = fieldType.getComponentType();
      }
      System.out.println(field.getName());
      Class<?> superclass = fieldType.getSuperclass();
      boolean isSubsystem =
          (superclass != null) && SubsystemBase.class.isAssignableFrom(superclass);
      var annotatedField = fieldType.getAnnotation(ConstantsUser.class);
      var singletonAnnotation = fieldType.getAnnotation(Singleton.class);
      if (isSubsystem || annotatedField != null) {
        if (singletonAnnotation != null) {
          processObject(fieldType);
        } else {
          processObjectRecursive(fieldType);
        }
      }
    }

    // recursive class
    Class<?> superClass = classz.getSuperclass();

    if (superClass != null && superClass != SubsystemBase.class) {
      processObjectRecursive(superClass);
    }
  }

  private static void processPorts() {
    Path path = Path.of("./src/main/java/frc/robot/generated/ports/Ports.java");

    Optional<ClassOrInterfaceDeclaration> myClass;
    try {
      myClass = StaticJavaParser.parse(path).getClassByName("Ports");
      if (myClass.isPresent()) {
        editPortFile();
      } else {
        createPortFile();
      }
    } catch (IOException e) {
      createPortFile();
    }
  }

  private static void createPortFile() {
    TypeSpec.Builder configFileBuilder =
        TypeSpec.classBuilder("Ports").addModifiers(Modifier.PUBLIC);

    for (PortPair Pair : portFieldNames) {
      FieldSpec.Builder fieldSpec =
          FieldSpec.builder(int.class, Pair.PortFieldName).addModifiers(Modifier.PUBLIC)
              .addModifiers(Modifier.STATIC).addModifiers(Modifier.FINAL).addJavadoc(Pair.javaDoc);
      String initializer = typeToInitializerMap.get(int.class);
      if (initializer != null) {
        fieldSpec.initializer(initializer);
      }
      configFileBuilder.addField(fieldSpec.build());
    }

    JavaFile javaFile =
        JavaFile.builder("frc.robot.generated.ports", configFileBuilder.build()).build();

    try {
      javaFile.writeTo(Paths.get("./src/main/java"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void editPortFile() {
    // Load the file
    Path path = Path.of("./src/main/java/frc/robot/generated/ports/Ports.java");
    CompilationUnit compilationUni;
    try {
      compilationUni = StaticJavaParser.parse(path);

      // Find the class
      ClassOrInterfaceDeclaration myClass = compilationUni.getClassByName("Ports")
          .orElseThrow(() -> new RuntimeException("Class not found"));

      for (PortPair pair : notInFile(path.toFile(), portFieldNames)) {

        // Create the field
        VariableDeclarator var = new VariableDeclarator(StaticJavaParser.parseType("int"),
            pair.PortFieldName, new IntegerLiteralExpr(typeToInitializerMap.get(int.class)));

        FieldDeclaration field =
            new FieldDeclaration().addVariable(var).setJavadocComment(pair.javaDoc).setModifiers(
                com.github.javaparser.ast.Modifier.Keyword.PUBLIC,
                com.github.javaparser.ast.Modifier.Keyword.STATIC,
                com.github.javaparser.ast.Modifier.Keyword.FINAL);

        // Add the field to the class
        myClass.addMember(field);
      }

      // Save the updated file
      Files.writeString(path, compilationUni.toString(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static boolean shouldCreateConstantsFile(String className, String classFile) {

    File targetFile = new File(classFile);

    return !hasAnnotation(className, targetFile, DoNotOverride.class);
  }

  private static boolean hasAnnotation(String className, File file,
      Class<? extends Annotation> annotation) {
    try (Scanner scanner = new Scanner(file)) {
      scanner.useDelimiter("\\Z");
      String wantedText = "@" + annotation.getSimpleName() + " public class " + className;
      String noCommentsText = scanner.next().replaceAll("(?m)^\\s*//.*", "");
      String normalizedContent = noCommentsText.replaceAll("\\s+", " ").trim();
      return normalizedContent.contains(wantedText);
    } catch (FileNotFoundException e) {
      return false;
    }
  }

  /**
   * @param file - The file to search for the text in
   * @param suspiciousTexts - The suspicious text to search for in the
   *        file
   * @return An ArrayList of all the {@code suspiciousTexts} that does
   *         appear in the file
   */
  private static ArrayList<PortPair> notInFile(File file, Set<PortPair> suspiciousTexts) {
    ArrayList<PortPair> unsuspiciousTexts = new ArrayList<>();
    try (Scanner scanner = new Scanner(file)) {
      scanner.useDelimiter("\\Z");
      String noCommentsText = scanner.next().replaceAll("(?m)^\\s*//.*", "");
      String normalizedContent = noCommentsText.replaceAll("\\s+", " ").trim();
      for (PortPair suspiciousText : suspiciousTexts) {

        if (!normalizedContent.matches("(?s).*\\b" + suspiciousText.PortFieldName + "\\b.*")) {
          unsuspiciousTexts.add(suspiciousText);
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return unsuspiciousTexts;
  }

  private static void createConstantsParentFile(Field[] fields, String constantsClassName) {
    for (Field field : fields) {
      var annotatedField = field.getAnnotation(CreateConstants.class);
      if (annotatedField == null) {
        continue;
      }

      String configFileName = "";
      configFileName += capitalizeFirstLetter(field.getName());
      if (!configFileName.toLowerCase().contains("constants")
          && !configFileName.toLowerCase().contains("config")) {
        configFileName += "Constants";
      }
      createdConstantsParentClassesNames
          .add(new ConstantsParentClassesName(field.getName(), configFileName));
      TypeSpec.Builder configParentFile = TypeSpec.classBuilder(configFileName)
          .addModifiers(Modifier.PUBLIC).addAnnotation(DoNotOverride.class);
      ArrayList<String> createdFields = new ArrayList<>();
      for (Class<? extends Config> configClass : annotatedField.configType()) {
        var annotatedConfigClass = configClass.getAnnotation(ConstantsName.class);
        if (annotatedConfigClass == null) {
          throw new RuntimeException("Constants file does not have a name");
        }
        String className = capitalizeFirstLetter(annotatedConfigClass.constantsName());
        if (!className.toLowerCase().contains("config")
            && !className.toLowerCase().contains("constants")) {
          className += "Constants";
        }

        String constantsClassObjectName = "";
        constantsClassObjectName += className;

        String newObjectName = "";
        newObjectName += constantsClassObjectName;

        if (createdFields.contains(newObjectName)) {
          continue;
        }
        createdFields.add(newObjectName);

        String packageName =
            "frc.robot.generated." + uncapitalizeFirstLetter(constantsClassName) + ".";
        packageName += field.getName();

        configParentFile.addField(FieldSpec
            .builder(ClassName.get(packageName, constantsClassObjectName),
                uncapitalizeFirstLetter(newObjectName))
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .initializer("new " + constantsClassObjectName + "()").build());
      }

      createConstantsContractor(configParentFile);

      String packageName = "frc.robot.generated.";
      packageName += uncapitalizeFirstLetter(constantsClassName) + ".";
      packageName += uncapitalizeFirstLetter(field.getName());
      String filePath =
          "./src/main/java/" + packageName.replace(".", "/") + "/" + configFileName + ".java";
      createdFieldNames.clear();
      if (shouldCreateConstantsFile(configFileName, filePath)) {

        JavaFile javaFile = JavaFile.builder(packageName, configParentFile.build()).build();

        try {
          javaFile.writeTo(Paths.get("./src/main/java"));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static void createConstantsMasterFile(String constantsClassName) {
    String configFileName = constantsClassName + "Constants";
    if (createdConstantsParentClassesNames.isEmpty()) {
      return;
    }

    // configFileName += "Constants";
    // createdConstantsParentClassesNames.add(configFileName);
    TypeSpec.Builder configMasterFile = TypeSpec.classBuilder(configFileName)
        .addModifiers(Modifier.PUBLIC).addAnnotation(DoNotOverride.class);
    for (ConstantsParentClassesName parent : createdConstantsParentClassesNames) {

      String packageName = "frc.robot.generated." + uncapitalizeFirstLetter(constantsClassName);
      ClassName parentClass = ClassName
          .get(packageName + "." + uncapitalizeFirstLetter(parent.FieldName), parent.className);

      configMasterFile
          .addField(FieldSpec.builder(parentClass, uncapitalizeFirstLetter(parent.className))
              .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
              .initializer("new $T()", parentClass).build());
    }

    // createConstantsContractor(configMasterFile);

    String packageName = "frc.robot.generated.";
    packageName += uncapitalizeFirstLetter(constantsClassName);
    String filePath =
        "./src/main/java/" + packageName.replace(".", "/") + "/" + configFileName + ".java";
    createdFieldNames.clear();
    if (shouldCreateConstantsFile(configFileName, filePath)) {

      JavaFile javaFile = JavaFile.builder(packageName, configMasterFile.build()).build();

      try {
        javaFile.writeTo(Paths.get("./src/main/java"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    createdConstantsParentClassesNames.clear();
  }

  private static void createConstantsFile(Field[] fields, String constantsClassName) {
    for (Field field : fields) {

      var annotatedField = field.getAnnotation(CreateConstants.class);
      if (annotatedField == null) {
        continue;
      }
      for (Class<? extends Config> configClass : annotatedField.configType()) {
        var annotatedConfigClass = configClass.getAnnotation(ConstantsName.class);
        if (annotatedConfigClass == null) {
          throw new RuntimeException("Constants file does not have a name");
        }
        String className = capitalizeFirstLetter(annotatedConfigClass.constantsName());
        if (!className.toLowerCase().contains("config")
            && !className.toLowerCase().contains("constants")) {
          className += "Constants";
        }

        TypeSpec.Builder configFileBuilder = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC).addAnnotation(DoNotOverride.class);

        createFieldsRecursive(configFileBuilder, field, configClass, constantsClassName);
        createConstructor(configFileBuilder,
            field,
            configClass,
            annotatedConfigClass.constantsName());
        createConstantsContractor(configFileBuilder);

        String packageName =
            "frc.robot.generated." + uncapitalizeFirstLetter(constantsClassName) + ".";
        packageName += uncapitalizeFirstLetter(field.getName());
        String filePath =
            "./src/main/java/" + packageName.replace(".", "/") + "/" + className + ".java";
        createdFieldNames.clear();

        if (shouldCreateConstantsFile(className, filePath)) {

          JavaFile javaFile = JavaFile.builder(packageName, configFileBuilder.build()).build();

          try {
            javaFile.writeTo(Paths.get("./src/main/java"));
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }

  private static void processObject(Class<?> classz) {
    createConstantsFile(classz.getDeclaredFields(), classz.getSimpleName());
    createConstantsParentFile(classz.getDeclaredFields(), classz.getSimpleName());
    createConstantsMasterFile(classz.getSimpleName());
  }

  private static void createConstantsContractor(TypeSpec.Builder configFileBuilder) {
    configFileBuilder
        .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build());
  }

  private static void createFields(TypeSpec.Builder configFileBuilder, Field field,
      Class<? extends Config> configClass, String objectName) {
    boolean isPrivate = getAnnotatedConstructorParameterNames(configClass.getName()).isPresent();
    for (Field configField : configClass.getDeclaredFields()) {
      // don't add protected fields to auto created file
      if (configField.getModifiers() == java.lang.reflect.Modifier.PROTECTED
          || configField.isSynthetic()) {
        continue;
      }
      String configFieldName = configField.getName();
      String newVariableName = field.getName();
      String javaDocString = "The " + objectName + "'s " + camelToWords(field.getName()) + " "
          + camelToWords(configField.getName());
      Class<?> newVariableType = configField.getType();
      if (newVariableType == OptionalDouble.class) {
        newVariableType = double.class;
      }
      if (newVariableType == OptionalInt.class) {
        newVariableType = int.class;
      }
      newVariableName +=
          configFieldName.substring(0, 1).toUpperCase() + configFieldName.substring(1);
      if (createdFieldNames.contains(newVariableName)) {
        continue;
      }
      createdFieldNames.add(newVariableName);
      if (newVariableName.toLowerCase().contains("port")) {
        portFieldNames.add(new PortPair(newVariableName, javaDocString));
        continue;
      }
      FieldSpec.Builder fieldSpec = FieldSpec.builder(newVariableType, newVariableName)
          .addModifiers(isPrivate ? Modifier.PRIVATE : Modifier.PUBLIC).addModifiers(Modifier.FINAL)
          .addJavadoc(javaDocString);
      String initializer = typeToInitializerMap.get(newVariableType);
      if (initializer != null) {
        fieldSpec.initializer(initializer);
      }
      configFileBuilder.addField(fieldSpec.build());
    }
  }

  @SuppressWarnings("unchecked")
  private static void createFieldsRecursive(TypeSpec.Builder configFileBuilder, Field field,
      Class<? extends Config> configClass, String objectName) {
    createFields(configFileBuilder, field, configClass, objectName);
    Class<?> superclass = configClass.getSuperclass();
    if (superclass != null && !superclass.equals(Object.class)) {
      if (Config.class.isAssignableFrom(superclass)) {
        createFieldsRecursive(configFileBuilder,
            field,
            (Class<? extends Config>) superclass,
            objectName);
      }
    }
  }

  private static boolean hasConstantsContractor(Class<?> configClass) {
    String configClassName = configClass.getName();
    return getAnnotatedConstructorParameterNames(configClassName).isPresent();
  }

  private static void createConstructor(TypeSpec.Builder configFileBuilder, Field field,
      Class<?> configClass, String configName) {
    String configClassName = configClass.getName();
    Optional<ArrayList<String>> paramNamesOpt =
        getAnnotatedConstructorParameterNames(configClassName);
    if (paramNamesOpt.isEmpty()) {
      return;
    }
    ArrayList<String> parameterNames = paramNamesOpt.get();
    String configClassSimpleName = configClass.getSimpleName();
    String javaDocString = "The " + camelToWords(field.getName()) + "'s "
        + camelToWords(configClassSimpleName) + " constructor";
    String configFieldName = configName;
    String newConstructorName = "config";
    if (!configFieldName.toLowerCase().contains("config")
        && !configFieldName.toLowerCase().contains("constants")) {
      configFieldName += "Constants";
    }
    if (createdFieldNames.contains(newConstructorName)) {
      return;
    }
    createdFieldNames.add(newConstructorName);
    String constructorString = "new " + configClassSimpleName + "(";
    for (String parameterName : parameterNames) {
      String newConstructorParameterName = "";
      if (parameterName.toLowerCase().contains("port")) {
        newConstructorParameterName += "frc.robot.generated.ports.Ports.";
      }
      newConstructorParameterName += field.getName();
      newConstructorParameterName +=
          parameterName.substring(0, 1).toUpperCase() + parameterName.substring(1);
      constructorString += newConstructorParameterName + ",";
    }
    if (!parameterNames.isEmpty()) {
      constructorString = constructorString.substring(0, constructorString.length() - 1);
    }
    constructorString += ")";
    FieldSpec fieldSpec = FieldSpec.builder(configClass, newConstructorName)
        .initializer(constructorString).addModifiers(Modifier.PUBLIC).addModifiers(Modifier.FINAL)
        .addJavadoc(javaDocString).build();
    configFileBuilder.addField(fieldSpec);
  }

  private static Optional<ArrayList<String>> getAnnotatedConstructorParameterNames(
      String configClassName) {
    try {
      InputStream classStream = TalonFXConfig.class
          .getResourceAsStream("/" + configClassName.replace('.', '/') + ".class");
      if (classStream == null)
        return Optional.empty();
      ClassReader reader = new ClassReader(classStream);
      final ArrayList<ConstructorAnnotationVisitor> foundVisitors = new ArrayList<>();
      reader.accept(new ClassVisitor(Opcodes.ASM9) {
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
            String signature, String[] exceptions) {
          if ("<init>".equals(name)) {
            ConstructorAnnotationVisitor visitor = new ConstructorAnnotationVisitor(
                super.visitMethod(access, name, descriptor, signature, exceptions),
                ConstantsConstructor.class.getSimpleName());
            foundVisitors.add(visitor);
            return visitor;
          }
          return super.visitMethod(access, name, descriptor, signature, exceptions);
        }
      }, 0);
      for (ConstructorAnnotationVisitor foundVisitor : foundVisitors) {
        var varNames = foundVisitor.getVariableNames();
        if (!varNames.isEmpty() || (foundVisitor != null
            && foundVisitor.getVariableNames().isEmpty() && foundVisitor.isAnnotated)) {
          return Optional.of(varNames);
        }
      }
    } catch (IOException e) {
    }
    return Optional.empty();
  }

  private static String camelToWords(String input) {
    // Step 1: Add space between:
    // - lower → upper (e.g., "poseControl")
    // - acronym → word (e.g., "JSONData" → "JSON Data")
    String step1 =
        input.replaceAll("(?<=[a-z])(?=[A-Z])", " ").replaceAll("(?<=[A-Z])(?=[A-Z][a-z])", " ");

    // Step 2: Split and lowercase non-acronyms
    String[] words = step1.split(" ");
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < words.length; i++) {
      String word = words[i];
      // Keep acronyms (e.g., "JSON") as-is; lowercase others
      if (word.equals(word.toUpperCase()) && word.length() > 1) {
        result.append(word);
      } else {
        result.append(word.toLowerCase());
      }
      if (i < words.length - 1)
        result.append(" ");
    }

    return result.toString();
  }

  private static String capitalizeFirstLetter(String string) {
    return string.substring(0, 1).toUpperCase() + string.substring(1);
  }

  private static String uncapitalizeFirstLetter(String string) {
    return string.substring(0, 1).toLowerCase() + string.substring(1);
  }

  private static String addDots(String input) {
    return input.replaceAll("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])", ".");
  }

  private static ClassName fromFullClassName(String fullClassName) {
    if (fullClassName == null || fullClassName.trim().isEmpty()) {
      throw new IllegalArgumentException("Class name must not be empty");
    }

    String[] parts = fullClassName.split("\\.");
    if (parts.length == 1) {
      // No package, only class name
      return ClassName.get("", parts[0]);
    } else {
      // Separate package and class name
      String className = parts[parts.length - 1];
      String packageName = String.join(".", java.util.Arrays.copyOf(parts, parts.length - 1));
      return ClassName.get(packageName, className);
    }
  }
}
