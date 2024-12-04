package sap.ass02.configurationserver.domain;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurationFilesObserver {

    private ConfigurationSharer sharer;
    private final Map<String, String> previousFileContents = new ConcurrentHashMap<>();

    public void start(ConfigurationSharer cs) {
        new Thread(() -> {
            this.sharer = cs;
            Path dir = Paths.get("/Configurations");

            try {
                System.out.println("Reading existing files in the directory:");
                Files.walk(dir)
                        .filter(file -> Files.isRegularFile(file) &&
                                file.toString().endsWith(".json"))
                        .forEach(file -> {
                            System.out.println("Found file: " + file);
                            try {
                                String fileName = file.getFileName().toString();

                                String content = Files.readString(file);

                                previousFileContents.put(fileName, content);

                                this.sharer.addConfiguration(fileName, content);

                            } catch (IOException e) {
                                System.err.println("Error reading file: " + file);
                                e.printStackTrace();
                            }
                        });

            } catch (IOException e) {
                e.printStackTrace();
            }

            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

                dir.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);

                System.out.println("Watching directory: " + dir);

                while (true) {
                    WatchKey key = watchService.take();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        WatchEvent<Path> watchEvent = (WatchEvent<Path>) event;
                        Path fileName = watchEvent.context();

                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            System.out.println("File created: " + fileName);
                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {

                            try {
                                String fileN = fileName.getFileName().toString();

                                String content = Files.readString(dir.resolve(fileName));

                                if (previousFileContents.get(fileN).compareTo(content) != 0) {

                                    System.out.println("File modified: " + fileName);

                                    this.sharer.updateConfiguration(fileN, content);
                                    previousFileContents.remove(fileN);
                                    previousFileContents.put(fileN, content);
                                }

                            } catch (IOException e) {
                                System.err.println("Error reading file: " + fileName);
                                e.printStackTrace();
                            }
                        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            System.out.println("File deleted: " + fileName);
                        }
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
