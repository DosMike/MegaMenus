package de.dosmike.sponge.megamenus.api.state;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * This is a generic state that can be attached to a menu. It is meant to store smaller data.
 * Generally these states do not persis, but you can get and set them to menus at any time.
 */
final public class StateObject implements Serializable {

    private Map<String, Serializable> state = new HashMap<>();

    /**
     * Set a value to the specified key. Keys are case-insesitive.
     * @param key the string key to save a value to
     * @param value the value to associate with the key
     */
    public void set(String key, Serializable value) {
        state.put(key.toLowerCase(), value);
    }

    /**
     * Retrieve a value for a key not casted to it's actual type.
     * @param key the string key to read a value from
     * @return the value as Serializable if present
     */
    public Optional<Serializable> get(String key) {
        return Optional.ofNullable(state.get(key.toLowerCase()));
    }

    public Optional<Byte> getByte(String key) {
        return getOfClass(key, Byte.class);
    }
    public Optional<Short> getShort(String key) {
        return getOfClass(key, Short.class);
    }
    public Optional<Integer> getInt(String key) {
        return getOfClass(key, Integer.class);
    }
    public Optional<Long> getLong(String key) {
        return getOfClass(key, Long.class);
    }
    public Optional<Float> getFloat(String key) {
        return getOfClass(key, Float.class);
    }
    public Optional<Double> getDouble(String key) {
        return getOfClass(key, Double.class);
    }
    public Optional<String> getString(String key) {
        return getOfClass(key, String.class);
    }
    public Optional<UUID> getUUID(String key) {
        return getOfClass(key, UUID.class);
    }
    public Optional<BigInteger> getBigInteger(String key) {
        return getOfClass(key, BigInteger.class);
    }
    public Optional<BigDecimal> getBigDecimal(String key) {
        return getOfClass(key, BigDecimal.class);
    }

    /**
     * Removes all entries from the internal map
     */
    public void clear() {
        state.clear();
    }

    /**
     * Retrieve a value for a key and try to cast if to the specified type.
     * @param key the string key to read a value from
     * @return the value as T
     * @throws ClassCastException probably
     */
    public <T> Optional<T> getOfClass(String key, Class<T> type) {
        Object o = state.get(key.toLowerCase());
        if (type.isInstance(o)) {
            return Optional.of((T)o);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Convert this StateObject into an JsonObject
     * @return this as Json
     */
    public JsonElement toJson() {
        JsonObject root = new JsonObject();

        for (Map.Entry<String, Serializable> e : state.entrySet()) {
            String key = e.getKey();
            Serializable value = e.getValue();
            if (value instanceof BigInteger) {
                root.addProperty("BI:"+key, (Number)value);
            } else if (value instanceof BigDecimal) {
                root.addProperty("BD:"+key, (Number)value);
            } else if (value instanceof Boolean) {
                root.addProperty("t:"+key, (Boolean)value);
            } else if (value instanceof Byte) {
                root.addProperty("b:"+key, (Number)value);
            } else if (value instanceof Short) {
                root.addProperty("s:"+key, (Number)value);
            } else if (value instanceof Integer) {
                root.addProperty("i:"+key, (Number)value);
            } else if (value instanceof Long) {
                root.addProperty("l:"+key, (Number)value);
            } else if (value instanceof Float) {
                root.addProperty("f:"+key, (Number)value);
            } else if (value instanceof Double) {
                root.addProperty("d:"+key, (Number)value);
            } else if (value instanceof String) {
                root.addProperty("S:"+key, (String)value);
            } else if (value instanceof UUID) {
                root.addProperty("U:"+key, ((UUID)value).toString());
            } else {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    root.addProperty("O:" + key, new String(Base64.getEncoder().encode(baos.toByteArray())));
                } catch (IOException except) {
                    throw new RuntimeException("Could not serialize State to Json", except);
                }
            }
        }
        return root;
    }

    /**
     * Create a new StateObject from Json
     * @return the restored StateObject
     */
    public static StateObject fromJson(JsonObject object) {
        StateObject result = new StateObject();
        object.entrySet().forEach(e->{
            int i = e.getKey().indexOf(':');
            if (i<1) throw new RuntimeException("Could not deserialize State from Json, not type information");
            String type = e.getKey().substring(0, i);
            String name = e.getKey().substring(i+1).toLowerCase();
            if (type.equals("b")) {
                result.set(name, e.getValue().getAsByte());
            } else if (type.equals("t")) {
                result.set(name, e.getValue().getAsBoolean());
            } else if (type.equals("s")) {
                result.set(name, e.getValue().getAsShort());
            } else if (type.equals("i")) {
                result.set(name, e.getValue().getAsInt());
            } else if (type.equals("l")) {
                result.set(name, e.getValue().getAsLong());
            } else if (type.equals("f")) {
                result.set(name, e.getValue().getAsFloat());
            } else if (type.equals("d")) {
                result.set(name, e.getValue().getAsDouble());
            } else if (type.equals("BI")) {
                result.set(name, e.getValue().getAsBigInteger());
            } else if (type.equals("BD")) {
                result.set(name, e.getValue().getAsBigDecimal());
            } else if (type.equals("S")) {
                result.set(name, e.getValue().getAsString());
            } else if (type.equals("O")) {
                try {
                    byte[] buffer = Base64.getDecoder().decode(e.getValue().getAsString());
                    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    result.set(name, (Serializable) ois.readObject());
                } catch (Exception exception) {
                    throw new RuntimeException("Could not deserialize State from Json, Object corrupted", exception);
                }
            }
        });
        return result;
    }

    /**
     * @return a shallow copy of this StateObject.
     * @see <a href="https://en.wikipedia.org/wiki/Object_copying#Shallow_copy">Wikipedia # Shallow copy</a>
     */
    public StateObject copy() {
        StateObject copy = new StateObject();
        copy.state.putAll(state);
        return copy;
    }
}
