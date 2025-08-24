package kr.rtuserver.framework.bukkit.api.platform;

import com.google.common.io.ByteStreams;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileResource {

    public static File copy(Plugin plugin, String sourceFile) {
        File targetFolder = createFolder(plugin.getDataFolder());
        File resultFile = new File(targetFolder, sourceFile);
        if (!resultFile.exists()) {
            try {
                resultFile.createNewFile();
                InputStream in = plugin.getResource(sourceFile);
                OutputStream out = new FileOutputStream(resultFile);
                ByteStreams.copy(in, out);
                out.close();
                in.close();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Error copying content " + sourceFile, e);
            }
        }
        return resultFile;
    }

    public static File copy(Plugin plugin, String sourceFolder, String sourceFile) {
        File targetFolder = createFolder(plugin.getDataFolder() + "/" + sourceFolder);
        File resultFile = new File(targetFolder, sourceFile);
        if (!resultFile.exists()) {
            try {
                resultFile.createNewFile();
                InputStream in = plugin.getResource(sourceFolder + "/" + sourceFile);
                OutputStream out = new FileOutputStream(resultFile);
                ByteStreams.copy(in, out);
                out.close();
                in.close();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Error copying file " + sourceFolder + "/" + sourceFile, e);
            }
        }
        return resultFile;
    }

    public static File copyResource(Plugin plugin, String sourceFolder, String sourceFile) {
        File targetFolder = createFolder(plugin.getDataFolder() + "/" + sourceFolder);
        File resultFile = new File(targetFolder, sourceFile);
        if (resultFile.exists()) return resultFile;
        try {
            InputStream in = plugin.getResource(sourceFolder + "/" + sourceFile);
            if (in == null) return null;
            if (resultFile.createNewFile()) {
                OutputStream out = new FileOutputStream(resultFile);
                ByteStreams.copy(in, out);
                out.close();
                in.close();
                return resultFile;
            }
            return null;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Error copying file " + sourceFolder + "/" + sourceFile, e);
            return null;
        }
    }

    public static File createFileCopy(Plugin plugin, String sourceFolder, String sourceFile) {
        File targetFolder = createFolder(plugin.getDataFolder() + "/" + sourceFolder);
        File resultFile = new File(targetFolder, sourceFile);
        if (resultFile.exists()) return resultFile;
        try {
            resultFile.createNewFile();
            InputStream in = plugin.getResource(sourceFolder + "/" + sourceFile);
            if (in == null) return resultFile;
            OutputStream out = new FileOutputStream(resultFile);
            ByteStreams.copy(in, out);
            out.close();
            in.close();
            return resultFile;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Error copying file " + sourceFolder + "/" + sourceFile, e);
            return null;
        }
    }

    public static File getResourceWithoutNew(String folder, String file) {
        File resource = new File(folder, file);
        if (!resource.exists()) {
            return null;
        } else {
            return resource;
        }
    }

    public static File createFile(String folder, String file) {
        File resourceFolder = new File(folder);
        if (!resourceFolder.exists()) {
            try {
                resourceFolder.mkdirs();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Error creating folder " + folder, e);
            }
        }
        File resourceFile = new File(folder, file);
        if (!resourceFile.exists()) {
            try {
                resourceFile.createNewFile();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Error creating file " + file, e);
            }
        }
        return resourceFile;
    }

    public static File createFolder(String folder) {
        File resourceFolder = new File(folder);
        if (!resourceFolder.exists()) {
            try {
                resourceFolder.mkdirs();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Error creating folder " + folder, e);
            }
        }
        return resourceFolder;
    }

    public static File createFolder(File folder) {
        if (!folder.exists()) {
            try {
                folder.mkdirs();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Error creating folder " + folder, e);
            }
        }
        return folder;
    }

    public static File createFile(File folder, String file) {
        if (!folder.exists()) {
            try {
                folder.mkdirs();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Error creating folder " + folder, e);
            }
        }
        File resourceFile = new File(folder, file);
        if (!resourceFile.exists()) {
            try {
                resourceFile.createNewFile();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Error creating file " + file, e);
            }
        }
        return resourceFile;
    }

}
