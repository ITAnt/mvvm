package com.miekir.mvvm.tools;

import com.miekir.mvvm.log.L;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author : zzc
 * @date : 2021/8/12 17:59
 */
public class ObjectTools {
    private ObjectTools() {}
    /**
     * 对象深度复制(对象必须是实现了Serializable接口)
     * @param obj 要复制的对象
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T clone(T obj) {
        T clonedObj = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            clonedObj = (T) ois.readObject();
            ois.close();
        } catch (Exception e) {
            L.e(e.getMessage());
        }
        return clonedObj;
    }
}
