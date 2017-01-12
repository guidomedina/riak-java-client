/*
 * This file is provided to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.basho.riak.client.convert.reflect;

import com.basho.riak.client.cap.VClock;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import com.basho.riak.client.convert.RiakIndex;
import com.basho.riak.client.convert.RiakKey;
import com.basho.riak.client.convert.RiakLinks;
import com.basho.riak.client.convert.RiakTombstone;
import com.basho.riak.client.convert.RiakUsermeta;
import com.basho.riak.client.convert.RiakVClock;
import com.basho.riak.client.convert.UsermetaField;

/**
 * A {@link Callable} that loops over a classes fields and pulls out the fields
 * for {@link RiakUsermeta} and {@link RiakKey}
 *
 * @author russell
 *
 */
public class AnnotationScanner implements Callable<AnnotationInfo> {

    @SuppressWarnings("rawtypes") private final Class classToScan;

    @SuppressWarnings("rawtypes") public AnnotationScanner(Class clazz) {
        this.classToScan = clazz;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Callable#call()
     */
    public AnnotationInfo call() throws Exception {
        Field riakKeyField = null;
        Method riakKeyGetterMethod = null;
        Method riakKeySetterMethod = null;
        Field riakVClockField = null;
        Field riakTombstoneField = null;
        Field usermetaMapField = null;
        Field linksField = null;
        List<UsermetaField> usermetaItemFields = new ArrayList<>();
        List<RiakIndexField> indexFields = new ArrayList<>();
        List<RiakIndexMethod> indexMethods = new ArrayList<>();

        Class currentClass = classToScan;
        while(currentClass != Object.class) {

            final Field[] fields = currentClass.getDeclaredFields();

            for (Field field : fields) {

                if (riakKeyField == null && field.isAnnotationPresent(RiakKey.class)) {

                    riakKeyField = ClassUtil.checkAndFixAccess(field);
                }

                if (riakVClockField == null && field.isAnnotationPresent(RiakVClock.class)) {

                    // restrict the field type to byte[] or VClock
                    if (!(field.getType().isArray() &&
                            field.getType().getComponentType().equals(byte.class)) &&
                            !field.getType().isAssignableFrom(VClock.class)
                            ) {
                        throw new IllegalArgumentException(field.getType().toString());
                    }

                    riakVClockField = ClassUtil.checkAndFixAccess(field);
                }

                if (riakTombstoneField == null && field.isAnnotationPresent(RiakTombstone.class)) {

                    // restrict the field to boolean
                    if (!field.getType().equals(Boolean.TYPE)) {
                        throw new IllegalArgumentException(field.getType().toString());
                    }
                    riakTombstoneField = ClassUtil.checkAndFixAccess(field);
                }

                if (field.isAnnotationPresent(RiakUsermeta.class)) {
                    RiakUsermeta a = field.getAnnotation(RiakUsermeta.class);
                    String key = a.key();

                    if (!"".equals(key)) {
                        usermetaItemFields.add(new UsermetaField(ClassUtil.checkAndFixAccess(field)));
                    } else if (usermetaMapField == null) {
                        usermetaMapField = ClassUtil.checkAndFixAccess(field);
                    }

                }

                if(field.isAnnotationPresent(RiakIndex.class)) {
                    indexFields.add(new RiakIndexField(ClassUtil.checkAndFixAccess(field)));
                }

                if (linksField == null && field.isAnnotationPresent(RiakLinks.class)) {
                    linksField = ClassUtil.checkAndFixAccess(field);
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        final Method[] methods = classToScan.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(RiakIndex.class)) {
                if (method == null || method.getAnnotation(RiakIndex.class) == null
                        || "".equals(method.getAnnotation(RiakIndex.class).name())
                        || (!method.getReturnType().equals(String.class)
                        && !method.getReturnType().equals(Integer.class)
                        && !method.getReturnType().equals(int.class))
                        && !method.getReturnType().equals(Long.class)
                        && !method.getReturnType().equals(long.class)
                        && !Set.class.isAssignableFrom(method.getReturnType())) {
                    continue;
                }
                if (Set.class.isAssignableFrom(method.getReturnType())) {
                    // Verify it's a Set<String> or Set<Integer>
                    final Type t = method.getGenericReturnType();
                    if (t instanceof ParameterizedType) {
                        final Class<?> genericType = (Class<?>) ((ParameterizedType) t).getActualTypeArguments()[0];
                        if (!genericType.equals(String.class) && !genericType.equals(Integer.class) && !genericType.equals(Long.class)) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
                indexMethods.add(new RiakIndexMethod(ClassUtil.checkAndFixAccess(method)));
            }
            else if (method.isAnnotationPresent(RiakKey.class))
            {
                if (method.getReturnType().equals(String.class)) {
                    riakKeyGetterMethod = ClassUtil.checkAndFixAccess(method);
                } else if (method.getReturnType().equals(Void.TYPE) &&
                            (method.getParameterTypes().length == 1 &&
                             method.getParameterTypes()[0].equals(String.class))) {
                    riakKeySetterMethod = ClassUtil.checkAndFixAccess(method);
                }
            }
        }

        return new AnnotationInfo(riakKeyField, riakKeyGetterMethod, riakKeySetterMethod,
                                  usermetaItemFields, usermetaMapField,
                                  indexFields, indexMethods, linksField, riakVClockField,
                                  riakTombstoneField);
    }
}
