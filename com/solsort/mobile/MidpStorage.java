package com.solsort.mobile;

import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import com.solsort.lightscript.Function;
import com.solsort.lightscript.LightScriptException;
import com.solsort.lightscript.Util;

public class MidpStorage {

    private Hashtable names;
    private String storeName;

    public Enumeration keys() {
        return names.keys();
    }
    public void set(String key, String value) throws LightScriptException {
        RecordStore records = openStore();
        try {
            int len = key.length() + value.length() + 1;
            byte data[] = (key + (char) 0 + value).getBytes();
            Object o = names.get(key);
            if (o != null) {
                int id = ((Integer) o).intValue();
                records.setRecord(id, data, 0, len);
            } else {
                int id = records.addRecord(data, 0, len);
                names.put(key, new Integer(id));
            }
        } catch (Exception ex) {
            this.closeStore(records);
            throw new LightScriptException(ex);
        }
        this.closeStore(records);
    }

    public String get(String key) throws LightScriptException {
        String result = null;
        Object o = names.get(key);
        if (o != null) {
            RecordStore records = openStore();
            try {
                int id = ((Integer) o).intValue();
                byte bytes[] = records.getRecord(id);
                int startpos = key.getBytes().length + 1;
                int length = bytes.length - startpos;
                result = new String(bytes, startpos, length);
            } catch (Exception ex) {
                this.closeStore(records);
                throw new LightScriptException(ex);
            }
            this.closeStore(records);
        }
        return result;
    }
    private static Hashtable stores = new Hashtable();

    public static MidpStorage openStorage(String name) throws LightScriptException {
        Object o = stores.get(name);
        if (o == null) {
            MidpStorage result;
            result = new MidpStorage(name);
            stores.put(name, result);
            return result;
        } else {
            return (MidpStorage) o;
        }
    }

    private MidpStorage(String storeName) throws LightScriptException {
        this.names = new Hashtable();
        this.storeName = storeName;
        if (Util.tupleIndexOf(RecordStore.listRecordStores(), storeName) != -1) {
            RecordStore records = openStore();
            try {
                RecordEnumeration iter = records.enumerateRecords(null, null, false);
                int ids[] = new int[10];
                int count = 0;
                while (iter.hasNextElement()) {
                    if (count >= ids.length) {
                        int t[] = new int[(count * 3) / 2];
                        System.arraycopy(ids, 0, t, 0, count);
                        ids = t;
                    }
                    ids[count] = iter.nextRecordId();
                    ++count;
                }
                for (int i = 0; i < count; ++i) {
                    byte record[] = records.getRecord(ids[i]);
                    int namelen = 0;
                    while (namelen < record.length && record[namelen] != 0) {
                        ++namelen;
                    }
                    char[] chars = new char[namelen];
                    for (int j = 0; j < namelen; ++j) {
                        chars[j] = (char) record[j];
                    }
                    names.put(String.valueOf(chars), new Integer(ids[i]));
                }
            } catch (Exception ex) {
                this.closeStore(records);
                throw new LightScriptException(ex);
            }
            this.closeStore(records);
        }
    }

    private RecordStore openStore() throws LightScriptException {
        try {
            return RecordStore.openRecordStore(storeName, true);
        } catch (Exception e) {
            throw new LightScriptException(e);
        }
    }

    private void closeStore(RecordStore r) throws LightScriptException {
        try {
            r.closeRecordStore();
        } catch (Exception e) {
            throw new LightScriptException(e);
        }
    }
}
