/*
 * Copyright (C) 2012 René Jeschke <rene_jeschke@yahoo.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rjeschke.neetutils.json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.rjeschke.neetutils.Classes;
import com.github.rjeschke.neetutils.Objects;
import com.github.rjeschke.neetutils.collections.Colls;
import com.github.rjeschke.neetutils.json.JSONTokenizer.Token;
import com.github.rjeschke.neetutils.json.annotations.JSONCatchAllField;
import com.github.rjeschke.neetutils.json.annotations.JSONForceField;
import com.github.rjeschke.neetutils.json.annotations.JSONGenericType;
import com.github.rjeschke.neetutils.json.annotations.JSONIgnoreField;
import com.github.rjeschke.neetutils.json.annotations.JSONObject;
import com.github.rjeschke.neetutils.json.annotations.JSONReadOnlyField;

/**
 * JSON encoder, decoder and utilities.
 *
 * @author René Jeschke (rene_jeschke@yahoo.de)
 *
 */
public final class JSON
{
    private JSON()
    {
        // meh!
    }

    /**
     * Beautifies the given JSON string.
     *
     * @param sb
     *            The StringBuilder to write into.
     * @param json
     *            The String to beautify.
     * @return The submitted StringBuilder.
     * @throws IOException
     *             if an IO or parsing error occurred
     */
    public final static StringBuilder beautify(final StringBuilder sb, final String json) throws IOException
    {
        Reader reader = null;
        try
        {
            reader = new StringReader(json);
            final JSONTokenizer tokenizer = new JSONTokenizer(reader);
            tokenizer.next();
            while (tokenizer.getCurrentToken() != Token.EOF)
            {
                beautify(sb, 0, tokenizer);
                if (tokenizer.getCurrentToken() != Token.EOF)
                {
                    sb.append('\n');
                }
            }
            return sb;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Beautifies the given JSON string.
     *
     * @param json
     *            The String to beautify.
     * @return The beautified String.
     * @throws IOException
     *             if an IO or parsing error occurred
     */
    public final static String beautify(final String json) throws IOException
    {
        return json == null || json.isEmpty() ? "" : beautify(new StringBuilder(json.length()), json).toString();
    }

    /**
     * Decodes the given JSON string into an object.
     *
     * @param string
     *            The string to decode.
     * @return The decoded object.
     * @throws IOException
     *             if an IO or processing error occurred.
     */
    public final static Object decode(final String string) throws IOException
    {
        StringReader reader = null;
        try
        {
            reader = new StringReader(string);
            return decode(reader);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Decodes a JSON string read from the given {@link Reader} into an object.
     *
     * @param reader
     *            The reader to read from.
     * @return The decoded object.
     * @throws IOException
     *             if an IO or processing error occurred.
     */
    public final static Object decode(final Reader reader) throws IOException
    {
        final JSONTokenizer tokenizer = new JSONTokenizer(reader);

        tokenizer.next();

        final Object ret = readObject(tokenizer);

        if (tokenizer.getCurrentToken() != Token.EOF) throw new IOException("Multiple JSON values in string" + tokenizer.getPosition());

        return ret;
    }

    /**
     * Decodes a JSON string containing a single object into the given {@link JSONMarshallable}.
     *
     * @param string
     *            The string to decode.
     * @param object
     *            The JSONMarshallable.
     * @return The decoded {@code object}.
     * @throws IOException
     *             if an IO or processing error occurred.
     */
    public final static <T extends JSONMarshallable> T decodeInto(final String string, final T object) throws IOException
    {
        StringReader reader = null;
        try
        {
            reader = new StringReader(string);
            return decodeInto(reader, object);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Decodes a JSON string read from the given {@link Reader} containing a single object into the given {@link JSONMarshallable}.
     *
     * @param reader
     *            The reader to read from.
     * @param object
     *            The JSONMarshallable.
     * @return The decoded {@code object}.
     * @throws IOException
     *             if an IO or processing error occurred.
     */
    public final static <T extends JSONMarshallable> T decodeInto(final Reader reader, final T object) throws IOException
    {
        final Object obj = decode(reader);

        if (!Objects.isMap(obj)) throw new IOException("JSON value ist not of type 'object'.");

        return decodeInto(asMap(obj), object);
    }

    /**
     * Fills a {@link JSONMarshallable} using the given map.
     *
     * @param jsonObject
     *            The map.
     * @param object
     *            The JSONMarshallable.
     * @return The decoded {@code object}.
     * @throws IOException
     *             if an IO or processing error occurred.
     */
    public final static <T extends JSONMarshallable> T decodeInto(final Map<String, Object> jsonObject, final T object) throws IOException
    {
        Field catchAll = null;
        final Map<String, Object> rest = new HashMap<>();

        final Class<?> toClass = object.getClass();

        boolean ignoreNull = true;
        int vis = JSONObjectVisibility.PUBLIC;
        if (toClass.isAnnotationPresent(JSONObject.class))
        {
            final JSONObject v = toClass.getAnnotation(JSONObject.class);
            vis = v.visibility();
            ignoreNull = v.ignoreNull();
        }

        for (final Field f : toClass.getDeclaredFields())
        {
            if (f.isAnnotationPresent(JSONCatchAllField.class))
            {
                catchAll = f;
                break;
            }
        }

        for (final Entry<String, Object> e : jsonObject.entrySet())
        {
            try
            {
                final Field f = toClass.getDeclaredField(e.getKey());

                if (ignoreNull && e.getValue() == null) continue;

                if (isFieldVisible(f, vis, false))
                {
                    final boolean isAccessible = f.isAccessible();
                    f.setAccessible(true);
                    if (f.getType().isEnum())
                    {
                        Method m;
                        try
                        {
                            m = f.getType().getMethod("fromJSONString", String.class);
                        }
                        catch (final NoSuchMethodException ex)
                        {
                            m = f.getType().getMethod("valueOf", String.class);
                        }
                        f.set(object, m.invoke(null, e.getValue().toString()));
                    }
                    else
                    {
                        if (Classes.implementsInterface(f.getType(), JSONMarshallable.class))
                        {
                            f.set(object, decodeInto(asMap(e.getValue()), newJSONInstance(f.getType())));
                        }
                        else
                        {
                            if (f.isAnnotationPresent(JSONGenericType.class))
                            {
                                final Class<?> t = f.getAnnotation(JSONGenericType.class).type();

                                if (Classes.implementsInterface(f.getType(), Map.class))
                                {
                                    final Map<String, Object> in = asMap(e.getValue());
                                    final Map<String, Object> out = new HashMap<>();

                                    if (t.isEnum())
                                    {
                                        Method m;
                                        try
                                        {
                                            m = t.getMethod("fromJSONString", String.class);
                                        }
                                        catch (final NoSuchMethodException ex)
                                        {
                                            m = t.getMethod("valueOf", String.class);
                                        }
                                        for (final Entry<String, Object> e2 : in.entrySet())
                                        {
                                            out.put(e2.getKey(), m.invoke(null, e2.getValue().toString()));
                                        }
                                    }
                                    else
                                    {
                                        for (final Entry<String, Object> e2 : in.entrySet())
                                        {
                                            out.put(e2.getKey(), decodeInto(asMap(e2.getValue()), newJSONInstance(t)));
                                        }
                                    }

                                    f.set(object, out);
                                }
                                else if (Classes.implementsInterface(f.getType(), List.class))
                                {
                                    final List<Object> in = asArray(e.getValue());
                                    final List<Object> out = Colls.list();

                                    if (t.isEnum())
                                    {
                                        Method m;
                                        try
                                        {
                                            m = t.getMethod("fromJSONString", String.class);
                                        }
                                        catch (final NoSuchMethodException ex)
                                        {
                                            m = t.getMethod("valueOf", String.class);
                                        }
                                        for (final Object o : in)
                                        {
                                            out.add(m.invoke(null, o.toString()));
                                        }
                                    }
                                    else
                                    {
                                        for (final Object o : in)
                                        {
                                            out.add(decodeInto(asMap(o), newJSONInstance(t)));
                                        }
                                    }
                                    f.set(object, out);
                                }
                                else
                                {
                                    throw new IOException("Marshalling for type " + toClass + " failed for '" + e.getKey() + "'");
                                }
                            }
                            else
                            {
                                f.set(object, e.getValue());
                            }
                        }
                    }
                    f.setAccessible(isAccessible);
                }
                else
                {
                    rest.put(e.getKey(), e.getValue());
                }
            }
            catch (final NoSuchFieldException ex)
            {
                rest.put(e.getKey(), e.getValue());
            }
            catch (IllegalArgumentException | IllegalAccessException | ClassCastException | InvocationTargetException | SecurityException
                    | NoSuchMethodException ex)
            {
                throw new IOException("Marshalling for type " + toClass + " failed for '" + e.getKey() + "'", ex);
            }
        }

        if (catchAll != null)
        {
            try
            {
                final boolean isAccessible = catchAll.isAccessible();
                catchAll.setAccessible(true);
                catchAll.set(object, rest);
                catchAll.setAccessible(isAccessible);
            }
            catch (IllegalArgumentException | IllegalAccessException ex)
            {
                throw new IOException("Marshalling for type " + toClass + " failed for '" + catchAll.getName() + "'", ex);
            }
        }

        return object;
    }

    /**
     * Encodes the given object into a JSON string representation.
     *
     * @param obj
     *            The object to encode.
     * @return The JSON string representation.
     */
    public final static String encode(final Object obj)
    {
        return encode(new StringBuilder(), obj).toString();
    }

    /**
     * Encodes the given object into a JSON string representation.
     *
     * @param sb
     *            The {@link StringBuilder} to write the result into.
     * @param obj
     *            The object to encode.
     * @return The given StringBuilder.
     */
    public final static StringBuilder encode(final StringBuilder sb, final Object obj)
    {
        writeObject(sb, obj);
        return sb;
    }

    /**
     * Returns the given object casted to {@code Map<String, Object>}.
     *
     * @param obj
     * @return The given object casted to {@code Map<String, Object>}.
     */
    @SuppressWarnings("unchecked")
    public final static Map<String, Object> asMap(final Object obj)
    {
        return (Map<String, Object>)obj;
    }

    /**
     * Returns the given object casted to {@code List<Object>}.
     *
     * @param obj
     * @return The given object casted to {@code List<Object>}.
     */
    @SuppressWarnings("unchecked")
    public final static List<Object> asArray(final Object obj)
    {
        return (List<Object>)obj;
    }

    /**
     * Returns the given object casted to {@code Number}.
     *
     * @param obj
     * @return The given object casted to {@code Number}.
     */
    public final static Number asNumber(final Object obj)
    {
        return (Number)obj;
    }

    /**
     * Returns the given object casted to {@code Boolean}.
     *
     * @param obj
     * @return The given object casted to {@code Boolean}.
     */
    public final static Boolean asBoolean(final Object obj)
    {
        return (Boolean)obj;
    }

    /**
     * Escapes the given string.
     *
     * @param value
     * @return The escaped string.
     */
    public final static String escapeString(final String value)
    {
        return escapeString(new StringBuilder(), value).toString();
    }

    /**
     * Escapes the given string to the given StringBuilder.
     *
     * @param sb
     * @param value
     * @return The provided StringBuilder.
     */
    public final static StringBuilder escapeString(final StringBuilder sb, final String value)
    {
        for (int i = 0; i < value.length(); i++)
        {
            final char ch = value.charAt(i);
            switch (ch)
            {
            case '"':
                sb.append("\\\"");
                break;
            case '/':
                sb.append("\\/");
                break;
            case '\\':
                sb.append("\\\\");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\r':
                sb.append("\\r");
                break;
            case '\t':
                sb.append("\\t");
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\f':
                sb.append("\\f");
                break;
            default:
                if (ch < 32)
                {
                    sb.append(String.format("\\u%04x", (int)ch));
                }
                else
                {
                    sb.append(ch);
                }
            }
        }
        return sb;
    }

    private final static boolean isPublicVisibility(final Field f)
    {
        return (f.getModifiers() & Modifier.PUBLIC) != 0;
    }

    private final static boolean isPrivateVisibility(final Field f)
    {
        return (f.getModifiers() & Modifier.PRIVATE) != 0;
    }

    private final static boolean isDefaultVisibility(final Field f)
    {
        return (f.getModifiers() & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED)) == 0;
    }

    private final static boolean isProtectedVisibility(final Field f)
    {
        return (f.getModifiers() & Modifier.PROTECTED) != 0;
    }

    private final static boolean isFieldVisible(final Field f, final int vis, final boolean read)
    {
        if (f.isAnnotationPresent(JSONIgnoreField.class)) return false;
        if (f.isAnnotationPresent(JSONForceField.class)) return true;
        if (!read && f.isAnnotationPresent(JSONReadOnlyField.class)) return false;

        if (!read && (f.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) != 0) return false;

        if ((vis & JSONObjectVisibility.PRIVATE) != 0 && isPrivateVisibility(f)) return true;
        if ((vis & JSONObjectVisibility.DEFAULT) != 0 && isDefaultVisibility(f)) return true;
        if ((vis & JSONObjectVisibility.PROTECTED) != 0 && isProtectedVisibility(f)) return true;
        if ((vis & JSONObjectVisibility.PUBLIC) != 0 && isPublicVisibility(f)) return true;

        return false;
    }

    /**
     * Parses a JSON array.
     *
     * @param tokenizer
     *            The tokenizer.
     * @return The parsed array as a {@code List}
     * @throws IOException
     *             if an IO or parsing error occurred.
     */
    public final static List<Object> readArray(final JSONTokenizer tokenizer) throws IOException
    {
        final List<Object> list = Colls.list();

        for (;;)
        {
            final Token t = tokenizer.getCurrentToken();
            if (t == Token.ARRAY_CLOSE)
            {
                tokenizer.next();
                break;
            }

            if (t == Token.EOF) throw new IOException("Unexpected end of string for array" + tokenizer.getPosition());

            list.add(readObject(tokenizer));

            if (tokenizer.getCurrentToken() != Token.COMMA && tokenizer.getCurrentToken() != Token.ARRAY_CLOSE)
            {
                throw new IOException("',' or ']' expected" + tokenizer.getPosition());
            }
            if (tokenizer.getCurrentToken() == Token.COMMA) tokenizer.next();
        }

        return list;
    }

    /**
     * Parses a JSON object.
     *
     * @param tokenizer
     *            The tokenizer.
     * @return The parsed object as a {@code Map}
     * @throws IOException
     *             if an IO or parsing error occurred.
     */
    public final static Map<String, Object> readMap(final JSONTokenizer tokenizer) throws IOException
    {
        final Map<String, Object> map = new HashMap<>();
        for (;;)
        {
            final Token t = tokenizer.getCurrentToken();
            if (t == Token.OBJECT_CLOSE)
            {
                tokenizer.next();
                break;
            }

            if (t != Token.STRING) throw new IOException("Object key expected" + tokenizer.getPosition());
            final String key = tokenizer.getStringValue();
            if (Token.COLON != tokenizer.next()) throw new IOException("':' expected" + tokenizer.getPosition());
            tokenizer.next();
            map.put(key, readObject(tokenizer));
            if (tokenizer.getCurrentToken() != Token.COMMA && tokenizer.getCurrentToken() != Token.OBJECT_CLOSE)
            {
                throw new IOException("',' or '}' expected" + tokenizer.getPosition());
            }
            if (tokenizer.getCurrentToken() == Token.COMMA) tokenizer.next();
        }
        return map;
    }

    /**
     * Parses a JSON value into a Java object.
     *
     * @param tokenizer
     *            The tokenizer.
     * @return The parsed object.
     * @throws IOException
     *             * if an IO or parsing error occurred.
     */
    public final static Object readObject(final JSONTokenizer tokenizer) throws IOException
    {
        switch (tokenizer.getCurrentToken())
        {
        case OBJECT_OPEN:
            tokenizer.next();
            return readMap(tokenizer);
        case ARRAY_OPEN:
            tokenizer.next();
            return readArray(tokenizer);
        case NULL:
            tokenizer.next();
            return null;
        case TRUE:
            tokenizer.next();
            return Boolean.TRUE;
        case FALSE:
            tokenizer.next();
            return Boolean.FALSE;
        case STRING:
            tokenizer.next();
            return tokenizer.getStringValue();
        case LONG:
            tokenizer.next();
            return tokenizer.getLongValue();
        case DOUBLE:
            tokenizer.next();
            return tokenizer.getDoubleValue();
        default:
            throw new IOException("Unexpected token: " + tokenizer.getCurrentToken() + "," + tokenizer.getPosition());
        }
    }

    /**
     * Writes a {@code long}.
     *
     * @param sb
     *            {@code StringBuilder} to write to
     * @param value
     *            Value to write.
     */
    public final static void writeNumber(final StringBuilder sb, final long value)
    {
        sb.append(value);
    }

    /**
     * Writes a {@code double}.
     *
     * @param sb
     *            {@code StringBuilder} to write to
     * @param value
     *            Value to write.
     */
    public final static void writeNumber(final StringBuilder sb, final double value)
    {
        sb.append(value);
    }

    /**
     * Writes a {@code Number}.
     *
     * @param sb
     *            {@code StringBuilder} to write to
     * @param value
     *            Value to write.
     */
    public final static void writeNumber(final StringBuilder sb, final Number value)
    {
        if (value instanceof Double || value instanceof Float)
        {
            writeNumber(sb, value.doubleValue());
        }
        else
        {
            writeNumber(sb, value.longValue());
        }
    }

    /**
     * Writes a {@code String}.
     *
     * @param sb
     *            {@code StringBuilder} to write to
     * @param value
     *            Value to write.
     */
    public final static void writeString(final StringBuilder sb, final String value)
    {
        sb.append('"');
        escapeString(sb, value);
        sb.append('"');
    }

    /**
     * Writes a {@code Collection}.
     *
     * @param sb
     *            {@code StringBuilder} to write to
     * @param list
     *            Value to write.
     */
    public final static void writeList(final StringBuilder sb, final Collection<?> list)
    {
        boolean second = false;
        sb.append('[');
        for (final Object o : list)
        {
            if (second)
            {
                sb.append(',');
            }
            else
            {
                second = true;
            }
            writeObject(sb, o);
        }
        sb.append(']');
    }

    /**
     * Writes a {@code Map} as a JSON object.
     *
     * @param sb
     *            {@code StringBuilder} to write to
     * @param map
     *            Value to write.
     */
    public final static void writeMap(final StringBuilder sb, final Map<?, ?> map)
    {
        boolean second = false;
        sb.append('{');
        for (final Entry<?, ?> e : map.entrySet())
        {
            if (second)
            {
                sb.append(',');
            }
            else
            {
                second = true;
            }
            writeString(sb, e.getKey().toString());
            sb.append(':');
            writeObject(sb, e.getValue());
        }
        sb.append('}');
    }

    private final static boolean hasInterface(final Class<?> clazz, final Class<?> inter)
    {
        if (clazz == null) return false;
        for (final Class<?> c : clazz.getInterfaces())
        {
            if (c == inter) return true;
        }
        return false;
    }

    private final static void writeMarshallable(final StringBuilder sb, final Object obj)
    {
        boolean second = false;
        sb.append('{');

        try
        {
            Class<?> clazz = obj.getClass();
            for (;;)
            {
                boolean ignoreNull = true;
                int vis = JSONObjectVisibility.PUBLIC;
                if (clazz.isAnnotationPresent(JSONObject.class))
                {
                    final JSONObject v = clazz.getAnnotation(JSONObject.class);
                    vis = v.visibility();
                    ignoreNull = v.ignoreNull();
                }
                for (final Field f : clazz.getDeclaredFields())
                {
                    if (isFieldVisible(f, vis, true))
                    {
                        final boolean isAccessible = f.isAccessible();
                        f.setAccessible(true);

                        final Object value = f.get(obj);

                        if (value == null && ignoreNull) continue;

                        if (second)
                        {
                            sb.append(',');
                        }
                        else
                        {
                            second = true;
                        }

                        writeString(sb, f.getName());
                        sb.append(':');
                        writeObject(sb, value);
                        f.setAccessible(isAccessible);
                    }
                }

                clazz = clazz.getSuperclass();
                if (!hasInterface(clazz, JSONMarshallable.class)) break;
            }
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            throw new IllegalArgumentException("Failed to write marshallable of type: " + obj.getClass(), e);
        }

        sb.append('}');
    }

    /**
     * Writes an {@code Object}.
     *
     * @param sb
     *            {@code StringBuilder} to write to
     * @param obj
     *            Object to write.
     */
    public final static void writeObject(final StringBuilder sb, final Object obj)
    {
        if (obj == null)
        {
            sb.append("null");
        }
        else if (obj instanceof JSONMarshallable)
        {
            writeMarshallable(sb, obj);
        }
        else if (obj instanceof Map)
        {
            writeMap(sb, (Map<?, ?>)obj);
        }
        else if (obj instanceof Collection)
        {
            writeList(sb, (Collection<?>)obj);
        }
        else if (obj instanceof Number)
        {
            writeNumber(sb, (Number)obj);
        }
        else if (obj instanceof Boolean)
        {
            sb.append(((Boolean)obj).booleanValue() ? "true" : "false");
        }
        else if (obj instanceof JSONEnum)
        {
            writeString(sb, ((JSONEnum)obj).toJSONString());
        }
        else
        {
            writeString(sb, obj.toString());
        }
    }

    private final static StringBuilder indent(final StringBuilder sb, final int indent)
    {
        for (int i = 0; i < indent; i++)
            sb.append(' ');
        return sb;
    }

    private final static void beautify(final StringBuilder sb, final int indent, final JSONTokenizer tokenizer) throws IOException
    {
        switch (tokenizer.getCurrentToken())
        {
        case OBJECT_OPEN:
            sb.append("{\n");
            tokenizer.next();
            while (tokenizer.getCurrentToken() != Token.OBJECT_CLOSE)
            {
                if (tokenizer.getCurrentToken() != Token.STRING)
                    throw new IOException("Object key expected, got: " + tokenizer.getCurrentToken() + "," + tokenizer.getPosition());
                indent(sb, indent + 2);
                writeString(sb, tokenizer.getStringValue());
                sb.append(" : ");
                tokenizer.next();
                if (tokenizer.getCurrentToken() != Token.COLON) throw new IOException("':' expected" + tokenizer.getPosition());
                tokenizer.next();
                beautify(sb, indent + 2, tokenizer);
                if (tokenizer.getCurrentToken() == Token.COMMA)
                {
                    sb.append(',');
                    tokenizer.next();
                }
                sb.append('\n');
            }
            indent(sb, indent).append('}');
            tokenizer.next();
            break;
        case ARRAY_OPEN:
            sb.append("[\n");
            tokenizer.next();
            while (tokenizer.getCurrentToken() != Token.ARRAY_CLOSE)
            {
                beautify(indent(sb, indent + 2), indent + 2, tokenizer);
                if (tokenizer.getCurrentToken() == Token.COMMA)
                {
                    sb.append(',');
                    tokenizer.next();
                }
                sb.append('\n');
            }
            indent(sb, indent).append(']');
            tokenizer.next();
            break;
        case STRING:
            writeString(sb, tokenizer.getStringValue());
            tokenizer.next();
            break;
        case TRUE:
            sb.append("true");
            tokenizer.next();
            break;
        case FALSE:
            sb.append("false");
            tokenizer.next();
            break;
        case NULL:
            sb.append("null");
            tokenizer.next();
            break;
        case DOUBLE:
            writeNumber(sb, tokenizer.getDoubleValue());
            tokenizer.next();
            break;
        case LONG:
            writeNumber(sb, tokenizer.getLongValue());
            tokenizer.next();
            break;
        default:
            throw new IOException("Unexpected token: " + tokenizer.getCurrentToken() + "," + tokenizer.getPosition());
        }
    }

    private final static <T extends JSONMarshallable> T newJSONInstance(final Class<?> clazz) throws IOException
    {
        try
        {
            try
            {
                final Method m = clazz.getMethod("createJSONInstance");
                final boolean isAccessible = m.isAccessible();
                m.setAccessible(true);
                final Object ret = m.invoke(null);
                m.setAccessible(isAccessible);
                return Objects.uncheckedCast(ret);
            }
            catch (final NoSuchMethodException e)
            {
                final Constructor<?> ctor = clazz.getConstructor();
                final boolean isAccessible = ctor.isAccessible();
                ctor.setAccessible(true);
                final Object ret = ctor.newInstance();
                ctor.setAccessible(isAccessible);
                return Objects.uncheckedCast(ret);
            }
        }
        catch (InstantiationException | IllegalAccessException | SecurityException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException e)
        {
            throw new IOException("Marshalling for type " + clazz + " failed", e);
        }
    }
}
