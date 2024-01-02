package org.example.framework;

import org.example.annotations.Autowired;
import org.example.annotations.Component;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Context {
    private Map<String, Class<?>> loadedClasses;
    private Context(Map<String, Class<?>> loadedClasses){
        this.loadedClasses = loadedClasses;
    }
    public static Context load(String packageName){
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
        Map<String, Class<?>> clazzes = reflections.getSubTypesOf(Object.class).stream()
                .filter(clazz -> clazz.isAnnotationPresent(Component.class))
                .collect(Collectors.toMap(clazz ->clazz.getAnnotation(Component.class).value(), clazz -> clazz));
        return new Context(clazzes);
    }

    public Map<String, Class<?>> getLoadedClasses() {
        return loadedClasses;
    }

    public Object get(String className) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        if (!loadedClasses.containsKey(className)){
            throw new RuntimeException("Нет такого объекта");
        }

        Class<?> clazz = loadedClasses.get(className);
        var constructors = clazz.getDeclaredConstructors();
        var annotatedConstructor = Arrays.stream(constructors)
                .filter(con -> con.isAnnotationPresent(Autowired.class))
                .findFirst();

        if (annotatedConstructor.isPresent()){
            return getParameterizedConstructor(annotatedConstructor.get());
        } else {
            return getDefaultConstructor(clazz.getDeclaredConstructor());
        }
    }
    private Object getParameterizedConstructor(Constructor<?> constructor) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        var parameterTypes = constructor.getParameterTypes();
        var params = Arrays.stream(parameterTypes)
                .map(
                        cl -> {
                            try {
                                return get(cl.getAnnotation(Component.class).value());
                            } catch (Exception e) {
                                throw new RuntimeException("Такой тип нельзя подставить как параметр");
                            }
                        }
                ).collect(Collectors.toList());
        return  constructor.newInstance(params.toArray());
    }
    private Object getDefaultConstructor(Constructor<?> constructor) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        var instanse = constructor.newInstance();
        Arrays.stream(instanse.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Autowired.class))
                .forEach(field -> {
                    try {
                    field.setAccessible(true);
                    field.set(instanse, get(field.getType().getAnnotation(Component.class).value()));
                    } catch (Exception e) {
                        throw new RuntimeException("В такое поле нельзя подставить значение");
                    }
                });
        return instanse;
    }
}
