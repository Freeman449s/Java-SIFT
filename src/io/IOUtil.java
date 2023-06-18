package io;

import core.KeyPointX;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;

public class IOUtil {
    /**
     * 向文件写入KeyPointX列表。该方法实际上会写入一个ArrayList<SerializableKeyPointX>对象。
     *
     * @param keyPointXes KeyPointX列表
     * @param filePath    文件路径
     * @param overwrite   是否允许覆盖
     * @throws IOException 当存在以下情况时，抛出IOException：写入文件已经存在但不允许覆盖；文件的上级路径不存在且无法被创建；其他Java内置API可能抛出的异常。
     */
    public static void writeKeyPointXes(ArrayList<KeyPointX> keyPointXes, String filePath, boolean overwrite) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            if (!overwrite) throw new FileAlreadyExistsException(filePath);
        } else { // !file.exists()
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs()) { // 尝试创建上级目录
                throw new IOException("The folder does not exist and the program failed to make such a folder.");
            }
        }

        ArrayList<SerializableKeyPointX> serializableKeyPointXes = new ArrayList<>();
        for (KeyPointX keyPointX : keyPointXes)
            serializableKeyPointXes.add(new SerializableKeyPointX(keyPointX));
        try (ObjectOutputStream outputStream =
                     new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) { // FileOutputStream会尝试创建不存在的文件；当文件已存在时，原来的内容会被清空
            outputStream.writeObject(serializableKeyPointXes);
        }
    }

    /**
     * 从文件读取KeyPointX列表。
     *
     * @param filePath 文件路径
     * @return KeyPointX列表
     * @throws IOException            在读取过程中发生IO异常
     * @throws ClassNotFoundException 需要反序列化的类（ArrayList<SerializableKeyPointX>）尚未加载
     */
    public static ArrayList<KeyPointX> readKeyPointXes(String filePath) throws IOException, ClassNotFoundException {
        File file = new File(filePath);
        ArrayList<KeyPointX> ret;
        ArrayList<SerializableKeyPointX> serializableKeyPointXes;
        try (ObjectInputStream inputStream =
                     new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            serializableKeyPointXes = (ArrayList<SerializableKeyPointX>) inputStream.readObject();
        }
        ret = new ArrayList<>(serializableKeyPointXes.size());
        for (SerializableKeyPointX serializableKeyPointX : serializableKeyPointXes) {
            ret.add(SerializableKeyPointX.toKeyPointX(serializableKeyPointX));
        }
        return ret;
    }
}
