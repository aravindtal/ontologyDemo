package com.talentica.cube.DomainActions;

import com.talentica.cube.Blaze.Query;
import org.openrdf.query.IncompatibleOperationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by aravindp on 22/2/16.
 */
public class ReflectionUtil {
    public static Object getInstanceOfDomainAction(String domainAction,Query query) throws Exception{
        Class<?> actionClass = Class.forName("com.talentica.cube.DomainActions."+domainAction);
        Constructor<?> constructor = actionClass.getConstructor(Query.class);

        Object instance = constructor.newInstance(query);

        return instance;
    }

    public static Object getReturnValueforFunction(Query query,String className,String methodName,Object arguments[]) throws Exception{
        Object instance = getInstanceOfDomainAction(className,query);
        Class<?> actionClass = instance.getClass();

        Object returnValue = null;
        try {
            Method methods[] = actionClass.getMethods();
            for (Method m: methods) {
                if(m.getName().equalsIgnoreCase(methodName)){
                    returnValue = m.invoke(instance,arguments);
                }
            }
        } catch (InvocationTargetException e) {
            throw new Exception(e.getCause().getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return returnValue;
    }

}
